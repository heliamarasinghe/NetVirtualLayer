package com.imaginelab.sdn_icf.deploy;

import static com.imaginelab.sdn_icf.main.Constants.BUILD_SLICE_DBG;
import static com.imaginelab.sdn_icf.main.Constants.COMMIT_DEPLOYMENT;
import static com.imaginelab.sdn_icf.main.Constants.FV_URI;
import static com.imaginelab.sdn_icf.main.Constants.SLICE_CREATE_CHECK;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.imaginelab.sdn_icf.containers.AcceptedReq;
import com.imaginelab.sdn_icf.containers.Datapath;
import com.imaginelab.sdn_icf.containers.PrintContainerValues;
import com.imaginelab.sdn_icf.containers.VM;
import com.imaginelab.sdn_icf.containers.VSw;
import com.imaginelab.sdn_icf.discover.FlowvisorApiCall;

public class CreateSliceForReq {

	// ============================================================================================================================== //
	// ---------------------------------------------------------- buildSlice() ------------------------------------------------------ //
	// ============================================================================================================================== //
	public static int buildSlice(AcceptedReq acceptedReq, HashMap<String, VM> allocVmMapForReq, HashMap<String, VSw> rsvdVSwIdToObjMap, HashMap<String, Datapath> datapathIdToObjMap, String controller_url){

		int threadSleepTime = 0;
		
		
		
		String reqUri = acceptedReq.getReqUri();
		
		//String fvReqMethod = "get-config";
		//String fvReqJsonString = new JsonObject().add("id",99).add("method", "get-config").add("params", new JsonObject()).add("jsonrpc", "2.0").toString();
		System.out.println("\nAdding slice for Request "+reqUri);
		//Multimap<String, String> hostedVmUris = getHostedVmUris(allocVmMapForReq);
		Map<String, String> hostedOvsUris = getHostedOvsUris(rsvdVSwIdToObjMap, datapathIdToObjMap);

		System.out.println("Allocated VMs = "+allocVmMapForReq.size());
		for(String allocVmUri : allocVmMapForReq.keySet()){
			VM allocVmObj = allocVmMapForReq.get(allocVmUri);
			System.out.println("\nresourceUri     \t = "+allocVmObj.getResourceURI());
			System.out.println("resourceLocation \t = "+allocVmObj.getResLocation());
			System.out.println("intfs.macAddress \t = "+allocVmObj.getIntfsObjArray()[0].getMacAddress());
			System.out.println("target VLAN-id \t = "+acceptedReq.getTargetVlan());
			
		}

		if(BUILD_SLICE_DBG)
			for(String rsvdVSwId : acceptedReq.getRsvdVsResList()){
				VSw vSwObj = rsvdVSwIdToObjMap.get(rsvdVSwId);
				PrintContainerValues.printVSwObj(vSwObj);
	
			}
			// TODO: Edit this to print only location Datapath info 
		if(BUILD_SLICE_DBG)
			for(String datapathUri : datapathIdToObjMap.keySet()){
				Datapath datapathObj = datapathIdToObjMap.get(datapathUri);
				PrintContainerValues.printDatapathObj(datapathObj);
	
			}
		
		//if(BUILD_SLICE_DBG)
		System.out.println("\nhostedOvsUris.size() = "+hostedOvsUris.size());
			for(String hostUri : hostedOvsUris.keySet())
				System.out.println("\t Host = "+hostUri+"\t Ovs = "+hostedOvsUris.get(hostUri));
		
		

		// Creating one flowvisor slice for each request
		JsonObject addSliceParamObj = new JsonObject();
		addSliceParamObj.add("slice-name", reqUri).add("controller-url", controller_url).add("admin-contact", "hamar040@uottawa.ca").add("password", "");
		JsonObject addSliceJsonReq= new JsonObject();
		addSliceJsonReq.add("id",1).add("method", "add-slice").add("params", addSliceParamObj).add("jsonrpc", "2.0");
		System.out.println(addSliceJsonReq.toString());
		// Commit slice creation by making flowvisor request
		if(COMMIT_DEPLOYMENT)makeFvRequest("add-slice", addSliceJsonReq.toString());
		else PrintContainerValues.prettyPrintJSONAsString(addSliceJsonReq.toString());
		
		// $ curl -k -d '{"id":1,"method":"add-slice","params":{"slice-name":"REQ-0","controller-url":"tcp:192.168.0.203:6633","admin-contact":"hamar040@uottawa.ca","password":""},"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.200:8081
		// Creating flowspace array
		//JsonArray addFlowspaceArray = new JsonArray();
		
		System.out.println("acceptedReq.getAlocVsResList().size() = "+acceptedReq.getRsvdVsResList().size());
		for (String rsvdVsResId : acceptedReq.getRsvdVsResList()){
			String virtualizedSwUri = rsvdVSwIdToObjMap.get(rsvdVsResId).getLocation();
			Datapath virtualizedSwObj = datapathIdToObjMap.get(virtualizedSwUri);
			String ovsMacAddress = virtualizedSwObj.getDpid();
			System.out.println("\t virtualizedSwUri = "+virtualizedSwUri+"\t  Location = "+virtualizedSwObj.getResLocation());
			
			if(COMMIT_DEPLOYMENT)
			try {
				Thread.sleep(2000);
				threadSleepTime += 2000;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Creating flowspace array
			JsonArray addFlowspaceArray = new JsonArray();
			// $ fvctl -f /dev/null add-flowspace all-host3-ovsbr0 00:00:d4:ae:52:d2:73:da  1 all pox1=7
			// $ curl -k -d '{"id":"1","method":"list-datapath-flowdb","params":{"dpid":"5e:3e:08:9e:01:93:5e:08"},"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.101:8081
			
			JsonObject sliceActionJsonObj = new JsonObject();
			sliceActionJsonObj.add("slice-name", reqUri).add("permission", 7);	//	{  "slice-name" : <name> ",  "permission" : <perm-value>  }
	
			JsonArray sliceActionJsonArray = new JsonArray();					//	[ {  "slice-name" : <name> ",  "permission" : <perm-value>  } ]
			sliceActionJsonArray.add(sliceActionJsonObj);
	
			JsonObject matchJsonObj = new JsonObject();							// 	{"dl_vlan":1000}
			matchJsonObj.add("dl_vlan", acceptedReq.getTargetVlan());
	
			JsonObject addFlowspaceParamObj = new JsonObject();
			//addFlowspaceParamObj.add("name",virtualizedSwUri+"_"+reqUri+"_"+flowSpaceNumber).add("dpid", ovsMacAddress).add("priority", 1).add("match", matchJsonObj).add("slice-action", sliceActionJsonArray);
			addFlowspaceParamObj.add("name",virtualizedSwUri+"_"+reqUri).add("dpid", ovsMacAddress).add("priority", 1).add("match", matchJsonObj).add("slice-action", sliceActionJsonArray);
			// {"name":"S-4_REQ-0","dpid":"00:00:f0:4d:a2:33:8e:7d","priority":1,"match":{"dl_src":"f0:62:15:9f:71:97"},"slice-action":[{"slice-name":"REQ-0","permission":7}]}
			
			addFlowspaceArray.add(addFlowspaceParamObj);
			// [{addFlowspaceParamObj1}, {addFlowspaceParamObj2}, ... ]
			
			JsonObject addFlowspaceJsonReq= new JsonObject();
			addFlowspaceJsonReq.add("id",1).add("method", "add-flowspace").add("params", addFlowspaceArray).add("jsonrpc", "2.0");
			
			//	Commit flowspace creation by making flowvisor request
			if(COMMIT_DEPLOYMENT)makeFvRequest("add-flowspace", addFlowspaceJsonReq.toString());
			//else PrintContainerValues.prettyPrintJSONAsString(addFlowspaceJsonReq.toString());
			
		}
		
		//JsonObject addFlowspaceJsonReq= new JsonObject();
		//addFlowspaceJsonReq.add("id",1).add("method", "add-flowspace").add("params", addFlowspaceArray).add("jsonrpc", "2.0");
		// {"id":1, "method":"add-flowspace", "params": [{addFlowspaceParamObj1}, {addFlowspaceParamObj2}, ... ], "jsonrpc":"2.0"}
		
		// curl -k -d '{"id":1,"method":"add-flowspace","params":[{"name":"S-1_REQ-0_FS-0","dpid":"00:0037d","priority":1,"match":{"dl_vlan":1000},"slice-action":[{"slice-name":"REQ-0","permission":7}]}],"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.200:8081
		// curl -k -d '{"id":1,"method":"add-flowspace","params":[{"name":"S-1_REQ-0_FS-0","dpid":"00:00:f0:4d:a2:33:8e:7d","priority":1,"match":{"dl_vlan":1000},"slice-action":[{"slice-name":"REQ-0","permission":7}]},{"name":"S-10_REQ-0_FS-1","dpid":"5e:3e:08:9e:01:93:5d:90","priority":1,"match":{"dl_vlan":1000},"slice-action":[{"slice-name":"REQ-0","permission":7}]},{"name":"S-4_REQ-0_FS-2","dpid":"00:00:d4:ae:52:d2:73:da","priority":1,"match":{"dl_vlan":1000},"slice-action":[{"slice-name":"REQ-0","permission":7}]},{"name":"S-3_REQ-0_FS-3","dpid":"5e:3e:08:9e:01:93:5e:08","priority":1,"match":{"dl_vlan":1000},"slice-action":[{"slice-name":"REQ-0","permission":7}]},{"name":"S-2_REQ-0_FS-4","dpid":"5e:3e:c4:54:44:4f:2b:24","priority":1,"match":{"dl_vlan":1000},"slice-action":[{"slice-name":"REQ-0","permission":7}]}],"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.200:8081
		
		
		
		
		// curl -k -d '{"id":1,"method":"add-flowspace","params":[{"name":"S-6_REQ-0","dpid":"00:00:f0:4d:a2:33:8e:7d","priority":1,"match":{"dl_src":"5e:ef:de:b5:7e:b2"},"slice-action":[{"slice-name":"REQ-0","permission":7}]},{"name":"S-5_REQ-0","dpid":"00:00:d4:ae:52:d2:73:da","priority":1,"match":{"dl_src":"32:bb:e5:61:66:fa"},"slice-action":[{"slice-name":"REQ-0","permission":7}]},{"name":"S-5_REQ-0","dpid":"00:00:d4:ae:52:d2:73:da","priority":1,"match":{"dl_src":"fa:83:bb:4b:cb:c0"},"slice-action":[{"slice-name":"REQ-0","permission":7}]}],"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.101:8081
		// 
		// curl -k -d '{"id":1,"method":"add-flowspace","params":[{"name":"S-3_REQ-0_FS-0","dpid":"00:00:f0:4d:a2:33:8e:7d","priority":1,"match":{"dl_src":"08:0d:40:bd:51:e2"},"slice-action":[{"slice-name":"REQ-0","permission":7}]}],"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.101:8081
		// curl -k -d '{"id":1,"method":"add-flowspace","params":[{"name":"S-4_REQ-0_FS-0","dpid":"00:00:d4:ae:52:d2:73:da","priority":1,"match":{"dl_src":"2c:ed:7a:9c:1a:2b"},"slice-action":[{"slice-name":"REQ-0","permission":7}]}],"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.101:8081
		// curl -k -d '{"id":1,"method":"add-flowspace","params":[{"name":"S-4_REQ-0_FS-1","dpid":"00:00:d4:ae:52:d2:73:da","priority":1,"match":{"dl_src":"00:34:63:74:74:dd"},"slice-action":[{"slice-name":"REQ-0","permission":7}]}],"jsonrpc":"2.0"}' https://fvadmin:@192.168.0.101:8081
		
		//PrintContainerValues.prettyPrintJSONAsString(addFlowspaceJsonReq.toString());
		//makeFvRequest("add-flowspace", addFlowspaceJsonReq.toString());
		return threadSleepTime;
	}

	
	
	// ============================================================================================================================== //
	// --------------------------------------------------------- makeFvRequest() ---------------------------------------------------- //
	// ============================================================================================================================== //
	private static void makeFvRequest(String fvReqMethod, String fvReqJsonString){
		System.out.println(fvReqJsonString);																							//print request
		FlowvisorApiCall fvApiCall = new FlowvisorApiCall(FV_URI);

		String fvResponse = null;
		fvApiCall.sendFvRequest(fvReqJsonString);
		fvResponse = fvApiCall.getFvResponse();

		if (fvResponse != null){
			System.out.println(fvResponse+"\n");																						//print response
			JsonObject fvResponceJsonObj = JsonObject.readFrom( fvResponse );	

			if(fvResponceJsonObj.get("error") != null){
				if(fvResponceJsonObj.get("error").isObject()){
					JsonObject errorJsonObj = fvResponceJsonObj.get("error").asObject();
					System.err.println("Flowvisor Request \""+fvReqMethod+"\" unsatisfied");
					System.err.println("\tMessage from FV :"+errorJsonObj.get("message").asString());
					System.err.println("\t Received Error Code :"+fvErrorCodeMap.get(errorJsonObj.get("code").asInt())+"\n");
				}
			}
			if(SLICE_CREATE_CHECK){
				switch(fvReqMethod){
				case "add-slice": 																										// If new slice added, list-slices
					JsonObject listSlicesJsonReq= new JsonObject();
					listSlicesJsonReq.add("id",1).add("method", "list-slices").add("jsonrpc", "2.0");
					makeFvRequest("list-slices", listSlicesJsonReq.toString());
					break;
	
				case "add-flowspace": 																									// If new flowspace is added, list-flowspace
					JsonObject listFlowspacesJsonReq= new JsonObject();
					listFlowspacesJsonReq.add("id",2).add("method", "list-flowspace").add("params", new JsonObject()).add("jsonrpc", "2.0");
					makeFvRequest("list-flowspace", listFlowspacesJsonReq.toString());
					break;
	
				default:
				}
			}
			
			
			
		}
		else System.err.println("CreateSliceForReq.makeFvRequest: \"null\" respond from flowvisor");


	}
	// ============================================================================================================================== //
	// ------------------------------------------------------ updateMacToVlanFiles() ------------------------------------------------ //
	// ============================================================================================================================== //
	//private static void updateMacToVlanFiles(){
		
		
	//}
	
	
	// ============================================================================================================================== //
	// ------------------------------------------------------- getHostedVmUris() ---------------------------------------------------- //
	// ============================================================================================================================== //
	//private static Multimap<String, String> getHostedVmUris(HashMap<String, VM> allocVmMapForReq){
	//	Multimap<String, String> hostedVmUris = ArrayListMultimap.create();
	//	for(String vmUri : allocVmMapForReq.keySet())
	//		hostedVmUris.put(allocVmMapForReq.get(vmUri).getResLocation(), vmUri);
	//	return hostedVmUris;
	//}
	
	// ============================================================================================================================== //
	// ------------------------------------------------------- getHostedOvsUris() --------------------------------------------------- //
	// ============================================================================================================================== //
	private static Map<String, String> getHostedOvsUris(HashMap<String, VSw> rsvdVSwIdToObjMap, HashMap<String, Datapath> datapathIdToObjMap){
		Map<String, String> hostedOvsUris  = new HashMap<String, String>();
		for(String vSwUri : rsvdVSwIdToObjMap.keySet()){
			String virtualizedSwitchUri = rsvdVSwIdToObjMap.get(vSwUri).getLocation();	// virtual switch (VSw) 'location' contains virtualized switch (parent switch <-- might be an Ovs or a physical switch)
			if(hostedOvsUris.containsKey(virtualizedSwitchUri)) 
				System.err.println("CreateSliceForReq.getHostedOvsUris: Key Duplication detected (Two OVSs in same host");
			
			if(datapathIdToObjMap.containsKey(virtualizedSwitchUri))
				hostedOvsUris.put(datapathIdToObjMap.get(virtualizedSwitchUri).getResLocation(), vSwUri);
		}
		return hostedOvsUris;
	}
	
	
// --------------------------- immutable Error code map -------------------------------------//
	private static final Map<Integer, String> fvErrorCodeMap;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(-32700, "Parse error (Invalid JSON was received by the server)");
        aMap.put(-32600, "Invalid Request (The JSON sent is not a valid Request object)");
        aMap.put(-32601, "Method not found (The method does not exist / is not available)");
        aMap.put(-32602, "Invalid params (Invalid method parameter/parameters)");
        aMap.put(-32603, "Internal error (Internal JSON-RPC error)");
        fvErrorCodeMap = Collections.unmodifiableMap(aMap);
    }

}


