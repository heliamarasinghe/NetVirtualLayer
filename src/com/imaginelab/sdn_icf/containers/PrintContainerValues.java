package com.imaginelab.sdn_icf.containers;



import java.util.ArrayList;

import com.imaginelab.sdn_icf.compose.interface_conn;

public class PrintContainerValues {
	
	// ============================================================================================================================== //
	// --------------------------------------------------- printGeneratedIaaSRequest() ---------------------------------------------- //
	// ============================================================================================================================== //
	public static void printGeneratedIaaSRequest(IaaSRequest iaaSRequestObj){
		System.out.println("IaaSRequest object");
		System.out.println("\t reqId = "+iaaSRequestObj.getReqId());
		System.out.println("\t reqClass = "+iaaSRequestObj.getReqClass());
		System.out.println("\t arrivalTime = "+iaaSRequestObj.getArrivalTime());
		System.out.println("\t reqDuratione = "+iaaSRequestObj.getReqDuration());
		System.out.println("\t minReqDuration = "+iaaSRequestObj.getMinReqDuration());
		//System.out.println("\t randMax = "+iaaSRequestObj.getRandMax());
		//System.out.println("\t status = "+iaaSRequestObj.getStatus());
		System.out.println("\t requestLifeTime = "+iaaSRequestObj.getRequestLifeTime());
		System.out.println("\n\t ArrayList<VResource>");
		/*ArrayList <VResource> vResPrintList = iaaSRequestObj.getVRs();
		for(VResource vResPrintObj : vResPrintList){
			System.out.println("\t*\t res_id = "+vResPrintObj.getResUri());
			System.out.println("\t\t opeation = "+vResPrintObj.getPriorityClass() );
			System.out.println("\t\t cpu = "+vResPrintObj.getCpu() );
			System.out.println("\t\t n_cores = "+vResPrintObj.getN_cores() );
			System.out.println("\t\t mem = "+vResPrintObj.getMem() );
			System.out.println("\t\t storage = "+vResPrintObj.getStr() );
			System.out.println("\t\t vmm = "+vResPrintObj.getVmm() );
			System.out.println("\t\t res_type = "+vResPrintObj.getRes_type() );
			System.out.println("\t\t bw = "+vResPrintObj.getBw() );
			System.out.println("\t\t delay = "+vResPrintObj.getDelay() );
			System.out.println("\t\t loss_rate = "+vResPrintObj.getLoss_rate() );
			System.out.println("\t\t stress_level = "+vResPrintObj.getStress_level() );
			System.out.println("\t\t loc = "+vResPrintObj.getLoc() );
			System.out.println("\t\t cost = "+vResPrintObj.getCost() );
			System.out.println("\t\t R_W_operations = "+vResPrintObj.getR_W_operations() );
			System.out.println("\t\t inter_type = "+vResPrintObj.getInter_type() );
			System.out.println("\t\t inter_dir = "+vResPrintObj.getInter_dir() );
			System.out.println("\t\t n_interfaces = "+vResPrintObj.getN_interfaces());
			System.out.println("\t\t ArrayList<<interface_conn>");
			ArrayList <interface_conn>  intfsConnPrintList = vResPrintObj.getInt_con();
			for(interface_conn intfsConnPrintObj : intfsConnPrintList){
				System.out.println("\t\t*\t bw = "+intfsConnPrintObj.getBw());
				System.out.println("\t\t\t delay = "+intfsConnPrintObj.getDelay());
				System.out.println("\t\t\t inter_type = "+intfsConnPrintObj.getInter_type());
			}
		}*/
	}
	
