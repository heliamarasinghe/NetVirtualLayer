package com.imaginelab.sdn_icf.request;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Random;

import com.imaginelab.sdn_icf.compose.ConnectivityQos;
import com.imaginelab.sdn_icf.compose.FilesManipulation;
import com.imaginelab.sdn_icf.compose.Limits;
import com.imaginelab.sdn_icf.containers.IaaSRequest;
import com.imaginelab.sdn_icf.containers.PrintContainerValues;
import com.imaginelab.sdn_icf.containers.VResource;

import static com.imaginelab.sdn_icf.main.Constants.NL;
import static com.imaginelab.sdn_icf.main.Constants.VMS_PER_REQ;
import static com.imaginelab.sdn_icf.main.Constants.NUM_OF_REQ;
import static com.imaginelab.sdn_icf.main.Constants.REQ_SER_FOLDR;
import static com.imaginelab.sdn_icf.main.Constants.REQ_SNET_FOLDR;
import static com.imaginelab.sdn_icf.main.Constants.REQ_TXT_FOLDR;
import static com.imaginelab.sdn_icf.main.Constants.TEST_REQ_SERF;
import static com.imaginelab.sdn_icf.main.Constants.TEST_REQ_SNETF;
import static com.imaginelab.sdn_icf.main.Constants.TEST_REQ_TXTF;


public class GenerateRequests {

	// ============================================================================================================================== //
	// --------------------------------------------------- createSpecificRequest() -------------------------------------------------- //
	// ============================================================================================================================== //
	public static void createSpecificRequest(){
		// IaaSRequest parameters
		//int numReq	= 150;
		try {
			for(int reqId=0; reqId<NUM_OF_REQ; reqId++){

				String reqClass = "C";
				double reqArrivalTime = reqId * 10;
				double reqDuration = 10000;
				//int numOfVResPerReq = 9;
	
				// creating vResObj resource values based on request class
				String resourceType = "compute";
				int bandwidthMbps;
				int cpuCore;
				int memoryGb;
				int diskStorageGb;
	
				switch(reqClass){
				case "A":
					bandwidthMbps = 80;
					cpuCore = 2;
					memoryGb = 4;
					diskStorageGb = 80;
					break;
				case "B":
					bandwidthMbps = 40;
					cpuCore = 1;
					memoryGb = 2;
					diskStorageGb = 40;
					break;
				default:	// reqClass 3 = default
					bandwidthMbps = 10;
					cpuCore = 1;
					memoryGb = 1;
					diskStorageGb = 20;
					break;
				}
	
				// Creating folder structure to save request files
				new File(TEST_REQ_TXTF).mkdir();
				new File(TEST_REQ_SERF).mkdir();
				new File(TEST_REQ_SNETF).mkdir();
	
				// File output streams are handled by the class FilesManipulation
				FilesManipulation writeFileReqTxt;
				writeFileReqTxt = new FilesManipulation(0, TEST_REQ_TXTF+"/req_"+reqId+".txt");
				FilesManipulation writeFileReqSecondNet = new FilesManipulation(0, TEST_REQ_SNETF+"/reqSN_"+reqId+".txt");
				FileOutputStream fileOutStream = new FileOutputStream(TEST_REQ_SERF+"/req_"+reqId+".ser");
				ObjectOutputStream objOutStream = new ObjectOutputStream(fileOutStream);
	
				IaaSRequest testIaaSRequest = new IaaSRequest(reqId);
				ArrayList<VResource>  vResObjListForReq = new ArrayList<VResource>();
	
	
				for (int vResObjItr = 0; vResObjItr<VMS_PER_REQ; vResObjItr++){
					VResource vResObj = new VResource();
					vResObj.setResUri("R-"+reqId+"_VR-"+vResObjItr);
					vResObj.setMem(memoryGb);
					vResObj.setN_cores(cpuCore); // cpu and n_cores are equivelant
					vResObj.setCpu(cpuCore);
					vResObj.setStr(diskStorageGb);
					vResObj.setRes_type(resourceType);
					vResObj.setInter_type("Ethernet");
					vResObj.setN_interfaces(1);
					vResObj.setBw(bandwidthMbps);
					vResObjListForReq.add(vResObj);
				}
				ConnectivityQos constraints = new ConnectivityQos(bandwidthMbps ,"Ethernet"); // avg_bw/n_vrs,"Ethernet");
				testIaaSRequest.setReqClass(reqClass);
				testIaaSRequest.setArrivalTime(reqArrivalTime);
				testIaaSRequest.setReqDuration(reqDuration);
				testIaaSRequest.setRequestLifeTime(reqDuration);
				testIaaSRequest.setvResourceList(vResObjListForReq);
				testIaaSRequest.setConstraints(constraints);
	
				PrintContainerValues.printGeneratedIaaSRequest(testIaaSRequest);
	
				objOutStream.writeObject(testIaaSRequest);
				objOutStream.close();
	
	
				writeFileReqSecondNet.Create_request_file(""+testIaaSRequest.getVRs().size()+" "+reqArrivalTime+" "+reqDuration);
				for(int vResItr=0; vResItr<testIaaSRequest.getVRs().size(); vResItr++){
					VResource vResObj = testIaaSRequest.getVRs().get(vResItr);
					writeFileReqSecondNet.Create_request_file(NL+vResObj.getCpu());
					writeFileReqSecondNet.Create_request_file(NL+vResObj.getMem());
					writeFileReqSecondNet.Create_request_file(NL+vResObj.getStr());
					writeFileReqSecondNet.Create_request_file(NL+vResObj.getBw());
				}
				System.out.println("Req "+ reqId +" reqArrivalTime = " +reqArrivalTime + "\t reqDuration = "+reqDuration +"\t reqClass = " + reqClass );
				writeFileReqTxt.Create_request_file(""+reqId);
				writeFileReqTxt.Create_request_file(""+reqArrivalTime);
				writeFileReqTxt.Create_request_file(""+reqDuration);
				// loop on the request to write in the file
	
				writeFileReqTxt.Close();
				writeFileReqSecondNet.Close();
			}
		} catch (IOException e) {
			System.err.println("GenerateRequests.createSpecificRequest: IO Exception caught");
			e.printStackTrace();
		}
		System.out.println("\nIC Requests created\n");
	}