/*
 		//------------------------------------------ get-config ----------------------------------------------//
		JsonObject getConfParamObj= new JsonObject();
		getConfParamObj.add("slice-name", "floodlight1");
		getConfParamObj.add("dpid", "04:f0:92:1c:3d:a7:00");
		JsonObject getConfigJsonReq= new JsonObject();
		getConfigJsonReq.add("id",1).add("method", "get-config").add("params", getConfParamObj).add("jsonrpc", "2.0");
		//{"id":1,"method":"get-config","params":{"slice-name":"floodlight1","dpid":"00:04:f0:92:1c:3d:a7:00"},"jsonrpc":"2.0"}
		//------------------------------------------ get-config ----------------------------------------------//

		//------------------------------------------ list-datapath-info ----------------------------------------------//
		JsonObject listDpInfoParamObj = new JsonObject();
		listDpInfoParamObj.add("dpid", "00:00:f0:4d:a2:33:8e:7d");

		JsonObject listDpInfoJsonReq = new JsonObject();
		listDpInfoJsonReq.add("id",1).add("method", "list-datapath-info").add("params", listDpInfoParamObj).add("jsonrpc", "2.0");
		//------------------------------------------ list-datapath-info ----------------------------------------------// 
*/

/*
[
 	{ 
   		"name" : <string>,
   		"dpid" : <dpid>,
   		"priority" : <number>, 
   		"match" : <match-struct>,
   		"queues" : [ <queue_id> ],    OPTIONAL
   		"force-enqueue" : <queue_id>, OPTIONAL
   		"slice-action" :
   		[
    		{ 
      			"slice-name" : <name> ", 
      			"permission" : <perm-value> 
    		}
   		]

 	}
]

----------------------------------------------------
[
	{
		"name":"S-4_REQ-0",
		"dpid":"00:00:f0:4d:a2:33:8e:7d",
		"priority":1,
		"match":"dl_src=f0:62:15:9f:71:97",
		"slice-action":
			[
				{
					"slice-name":"REQ-0",
					"permission":7
				}
			]
	}
]


*/