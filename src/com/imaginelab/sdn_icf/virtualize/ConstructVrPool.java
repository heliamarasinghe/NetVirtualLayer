package com.imaginelab.sdn_icf.virtualize;



import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.jena.atlas.io.IndentedWriter;

import com.google.common.collect.ArrayListMultimap;
//import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.imaginelab.sdn_icf.compose.Limits;
import com.imaginelab.sdn_icf.containers.PSwitch;
import com.imaginelab.sdn_icf.containers.VResource;

import static com.imaginelab.sdn_icf.main.Constants.BLDVR_PERF_FILE;
import static com.imaginelab.sdn_icf.main.Constants.BW_OVRSUB_FACTOR;
import static com.imaginelab.sdn_icf.main.Constants.CLOSENESS_FILE;
import static com.imaginelab.sdn_icf.main.Constants.CPU_OVRSUB_FACTOR;
import static com.imaginelab.sdn_icf.main.Constants.HOST_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.INFINITY;
import static com.imaginelab.sdn_icf.main.Constants.LINKVR_SERFILE;
import static com.imaginelab.sdn_icf.main.Constants.LOG_PERFORMANCE;
import static com.imaginelab.sdn_icf.main.Constants.MEM_OVRSUB_FACTOR;
import static com.imaginelab.sdn_icf.main.Constants.NL;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL_QUERY_PREFIX;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL_READ_DBG;
import static com.imaginelab.sdn_icf.main.Constants.STR_OVRSUB_FACTOR;
import static com.imaginelab.sdn_icf.main.Constants.SWCH_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_CONST_DBG;


public class ConstructVrPool {

	private static HashMap<String, Integer> resUriToResNumMap;
	private static OntModel physicalResourceModel;
	
	private static String constVrPoolResultLine = "";
	private static int queryCount = 0;
	private static long totalQueryTime = 0L;
	
	
	//================================================================================================================================================================
	//-------------------------------------------------------------------- readPrPool() ------------------------------------------------------------------------------
	//	SUMMARY:	Method called by ProjectMain to read PR_Pool, create VResources and populate VR_Pool
	//================================================================================================================================================================
	public static void readPrPool(){

		try{	
			physicalResourceModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
			physicalResourceModel.read(PRPOOL, "RDF/XML");

			//	Quering PR_Pool for physical resource information
			//Multimap<String, String> hasVNodeMultiMap = queryHasVNode();												// -----------------  1 
			//Multimap<String, String> hasInterfaceMultiMap = queryHasInterface();										// -----------------  2  
			//Multimap<String, String> connectedToIntfsMultiMap = queryConnectedToIntfs();								// -----------------  3 	
			HashMap<String, HashMap<String, String>> vmHostDpHashMap = queryVmHostIndividuals();						// -----------------  4 	
			HashMap<String, HashMap<String, String>> switchDpHashMap = querySwitchIndividuals();						// -----------------  5 	
			//HashMap<String, HashMap<String, String>> vmDpHashMap = queryVmIndividuals();								// -----------------  6  
			HashMap<String, HashMap<String, String>> cmpCompDpHashMap = queryCmpCompIndividuals();						// -----------------  7 	
			HashMap<String, HashMap<String, String>> stoCompDpHashMap = queryStoCompIndividuals();						// -----------------  8 	
			//HashMap<String, HashMap<String, String>> swCompDpHashMap = querySwCompIndividuals();						// -----------------  9  
			HashMap<String, HashMap<String, String>> intfsDpHashMap = queryIntfsIndividuals();							// ----------------- 10 	
			HashMap<String, HashMap<String, String>> uniDLinkDpHashMap = queryUniDLinkIndividuals();					// ----------------- 11  
			//HashMap<String, HashMap<String, String>> bniDLinkDpHashMap = queryUdlIndividualsAndCreateBdl();			// ----------------- 12
			
			HashMap<String, HashMap<String, String>> biDLinkDpHashMap = creteBiDLinksFromUniDLinks(uniDLinkDpHashMap);
			physicalResourceModel.close();
			
			// "queryCount" and "totalQueryTime" are updated at each jena query to PrPool
			constVrPoolResultLine = queryCount +"\t"+ Long.toString(totalQueryTime) + "\t";
			totalQueryTime = 0L;
			queryCount = 0;
			
			long createVirtResST = System.currentTimeMillis();
			//	Methods to construct phyToVirtResMap, switch_list, conn_matrix, closeness_mat and call ReadWriteOWL save created VRs
			String[][] conn_matrix = createConn_matAndCloseness_mat(vmHostDpHashMap, switchDpHashMap, biDLinkDpHashMap);
			ArrayList<PSwitch> switch_list = populateSwitch_list(switchDpHashMap, intfsDpHashMap);
			//populateLinkList(linkDpHashMap);
			Multimap<String, VResource> phyToVirtResMap = populatePhyToVirtResMap(switch_list, vmHostDpHashMap, biDLinkDpHashMap, cmpCompDpHashMap, stoCompDpHashMap);
			constVrPoolResultLine += phyToVirtResMap.size() + "\t";
			
			long createVirtResET = System.currentTimeMillis();
			constVrPoolResultLine += Long.toString(createVirtResET-createVirtResST) + "\t";
			
			long updateVrPoolOwlST = System.currentTimeMillis();
			updateVrPool(conn_matrix, phyToVirtResMap);
			long updateVrPoolOwlET = System.currentTimeMillis();
			constVrPoolResultLine += Long.toString(updateVrPoolOwlET-updateVrPoolOwlST) + "\t";

			System.out.println("\nVrPool updated\n");
			
			// Write result file
			if (LOG_PERFORMANCE) writeToResultBuildVrPool();

		}catch (JenaException je) {
			System.err.println("ConstructVrPool.readPrPool(): JenaException generated");
			je.printStackTrace();
		}catch (Exception e) {
			System.err.println("General Exception generated");
			e.printStackTrace();
		}
	}
	

	
	
