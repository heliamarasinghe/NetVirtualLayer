/*Interface Adjacency Martix
 
	H-1	H-2	H-3	H-4		S-1	S-2	S-3	S-4	S-5	S-6
	  ____________		 _____________________
H-1	 |0	 0	 0	 0|		|1	 0	 0	 0	 0	 0|
H-2	 |0	 0	 0	 0|		|0	 1	 0	 0	 0	 0| <-- Host to Switch Quadrent
H-3	 |0	 0	 0	 0|		|0	 0	 0	 1	 0	 0|
H-4	 |0	 0	 0	 0|		|0	 0	 1	 0	 0	 0|
	  ------------   	 ---------------------
	  ____________		 _____________________
S-1	 |1	 0	 0	 0|		|0	 1	 1	 1	 0	 1|
S-2	 |0	 1	 0	 0|		|0	 0	 1	 1	 0	 1|
S-3	 |0	 0	 0	 1|		|0	 1	 0	 1	 0	 1| <-- Switch to Switch Quadrent
S-4	 |0	 0	 1	 0|		|0	 1	 1	 0	 0	 1|
S-5	 |0	 0	 0	 0|		|0	 0	 0	 0	 0	 1|
S-6	 |0	 0	 0	 0|		|0	 1	 1	 1	 1	 0|
	  ------------	 	 ---------------------
	  switch ^ to
	 host quadrent	
 */


package com.imaginelab.sdn_icf.discover;
import static com.imaginelab.sdn_icf.main.Constants.BLDPR_PERF_FILE;
import static com.imaginelab.sdn_icf.main.Constants.INTFS_CONNECT_DBG;
import static com.imaginelab.sdn_icf.main.Constants.LOG_PERFORMANCE;
import static com.imaginelab.sdn_icf.main.Constants.READ_PHYSICAL_IT;
import static com.imaginelab.sdn_icf.main.Constants.READ_PHYSICAL_NET;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import com.imaginelab.sdn_icf.containers.*;

public class BuildPrTopology {
	
	static boolean netReadSuccess = false;
	static boolean itReadSuccess = false;
	private static Datapath[] datapathObjArray = null;
	private static ArrayList<Link> linkObjList = null;						
	//private static Link[] allLinkObjArray = null;						
	private static VmHost[] vmHostObjArray = null;
	
	private static String constPrPoolResultLine;

	
	// ============================================================================================================================== //
	// --------------------------------------------------------- updatePrPool() ----------------------------------------------------- //
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Method Invoked from ProjectMain ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
	// ============================================================================================================================== //
	public static void updatePrPool(){
		
		readPhysicalResources();
		
		long buildConnectivityST = System.currentTimeMillis();
		buildConnectivity();
		long buildConnectivityET = System.currentTimeMillis();
		constPrPoolResultLine += Long.toString(buildConnectivityET-buildConnectivityST) + "\t";
		// 3 constPrPoolResultLine = net-update-time \t IT-update-time \t build-connect-time
		
		//Link[] allLinkObjArray = (Link[]) linkObjList.toArray();
		
		// Update ontology
		if(!netReadSuccess)
			System.err.println("BuildPrTopology.updatePrPool(): Reading network device information failed");
		if(!itReadSuccess)
			System.err.println("BuildPrTopology.updatePrPool(): Reading IT server information failed");
		if(netReadSuccess || itReadSuccess){
			long updatePrPoolST = System.currentTimeMillis();
			PrPoolAccess.updatePrPool(datapathObjArray, vmHostObjArray, linkObjList);
			long updatePrPoolET = System.currentTimeMillis();
			constPrPoolResultLine += Long.toString(updatePrPoolET-updatePrPoolST) + "\t";
			// 4 constPrPoolResultLine = net-update-time \t IT-update-time \t build-connect-time \t owl-update-time
		}

		
		// Write result file
		if (LOG_PERFORMANCE) writeToResultBuildPrPool();
		
		
	}
	