	// ============================================================================================================================== //
	// ------------------------------------------------------- createRandReq() ------------------------------------------------------ //
	// ============================================================================================================================== //
	public static void createRandReq(int n_Class1, int n_Class2, int n_Class3){

		boolean done=false;
		int total_simulation_time=50000;
		int duration ;
		int k = 0, countk = 0,p = 0, start = 0,time;
		int req1_count = 0;
		int req2_count = 0;
		int req3_count = 0;
		int req = 0;
		double POISSON_MEAN=25;//25//50
		double REQ_DURATION=1000; // 5000
		double MIN_REQ_DURATION = 250;//1000;

		ArrayList<Integer> random_number_list = new ArrayList<Integer>();
		Random Request_generator = new Random();
		FilesManipulation Write_Req, Write_Req_SecondNet;
		FileOutputStream fout ;
		ObjectOutputStream oos ; 

		int numOfReq = n_Class1+n_Class2+n_Class3;

		File dir = new File(REQ_TXT_FOLDR);
		File dir1 = new File(REQ_SER_FOLDR);
		File dir2 = new File(REQ_SNET_FOLDR);

		dir.mkdirs();
		dir1.mkdirs();
		dir2.mkdirs();
		Random Class_R1 = new Random();
		Random Class_R2 = new Random();
		IaaSRequest iaaSRequestObj;

		int Class_1 = Class_R1.nextInt(10)+1;
		int Class_2 = Class_R2.nextInt(10)+11;
		int Class_3 = 100-(Class_1+Class_2);
		int req_new_ID;   

		int n_Class1_count = n_Class1;
		int n_Class2_count = n_Class2;
		int n_Class3_count = n_Class3;


		System.out.println("class 1  "+ n_Class1 +" class 2 " +n_Class2 + " class 3: "+n_Class3 );	  
		try{
			for(int i=0;i<numOfReq;i++){
				System.out.println("numOfReq = "+numOfReq);
				int loopBreaker = 0;
				do{
					req_new_ID = Request_generator.nextInt(numOfReq);
					while  ( random_number_list.contains(req_new_ID)) req_new_ID = Request_generator.nextInt(numOfReq); 
					System.out.println("\t req_new_ID = "+req_new_ID);
					random_number_list.add(req_new_ID);
	
					Write_Req=new FilesManipulation(0, REQ_TXT_FOLDR+"/req_"+req_new_ID+".txt");
					Write_Req_SecondNet=new FilesManipulation(0, REQ_SNET_FOLDR+"/reqSN_"+req_new_ID+".txt");
					fout = new FileOutputStream(REQ_SER_FOLDR+"/req_"+req_new_ID+".ser");
					oos = new ObjectOutputStream(fout);                     
					duration = (int) (MIN_REQ_DURATION + (int)(-Math.log(Math.random()) * (REQ_DURATION - MIN_REQ_DURATION))); // exponentially distributed duration
					iaaSRequestObj = new IaaSRequest(req_new_ID);
					Random Class_Selection = new Random();
					int class_id = Class_Selection.nextInt(100);
	
					if (class_id >= 0 && class_id<=Class_1 && n_Class1_count>0){
						System.out.println("req_new_ID  "+ req_new_ID);
						iaaSRequestObj = generateRandomRequest(duration,1);
						req=1;
						n_Class1_count--;
						done=true;
						req1_count++;
					}
	
					else  if (class_id > Class_1 && class_id<=Class_2 && n_Class2_count>0){
						System.out.println("req_new_ID  "+ req_new_ID);
						iaaSRequestObj = generateRandomRequest(duration,2);
						req=2;
						done=true;
						n_Class2_count--;
						req2_count++;
					}
	
					else  if (class_id > Class_2 && n_Class3_count>0){
						System.out.println("req_new_ID  "+ req_new_ID);
						iaaSRequestObj = generateRandomRequest(duration,3);
						req = 3;
						done = true;
						n_Class3_count--;
						req3_count++;
					}
					else{
	
						done = false;
						int  indx = random_number_list.indexOf(req_new_ID);
						random_number_list.remove(indx);
						System.out.println("class_id = "+class_id);
						System.out.println("Class_1 = "+Class_1+" \t"+" \t Class_2 = "+Class_2+" \t Class_3 = "+Class_3);
						System.out.println("n_Class1_count = "+n_Class1_count+" \t n_Class2_count = "+n_Class2_count+" \t"+" \t n_Class3_count = "+n_Class3_count);
					}
	
					if(loopBreaker >= 1000) {
						System.err.println("loopBreaker(limit = "+loopBreaker+") activated");
						done = true;
					}
					else loopBreaker++;
	
				}while(!done);
	
				PrintContainerValues.printGeneratedIaaSRequest(iaaSRequestObj);
	
				try{
					oos.writeObject(iaaSRequestObj);
					oos.close();
	
				}catch(Exception ex){
					ex.printStackTrace();
				}
				if (countk == k) {
					k = 0;
					while( k == 0) {
						k = poisson(POISSON_MEAN);
					}
					countk = 0;
	
					start = (int) ((p * total_simulation_time * POISSON_MEAN) / numOfReq);
					p++; 
				}
				time = (int) (start + ((countk + 1) * total_simulation_time * POISSON_MEAN) / (numOfReq * (k + 1)));
	
	
				countk ++;
				Write_Req_SecondNet.Create_request_file(""+iaaSRequestObj.getVRs().size()+" "+time+" "+duration);
				for(int SN=0;SN<iaaSRequestObj.getVRs().size();SN++){
					VResource temp_v=(VResource)iaaSRequestObj.getVRs().get(SN);
					Write_Req_SecondNet.Create_request_file("\r\n"+temp_v.getCpu());
					Write_Req_SecondNet.Create_request_file("\r\n"+temp_v.getMem());
					Write_Req_SecondNet.Create_request_file("\r\n"+temp_v.getStr());
					Write_Req_SecondNet.Create_request_file("\r\n"+temp_v.getBw());
					//	Write_Req_SecondNet.Create_request_file(""+temp_v.getCpu());
				}
				System.out.println("Req "+ i +" arrival " +time + " duration: "+duration +" class " + " classified "+ req );
				Write_Req.Create_request_file(""+req_new_ID);
				Write_Req.Create_request_file(""+time);
				Write_Req.Create_request_file(""+duration);
				// loop on the request to write in the file
	
				Write_Req.Close();
				Write_Req_SecondNet.Close();
			}
		} catch (IOException e) {
			System.err.println("GenerateRequests.createSpecificRequest: IO Exception caught");
			e.printStackTrace();
		}

		random_number_list.clear();
		System.out.println("Class 1 %: "+Class_1 +" Class 2 %: "+ Class_2 + " Class 3 %: "+ Class_3 );
		System.out.println("Class 1 : "+n_Class1 +" Class 2 : "+ n_Class2 + " Class 3 : "+ n_Class3 );
		System.out.println("Generated Class 1 : "+n_Class1_count +" \t Generated Class 2 : "+ n_Class2_count + " \t Generated Class 3 : "+ n_Class3_count );
		System.out.println("Generated Class 1 : "+req1_count +" \t Generated Class 2 : "+ req2_count + " \t Generated Class 3 : "+ req3_count );
	}

