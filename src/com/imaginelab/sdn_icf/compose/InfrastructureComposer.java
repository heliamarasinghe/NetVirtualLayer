package com.imaginelab.sdn_icf.compose;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.Scanner;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileManager;
import com.imaginelab.sdn_icf.containers.IaaSRequest;
import com.imaginelab.sdn_icf.containers.VResource;

import static com.imaginelab.sdn_icf.main.Constants.BDLINK_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.CLOSENESS_FILE;

import static com.imaginelab.sdn_icf.main.Constants.COMPO_INIT_DBG;
import static com.imaginelab.sdn_icf.main.Constants.COMPO_EVENT_MAN_DBG;
import static com.imaginelab.sdn_icf.main.Constants.DISCVR_ALGO_DBG;
import static com.imaginelab.sdn_icf.main.Constants.COMPOS_ALGO_DBG;

import static com.imaginelab.sdn_icf.main.Constants.COMPO_PERF_FILE;

import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_UPDATE_DBG;
import static com.imaginelab.sdn_icf.main.Constants.HOST_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.NL;
import static com.imaginelab.sdn_icf.main.Constants.REQ_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.REQ_SER_FOLDR;
import static com.imaginelab.sdn_icf.main.Constants.REQ_TXT_FOLDR;
import static com.imaginelab.sdn_icf.main.Constants.SINGLE_TEST_REQ;
import static com.imaginelab.sdn_icf.main.Constants.TEST_REQ_SERF;
import static com.imaginelab.sdn_icf.main.Constants.TEST_REQ_TXTF;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_NS;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_QUERY_PREFIX;

public class InfrastructureComposer {
	
	static ArrayList<ArrayList<Object>> All_VRs;
	public static  int Functional_P_count=2;
	static OntModel vrpoolmodel;

	// ============================================================================================================================== //
	// --------------------------------------------------- compositionEventManager() ------------------------------------------------ //
	//	Method directly invoked by ProjectMain.main();						
	// ______________________________________________________________________________________________________________________________ //
	// ============================================================================================================================== //	
	public static void compositionEventManager(int numOfRequests) {
		String reqClass = "none";
		VResource vResourceObj;
		ArrayList<VResource> vResourceObjList = new ArrayList<VResource>();
		IaaSRequest iaaSRequest = new IaaSRequest();

		System.out.println("---------------------------------------------------------------------------------------------------------------");
		System.out.println("\t\t\t\t\t\t discovering Resources And Compose");
		System.out.println("---------------------------------------------------------------------------------------------------------------");

		try{
			String [][] closenessMat = getClosenessMatrix();
			PriorityQueue<IaaSRequest> IaaSReq_Q = initializeAndReadFiles(numOfRequests);
			Multimap<String, String> connectedToVResMultiMap = constructConnectedToMultiMap();
			
			while (!IaaSReq_Q.isEmpty()) {
				iaaSRequest = IaaSReq_Q.poll();
				vResourceObjList = iaaSRequest.getVRs();
				vResourceObj = vResourceObjList.get(0);
				if ((vResourceObj.getCpu() > 2 && vResourceObj.getCpu() <=4 ) || ( vResourceObj.getMem() >4 &&   vResourceObj.getMem() <=8 )) reqClass="A";
				else if ((vResourceObj.getCpu() >1 && vResourceObj.getCpu() <=2) ||  (vResourceObj.getMem() >2 &&   vResourceObj.getMem() <=4 )) reqClass="B";
				else if ((vResourceObj.getCpu() >0 && vResourceObj.getCpu() <=1) && ( vResourceObj.getMem() >2 )) reqClass="B";
				else if ((vResourceObj.getCpu() >0 && vResourceObj.getCpu() <=1) &&( vResourceObj.getMem() >=2 ) && ( vResourceObj.getStr() >=250)) reqClass="B";
				else reqClass = "C";
				String reqClassByUser = iaaSRequest.getReqClass();
				if(!reqClassByUser.equals(reqClass)) System.err.println("reqClassByComposer["+reqClass+"] different from reqClassByUser["+reqClassByUser+"]");

				int reqId = (int)iaaSRequest.getReqId();
				
				System.out.print("\nPROCESSING Request :"+ reqId + " of class = "+reqClass);

				if(iaaSRequest.getStatus().equals(Status.ARRIVAL)){// handle arrival request ,start to discover and compose
					System.out.println("\t\t ---------> ARRIVAL");
					
					int reqAcceptReject = 0;								//	rejected at discovery[-2], rejected at composition[-1], accepted[1]
					long reqDiscoveryST = 0L;
					long reqDiscoveryET = 0L;
					long reqProcessingST = 0L;
					long reqProcessingET = 0L;
					long reqCompositionST = 0L;
					long reqCompositionET = 0L;
					
					reqProcessingST = System.currentTimeMillis();
					reqDiscoveryST = reqProcessingST;
					ArrayList<VResource> discoveredVResList = discoveryAlgorithm(iaaSRequest, reqClass, closenessMat);
					reqDiscoveryET = System.currentTimeMillis();
					
					
					String discoveredVResString = "[";
					for(VResource vRes: discoveredVResList)
						discoveredVResString += vRes.getResUri()+", ";
					discoveredVResString += "]";
					
					
					int numberOfResDiscovered = discoveredVResList.size();
					int numberOfResRequested = iaaSRequest.getVRs().size();
					ConnectivityQos reqConstraints = iaaSRequest.getConstraints();

					CompositionResult compositionResult = null;

					

					//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
					//	Checking for sufficient number of resources discovered and proceed to composition
					//-----------------------------------------------------------------------------------------------------------------------------------------------------------------
					if  (numberOfResDiscovered != numberOfResRequested){
						reqProcessingET = System.currentTimeMillis();
						reqAcceptReject = -2;
						System.out.println("\t\t\tRequest : "+reqId+" rejected at discovery");
						if (numberOfResDiscovered > numberOfResRequested) System.err.println("More than enough resources has been discovered for "+reqId+". But rejected");
					}
					else{

						if(COMPO_EVENT_MAN_DBG)System.out.println("Request : "+reqId+" forwarded for composition");
						reqCompositionST = System.currentTimeMillis();
						compositionResult = compositionAlgorithm(reqId, discoveredVResList, reqConstraints, connectedToVResMultiMap, closenessMat);
						reqCompositionET = System.currentTimeMillis();
						if(COMPO_EVENT_MAN_DBG) System.out.println("compositionAlgorithm returned compositionResult to compositionEventManager");
					
						if (compositionResult.getreservedVResListForReq().size() > 0){
							reqProcessingET = System.currentTimeMillis();
							reqAcceptReject = 1;
					
							if(COMPO_EVENT_MAN_DBG)System.out.println("\n\tCompositionEventManager: composition is successful for request "+reqId);
							
							ArrayList<VResource> reservedVResList = compositionResult.getreservedVResListForReq();
							HashMap <String, HashMap <String, VResource>> partitionedLinkVResForReq = compositionResult.getPartitionedLinkVResForReq();
							
							if(VRPOOL_UPDATE_DBG){
								System.out.println("\n\t compositionEventManager.reservedVResList.size() = "+reservedVResList.size());
								for(VResource reservedVRes : reservedVResList) System.out.println("\t\t"+reservedVRes.getResUri());
								System.out.println("\n compositionEventManager.partitionedLinkVResForReq.size() = "+partitionedLinkVResForReq.size());
								for(String origLinkVResId : partitionedLinkVResForReq.keySet()){
									HashMap<String, VResource> innerMap = partitionedLinkVResForReq.get(origLinkVResId);
									System.out.println("\t"+origLinkVResId+"-->");
									for(String newLinkId : innerMap.keySet())
										System.out.println("\t\t"+newLinkId+"\t:\t"+innerMap.get(newLinkId).getBw());
								}
							}

							updateIaaSRequestsInVrPool(iaaSRequest, reqClass, reservedVResList, true);
							updateVResourcesInVrPool(reservedVResList, iaaSRequest.getReqId(), true);  // update the resources to be reserved  Composed_VRs_List
							updateLinkVResInVrPool(partitionedLinkVResForReq, iaaSRequest.getReqId(), true);

							saveVrPerReq(reservedVResList, (int) iaaSRequest.getReqId());
							reservedVResList.clear();

							System.out.println("\tResources reservation successful for Request : "+(int)iaaSRequest.getReqId());

							iaaSRequest.setStatus(Status.ACCEPTED);

							// Re-queuing has been desabled by heli for departure event. IaaSReq_Q.add(temp_iaaS_req);
						}
						else{
							reqProcessingET = System.currentTimeMillis();
							System.err.println("Request: "+iaaSRequest.getReqId()+" Rejected at Composition");
							reqAcceptReject = -1;
						}
					}	
					
					String discoveryTime = Long.toString(reqDiscoveryET-reqDiscoveryST);
					String compositionTime = Long.toString(reqCompositionET-reqCompositionST);
					String reqProcessignTime = Long.toString(reqProcessingET-reqProcessingST);
					

					try{
						PrintWriter printwriter = new PrintWriter(new FileWriter(COMPO_PERF_FILE, true));
						printwriter.println(reqId +"\t"+ numberOfResRequested + "\t" + reqAcceptReject +"\t"+ discoveryTime +"\t"+ discoveredVResString +"\t"+ compositionTime  +"\t"+ reqProcessignTime);
						printwriter.close();
						} catch(IOException ioEx){
							System.err.println("InfrastructureComposer.compositionEventManager: File writer exception caught when writing performance results");
							ioEx.printStackTrace();
						}
					
					
				}	// END if(ARRIVAL)
				// handle departure of the LinkList<IaaSRequest>request , free resources and update the poool
				// Calling this block has been disabled by heli
				else if (iaaSRequest.getStatus().equals(Status.DEPARTURE)){
					System.out.println("Request: "+ iaaSRequest.getReqId() +" Departed");
					// free the resources given req_id
					vResourceObjList = Retrieve_VR_per_request((int)iaaSRequest.getReqId());
					System.out.println("Updating VrPool for request : "+iaaSRequest.getReqId());
					updateVResourcesInVrPool(vResourceObjList, iaaSRequest.getReqId(), false);   // free the resources to be free
				}
			} // END while (!IaaSReq_Q.isEmpty())
			
		}
		catch (ClassNotFoundException noClsEx){
			noClsEx.printStackTrace();
		}
		catch (ClassCastException clsCstEx){
			System.err.println("CompositionSimulation.prepareSimulation(): ClassCastException Caught");
			System.err.println("\t Most possible reason of this error is change in class structure or attributes in IaaSRequest class after generating searialized request files");
			System.err.println("\t Re-creation of request files using GenRandomRequests class will solve the problem\n");
			clsCstEx.printStackTrace();
		}
		catch (IOException ioEx){
			ioEx.printStackTrace();
		}
	}
	
	
	// ============================================================================================================================== //
	// --------------------------------------------------- initializeAndReadFiles() ------------------------------------------------- //
	//  Invokes Load_matrix_dimesnsion(), Load_connection_matrix(), Load_closeness_matrix(), load_Total_dataCenter_capacity(),
	// 		Load_Req_file_data() and Load_Req_file_ser() methods to read requests and populate priority queue: IaasReq_Q						
	// ______________________________________________________________________________________________________________________________ //
	// ============================================================================================================================== //
	public static PriorityQueue<IaaSRequest> initializeAndReadFiles(int numOfRequests) throws ClassNotFoundException, ClassCastException, IOException {
		vrpoolmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		vrpoolmodel.read(FileManager.get().open(VRPOOL),"RDF/XML"); 
		if(COMPO_INIT_DBG)System.out.println("vrpoolmodel successfully loaded");

		Comparator<IaaSRequest> comparator = new StringLengthComparator();
		PriorityQueue<IaaSRequest> IaaSReq_Q = new PriorityQueue<IaaSRequest>(10000, comparator);
		All_VRs = new ArrayList<ArrayList<Object>>();

		ArrayList<String> reqIdTimeDurationList = new ArrayList<String>();
		IaaSRequest iaaSRequestObj = new IaaSRequest();
		// read requests
		for (int reqId = 0; reqId < numOfRequests; reqId++) {
			reqIdTimeDurationList = loadDataFromReqFile(reqId);
			iaaSRequestObj = loadSearealizedIaaSReq(reqId);
			iaaSRequestObj.setReqId(Integer.parseInt((String)reqIdTimeDurationList.get(0)));
			iaaSRequestObj.setArrivalTime(Double.parseDouble((String)reqIdTimeDurationList.get(1)));
			iaaSRequestObj.setRequestLifeTime(Double.parseDouble((String)reqIdTimeDurationList.get(2)));
			iaaSRequestObj.setStatus(Status.ARRIVAL);
			IaaSReq_Q.add(iaaSRequestObj);
		}
		return IaaSReq_Q;
	}

