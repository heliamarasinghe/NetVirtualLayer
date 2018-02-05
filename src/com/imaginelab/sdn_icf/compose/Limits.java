//========================================================================================================================================
//
//							Limits file in  sdnvn.fillvrpool				edit  OK
//

package com.imaginelab.sdn_icf.compose;


//====================================================================================================================================================//
//---------------------------------------------------------------- ResLimits Class -------------------------------------------------------------------//

 	//	Resource Type (rt) = {cpuCore, Ram, diskStorage, bandwidth}
	//
	//						|    a  -  b    |
	//	Sim(a, b, rt) = 1 -	|---------------| 
	//						| MAXrt - MINrt |
	//
	//	for each requested resource unit (VR_req), similarity score is calculated for each composable unit (virtual resource) x in the VR pool (VRx_pool),
	//	a = rt value of VRx_pool
	//	b = rt value of VR_req
	//	MAXrt = maximum value of the rt in the physical resource which host VRx_pool
	//	MINrt = minimum value of the rt in the physical resource which host VRx_pool
	//	Example:
	//	Customer request a VM with 1Gb Ram (rt = Ram, b = 1)
	//	In the VR pool, there exist a virtual machine with 2 Gb Ram (a = 2)
	//	that virtual machine was hosted in a server
/* 	 ___________________________________________________________________
 	|		  \ Class	|				|				|				|
	| Resource \ 		|	Class C		|	Class B		|	Class A		|
	|-------------------|---------------|---------------|---------------|
	|			| lower	|		1		|		1		|		1		|
	| 	vCpu	|-------|---------------|---------------|---------------|
	|  (cores)	| upper	|		1		|		1		|		2		|
	|-----------|-------|---------------|---------------|---------------|
	|			| lower	|	  512Mb		|	    1Gb		|	    2Gb		|
	|  vMemory	|-------|---------------|---------------|---------------|
	|	(mem)	| upper	|	    1Gb		|	    2Gb		|	    4Gb		|
	|-----------|-------|---------------|---------------|---------------|
	|			| lower	|	   20Gb		|	   40Gb		|	   80Gb		|
	|  vStorage	|-------|---------------|---------------|---------------|
	|	(str)	| upper	|	   40Gb		|	   80Gb		|	  160Gb		|
	|-----------|-------|---------------|---------------|---------------|
	|			| lower	|	   10Mbps	|	   20Mbps	|	   40Mbps	|
	| vBandwith	|-------|---------------|---------------|---------------|
	|	(bw)	| upper	|	   20Mbps	|	   40Mbps	|	   80Mbps	|
	|___________|_______|_______________|_______________|_______________|

*/	

public final class Limits {
// limits for physical machines
	
// --------------------------------------virtual machine limits-----------------------------------------
//usage: InfrastructureComposer.evaluateSimilarityConcept()
// -----------------------------------------------------------------------------------------------------
public static int MINcpu=1;  						// GhZ
public static int MAXcpu=2;

public static int str_lower=20;
public static int str_upper=40; 						// GB 

public static int n_cores_lower=1;  					// 1 core
public static int n_cores_upper=2;  					// 4 cores

public static int mem_lower=1;
public static int mem_upper=2; 							// GB

public static double bw_lower=20;
public static double bw_upper=40;   					// Mbps 1000 Mbps

// throughput rate for storage
//public static int storage_RW_low=100; 					//Gbps
//public static int storage_RW_high=1000; 				//Gbps



// class A limits
public static int cpu_lower_A=1;  						// GhZ
public static int cpu_upper_A=2;

public static int str_lower_A=80;
public static int str_upper_A=160; 						// GB 

public static int n_cores_lower_A=1;  					// 1 core
public static int n_cores_upper_A=2;  					// 4 cores

public static int mem_lower_A=2;
public static int mem_upper_A=4; 						// GB

public static double bw_lower_A=40;
//public static double bw_upper_A=80;   					// Mbps 1000 Mbps



// class B limits
public static int cpu_lower_B=1;  						// GhZ
public static int cpu_upper_B=2;

public static int str_lower_B=40;
public static int str_upper_B=80; 						// GB 

public static int n_cores_lower_B=1;  					// 1 core
public static int n_cores_upper_B=1;  					// 4 cores	

public static int mem_lower_B=1;
public static int mem_upper_B=2; 						// GB

public static double bw_lower_B=20;
//public static double bw_upper_B=40;   					// Mbps 1000 Mbps






// class C limits
public static int cpu_lower_C=1;  						// GhZ
public static int cpu_upper_C=1;

public static int str_lower_C=20;
public static int str_upper_C=40; 						// GB 

public static int n_cores_lower_C=1; 	 				// 1 core
public static int n_cores_upper_C=1;  					// 4 cores

public static int mem_lower_C=1;
public static int mem_upper_C=2; 						// GB

public static double bw_lower_C=10;
//public static double bw_upper_C=50;   					// Mbps 1000 Mbps


}