	// ============================================================================================================================== //
	// ---------------------------------------------------- readPhysicalResources() ------------------------------------------------- //
	// ============================================================================================================================== //
	public static void readPhysicalResources(){
	
		// Obtain network switch and link information from flowvisor 
		if(READ_PHYSICAL_NET){  
			System.out.println("\nReading physical network information .....");
			
			long updateNetStateST = System.currentTimeMillis();
			netReadSuccess = ReadPhysicalNet.updateNetState();
			long updateNetStateET = System.currentTimeMillis();
			constPrPoolResultLine = Long.toString(updateNetStateET-updateNetStateST) + "\t";
			// 1 constPrPoolResultLine = net-update-time
			
			if(netReadSuccess){
				datapathObjArray = ReadPhysicalNet.getDatapathObjArray();
				linkObjList = new ArrayList<Link>(Arrays.asList(ReadPhysicalNet.getLinkObjArray()));			// putting inter switch link objects. Links between Host-Switch are added manually in this class
				System.out.println("\t\t\t\t ...... Physical network read successful\n");
			}
			else System.err.println("BuildPrTopology.readPhysicalResources() --> Physical network read unsuccessful");
		}
			
		// Obtain physical Host information from individual hypervisors
		if(READ_PHYSICAL_IT) {
			System.out.println("\nReading physical IT information .....");
			
			long updateITStateST = System.currentTimeMillis();
			itReadSuccess = ReadPhysicalIT.updateITState();
			long updateITStateET = System.currentTimeMillis();
			constPrPoolResultLine += Long.toString(updateITStateET-updateITStateST) + "\t";
			// 2 constPrPoolResultLine = net-update-time \t IT-update-time
			
			if(itReadSuccess){
				vmHostObjArray = ReadPhysicalIT.getVmHostObjArray();
				System.out.println("\t\t\t\t ...... Physical IT"
						+ " read successful\n");
			}
			else System.err.println("BuildPrTopology.readPhysicalResources() --> Physical IT Server read unsuccessful");
		}
	}
	
///===============================================================================================================================================
///-------------------------------------------------------- buildConnectivity() ------------------------------------------------------------------
///	SUMMARY: 	Populate connectedIntfsUriList in Intfs objects
///				Construct Adjacency matrix adjacencyMatrix[][]
///	DESCRIPTION:Identify OVS's in vmHosts by matching MAC address, add vmHost Intfs resourceURI into ovs connectedIntfsUriLists and vice-versa
///				Read links to identify Adjacency between switch Intfs and add linked Switch-Intfs-resourceURIs to connectedIntfsUriLists
///				Populate adjacencyMatrix[][] by adding vmHost-Intfs <---> ovs-Intfs connectivity and Switch-Intfs <---> Switch-Intfs connectivity
///	NOTE:		If any vmHost has more than one interface connected switches (including OVS), adjacencyMatrix[][] will be faulty
///===============================================================================================================================================
	
