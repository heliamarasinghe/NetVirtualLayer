package com.imaginelab.sdn_icf.containers;

class PLink {

	public PLink(int from, int to, double bw, double delay,int ID) {
		super();
		this.from = from;
		this.to = to;

		Bw = bw;
		this.delay = delay;
		this.Res_ID=ID;
	}
	double cpu=0.0;      // 1 GHz
	double n_core=0.0;   // 1 core
	double memory=0.0;   // GB , 500 MB
	double storage=0.0;   // GB
	String from_src_interface;  // example for interface is the hostID_interfaceID  e.g.  100_1
	String to_dst_interface;
	int from;        // source ID
	int to;	      // Dst ID
	double Bw=1000.0; //1 Gbps
	double delay;
	int Res_ID;
	int res_type=3;  // server or storage disk or network
}