	// ============================================================================================================================== //
	// ------------------------------------------------------- printVResourceObj() -------------------------------------------------- //
	// ============================================================================================================================== //
	public void printVResourceObj(VResource vResPrintObj){
		System.out.println("\n\t*\t res_id = "+vResPrintObj.getResUri());
		System.out.println("\t\t opeation = "+vResPrintObj.getPriorityClass() );
		System.out.println("\t\t cpu = "+vResPrintObj.getCpu() );
		System.out.println("\t\t n_cores = "+vResPrintObj.getN_cores() );
		System.out.println("\t\t mem = "+vResPrintObj.getMem() );
		System.out.println("\t\t storage = "+vResPrintObj.getStr() );
		System.out.println("\t\t vmm = "+vResPrintObj.getVmm() );
		System.out.println("\t\t res_type = "+vResPrintObj.getRes_type() );
		System.out.println("\t\t bw = "+vResPrintObj.getBw() );
		System.out.println("\t\t delay = "+vResPrintObj.getDelay() );
		System.out.println("\t\t loss_rate = "+vResPrintObj.getLoss_rate() );
		System.out.println("\t\t stress_level = "+vResPrintObj.getStress_level() );
		System.out.println("\t\t loc = "+vResPrintObj.getLoc() );
		System.out.println("\t\t cost = "+vResPrintObj.getCost() );
		System.out.println("\t\t R_W_operations = "+vResPrintObj.getR_W_operations() );
		System.out.println("\t\t inter_type = "+vResPrintObj.getInter_type() );
		System.out.println("\t\t inter_dir = "+vResPrintObj.getInter_dir() );
		System.out.println("\t\t n_interfaces = "+vResPrintObj.getN_interfaces());
		System.out.println("\t\t ArrayList<<interface_conn>");
		ArrayList <interface_conn>  intfsConnPrintList = vResPrintObj.getInt_con();
		for(interface_conn intfsConnPrintObj : intfsConnPrintList){
			System.out.println("\t\t*\t bw = "+intfsConnPrintObj.getBw());
			System.out.println("\t\t\t delay = "+intfsConnPrintObj.getDelay());
			System.out.println("\t\t\t inter_type = "+intfsConnPrintObj.getInter_type());
		}
	}
	
	// ============================================================================================================================== //
	// --------------------------------------------------------- printVmHostObj() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static void printVmHostObj(VmHost vmHostObj){
		String vmHostName = vmHostObj.getResourceName();
		System.out.println("\n Obtained Information from "+vmHostName+" \n ====================================");
		System.out.println("\t resourceURI \t\t\t= "+vmHostObj.getResourceURI());
		System.out.println("\t resourceName \t\t\t= "+vmHostObj.getResourceName());
		System.out.println("\t model \t\t\t\t= "+vmHostObj.getResModel());
		System.out.println("\t hyperType \t\t\t= "+vmHostObj.getHyperType());
		System.out.println("\t hyperVer \t\t\t= "+vmHostObj.getHyperVer());
		System.out.println("\t uri \t\t\t\t= "+vmHostObj.getUri());
		System.out.println("\t cpuSockets \t\t\t= "+vmHostObj.getCpuSockets());
		System.out.println("\t activeCpus \t\t\t= "+vmHostObj.getActiveCpus());
		System.out.println("\t coresPerSocket \t\t= "+vmHostObj.getCoresPerSocket());
		System.out.println("\t threadsPerCore \t\t= "+vmHostObj.getThreadsPerCore());
		System.out.println("\t cpuFrequency \t\t\t= "+vmHostObj.getCpuFrequency());
		System.out.println("\t totalMemory \t\t\t= "+vmHostObj.getTotalMemory());
		System.out.println("\t availableMemory \t\t= "+vmHostObj.getAvailableMemory());
		System.out.println("\t totalStorage \t\t\t= "+vmHostObj.getTotalStorage());
		System.out.println("\t availableStorage \t\t= "+vmHostObj.getAvailableStorage());
		System.out.println("\t numOfOvs \t\t\t= "+vmHostObj.getNumOfOvs());
		
		if(vmHostObj.getIntfsObjArray() != null){
			System.out.println("\n\t NumOfInterfaces \t\t= "+vmHostObj.getNumOfInterfaces());
			for (Intfs intfsItr : vmHostObj.getIntfsObjArray())
				printIntfsObj(intfsItr);
		}
		
		if(vmHostObj.getActiveVmObjArray() != null){
			System.out.println("\n Active Virtual machines in "+vmHostName+" \n --------------------------------------");
			for (VM vmObj : vmHostObj.getActiveVmObjArray())
				printVmObj(vmObj);
		}
		
