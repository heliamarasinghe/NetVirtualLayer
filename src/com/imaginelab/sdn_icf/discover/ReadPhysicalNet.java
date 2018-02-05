package com.imaginelab.sdn_icf.discover;

import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.imaginelab.sdn_icf.containers.*;

import static com.imaginelab.sdn_icf.main.Constants.FV_URI;
import static com.imaginelab.sdn_icf.main.Constants.GET_CONFIG;
import static com.imaginelab.sdn_icf.main.Constants.LINK_DBG;
import static com.imaginelab.sdn_icf.main.Constants.SET_CONFIG;
import static com.imaginelab.sdn_icf.main.Constants.SWCH_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.SWITCH_DBG;
import static com.imaginelab.sdn_icf.main.Constants.UDLINK_PRFX;

public class ReadPhysicalNet {

	private static Datapath[] datapathObjArray = null;
	private static Link[] linkObjArray = null;



	// ================================================================================================================================== //
	// -------------------------------------------------------- updateNetState() -------------------------------------------------------- //
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ Method Invoked from BuildPrTopology ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ //
	// ================================================================================================================================== //
	public static boolean updateNetState(){

		int iFvReqId = 1;
		FlowvisorApiCall fvApiCall = new FlowvisorApiCall(FV_URI);

		System.out.println("\tObtaining Switch and Link information from Flowvisor .....");

		// Get all switch DPIDs and populate intfsMacToUriMap<>
		HashMap <String, String> intfsMacToUriMap = listDpids(fvApiCall, iFvReqId++);		// Also polpulates switchIdToMacMap
		if(SWITCH_DBG) for( Map.Entry<String, String> entry : intfsMacToUriMap.entrySet() ) 
			System.out.println(entry.getKey()+" --> "+entry.getValue());

		// Print All Datapath Objects and populate datapathObjArray[]
		listDpInfo(fvApiCall, iFvReqId++, intfsMacToUriMap);
		if(SWITCH_DBG) {
			System.out.println("\n Printing Datapath Objects");
			for(Datapath datapathObj : datapathObjArray)
				PrintContainerValues.printDatapathObj(datapathObj);
		}

		// Print All Link Objects
		listLinks(fvApiCall, iFvReqId++, intfsMacToUriMap);
		if(LINK_DBG){
			System.out.println("\n Printing Link Objects");
			for(Link linkObj : linkObjArray)
				PrintContainerValues.printLinkObj(linkObj);
		}

		String testMac = "00:04:f0:92:1c:3d:a7:00";		//hp switch MAC to test with get-config

		// Get current FlowVisor configurations
		if(GET_CONFIG){
			JsonObject fvReqParamObj= new JsonObject();
			fvReqParamObj.add("slice-name", "floodlight1");
			fvReqParamObj.add("dpid", testMac);
			getConfig(fvApiCall, iFvReqId++, fvReqParamObj);
		}
		// Set Flowvisor Configuration
		if(SET_CONFIG) setConfig(fvApiCall, iFvReqId++, testMac);

		if(datapathObjArray != null && linkObjArray != null) 
			return true; 
		else 
			return false;

	}





	// ============================================================================================================================== //
	// ---------------------------------------------------------- buildSlice() ------------------------------------------------------ //
	//  Method that reads all avilable switch-interface MAC (DPID) addresses and put them in to a Map data structure, assigning each of them a unique ID.
	//  Interface IDs will change at each run of the program.
	//   	switchResourceURI	= S-1, S-2, S-3, ...
	//   	intfsResourceURI	= S-1_I-0, S-2_I-0, S-3_I-0, ....
	// ============================================================================================================================== //
	public static HashMap<String, String> listDpids(FlowvisorApiCall fvApiCall, int iFvReqId){
		String fvReqId = Integer.toString(iFvReqId);
		String fvReqJsonString = null;
		//String[] dpidSArray = null;
		HashMap <String, String> intfsMacToUriMap = new HashMap<String, String>();

		if(SWITCH_DBG) System.out.println("\nCalled method = LIST_DATAPATHS");
		JsonObject fvReqJsonObj = new JsonObject();
		fvReqJsonObj.add("id",fvReqId).add("method", "list-datapaths").add("jsonrpc", "2.0");
		fvReqJsonString = fvReqJsonObj.toString();

		if(fvReqJsonString != null){ 
			fvApiCall.sendFvRequest(fvReqJsonString);
			String fvResponse = fvApiCall.getFvResponse();
			if (fvResponse != null){
				JsonObject jsonObject = JsonObject.readFrom( fvResponse );
				JsonArray dpidArray = jsonObject.get("result").asArray();
				//dpidSArray = new String[dpidArray.size()];
				String intfsResourceURI = "empty_from_listDpids";
				for (int dpidItr = 0; dpidItr<dpidArray.size(); dpidItr++){
					//dpidSArray[i] = dpidArray.get(i).asString();
					intfsResourceURI = SWCH_PRFX+"-"+(dpidItr+1)+"_I-0";
					intfsMacToUriMap.put(dpidArray.get(dpidItr).asString(), intfsResourceURI);			// intfsURIToMacMap = {"00:e0:18:7e:fe:aa", "S-1_I-0"}, {"f0:4d:a2:33:8e:7d", "S-2_I-0", },...			
				}
			}
		}
		return intfsMacToUriMap;
	}

