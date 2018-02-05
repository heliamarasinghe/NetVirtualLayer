package com.imaginelab.sdn_icf.containers;

public class PNode_C {

	String used_VMM="KVM";
	String used_platform="64 bit";
	int res_type=1;  // server=1 or storage disk=2 or vswitch =3
	int n_interfaces=1;  // more than 1 in vswitch
	// String service;
	String location;// Rack ID
	int Res_ID;   // unique identifier for the physical resource
	
	
	public PNode_C(String loc,int iD,int res_typ,int num_interface) {
		super();
		location=loc;
		Res_ID = iD;
		res_type=res_typ;
		n_interfaces=num_interface;
	}
	
	double total_CPU		=4.0; // total 4 cpu cores
	double total_mem		=8.0; // 8 GB memory
	double total_storage	=1000.0 ; //1000 GB of disk space  1 Tera
	double total_BW			=1000.0; // 1000  Mbps total bandwidth  1 Gbps
	double n_cores			=4;
	double total_delay		=0; // 50 msec
	double R_W_Storage; // read write storage rate
	
}
