package com.imaginelab.sdn_icf.containers;
import java.io.Serializable;
import java.util.ArrayList;

import com.imaginelab.sdn_icf.compose.ConnectivityQos;
import com.imaginelab.sdn_icf.compose.Status;


public class IaaSRequest implements Serializable{	
	private static final long serialVersionUID = 1L;						//	Default Serialization id
	private double reqId = 99;
	private String reqClass = "none";
	private double arrivalTime;
	private double reqDuration = 99;
	private double minReqDuration = 99;
	private double requestLifeTime;
	private ConnectivityQos  connectivityQosObj;
	private ArrayList<VResource> vResourceList;
	private Status status = Status.ARRIVAL;
	

	// ============================================================================================================================== //
	// -------------------------------------------------------- Constructors -------------------------------------------------------- //
	// ============================================================================================================================== //
	public IaaSRequest(){
		setConstraints(new ConnectivityQos());
	}
	public IaaSRequest(int reqId){	
		setConstraints(new ConnectivityQos());
		this.reqId=reqId;
	}
	public IaaSRequest(ArrayList<VResource> vResourceList, ConnectivityQos constraints) {
		super();
		setvResourceList(vResourceList);
		this.setConstraints(constraints);
	}
	// ============================================================================================================Dom parser vs sax parser memory usage vs speed================== //
	// ----------------------------------------------------- Getters and Setters ---------------------------------------------------- //
	// ============================================================================================================================== //
	public double getReqId() {
		return reqId;
	}
	public void setReqId(double reqId) {
		this.reqId = reqId;
	}
	public String getReqClass() {
		return reqClass;
	}
	public void setReqClass(String reqClass) {
		this.reqClass = reqClass;
	}
	public double getArrivalTime() {
		return arrivalTime;
	}
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}
	public double getReqDuration() {
		return reqDuration;
	}
	public void setReqDuration(double reqDuration) {
		this.reqDuration = reqDuration;
	}
	public double getMinReqDuration() {
		return minReqDuration;
	}
	public void setMinReqDuration(double minReqDuration) {
		this.minReqDuration = minReqDuration;
	}
	public double getRequestLifeTime() {
		return requestLifeTime;
	}
	public void setRequestLifeTime(double requestLifeTime) {
		this.requestLifeTime = requestLifeTime;
	}
	public Status getStatus() {
		return status;
	}
	public void setStatus(Status status) {
		this.status = status;
	}
	
	public ArrayList<VResource> getVRs() {
		return vResourceList;
	}
	public void setvResourceList(ArrayList<VResource> vResourceList) {
		this.vResourceList = new ArrayList<VResource>(vResourceList);
	}
	public ConnectivityQos getConstraints() {
		return connectivityQosObj;
	}
	public void setConstraints(ConnectivityQos constraints) {
		this.connectivityQosObj = constraints;
	}
}

