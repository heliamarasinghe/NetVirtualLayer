package com.imaginelab.sdn_icf.compose;

import java.io.Serializable;

public class interface_conn implements Serializable {
	
	static final long serialVersionUID = 1234;
	double bw;
	double delay;
	String inter_type;  // ethernet -ATM - SONET
	
	
	public interface_conn(double bw, double delay,String inter_type) {
		super();
		this.bw = bw;
		this.delay = delay;
		this.inter_type = inter_type;
		
	}
	public interface_conn() {
		super();
	}
	// non functional parameters
	
	public double getBw() {
		return bw;
	}
	public void setBw(double bw) {
		this.bw = bw;
	}
	public double getDelay() {
		return delay;
	}
	public void setDelay(double delay) {
		this.delay = delay;
	}
	public String getInter_type() {
		return inter_type;
	}
	public void setInter_type(String inter_type) {
		this.inter_type = inter_type;
	}
}
