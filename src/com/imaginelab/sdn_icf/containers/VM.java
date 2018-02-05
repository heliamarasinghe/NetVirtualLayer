package com.imaginelab.sdn_icf.containers;


public class VM {
	// -------------------------------- Node Datatype Properties -----------------------------------
	private String resourceURI = "H-0_VM-0";
	private String resourceName = "none";												// <name>ubuntu-desk1</name>
	private String vmType = "kvm";														// <domain type='kvm' id='2'>
	private int vmId = 0;
	private String priorityClass = "none";
	private String resLocation 		= "Rack";
	private String uuid = "none";														// <uuid>d0ab83ed-9526-5e92-4bea-3c704b9df9df</uuid>
	
	// -------------------------- Compute Component Datatype Properties ----------------------------
	
	private long maxMemory = 0;															// <memory unit='KiB'>1048576</memory>
	private long currentMemory = 0;														//<currentMemory unit='KiB'>1048576</currentMemory>
	private int vCpus = 0;																// <vcpu placement='static'>1</vcpu>
	private String cpuArch = "none";													// <os><type arch='x86_64' machine='pc-i440fx-trusty'>hvm</type></os>
	private String machineOs = "none";	 
	private int maxCpus = 0;															// Domain.getMaxVcpus()
	private long cpuTime = 0;															// DomainInfo.cpuTime - not from Domain.xml
	// ------------------------- Storage Component Datatype Properties -----------------------------
	// TODO Domain.xml has multiple disk tags for HDD and cdrom	
	private double totalStorage = 100;
	// ------------------------ Switching Component Datatype Properties ----------------------------
	private int numberOfIntfs = 0;	
	// ------------------------ classes related by Objecttype Properties ----------------------------
	private Intfs[] intfsObjArray = null;
	//--------------------------- Attributes not published in PR_Pool -------------------------------
	
	
	
	
	
		
	
	
	
	
	
	/*
	String res_id="";
	String vmm="";				// vmType = kvm
	double mem=0.0;				// currentMemory
	double n_cores=0.0;			// vCpus
	
	String opeation;
	double cpu=0.0;
	String res_type="";     // compute -Storage -network
	double str=0.0;
	
	// functional for interfaces
 	String inter_type="";  // ethernet -ATM - SONET
 	double inter_dir=0.0;  // input -output
	ArrayList<interface_conn>  int_con;
	public int n_interfaces=1;
	
	// non functional parameters
	double bw=0.0;
	double delay=0.0;
	double loss_rate=0.0;
	double stress_level=0.0;
	String loc="";
	double cost=0.0;
	double R_W_operations=0.0;
	 */
	
///===========================================================================================================
///---------------------------------------- getters and setters ----------------------------------------------
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
	public String getVmType() {
		return vmType;
	}
	public void setVmType(String vmType) {
		this.vmType = vmType;
	}
	public int getVmId() {
		return vmId;
	}
	public void setVmId(int vmId) {
		this.vmId = vmId;
	}
	public String getPriorityClass() {
		return priorityClass;
	}
	public void setPriorityClass(String priorityClass) {
		this.priorityClass = priorityClass;
	}
	public String getResLocation() {
		return resLocation;
	}
	public void setResLocation(String resLocation) {
		this.resLocation = resLocation;
	}
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public long getMaxMemory() {
		return maxMemory;
	}
	public void setMaxMemory(long maxMemory) {
		this.maxMemory = maxMemory;
	}
	public long getCurrentMemory() {
		return currentMemory;
	}
	public void setCurrentMemory(long currentMemory) {
		this.currentMemory = currentMemory;
	}
	public int getvCpus() {
		return vCpus;
	}
	public void setvCpus(int vCpus) {
		this.vCpus = vCpus;
	}
	public String getCpuArch() {
		return cpuArch;
	}
	public void setCpuArch(String cpuArch) {
		this.cpuArch = cpuArch;
	}
	public String getMachineOs() {
		return machineOs;
	}
	public void setMachineOs(String machineOs) {
		this.machineOs = machineOs;
	}
	public int getNumberOfIntfs() {
		return numberOfIntfs;
	}
	public void setNumberOfIntfs(int numberOfIntfs) {
		this.numberOfIntfs = numberOfIntfs;
	}
	public int getMaxCpus() {
		return maxCpus;
	}
	public void setMaxCpus(int maxCpus) {
		this.maxCpus = maxCpus;
	}
	public long getCpuTime() {
		return cpuTime;
	}
	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}
	public double getTotalStorage() {
		return totalStorage;
	}
	public void setTotalStorage(double totalStorage) {
		this.totalStorage = totalStorage;
	}
	public Intfs[] getIntfsObjArray() {
		return intfsObjArray;
	}
	public void setIntfsObjArray(Intfs[] intfsObjArray) {
		this.intfsObjArray = intfsObjArray;
	}
}
