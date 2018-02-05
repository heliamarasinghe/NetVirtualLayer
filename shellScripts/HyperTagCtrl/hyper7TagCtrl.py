#---------------------------------------------------------------------------------------------------------------	#
# This file id designed for kvm-host7 pox controller							     	#
# If you are using this for any other hypervisor controller, manually change trunk port 	#
# 	by checking 'patchintfs' from 'ovs-ovctl show ovsbr0'							#
#---------------------------------------------------------------------------------------------------------------	#
from pox.core import core
import pox.openflow.libopenflow_01 as of
from pox.lib.addresses import IPAddr, EthAddr
from pox.lib.util import dpid_to_str
from pox.lib.util import str_to_bool
from pox.lib.packet.ipv4 import ipv4
import pox.lib.packet as pkt
import time

log = core.getLogger()

_flood_delay = 0

class TagSwitch (object):

  def __init__ (self, connection, transparent):
    self.connection = connection
    self.transparent = transparent
    # Mac to Port Table
    self.macToPort = {}
    # listen to the connection for PacketIn messages
    connection.addListeners(self)
    self.hold_down_expired = _flood_delay == 10
    #self.floodCount = 0

  #Handle packet in messages from the switch
  def _handle_PacketIn (self, event):
    trunk = 1		# Change trunk port by manually checking ovs-ofctl 
    macToVlanFile = 'macToVlan7.txt'
    packet = event.parsed
    match = of.ofp_match.from_packet(packet)
    ip_packet = packet.find(pkt.ipv4)

    def flood (message = None):
      """ Floods the packet """
      msg = of.ofp_packet_out()
      if time.time() - self.connection.connect_time >= _flood_delay:

        if self.hold_down_expired is False:
          self.hold_down_expired = True
          log.info("%s: Flood hold-down expired -- flooding",
              dpid_to_str(event.dpid))

        if message is not None: log.debug(message)

        print("stripping vlan tag before flooding")
        msg.actions.append(of.ofp_action_strip_vlan())
        msg.actions.append(of.ofp_action_output(port = of.OFPP_FLOOD))
      else:
        pass
      msg.data = event.ofp
      msg.in_port = event.port
      self.connection.send(msg)

    
    def drop (duration = None):
      """
      Drops this packet and optionally installs a flow to continue
      dropping similar ones for a while
      """
      if duration is not None:
        if not isinstance(duration, tuple):
          duration = (duration,duration)
        msg = of.ofp_flow_mod()
        msg.match = of.ofp_match.from_packet(packet)
        msg.idle_timeout = duration[0]
        msg.hard_timeout = duration[1]
        msg.buffer_id = event.ofp.buffer_id
        self.connection.send(msg)
      elif event.ofp.buffer_id is not None:
        msg = of.ofp_packet_out()
        msg.buffer_id = event.ofp.buffer_id
        msg.in_port = event.port
        self.connection.send(msg)


    # get VLAN  tag corresponding to MAC address --------------------------------
    def getTag (mac = None):
      """
      Read macToVlan.txt file and returns vlan tag corresponding to mac address
      """
      macToVlan = {}
      with open(macToVlanFile, 'r') as file:
        data = file.readlines()
        for line in data:
          x=line.split(',')
          macToVlan[x[0]] = x[1].rstrip()
      if macToVlan.has_key(mac):
        log.debug("\tmac %s found in file" %(mac))
        return macToVlan[mac]
      else:
        log.debug("\tmac %s not in file" %(mac))
        return None
        

    def getMacsForTag (matchTag):
      vlanMacValList = {}
      with open('macToVlan.txt', 'r') as file:
        data = file.readlines()
        for line in data:
          x=line.split(',')
          vlanMacValList.setdefault(x[1].rstrip(),[]).append(x[0])
      print('\tvlanMacValList')
      print vlanMacValList

      if vlanMacValList.has_key(matchTag):
        log.debug("\ttag %s found in file" %(matchTag))
        print vlanMacValList[matchTag]
        return vlanMacValList[matchTag]
      else:
        log.debug("\ttag %s not in file" %(matchTag))
        return None

    # Following will give a key error if 'mac' is not in 'macToPort' table
    def floodToVlan (matchTag, message = None):
      """ Floods the packet to VMs in given VLAN"""
      msg = of.ofp_packet_out()
      if time.time() - self.connection.connect_time >= _flood_delay:
        # Only flood if we've been connected for a little while...

        if self.hold_down_expired is False:
          # Oh yes it is!
          self.hold_down_expired = True
          log.info("%s: Flood hold-down expired -- flooding",
              dpid_to_str(event.dpid))

        if message is not None: log.debug(message)
        # get a list of MAC addresses that belong to VLAN given by 'matchTag'
        vlanMacList = getMacsForTag (str(matchTag))
        if vlanMacList is not None:
          print("stripping vlan tag before flooding")
          msg.actions.append(of.ofp_action_strip_vlan())
          for mac in vlanMacList:
            msg.actions.append(of.ofp_action_output(port = self.macToPort[mac]))
        else:
          log.debug('\tNo VMs identified as part of VLAN %s' %matchTag)
          drop(30)
      else:
        pass
        #log.info("Holding down flood for %s", dpid_to_str(event.dpid))
      msg.data = event.ofp
      msg.in_port = event.port
      self.connection.send(msg)


 # Tagging switch operation -------------------------------------------------

    # Pretty print packets
    pktType = pkt.ETHERNET.ethernet.getNameForType(packet.type)
    print('\ntype=%s  in-port=%s  dl_vlan=%s  dl_src=%s  dl_dst=%s' %(pktType, event.port, match.dl_vlan, match.dl_src, match.dl_dst))

    # Filter LLDP and bridge filtered traffic
    if packet.type == packet.LLDP_TYPE or packet.dst.isBridgeFiltered():
      print('\tLLDP or BridgeFiltered traffic -- drop(60)')
      drop(60)
    else:
      # if packet is coming from trunk
      if event.port == trunk:        
        matchTag = match.dl_vlan
        log.debug("\tvlan tag incomig trunk = %s" %(matchTag))
        if int(matchTag) == 65535:
          log.debug('\tun-tagged packet from trunk -- drop(30)')
          drop(30)
        elif match.dl_dst.toStr() == 'ff:ff:ff:ff:ff:ff':
          #floodToVlan(matchTag, '\tBroadcast received from Trunk')
          flood('\tBroadcast received from Trunk  -- flood')
        else:
          # get VLAN ID corresponding to dl_dst
          tagFrmFile = getTag(match.dl_dst.toStr())
          print ("tagFrmFile = %s \t matchTag = %s" %(tagFrmFile,matchTag))
          #print "match.dl_vlan = "+match.dl_vlan.toStr()
          if tagFrmFile == str(matchTag):
            if packet.dst not in self.macToPort: # 4
              flood("\tPort for %s unknown -- flooding" % (packet.dst,)) # 4a
            else:
              toPort = self.macToPort[packet.dst]
              log.debug("\tinstalling flow for packets coming from trunk --->")
              log.debug("\tstriped-vlan=%i  src=%s.%i  dst=%s.%i" % (matchTag, packet.src, event.port, packet.dst, toPort))
              msg = of.ofp_flow_mod()
              msg.match = of.ofp_match.from_packet(packet, event.port)
              msg.idle_timeout = 30
              msg.hard_timeout = 60
              msg.actions.append(of.ofp_action_strip_vlan())
              msg.actions.append(of.ofp_action_output(port = toPort))
              msg.data = event.ofp # 6a
              self.connection.send(msg)
          else:	# <-- tag from file not equal to incoming tag for destination MAC 
            log.debug('\ttag mismatch packet from trunk -- drop(30)')
            drop(30)
       # if packet is coming from local VM
      else:
        # Learn MAC and Port of all local incoming frames
        self.macToPort[packet.src] = event.port

        matchTag = match.dl_vlan
        if matchTag != 65535:
          log.debug('\ttagged packet from VMs -- drop(30)')
          drop(30)
        else:
          tag = getTag(match.dl_src.toStr())
          if tag is None:
            log.debug('\tNo tag found in file for packet coming from %s -- drop(5)' %match.dl_src)
            drop(5)
          else:
            log.debug('\tlocal VM packet from %s -- set tag=%s forward to trunk=%i' %(match.dl_src, tag, trunk))
            msg = of.ofp_flow_mod()
            msg.match = of.ofp_match.from_packet(packet, event.port)
            msg.idle_timeout = 30
            msg.hard_timeout = 60
            msg.actions.append(of.ofp_action_vlan_vid(vlan_vid=int(tag)))
            msg.actions.append(of.ofp_action_output(port = trunk))
            msg.data = event.ofp # 6a
            self.connection.send(msg)


    print '\tMAC to Port table'
    for mac,port in self.macToPort.items():
      print "\t\t", mac, "-->", port



class CreateTagSwitch (object):
#Waits for OpenFlow switches to connect
  def __init__ (self, transparent):
    core.openflow.addListeners(self)
    self.transparent = transparent

  def _handle_ConnectionUp (self, event):
    log.debug("Connection %s" % (event.connection,))
    TagSwitch(event.connection, self.transparent)


def launch (transparent=False, hold_down=_flood_delay):
  """
  Starts an L2 learning switch.
  """
  try:
    global _flood_delay
    _flood_delay = int(str(hold_down), 10)
    assert _flood_delay >= 0
  except:
    raise RuntimeError("Expected hold-down to be a number")

  core.registerNew(CreateTagSwitch, str_to_bool(transparent))
