package com.imaginelab.sdn_icf.containers;

import java.io.Serializable;
import java.util.ArrayList;

import com.imaginelab.sdn_icf.compose.interface_conn;

public class VResource implements Serializable {


	static final long serialVersionUID = 123;
	String priorityClass	= "none";													// operation: switching, storage, compute
	double cpu				= 0.0;
	double n_cores			= 0.0;
	double mem				= 0.0;
	double storage			= 0.0;
	String vmm 				= "";
	String res_type			= "";     									// compute -Storage -network
	String resUri			= "";
	private int resNum 		= 0;
	// non functional parameters
	double bw				= 0.0;
	double delay			= 0.0;
	double loss_rate		= 0.0;
	double stress_level		= 0.0;
	String loc				= "";
	double cost				= 0.0;
	double R_W_operations 	= 0.0;
	// functional for interfaces
	String inter_type		= "";  										// ethernet -ATM - SONET
	double inter_dir		= 0.0;  									// input -output
	public int n_interfaces	= 1;
	public ArrayList<interface_conn>  int_con;
	private ArrayList<String> connectedToList;

	public VResource()
	{
		interface_conn IC=new interface_conn(0, 0, "Ethernet");
		int_con=new ArrayList<interface_conn>();
		int_con.add(IC);
	}

	public VResource(double cpu, double mem, double str, String res_type, String inter_type, double bw, double delay, int n_interfac) {
		super();
		this.cpu = cpu;
		this.mem = mem;
		this.storage = str;
		this.res_type = res_type;
		this.inter_type = inter_type;
		this.bw = bw;
		this.delay = delay;
		this.n_interfaces=n_interfac;
		for(int i=0;i<n_interfac;i++)
		{
			interface_conn IC=new interface_conn(bw, delay, inter_type);
			int_con=new ArrayList<interface_conn>();
			int_con.add(IC);
		}
	}

	public VResource(double cpu, double mem, double storage, String vmm, String res_type, String res_id, String inter_type, double bw,
			double delay, double loss_rate, double stress_level, String loc,  String opeation, int n_interfac) {
		super();
		this.cpu = cpu;
		this.mem = mem;
		this.storage = storage;
		this.vmm = vmm;
		this.res_type = res_type;
		this.resUri = res_id;
		this.inter_type = inter_type;
		this.bw = bw;
		this.delay = delay;
		this.loss_rate = loss_rate;
		this.stress_level = stress_level;
		this.loc = loc;
		this.priorityClass = opeation;
		this.n_interfaces=n_interfac;
		for(int i=0;i<n_interfac;i++){
			interface_conn IC=new interface_conn(bw, delay, inter_type);
			int_con=new ArrayList<interface_conn>();
			int_con.add(IC);
		}	
	}
	
	@Override
	public String toString() {
		return "VResource [cpu=" + cpu + ", n_cores=" + n_cores + ", mem="
				+ mem + ", str=" + storage + ", vmm=" + vmm + ", res_type="
				+ res_type + ", res_id=" + resUri + ", bw=" + bw
				+ ", delay=" + delay + ", loss_rate=" + loss_rate
				+ ", stress_level=" + stress_level + ", loc=" + loc + "]";
	}
	
	public double getCpu() {
		return cpu;
	}
	public void setCpu(double cpu) {
		this.cpu = cpu;
	}
	public double getMem() {
		return mem;
	}
	public void setMem(double mem) {
		this.mem = mem;
	}
	public double getStr() {
		return storage;
	}
	public void setStr(double str) {
		this.storage = str;
	}
	public String getVmm() {
		return vmm;
	}
	public void setVmm(String vmm) {
		this.vmm = vmm;
	}
	public String getRes_type() {
		return res_type;
	}
	public void setRes_type(String res_type) {
		this.res_type = res_type;
	}
	public String getResUri() {
		return resUri;
	}
	public void setResUri(String resUri) {
		this.resUri = resUri;
	}
	public String getInter_type() {
		return inter_type;
	}
	public void setInter_type(String inter_type) {
		this.inter_type = inter_type;
	}
	public double getInter_dir() {
		return inter_dir;
	}
	public void setInter_dir(double inter_dir) {
		this.inter_dir = inter_dir;
	}
	public ArrayList<interface_conn> getInt_con() {
		return int_con;
	}
	public void setInt_con(ArrayList<interface_conn> int_con) {
		this.int_con = int_con;
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
	public double getLoss_rate() {
		return loss_rate;
	}
	public void setLoss_rate(double loss_rate) {
		this.loss_rate = loss_rate;
	}
	public double getStress_level() {
		return stress_level;
	}
	public void setStress_level(double stress_level) {
		this.stress_level = stress_level;
	}
	public String getLoc() {
		return loc;
	}
	public void setLoc(String loc) {
		this.loc = loc;
	}
	public double getCost() {
		return cost;
	}
	public void setCost(double cost) {
		this.cost = cost;
	}
	public String getPriorityClass() {
		return priorityClass;
	}
	public void setPriorityClass(String opeation) {
		this.priorityClass = opeation;
	}
	public double getN_cores() {
		return n_cores;
	}
	public void setN_cores(double n_cores) {
		this.n_cores = n_cores;
	}
	public int getN_interfaces() {
		return n_interfaces;
	}
	public void setN_interfaces(int n_interfaces) {
		this.n_interfaces = n_interfaces;
	}
	public double getR_W_operations() {
		return R_W_operations;
	}
	public void setR_W_operations(double r_W_operations) {
		R_W_operations = r_W_operations;
	}

	public ArrayList<String> getConnectedToList() {
		return connectedToList;
	}

	public void setConnectedToList(ArrayList<String> connectedToList) {
		this.connectedToList = connectedToList;
	}

	public int getResNum() {
		return resNum;
	}

	public void setResNum(int resNum) {
		this.resNum = resNum;
	}
}