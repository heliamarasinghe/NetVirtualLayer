package com.imaginelab.sdn_icf.containers;

//import java.util.ArrayList;

public class PSwitch{

	//ArrayList connected_up=new ArrayList();
		//ArrayList connected_down=new ArrayList();
		private double cpu=1.0;      // 1 GHz
		private double n_core=1.0;   // 1 core
		private double memory=1.0;   // GB , 500 MB
		private double storage=1.0;   // GB
		private double up_level_BW=1000.0; //1 Gbps
		private double down_level_BW=1000.0;  // 1 Gbps
		private String Level;//Aggregate switch, TOR switch ,Core switches
		private int number_interfaces=2;
		private String location;// Rack ID
		private int res_type=3;  // server or storage disk or network
		private String switchURI = "";
	
	
	
	
	
	private int Switch_id;
	public PSwitch(String switchURI, int switch_id,String level,String loc, int number_interfaces) {
		this.setSwitchURI(switchURI);
		setSwitch_id(switch_id);
		setLevel(level);
		this.setNumber_interfaces(number_interfaces);
		setLocation(loc);
	}
	public double getCpu() {
		return cpu;
	}
	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	public double getMemory() {
		return memory;
	}
	public void setMemory(double memory) {
		this.memory = memory;
	}
	public double getN_core() {
		return n_core;
	}
	public void setN_core(double n_core) {
		this.n_core = n_core;
	}
	public double getStorage() {
		return storage;
	}
	public void setStorage(double storage) {
		this.storage = storage;
	}
	public double getUp_level_BW() {
		return up_level_BW;
	}
	public void setUp_level_BW(double up_level_BW) {
		this.up_level_BW = up_level_BW;
	}
	public double getDown_level_BW() {
		return down_level_BW;
	}
	public void setDown_level_BW(double down_level_BW) {
		this.down_level_BW = down_level_BW;
	}
	public String getLevel() {
		return Level;
	}
	public void setLevel(String level) {
		Level = level;
	}
	public int getNumber_interfaces() {
		return number_interfaces;
	}
	public void setNumber_interfaces(int number_interfaces) {
		this.number_interfaces = number_interfaces;
	}
	public String getLocation() {
		return location;
	}
	public void setLocation(String location) {
		this.location = location;
	}
	public String getSwitchURI() {
		return switchURI;
	}
	public void setSwitchURI(String switchURI) {
		this.switchURI = switchURI;
	}
	public int getRes_type() {
		return res_type;
	}
	public void setRes_type(int res_type) {
		this.res_type = res_type;
	}
	public int getSwitch_id() {
		return Switch_id;
	}
	public void setSwitch_id(int switch_id) {
		Switch_id = switch_id;
	}
	
}
