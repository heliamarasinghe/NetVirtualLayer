package com.imaginelab.sdn_icf.containers;

import java.util.HashMap;

public class VmHost {
	// -------------------------------- Node Datatype Properties -----------------------------------
	private String resourceURI 		= "H-0";
	private String resourceName 	= "empty";
	private String resLocation 		= "Rack";
	private String resModel 		= "Dell";
	private String nodeClass 		= "empty";
	private int vmHostNum 			= 0;
		
	// -------------------------- Compute Component Datatype Properties ----------------------------
	private String hyperType = "empty";
	private String hyperVer = "empty";
	private String uri = "empty";
	private int cpuSockets = 0;
	private int activeCpus = 0;
	private int coresPerSocket = 0;
	private int threadsPerCore = 0;
	private int cpuFrequency = 0;
	private int availableCores = 0;
	private long totalMemory = 0;
	private long availableMemory = 0;
	
	// ------------------------- Storage Component Datatype Properties -----------------------------
	private double totalStorage = 2000;
	private double availableStorage = 1600;
	
	// ------------------------ Switching Component Datatype Properties ----------------------------
	private int numOfOvs = 0;
	private Intfs[] intfsObjArray = null;
	
	// ------------------------ classes related by Objecttype Properties ----------------------------
	private HashMap <String, String> defInterfaces  = new HashMap<String, String>();
	private HashMap <String, String> networks  = new HashMap<String, String>();
	private HashMap <String, String> defNetworks  = new HashMap<String, String>();
	private VM[] activeVmObjArray = null;
	private VM[] definedVmObjArray = null;
	
	//--------------------------- Attributes not published in PR_Pool -------------------------------
						// nonUniId = 1,2,3, ... assigned based on the order of detection by libvirt. 0 for H-0, 1 for H-1 etc 
	private int numOfInterfaces = 0;
	private int numOfVms = 0;
	
///===========================================================================================================
///--------------------------------------------- Constructors ------------------------------------------------
///===========================================================================================================
	public VmHost(){
	}
	
	public VmHost(String resourceURI, String resourceName){
		this.resourceURI = resourceURI;
		this.resourceName = resourceName;
	}
	

///===========================================================================================================
///------------------------------------------ getters and setters --------------------------------------------
///===========================================================================================================
	
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
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

	public void setResLocation(String resLocation) {
		this.resLocation = resLocation;
	}

	public int getVmHostNum() {
		return vmHostNum;
	}
	public void setVmHostNum(int vmHostNum) {
		this.vmHostNum = vmHostNum;
	}
	public String getResModel() {
		return resModel;
	}
	public void setResModel(String model) {
		this.resModel = model;
	}
	public String getNodeClass() {
		return nodeClass;
	}
	public void setNodeClass(String nodeClass) {
		this.nodeClass = nodeClass;
	}
	public String getHyperType() {
		return hyperType;
	}
	public void setHyperType(String hyperType) {
		this.hyperType = hyperType;
	}
	public String getHyperVer() {
		return hyperVer;
	}
	public void setHyperVer(String hyperVer) {
		this.hyperVer = hyperVer;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public int getCpuSockets() {
		return cpuSockets;
	}
	public void setCpuSockets(int cpuSockets) {
		this.cpuSockets = cpuSockets;
	}
	public int getActiveCpus() {
		return activeCpus;
	}
	public void setActiveCpus(int activeCpus) {
		this.activeCpus = activeCpus;
	}
	public int getCoresPerSocket() {
		return coresPerSocket;
	}
	public void setCoresPerSocket(int coresPerSocket) {
		this.coresPerSocket = coresPerSocket;
	}
	public int getCpuFrequency() {
		return cpuFrequency;
	}
	public void setCpuFrequency(int cpuFrequency) {
		this.cpuFrequency = cpuFrequency;
	}
	public int getAvailableCores() {
		return availableCores;
	}

	public void setAvailableCores(int availableCores) {
		this.availableCores = availableCores;
	}

	public int getThreadsPerCore() {
		return threadsPerCore;
	}
	public void setThreadsPerCore(int threadsPerCore) {
		this.threadsPerCore = threadsPerCore;
	}
	public long getTotalMemory() {
		return totalMemory;
	}
	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}
	public long getAvailableMemory() {
		return availableMemory;
	}
	public void setAvailableMemory(long availableMemory) {
		this.availableMemory = availableMemory;
	}
	public double getTotalStorage() {
		return totalStorage;
	}
	public void setTotalStorage(double totalStorage) {
		this.totalStorage = totalStorage;
	}
	public double getAvailableStorage() {
		return availableStorage;
	}
	public void setAvailableStorage(double availableStorage) {
		this.availableStorage = availableStorage;
	}
	public int getNumOfInterfaces() {
		return numOfInterfaces;
	}
	public void setNumOfInterfaces(int numOfInterfaces) {
		this.numOfInterfaces = numOfInterfaces;
	}
public int getNumOfVms() {
		return numOfVms;
	}
	public void setNumOfVms(int numOfVms) {
		this.numOfVms = numOfVms;
	}
	//	public String getInterfaceType() {
//		return interfaceType;
//	}
//	public void setInterfaceType(String interfaceType) {
//		this.interfaceType = interfaceType;
//	}
	public int getNumOfOvs() {
		return numOfOvs;
	}
	public void setNumOfOvs(int numOfOvs) {
		this.numOfOvs = numOfOvs;
	}
public Intfs[] getIntfsObjArray() {
		return intfsObjArray;
	}
	public void setIntfsObjArray(Intfs[] intfsObjArray) {
		this.intfsObjArray = intfsObjArray;
	}
	//	public HashMap <String, String> getInterfaces() {
//		return interfaces;
//	}
//	public void setInterfaces(HashMap <String, String> interfaces) {
//		//this.interfaces = interfaces;										// "equal" will only copy reference.
//		this.interfaces.putAll(interfaces);									// To copy all elements, "putAll()" can be used.
//	}
	public HashMap <String, String> getDefInterfaces() {
		return defInterfaces;
	}
	public void setDefInterfaces(HashMap <String, String> defInterfaces) {
		this.defInterfaces.putAll(defInterfaces);
	}
	public HashMap <String, String> getNetworks() {
		return networks;
	}
	public void setNetworks(HashMap <String, String> networks) {
		this.networks.putAll(networks);
	}
	public HashMap <String, String> getDefNetworks() {
		return defNetworks;
	}
	public void setDefNetworks(HashMap <String, String> defNetworks) {
		this.defNetworks.putAll(defNetworks);
	}
	public VM[] getActiveVmObjArray() {
		return activeVmObjArray;
	}
	public void setActiveVmObjArray(VM[] vmObjArray) {
		this.activeVmObjArray = vmObjArray;
	}
	public VM[] getDefinedVmObjArray() {
		return definedVmObjArray;
	}
	public void setDefinedVmObjArray(VM[] definedVmObjArray) {
		this.definedVmObjArray = definedVmObjArray;
	}

	
	
}
