package com.imaginelab.sdn_icf.containers;

import java.util.ArrayList;
import java.util.List;

public class VSw {
	private String resourceURI = "none";		// "S-4_VS"		
	private String resourceName = "none";
	private double bw = 0.0;
	private String VMM = "none";
	private double n_cores = 0.0;
	private double loss_rate = 0.0;
	private double storage = 0.0;
	private double memory = 0.0;
	private String location = "none";
	private double delay = 0.0;
	private double cpu = 0.0;
	private List<String> connectedResList = new ArrayList<String>();
	
	
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
	public double getBw() {
		return bw;
	}
	public void setBw(double bw) {
		this.bw = bw;
	}
	public String getVMM() {
		return VMM;
	}
	public void setVMM(String vMM) {
		VMM = vMM;
	}
	public double getN_cores() {
		return n_cores;
	}
	public void setN_cores(double n_cores) {
		this.n_cores = n_cores;
	}
	public double getLoss_rate() {
		return loss_rate;
	}
	public void setLoss_rate(double loss_rate) {
		this.loss_rate = loss_rate;
	}
	public double getStorage() {
		return storage;
	}
	public void setStorage(double storage) {
		this.storage = storage;
	}
	public double getMemory() {
		return memory;
	}
	public void setMemory(double memory) {
		this.memory = memory;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public double getDelay() {
		return delay;
	}
	public void setDelay(double delay) {
		this.delay = delay;
	}
	public double getCpu() {
		return cpu;
	}
	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	public List<String> getRsvdVsResList() {
		return connectedResList;
	}
	public void addToAlocVmResList (String alocVmResUri){
		connectedResList.add(alocVmResUri);	
	}
	
	
	
	
	
	
}