	// ============================================================================================================================== //
	// --------------------------------------------------- generateRandomRequest() -------------------------------------------------- //
	// ============================================================================================================================== //
	public static IaaSRequest generateRandomRequest(int life_t,int class_n){
		VResource temp = new VResource();
		int res_type;
		int numOfVResources=0;
		int bw=0;
		int cpu=0;
		int mem=0;
		int str=0;
		//double request_life_time;
		Random rand_generator=new Random();
		ArrayList<VResource>  vResObjListForReq = new ArrayList<VResource>();
		ConnectivityQos  constraints;
		IaaSRequest iaaSRequest = new IaaSRequest();

		if (class_n == 1){
			numOfVResources = rand_generator.nextInt(5)+1;//1-3-5
			bw = 80 + rand_generator.nextInt(20); // unified bandwidth between all VMs in the same request
			for (int i = 0; i<numOfVResources; i++){
				res_type = 1;
				cpu = rand_generator.nextInt(2)+3;  //3,4
				mem = rand_generator.nextInt(5)+4; // 4--> 8
				str = 10 *  (50+rand_generator.nextInt(50)); // 500-1000
				temp.setMem(mem);
				temp.setN_cores(cpu); // cpu and n_cores are equivelant
				temp.setCpu(cpu);
				temp.setStr(str);
				if (res_type == 1)		temp.setRes_type("compute");
				else if (res_type == 2)	temp.setRes_type("storage");
				else if (res_type == 3)	temp.setRes_type("network");
				temp.setInter_type("Ethernet");
				temp.setN_interfaces(1);
				temp.setBw(bw);
				vResObjListForReq.add(temp);
			}
		}
		else if (class_n==2){                                       
			numOfVResources=rand_generator.nextInt(4)+5;//3-5-8  was 3--3
			bw=50+rand_generator.nextInt(30); // unified bandwidth between all VMs in the same request
			for (int i=0;i<numOfVResources;i++){
				res_type=1;
				cpu = rand_generator.nextInt(Limits.MAXcpu/2)+1;		//1,2
				mem = rand_generator.nextInt(3)+2;  // 2-->4
				str =10 *  (30+rand_generator.nextInt(20)); // 300-500
				temp.setMem(mem);
				temp.setN_cores(cpu); // cpu and n_cores are equivelant
				temp.setCpu(cpu);
				temp.setStr(str);
				if (res_type==1)		temp.setRes_type("compute");
				else if (res_type==2)	temp.setRes_type("storage");
				else if (res_type==3)	temp.setRes_type("network");
				temp.setInter_type("Ethernet");
				temp.setN_interfaces(1);
				temp.setBw(bw);
				vResObjListForReq.add(temp);
			}
		}
		else if (class_n == 3){
			numOfVResources=rand_generator.nextInt(6)+8;//5-10 was 5-5   8-13
			bw=10+rand_generator.nextInt(40); // unified bandwidth between all VMs in the same request
			for (int i=0;i<numOfVResources;i++){
				res_type = 1;
				cpu = 1;  // all the time 1
				mem = rand_generator.nextInt(Limits.mem_upper/4)+1; // 1-- >2
				str = 10*(rand_generator.nextInt(30)+1);
				temp.setMem(mem);
				temp.setN_cores(cpu); // cpu and n_cores are equivelant
				temp.setCpu(cpu);
				temp.setStr(str);
				if (res_type==1)		temp.setRes_type("compute");
				else if (res_type==2)	temp.setRes_type("storage");
				else if (res_type==3)	temp.setRes_type("network");
				temp.setInter_type("Ethernet");
				temp.setN_interfaces(1);
				temp.setBw(bw);
				vResObjListForReq.add(temp);
			}
		}			
		constraints = new ConnectivityQos(bw ,"Ethernet"); // avg_bw/n_vrs,"Ethernet");
		//request_life_time = life_t;//life_time;
		iaaSRequest.setRequestLifeTime(life_t);
		iaaSRequest.setvResourceList(vResObjListForReq);
		iaaSRequest.setConstraints(constraints);
		return iaaSRequest;
	}