	// ============================================================================================================================== //
	// ------------------------------------------------ constructConnectedToMultiMap() ---------------------------------------------- //
	// ============================================================================================================================== //
	public static Multimap<String, String> constructConnectedToMultiMap(){
		Multimap<String, String> connectedToVResMultiMap = ArrayListMultimap.create();
		
		if(COMPO_INIT_DBG)System.out.println("\n\n Querying for vResources related by connectedto object property");
		String selectString = "SELECT DISTINCT ?vRes1 ?vRes2";
		String whereString = "WHERE {?vRes1 a cloud_ont:VR. ?vRes1 cloud_ont:connectedto ?vRes2. ?vRes1 cloud_ont:has_state ?state. ?state rdf:type cloud_ont:free.}";
		Query hasVNodeQry = QueryFactory.create(VRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution hasVNodeQryExec = QueryExecutionFactory.create(hasVNodeQry, vrpoolmodel) ;
		ResultSet hasVNodeRsltset = hasVNodeQryExec.execSelect() ;
		while (hasVNodeRsltset.hasNext()){
			QuerySolution hasVNodeSolution = hasVNodeRsltset.nextSolution();
			String vRes1 = hasVNodeSolution.get("vRes1").asResource().toString().split("#")[1];
			String vRes2 = hasVNodeSolution.get("vRes2").asResource().toString().split("#")[1];
			connectedToVResMultiMap.put(vRes1, vRes2);
		}
		hasVNodeQryExec.close() ;
		if (COMPO_EVENT_MAN_DBG) for(String vRes1 : connectedToVResMultiMap.keySet()){
			Collection<String> connectedToVRes = connectedToVResMultiMap.get(vRes1);
			System.out.println("\tvRes connected to "+vRes1);
			for(String vRes2 : connectedToVRes)
				System.out.println("\t\t"+vRes2);
		}
		return connectedToVResMultiMap;
	}
	
	// ============================================================================================================================== //
	// ----------------------------------------------------- getClosenessMatrix() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static String[][] getClosenessMatrix() throws FileNotFoundException{
		
		Scanner matSizeScanner = new Scanner (new File(CLOSENESS_FILE));
		int size = 0;
		while(matSizeScanner.hasNextLine()){
		    ++size;
		    matSizeScanner.nextLine();
		}
		matSizeScanner.close();
		
		String[][] closenessMat = new String[size][size];
		Scanner matContentScanner = new Scanner(new File(CLOSENESS_FILE));
		for(int i = 0; i < size; ++i){
		    for(int j = 0; j < size; ++j){
		        if(matContentScanner.hasNext())
		        	closenessMat[i][j] = matContentScanner.next();
		    }
		}
		matContentScanner.close();
		
		if(COMPO_INIT_DBG){
			System.out.println("closenessMat size = "+size);
			for(int i = 0; i < size; ++i){
			    for(int j = 0; j < size; ++j)
			    	System.out.print(closenessMat[i][j]+"\t");
			    System.out.println();
			}
		}
		return closenessMat;
	}