	//==================================================================================================================================================================
	//--------------------------------------------------------------- createConnMatAndClosenessMat()--------------------------------------------------------------------
	// SUMMARY:		Populate idToUriMap, conn_matrix and closeness_mat
	// DESCRIPTION:	
	//		stringUriToIntIddMap- 	VmHosts, Switches and Links are identified by String resourceURI in PR_Pool and as int resId in VR_Pool. This is a Bidirectional Map
	//								used to convert between these identities								
	//		conn_matrix			-	Shows connected link IDs between each pair of physical Host and Switch IDs
	//		closeness_mat 		-	Shortest path matrix created from adjacency matrix of physical Host and Switch resources. 
	//								Floyd-Warshall algorithm. Complexity = O(n^3). 100 = infinite distance
	/*
	conn_matrix (adjacency matrix with link IDs) 
		conn	H-1		H-2		H-3		H-4		S-1		S-2		S-3		S-4		S-5		S-6
		H-1		0		0		0		0		BDL-13	0		0		0		0		0
		H-2		0		0		0		0		0		BDL-15	0		0		0		0
		H-3		0		0		0		0		0		0		0		BDL-1	0		0
		H-4		0		0		0		0		0		0		BDL-14	0		0		0
		S-1		BDL-13	0		0		0		0		BDL-7	BDL-5	BDL-10	0		BDL-6
		S-2		0		BDL-15	0		0		BDL-7	0		BDL-2	BDL-12	0		BDL-3
		S-3		0		0		0		BDL-14	BDL-5	BDL-2	0		BDL-11	0		BDL-8
		S-4		0		0		BDL-1	0		BDL-10	BDL-12	BDL-11	0		0		BDL-9
		S-5		0		0		0		0		0		0		0		0		0		BDL-4
		S-6		0		0		0		0		BDL-6	BDL-3	BDL-8	BDL-9	BDL-4	0

	closeness_mat (Floyd-Warshall shortest path matrix with 9999 as infinite distance)
		0		1001	1002	1003	1004	11		12		13		14		15		16
		1001	0		3		3		3		1		2		2		2		3		2
		1002	3		0		3		3		2		1		2		2		3		2
		1003	3		3		0		3		2		2		2		1		3		2
		1004	3		3		3		0		2		2		1		2		3		2
		11		1		2		2		2		0		1		1		1		2		1
		12		2		1		2		2		1		0		1		1		2		1
		13		2		2		2		1		1		1		0		1		2		1
		14		2		2		1		2		1		1		1		0		2		1
		15		3		3		3		3		2		2		2		2		0		1
		16		2		2		2		2		1		1		1		1		1		0
	 */
	//==================================================================================================================================================================
	public static String[][] createConn_matAndCloseness_mat(HashMap<String, HashMap<String, String>> vmHostDpHashMap, 
			HashMap<String, HashMap<String, String>> switchDpHashMap, HashMap<String, HashMap<String, String>> biDLinkDpHashMap) throws Exception{

		int numOfHosts = vmHostDpHashMap.keySet().size();
		int numOfSwitches = switchDpHashMap.keySet().size();
		if(numOfSwitches>989 || numOfHosts>8999) System.err.println("ConstrucVrPool.createConnMatAndIdMap: switchId or hostId scalability error");

		resUriToResNumMap = new HashMap<String, Integer>();
		// Creating IDs for Hosts and Switches and adding to idToUriMap
		for (String hostUri : vmHostDpHashMap.keySet()){
			int hostNum = Integer.parseInt(hostUri.split("-")[1]);																								// hostId = 1001, 1002, 1003, ...
			if(resUriToResNumMap.containsKey(hostNum)) System.err.println("ConstrucVrPool.createIdToUriMap(): Duplicate hostId detected");
			else resUriToResNumMap.put(hostUri, hostNum);	
		}
		for (String switchUri : switchDpHashMap.keySet()){
			int switchNum = Integer.parseInt(switchUri.split("-")[1]);																								
			if(resUriToResNumMap.containsKey(switchNum)) System.err.println("ConstrucVrPool.createIdToUriMap(): Duplicate switchId detected");
			else resUriToResNumMap.put(switchUri, switchNum);																			// switchId = 11, 12, 13, ...
		}

		int adjMatSize = numOfHosts + numOfSwitches;
		int conMatSize = adjMatSize+1;
		int [][] adjacencyMatrix = new int [adjMatSize][adjMatSize];	// Adjacency matrix will be created and used only to calculate shortest parths
		String [][] connMatrix = new String[adjMatSize+1][adjMatSize+1];
		String [][] closenessMat = new String[adjMatSize+1][adjMatSize+1];

		// Initializing conn_mat and closeness_mat with raw and colume names by iterating over hosts and switches
		for(int hItr = 1; hItr<=numOfHosts; hItr++)   {	connMatrix[0][hItr] = HOST_PRFX+"-"+(hItr);				closenessMat[0][hItr] = HOST_PRFX+"-"+(hItr);}
		for(int sItr = 1; sItr<=numOfSwitches; sItr++){	connMatrix[0][sItr+numOfHosts] = SWCH_PRFX+"-"+(sItr);	closenessMat[0][sItr+numOfHosts] = SWCH_PRFX+"-"+(sItr);}
		for(int hItr = 1; hItr<=numOfHosts; hItr++)	  {	connMatrix[hItr][0] = HOST_PRFX+"-"+(hItr);				closenessMat[hItr][0] = HOST_PRFX+"-"+(hItr);}
		for(int sItr = 1; sItr<=numOfSwitches; sItr++){	connMatrix[sItr+numOfHosts][0] = SWCH_PRFX+"-"+(sItr);	closenessMat[sItr+numOfHosts][0] = SWCH_PRFX+"-"+(sItr);}


		// Initializing closeness_mat with and conn_mat
		connMatrix[0][0] = "conn";
		closenessMat[0][0] = "close";
		for(int row = 1; row<conMatSize; row++)
			for(int col = 1; col<conMatSize; col++){
				connMatrix[row][col] = "0";
				closenessMat[row][col] = "0";
			}
			
		// Initializing adjacency matrix with infinite distances (0 distance for same node)
		for(int row = 0; row<adjMatSize; row++)
			for(int col = 0; col<adjMatSize; col++)
				adjacencyMatrix[row][col] = (row != col)? INFINITY : 0;
		
		// Create Bidirectional links from Unidirectional links
		

		// Populating connection matrix and adjacency matrix adding link resourcesIds to idToUriMap		
		for (String linkURI : biDLinkDpHashMap.keySet()){
			resUriToResNumMap.put(linkURI, Integer.parseInt(linkURI.split("-")[1]));
			HashMap<String, String> innerLinkDpMap = biDLinkDpHashMap.get(linkURI);
			String srcIntfsURI = innerLinkDpMap.get("srcIntfs");
			String destIntfsURI = innerLinkDpMap.get("destIntfs");
			String[] srcIntfsUriSplitArray = srcIntfsURI.split("-|\\_");
			String[] destIntfsUriSplitArray = destIntfsURI.split("-|\\_");
			int rowNum = 0;
			int colNum = 0;								
			if(srcIntfsUriSplitArray[0].equals(HOST_PRFX)){
				rowNum = Integer.parseInt(srcIntfsUriSplitArray[1]);
				if(destIntfsUriSplitArray[0].equals(HOST_PRFX)) colNum = Integer.parseInt(destIntfsUriSplitArray[1]);
				else if (destIntfsUriSplitArray[0].equals(SWCH_PRFX)) colNum = numOfHosts + Integer.parseInt(destIntfsUriSplitArray[1]);
				else System.err.println("ConstructVrPoll.createConnMatAndIdMap.if-H: Unknown toIntfsURI");
			}
			else if(srcIntfsUriSplitArray[0].equals(SWCH_PRFX)){
				rowNum = numOfHosts + Integer.parseInt(srcIntfsUriSplitArray[1]);
				if(destIntfsUriSplitArray[0].equals(HOST_PRFX)) colNum = Integer.parseInt(destIntfsUriSplitArray[1]);
				else if (destIntfsUriSplitArray[0].equals(SWCH_PRFX)) colNum = numOfHosts + Integer.parseInt(destIntfsUriSplitArray[1]);
				else System.err.println("ConstructVrPoll.createConnMatAndIdMap.if-S: Unknown toIntfsURI");
			}
			else System.err.println("ConstructVrPoll.createConnMatAndIdMap: Unknown frmIntfsURI");
			connMatrix[rowNum][colNum] = linkURI;
			connMatrix[colNum][rowNum] = linkURI;
			
			adjacencyMatrix[rowNum-1][colNum-1] = 1;												// populate adjacency matrix with 1 for each link 
			adjacencyMatrix[colNum-1][rowNum-1] = 1;
		}
		
		// Constructing Floyd-Warshall shotrest parth matrix from adjacency matrix
		// The minimum distance from i to j must be the minimum of path[i][j], path[i][k]+path[k][j] for all values of k.
		for(int k=0;k<adjMatSize;k++){
			for(int i=0;i<adjMatSize;i++){
				for(int j=0;j<adjMatSize;j++){
					adjacencyMatrix[i][j]=Math.min(adjacencyMatrix[i][j],adjacencyMatrix[i][k]+adjacencyMatrix[k][j]);
				}
			}
		}
		
		for(int row = 1; row<conMatSize; row++)
			for(int col = 1; col<conMatSize; col++)
				closenessMat[row][col] = Integer.toString(adjacencyMatrix[row-1][col-1]);

		// Write closeness matrix to a file 
		PrintWriter closenessMatWriter = new PrintWriter(CLOSENESS_FILE, "UTF-8");
		for(int row = 0; row<conMatSize; row++){
			for(int col = 0; col<conMatSize; col++)
				closenessMatWriter.print(closenessMat[row][col]+"\t");
			closenessMatWriter.println();
		}
		closenessMatWriter.close();


		if(VRPOOL_CONST_DBG){
			
			System.out.println("\nidToUriMap");
			for(String resURI : resUriToResNumMap.keySet())
				System.out.println("\t"+resURI+"\t:\t"+resUriToResNumMap.get(resURI));
	
			System.out.println("\n\tconn_matrix (adjacency matrix with link IDs) ");
			for(int row = 0; row<conMatSize; row++){
				for(int col = 0; col<conMatSize; col++)
					System.out.print("\t\t"+connMatrix[row][col]);
				System.out.println();
			}
			System.out.println("\n\tcloseness_mat (Floyd-Warshall shortest path matrix with "+INFINITY+" as infinite distance)"); 
			for(int row = 0; row<conMatSize; row++){
				for(int col = 0; col<conMatSize; col++)
					System.out.print("\t\t"+closenessMat[row][col]);
				System.out.println();
			}
		}
		return connMatrix;
	}

