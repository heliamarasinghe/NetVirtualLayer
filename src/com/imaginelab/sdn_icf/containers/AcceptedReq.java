package com.imaginelab.sdn_icf.containers;

import java.util.ArrayList;
import java.util.List;

public class AcceptedReq{

	private String reqUri		= "none";
	private int reqId 			= 9999;
	private String reqName		= "none";
	private int reqArrival 		= 9999;
	private int reqDuration		= 9999;
	private String priorityCls	= "none";
	private String reqState		= "none";
	private int targetVlan 		= 1;
	private List<String> rsvdVmResList = new ArrayList<String>();
	private List<String> rsvdVsResList = new ArrayList<String>();
	private List<String> rsvdVlResList = new ArrayList<String>();
	private List<String> alocVmResList = new ArrayList<String>();
	private List<String> alocVsResList = new ArrayList<String>();
	private List<String> alocVlResList = new ArrayList<String>();
	

	//Constructor
	public AcceptedReq(String reqUri, int reqArrival){
		this.reqUri = reqUri;
		this.reqArrival = reqArrival;
	}
	
	//Getters and Setters
	public String getReqUri() {
		return reqUri;
	}
	public int getReqId() {
		return reqId;
	}
	public void setReqId(int reqId) {
		this.reqId = reqId;
	}
	public String getReqName() {
		return reqName;
	}
	public void setReqName(String reqName) {
		this.reqName = reqName;
	}
	public int getReqArrival() {
		return reqArrival;
	}
	public int getReqDuration() {
		return reqDuration;
	}
	public void setReqDuration(int reqDuration) {
		this.reqDuration = reqDuration;
	}
	public String getPriorityCls() {
		return priorityCls;
	}
	public void setPriorityCls(String priorityCls) {
		this.priorityCls = priorityCls;
	}
	public String getReqState() {
		return reqState;
	}
	public void setReqState(String reqState) {
		this.reqState = reqState;
	}
	public int getTargetVlan() {
		return targetVlan;
	}
	public void setTargetVlan(int targetVlan) {
		this.targetVlan = targetVlan;
	}
	public void addToRsvdVmResList (String rsvdVmResUri){
		rsvdVmResList.add(rsvdVmResUri);	
	}
	public List<String> getRsvdVmResList (){
		return rsvdVmResList;
	}
	
	public void addToRsvdVsResList (String rsvdVsResUri){
		rsvdVsResList.add(rsvdVsResUri);	
	}
	public List<String> getRsvdVsResList (){
		return rsvdVsResList;
	}
	
	public void addToRsvdVlResList (String rsvdVlResUri){
		rsvdVlResList.add(rsvdVlResUri);	
	}
	public List<String> getRsvdVlResList (){
		return rsvdVlResList;
	}
	public void addToAlocVmResList (String alocVmResUri){
		alocVmResList.add(alocVmResUri);	
	}
	public List<String> getAlocVmResList (){
		return alocVmResList;
	}
	
	public void addToAlocVsResList (String alocVsResUri){
		alocVsResList.add(alocVsResUri);	
	}
	public List<String> getAlocVsResList (){
		return alocVsResList;
	}
	
	public void addToAlocVlResList (String alocVlResUri){
		alocVlResList.add(alocVlResUri);	
	}
	public List<String> getAlocVlResList (){
		return alocVlResList;
	}
}