		if(vmHostObj.getDefinedVmObjArray() != null){
			System.out.println("\n Defined Virtual machines in "+vmHostName+" \n --------------------------------------");
			for (VM defVmObj : vmHostObj.getDefinedVmObjArray())
				printVmObj(defVmObj);
		}
	}
	
	// ============================================================================================================================== //
	// ----------------------------------------------------------- printVmObj() ----------------------------------------------------- //
	// ============================================================================================================================== //
	public static void printVmObj (VM vmObj){
		System.out.println(" VM\t resourceURI  \t\t\t= "+vmObj.getResourceURI());												// resourceURI
		System.out.println("\t resourceName  \t\t\t= "+vmObj.getResourceName());												// resourceName
		System.out.println("\t vmType \t\t\t= "+vmObj.getVmType());																// vmType
		System.out.println("\t vmId  \t\t\t\t= "+vmObj.getVmId());																// vmId
		System.out.println("\t priorityClass  \t\t= "+vmObj.getPriorityClass());												// priority Class
		System.out.println("\t vmUuid  \t\t\t= "+vmObj.getUuid());																// vmUuid
		System.out.println("\t maxMem  \t\t\t= "+Long.toString(vmObj.getMaxMemory()/1024)+" Mb");								// maxMem
		System.out.println("\t currentMemory  \t\t= "+Long.toString(vmObj.getCurrentMemory()/1024)+" Mb");						// currentMemory
		System.out.println("\t vCpus  \t\t\t= "+vmObj.getvCpus());																// vCpus
		System.out.println("\t cpuArch  \t\t\t= "+vmObj.getCpuArch());															// cpuArch
		System.out.println("\t machineOs  \t\t\t= "+vmObj.getMachineOs());														// machineOs
		System.out.println("\t maxCpus  \t\t\t= "+vmObj.getMaxCpus());															// maxCpus
		System.out.println("\t cpuTime  \t\t\t= "+Long.toString(vmObj.getCpuTime()/1000000)+" Seconds");						// cpuTime
		
		System.out.println("\t NumberOfIntfs \t\t\t= "+vmObj.getNumberOfIntfs());
		if(vmObj.getIntfsObjArray() != null)
			for (Intfs intfsItr : vmObj.getIntfsObjArray())
				printIntfsObj(intfsItr);
	}
	
	// ============================================================================================================================== //
	// ---------------------------------------------------------- printIntfsObj() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static void printIntfsObj(Intfs intfsObj){
		System.out.println("\t I\t interfaceURI \t\t= "+intfsObj.getResourceURI());												// interfaceURI
		System.out.println("\t\t interfaceName \t\t= "+intfsObj.getResourceName());												// interfaceName
		System.out.println("\t\t macAddress \t\t= "+intfsObj.getMacAddress());														// macAddress
		System.out.println("\t\t interfaceType \t\t= "+intfsObj.getInterfaceType());												// interfaceType
		System.out.println("\t\t sourceNetwork \t\t= "+intfsObj.getSourceNetwork());												// sourceNetwork
		System.out.println("\t\t numPorts \t\t= "+intfsObj.getNumPorts());															// numPorts
		System.out.println("\t\t totalPorts \t\t= "+intfsObj.getTotalPorts());														// totalPorts
		System.out.println("\t\t ipAddress \t\t= "+intfsObj.getIpAddress());														// ipAddress
		System.out.println("\t\t virtualPortType \t= "+intfsObj.getVirtualPortType());												// virtualPortType
		System.out.println("\t\t model \t\t\t= "+intfsObj.getModelType());																// model
		
		System.out.println("\t\t numPorts \t\t= "+intfsObj.getNumPorts());
		if (intfsObj.getPortObjArray() != null) 
			for (Port portItr : intfsObj.getPortObjArray()){	
				System.out.println("\t\t P\t resourceId \t= "+ portItr.getResourceURI());										// resourceId
				System.out.println("\t\t\t portName  \t= "+ portItr.getResourceName());											// portName
				System.out.println("\t\t\t portBuffer \t= "+ portItr.getPortBuffer());											// portBuffer
				System.out.println("\t\t\t portNumber \t= "+ portItr.getPortNumber()+"\n");										// portNumber
			}
	}
	
	// ============================================================================================================================== //
	// --------------------------------------------------------- acceptedReqObj() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static void acceptedReqObj(AcceptedReq acceptedReqObj){
		System.out.println("\nRequest URI \t\t: "+acceptedReqObj.getReqUri());
		System.out.println("\tID \t\t: "+acceptedReqObj.getReqId());
		System.out.println("\tName \t\t: "+acceptedReqObj.getReqName());
		System.out.println("\tArrival \t: "+acceptedReqObj.getReqArrival());
		System.out.println("\tDuration\t: "+acceptedReqObj.getReqDuration());
		System.out.println("\tState\t\t: "+acceptedReqObj.getReqState());
		System.out.println("\tPriority\t: "+acceptedReqObj.getPriorityCls());
		System.out.print("\tReserved VMs\t: ");  for(String rsvdVmUri : acceptedReqObj.getRsvdVmResList()) System.out.print(rsvdVmUri+"\t"); System.out.println();
		System.out.print("\tReserved VSs\t: ");  for(String rsvdVmUri : acceptedReqObj.getRsvdVsResList()) System.out.print(rsvdVmUri+"\t"); System.out.println();
		System.out.print("\tReserved VLs\t: ");  for(String rsvdVmUri : acceptedReqObj.getRsvdVlResList()) System.out.print(rsvdVmUri+"\t"); System.out.println();
		System.out.print("\tAllocated VMs\t: "); for(String rsvdVmUri : acceptedReqObj.getAlocVmResList()) System.out.print(rsvdVmUri+"\t"); System.out.println();
		System.out.print("\tAllocated VSs\t: "); for(String rsvdVmUri : acceptedReqObj.getAlocVsResList()) System.out.print(rsvdVmUri+"\t"); System.out.println();
		System.out.print("\tAllocated VLs\t: "); for(String rsvdVmUri : acceptedReqObj.getAlocVlResList()) System.out.print(rsvdVmUri+"\t"); System.out.println("\n");
	}
	
	// ============================================================================================================================== //
	// ---------------------------------------------------------- printVSwObj() ----------------------------------------------------- //
	// ============================================================================================================================== //
	public static void printVSwObj(VSw vSwObj){
		System.out.println("\nvSwitch resourceURI\t: "+vSwObj.getResourceURI());
		System.out.println("\tresourceName \t: "+vSwObj.getResourceName());
		System.out.println("\tbw \t\t: "+vSwObj.getBw());
		System.out.println("\tVMM \t\t: "+vSwObj.getVMM());
		System.out.println("\tn_cores \t: "+vSwObj.getN_cores());
		System.out.println("\tloss_rate \t: "+vSwObj.getLoss_rate());
		System.out.println("\tstorage \t: "+vSwObj.getStorage());
		System.out.println("\tmemory \t\t: "+vSwObj.getMemory());
		System.out.println("\tlocation \t: "+vSwObj.getLocation());
		System.out.println("\tdelay \t\t: "+vSwObj.getDelay());
		System.out.println("\tcpu \t\t: "+vSwObj.getCpu());
		System.out.println("\tTotal connected virtual resources = "+vSwObj.getRsvdVsResList().size());
		for (String connectedRes : vSwObj.getRsvdVsResList())
			System.out.println("\t\t\t"+connectedRes);
	}
	
	// ============================================================================================================================== //
	// -------------------------------------------------------- printDatapathObj() -------------------------------------------------- //
	// ============================================================================================================================== //
	public static void printDatapathObj(Datapath datapathObj){
		System.out.println("\n Switch Information");
		System.out.println("\t resourceURL \t\t= "+datapathObj.getResourceURI());
		System.out.println("\t resourceName \t\t= "+datapathObj.getResourceName());
		System.out.println("\t resLocation \t\t= "+datapathObj.getResLocation());
		System.out.println("\t resModel \t\t= "+datapathObj.getResModel());
		System.out.println("\t switchNum \t\t= "+datapathObj.getSwitchNum());
		// ------------------ Compute Component Datatype Properties -------------------
		System.out.println("\t managementCpu \t\t= "+datapathObj.getManagementCpu());
		System.out.println("\t managementMemory \t= "+datapathObj.getManagementMemory());
		// ----------------- Switching Component Datatype Properties ------------------
		System.out.println("\t dataPlaneCpu \t\t= "+datapathObj.getDataPlaneCpu());
		System.out.println("\t dataPlaneMemory \t= "+datapathObj.getDataPlaneMemory());
		System.out.println("\t flow_table_size \t= "+datapathObj.getFlow_table_size());
		System.out.println("\t latency \t\t= "+datapathObj.getLatency());
		System.out.println("\t maxThroughput \t\t= "+datapathObj.getMaxThroughput());
		System.out.println("\t switchingSpeed \t= "+datapathObj.getSwitchingSpeed());
		// -------------------- Attributes not published in PR_Pool -------------------
		System.out.println("\t dpid \t\t\t= "+datapathObj.getDpid());

		// Print Flow Mod Usage Information
		if(datapathObj.getFlowmodUsageMap() != null){
			System.out.println("\n Flow Mod Usage Information");
			for (String flowspaceId : datapathObj.getFlowmodUsageMap().keySet()) 
				System.out.println("\t flowspace = "+flowspaceId + " \t rules =" + datapathObj.getFlowmodUsageMap().get(flowspaceId));
		}
		// Print Interface information
		if(datapathObj.getIntfsObjArray() != null){
			Intfs[] intfsObjArray = datapathObj.getIntfsObjArray();
			for(Intfs intfsObj : intfsObjArray)
				printIntfsObj(intfsObj);
			
		}
	}
	
	// ============================================================================================================================== //
	// ----------------------------------------------------------- printLinkObj() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static void printLinkObj(Link linkObj){
		System.out.println("\t resourceURI \t= "+linkObj.getResourceURI());
		System.out.println("\t resourceName \t= "+linkObj.getResourceName());
		System.out.println("\t srcDpid \t= "+linkObj.getSrcIntfsMac());
		System.out.println("\t srcIntfsURI \t= "+linkObj.getSrcIntfsResourceURI());
		System.out.println("\t srcPortURI \t= "+linkObj.getSrcPortResourceURI());
		System.out.println("\t destDpid \t= "+linkObj.getDestIntfsMac());
		System.out.println("\t destIntfsURI \t= "+linkObj.getDestIntfsResourceURI());
		System.out.println("\t destPortURI \t= "+linkObj.getDestPortResourceURI()+"\n");
	}
	
	// ============================================================================================================================== //
	// ------------------------------------------------------ prettyPrintJSONAsString() --------------------------------------------- //
	// ============================================================================================================================== //
	public static void prettyPrintJSONAsString(String jsonString) {

	    int tabCount = 0;
	    StringBuffer prettyPrintJson = new StringBuffer();
	    String lineSeparator = "\r\n";
	    String tab = "  ";
	    boolean ignoreNext = false;
	    boolean inQuote = false;

	    char character;

	    /* Loop through each character to style the output */
	    for (int i = 0; i < jsonString.length(); i++) {

	        character = jsonString.charAt(i);

	        if (inQuote) {

	            if (ignoreNext) {
	                ignoreNext = false;
	            } else if (character == '"') {
	                inQuote = !inQuote;
	            }
	            prettyPrintJson.append(character);
	        } else {

	            if (ignoreNext ? ignoreNext = !ignoreNext : ignoreNext);

	            switch (character) {

	            case '[':
	                ++tabCount;
	                prettyPrintJson.append(character);
	                prettyPrintJson.append(lineSeparator);
	                printIndent(tabCount, prettyPrintJson, tab);
	                break;

	            case ']':
	                --tabCount;
	                prettyPrintJson.append(lineSeparator);
	                printIndent(tabCount, prettyPrintJson, tab);
	                prettyPrintJson.append(character);
	                break;

	            case '{':
	                ++tabCount;
	                prettyPrintJson.append(character);
	                prettyPrintJson.append(lineSeparator);
	                printIndent(tabCount, prettyPrintJson, tab);
	                break;

	            case '}':
	                --tabCount;
	                prettyPrintJson.append(lineSeparator);
	                printIndent(tabCount, prettyPrintJson, tab);
	                prettyPrintJson.append(character);
	                break;

	            case '"':
	                inQuote = !inQuote;
	                prettyPrintJson.append(character);
	                break;

	            case ',':
	                prettyPrintJson.append(character);
	                prettyPrintJson.append(lineSeparator);
	                printIndent(tabCount, prettyPrintJson, tab);
	                break;

	            case ':':
	                prettyPrintJson.append(character + " ");
	                break;

	            case '\\':
	                prettyPrintJson.append(character);
	                ignoreNext = true;
	                break;

	            default:
	                prettyPrintJson.append(character);
	                break;
	            }
	        }
	    }

	    System.out.println(prettyPrintJson.toString());
	}

	private static void printIndent(int count, StringBuffer stringBuffer, String indent) {
	    for (int i = 0; i < count; i++) {
	        stringBuffer.append(indent);
	    }
	}
	
	
}