	public static void buildConnectivity(){
		HashMap <String, String> switchMacToIntfsUriMap = new HashMap<String, String>();
		HashMap <String, String> ovsIntfsToHostIntfsMap = new HashMap<String, String>();
		int numOfSwitches = 0;
		int numOfVmHosts = 0;
		if(READ_PHYSICAL_IT) numOfVmHosts = vmHostObjArray.length;
		if (datapathObjArray != null) numOfSwitches = datapathObjArray.length;
		int intfsConnMatrixSize = numOfSwitches+numOfVmHosts+1;
		int[][] adjacencyMatrix = new int [intfsConnMatrixSize][intfsConnMatrixSize];	// 0'th row and column are not used to be inlined with composer input connectivity matrix  
		
		//	----------------------------------- Construct HashMap that maps Switch-Intfs-Mac  -->  Switch-Intfs-resourceURI	---------------------------------------
		for (Datapath dpObj : datapathObjArray){
			for (Intfs dpIntfsObj : dpObj.getIntfsObjArray()){
				switchMacToIntfsUriMap.put(dpIntfsObj.getMacAddress().replaceAll("^(00:)*", ""), dpIntfsObj.getResourceURI()); // ^(00:)* is the regEx used to remove leading zeros from Mac
			}
		}
		if(INTFS_CONNECT_DBG){
			System.out.println("\nPrint macToIntfsUriMap entries \n Switch-Intfs-Mac \t Switch-Intfs-resourceURI");
			for(Entry<String, String> dpMacEntry : switchMacToIntfsUriMap.entrySet()) System.out.println(dpMacEntry.getKey()+"\t --> \t"+dpMacEntry.getValue());
		}
		
		
		// ----------------------------------------- Read Links obtaind from Flowvisor and populate Adjacency Matrix -----------------------------------------------
		//	interSwitchLinkObjArray contains connectivity information obtained form flowvisor. 
		//	Hence it only contains links between switches and does not have connectivity information between Hosts-Switches and VMs-switches
		// 	Populating Switch to Switch Quadrent in adjacencyMatrix
		//		S-1	S-2	S-3	S-4	S-5	S-6
		//	S-1	 0	 1	 1	 1	 0	 1
		//	S-2	 0	 0	 1	 1	 0	 1
		//	S-3	 0	 1	 0	 1	 0	 1
		//	S-4	 0	 1	 1	 0	 0	 1
		//	S-5	 0	 0	 0	 0	 0	 1
		//	S-6	 0	 1	 1	 1	 1	 0
		for (Link linkObj : linkObjList){
			int srcIntfsSplit = Integer.parseInt(linkObj.getSrcIntfsResourceURI().split("-|\\_")[1]);
			int destIntfsSplit = Integer.parseInt(linkObj.getDestIntfsResourceURI().split("-|\\_")[1]);
			adjacencyMatrix[numOfVmHosts + srcIntfsSplit][numOfVmHosts + destIntfsSplit] = 1;
		}
		
		
		//	------------------------------------	Put Ovs-Intfs-resourceURI --> vmHost-Intfs-connectedIntfsUriList  ----------------------------------------
		//	----------------------------	Construct HashMap that maps Switch-Intfs-resourceURI  -->  vmHost-Intfs-resourceURI	------------------------------
		//	----------------------------------------------------	Adding Host to OVS link to linkObjList ---------------------------------------------------
		// Populating Host to Switch Quadrent in adjacencyMatrix
		//		S-1	S-2	S-3	S-4	S-5	S-6
		//	H-1  1	 0	 0	 0	 0	 0
		//	H-2	 0	 1	 0	 0	 0	 0 
		//	H-3	 0	 0	 0	 1	 0	 0
		//	H-4	 0	 0	 1	 0	 0	 0
		int linkNum = linkObjList.size();
		if(READ_PHYSICAL_IT)
		for(VmHost vmHostObj : vmHostObjArray)
			for (Intfs hostIntfsObj : vmHostObj.getIntfsObjArray()){
				String connectedOvsIntfsURI =  switchMacToIntfsUriMap.get(hostIntfsObj.getMacAddress());					// get the swith URI with same MAC as host interface
				if(connectedOvsIntfsURI != null){
					linkNum++;
					ovsIntfsToHostIntfsMap.put(connectedOvsIntfsURI, hostIntfsObj.getResourceURI());
					hostIntfsObj.setConnectedIntfsURI(connectedOvsIntfsURI);
					int switchNum = Integer.parseInt(connectedOvsIntfsURI.split("-|\\_")[1]);
					// Adding vmHost-Intfs <--> Switch-Intfs connection to adjacencyMatrix[][]
					adjacencyMatrix[vmHostObj.getVmHostNum()][numOfVmHosts + switchNum] = 1;	// nonUniqueId: 1 for H-1, 2 for H-2, 1 for S-1 etc.. 
					// Creating Link object and for Host to OVS link and add to linkObjList
					Link linkObj = new Link("UDL-"+linkNum, hostIntfsObj.getResourceURI()+"_P-0", connectedOvsIntfsURI+"_P-1");	// TODO: OVS port 1 assumed to be connected to port 0 of its Host computer 
					linkObj.setSrcIntfsMac(hostIntfsObj.getMacAddress());
					linkObj.setDestIntfsMac(hostIntfsObj.getMacAddress());
					linkObjList.add(linkObj);	
					
				}
			}
		
		if(INTFS_CONNECT_DBG){
			System.out.println("\nPrint ovsIntfsToHostIntfsMap entries \n Ovs-Intfs-resourceURI \t vmHost-Intfs-resourceURI");
			for(Entry<String, String> intfsMapEntry : ovsIntfsToHostIntfsMap.entrySet()) System.out.println("\t"+intfsMapEntry.getKey()+"\t\t --> \t"+intfsMapEntry.getValue());
		}
		
		
		// ------------------------------------------populate connectedIntfsUriList, switch-location and Adjacency Matrix --------------------------------------
		//	1.	Put vmHost-Intfs-resourceURI --> Ovs-Intfs-connectedIntfsUriList
		//	2.	Adding hostResourceURI as switch location for OVS's
		// 	3.	Populating Switch to Host Quadrent in adjacencyMatrix (Assumption: All physical switches is connected to physical host interfaces through OVS's) 
		// 		H-1	H-2	H-3	H-4	
		//  S-1	 1	 0	 0	 0	
		//  S-2	 0	 1	 0	 0	
		//  S-3	 0	 0	 0	 1	
		//  S-4	 0	 0	 1	 0	
		//  S-5	 0	 0	 0	 0	
		//  S-6	 0	 0	 0	 0		 
		for (Datapath switchObj : datapathObjArray){
			for (Intfs switchIntfsObj : switchObj.getIntfsObjArray()){
				String ovsIntfsUri = switchIntfsObj.getResourceURI();
				String connectedHostIntfsURI = ovsIntfsToHostIntfsMap.get(ovsIntfsUri);
				if(connectedHostIntfsURI != null){
					linkNum++;
					switchIntfsObj.setConnectedIntfsURI(connectedHostIntfsURI);
					switchObj.setResLocation(connectedHostIntfsURI.split("_")[0]);
					int vmHostNum = Integer.parseInt(connectedHostIntfsURI.split("-|\\_")[1]);
					adjacencyMatrix[numOfVmHosts+switchObj.getSwitchNum()][vmHostNum] = 1;	// Adding Switch-Intfs to vmHost-Intfs connection to adjacencyMatrix[][]
					// Creating Link object and for Host to OVS link and add to linkObjList
					Link linkObj = new Link("UDL-"+linkNum, ovsIntfsUri+"_P-1", connectedHostIntfsURI+"_P-0");	// TODO: OVS port 1 assumed to be connected to port 0 of its Host computer 
					linkObj.setSrcIntfsMac(switchIntfsObj.getMacAddress());
					linkObj.setDestIntfsMac(switchIntfsObj.getMacAddress());
					linkObjList.add(linkObj);
					
				}
			}
		}
		
		
		
		
		// Print Adjacency Matrix
		
		if(INTFS_CONNECT_DBG){
			System.out.println("\nAdjacency Martix after adding inter-switch links");
			for(int fromItr = 1; fromItr<intfsConnMatrixSize; fromItr++){
				if(fromItr == 1){
					//System.out.print("\t");
					for (int hItr = 0; hItr<numOfVmHosts; hItr++) System.out.print("\tH-"+(fromItr+hItr));
					for (int sItr = 0; sItr<numOfSwitches; sItr++) System.out.print("\tS-"+(fromItr+sItr));
					System.out.println();
				}
				if(fromItr<=numOfVmHosts)System.out.print("H-"+(fromItr));
				else System.out.print("S-"+(fromItr-numOfVmHosts));
				for(int toItr = 1; toItr<intfsConnMatrixSize; toItr++)
					System.out.print("\t "+adjacencyMatrix[fromItr][toItr]);
				System.out.println();
			}
		}
		
		// -----------------------	Adding other linked (connected by link) switch interfaces to Switch-Intfs-setConnectedIntfsUriList	-------------------------
		for (Datapath dpObj : datapathObjArray)
			for (Intfs dpIntfsObj : dpObj.getIntfsObjArray()){
				int switchId = dpObj.getSwitchNum();
				for(int dpItr = 1; dpItr<=numOfSwitches; dpItr++)
					if(adjacencyMatrix[numOfVmHosts+switchId][numOfVmHosts+dpItr] > 0)
						dpIntfsObj.setConnectedIntfsURI("S-"+(dpItr)+"_I-0");
			}
		
		
		
		//Link[] allLinkObjArray = new Link[];
		
		
		// Print Host interfaces and switch interfaces
		if(INTFS_CONNECT_DBG){
			System.out.print("\n\nOVS interfaces connected to vmHosts");
			if(READ_PHYSICAL_IT)
			for (VmHost vmHostObj : vmHostObjArray)
				for (Intfs vmHostIntfsObj : vmHostObj.getIntfsObjArray()){
					System.out.print("\n"+vmHostIntfsObj.getResourceURI()+"\t -->  ");
					for(String connectedIntfs : vmHostIntfsObj.getConnectedIntfsUriList())
						System.out.print("\t"+connectedIntfs);
				}
			
			System.out.print("\n\nHost and Switch Interfaces connected to switch interfaces");
			for (Datapath dpObj : datapathObjArray)
				for (Intfs dpIntfsObj : dpObj.getIntfsObjArray()){
					System.out.print("\n"+dpIntfsObj.getResourceURI()+"\t -->  ");
					for(String connectedIntfs : dpIntfsObj.getConnectedIntfsUriList())
						System.out.print("\t  "+connectedIntfs);
				}
		}
		
	}// End of buildConnectivity()
	
	
	public static void writeToResultBuildPrPool() {
		try{
		PrintWriter printwriter = new PrintWriter(new FileWriter(BLDPR_PERF_FILE, true));
		printwriter.println(constPrPoolResultLine);
		printwriter.close();
		} catch(IOException ioEx){
			System.err.println("BuildPrTopology.writeToResultBuildPrPool: File writer exception caught");
			ioEx.printStackTrace();
		}
	}
	
	
}// End of Class