	//==================================================================================================================================================
	//----------------------------------------------------------- populateSwitch_list() ----------------------------------------------------------------
	//==================================================================================================================================================
	public static ArrayList<PSwitch> populateSwitch_list(HashMap<String, HashMap<String, String>> switchDpMultiMap, HashMap<String, HashMap<String, String>>intfsDpMultiMap){
		String type = "TOR";
		PSwitch pSwitchObj;		
		int number_interfaces=0;								// Khaled has used number_interfaces instead of number of ports
		
		String location = "";
		ArrayList<PSwitch> switch_list = new ArrayList<PSwitch>();
		for (String switchURI : switchDpMultiMap.keySet()){
			int switchNum = resUriToResNumMap.get(switchURI);
			HashMap<String, String> innerVmHostDpMap = switchDpMultiMap.get(switchURI);
			if(innerVmHostDpMap.get("resLocation") != null)
				//if(innerVmHostDpMap.get("resLocation").equals("Rack")) 				// commented to treat both physical switches and OVS's in the same manner
					location = switchURI;				
				//else 
					//location = stringUriToIntIddMap.get(innerVmHostDpMap.get("resLocation")).toString();
			number_interfaces = Integer.parseInt(intfsDpMultiMap.get(switchURI+"_I-0").get("numPorts"));
			pSwitchObj=new PSwitch(switchURI, switchNum, type, location, number_interfaces);
			switch_list.add(pSwitchObj);
		}
		return switch_list;
	}

