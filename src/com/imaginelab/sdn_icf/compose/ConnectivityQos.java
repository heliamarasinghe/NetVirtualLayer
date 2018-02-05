package com.imaginelab.sdn_icf.compose;

import java.io.Serializable;


public class ConnectivityQos implements Serializable{
	
	private static final long serialVersionUID = 2L;
	private double bw = 0.0;
	private double delay = 0.0;
	private double stress_level = 0.0;
	private double loss_rate = 0.0;
	private double cost = 0.0;
	private String inter_type = "empty";  // ethernet -ATM - SONET

	// ============================================================================================================================== //
	// -------------------------------------------------------- Constructors -------------------------------------------------------- //
	// ============================================================================================================================== //
	public ConnectivityQos() {
		super();
	}

	public ConnectivityQos(double bw, String typ) {
		super();
		this.bw = bw;
		this.inter_type=typ;
	}

	public ConnectivityQos(double bw, double delay, double stress_level,
			double loss_rate, double cost) {
		super();
		this.bw = bw;
		this.delay = delay;
		this.stress_level = stress_level;
		this.loss_rate = loss_rate;
		this.cost = cost;
	}
	// ============================================================================================================================== //
	// ----------------------------------------------------- Getters and Setters ---------------------------------------------------- //
	// ============================================================================================================================== //
	public String getInter_type() {
		return inter_type;
	}
	public void setInter_type(String inter_type) {
		this.inter_type = inter_type;
	}
	
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

	public double getStress_level() {
		return stress_level;
	}
	public void setStress_level(double stress_level) {
		this.stress_level = stress_level;
	}

	public double getLoss_rate() {
		return loss_rate;
	}
	public void setLoss_rate(double loss_rate) {
		this.loss_rate = loss_rate;
	}

	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
}