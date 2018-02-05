package com.imaginelab.sdn_icf.containers;

import java.util.HashMap;
// some switches can have multiple logical bridges, br0, br1 etc.
// this basic container needs some adjustments to keep multiple local switch ports

public class  Datapath {
	// -------------------------------- Node Datatype Properties -----------------------------------
	private String resourceURI 		= "S-0";						// static fill: 100, 200, 300, ...
	private String resourceName 	= "switchName";
	private String resLocation 		= "Rack";
	private String resModel 		= "PicaOrHP";
	private int switchNum 			= 0;	
		
	// -------------------------- Compute Component Datatype Properties ----------------------------
	private int managementCpu 		= 666;							// Hp E5400zl Management module cpu
	private long managementMemory 	= 256;							// Hp E5400zl Management module DDR SDRAM
	
	// ------------------------ Switching Component Datatype Properties ----------------------------
	private double dataPlaneCpu 	= 200.0;						// Hp E5400zl 1G module cpu
	private long dataPlaneMemory 	= 144000;						// Hp E5400zl 1G module memory
	private int flowTableSize 		= 1000;							// static define: pica8 = 2000, HP = 1500per card
	private double latency 			= 3.7;							// 1000Mb latency
	private double maxThroughput 	= 282.1;						// up to 282.1 million pps
	private double switchingSpeed 	= 379.2;						// Switching fabric speed = 379.2 Gbps
	
	// ------------------------ classes related by Objecttype Properties ---------------------------- 
	private Intfs[] intfsObjArray 	= null;
	
	//--------------------------- Attributes not published in PR_Pool -------------------------------
								// switchNum = 1,2,3, ... Assigned based on the order of switchs in the Flowvisor reply
	private String dpid 			= "empty";						// filled
	private String connection 		= "empty";						// filled
	private HashMap <String, Integer> flowmodUsageMap;				//filled
	
///===========================================================================================================
///---------------------------------------- getters and setters ----------------------------------------------
///===========================================================================================================
	
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resource_id) {
		this.resourceURI = resource_id;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public String getResLocation() {
		return resLocation;
	}
	public void setResLocation(String location) {
		this.resLocation = location;
	}
	public String getResModel() {
		return resModel;
	}
	public void setResModel(String resModel) {
		this.resModel = resModel;
	}
	public int getSwitchNum() {
		return switchNum;
	}
	public void setSwitchNum(int switchNum) {
		this.switchNum = switchNum;
	}
	/*public String getInterfaceType() {
		return interfaceType;
	}
	public void setInterfaceType(String interfaceType) {
		this.interfaceType = interfaceType;
	}*/
	public double getDataPlaneCpu() {
		return dataPlaneCpu;
	}
	public void setDataPlaneCpu(float cpu) {
		this.dataPlaneCpu = cpu;
	}
	public long getDataPlaneMemory() {
		return dataPlaneMemory;
	}
	public void setDataPlaneMemory(long memory) {
		this.dataPlaneMemory = memory;
	}
	public int getManagementCpu() {
		return managementCpu;
	}
	public void setManagementCpu(int managementCpu) {
		this.managementCpu = managementCpu;
	}
	public long getManagementMemory() {
		return managementMemory;
	}
	public void setManagementMemory(long managementMemory) {
		this.managementMemory = managementMemory;
	}
	public String getDpid() {
		return dpid;
	}
	public void setDpid(String dpid) {
		this.dpid = dpid;
	}
	public int getFlow_table_size() {
		return flowTableSize;
	}
	public void setFlow_table_size(int flow_table_size) {
		this.flowTableSize = flow_table_size;
	}
	/*public String getIp_address() {
		return ipAddress;
	}
	public void setIp_address(String ip_address) {
		this.ipAddress = ip_address;
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
	}*/
	public double getLatency() {
		return latency;
	}
	public void setLatency(double latency) {
		this.latency = latency;
	}
	public double getMaxThroughput() {
		return maxThroughput;
	}
	public void setMaxThroughput(double maxThroughput) {
		this.maxThroughput = maxThroughput;
	}
	public double getSwitchingSpeed() {
		return switchingSpeed;
	}
	public void setSwitchingSpeed(double switchingSpeed) {
		this.switchingSpeed = switchingSpeed;
	}
	public Intfs[] getIntfsObjArray() {
		return intfsObjArray;
	}
	public void setIntfsObjArray(Intfs[] intfsObjArray) {
		this.intfsObjArray = intfsObjArray;
	}
	public String getConnection() {
		return connection;
	}
	public void setConnection(String connection) {
		this.connection = connection;
	}
	public HashMap <String, Integer> getFlowmodUsageMap() {
		return flowmodUsageMap;
	}
	public void setFlowmodUsageMap(HashMap <String, Integer> flowmodUsageMap) {
		this.flowmodUsageMap = flowmodUsageMap;
	}
	/*public Port[] getPortArray() {
		return portArray;
	}
	public void setPortArray(Port[] portArray) {
		this.portArray = portArray;
	}*/
	
	
	
}