	// ============================================================================================================================== //
	// ---------------------------------------------------------- buildSlice() ------------------------------------------------------ //
	// Method That takes switch MAC (DPID) as input and provide port and flow details of the switch
	// Currently resource_id field in Datapath is statically assigned. 
	// This might lead to not-unique Datapath IDs because each topology read generate new IDs.
	// Request--> {"id":"1","method":"list-datapath-info","params":{"dpid":"00:00:f0:4d:a2:33:8e:7d"},"jsonrpc":"2.0"}
	// Response-->{"id":"1","result":{"current-flowmod-usage":{"floodlight1":0,"fvadmin":0},"connection":"\/192.168.0.101:6633-->\/192.168.0.121:46263","port-list":[3,1,4,2,65534],"num-ports":5,"dpid":"00:00:f0:4d:a2:33:8e:7d","port-names":["vnet1","eth1","vnet2","vnet0","ovsbr0"]},"jsonrpc":"2.0"}
	// 
	// 		Switch	resourceName = Switch_1, Swith_2, ...
	// 				resourceId	 = set at listDatapaths().switchIdToMacMap = 100, 200, 300, ...
	// 		Port	resourceName = ge1/1/1, ge1/1/2, eth0, eth1, A1, A2, ...
	// 				resourceId 	 = 101, 102, ... 201, 202, ...
	// ============================================================================================================================== //
	public static void listDpInfo(FlowvisorApiCall fvApiCall, int FvReqId, HashMap<String, String> intfsMacToUriMap){
		//String fvReqId = Integer.toString(iFvReqId);
		String fvReqJsonString = null;
		int numberOfSwitches = intfsMacToUriMap.size();
		datapathObjArray = new Datapath[numberOfSwitches];

		//Link[] listLinkResultArray = null;

		if(SWITCH_DBG)System.out.println("\nCalled method = LIST_DATAPATH_INFO");
		int dpthItr = 0;
		for(Map.Entry<String, String> entry : intfsMacToUriMap.entrySet()){

			JsonObject fvReqParamObj = new JsonObject();
			fvReqParamObj.add("dpid", entry.getKey());

			JsonObject fvReqJsonObj = new JsonObject();
			fvReqJsonObj.add("id",FvReqId).add("method", "list-datapath-info").add("params", fvReqParamObj).add("jsonrpc", "2.0");
			//fvReqJsonObj.add("id",fvReqId).add("method", "list-datapath-flowdb").add("params", fvReqParamObj).add("jsonrpc", "2.0");
			fvReqJsonString = fvReqJsonObj.toString();

			if(fvReqJsonString != null){
				fvApiCall.sendFvRequest(fvReqJsonString);
				String fvResponse = fvApiCall.getFvResponse();
				datapathObjArray[dpthItr] = new Datapath();



				JsonObject jsonObject = JsonObject.readFrom( fvResponse );
				if(SWITCH_DBG) System.out.println("fvResponse from listDPInfo = "+fvResponse);		

				// Verify the result
				String receivedMac = jsonObject.get("result").asObject().get("dpid").asString();
				if(!entry.getKey().equals(receivedMac)) System.err.println("UpdateVrPoolNet.listDpInfo()--> WARNING: Switch MAC address not matched");

				// Populating variables in Datapath container
				String[] extractSwitchURI = entry.getValue().split("_"); // intfsMacToUriMap entry.getValue = "S-1_I-0". It need to be split by "_' to get switch URI
				String switchResourceURI = extractSwitchURI[0];														// switchResourceURI
				String[] extractNonUniqId = switchResourceURI.split("-");
				String switchResourceName = "Switch_"+switchResourceURI;											// switchResourceName
				datapathObjArray[dpthItr].setResourceURI(switchResourceURI);										// resourceId: = S-1, S-2, S-3, ...
				datapathObjArray[dpthItr].setResourceName(switchResourceName);										// switch_S-1, switch_S-2 , ...
				datapathObjArray[dpthItr].setSwitchNum(Integer.parseInt(extractNonUniqId[1]));						// nonUniqId = 1,2,3, ...
				datapathObjArray[dpthItr].setDpid(receivedMac);														// MAC address
				String connection = jsonObject.get("result").asObject().get("connection").asString();
				datapathObjArray[dpthItr].setConnection(connection);												// connection

				//for currentFlowmodUsage																		// flowmodUsageMap
				JsonObject currentFlowmodUsage = jsonObject.get("result").asObject().get("current-flowmod-usage").asObject();
				HashMap <String, Integer> flowmodUsageMap = new HashMap<String, Integer>();
				for( Member member : currentFlowmodUsage ) {
					String name = member.getName();
					int value = member.getValue().asInt();
					flowmodUsageMap.put(name, value);
					//System.out.println(name+" : "+value);
				}
				datapathObjArray[dpthItr].setFlowmodUsageMap(flowmodUsageMap);

				// TODO listDpInfoResult[iDp].setCpu(cpu);														// cpu
				// TODO listDpInfoResult[iDp].setMemory(memory);												// memory
				// TODO listDpInfoResult[iDp].setFlow_table_size(flow_table_size);								// flow_table_size



				// We assume that each switch has only one interface
				int numOfIntfs = 1;
				Intfs[] intfsObjArray = new Intfs[numOfIntfs]; 
				String intfsResourceURI = entry.getValue();							// entry.getValye = S-1_I-0, S-2_I-0, ...
				intfsObjArray[0] = new Intfs(intfsResourceURI);											
				String intfsResourceName = "interface_"+switchResourceName;
				//intfsObjArray[0].setResourceURI(intfsResourceURI);		//  Since each switch has only one interface, intfsResourceURI = S-0_I-0, S-1_I-0, S-2_I-0 ... 
				intfsObjArray[0].setResourceName(intfsResourceName);
				intfsObjArray[0].setMacAddress(receivedMac);

				String[] extractIpFromConnection = connection.split("/|:");
				if(extractIpFromConnection.length>=3) intfsObjArray[0].setIpAddress(extractIpFromConnection[3]);										// ip_address
				intfsObjArray[0].setNumPorts(jsonObject.get("result").asObject().get("num-ports").asInt());// numPorts	

				// Each Interface has set of ports known as portList																					// portNumsNames
				JsonArray portList = jsonObject.get("result").asObject().get("port-list").asArray();
				JsonArray portNames = jsonObject.get("result").asObject().get("port-names").asArray();
				if(portList.size() != intfsObjArray[0].getNumPorts()) System.err.println("WARNING: Port size not matched. Detected at UpdateVrPool.listDpInfo()");
				Port[] portObjArray = new Port[portList.size()];
				for (int portItr = 0; portItr<portList.size(); portItr++){	
					portObjArray[portItr] = new Port();
					portObjArray[portItr].setPortNumber(portList.get(portItr).asInt());
					portObjArray[portItr].setResourceName(portNames.get(portItr).asString());
					portObjArray[portItr].setResourceURI(intfsResourceURI+"_P-"+ portList.get(portItr).asInt());		// Port resourceId: S-1_P-1, S-1_P-2.....
					//				if (portList.get(portItr).asInt()<100)
					//					portObjArray[portItr].setResourceURI(datapathObjArray[dpthItr].getResourceURI()+"P-"+ portList.get(portItr).asInt());		// Port resourceId: S-1_P-1, S-1_P-2.....
					//				else if(portList.get(portItr).asInt()<1000) 
					//					portObjArray[portItr].setResourceURI(Integer.toString(datapathObjArray[dpthItr].getResourceURI() + portList.get(portItr).asInt()));
					//				else portObjArray[portItr].setResourceURI(Integer.toString(datapathObjArray[dpthItr].getResourceURI()*10000 + portList.get(portItr).asInt()));
					//local switch port is usually 65534. Its port IDs are 1065534, 2065534, 3065543 ...
				}
				intfsObjArray[0].setPortObjArray(portObjArray);
				datapathObjArray[dpthItr].setIntfsObjArray(intfsObjArray);
			}
			dpthItr++;
		}

		//return datapathObjArray;
	}