	// ============================================================================================================================== //
	// ---------------------------------------------------------- poisson() --------------------------------------------------------- //
	// ============================================================================================================================== //
	private static int poisson(double lambda) {
		Random arrival_generator = new Random();
		double p;
		int r;
		p = 0;
		r = 0;
		while (true) {
			p = p - Math.log(arrival_generator.nextDouble());/// (double)RAND_MAX);
			if (p < lambda) {
				r++;
			} else {
				break;
			}
		}
		return r;
	}

	
		/*
		// IaaSRequest attributes
	 	private static final long serialVersionUID = 1L;						//	Default Serialization id
		private double reqId = 0;
		private double arrivalTime;
		private double reqDuration = 1000;
		private double minReqDuration = 250;
		private double randMax = 2147483647;
		private int status = 1;
		private double requestLifeTime;
		private ConnectivityQos  constraints;
		private ArrayList<VResource> vResourceList;

		// VResource attributes
		 	String res_id			= "";
			String opeation;													// operation: switching, storage, compute
			double cpu				= 0.0;
			double n_cores			= 0.0;
			double mem				= 0.0;
			double storage			= 0.0;
			String vmm 				= "";
			String res_type			= "";     									// compute -Storage -network
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

			//interface_conn Attributes
			 	double bw;
				double delay;
				String inter_type;
		 */

}
