#!/usr/bin/python

"""
Create a 64-host network, and run the CLI on it.
If this fails because of kernel limits, you may have
to adjust them, e.g. by adding entries to /etc/sysctl.conf
and running sysctl -p. Check util/sysctl_addon.
"""

from mininet.cli import CLI
from mininet.log import setLogLevel
from mininet.node import OVSSwitch, RemoteController
from mininet.topolib import TreeNet
from mininet.net import Mininet
from mininet.topolib import TreeTopo

def simpleTree():
    network = TreeNet( depth=2, fanout=2, switch=OVSSwitch )
    network.run( CLI, network )

def connectCtrl():
    topology = TreeTopo( depth=2, fanout=2 )
    #net = Mininet( topo=topology, controller=lambda name: RemoteController( name, defaultIP='192.168.0.202' ), listenPort=6634 )
    net = Mininet(topo=topology, controller= lambda x:RemoteController(x, '192.168.0.202', 6633), listenPort=6633, xterms=False, autoSetMacs=True)
    net.start()
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel( 'info' )
    #simpleTree()
    connectCtrl()
    