	// ============================================================================================================================== //
	// ---------------------------------------------------- loadDataFromReqFile() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static ArrayList<String> loadDataFromReqFile(int reqId) throws FileNotFoundException{
		String reqTxtFile = SINGLE_TEST_REQ? TEST_REQ_TXTF+"/req_"+ reqId +".txt" : REQ_TXT_FOLDR+"/req_"+ reqId +".txt";
		String sCurrentLine;
		ArrayList<String> reqIdTimeDurationList = new ArrayList<String>();
		if(COMPO_INIT_DBG)System.out.println("\n"+reqId+"\tReading request txt file = "+reqTxtFile+"\n\treqId \tarrive \tDuration");
		BufferedReader File_reader= new BufferedReader( new FileReader(reqTxtFile));
		try {
			while ((sCurrentLine = File_reader.readLine()) != null) {
				if(COMPO_INIT_DBG)System.out.println("\t"+sCurrentLine);
				String[] reqReadSplit= sCurrentLine.split("\t+");
				reqIdTimeDurationList.add(reqReadSplit[0]); // request id
				reqIdTimeDurationList.add(reqReadSplit[1]); // request  arrival time
				reqIdTimeDurationList.add(reqReadSplit[2]); // request duration
			}
			// request rest of parameters all vrs and constraints
		} catch (IOException ioEx) {
			System.err.println("CompositionSimulation.Load_Req_file_data: IO Exception caught");
			ioEx.printStackTrace();
		} finally {
			try {
				if (File_reader != null)File_reader.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return reqIdTimeDurationList;
	}
	// ============================================================================================================================== //
	// ---------------------------------------------------- loadSearealizedIaaSReq() ------------------------------------------------ //
	// ============================================================================================================================== //
	public static IaaSRequest loadSearealizedIaaSReq(int reqId)throws ClassNotFoundException, ClassCastException, IOException{
		IaaSRequest iaaSRequestObj = null;

		String reqSerFile = SINGLE_TEST_REQ ? TEST_REQ_SERF+"/req_"+reqId+".ser": REQ_SER_FOLDR+"/req_"+reqId+".ser";
		if(COMPO_INIT_DBG)System.out.println("\tReading request ser file  = "+reqSerFile);
		FileInputStream fileInputStream = new FileInputStream(reqSerFile);
		ObjectInputStream objInputStream = new ObjectInputStream(fileInputStream);
		iaaSRequestObj = (IaaSRequest) objInputStream.readObject();
		objInputStream.close();

		return iaaSRequestObj;
	}

	// ============================================================================================================================== //
	// --------------------------------------------- Calculate_Resources_utilization() ---------------------------------------------- //
	// ============================================================================================================================== //	
//	public static double Calculate_Resources_utilization() throws IOException{
//		String Total_res = Query_Total_resources_and_count();
//		String reserved_res = Query_reserved_resources_and_count();
//		String Free_res = Query_Free_resources_and_count();
//
//		double server_utilization;
//		if (Double.parseDouble(Total_res) == Double.parseDouble(reserved_res) + Double.parseDouble(Free_res))
//			server_utilization = (Double.parseDouble(reserved_res)/Double.parseDouble(Total_res)*100);
//		else
//			server_utilization=0;
//		return server_utilization;
//	}
	// ============================================================================================================================== //
	// ---------------------------------------------- Query_Total_resources_and_count()---------------------------------------------- //
	// ============================================================================================================================== //
	/*public static String Query_Total_resources_and_count(){
		String sqlssparql = VRPOOL_QUERY_PREFIX +
				"SELECT (COUNT(*) AS ?count)"+
				" WHERE {?subject rdf:type cloud_ont:VR."+
				" {?subject cloud_ont:provideservice cloud_ont:compute1.}"+
				" union {?subject cloud_ont:provideservice cloud_ont:storage1.}"+
				"}";

		Query qq=QueryFactory.create(sqlssparql);
		QueryExecution qex=QueryExecutionFactory.create(qq,vrpoolmodel);
		try {
			ResultSet res=qex.execSelect();
			ByteArrayOutputStream baos= new ByteArrayOutputStream();
			ResultSetFormatter.outputAsCSV(baos, res);
			String answer= baos.toString();
			String[] tem_s=answer.split("[,\r\n]+");

			if (res!=null) return tem_s[1];
			else return "";
		}
		finally{
			qex.close();
		}
	}*/
	// ============================================================================================================================== //
	// -------------------------------------------- Query_reserved_resources_and_count()-------------------------------------------- //
	// ============================================================================================================================== //
	/*public static String Query_reserved_resources_and_count(){
		String sqlssparql= VRPOOL_QUERY_PREFIX +
				"SELECT (COUNT(*) AS ?count)"+
				" WHERE {?subject rdf:type cloud_ont:VR."+
				" ?subject rdf:type cloud_ont:reservedResources."+
				" {?subject cloud_ont:provideservice cloud_ont:compute1.}"+
				" union {?subject cloud_ont:provideservice cloud_ont:storage1.}"+
				"}";

		Query qq=QueryFactory.create(sqlssparql);
		QueryExecution qex=QueryExecutionFactory.create(qq,vrpoolmodel);
		try {
			ResultSet res=qex.execSelect();
			ByteArrayOutputStream baos= new ByteArrayOutputStream();
			ResultSetFormatter.outputAsCSV(baos, res);

			String answer= baos.toString();
			String[] tem_s=answer.split("[,\r\n]+");

			if (res!=null) return tem_s[1];
			else return "";
		}
		finally{
			qex.close();
		}
	}*/

	// ============================================================================================================================== //
	// ---------------------------------------------- Query_Free_resources_and_count() ---------------------------------------------- //
	// ============================================================================================================================== //	
	/*public static String Query_Free_resources_and_count(){
		String sqlssparql= VRPOOL_QUERY_PREFIX +
				"SELECT (COUNT(*) AS ?count)"+
				" WHERE {?subject rdf:type cloud_ont:VR."+
				" ?subject rdf:type cloud_ont:FreeResources."+
				" {?subject cloud_ont:provideservice cloud_ont:compute1.}"+
				" union {?subject cloud_ont:provideservice cloud_ont:storage1.}"+
				"}";

		Query qq=QueryFactory.create(sqlssparql);
		QueryExecution qex=QueryExecutionFactory.create(qq,vrpoolmodel);
		try {
			ResultSet res=qex.execSelect();
			ByteArrayOutputStream baos= new ByteArrayOutputStream();
			ResultSetFormatter.outputAsCSV(baos, res);
			String answer= baos.toString();
			String[] tem_s=answer.split("[,\r\n]+");

			if (res!=null) return tem_s[1];
			else return "";
		}
		finally{
			qex.close();
		}
	}*/
	

	



	// ================================================================================================================================================================= //
	// -------------------------------------------------------------------------- discoveryAlgorithm() ----------------------------------------------------------------- //
	// ================================================================================================================================================================= //
	public static ArrayList<VResource> discoveryAlgorithm(IaaSRequest iaaSRequest, String reqClass, String [][] closenessMat) throws IOException{

		double maxSimilarityScore = 0;
		int indxOfMaxScoredVRes = 0;
		boolean relaxedFlag = false;
		int relaxedNumOfTimes = 0;
		String machingVRQuery = "";
		
		ArrayList<VResource> discoveredVResList = new ArrayList<VResource>();
		VResource reqVResObj = new VResource();
		ArrayList<VResource> reqVRList = iaaSRequest.getVRs();
		HashMap<String, String> selectedVResToReqVResMap = new HashMap<String, String>();

		if(DISCVR_ALGO_DBG){
			System.out.println("\nstartDiscovery.requestedVResList.size() = "+iaaSRequest.getVRs().size());
			for(VResource requestedVRes : iaaSRequest.getVRs()) System.out.println("\t"+requestedVRes.getResUri());
		}
		reqVResObj = reqVRList.get(0);
		for(int reqVRItr=0; reqVRItr<reqVRList.size(); reqVRItr++){										// for each VResource in the request
			reqVResObj = reqVRList.get(reqVRItr);
			if(DISCVR_ALGO_DBG)System.out.println("\n\n*** Iteration "+reqVRItr+" ***   \n\tLooking for matching resource for : "+reqVResObj.getResUri());
			//prepare a query for that VR
			if (relaxedFlag && relaxedNumOfTimes<2){									// if relaxed flag set and number of times relaxed is not more than once, query for VMs with relaxed values
				machingVRQuery = queryFuncClassLimits(reqVResObj.getCpu(), reqVResObj.getN_cores(), 0, 0, reqVResObj.getRes_type(), reqClass);
				relaxedFlag = false;
			}
			else{																		// if relaxed flag not set or number of times relaxed is more than once, query with specific values
				machingVRQuery = queryFuncClassLimits(reqVResObj.getCpu(), reqVResObj.getN_cores(),reqVResObj.getMem(),reqVResObj.getStr(), reqVResObj.getRes_type(), reqClass);
				relaxedFlag = false;	
				relaxedNumOfTimes = 0;
			}

			String matchingVRQueryRSet = queryOntology(machingVRQuery);
			// parse the returened result set into VRs
			ArrayList<VResource> matchingVResObjList = new ArrayList<VResource>();

			String[] matchinVRSplitArray = matchingVRQueryRSet.split("[,\r\n]+");

			// fill in the VResources structure with the data got from the OWL
			for(int matchVrItr = Functional_P_count; matchVrItr<matchinVRSplitArray.length; matchVrItr += Functional_P_count-1){
				VResource matchedVResource = new VResource();
				// get all the properties of the matched VRs for further similarity evaluation and fill a VR variable

				matchedVResource = qryForPropertiesByVResId(matchinVRSplitArray[matchVrItr+1],vrpoolmodel);
				matchingVResObjList.add(matchedVResource);
			}
			matchinVRSplitArray = null;
			if (matchingVResObjList.size()==0){			// If not matching resources found, set relax flat and re-run the loop
				if (relaxedNumOfTimes==0){
					reqVRItr = reqVRItr-1;
					relaxedFlag = true;
					relaxedNumOfTimes++;
					continue;
				}
				else
					continue;
			}
			// start to evaluate the returned VR list against the requested one

			if(DISCVR_ALGO_DBG){
				System.out.println("\nstartDiscovery.matchingVResObjList.size() : "+matchingVResObjList.size());
				for(VResource matchedVRes : matchingVResObjList) System.out.println("\t"+matchedVRes.getResUri());
			}
			double [] numaricalSimilarityScoreArray = new double [matchingVResObjList.size()];
			double [] closenessValueArray = new double [matchingVResObjList.size()];

			VResource previouslyDiscoveredVRes = new VResource();
			numaricalSimilarityScoreArray = calculateSimilarityNumerical(matchingVResObjList, reqVResObj);
			
			// optimization to be near the source node (Pre discovered node)
			if(DISCVR_ALGO_DBG){
				System.out.println("startDiscovery.Discovered_VRs_List.size() = "+discoveredVResList.size()+" \t(previously discevered maxSimilarityScoredMatchingVRes)");
				for (VResource discVRes : discoveredVResList) System.out.println(discVRes.getResUri());
			}

			if (reqVRItr>0 && reqVRItr == discoveredVResList.size() ){
				if (discoveredVResList.size()>= reqVRItr-1 && matchingVResObjList.size() >= reqVRItr-1){
					previouslyDiscoveredVRes = discoveredVResList.get(reqVRItr-1);
					if(DISCVR_ALGO_DBG) System.out.println("pre_temp_VR = "+previouslyDiscoveredVRes.getResUri());
					closenessValueArray = calculateSimilarityCentrality(matchingVResObjList, previouslyDiscoveredVRes, closenessMat);   	// get closeness similarity values to previously discovered VResource
					for (int matchingVResItr=0; matchingVResItr<matchingVResObjList.size(); matchingVResItr++)
						numaricalSimilarityScoreArray[matchingVResItr] = numaricalSimilarityScoreArray[matchingVResItr]+100 - (10 * closenessValueArray[matchingVResItr]);
				}
				// get maximum evaluated score and its index	// get the max and normalize to make a threshold
			}
			if(DISCVR_ALGO_DBG){
				System.out.println("numaricalSimilarityScoreArray just after combining with closeness values  "+numaricalSimilarityScoreArray.length);
				for (double simScore : numaricalSimilarityScoreArray) System.out.println("\t"+simScore);
			}

			for(int matchingVResItr=0; matchingVResItr<matchingVResObjList.size(); matchingVResItr++)
				if(selectedVResToReqVResMap.containsKey(matchingVResObjList.get(matchingVResItr).getResUri())){
					if(DISCVR_ALGO_DBG)System.out.println("Setting simScore of already selected  VRes "+matchingVResObjList.get(matchingVResItr).getResUri()+" from "+numaricalSimilarityScoreArray[matchingVResItr]+" to Zero");
					numaricalSimilarityScoreArray[matchingVResItr] = 0;
				}

			if(DISCVR_ALGO_DBG){
				System.out.println("numaricalSimilarityScoreArray after setting zeros for selected = "+numaricalSimilarityScoreArray.length);
				for (double simScore : numaricalSimilarityScoreArray) System.out.println("\t"+simScore);
			}

			maxSimilarityScore = 0;
			indxOfMaxScoredVRes = 0;
			for(int matchingVResItr=0; matchingVResItr<matchingVResObjList.size(); matchingVResItr++)
				if (numaricalSimilarityScoreArray[matchingVResItr] >maxSimilarityScore){
					maxSimilarityScore = numaricalSimilarityScoreArray[matchingVResItr];
					indxOfMaxScoredVRes = matchingVResItr;
				}
			VResource maxSimilarityScoredMatchingVRes = matchingVResObjList.get(indxOfMaxScoredVRes);

			if(DISCVR_ALGO_DBG) System.out.println("\nFound VRes : "+maxSimilarityScoredMatchingVRes.getResUri()+"  Index : "+indxOfMaxScoredVRes+"  has the highest score : "+maxSimilarityScore);
			if (maxSimilarityScore ==0){
				if (relaxedNumOfTimes==0){	// if number of times relaxed is zero
					reqVRItr=reqVRItr-1;
					relaxedFlag = true;
					relaxedNumOfTimes++;
					if(DISCVR_ALGO_DBG) System.out.println("SimScore of found VRes is ZERO. Hence relaxing request parameters of Req :"+iaaSRequest.getReqId());
					continue;
				}
				else//this means the query has been already relaxed and no suitable resources found
					continue;
			}
			numaricalSimilarityScoreArray = null;
			closenessValueArray = null;

			if(DISCVR_ALGO_DBG)System.out.println("Adding max similarity scored VRs : "+maxSimilarityScoredMatchingVRes.getResUri()+" into discoveredVResList");
			discoveredVResList.add(maxSimilarityScoredMatchingVRes);

			selectedVResToReqVResMap.put(maxSimilarityScoredMatchingVRes.getResUri(), reqVResObj.getResUri());	// put selected VResources and requested VRes into hashmap
			matchingVResObjList.clear();
			//evaluate similarity among the query result set, rank the resources according to similarity, store the highest score in Can_VRs_List, Can_VRs_List.add(VR_obj)
		}// End of for each requested VR in the IaaSRequest

		if(DISCVR_ALGO_DBG){
			System.out.println("\nstartDiscovery.selectedVResToReqVResMap.size() after discovery for resources: "+selectedVResToReqVResMap.size());
			for(String selectedVRes : selectedVResToReqVResMap.keySet()) System.out.println("\t"+selectedVRes+" : "+selectedVResToReqVResMap.get(selectedVRes));
		}
		return discoveredVResList;
	} 	// End of startDiscovery()

	// ============================================================================================================================== //
	// --------------------------------------------------------- queryOntology() ---------------------------------------------------- //
	// ============================================================================================================================== //
	public static String queryOntology(String para){
		Query qq = QueryFactory.create(para);
		QueryExecution qex = QueryExecutionFactory.create(qq,vrpoolmodel);
		try {
			ResultSet res = qex.execSelect();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsCSV(baos, res);
			String answer = baos.toString();
			if (res!=null)
				return answer;
			else
				return "";
		}
		finally{
			qex.close();
		}
	}

	// ============================================================================================================================== //
	// -------------------------------------------------- qryForPropertiesByVResId() ------------------------------------------------ //
	// ============================================================================================================================== //
	public static VResource qryForPropertiesByVResId(String vResId, OntModel model11){
		VResource vResObj = new VResource();

		String VRSQL = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX cloud_ont: <http://www.semanticweb.org/kmetw028/ontologies/2013/11/untitled-ontology-52#>"+
				"SELECT DISTINCT ?vr ?property ?value "+
				"WHERE {?vr rdf:type cloud_ont:VR."+
				"?vr ?property ?value ."+
				"FILTER ((?vr) = cloud_ont:"+vResId+") "+  //FILTER(?x =\"1\")" +    "+d+")}";*
				"FILTER ((?property) != rdf:type) "+
				"FILTER ((?property) != owl:differentFrom) "+
				"FILTER (!isBlank(?value)) "+
				"FILTER ((?property) != owl:sameAs) "+
				"}";
		Query qq2 = QueryFactory.create(VRSQL);
		QueryExecution qex = QueryExecutionFactory.create(qq2,model11);
		try {
			ResultSet res = qex.execSelect();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ResultSetFormatter.outputAsCSV(baos, res);
			String answer= baos.toString();
			String[] tem_s3;
			String[] tem_s=answer.split("[,\r\n]+");
			for (int ii=4;ii<tem_s.length;ii+=3){
				String[] tem_s2=tem_s[ii].split("#");
				if (tem_s2.length==1)
					System.out.println(tem_s2[0] + " error in parsing");
				else{
					if (tem_s2[1].equals("cpu")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setCpu(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("bw")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setBw(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("memory")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setMem(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("storage")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setStr(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("n_cores")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setN_cores(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("loss_rate")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setLoss_rate(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("delay")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setDelay(Double.parseDouble(tem_s3[0]));
					}
					else if (tem_s2[1].equals("location")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setLoc(tem_s3[0]);
					}
					else if (tem_s2[1].equals("res_id")){
						tem_s3=tem_s[ii+1].split("#");
						vResObj.setResUri(tem_s3[0]);
					}
				}
			}
		}

		finally{
			qex.close();
		}
		return vResObj;
	}
	// ============================================================================================================================== //
	// -------------------------------------------------- calculateSimilarityNumerical() -------------------------------------------- //
	// Compute the similarity between sibling concepts
	// ============================================================================================================================== //
	public static double[] calculateSimilarityNumerical(ArrayList<VResource> connectedVRList, VResource vResUnderEval){  // connectedVRList, vResUnderEval
		double[] numaricalSimilarityScoreArray = new double[connectedVRList.size()];
		VResource vResource;
		double score=0;
		for (int i=0; i<connectedVRList.size(); i++ ){
			vResource = new VResource();
			vResource = connectedVRList.get(i);
			score += evaluateSimilarityConcept(vResource.getCpu(), vResUnderEval.getCpu(),"cpu");
			score += evaluateSimilarityConcept(vResource.getMem(), vResUnderEval.getMem(),"mem");
			score += evaluateSimilarityConcept(vResource.getStr(), vResUnderEval.getStr(),"storage");
			score += evaluateSimilarityConcept(vResource.getBw(), vResUnderEval.getBw(),"bw");
			score += evaluateSimilarityConcept(vResource.getDelay(), vResUnderEval.getDelay(),"n_cores");
			numaricalSimilarityScoreArray[i] = score/5;
			score = 0;
		}
		return numaricalSimilarityScoreArray;
	}

	// ============================================================================================================================== //
	// -------------------------------------------------- evaluateSimilarityConcept() ----------------------------------------------- //
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
	// ============================================================================================================================== //
	public static double evaluateSimilarityConcept(double Req, double Adv,String resType){
		double result=0;
		if (resType.equals("cpu"))
			// get max min from the limits
			result=1-Math.abs((double)(Req-Adv)/(Limits.MAXcpu-Limits.MINcpu));
		else if (resType.equals("mem"))
			result=1-Math.abs((double)(Req-Adv)/(Limits.mem_upper-Limits.mem_lower));
		else if (resType.equals("storage"))
			result=1-Math.abs((double)(Req-Adv)/(int)(Limits.str_upper-Limits.str_lower));
		else if (resType.equals("bw"))
			result= (1-Math.abs((double)(Req-Adv)/(Limits.bw_upper-Limits.bw_lower)));
		else if (resType.equals("n_cores"))
			result=1-Math.abs((int)(Req-Adv)/(Limits.n_cores_upper-Limits.n_cores_lower));
		return result;
	}
	// ============================================================================================================================== //
	// ----------------------------------------------- calculateSimilarityConstraintsQoS() ----------------------------------------------- //
	// ============================================================================================================================== //
	public static double[]  calculateSimilarityConstraintsQoS(ArrayList<VResource> connectedVRList, ConnectivityQos reqNetQoSConstraints){	//connectedVRList, req_constraints
		double[] qosSimilarityScoreArray = new double[connectedVRList.size()];
		VResource temp;
		double score=0;
		// we have to assign an order to the concepts
		for (int i=0;i<connectedVRList.size();i++ ){
			temp = new VResource();
			temp = connectedVRList.get(i);
			score += evaluateSimilarityConcept(temp.getBw(),reqNetQoSConstraints.getBw(),"bw");
			qosSimilarityScoreArray[i]=score;
			score=0;
		}
		return qosSimilarityScoreArray;
	}
	// ============================================================================================================================== //
	// ------------------------------------------------- calculateSimilarityCentrality() -------------------------------------------- //
	// ============================================================================================================================== //
	public static double[] calculateSimilarityCentrality(ArrayList<VResource> connectedVResList, VResource vResUnderEval, String [][] closenessMat){
		double[] closenessValueArray = new double[connectedVResList.size()];
		VResource vResObj;
		for (int conVRItr=0; conVRItr<connectedVResList.size(); conVRItr++ ){
			vResObj = new VResource();
			vResObj = connectedVResList.get(conVRItr);
			
			int rawNum = 0;
			String vResLoc = vResObj.getLoc();
			for (int i=0; i<closenessMat.length; i++)
				if (closenessMat[0][i].equals(vResLoc)){
					rawNum = i;
					break;
				}
			int colNum = 0;
			String vResEvalLoc = vResUnderEval.getLoc();
			for (int i=0; i<closenessMat.length; i++)
				if (closenessMat[i][0].equals(vResEvalLoc)){
					colNum = i;
					break;
				}
			closenessValueArray[conVRItr] = Double.parseDouble(closenessMat[rawNum][colNum]);
		}
		return closenessValueArray;
	}

	// ============================================================================================================================== //
	// -------------------------------------------------- compositionAlgorithm()----------------------------------------------------- //
	// ============================================================================================================================== //
	public static CompositionResult compositionAlgorithm(double iaaSReqId, ArrayList<VResource> discoveredVResList, ConnectivityQos reqNetQoSConstraints, Multimap<String, String> connectedToVResMultiMap, String [][] closenessMat) throws IOException{
		ArrayList<String> Exception_list = new ArrayList<String>();
		ArrayList<String> reservedVResIdsForReq = new ArrayList<String>();
		ArrayList<VResource> reservedVResListForReq = new ArrayList<VResource>();
		ArrayList<VResource> connectedVRList = new ArrayList<VResource>();
		boolean sufficintLinkBw = false;
		boolean compositionFailed = false;
		ArrayList<VResource> reservedVResListForPath = new ArrayList<VResource>();
		reservedVResListForReq.clear();
		double [] closenessValueArray, numaricalSimilarityScoreArray,qosSimilarityScoreArray,finalSimilarityResultArray; 		//=new int [Can_VRs_List.size()];
		VResource startVResObj=new VResource();
		VResource vResUnderEval=new VResource();
		VResource intermediateVRes=new VResource();
		HashMap <String, HashMap <String, VResource>> partitionedLinkVResForReq = new HashMap <String, HashMap <String, VResource>>();
		Exception_list.clear();

		
		// Interconnecting discovered resources through composition
		if (discoveredVResList.size() != 1){																					// if request has only one VR skip composition
			if(COMPOS_ALGO_DBG){
				System.out.println("\n\tcompositionAlgorithm.discoveredVResList.size() = "+discoveredVResList.size());
				for(VResource discoveredVRes : discoveredVResList) System.out.println("\t\t"+discoveredVRes.getResUri());
			}
			
			for(int vRListItr=0; vRListItr<discoveredVResList.size()-1; vRListItr++){
				startVResObj = discoveredVResList.get(0);																		// startVResObj is the first discovered resource
				if(vRListItr+1 != discoveredVResList.size()){
					vResUnderEval = discoveredVResList.get(vRListItr+1);														// vResUnderEval is the second discovered resource
					reservedVResListForPath.clear(); // clear instantantiously between paths in the same request
				}
				else{ 
					System.out.println("\tvResUnderEval still remain as "+vResUnderEval.getResUri()+" because its the last element in discovered list");
				}
				Exception_list.clear(); // clear instantantiously between paths in the same request
				connectedVRList.clear();
				reservedVResListForPath.add(startVResObj);																		// add startVResObj to composedVResList
				Exception_list.add(startVResObj.getResUri());																	// remove already composed resources from VR_Pool

				if(COMPOS_ALGO_DBG){
					System.out.println("\n\tFirst startVResObj = "+startVResObj.getResUri());
					System.out.println("\tFirst vResUnderEval = "+vResUnderEval.getResUri());
				}

				while(!startVResObj.getResUri().equals(vResUnderEval.getResUri())){												// while startVResObj is not same as vResUnderEval
					String startVResId = startVResObj.getResUri();
					//Query for "connectedto" property of that VR based on res_id
					String connectedVResQryResult = queryForConnectedToVResExceptLinks(startVResId);							// get connected VRs to startVResObj from ontology

					String[] connectedVResSplit = connectedVResQryResult.split("[,\r\n]+");
					for (int splitResultItr = 3; splitResultItr<connectedVResSplit.length; splitResultItr += 2){				// for each connected VR of startVResObj
						String[] connectedVResId = connectedVResSplit[splitResultItr].split("#");
						if (connectedVResId[1].split("-")[0].equals(BDLINK_PRFX)) continue;										// remove link that comes with cofnnectedTo query
						if (Exception_list.contains(connectedVResId[1])) 	continue;											// if connected VR is alredy in composed list, jump to next connected VR
						VResource connectedVResObj = qryForPropertiesByVResId(connectedVResId[1], vrpoolmodel);					// else get connectedVResObj correponding to connectedVResId
						connectedVRList.add(connectedVResObj);																	// add connectedVResObj to connectedVRList
					}																												
					// now we have all connectedVResObj directly connected to startVResObj, except once already composed
					if(COMPOS_ALGO_DBG){
						System.out.println("\n\tstartVResObj = "+startVResId);
						System.out.println("\tDirectly connected resources to "+startVResId);
						for(VResource connectedVResObj : connectedVRList) System.out.println("\t\t"+connectedVResObj.getResUri());
					}
					
					int numOfConnectedVRes = connectedVRList.size();
					if (numOfConnectedVRes < 1){

						System.err.println("Composition Dead-End. No connected resources found for "+startVResId);
						break;
					}

					closenessValueArray 			= new double [numOfConnectedVRes];
					numaricalSimilarityScoreArray 	= new double [numOfConnectedVRes];
					qosSimilarityScoreArray 		= new double [numOfConnectedVRes];
					finalSimilarityResultArray 		= new double [numOfConnectedVRes];

					int intermediateVResIndex = -1;
					for(int conVResItr = 0; conVResItr<connectedVRList.size(); conVResItr++){
						if(connectedVRList.get(conVResItr).getResUri().equals(vResUnderEval.getResUri())){
							intermediateVResIndex = conVResItr;
						}
					}
					String linkVResId = "";
					VResource linkVResObj = null;

					if (intermediateVResIndex >= 0){
						System.out.println("\n\tvResUnderEval: "+vResUnderEval.getResUri()+" is directly connected to startVResId: "+startVResId);
						intermediateVRes = connectedVRList.get(intermediateVResIndex);											//  if vResUnderEval is in connectedVRList consider it as intermediateVRes

						linkVResId = getInterconnectingLinkVResId(startVResId, intermediateVRes.getResUri(), connectedToVResMultiMap);
						if(partitionedLinkVResForReq.containsKey(linkVResId))													// If the link is avialable in partitionedLinkVResMap, get the linkVResObj from it
							linkVResObj = partitionedLinkVResForReq.get(linkVResId).get(linkVResId);						
						else{	
							linkVResObj = qryForPropertiesByVResId(linkVResId, vrpoolmodel);									// else query for linkVResObj and put it in partitionedLinkVResMap
							HashMap<String, VResource> innerPartitionedLinkVResMap = new HashMap<String, VResource>();
							innerPartitionedLinkVResMap.put(linkVResId, linkVResObj);
							partitionedLinkVResForReq.put(linkVResId, innerPartitionedLinkVResMap);
						}// At this point, linkVResObj is available in the partitionedLinkVResMap
						if(COMPOS_ALGO_DBG){
							System.out.println("\n\t Available bandwidth in "+linkVResObj.getResUri()+" = "+linkVResObj.getBw());
							System.out.println("\t Requested bandwidth = "+ reqNetQoSConstraints.getBw());
						}
						if(linkVResObj.getBw()>reqNetQoSConstraints.getBw()){
							if(COMPOS_ALGO_DBG)System.out.println("\tLink: "+linkVResObj.getResUri()+" has bandwidth: "+linkVResObj.getBw()+"Mbps and required bandwidth: "+reqNetQoSConstraints.getBw());
							sufficintLinkBw = true;
						}
					}
					else{																										// if vResUnderEval is NOT a part of connectedVRList
						if(COMPOS_ALGO_DBG)System.out.println("\tintermediateVResIndex = "+intermediateVResIndex+"  This means vResUnderEval is not directly connected to startVResId: "+startVResId);
						closenessValueArray = calculateSimilarityCentrality(connectedVRList, vResUnderEval, closenessMat);		// obtain network closeness between vResUnderEval and connectedVRList members
						numaricalSimilarityScoreArray = calculateSimilarityNumerical(connectedVRList, vResUnderEval);			// obtain available resource value closeness between vResUnderEval and connectedVRList members
						qosSimilarityScoreArray = calculateSimilarityConstraintsQoS(connectedVRList, reqNetQoSConstraints);		// obtain available bandwidth value closeness between request and connectedVRList

						for(int conVRListItr=0; conVRListItr<connectedVRList.size(); conVRListItr++)							// calculate total similarity scores for each resource 
							finalSimilarityResultArray[conVRListItr] = numaricalSimilarityScoreArray[conVRListItr] + qosSimilarityScoreArray[conVRListItr] + 100-(closenessValueArray[conVRListItr]*10);

						if(COMPOS_ALGO_DBG){
							System.out.println("\tfinalSimilarityResultArray length = "+finalSimilarityResultArray.length);
							for(double simVal : finalSimilarityResultArray) System.out.println("\t\t"+simVal);
						}
						
						double maximumSimilarityValue = 0;
						int indexOfMaxSimilarityVR = 0;
						numaricalSimilarityScoreArray = null;																							
						qosSimilarityScoreArray = null;
						closenessValueArray = null;

						for(int conVRListItr=0; conVRListItr<connectedVRList.size(); conVRListItr++)
							if (finalSimilarityResultArray[conVRListItr] > maximumSimilarityValue){
								maximumSimilarityValue = finalSimilarityResultArray[conVRListItr];
								indexOfMaxSimilarityVR = conVRListItr;												
							}
						intermediateVRes = connectedVRList.get(indexOfMaxSimilarityVR);											//  get the most similarity scored resource and consider it as intermediateVRes

						linkVResId = getInterconnectingLinkVResId(startVResId, intermediateVRes.getResUri(), connectedToVResMultiMap);

						if(partitionedLinkVResForReq.containsKey(linkVResId))													// If the link is avialable in partitionedLinkVResMap, get the linkVResObj from it
							linkVResObj = partitionedLinkVResForReq.get(linkVResId).get(linkVResId);						
						else{	
							linkVResObj = qryForPropertiesByVResId(linkVResId, vrpoolmodel);									// else query for linkVResObj and put it in partitionedLinkVResMap
							HashMap<String, VResource> innerPartitionedLinkVResMap = new HashMap<String, VResource>();
							innerPartitionedLinkVResMap.put(linkVResId, linkVResObj);
							partitionedLinkVResForReq.put(linkVResId, innerPartitionedLinkVResMap);
						}// At this point, linkVResObj is available in the partitionedLinkVResMap

						if(linkVResObj.getBw()>reqNetQoSConstraints.getBw()) sufficintLinkBw = true;							// if the link has sufficient amount of bandwidth, set sufficintLinkBw true
						else{																									// if available bandwidth is insufficient between startVResObj and intermediateVRes	

							if(COMPOS_ALGO_DBG){
								System.out.println("\n\tRequested bandwidth is not available between "+startVResObj.getResUri()+" and "+intermediateVRes.getResUri());
								System.out.println("\t Checking next highest similarity scored VRes in the list for sufficient bandwidth");
							}
							do {																								//iterate over all connected resources in descending order of similarity score (highest scored resource checked first)
								// set similariySchore of old indexOfMaxSimilarityVR to zero and get index of next highest score VR. If none, sent -1 as the index
								indexOfMaxSimilarityVR = setSimilarityScoreToZeroAndGetNext(connectedVRList, finalSimilarityResultArray, indexOfMaxSimilarityVR);
								if (indexOfMaxSimilarityVR != -1){																// get index of next available highest score until all has zeros
									intermediateVRes = connectedVRList.get(indexOfMaxSimilarityVR);								// set next Max Scored VR as intermediateVRes 

									if(COMPOS_ALGO_DBG)System.out.println("\t\tconsidering : "+intermediateVRes.getResUri()+" as intermediateVRes");
									linkVResId = getInterconnectingLinkVResId(startVResId, intermediateVRes.getResUri(), connectedToVResMultiMap);
									if(partitionedLinkVResForReq.containsKey(linkVResId))										// If the link is avialable in partitionedLinkVResMap, get the linkVResObj from it
										linkVResObj = partitionedLinkVResForReq.get(linkVResId).get(linkVResId);						
									else{	
										linkVResObj = qryForPropertiesByVResId(linkVResId, vrpoolmodel);						// else query for linkVResObj and put it in partitionedLinkVResMap
										HashMap<String, VResource> innerPartitionedLinkVResMap = new HashMap<String, VResource>();
										innerPartitionedLinkVResMap.put(linkVResId, linkVResObj);
										partitionedLinkVResForReq.put(linkVResId, innerPartitionedLinkVResMap);
									}// At this point, linkVResObj is available in the partitionedLinkVResMap
									if(linkVResObj.getBw()>reqNetQoSConstraints.getBw()) {
										sufficintLinkBw = true;
										if(COMPOS_ALGO_DBG)System.out.println("\tsufficient bandwidth available between "+startVResObj.getResUri()+" and "+intermediateVRes.getResUri());
									}
								}
							}while (!sufficintLinkBw && indexOfMaxSimilarityVR != -1);
						}
					}// End-if vResUnderEval is NOT a part of connectedVRList

					// common code
					if (sufficintLinkBw){																						// if requested link bandwidth available between startVResObj and intermediateVRes
						if(COMPOS_ALGO_DBG)System.out.println("\tSufficient link bandwidth available in link : "+linkVResId+" which is in between "+startVResObj.getResUri()+" and "+intermediateVRes.getResUri());

						HashMap<String, VResource> innerPartitionedLinkVResMap = partitionedLinkVResForReq.get(linkVResId);

						String[] originalLinkVResIdSplit = linkVResId.split("-");
						String newLinkVResId = originalLinkVResIdSplit[0]+"-"+originalLinkVResIdSplit[1]+"-"+(int)iaaSReqId;
						if(COMPOS_ALGO_DBG)System.out.println("\tnewLinkVResId = "+newLinkVResId);													// newLinkVResId will look like "L-10010_VL-0_2"

						if(!innerPartitionedLinkVResMap.containsKey(newLinkVResId)){
							linkVResObj.setBw(linkVResObj.getBw() - reqNetQoSConstraints.getBw());								// deduct the new linkVRes bandwidth from original linkVRes
							innerPartitionedLinkVResMap.remove(linkVResId);
							innerPartitionedLinkVResMap.put(linkVResId, linkVResObj);


							ArrayList<String> connectedToList = new ArrayList<String>();
							connectedToList.add(startVResObj.getResUri());
							connectedToList.add(intermediateVRes.getResUri());
							innerPartitionedLinkVResMap.put(newLinkVResId, createLinkVResObj(newLinkVResId, linkVResObj.getLoc(), reqNetQoSConstraints.getBw(), connectedToList));
							partitionedLinkVResForReq.put(linkVResId, innerPartitionedLinkVResMap);
						}
						else System.out.println("link already exist in list of reserved resources for the request");

						//composedVResList.add(intermediateVRes);																//	add intermediateVRes to composedVResList
						finalSimilarityResultArray = null;				

						
						if(COMPOS_ALGO_DBG)System.out.println("\t partitionedLinkVResMap.size ="+partitionedLinkVResForReq.size());
						for(String origLinkVResId : partitionedLinkVResForReq.keySet()){
							HashMap<String, VResource> innerMap = partitionedLinkVResForReq.get(origLinkVResId);
							if(COMPOS_ALGO_DBG){
								System.out.println("\t"+origLinkVResId+"-->");
								for(String newLinkId : innerMap.keySet())
									System.out.println("\t\t"+newLinkId+"\t:\t"+innerMap.get(newLinkId).getBw());
							}
						}

						sufficintLinkBw = false;
						reservedVResListForPath.add(intermediateVRes);															// 	add intermediateVRes to composedVResList
						Exception_list.add(intermediateVRes.getResUri());
						System.out.println("\t"+intermediateVRes.getResUri()+" added to Exception_list");

						connectedVRList.clear();
						startVResObj=intermediateVRes;																			// start over the loop by considering intermediateVRes as startVResObj
						if(startVResObj.getResUri().equals(vResUnderEval.getResUri()))
							System.out.println("  STOP -- Done composition for request "+iaaSReqId);
						else
							System.out.println("  REPEAT -- Suitable VResource :"+startVResObj.getResUri()+" found. Proceed composition considering it as startVResObj");
						
					}
					else{																										// else do not add it
						System.out.println("\n\n\tinsufficient bandwidth in link between "+startVResObj.getResUri()+" and "+intermediateVRes.getResUri());
						if(COMPOS_ALGO_DBG)System.out.println("\tClearing partitionedLinkVResForReq, connectedVRList and composedVResList");
						partitionedLinkVResForReq.clear();
						connectedVRList.clear();
						reservedVResListForPath.clear();
						vRListItr = discoveredVResList.size();																	// exist condition for outer loop
						compositionFailed = true;
						break;
					}


				} // End-while startVResObj is not same as vResUnderEval


				if (!compositionFailed){																						// empty the composed VR list into reserved one and reset

					System.out.println("\n\t------------------------------ virtual path composition done----------------------------------");
					if(COMPOS_ALGO_DBG){
						System.out.println("\n\tcompositionAlgorithm.reservedVResListForPath.size() = "+reservedVResListForPath.size());
						for(VResource vResInComposeList : reservedVResListForPath) System.out.println("\t\t"+vResInComposeList.getResUri());

						System.out.println("\n\tcompositionAlgorithm.reservedVResListForReq.size() before adding resources of curreng path= "+reservedVResListForReq.size());
						for(VResource vResInreservedList : reservedVResListForReq) System.out.println("\t\t"+vResInreservedList.getResUri());
					}
						
					for(VResource vResRsvdForPath : reservedVResListForPath){

						if(!reservedVResIdsForReq.contains(vResRsvdForPath.getResUri())){
							System.out.println("\tAdding "+vResRsvdForPath.getResUri()+" to reservedVResListForReq. Size = "+reservedVResListForReq.size());
							reservedVResIdsForReq.add(vResRsvdForPath.getResUri());
							reservedVResListForReq.add(vResRsvdForPath);
						}
						else System.out.println("\t"+vResRsvdForPath.getResUri()+" already available in reservedVResListForReq");
					}
					System.out.println("\n\tcompositionAlgorithm.reservedVResListForReq.size() after adding resources of curreng path= "+reservedVResListForReq.size());
				} //end if (composedVResList.size() > 0 )
				else {
					System.out.println("\tComposition Unsuccessful. Clearing reservedVResList");
					reservedVResListForReq.clear();	
				}
				System.out.println("\tvRListItr: "+vRListItr+"  discoveredVResList.size():"+discoveredVResList.size());
			} // end for(int vRListItr=0; vRListItr<discoveredVResList.size(); vRListItr++)
		}
		else{	//	one VR no need for composition
			System.out.println("\ncompositionAlgorithm.discoveredVResList.size() = 1. No need for composition-------\n");
			VResource vResObj = discoveredVResList.get(0);
			reservedVResListForPath.add(vResObj);
			reservedVResListForReq.add(vResObj);
		}
		System.out.println("------------------------------- Composer: Done processing Request : "+iaaSReqId+"  -----------------------------------");

		CompositionResult compositionResult = new CompositionResult();
		compositionResult.setReservedVResListForReq(reservedVResListForReq);
		compositionResult.setPartitionedLinkVResForReq(partitionedLinkVResForReq);
		System.out.println("Returning compositionResult to compositionEventManager");
		return compositionResult;
	}

	// ============================================================================================================================== //
	// ----------------------------------------------------- createLinkVResObj() ---------------------------------------------------- //
	// ============================================================================================================================== //
	public static VResource createLinkVResObj(String newLinkVResId, String linkVReslocation, double newLinkVResBw, ArrayList<String> connectedToList){

		VResource linkVResObj = new VResource();
		linkVResObj.setCpu(0);
		linkVResObj.setN_cores(0);
		linkVResObj.setMem(0);
		linkVResObj.setStr(0);
		linkVResObj.setBw(newLinkVResBw);
		linkVResObj.setLoc(linkVReslocation);
		linkVResObj.setRes_type("network");
		linkVResObj.setPriorityClass("linking");
		linkVResObj.setN_interfaces(2);
		linkVResObj.setResUri(newLinkVResId);
		linkVResObj.setConnectedToList(connectedToList);
		return linkVResObj;
	}

	// ============================================================================================================================== //
	// ------------------------------------------------------ queryConnection() ----------------------------------------------------- //
	// ============================================================================================================================== //
	public static String queryForConnectedToVResExceptLinks(String res_id){
		//System.out.println("CompositionSimulation.queryConnection(): ");
		String answer = "";
		String sqlssparql="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX cloud_ont: <http://www.semanticweb.org/kmetw028/ontologies/2013/11/untitled-ontology-52#>"+
				"SELECT ?subject ?object"+
				" WHERE {?subject rdf:type cloud_ont:VR."+
				" ?subject cloud_ont:connectedto ?object."+
				" Filter(?subject != ?object)"+
				" Filter(?subject=cloud_ont:"+res_id+")"+
				"}";
		Query qq = QueryFactory.create(sqlssparql);
		QueryExecution qex=QueryExecutionFactory.create(qq,vrpoolmodel);
		try {
			ResultSet res = qex.execSelect();
			ByteArrayOutputStream baos= new ByteArrayOutputStream();
			ResultSetFormatter.outputAsCSV(baos, res);
			answer = (res!=null) ? baos.toString(): "";
		}
		finally{
			qex.close();
		}
		return answer;
	}

	// ============================================================================================================================== //
	// ------------------------------------------------ getInterconnectingLinkVResId() ---------------------------------------------- //
	// ============================================================================================================================== //
	public static String getInterconnectingLinkVResId(String startVResId, String interVResId, Multimap<String, String> connectedToVResMultiMap){
		String linkResId = null;
		Collection<String> connToVResIds = connectedToVResMultiMap.get(startVResId);
		for(String eachConnToVResId : connToVResIds){
			if(eachConnToVResId.split("-")[0].equals(BDLINK_PRFX)){
				Collection<String> vResConnToLink = connectedToVResMultiMap.get(eachConnToVResId);
				for(String eachVResConnToVLink : vResConnToLink)
					if(interVResId.equals(eachVResConnToVLink))
						linkResId = eachConnToVResId;
			}
		}
		if(linkResId == null)System.err.println("\tgetInterconnectingLinkVResId: Unable to detect link between "+startVResId+" and "+interVResId); 
		else if(COMPOS_ALGO_DBG) System.out.println("\tgetInterconnectingLinkVResId: link between "+startVResId+" and "+interVResId+"  = "+linkResId+"\n\n");
		return linkResId;
	}

	// ============================================================================================================================== //
	// --------------------------------------------------- Remove_and_Back_track() -------------------------------------------------- //
	// ============================================================================================================================== //
	public static int setSimilarityScoreToZeroAndGetNext(ArrayList<VResource> connectedVRList, double[] finalSimilarityResultArray, int indexOfOldMaxSimilarityVR){
		double max = 0;
		int indexOfNewMaxSimilarityVR = -1;
		finalSimilarityResultArray[indexOfOldMaxSimilarityVR] = 0;
		for(int connVRListItr=0; connVRListItr<connectedVRList.size(); connVRListItr++)
			if (finalSimilarityResultArray[connVRListItr] > max){
				max = finalSimilarityResultArray[connVRListItr];
				indexOfNewMaxSimilarityVR = connVRListItr;
			}
		return indexOfNewMaxSimilarityVR;
	}

	// ============================================================================================================================== //
	// ----------------------------------------------------- Retrieve_VR_per_request() ---------------------------------------------- //
	// ============================================================================================================================== //
	public static ArrayList<VResource> Retrieve_VR_per_request(int ID){
		VResource[] temp_VR;
		ArrayList<VResource> VR_Ret = new ArrayList<VResource>();
		ArrayList<Object> tt = new ArrayList<Object>();
		for (int i = 0; i<All_VRs.size(); i++){
			tt = All_VRs.get(i);
			if ((Integer)tt.get(0) == ID ){
				temp_VR=(VResource[])tt.get(1);
				for (int j=0;j<temp_VR.length;j++)
					VR_Ret.add(temp_VR[j]);
				All_VRs.remove(i);
			}
		}
		return VR_Ret;
	}

	// ============================================================================================================================== //
	// -------------------------------------------------- updateIaaSRequestsInVrPool() ---------------------------------------------- //
	// ============================================================================================================================== //
	public static void updateIaaSRequestsInVrPool(IaaSRequest iaasReqObj, String Req_class, ArrayList<VResource> reservedVResList, boolean accepted)throws FileNotFoundException{
		
		int reqId = (int)iaasReqObj.getReqId();
		System.out.println("Adding request : "+reqId+" to VrPool");
		String reqName = "REQ-"+reqId;
		OntClass iaasReqClass = vrpoolmodel.getOntClass(VRPOOL_NS+"IaaS_Request");
		Individual iaasReqIndividual = vrpoolmodel.createIndividual(VRPOOL_NS + reqName, iaasReqClass );

		DatatypeProperty dpReqId = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "reqId" );
		DatatypeProperty dpReqName = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "reqName" );
		DatatypeProperty dpReqArrival = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "reqArrival" );
		DatatypeProperty dpReqDuration = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "reqDuration" );
		iaasReqIndividual.addProperty( dpReqId, vrpoolmodel.createTypedLiteral(reqId))
		.addProperty( dpReqName, vrpoolmodel.createTypedLiteral(reqName))
		.addProperty( dpReqArrival, vrpoolmodel.createTypedLiteral((int)iaasReqObj.getArrivalTime()))
		.addProperty( dpReqDuration, vrpoolmodel.createTypedLiteral((int)iaasReqObj.getReqDuration()));

		ObjectProperty hasReqState = vrpoolmodel.getObjectProperty(VRPOOL_NS+"hasReqState");
		Individual acceptOrRejectReqIndividual;
		if(accepted) acceptOrRejectReqIndividual = vrpoolmodel.getIndividual(VRPOOL_NS+"reqAccepted");
		else acceptOrRejectReqIndividual = vrpoolmodel.getIndividual(VRPOOL_NS+"reqRejected");
		iaasReqIndividual.addProperty(hasReqState, acceptOrRejectReqIndividual);
		ObjectProperty hasRsvdRes = vrpoolmodel.getObjectProperty(VRPOOL_NS + "hasRsvdRes");
		for (VResource reservedVRes : reservedVResList){
			Individual reservedVResIndividual = vrpoolmodel.getIndividual(VRPOOL_NS + reservedVRes.getResUri());
			iaasReqIndividual.addProperty(hasRsvdRes, reservedVResIndividual);
		}
		ObjectProperty hasPriority = vrpoolmodel.getObjectProperty(VRPOOL_NS+"haspriority");
		Individual priorityIndividual = null;
		switch (Req_class) {
		case "A": priorityIndividual = vrpoolmodel.getIndividual(VRPOOL_NS + "class_a"); break;
		case "B": priorityIndividual = vrpoolmodel.getIndividual(VRPOOL_NS + "class_b"); break;
		case "C": priorityIndividual = vrpoolmodel.getIndividual(VRPOOL_NS + "class_c"); break;
		default: System.err.println("InfrastructureComposer.updateIaaSRequestsInVrPool: Unknown class error");
		}
		iaasReqIndividual.addProperty(hasPriority, priorityIndividual);
		
		PrintStream printStrm = new PrintStream(VRPOOL);
		vrpoolmodel.write(printStrm, "RDF/XML");
		System.out.println("Request : "+reqName +" accept = "+accepted+" added to ontology");
	}

	// ============================================================================================================================== //
	// --------------------------------------------------- updateVResourcesInVrPool() ----------------------------------------------- //
	// ============================================================================================================================== //
	public static void updateVResourcesInVrPool(ArrayList<VResource> reservedVResList, double iaaSReqId, boolean arrival) throws FileNotFoundException{

		String iaaSReqName = REQ_PRFX+"-"+(int)iaaSReqId;
		ObjectProperty VR_status = vrpoolmodel.getObjectProperty(VRPOOL_NS+"has_state");
		ObjectProperty isRsvdResof = vrpoolmodel.getObjectProperty(VRPOOL_NS + "isRsvdResof");//isRsvdResof
		Individual freeIndividual = vrpoolmodel.getIndividual(VRPOOL_NS+"free1");
		Individual reservedIndividual = vrpoolmodel.getIndividual(VRPOOL_NS+"reserved");
		Individual iaaS_Request = vrpoolmodel.getIndividual(VRPOOL_NS + iaaSReqName);
		System.out.println("updateVResourcesInVrPool.reservedVResList.size() = "+reservedVResList.size());
		for(VResource vResPrintObj : reservedVResList) System.out.println("\t"+vResPrintObj.getResUri());

		for(int vResItr = 0; vResItr<reservedVResList.size(); vResItr++){
			VResource vResourceObj = reservedVResList.get(vResItr);
			if (vResourceObj.getResUri().split("-")[0].equals(HOST_PRFX)){		// change the state of VMs only
				String res_id = vResourceObj.getResUri();
				Individual vResIndividual = vrpoolmodel.getIndividual(VRPOOL_NS + res_id);
				if (arrival){  // reserve the resource
					vResIndividual.removeProperty(VR_status, freeIndividual);
					vResIndividual.addProperty(VR_status, reservedIndividual);
					vResIndividual.addProperty(isRsvdResof, iaaS_Request);
					System.out.println("\t\t"+res_id +" ----change state---> reserved");
				}
				else { // free the resources 
					System.out.println("\t\tChanging state of: "+vResourceObj.getResUri()+" to \"free\" ");
					vResIndividual.removeProperty(VR_status, reservedIndividual);
					vResIndividual.addProperty(VR_status, freeIndividual );
				}
				PrintStream p= new PrintStream(VRPOOL);
				vrpoolmodel.write(p, "RDF/XML");
			}
			else{
				System.out.println("\t\tstate of "+vResourceObj.getResUri()+" not changed");
			}
		}
	}

	// ============================================================================================================================== //
	// ---------------------------------------------------- updateLinkVResInVrPool() ------------------------------------------------ //
	// ============================================================================================================================== //
	public static void updateLinkVResInVrPool(HashMap <String, HashMap <String, VResource>> partitionedLinkVResForReq, double iaaSReqId, boolean arrival) throws FileNotFoundException{
		
		String iaaSReqName = REQ_PRFX+"-"+(int)iaaSReqId;
		OntClass linkVResClass = vrpoolmodel.getOntClass(VRPOOL_NS+"VR");
		ObjectProperty VR_status = vrpoolmodel.getObjectProperty(VRPOOL_NS + "has_state");
		ObjectProperty connected_to = vrpoolmodel.getObjectProperty(VRPOOL_NS + "connectedto");
		ObjectProperty provideservices = vrpoolmodel.getObjectProperty(VRPOOL_NS + "provideservice");
		ObjectProperty has_interface = vrpoolmodel.getObjectProperty(VRPOOL_NS + "hasinterface");
		ObjectProperty isRsvdResof = vrpoolmodel.getObjectProperty(VRPOOL_NS + "isRsvdResof");
		ObjectProperty hasRsvdRes = vrpoolmodel.getObjectProperty(VRPOOL_NS + "hasRsvdRes"); //hasRsvdRes

		Individual linking = vrpoolmodel.getIndividual(VRPOOL_NS + "linking");
		Individual interf_Eth = vrpoolmodel.getIndividual(VRPOOL_NS + "Ether1");
		Individual reserved = vrpoolmodel.getIndividual(VRPOOL_NS + "reserved");
		Individual iaaS_Request = vrpoolmodel.getIndividual(VRPOOL_NS + iaaSReqName);
		
		System.out.println("\tCreating links and updating bandwidths in ontology:");

		for(String originalLinkId : partitionedLinkVResForReq.keySet()){
			HashMap <String, VResource> innerPartitionedLinkVResMap = partitionedLinkVResForReq.get(originalLinkId);
			for(String newLinkVResId : innerPartitionedLinkVResMap.keySet()){
				VResource linkVResObj = innerPartitionedLinkVResMap.get(newLinkVResId);
				double oldBw = 0.0;
				if(newLinkVResId.split("-").length > 2){			// New partitioned links
					Individual newLinkVResIndividual = vrpoolmodel.createIndividual(VRPOOL_NS + newLinkVResId, linkVResClass );
					
					DatatypeProperty cpu = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "cpu" );
					DatatypeProperty memory = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "memory" );
					DatatypeProperty storage = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "storage" );
					DatatypeProperty bw = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "bw" );
					DatatypeProperty delay = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "delay" );
					DatatypeProperty loss_rate = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "loss_rate" );
					DatatypeProperty location = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "location" );
					DatatypeProperty n_cores = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "n_cores" );
					DatatypeProperty VMM = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "VMM" );
					DatatypeProperty res_id = vrpoolmodel.createDatatypeProperty( VRPOOL_NS + "res_id" );

					newLinkVResIndividual	.addProperty( cpu, vrpoolmodel.createTypedLiteral( linkVResObj.getCpu() ) )
					.addProperty( memory, vrpoolmodel.createTypedLiteral( linkVResObj.getMem() ) ) 
					.addProperty( storage, vrpoolmodel.createTypedLiteral(linkVResObj.getStr() ) ) 
					.addProperty( bw, vrpoolmodel.createTypedLiteral( linkVResObj.getBw()) ) 
					.addProperty( delay, vrpoolmodel.createTypedLiteral( linkVResObj.getDelay() ) ) 
					.addProperty( loss_rate, vrpoolmodel.createTypedLiteral( linkVResObj.getLoss_rate() ) ) 
					.addProperty( n_cores, vrpoolmodel.createTypedLiteral(linkVResObj.getN_cores() ) ) 
					.addProperty( location, vrpoolmodel.createLiteral(linkVResObj.getLoc() ) ) 
					.addProperty( VMM, vrpoolmodel.createLiteral(linkVResObj.getVmm() ) ) 
					.addProperty( res_id, vrpoolmodel.createLiteral( linkVResObj.getResUri() ) );

					newLinkVResIndividual.addProperty(VR_status, reserved);
					newLinkVResIndividual.addProperty(provideservices, linking);
					newLinkVResIndividual.addProperty(has_interface,interf_Eth);
					newLinkVResIndividual.addProperty(isRsvdResof, iaaS_Request);
					

					if(linkVResObj.getConnectedToList().size()>1){
						Individual connectedVRes1 = vrpoolmodel.getIndividual(VRPOOL_NS + linkVResObj.getConnectedToList().get(0));
						Individual connectedVRes2 = vrpoolmodel.getIndividual(VRPOOL_NS + linkVResObj.getConnectedToList().get(1));
						newLinkVResIndividual.addProperty(connected_to, connectedVRes1);
						newLinkVResIndividual.addProperty(connected_to, connectedVRes2);
					}
					else System.err.println("updateLinkVResInVrPool(): connected resources not found");
					iaaS_Request.addProperty(hasRsvdRes, newLinkVResIndividual);
					
				}
				else{												// Original links
					Individual originalLinkVResIndividual = vrpoolmodel.getIndividual(VRPOOL_NS+newLinkVResId);
					DatatypeProperty bw = vrpoolmodel.getDatatypeProperty( VRPOOL_NS + "bw" );
					oldBw = originalLinkVResIndividual.getPropertyValue(bw).asLiteral().getDouble();
					originalLinkVResIndividual.removeAll(bw);
					originalLinkVResIndividual.addProperty( bw, vrpoolmodel.createTypedLiteral( linkVResObj.getBw()) );
				}
				System.out.println("\t\t"+newLinkVResId +"\t BW: "+oldBw+" -----> "+linkVResObj.getBw());
			}
		}
		PrintStream printStrm = new PrintStream(VRPOOL);
		vrpoolmodel.write(printStrm, "RDF/XML");
	}
	
	

	

	// ============================================================================================================================== //
	// ------------------------------------------------------- saveVrPerReq() ------------------------------------------------------- //
	// ============================================================================================================================== //
	public static void saveVrPerReq(ArrayList<VResource> VR, int ID){
		ArrayList<Object> tt = new ArrayList<Object>();
		VResource[] VRes = new VResource[VR.size()];
		for (int i=0;i<VR.size();i++)
			VRes[i] = VR.get(i);
		tt.add(ID);
		tt.add(VRes);
		All_VRs.add(tt);
	}

	// ============================================================================================================================== //
	// --------------------------------------------------- queryFuncClassLimits() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static String queryFuncClassLimits(double cpu, double n_cores, double mem, double str, String res_type, String class_n){
		// reqVResObj.getCpu(), reqVResObj.getN_cores(),reqVResObj.getMem(),reqVResObj.getStr(), reqVResObj.getRes_type(), reqClass

		String sqlssparql="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"PREFIX ace_lexicon: <http://attempto.ifi.uzh.ch/ace_lexicon#>"+
				"PREFIX cloud_ont: <http://www.semanticweb.org/kmetw028/ontologies/2013/11/untitled-ontology-52#>";

		String select_sentense="SELECT ?res ?res_id ";
		String where_sentense="WHERE { ?res rdf:type cloud_ont:VR. ?res cloud_ont:res_id ?res_id. ?res cloud_ont:has_state ?r. ?r rdf:type cloud_ont:free.";
		Functional_P_count=2;

		if (cpu != 0.0){
			select_sentense+="?cpu ";
			Functional_P_count++;
			where_sentense+= " ?res cloud_ont:cpu ?cpu.";
//			if (class_n==1)
//				where_sentense+= " FILTER (xsd:double(?cpu) >= "+ Limits.cpu_lower_A +" && xsd:double(?cpu) <= "+Limits.cpu_upper_A+")";//" ?res cloud_ont:cost ?cpu."; 
//			else if (class_n==2)
//				where_sentense+= " FILTER (xsd:double(?cpu) >= "+ Limits.cpu_lower_B  +" && xsd:double(?cpu) <= "+Limits.cpu_upper_B+")";//" ?res cloud_ont:cost ?cpu."; 
//			else if (class_n==3)
//				where_sentense+= " FILTER (xsd:double(?cpu) >= "+ Limits.cpu_lower_C +" && xsd:double(?cpu) <= "+Limits.cpu_upper_C+")";//" ?res cloud_ont:cost ?cpu."; 
			
			switch (class_n){
			case "A": where_sentense+= " FILTER (xsd:double(?cpu) >= "+ Limits.cpu_lower_A +" && xsd:double(?cpu) <= "+Limits.cpu_upper_A+")";	break;
			case "B": where_sentense+= " FILTER (xsd:double(?cpu) >= "+ Limits.cpu_lower_B  +" && xsd:double(?cpu) <= "+Limits.cpu_upper_B+")";	break;
			case "C": where_sentense+= " FILTER (xsd:double(?cpu) >= "+ Limits.cpu_lower_C +" && xsd:double(?cpu) <= "+Limits.cpu_upper_C+")";	break;
				default : System.err.println("InfrastructureComposer.queryFuncClassLimits: Class mismatch error"); 
			}
			
			
		}
		if (n_cores != 0.0){
			select_sentense+="?n_cores ";
			Functional_P_count++;
			where_sentense+= " ?res cloud_ont:n_cores ?n_cores.";
			/*if (class_n==1)
				where_sentense+= " FILTER (xsd:double(?n_cores) >= "+Limits.n_cores_lower_A+" && xsd:double(?n_cores) <= "+Limits.n_cores_upper_A+")";//" ?res cloud_ont:cost ?cpu."; 
			else if (class_n==2)
				where_sentense+= " FILTER (xsd:double(?n_cores) >= "+Limits.n_cores_lower_B+" && xsd:double(?n_cores) <= "+Limits.n_cores_upper_B+")";//" ?res cloud_ont:cost ?cpu."; 
			else if (class_n==3)
				where_sentense+= " FILTER (xsd:double(?n_cores) >= "+Limits.n_cores_lower_B+" && xsd:double(?n_cores) <= "+Limits.n_cores_upper_B+")";//" ?res cloud_ont:cost ?cpu."; 

*/
			switch (class_n){
			case "A": where_sentense+= " FILTER (xsd:double(?n_cores) >= "+Limits.n_cores_lower_A+" && xsd:double(?n_cores) <= "+Limits.n_cores_upper_A+")";	break;
			case "B": where_sentense+= " FILTER (xsd:double(?n_cores) >= "+Limits.n_cores_lower_B+" && xsd:double(?n_cores) <= "+Limits.n_cores_upper_B+")";	break;
			case "C": where_sentense+= " FILTER (xsd:double(?n_cores) >= "+Limits.n_cores_lower_B+" && xsd:double(?n_cores) <= "+Limits.n_cores_upper_B+")";	break;
				default : System.err.println("InfrastructureComposer.queryFuncClassLimits: Class mismatch error. \t if (n_cores != 0.0)"); 
			} 
		}
		if (str!=0.0){
			select_sentense+="?str ";
			Functional_P_count++;
			where_sentense+= " ?res cloud_ont:storage ?str.";
			/*if (class_n==1)
				where_sentense+= " FILTER (xsd:double(?str) >= "+Limits.str_lower_A+")";// && xsd:double(?str) < "+(str+30)+")"; //" ?res cloud_ont:str ?str.";
			else if (class_n==2)
				where_sentense+= " FILTER (xsd:double(?str) >= "+Limits.str_lower_B+")";// && xsd:double(?str) < "+(str+30)+")"; //" ?res cloud_ont:str ?str.";
			else if (class_n==3)
				where_sentense+= " FILTER (xsd:double(?str) >= "+Limits.str_lower_C+")";// && xsd:double(?str) < "+(str+30)+")"; //" ?res cloud_ont:str ?str.";
			*/
			switch (class_n){
			case "A": where_sentense+= " FILTER (xsd:double(?str) >= "+Limits.str_lower_A+")";	break;
			case "B": where_sentense+= " FILTER (xsd:double(?str) >= "+Limits.str_lower_B+")";	break;
			case "C": where_sentense+= " FILTER (xsd:double(?str) >= "+Limits.str_lower_C+")";	break;
				default : System.err.println("InfrastructureComposer.queryFuncClassLimits: Class mismatch error. \t if (str!=0.0)"); 
			} 

		}
		if (mem!=0.0){
			select_sentense+="?mem ";
			Functional_P_count++;
			where_sentense+= " ?res cloud_ont:memory ?mem.";
			/*if (class_n==1)
				where_sentense+= " FILTER (xsd:double(?mem) >= "+Limits.mem_lower_A+" && xsd:double(?mem) <= "+Limits.mem_upper_A+")"; //" ?res cloud_ont:str ?str.";
			else if (class_n==2)
				where_sentense+= " FILTER (xsd:double(?mem) >= "+Limits.mem_lower_B+" && xsd:double(?mem) <= "+Limits.mem_upper_B+")"; //" ?res cloud_ont:str ?str.";
			else if (class_n==3)
				where_sentense+= " FILTER (xsd:double(?mem) >= "+Limits.mem_lower_C+" && xsd:double(?mem) <= "+Limits.mem_upper_C+")"; //" ?res cloud_ont:str ?str.";
			*/
			switch (class_n){
			case "A": where_sentense+= " FILTER (xsd:double(?mem) >= "+Limits.mem_lower_A+" && xsd:double(?mem) <= "+Limits.mem_upper_A+")";	break;
			case "B": where_sentense+= " FILTER (xsd:double(?mem) >= "+Limits.mem_lower_B+" && xsd:double(?mem) <= "+Limits.mem_upper_B+")";	break;
			case "C": where_sentense+= " FILTER (xsd:double(?mem) >= "+Limits.mem_lower_C+" && xsd:double(?mem) <= "+Limits.mem_upper_C+")";	break;
				default : System.err.println("InfrastructureComposer.queryFuncClassLimits: Class mismatch error. \t if (mem!=0.0)"); 
			} 
		}
		
		
		if (!res_type.equals("")){
			select_sentense+="?res_type ";
			Functional_P_count++;
			if (res_type.equals("compute")){
				//select_sentense+="?res_type ";
				//Functional_P_count++;
				where_sentense+= " ?res rdf:type cloud_ont:compute."; // using reasoning
			}

			else if (res_type.equals("storage")){
				//select_sentense+="?res_type ";
				//Functional_P_count++;
				where_sentense+= " ?res rdf:type cloud_ont:storage.";  // using reasoning
			}
			else if (res_type.equals("network")){
				//select_sentense+="?res_type ";
				//Functional_P_count++;
				where_sentense+= " ?res rdf:type cloud_ont:netwrok.";  // using reasoning
			}
		}
		/*
		if (class_n==1){
			select_sentense+="?class_ ";
			Functional_P_count++;
			where_sentense+= " ?res rdf:type cloud_ont:Class_A.";  // using reasoning
			where_sentense+= " ?res cloud_ont:haspriority ?class_.";  
		}
		else if (class_n==2){
			select_sentense+="?class_ ";
			Functional_P_count++;
			where_sentense+= " ?res rdf:type cloud_ont:Class_B.";  // using reasoning
			where_sentense+= " ?res cloud_ont:haspriority ?class_.";  
		}
		else if (class_n==3){
			select_sentense+="?class_ ";
			Functional_P_count++;
			where_sentense+= " ?res rdf:type cloud_ont:Class_C.";  // using reasoning
			where_sentense+= " ?res cloud_ont:haspriority ?class_.";  
		}
		*/
		select_sentense+="?class_ ";
		Functional_P_count++;
		switch (class_n){
		case "A": where_sentense+= " ?res rdf:type cloud_ont:Class_A. ?res cloud_ont:haspriority ?class_.";  	break;
		case "B": where_sentense+= " ?res rdf:type cloud_ont:Class_B. ?res cloud_ont:haspriority ?class_.";  	break;
		case "C": where_sentense+= " ?res rdf:type cloud_ont:Class_C. ?res cloud_ont:haspriority ?class_.";  	break;
			default : System.err.println("InfrastructureComposer.queryFuncClassLimits: Class mismatch error."); 
		}
		where_sentense+=" }";
		sqlssparql+=select_sentense+where_sentense;
		return sqlssparql;
	}
	
	
	

	
	
	// ============================================================================================================================== //
	// ============================================================================================================================== //
	// -------------------------------------------------  Resource Utilization Code  ----------------------------------------------- //
	// ============================================================================================================================== //
	// ============================================================================================================================== //
	
	
}

