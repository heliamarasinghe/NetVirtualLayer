package com.imaginelab.sdn_icf.containers;

import java.util.ArrayList;

public class Intfs {
	private String resourceURI = "H-0_VM-0_I-0";
	private String resourceName = "none";
	// Following information corresponds to <interface /> in domain.xml
	/*
	<interface type='bridge'>
      <virtualport type='openvswitch'/>
      <mac address='52:44:01:28:d8:ef'/>
      <source network='ovsbr0'/>
      <target dev='ovsintfs99-vm99'/>
      <vlan>
        <tag id='2001'/>
      </vlan>
      <model type='virtio'/>
      <address type='pci' domain='0x0000' bus='0x00' slot='0x03' function='0x0'/>
    </interface>
	  
	 */
	private String interfaceType = "empty";				// bridge
	private String virtualPortType = "none";			// <virtualport type='openvswitch'>
	private String macAddress = "empty";				// guest interface mac address
	private String sourceNetwork = "empty";				// only in definedDomain XML <source network='ovsbr0'/> 
	private String targetOvsIntfs = "empty";				// only in definedDomain XML <source network='ovsbr0'/>
	private String modelType = "none";					// <model type='virtio'/> Only available for defined domains
	
	
	
	
	private int numPorts = 0;							// number of ports used
	private int totalPorts = 0;
	private String ipAddress = "empty";					// IP address is constantly changing  
	
	
	//private String model = "none";						// <model type='virtio'/> Only available for defined domains
	//private String model = "none";						// <model type='virtio'/> Only available for defined domains
	
	private ArrayList<String> connectedIntfsUriList = new ArrayList<String>();	
	private Port[] portObjArray = null;
	
	
	public Intfs(String resourceURI){
		this.resourceURI = resourceURI;
	}
	
///===========================================================================================================
///---------------------------------------- getters and setters ----------------------------------------------
///===========================================================================================================
	public String getResourceURI() {
		return resourceURI;
	}

	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getMacAddress() {
		return macAddress;
	}
	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}
	public String getInterfaceType() {
		return interfaceType;
	}
	public void setInterfaceType(String interfaceType) {
		this.interfaceType = interfaceType;
	}
	public String getSourceNetwork() {
		return sourceNetwork;
	}
	public void setSourceNetwork(String sourceNetwork) {
		this.sourceNetwork = sourceNetwork;
	}
	public String getTargetOvsIntfs() {
		return targetOvsIntfs;
	}

	public void setTargetOvsIntfs(String targetOvsIntfs) {
		this.targetOvsIntfs = targetOvsIntfs;
	}

	public int getNumPorts() {
		return numPorts;
	}
	public void setNumPorts(int numPorts) {
		this.numPorts = numPorts;
	}
	public int getTotalPorts() {
		return totalPorts;
	}
	public void setTotalPorts(int totalPorts) {
		this.totalPorts = totalPorts;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
		
	}
	public Port[] getPortObjArray() {
		return portObjArray;
	}
	public void setPortObjArray(Port[] portObjArray) {
		this.portObjArray = portObjArray;
	}
	public String getVirtualPortType() {
		return virtualPortType;
	}
	public void setVirtualPortType(String virtualPortType) {
		this.virtualPortType = virtualPortType;
	}
	public String getModelType() {
		return modelType;
	}
	public void setModelType(String model) {
		this.modelType = model;
	}

	public ArrayList<String> getConnectedIntfsUriList() {
		return connectedIntfsUriList;
	}
	public void setConnectedIntfsURI(String connectedIntfsURI) {
		connectedIntfsUriList.add(connectedIntfsURI);
	}
	
	
	
	
}
