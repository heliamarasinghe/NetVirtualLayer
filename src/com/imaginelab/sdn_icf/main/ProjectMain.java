package com.imaginelab.sdn_icf.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.imaginelab.sdn_icf.compose.InfrastructureComposer;
import com.imaginelab.sdn_icf.deploy.ReqImplementPlanner;
import com.imaginelab.sdn_icf.discover.BuildPrTopology;
import com.imaginelab.sdn_icf.request.GenerateRequests;
import com.imaginelab.sdn_icf.virtualize.ConstructVrPool;

import static com.imaginelab.sdn_icf.main.Constants.COMPOSITION;
import static com.imaginelab.sdn_icf.main.Constants.CONSTRUCT_PRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.CONSTRUCT_VRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.CTRL1_IP;
import static com.imaginelab.sdn_icf.main.Constants.CTRL2_IP;
import static com.imaginelab.sdn_icf.main.Constants.CTRL3_IP;
import static com.imaginelab.sdn_icf.main.Constants.DEPLOYMENT_PLAN;
import static com.imaginelab.sdn_icf.main.Constants.FV_IP;
import static com.imaginelab.sdn_icf.main.Constants.GEN_IC_REQ;
import static com.imaginelab.sdn_icf.main.Constants.KVM1_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM2_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM3_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM4_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM5_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM6_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM7_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM8_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM9_IP;
import static com.imaginelab.sdn_icf.main.Constants.NUM_OF_REQ;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL_EMPTY;
import static com.imaginelab.sdn_icf.main.Constants.RMVE_INDVL_PRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.RMVE_INDVL_VRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.SINGLE_TEST_REQ;
import static com.imaginelab.sdn_icf.main.Constants.TEST_CONNECTIVITY;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_EMPTY;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_FREE;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_RPLCE_FREE;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_RPLCE_RSVD;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_RSVD;


public class ProjectMain {