	//==================================================================================================================================================
	//--------------------------------------------------------- populatePhyToVirtResMap() --------------------------------------------------------------
	// SUMMARY: virtualize IT, Switch and Link resources and construct hashmap: phyToVirtResMap
	//==================================================================================================================================================
	public static Multimap<String, VResource> populatePhyToVirtResMap(ArrayList<PSwitch> switch_list, HashMap<String, HashMap<String, String>> vmHostDpHashMap,
			HashMap<String, HashMap<String, String>> biDLinkDpHashMap, HashMap<String, HashMap<String, String>> cmpCompDpHashMap, 
			HashMap<String, HashMap<String, String>> stoCompDpHashMap){

		Multimap<String, VResource> phyToVirtResMap = ArrayListMultimap.create();

		// ----------------------------------- Adding IT VResources into phyToVirtResMap ----------------------------------------
		for(String vmHostURI : vmHostDpHashMap.keySet()){										
			int vResCpu = 0;
			int vResMem = 0;
			int vResSto = 0;
			int vResBw = 0;
			String operation = "";
			boolean createVmFlag = false;
			int vmCountPerHost = 0;

			String vmhostCmpComp = vmHostURI+"_CMP";
			String vmhostStoComp = vmHostURI+"_STO";
			HashMap<String, String> innerVmHostDpMap = vmHostDpHashMap.get(vmHostURI);
			HashMap<String, String> innerCmpCompDpMap = cmpCompDpHashMap.get(vmhostCmpComp);
			HashMap<String, String> innerStoCompDpMap = stoCompDpHashMap.get(vmhostStoComp);
			String nodeClass = (innerVmHostDpMap.get("nodeClass") != null) ? innerVmHostDpMap.get("nodeClass") : "NA";
			int availableCoresInHost =  (innerCmpCompDpMap.get("availableCores") != null) ? Integer.parseInt(innerCmpCompDpMap.get("availableCores")) : 0;
			int availableMemInHost = (int) ((innerCmpCompDpMap.get("availableMemory") != null) ? Long.parseLong(innerCmpCompDpMap.get("availableMemory"))/1024 : 0);
			double availableStoInHost = (innerStoCompDpMap.get("availableStorage") != null) ? Double.parseDouble(innerStoCompDpMap.get("availableStorage")) : 0;
			
			// multiplied by Oversubscription factor
			availableCoresInHost = (int) Math.round(availableCoresInHost * CPU_OVRSUB_FACTOR);
			availableMemInHost = (int) Math.round(availableMemInHost * MEM_OVRSUB_FACTOR);
			availableStoInHost = availableStoInHost * STR_OVRSUB_FACTOR;

			if(VRPOOL_CONST_DBG){
				System.out.println("\nCompute Resources Available in "+vmHostURI);
				for(String dpNameCmpComp : innerCmpCompDpMap.keySet())
					System.out.println("\t"+dpNameCmpComp+"\t=\t"+innerCmpCompDpMap.get(dpNameCmpComp));

				System.out.println("\nStorage Resources Available in "+vmHostURI);
				for(String dpNameStoComp : innerStoCompDpMap.keySet())
					System.out.println("\t"+dpNameStoComp+"\t=\t"+innerCmpCompDpMap.get(dpNameStoComp));

				System.out.println("\nDefining VResource instances in = "+vmHostURI);
			}
			if(nodeClass.equals("A")){
				vResCpu = Limits.cpu_lower_A;
				vResMem = Limits.mem_lower_A;
				vResSto = Limits.str_lower_A;
				vResBw = (int)Limits.bw_lower_A;
				operation = "class_a";
				createVmFlag = true;
			}
			else if(nodeClass.equals("B")){
				vResCpu = Limits.cpu_lower_B;
				vResMem = Limits.mem_lower_B;
				vResSto = Limits.str_lower_B;
				vResBw = (int)Limits.bw_lower_B;
				operation = "class_b";
				createVmFlag = true;
			}
			else if (nodeClass.equals("C")){
				vResCpu = Limits.cpu_lower_C;
				vResMem = Limits.mem_lower_C;
				vResSto = Limits.str_lower_C;
				vResBw = (int)Limits.bw_lower_C;
				operation = "class_c";
				createVmFlag = true;
			}
			else System.err.println("ConstrucVrPool.populatePool_VR: Unknown NodeClass \""+nodeClass+"\". Unable to create VResources in "+vmHostURI);
			while(createVmFlag){
				if(availableCoresInHost < vResCpu){
					if(VRPOOL_CONST_DBG)System.out.println("\n\tInsufficient CPU resources to create another Class-"+nodeClass+" VResource in "+vmHostURI);
					createVmFlag = false;
				} 
				else if (availableMemInHost < vResMem){
					if(VRPOOL_CONST_DBG)System.out.println("\n\tInsufficient Memory resources to create another Class-"+nodeClass+" VResource in "+vmHostURI);
					createVmFlag = false;
				}
				else if (availableStoInHost < (double)(vResSto)){
					if(VRPOOL_CONST_DBG)System.out.println("\n\tInsufficient Storage resources to create another Class-"+nodeClass+" VResource in "+vmHostURI);
					createVmFlag = false;
				}
				else{
					vmCountPerHost++;
					//String vmHostId = vmHostURI;
					VResource vResource = new VResource();
					vResource.setResUri(vmHostURI+"_"+"VM-"+vmCountPerHost);
					vResource.setResNum(vmCountPerHost);
					vResource.setCpu(vResCpu);
					vResource.setN_cores(vResCpu);
					vResource.setMem(vResMem);
					vResource.setStr(vResSto);   
					vResource.setBw(vResBw);
					vResource.setLoc(vmHostURI);
					vResource.setRes_type("compute");
					vResource.setPriorityClass(operation);
					phyToVirtResMap.put(vmHostURI, vResource);
					
					availableCoresInHost -= vResCpu;
					availableMemInHost -= vResMem;
					availableStoInHost -= vResSto;
					if(VRPOOL_CONST_DBG){
						System.out.println("\n\tClass-"+nodeClass+"  VResource successfully created in "+vmHostURI);
						System.out.println("\tCPU Cores Allocated   =  "+vResCpu+"  \t Remaining = " +availableCoresInHost);
						System.out.println("\tMemory GBs Allocated  =  "+vResMem+"  \t Remaining = "+availableMemInHost);
						System.out.println("\tStorage GBs Allocated =  "+vResSto+"  \t Remaining = "+availableStoInHost);
					}
					createVmFlag = true;
				}
			}
			

		} // IT vResources added

		

		// ----------------------------------- Adding Switch VResources into phyToVirtResMap --------------------------------------
		for(int pSwItr = 0; pSwItr<switch_list.size(); pSwItr++){
			PSwitch phySwitch = switch_list.get(pSwItr);
			VResource vResource = new VResource();
			vResource.setResUri(SWCH_PRFX+"-"+phySwitch.getSwitch_id()+"_VS");
			vResource.setResNum(phySwitch.getSwitch_id());
			vResource.setCpu(phySwitch.getCpu());
			vResource.setN_cores(phySwitch.getN_core());
			vResource.setMem(phySwitch.getMemory());
			vResource.setStr(phySwitch.getStorage());
			vResource.setBw(Limits.bw_upper);
			vResource.setLoc(phySwitch.getLocation());
			vResource.setRes_type("switching");
			vResource.setN_interfaces(phySwitch.getNumber_interfaces());
			phyToVirtResMap.put(phySwitch.getSwitchURI(), vResource);
		} // Switch vResources added
		
		// ----------------------------------- Adding Link VResources into phyToVirtResMap ----------------------------------------
		ArrayList<VResource> linkList = new ArrayList<VResource>();
		for(String linkURI : biDLinkDpHashMap.keySet()){
			HashMap<String, String> innerLinkDpMap = biDLinkDpHashMap.get(linkURI);
			VResource vResource = new VResource();
			vResource.setResUri(linkURI+"_VL");
			vResource.setCpu(0);
			vResource.setN_cores(0);
			vResource.setMem(0);
			vResource.setStr(0);
			vResource.setBw(Double.parseDouble(innerLinkDpMap.get("bandwidth")) * BW_OVRSUB_FACTOR);
			vResource.setLoc(innerLinkDpMap.get("location"));
			vResource.setRes_type("linking");
			vResource.setN_interfaces(2);
			
			linkList.add(vResource);
			phyToVirtResMap.put(linkURI, vResource);
			
		}
		try{
			FileOutputStream fout ;
			ObjectOutputStream oos ; 
			fout = new FileOutputStream(LINKVR_SERFILE);
			oos = new ObjectOutputStream(fout);  
			oos.writeObject(linkList);
			oos.close();
		} catch (Exception Ex){
			System.err.println("\nConstructVrPool.createVLinksSerFile: General exception caught\n");
			Ex.printStackTrace();
		}// Link vResources added
		
		
		if(VRPOOL_CONST_DBG) 
			System.out.println("\nSize of phyToVirtResMap after virtualizing hosts, switches and links = "+phyToVirtResMap.size());
		return phyToVirtResMap;
	}