	// ============================================================================================================================== //
	// ----------------------------------------------------------- listLinks() ------------------------------------------------------ //
	// Method to retrieve all the links in the physical topology and populate link object array:  Link[] linkObjArray
	// Currently resource_id field in Link is statically assigned. 
	// This might lead to not-unique link IDs because each topology read generate new IDs.
	// TODO Guarantee Link resource_id uniqueness
	// 		Link resourceName 	= Link_1, Link_2,....
	// 			 resourceId 	= 10001, 10002, ...
	// 			 srcPort 		= srcDPID + 1, srcDPID + 2, ... = 101, 102,...
	// 			 destPort 		= destDPID +1, destDPID +2, ... = 101, 102,...
	//  Assumption: Links does not have internal virtual port 65534
	// ============================================================================================================================== //
	public static void listLinks(FlowvisorApiCall fvApiCall, int iFvReqId, HashMap<String, String> intfsMacToUriMap){



		String fvReqId = Integer.toString(iFvReqId);
		String fvReqJsonString = null;
		//Link[] linkObjArray = null;
		//String[] dpidSArray = null;

		if(LINK_DBG) System.out.println("\nCalled method = LIST_LINKS");
		JsonObject fvReqJsonObj = new JsonObject();
		fvReqJsonObj.add("id",fvReqId).add("method", "list-links").add("jsonrpc", "2.0");
		fvReqJsonString = fvReqJsonObj.toString();

		if(fvReqJsonString != null){ 
			fvApiCall.sendFvRequest(fvReqJsonString);
			String fvResponse = fvApiCall.getFvResponse();
			if(LINK_DBG) System.out.println(fvResponse);
			if (fvResponse != null){
				JsonObject jsonObject = JsonObject.readFrom( fvResponse );
				JsonArray linkArray = jsonObject.get("result").asArray();
				linkObjArray = new Link[linkArray.size()];
				for (int linkItr = 0; linkItr<linkArray.size(); linkItr++) {
					String srcIntfsURI = intfsMacToUriMap.get(linkArray.get(linkItr).asObject().get("srcDPID").asString());		// srcResourceURI = S-0_I-0
					int srcPort = Integer.parseInt(linkArray.get(linkItr).asObject().get("srcPort").asString());
					String srcPortURI = srcIntfsURI+"_P-"+srcPort;																		// srcPortURI = S-0_I-0_P-0
					String destIntfsURI = intfsMacToUriMap.get(linkArray.get(linkItr).asObject().get("dstDPID").asString());
					int destPort = Integer.parseInt(linkArray.get(linkItr).asObject().get("dstPort").asString());
					String destPortURI = destIntfsURI+"_P-"+destPort;

					//String linkResourceURI = srcPortURI+":"+destPortURI;						// linkResourceURI = UDL-1, UDL-2, ...
					String linkResourceURI = UDLINK_PRFX+"-"+(linkItr+1);
					String linkResourceName = "Link_"+(linkItr+1);

					linkObjArray[linkItr] = new Link();
					linkObjArray[linkItr].setResourceURI((linkResourceURI));						
					linkObjArray[linkItr].setResourcenName(linkResourceName);
					linkObjArray[linkItr].setSrcIntfsResourceURI(srcIntfsURI);													// temporarly use Switch URI instead of Interface URI 
					linkObjArray[linkItr].setSrcIntfsMac(linkArray.get(linkItr).asObject().get("srcDPID").asString());
					//linkObjArray[linkItr].setSrcPort(srcPort);			// Port resourceId: 1, 2, 3, ... 
					linkObjArray[linkItr].setSrcPortResourceURI(srcPortURI);											// Port resourceId: 101, 102, ... 201, 202, ...
					linkObjArray[linkItr].setDestIntfsResourceURI(destIntfsURI);
					linkObjArray[linkItr].setDestIntfsMac(linkArray.get(linkItr).asObject().get("dstDPID").asString());	
					//linkObjArray[linkItr].setDestPort(destPort);			// Port resourceId: 1, 2, 3, ...
					linkObjArray[linkItr].setDestPortResourceURI(destPortURI);										// Port resourceId: 101, 102, ... 201, 202, ...
					// Assumption: Links does not have internal virtual port 65534
				}
			}
		}
		//return linkObjArray;
	}