	///===========================================================================================================================================
	///-------------------------main----------------------main-------------------------main----------------------------main-----------------------
	///===========================================================================================================================================
	public static void main(String [] args) {

		//	Sub-Module I: Pinging network components to test L3 connectivity
		if (TEST_CONNECTIVITY){
			// ping check IP addresses of Flowvisor, Floodlight controller and two Hypervisors. 
			//If ping is "OK" and you still get exceptions, check whether the application is using the correct port in the server
			System.out.println("Checking network connectivity to remote servers...");
			checkConnections();	
		}

		//	Sub-Module II: Generate IC Requests
		if (GEN_IC_REQ){
			System.out.println("Generating requests");
			//int numCls1 = 3;
			//int numCls2 = 3;
			//int numCls3 = 4;
			//GenerateRequests.createRandReq(numCls1, numCls2, numCls3);
			GenerateRequests.createSpecificRequest();
		}



		// Profilers: http://ancitconsulting.com/tiki-read_article.php?articleId=10
		// JVM Monitor: http://www.jvmmonitor.org/doc/index.html#Getting_started
		//for(int i = 0; i<6; i++){


			//	Main Modules of the program
			//	===========================
			// 	Module 1: Build Physical Topology and update PrPool
			// 	---------------------------------------------------
			if(CONSTRUCT_PRPOOL){
				if (RMVE_INDVL_PRPOOL) replaceFile_1to2(PRPOOL_EMPTY, PRPOOL);		// Replace existing PrPool owl file with empty (no-individuals) template
				BuildPrTopology.updatePrPool();
			}
			

			// Module 2: Construct virtual resource pool from physical resource pool
			// 	--------------------------------------------------------------------
			if(CONSTRUCT_VRPOOL){
				if (RMVE_INDVL_VRPOOL) replaceFile_1to2(VRPOOL_EMPTY, VRPOOL);		// Replace existing VrPool owl file with empty (no-individuals) template
				System.out.println("Reading PrPool and populate VrPool");
				ConstructVrPool.readPrPool();
				if (VRPOOL_RPLCE_FREE)replaceFile_1to2(VRPOOL, VRPOOL_FREE);			// Creating free-state template
				System.out.println("Done populating VrPool");
			}

			// Module 3: Compose virtual infrastructure to satisfy IC request 	
			// 	-------------------------------------------------------------
			if(COMPOSITION){
				if (VRPOOL_RPLCE_FREE) replaceFile_1to2(VRPOOL_FREE, VRPOOL);		// Replacing VRPool owl file with free-state template (change VR state from 'reserved' to 'free') 
				InfrastructureComposer.compositionEventManager(SINGLE_TEST_REQ? 3: NUM_OF_REQ);
				if (VRPOOL_RPLCE_RSVD) replaceFile_1to2(VRPOOL, VRPOOL_RSVD);		// Creating rsvd-state template

				System.out.println("Composition Done");
			}

			// Module 4: Deploy composed virtual infrastructure over physical infrastructure
			// 	----------------------------------------------------------------------------
			if(DEPLOYMENT_PLAN){
				if (VRPOOL_RPLCE_RSVD) replaceFile_1to2(VRPOOL_RSVD, VRPOOL);		// Replacing VRPool owl file with rsvd-state template (change VR state from 'allocated' to 'reserved') 
				ReqImplementPlanner.createVMsForAcceptedRequests();
				System.out.println("Implementation Done");
			}
			System.out.println("Reply From ProjectMain.main() \t = \t DONE \nDebugging can be enabled from \"Constants.java\"");

			try {
				Thread.sleep(2000);
				System.gc();
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		//}		//END FOR(i = ...)
	}


	///====================================================================================================
	///------------------------------------------PRE_CODE methods------------------------------------------
	///====================================================================================================	
	private static final Map<String, String> ipToHostNameMap;
	static {
		Map<String, String> aMap = new HashMap<String, String>();
		aMap.put(FV_IP , 	"FlowVisor");
		aMap.put(CTRL1_IP, 	"ctrl1-pox1");
		aMap.put(CTRL2_IP, 	"ctrl2-pox2");
		aMap.put(CTRL3_IP, 	"ctrl3-pox3");
		aMap.put(KVM1_IP, 	"kvm-host1");
		aMap.put(KVM2_IP, 	"kvm-host2");
		aMap.put(KVM3_IP, 	"kvm-host3");
		aMap.put(KVM4_IP, 	"kvm-host4");
		aMap.put(KVM5_IP, 	"kvm-host5");
		aMap.put(KVM6_IP, 	"kvm-host6");
		aMap.put(KVM7_IP, 	"kvm-host7");
		aMap.put(KVM8_IP, 	"kvm-host8");
		aMap.put(KVM9_IP, 	"kvm-host9");
		ipToHostNameMap = Collections.unmodifiableMap(aMap);
	}



	public static void checkConnections (){

		try {
			for(String ipAddress : ipToHostNameMap.keySet())
				System.out.println(ipToHostNameMap.get(ipAddress) + (InetAddress.getByName(ipAddress).isReachable(5000) ? "\t OK" : "\t Not reachable from "+ipAddress));

		} catch (IOException ioE){
			System.out.println("IO Exception generated at ProjectMain.checkconnections()");
			ioE.printStackTrace();
		}
	}

	public static void replaceFile_1to2(String newFile, String oldFile) {
		try {
			//String[] cmd = {"/bin/bash","-c","echo password| sudo -S ls"};
			String[] cmd = {"/bin/bash","-c", "cp "+newFile+" "+oldFile};
			Process pb = Runtime.getRuntime().exec(cmd);
			pb.waitFor();
			String line;
			BufferedReader input = new BufferedReader(new InputStreamReader(pb.getInputStream()));
			while ((line = input.readLine()) != null) System.out.println(line);
			input.close();

		} catch (Exception Ex){
			Ex.printStackTrace();
		}
	}

}