	//==================================================================================================================================================
	//---------------------------------------------------------------- updateVrPool() ------------------------------------------------------------------
	//==================================================================================================================================================
	public static void updateVrPool(String [][] conn_matrix, Multimap<String, VResource> phyToVirtResMap) throws FileNotFoundException{
		
		System.out.println("Updating VR_Pool ontology");	
		if(VRPOOL_CONST_DBG){
			System.out.println("phyToVirtResMap");
			for(String phyResId : phyToVirtResMap.keySet()){
				Collection<VResource> vResourceCollection = phyToVirtResMap.get(phyResId);
				System.out.print("\t"+phyResId+" \t -->");
				for(VResource vResource : vResourceCollection)
					System.out.print("\t"+vResource.getResUri());
				System.out.println();
			}
		}
		VrPoolAccess.updateVrPool(conn_matrix, phyToVirtResMap);
	}







	//==================================================================================================================================================
	//------------------------------------------------ SPARQL Querry methods to read PR_Pool Ontology---------------------------------------------------

	//	Query#		Queried Data																Output Data Structure

	// 	 1			Hosts and VMs related by hasVNode object property							Multimap<String, String> hasVNodeMultiMap
	//   2			all subjects and objects related by hasInterface object property			Multimap<String, String> hasInterfaceMultiMap
	//   3			All subjects and objects related by connectedToIntfs object property		Multimap<String, String> connectedToIntfsMultiMap
	//   4			Datatype Properties of vmHost Individuals									HashMap<String, HashMap<String, String>> vmHostDpHashMap
	//   5			Datatype Properties of Switch Individuals									HashMap<String, HashMap<String, String>> switchDpHashMap
	//   6			Datatype Properties of VM Individuals										HashMap<String, HashMap<String, String>> vmDpHashMap
	//   7			Datatype Properties of ComputeComponent Individuals							HashMap<String, HashMap<String, String>> cmpCompDpHashMap
	//   8			Datatype Properties of StorageComponent Individuals							HashMap<String, HashMap<String, String>> swCompDpHashMap
	//   9			Datatype Properties of SwitchingComponent Individuals						HashMap<String, HashMap<String, String>> stoCompDpHashMap
	//  10			Datatype Properties of Interface Individuals								HashMap<String, HashMap<String, String>> intfsDpHashMap
	//  11			Datatype Properties of Link Individuals										HashMap<String, HashMap<String, String>> linkDpHashMap
	//==================================================================================================================================================