	// ============================================================================================================================== //
	// ----------------------------------------------------------- getConfig() ------------------------------------------------------ //
	// ============================================================================================================================== //
	public static void getConfig(FlowvisorApiCall fvApiCall, int iFvReqId, JsonObject fvReqParamObj){
		System.out.println("Called method = GET_CONFIG(dpid and/or slice-name)");
		JsonObject fvReqJsonObj= new JsonObject();
		fvReqJsonObj.add("id",iFvReqId).add("method", "get-config").add("params", fvReqParamObj).add("jsonrpc", "2.0");
		String fvReqJsonString = fvReqJsonObj.toString();
		System.out.println(fvReqJsonString);						//print request

		if(fvReqJsonString != null){ 
			fvApiCall.sendFvRequest(fvReqJsonString);
			String fvResponse = fvApiCall.getFvResponse();
			if (fvResponse != null){
				System.out.println(fvResponse+"\n");						//print response
				//JsonObject jsonObject = JsonObject.readFrom( fvResponse );
				//String host = jsonObject.get("result").asObject().get("host").asString();
				//System.out.println(host);
			}
		}
	}

	// ============================================================================================================================== //
	// ----------------------------------------------------------- setConfig() ------------------------------------------------------ //
	// ============================================================================================================================== //

	public static void setConfig(FlowvisorApiCall fvApiCall, int iFvReqId, String dpid){
		System.out.println("Called method = SET_CONFIG()");

		JsonObject floodPermObj = new JsonObject();
		floodPermObj.add("slice-name", "floodlight1").add("dpid", dpid);
		JsonObject flowmodLimit = new JsonObject();
		flowmodLimit.add("slice-name", "floodlight1").add("dpid", dpid).add("limit", 500);
		JsonObject fvReqParamObj = new JsonObject();
		fvReqParamObj.add("flood-perm", floodPermObj).add("flowmod-limit", flowmodLimit).add("track-flows", true).add("stats-desc", true).add("enable-topo-ctrl", true).add("flow-stats-cache", 30);

		JsonObject fvReqJsonObj = new JsonObject();
		fvReqJsonObj.add("id",iFvReqId).add("method", "set-config").add("params", fvReqParamObj).add("jsonrpc", "2.0");
		String fvReqJsonString = fvReqJsonObj.toString();
		System.out.println(fvReqJsonString);
		if(fvReqJsonString != null){ 
			fvApiCall.sendFvRequest(fvReqJsonString);
			String fvResponse = fvApiCall.getFvResponse();
			if (fvResponse != null){
				JsonObject jsonObject = JsonObject.readFrom(fvResponse);
				if(jsonObject.get("result").asBoolean())
					System.out.println("Configuration SUCCESSFUL");						//print response
				else
					System.err.println("Configuration FAILED");
			}
		}
	}





