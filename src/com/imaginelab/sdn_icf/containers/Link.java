/*
 * 
 * resource_id
	latency
	link_type
	bandwidth
	loss_rate
	src_mac
	dest_mac
 * 
 * 
 */
package com.imaginelab.sdn_icf.containers;

public class Link {

	
	private int FvReqID = 0;				
	private String resourceURI = "S-0_I-0_P-0:S-0_I-0_P-0";
	private String resourceName = "link";
	private double latency = 0.2;				// mS
	private String linkType = "emptyLinkType";
	private double bandwidth = 1000.0;			// Mbps
	private double lossRate = 0.4;				// packets per 100
	
	private String srcIntfsResourceURI = "emptySrcIntfs";
	private String destIntfsResourceURI = "emptyDestIntfs";
	private String srcIntfsMac = "emptySrcMac";
	private String destIntfsMac = "emptyDestMac";
	private String srcPortResourceURI = "emptySrcPort";
	private String destPortResourceURI = "emptyDestPort";
	
	public Link(){
	}
	
	public Link(String LinkURI, String srcPortURI, String destPortURI){
		this.resourceURI =  LinkURI;
		this.resourceName = "defaultLink";
		this.linkType = "nonSwitchToSwitch";
		String[] srcPortSplitArray = srcPortURI.split("_");
		String[] destPortSplitArray = destPortURI.split("_");
		this.srcIntfsResourceURI = srcPortSplitArray[0]+"_"+srcPortSplitArray[1];
		this.destIntfsResourceURI = destPortSplitArray[0]+"_"+destPortSplitArray[1];
		this.srcPortResourceURI = srcPortURI;
		this.destPortResourceURI = destPortURI;
	}
	
///===========================================================================================================
///---------------------------------------- getters and setters ----------------------------------------------
///===========================================================================================================
	public int getFvReqID() {
		return FvReqID;
	}
	public void setFvReqID(int fvReqID) {
		FvReqID = fvReqID;
	}
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	public String getResourceName() {
		return resourceName;
	}
	public void setResourcenName(String resourceName) {
		this.resourceName = resourceName;
	}
	public double getLatency() {
		return latency;
	}
	public void setLatency(double latency) {
		this.latency = latency;
	}
	public String getLinkType() {
		return linkType;
	}
	public void setLinkType(String link_type) {
		this.linkType = link_type;
	}
	public double getBandwidth() {
		return bandwidth;
	}
	public void setBandwidth(double bandwidth) {
		this.bandwidth = bandwidth;
	}
	public double getLossRate() {
		return lossRate;
	}
	public void setLossRate(double loss_rate) {
		this.lossRate = loss_rate;
	}
	public String getSrcIntfsResourceURI() {
		return srcIntfsResourceURI;
	}
	public void setSrcIntfsResourceURI(String srcIntfsResourceURI) {
		this.srcIntfsResourceURI = srcIntfsResourceURI;
	}
	public String getDestIntfsResourceURI() {
		return destIntfsResourceURI;
	}
	public void setDestIntfsResourceURI(String destIntfsResourceURI) {
		this.destIntfsResourceURI = destIntfsResourceURI;
	}
	public String getSrcIntfsMac() {
		return srcIntfsMac;
	}
	public void setSrcIntfsMac(String srcIntfsMac) {
		this.srcIntfsMac = srcIntfsMac;
	}
	public String getDestIntfsMac() {
		return destIntfsMac;
	}
	public void setDestIntfsMac(String destIntfsMac) {
		this.destIntfsMac = destIntfsMac;
	}
	public String getSrcPortResourceURI() {
		return srcPortResourceURI;
	}
	public void setSrcPortResourceURI(String srcPortResourceURI) {
		this.srcPortResourceURI = srcPortResourceURI;
	}
	public String getDestPortResourceURI() {
		return destPortResourceURI;
	}
	public void setDestPortResourceURI(String destPortResourceURI) {
		this.destPortResourceURI = destPortResourceURI;
	}
	
	
	
}