	/* 1 */// Querying for vmHosts and VMs related by hasVNode object property
	public static Multimap<String, String> queryHasVNode(){
		Multimap<String, String> hasVNodeMultiMap = ArrayListMultimap.create();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for vmHosts and VMs related by hasVNode object property");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE { ?vmHostURI a prpool:Host. ?vmHostURI prpool:hasVNode ?vmURI }";
		Query hasVNodeQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution hasVNodeQryExec = QueryExecutionFactory.create(hasVNodeQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet hasVNodeRsltset = hasVNodeQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		
		while (hasVNodeRsltset.hasNext()){
			QuerySolution hasVNodeSolution = hasVNodeRsltset.nextSolution();
			String vmHostURI = hasVNodeSolution.get("vmHostURI").asResource().toString().split("#")[1];
			String vmURI = hasVNodeSolution.get("vmURI").asResource().toString().split("#")[1];
			hasVNodeMultiMap.put(vmHostURI, vmURI);
		}
		hasVNodeQryExec.close() ;
		if(PRPOOL_READ_DBG)for(String vmHostURI : hasVNodeMultiMap.keySet()){
			Collection<String> hasVNodes = hasVNodeMultiMap.get(vmHostURI);
			System.out.println("VMs hosted in "+vmHostURI);
			for(String vmURI : hasVNodes)
				System.out.println("\t"+vmURI);
		}
		return hasVNodeMultiMap;
	}

	/* 2 */// Querying for all subjects and objects related by hasInterface object property
	public static Multimap<String, String> queryHasInterface(){
		Multimap<String, String> hasInterfaceMultiMap = ArrayListMultimap.create();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for all subjects and objects related by hasInterface object property");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE {?nodeURI prpool:hasInterface ?interfaceURI }";
		Query hasInterfaceQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution hasInterfaceQryExec = QueryExecutionFactory.create(hasInterfaceQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet hasInterfaceRsltset = hasInterfaceQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (hasInterfaceRsltset.hasNext()){
			QuerySolution hasInterfaceSolution = hasInterfaceRsltset.nextSolution();
			String nodeURI = hasInterfaceSolution.get("nodeURI").asResource().toString().split("#")[1];
			String interfaceURI = hasInterfaceSolution.get("interfaceURI").asResource().toString().split("#")[1];
			hasInterfaceMultiMap.put(nodeURI, interfaceURI);
		}
		hasInterfaceQryExec.close() ;
		if(PRPOOL_READ_DBG)for(String nodeURI : hasInterfaceMultiMap.keySet()){
			Collection<String> hasVNodes = hasInterfaceMultiMap.get(nodeURI);
			System.out.println("Interfaces in "+nodeURI);
			for(String interfaceURI : hasVNodes)
				System.out.println("\t"+interfaceURI);
		}
		return hasInterfaceMultiMap;
	}

	/* 3 */// Querying for all subjects and objects related by connectedToIntfs object property
	public static Multimap<String, String> queryConnectedToIntfs(){
		Multimap<String, String> connectedToIntfsMultiMap = ArrayListMultimap.create();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for all subjects and objects related by connectedToIntfs object property");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE {?interface1URI prpool:connectedToIntfs ?interface2URI }";
		Query connectedToIntfsQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution connectedToIntfsQryExec = QueryExecutionFactory.create(connectedToIntfsQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet connectedToIntfsRsltset = connectedToIntfsQryExec.execSelect();
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (connectedToIntfsRsltset.hasNext()){
			QuerySolution hasInterfaceSolution = connectedToIntfsRsltset.nextSolution();
			String interface1URI = hasInterfaceSolution.get("interface1URI").asResource().toString().split("#")[1];
			String interface2URI = hasInterfaceSolution.get("interface2URI").asResource().toString().split("#")[1];
			connectedToIntfsMultiMap.put(interface1URI, interface2URI);
		}
		connectedToIntfsQryExec.close() ;

		if(PRPOOL_READ_DBG)for(String interface1URI : connectedToIntfsMultiMap.keySet()){
			Collection<String> connectedToIntfs = connectedToIntfsMultiMap.get(interface1URI);
			System.out.println("Other interfaces connected to interface "+interface1URI);
			for(String interface2URI : connectedToIntfs)
				System.out.println("\t"+interface2URI);
		}
		return connectedToIntfsMultiMap;
	}

	/* 4 */// Querying for Datatype Properties of vmHost Individuals
	public static HashMap<String, HashMap<String, String>> queryVmHostIndividuals(){
		HashMap<String, HashMap<String, String>> vmHostDpHashMap = new HashMap<String, HashMap<String, String>>();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of VmHost Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE { ?vmHost rdf:type prpool:Host. ?vmHost ?dpName ?dpValue . "
				+ "FILTER (isLiteral(?dpValue) && ?dpName != owl:topDataProperty) }";
		Query vmHostQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		if(PRPOOL_READ_DBG)vmHostQry.serialize(new IndentedWriter(System.out,true));
		QueryExecution vmHostQryExec = QueryExecutionFactory.create(vmHostQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet vmHostRsltset = vmHostQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (vmHostRsltset.hasNext()){
			QuerySolution vmHostQrySolution = vmHostRsltset.nextSolution();
			String resURI = vmHostQrySolution.get("vmHost").asResource().toString().split("#")[1];
			String dpName = vmHostQrySolution.get("dpName").asResource().toString().split("#")[1];
			String dpValue= vmHostQrySolution.get("dpValue").asLiteral().getString();
			//vmHostDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (vmHostDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = vmHostDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				vmHostDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				vmHostDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		vmHostQryExec.close();
		if(PRPOOL_READ_DBG)for(String resURI : vmHostDpHashMap.keySet()){
			System.out.println("\n Datatype Properties of Host  "+resURI);
			//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
			HashMap<String, String> innerResDpMap = vmHostDpHashMap.get(resURI);
			for(String dpName : innerResDpMap.keySet())
				System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
		}
		return vmHostDpHashMap;
	}

	/* 5 */// Querying for Datatype Properties of Switch Individuals
	public static HashMap<String, HashMap<String, String>> querySwitchIndividuals(){
		HashMap<String, HashMap<String, String>> switchDpHashMap = new HashMap<String, HashMap<String, String>>();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of Switch Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE { ?resURI rdf:type prpool:Switch. ?resURI ?dpName ?dpValue . "
				+ "FILTER (isLiteral(?dpValue) && ?dpName != owl:topDataProperty) }";
		Query switchQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution switchQryExec = QueryExecutionFactory.create(switchQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet switchAtribRsltset = switchQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (switchAtribRsltset.hasNext()){
			QuerySolution switchQrySolution = switchAtribRsltset.nextSolution();
			String resURI = switchQrySolution.get("resURI").asResource().toString().split("#")[1];
			String dpName = switchQrySolution.get("dpName").asResource().toString().split("#")[1];
			String dpValue= switchQrySolution.get("dpValue").asLiteral().getString();
			//switchDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (switchDpHashMap.get(resURI) != null){
				HashMap<String, String> innerMapOfRdsDp = switchDpHashMap.remove(resURI);
				innerMapOfRdsDp.put(dpName, dpValue);
				switchDpHashMap.put(resURI, innerMapOfRdsDp);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				switchDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		switchQryExec.close() ;
		if(PRPOOL_READ_DBG)
			for(String resURI : switchDpHashMap.keySet()){
				System.out.println("\n Datatype Properties of Switch  "+resURI);
				//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
				HashMap<String, String> innerResDpMap = switchDpHashMap.get(resURI);
				for(String dpName : innerResDpMap.keySet())
					System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
			}
		return switchDpHashMap;
	}

	/* 6 */// Querying for Datatype Properties of VM Individuals
	public static HashMap<String, HashMap<String, String>> queryVmIndividuals(){
		HashMap<String, HashMap<String, String>> vmDpHashMap = new HashMap<String, HashMap<String, String>>();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of VM Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE { ?resURI rdf:type prpool:VNode. ?resURI ?dpName ?dpValue . "
				+ "FILTER (isLiteral(?dpValue) && ?dpName != owl:topDataProperty) }";
		Query vmQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution vmQryExec = QueryExecutionFactory.create(vmQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet vmAtribRsltset = vmQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (vmAtribRsltset.hasNext()){
			QuerySolution vmQrySolution = vmAtribRsltset.nextSolution();
			String resURI = vmQrySolution.get("resURI").asResource().toString().split("#")[1];
			String dpName = vmQrySolution.get("dpName").asResource().toString().split("#")[1];
			String dpValue= vmQrySolution.get("dpValue").asLiteral().getString();
			//vmDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (vmDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = vmDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				vmDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				vmDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		vmQryExec.close() ;
		if(PRPOOL_READ_DBG)for(String resURI : vmDpHashMap.keySet()){
			System.out.println("\n Datatype Properties of VM  "+resURI);
			//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
			HashMap<String, String> innerResDpMap = vmDpHashMap.get(resURI);
			for(String dpName : innerResDpMap.keySet())
				System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
		}
		return vmDpHashMap;
	}

	/* 7 */// Querying for Datatype Properties of ComputeComponent Individuals
	public static HashMap<String, HashMap<String, String>> queryCmpCompIndividuals(){
		HashMap<String, HashMap<String, String>> cmpCompDpHashMap = new HashMap<String, HashMap<String, String>>();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of ComputeComponent Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString	= "WHERE { ?component rdf:type prpool:ComputeComponent. ?component ?property ?value . "
				+ "FILTER (?property != rdf:type && ?property != prpool:nodeProperties && ?property != prpool:computeProperties && ?property != owl:topDataProperty )"
				+ "FILTER (?property != prpool:resourceURI && ?property != prpool:resourceName)}";
		Query cmpCompQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution cmpCompQryExec = QueryExecutionFactory.create(cmpCompQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet cmpCompRsltset = cmpCompQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (cmpCompRsltset.hasNext()){
			QuerySolution cmpCompQrySolution = cmpCompRsltset.nextSolution();
			String resURI = cmpCompQrySolution.get("component").asResource().toString().split("#")[1];
			String dpName = cmpCompQrySolution.get("property").asResource().toString().split("#")[1];
			String dpValue= cmpCompQrySolution.get("value").asLiteral().getString();
			//cmpCompDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (cmpCompDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = cmpCompDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				cmpCompDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				cmpCompDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		cmpCompQryExec.close() ;
		if(PRPOOL_READ_DBG)for(String resURI : cmpCompDpHashMap.keySet()){
			System.out.println("\n Datatype Properties of ComputeComponent  "+resURI);
			//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
			HashMap<String, String> innerResDpMap = cmpCompDpHashMap.get(resURI);
			for(String dpName : innerResDpMap.keySet())
				System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
		}
		return cmpCompDpHashMap;
	}

	/* 8 */// Querying for Datatype Properties of StorageComponent Individuals
	public static HashMap<String, HashMap<String, String>> queryStoCompIndividuals(){
		HashMap<String, HashMap<String, String>> stoCompDpHashMap = new HashMap<String, HashMap<String, String>>();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of StorageComponent Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString	= "WHERE { ?component rdf:type prpool:StorageComponent. ?component ?property ?value . "
				+ "FILTER (?property != rdf:type && ?property != prpool:nodeProperties && ?property != prpool:storageProperties && ?property != owl:topDataProperty)"
				+ "FILTER (?property != prpool:resourceURI && ?property != prpool:resourceName) }";
		Query stoCompQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution stoCompQryExec = QueryExecutionFactory.create(stoCompQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet stoCompRsltset = stoCompQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (stoCompRsltset.hasNext()){
			QuerySolution stoCompQrySolution = stoCompRsltset.nextSolution();
			String resURI = stoCompQrySolution.get("component").asResource().toString().split("#")[1];
			String dpName = stoCompQrySolution.get("property").asResource().toString().split("#")[1];
			String dpValue= stoCompQrySolution.get("value").asLiteral().getString();
			//stoCompDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (stoCompDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = stoCompDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				stoCompDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				stoCompDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		stoCompQryExec.close() ;
		if(PRPOOL_READ_DBG)for(String resURI : stoCompDpHashMap.keySet()){
			System.out.println("\n Datatype Properties of StorageComponent  "+resURI);
			//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
			HashMap<String, String> innerResDpMap = stoCompDpHashMap.get(resURI);
			for(String dpName : innerResDpMap.keySet())
				System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
		}
		return stoCompDpHashMap;
	}

	/* 9 */// Querying for Datatype Properties of SwitchingComponent Individuals
	public static HashMap<String, HashMap<String, String>> querySwCompIndividuals(){
		HashMap<String, HashMap<String, String>> swCompDpHashMap = new HashMap<String, HashMap<String, String>>();
		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of SwitchingComponent Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString	= "WHERE { ?component rdf:type prpool:SwitchingComponent. ?component ?property ?value . "
				+ "FILTER (?property != rdf:type && ?property != prpool:nodeProperties && ?property != prpool:switchingProperties && ?property != owl:topDataProperty)"
				+ "FILTER (?property != prpool:resourceURI && ?property != prpool:resourceName) }";
		Query swCompQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution swCompQryExec = QueryExecutionFactory.create(swCompQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet swCompRsltset = swCompQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (swCompRsltset.hasNext()){
			QuerySolution swCompQrySolution = swCompRsltset.nextSolution();
			String resURI = swCompQrySolution.get("component").asResource().toString().split("#")[1];
			String dpName = swCompQrySolution.get("property").asResource().toString().split("#")[1];
			String dpValue= swCompQrySolution.get("value").asLiteral().getString();
			//swCompDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (swCompDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = swCompDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				swCompDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				swCompDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		swCompQryExec.close() ;
		//if(PRPOOL_READ_DBG)
			for(String resURI : swCompDpHashMap.keySet()){
				System.out.println("\n Datatype Properties of SwitchingComponent  "+resURI);
				//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
				HashMap<String, String> innerResDpMap = swCompDpHashMap.get(resURI);
				for(String dpName : innerResDpMap.keySet())
					System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
			}
		return swCompDpHashMap;
	}

	/*10 */// Querying for Datatype Properties of Interface Individuals
	public static HashMap<String, HashMap<String, String>> queryIntfsIndividuals(){
		HashMap<String, HashMap<String, String>> intfsDpHashMap = new HashMap<String, HashMap<String, String>>();

		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of Interface Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString	= "WHERE { ?interface rdf:type prpool:Interface. ?interface ?property ?value . "
				+ "FILTER (?property != rdf:type &&  isLiteral(?value)) "
				+ "FILTER (?property != prpool:interfaceProperties && ?property != owl:topDataProperty)}";
		Query intfsQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution intfsQryExec = QueryExecutionFactory.create(intfsQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet intfsRsltset = intfsQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (intfsRsltset.hasNext()){
			QuerySolution intfsQrySolution = intfsRsltset.nextSolution();
			String resURI = intfsQrySolution.get("interface").asResource().toString().split("#")[1];
			String dpName = intfsQrySolution.get("property").asResource().toString().split("#")[1];
			String dpValue= intfsQrySolution.get("value").asLiteral().getString();
			//intfsDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (intfsDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = intfsDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				intfsDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				intfsDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		intfsQryExec.close() ;
		if(PRPOOL_READ_DBG)for(String resURI : intfsDpHashMap.keySet()){
			System.out.println("\n Datatype Properties of Interface  "+resURI);
			//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
			HashMap<String, String> innerResDpMap = intfsDpHashMap.get(resURI);
			for(String dpName : innerResDpMap.keySet())
				System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
		}
		return intfsDpHashMap;
	}

	/*11 */// Querying for Datatype Properties of Link Individuals
	public static HashMap<String, HashMap<String, String>> queryUniDLinkIndividuals(){
		HashMap<String, HashMap<String, String>> uniDLinkDpHashMap = new HashMap<String, HashMap<String, String>>();

		if(PRPOOL_READ_DBG)System.out.println("\n Querying for Datatype Properties of Link Individuals");
		String selectString = "SELECT DISTINCT *";
		String whereString	= "WHERE { ?link rdf:type prpool:Link. ?link ?property ?value . "
				+ "FILTER (?property != rdf:type &&  isLiteral(?value)) "
				+ "FILTER (?property != prpool:linkProperties && ?property != owl:topDataProperty)}";
		Query linkQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution linkQryExec = QueryExecutionFactory.create(linkQry, physicalResourceModel) ;
		
		long qryExecST = System.currentTimeMillis();
		ResultSet linkRsltSet = linkQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (linkRsltSet.hasNext()){
			QuerySolution intfsQrySolution = linkRsltSet.nextSolution();
			String resURI = intfsQrySolution.get("link").asResource().toString().split("#")[1];
			String dpName = intfsQrySolution.get("property").asResource().toString().split("#")[1];
			String dpValue= intfsQrySolution.get("value").asLiteral().getString();
			//intfsDpMultiMap.put(resURI, new DpTuple(resURI, dpName, dpValue));
			if (uniDLinkDpHashMap.get(resURI) != null){
				HashMap<String, String> dpMapOfRes = uniDLinkDpHashMap.remove(resURI);
				dpMapOfRes.put(dpName, dpValue);
				uniDLinkDpHashMap.put(resURI, dpMapOfRes);
			}
			else{
				HashMap<String, String> dpMapOfRes = new HashMap<String, String>();
				dpMapOfRes.put(dpName, dpValue);
				uniDLinkDpHashMap.put(resURI, dpMapOfRes);
			}
		}
		linkQryExec.close();
		if(PRPOOL_READ_DBG)
			for(String resURI : uniDLinkDpHashMap.keySet()){
				System.out.println("\n Datatype Properties of Link  "+resURI);
				//Collection<DpTuple> dpTupleCollection = vmHostDpMultiMap.get(resURI);
				HashMap<String, String> innerResDpMap = uniDLinkDpHashMap.get(resURI);
				for(String dpName : innerResDpMap.keySet())
					System.out.println("\t"+dpName+"\t"+innerResDpMap.get(dpName));
			}
		return uniDLinkDpHashMap;
	}

	//==================================================================================================================================================
	//------------------------------------------------------- creteBiDLinksFromUniDLinks() -------------------------------------------------------------
	//==================================================================================================================================================
	private static HashMap<String, HashMap<String, String>> creteBiDLinksFromUniDLinks(HashMap<String, HashMap<String, String>> uniDLinkDpHashMap){
		
		HashMap<String, HashMap<String, String>> biDLinkDpHashMap = new HashMap<String, HashMap<String, String>>();		
		
		HashMap<String, String> upLinkDownLinkMap = new HashMap<String,String>();
		String selectString = "SELECT ?udl1 ?udl2";
		String whereString	= "WHERE { ?udl1 a prpool:Link. "
							+ "?udl2 a prpool:Link. "
							+ "?udl1 prpool:hasSrcPort ?src1. "
							+ "?udl2 prpool:hasDestPort ?dest2. "
							+ "?udl2 prpool:hasSrcPort ?src2. "
							+ "?udl1 prpool:hasDestPort ?dest1. "
							+ "FILTER(?src1 = ?dest2 && ?src2 = ?dest1)"
						+ "}";
		
		Query biLinkQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		QueryExecution biLinkQryExec = QueryExecutionFactory.create(biLinkQry, physicalResourceModel) ;
		
		
		long qryExecST = System.currentTimeMillis();
		ResultSet biLinkRsltSet = biLinkQryExec.execSelect() ;
		long qryExecET = System.currentTimeMillis();
		totalQueryTime += qryExecET- qryExecST;
		queryCount++;
		
		while (biLinkRsltSet.hasNext()){
			QuerySolution intfsQrySolution = biLinkRsltSet.nextSolution();
			String udl1 = intfsQrySolution.get("udl1").asResource().toString().split("#")[1];
			String udl2 = intfsQrySolution.get("udl2").asResource().toString().split("#")[1];
			upLinkDownLinkMap.put(udl1, udl2);
			
		
		}
		biLinkQryExec.close();
		List<String> alreadyAddedList = new ArrayList<String>();
		int biDLinkNum = 1;
		
		Iterator<Entry<String, HashMap<String, String>>> uniDLinkMapItr = uniDLinkDpHashMap.entrySet().iterator();
	    while (uniDLinkMapItr.hasNext()) {
			Entry<String, HashMap<String, String>> uniDLinkEntry = uniDLinkMapItr.next();
			String uniDLinkURI = uniDLinkEntry.getKey();
			HashMap<String, String> uniDLinkDpValMap =  uniDLinkEntry.getValue();
			String returnUniDLinkUri = upLinkDownLinkMap.get(uniDLinkURI);
			if(alreadyAddedList.contains(returnUniDLinkUri)){		// if return link already exist in bidirectional link map, verify mac addresses
				HashMap<String, String> returnUniDLinkDpValMap =  uniDLinkDpHashMap.get(returnUniDLinkUri);
				if(!uniDLinkDpValMap.get("srcMac").replaceAll("^(00:)*", "").equals(returnUniDLinkDpValMap.get("destMac").replaceAll("^(00:)*", "")))
					System.err.println("ConstructVrPool.creteBiDFromUniDLinks() : Link src and dest MAC mismatch Error");	
			}
			else{													// if return link is not in the bidirectional link map, put only the forward link
				alreadyAddedList.add(uniDLinkURI);
				uniDLinkDpValMap.put("location", uniDLinkURI+":"+returnUniDLinkUri);
				biDLinkDpHashMap.put("BDL-"+biDLinkNum, uniDLinkDpValMap);
				biDLinkNum++;
			}
		}
		if (VRPOOL_CONST_DBG){
			System.out.println("\n\tNumber of unidirectional links = \t"+uniDLinkDpHashMap.size());
			System.out.println("\tNumber of biidirectional links = \t"+biDLinkDpHashMap.size());
			for(String biDLinkUri : biDLinkDpHashMap.keySet())
				System.out.println("\t"+biDLinkUri);
			System.out.println("\talreadyAddedList.size() = \t"+alreadyAddedList.size());
			for (String existElement : alreadyAddedList)
				System.out.println("\t"+existElement);
		}
		return biDLinkDpHashMap;
	}
	
	
	public static void writeToResultBuildVrPool() {
		try{
		PrintWriter printwriter = new PrintWriter(new FileWriter(BLDVR_PERF_FILE, true));
		printwriter.println(constVrPoolResultLine);
		printwriter.close();
		} catch(IOException ioEx){
			System.err.println("BuildPrTopology.writeToResultBuildPrPool: File writer exception caught");
			ioEx.printStackTrace();
		}
	}
	
}