	public static Datapath[] getDatapathObjArray(){
		return datapathObjArray;
	}
	public static Link[] getLinkObjArray(){
		return linkObjArray;
	}

}




/*
 * -------------------- Request Example --------------------
 *
{ "id":6, "method":"get-config","params":{"slice-name":"floodlight1","dpid":"00:04:f0:92:1c:3d:a7:00"},"jsonrpc":"2.0"}


 *
 * -------------------- Responce Example ----------------------
 *
{
  "id":6,
  "result":{
	"flowmod-limit":{
		"floodlight1":{
			"00:04:f0:92:1c:3d:a7:00":-1
		}
	},
  	"enable-topo-ctrl":true,
 	"api_webserver_port":8080,
  	"db_version":"2",
  	"host":"localhost",
  	"log_ident":"flowvisor",
  	"checkpointing":false,
  	"logging":"NOTE",
  	"log_facility":"LOG_LOCAL7",
  	"stats-desc":false,
  	"version":"flowvisor-1.4.0",
  	"track-flows":false,
  	"config_name":"default",
  	"flow-stats-cache":30,
  	"api_jetty_webserver_port":8081,
  	"flood-perm":{
		"slice-name":"",
		"dpid":"00:04:f0:92:1c:3d:a7:00"
	}
  },
  "jsonrpc":"2.0"
}
 */
