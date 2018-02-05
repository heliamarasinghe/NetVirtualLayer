package com.imaginelab.sdn_icf.discover;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.imaginelab.sdn_icf.containers.*;

import static com.imaginelab.sdn_icf.main.Constants.*;

public class PrPoolAccess {
	static OntModel phyResourcePoolModel;


	///===========================================================================================================
	///------------------------------------------- updatePrPool() -----------------------------------------------
	///===========================================================================================================
	public static void updatePrPool(Datapath[] datapathObjArray, VmHost[] vmHostObjArray, ArrayList<Link> linkObjList){
		boolean switchesAdded = false;
		boolean vmhostsAdded = false;
		phyResourcePoolModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);

		try{
			phyResourcePoolModel.read(PRPOOL, "RDF/XML");
			if (PRPOOL_UPDATE_DBG)System.out.println("PrPoolAccess.updatePrPool(): OWL file detected");
			if(UPDATE_OWL_IT) {
				System.out.println("\n\nAdding physical Host information into Ontology");
				vmhostsAdded = storeHosts(vmHostObjArray);													// Update ontology
			}
			if(UPDATE_OWL_NET) {
				System.out.println("\nAdding Switch and Link information into Ontology");
				switchesAdded = storeDatapaths(datapathObjArray);
				storeLinks(linkObjList);
			}
			if(switchesAdded && vmhostsAdded){
				System.out.println("\nAdding Interface connectivity information into Ontology");
				storeConnectivityData(datapathObjArray, vmHostObjArray);
			}

			PrintStream prntStrm= new PrintStream(PRPOOL); //c:/data.rdf");
			phyResourcePoolModel.write(prntStrm, "RDF/XML");
			System.out.println("\nDONE Host, Switch and Link information added\n");

		}catch (FileNotFoundException no_file) {
			System.err.println("Unable to access ontology.");
			System.out.println("Make sure you have \""+PRPOOL+"\" and restart the program.");
			no_file.printStackTrace();
			System.exit(0);
		}catch (JenaException je) {
			System.err.println("JenaException has been generated at updatePrPool");
			je.printStackTrace();
		}catch (Exception e) {
			System.err.println("General Exception has been generated at updatePrPool");
			e.printStackTrace();
		}
	}





///=======================================================================================================================================
///------------------------------------------------------------	storeDatapaths() ---------------------------------------------------------
// SUMMARY: 	Create switch object from Switch class in the Ontology by adding all DatatypeProperty and ObjectProperty into it.
// DESCRIPTION: A Switch has set of properties including switchName, switchURI, managementCPU, managementMemory switchingSpeed,  
//				maxThroughput, ipAddress, macAddress, portNames, portNumbers etc.
// 				These are devided into;
//					device properties   				--> resourceName, resourceURI
//						compute component resources  	-->	managementCPU, managementMemory, ...
//						switching component resources	--> switchingSpeed, maxThroughput, ...
//					interface properties				-->	ipAddress, macAddress
//					port properties						-->	portName, portNumber
//				Device properties are defined as DatatypeProperties of the switch object itself. 
//				Compute and switching resources are defined as DatatypeProperties of computeComponent and switchingComponent class objectss
//					and related to switch by hasComponent ObjectProperty.
//				Interface properties are defined as DatatypeProperties of Interface class object and related by hasInterface ObjectProperty.
//				Port properties are defined as DatatypeProperties of Port class object and related by hasPort ObjectProperty.
///========================================================================================================================================
	public static boolean storeDatapaths(Datapath[] datapathObjArray) throws FileNotFoundException{

		for(int dpItr = 0; dpItr<datapathObjArray.length;dpItr++){
			// SWITCH
			String switchResourceName = datapathObjArray[dpItr].getResourceName();
			String switchResourceURI = datapathObjArray[dpItr].getResourceURI();
			OntClass switchResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"Switch"); 					//System.out.println("getOntClass --> "+NS+"Node");
			Individual switchIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS + switchResourceURI, switchResourceClass );
			// device properties   --> resourceName, resourceURI
			DatatypeProperty dpResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpResLocation = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resLocation" );
			DatatypeProperty dpResModel = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resModel" );
			DatatypeProperty dpResIdNum = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resIdNum" );
			switchIndividual.addProperty( dpResourceURI, phyResourcePoolModel.createTypedLiteral(switchResourceURI))
			.addProperty( dpResourceName, phyResourcePoolModel.createTypedLiteral(switchResourceName))
			.addProperty( dpResLocation, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getResLocation()))
			.addProperty( dpResModel, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getResModel()))
			.addProperty( dpResIdNum, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getSwitchNum()));

			//	switchIndividual	----hasComponent---->	computeComponentIndividual
			//						----hasComponent---->	switchingComponentIndividual
			//						----hasInterface---->	interfaceIndividual
			//  interfaceIndividual	-------hasPort------>	portIndividual
			ObjectProperty hasComponent = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasComponent");

			// SW-COMPUTE
			// compute component resources   -->	managementCPU, managementMemory
			String computeResourceName = switchResourceName +"_compute";
			String computeResourceURI = switchResourceURI+"_CMP";
			OntClass computeResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"ComputeComponent");
			Individual computeResourceIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS + computeResourceURI, computeResourceClass);



			DatatypeProperty dpComputeResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpComputeResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpCpuFrequency = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "cpuFrequency" );  	// managementCpu
			DatatypeProperty dpTotalMemory = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "totalMemory" );		// managementMemory
			computeResourceIndividual	.addProperty( dpComputeResourceURI, phyResourcePoolModel.createTypedLiteral(computeResourceURI))
			.addProperty( dpComputeResourceName, phyResourcePoolModel.createTypedLiteral(computeResourceName))
			.addProperty( dpCpuFrequency, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getManagementCpu()) )
			.addProperty( dpTotalMemory, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getManagementMemory()));
			switchIndividual.addProperty(hasComponent, computeResourceIndividual);

			// SW-SWITCHING
			// switching resources --> switchingSpeed, maxThroughput, usedPorts, dataPlaneMemory, flowTableSize...
			String switchingResourceName =switchResourceName +"_switching";
			String switchingResourceURI = switchResourceURI+"_SWNG";
			OntClass switchingResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"SwitchingComponent");
			Individual switchingResourceIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+switchingResourceURI, switchingResourceClass);

			DatatypeProperty dpSwitchingResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpSwitchingResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			
			DatatypeProperty switchingSpeed = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "switchingSpeed" );
			DatatypeProperty maxThroughput = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "maxThroughput" );
			DatatypeProperty dataPlaneMemory = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "dataPlaneMemory" );
			DatatypeProperty dataPlaneCpu = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "dataPlaneCpu" );
			DatatypeProperty flowTableSize = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "flowTableSize" );

			switchingResourceIndividual.addProperty( dpSwitchingResourceURI, phyResourcePoolModel.createTypedLiteral(switchingResourceURI))
			.addProperty( dpSwitchingResourceName, phyResourcePoolModel.createTypedLiteral(switchingResourceName))
			
			.addProperty( switchingSpeed, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getSwitchingSpeed()))
			.addProperty( maxThroughput, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getMaxThroughput()))
			.addProperty( dataPlaneMemory, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getDataPlaneMemory()))
			.addProperty( dataPlaneCpu, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getDataPlaneCpu()))
			.addProperty( flowTableSize, phyResourcePoolModel.createTypedLiteral(datapathObjArray[dpItr].getFlow_table_size()));
			switchIndividual.addProperty(hasComponent, switchingResourceIndividual);

			// interface properties-->	ipAddress, macAddress, interfaceType
			if(datapathObjArray[dpItr].getIntfsObjArray() != null){
				ObjectProperty hasInterface = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasInterface");
				// interfaceIndividual has one or more port individuals
				for(Intfs intfsObj : datapathObjArray[dpItr].getIntfsObjArray()){
					Individual interfaceIndividual =  storeInterfaces (intfsObj);
					switchIndividual.addProperty(hasInterface, interfaceIndividual);
				}
			}
			if(PRPOOL_UPDATE_DBG) System.out.println("Switch Individual added to "+PRPOOL);
			// TODO flowmodusage
		}
		return true;
	}

///=========================================================================================================================================
///--------------------------------------------------------------- storeLinks() ------------------------------------------------------------
///=========================================================================================================================================

	public static void storeLinks(ArrayList<Link> linkObjList) throws FileNotFoundException{
		for(int linkItr = 0; linkItr<linkObjList.size(); linkItr++){

			String linkResourceName = linkObjList.get(linkItr).getResourceName();
			String linkResourceURI = linkObjList.get(linkItr).getResourceURI();
			OntClass linkResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"Link"); 					//System.out.println("getOntClass --> "+NS+"Node");
			Individual linkIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS + linkResourceURI, linkResourceClass);
			if(PRPOOL_UPDATE_DBG)System.out.println("Defining DatatypeProperties");

			DatatypeProperty dpResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpLatency = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "latency" );
			DatatypeProperty dpLinkType = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "linkType" );
			DatatypeProperty dpBandwidth = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "bandwidth" );
			DatatypeProperty dpLossRate = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "lossRate" );
			DatatypeProperty dpSrcIntfsResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "srcIntfs" );
			DatatypeProperty dpDestIntfsResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "destIntfs" );
			DatatypeProperty dpSrcMac = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "srcMac" );
			DatatypeProperty dpDestMac = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "destMac" );

			if(PRPOOL_UPDATE_DBG)System.out.println("Adding Datatype Properties to link individual");
			linkIndividual.addProperty( dpResourceURI, phyResourcePoolModel.createTypedLiteral(linkResourceURI))
			.addProperty( dpResourceName, phyResourcePoolModel.createTypedLiteral(linkResourceName))
			.addProperty( dpLatency, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getLatency()) )				
			.addProperty( dpLinkType, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getLinkType()))			
			.addProperty( dpBandwidth, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getBandwidth()))					
			.addProperty( dpLossRate, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getLossRate()))
			.addProperty( dpSrcIntfsResourceURI, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getSrcIntfsResourceURI()))
			.addProperty( dpDestIntfsResourceURI, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getDestIntfsResourceURI()))
			.addProperty( dpSrcMac, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getSrcIntfsMac()))	
			.addProperty( dpDestMac, phyResourcePoolModel.createTypedLiteral(linkObjList.get(linkItr).getDestIntfsMac()));		

			ObjectProperty hasSrcPort = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasSrcPort");
			Individual srcPortIndividual = phyResourcePoolModel.getIndividual(PRPOOL_NS + linkObjList.get(linkItr).getSrcPortResourceURI());
			if(PRPOOL_UPDATE_DBG) System.out.println("Adding Object Property: hasSrcPort--> "+hasSrcPort+"\n\t\t     srcPort--> "+srcPortIndividual);
			if(srcPortIndividual != null)
				linkIndividual.addProperty(hasSrcPort, srcPortIndividual);
			else System.err.println("PrPoolAccess.storeLinks(): srcPort mismatch error. \n "
						+ "\t This can be due to;\n"
						+ "\t\t1. Topology control not enabled in flowvisor. check \"list-links\" in flowvisor\n"
						+ "\t\t   If you see \"fakeLink=true\", enable topology control by \"fvctl -f /dev/null set-config --enable-topo-ctrl\" and restart flowvisor"
						+ "\t\t2. Assumption in defining OVS-Host links: OVS is always connected to Host over port 1. See BuildPrTopology.buildConnectivity()\n");
			
			ObjectProperty hasDestPort = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasDestPort");
			Individual destPortIndividual = phyResourcePoolModel.getIndividual(PRPOOL_NS + linkObjList.get(linkItr).getDestPortResourceURI());
			if(PRPOOL_UPDATE_DBG) System.out.println("Adding Object Property: hasDestPort--> "+hasDestPort+"\n\t\t     destPort--> "+destPortIndividual);
			if(destPortIndividual != null)
				linkIndividual.addProperty(hasDestPort, destPortIndividual);
			else System.err.println("PrPoolAccess.storeLinks(): destPort mismatch error. \n"
						+ "For OVS-Host links, this can be due assumption that OVS the is always connected to Host over port 1. See BuildPrTopology.buildConnectivity()");
			
			if(PRPOOL_UPDATE_DBG) System.out.println("Link individual added to "+PRPOOL);

		}	
	}

///============================================================================================================================================
///------------------------------------------------------------	storeHosts() ------------------------------------------------------------------
// SUMMARY: 	Create host object from Host class in the Ontology by adding all DatatypeProperty and ObjectProperty into it.
// DESCRIPTION: A Host has set of properties including hostName, hostURI, cpuFrequency, totalMemory, totalStorage, availableStorage
//					usedPorts, ipAddress, macAddress, portNames, portNumbers etc.
//	 				These are devided into;
//						node properties   	--> resourceName, resourceURI, resLocation, resModel, nodeClass
//						compute resources   -->	cpuFrequency, totalMemory, ...
//						storage resources   -->	totalStorage, availableStorage, ...
//						switching resources --> usedPorts
//						interface properties-->	ipAddress, macAddress
//						port properties		-->	portName, portNumber
//					Device properties are defined as DatatypeProperties of the host object itself. 
//					Compute and switching resources are defined as DatatypeProperties of computeComponent and switchingComponent class objectss
//						and related to host by hasComponent ObjectProperty.
//					Interface properties are defined as DatatypeProperties of Interface class object and related by hasInterface ObjectProperty.
//					Port properties are defined as DatatypeProperties of Port class object and related by hasPort ObjectProperty.
///=============================================================================================================================================
	public static boolean storeHosts(VmHost[] vmHostObjArray) throws FileNotFoundException{

		for(int hostItr = 0; hostItr<vmHostObjArray.length; hostItr++){
			String hostResourceName = vmHostObjArray[hostItr].getResourceName();	
			String hostResourceURI = vmHostObjArray[hostItr].getResourceURI();	
			OntClass hostResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"Host"); 					//System.out.println("getOntClass --> "+NS+"Node");
			Individual hostIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+hostResourceURI, hostResourceClass );

			if(PRPOOL_UPDATE_DBG) System.out.println("Create Individual: hostResourceURI-->"+hostResourceURI+"\n\t\t  Node_Cls-->"+hostResourceClass);

			// device properties   --> hostName, hostURI
			if(PRPOOL_UPDATE_DBG) System.out.println("Adding device properties   --> hostName, hostURI");
			DatatypeProperty dpResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpResLocation = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resLocation" );
			DatatypeProperty dpResModel = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resModel" );
			DatatypeProperty dpNodeClass = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "nodeClass" );
			DatatypeProperty dpResIdNum = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resIdNum" );
			hostIndividual.addProperty( dpResourceURI, phyResourcePoolModel.createTypedLiteral(hostResourceURI))
			.addProperty( dpResourceName, phyResourcePoolModel.createTypedLiteral(hostResourceName))
			.addProperty( dpResLocation, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getResLocation()))
			.addProperty( dpResModel, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getResModel()))
			.addProperty( dpNodeClass, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getNodeClass()))
			.addProperty( dpResIdNum, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getVmHostNum()));

			ObjectProperty hasComponent = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasComponent");
			//	hostIndividual		----hasComponent---->	computeComponentIndividual
			//						----hasComponent---->	storageComponentIndividual
			//						----hasComponent---->	switchingComponentIndividual
			//
			//						----hasInterface---->	interfaceIndividual
			//  											interfaceIndividual	-------hasPort------>	portIndividual
			//						------hasVNode------>	vNodeIndividual			


			// compute resources   -->	cpuFrequency, totalMemory, ...
			String computeComponentName = hostResourceName +"_compute";
			String computeComponentURI = hostResourceURI +"_CMP";
			OntClass computeComponentClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"ComputeComponent");
			Individual computeComponentIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+computeComponentURI, computeComponentClass);

			DatatypeProperty dpComputeResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpComputeResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpHyperVer = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "hyperVer" );  	
			DatatypeProperty dpHyperType = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "hyperType" );		
			DatatypeProperty dpTotalMemory = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "totalMemory" );
			DatatypeProperty dpAvailableMemory = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "availableMemory" );
			DatatypeProperty dpCpuSockets = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "cpuSockets" );
			DatatypeProperty dpCoresPerSocket = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "coresPerSocket" );
			DatatypeProperty dpThreadsPerCore = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "threadsPerCore" );
			DatatypeProperty dpCpuFrequency = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "cpuFrequency" );
			DatatypeProperty dpAvailableCores = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "availableCores" );

			computeComponentIndividual	.addProperty( dpComputeResourceURI, phyResourcePoolModel.createTypedLiteral(computeComponentURI))
			.addProperty( dpComputeResourceName, phyResourcePoolModel.createTypedLiteral(computeComponentName))
			.addProperty( dpHyperVer, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getHyperVer()))
			.addProperty( dpHyperType, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getHyperType()))
			.addProperty( dpTotalMemory, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getTotalMemory()))
			.addProperty( dpAvailableMemory, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getAvailableMemory()))
			.addProperty( dpCpuSockets, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getCpuSockets()))
			.addProperty( dpCoresPerSocket, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getCoresPerSocket()))
			.addProperty( dpThreadsPerCore, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getThreadsPerCore()))
			.addProperty( dpCpuFrequency, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getCpuFrequency()))
			.addProperty( dpAvailableCores, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getAvailableCores()));
			hostIndividual.addProperty(hasComponent, computeComponentIndividual);

			// storage resources --> totalStorage, availableStorage
			String storageComponentName = hostResourceName +"_storage";
			String storageComponentURI = hostResourceURI+"_STO";
			OntClass storageComponentClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"StorageComponent");
			Individual storageComponentIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+storageComponentURI, storageComponentClass);

			DatatypeProperty dpStorageResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpStorageResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpTotalStorage = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "totalStorage" );
			DatatypeProperty dpAvailableStorage = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "availableStorage" );
			storageComponentIndividual	.addProperty( dpStorageResourceURI, phyResourcePoolModel.createTypedLiteral(storageComponentURI))
			.addProperty( dpStorageResourceName, phyResourcePoolModel.createTypedLiteral(storageComponentName))
			.addProperty( dpTotalStorage, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getTotalStorage()))
			.addProperty( dpAvailableStorage, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getAvailableStorage()));
			hostIndividual.addProperty(hasComponent, storageComponentIndividual);

			// switching resources --> usedPorts, numberOfOvs
			String switchingComponentName = hostResourceName +"_switching";
			String switchingComponentURI = hostResourceURI +"_SWNG";
			OntClass switchingComponentClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"SwitchingComponent");
			Individual switchingComponentIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+switchingComponentURI, switchingComponentClass);

			DatatypeProperty dpSwitchingeResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
			DatatypeProperty dpSwitchingResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
			DatatypeProperty dpUsedPorts = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "usedPorts" );
			DatatypeProperty dpNumberOfOvs = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "numberOfOvs" );

			switchingComponentIndividual.addProperty( dpSwitchingeResourceURI, phyResourcePoolModel.createTypedLiteral(switchingComponentURI))
			.addProperty( dpSwitchingResourceName, phyResourcePoolModel.createTypedLiteral(switchingComponentName))
			.addProperty( dpUsedPorts, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getNumOfInterfaces()))
			.addProperty( dpNumberOfOvs, phyResourcePoolModel.createTypedLiteral(vmHostObjArray[hostItr].getNumOfOvs()));
			hostIndividual.addProperty(hasComponent, switchingComponentIndividual);

			// interface properties-->	ipAddress, macAddress, interfaceType
			if(vmHostObjArray[hostItr].getIntfsObjArray() != null){
				ObjectProperty hasInterface = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasInterface");
				for (Intfs intfs : vmHostObjArray[hostItr].getIntfsObjArray()) {
					Individual interfaceIndividual =  storeInterfaces (intfs);
					hostIndividual.addProperty(hasInterface, interfaceIndividual);
				}
			}

			//			------hasVNode------>	vNodeIndividual
			ObjectProperty hasVNode = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasVNode");
			if(vmHostObjArray[hostItr].getActiveVmObjArray() != null)
				for (VM activeVmObj : vmHostObjArray[hostItr].getActiveVmObjArray()){
					Individual vNodeIndividual = storeVNode(activeVmObj, "active");
					hostIndividual.addProperty(hasVNode, vNodeIndividual);
				}
			
			if(vmHostObjArray[hostItr].getDefinedVmObjArray() != null)
				for (VM definedVmObj : vmHostObjArray[hostItr].getDefinedVmObjArray()){
					Individual vNodeIndividual = storeVNode(definedVmObj, "defined");
					hostIndividual.addProperty(hasVNode, vNodeIndividual);
				}
			

			if(PRPOOL_UPDATE_DBG) System.out.println("Host Individual added to "+PRPOOL);
		}		
		return true;
	}

///=========================================================================================================================================
///--------------------------------------------------------------- storeVNode() ------------------------------------------------------------
///=========================================================================================================================================
	public static Individual storeVNode(VM vmObj, String status) throws FileNotFoundException{

		String vmResourceName = vmObj.getResourceName();
		String vmResourceURI = vmObj.getResourceURI();
		OntClass vmResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"VNode");
		Individual vmIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+vmResourceURI, vmResourceClass);

		DatatypeProperty dpVmResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );		// resourceURI
		DatatypeProperty dpVmResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );	// resourceName
		DatatypeProperty dpResModel = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resModel" );
		DatatypeProperty dpResIdNum = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resIdNum" );
		DatatypeProperty dpResLocation = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resLocation" );
		DatatypeProperty dpResUUID = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resUUID" );					// uuid
		vmIndividual		.addProperty( dpVmResourceURI, phyResourcePoolModel.createTypedLiteral(vmResourceURI))
		.addProperty( dpVmResourceName, phyResourcePoolModel.createTypedLiteral(vmResourceName))
		.addProperty( dpResModel, phyResourcePoolModel.createTypedLiteral(vmObj.getVmType()))
		.addProperty( dpResIdNum, phyResourcePoolModel.createTypedLiteral(vmObj.getVmId()))
		.addProperty( dpResLocation, phyResourcePoolModel.createTypedLiteral(vmObj.getResLocation()))
		.addProperty( dpResUUID, phyResourcePoolModel.createTypedLiteral(vmObj.getUuid()));

		//---------------------------------------------------------------------------------------------------------------//
		//	vmIndividual		----hasComponent---->	computeComponentIndividual
		//						----hasComponent---->	storageComponentIndividual
		//						----hasComponent---->	switchingComponentIndividual
		//						----hasInterface---->	interfaceIndividual
		//  											interfaceIndividual	-------hasPort------>	portIndividual		
		//						-----hasStatues----->	active/defined
		//---------------------------------------------------------------------------------------------------------------//
		
		//	vmIndividual		----hasComponent---->	computeComponentIndividual
		ObjectProperty hasComponent = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasComponent");
		String vmComputeComponentName = vmResourceName +"_compute";
		String vmComputeComponentURI = vmResourceURI +"_CMP";
		OntClass vmComputeComponentClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"ComputeComponent");
		Individual vmComputeComponentIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+vmComputeComponentURI, vmComputeComponentClass);
		DatatypeProperty dpVmComputeResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
		DatatypeProperty dpVmComputeResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
//		DatatypeProperty dpVmType = phyResourcePoolModel.createDatatypeProperty( NS + "vmType" );					// vmType
//		DatatypeProperty dpVmId = phyResourcePoolModel.createDatatypeProperty( NS + "vmId" );						// vmId
		//DatatypeProperty dpVmUuid = phyResourcePoolModel.createDatatypeProperty( NS + "uuid" );					// uuid
		DatatypeProperty dpVmMaxMemory = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "maxMemory" );			// maxMemory
		DatatypeProperty dpVmCurrentMemory = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "currentMemory" );	// currentMemory
		DatatypeProperty dpVmVCpus = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "vCpus" );					// vCpus
		DatatypeProperty dpVmVCpuArch = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "vCpuArch" );			// cpuArch
		DatatypeProperty dpVmMachineOs = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "machineOs" );			// machineOs
		DatatypeProperty dpVmMaxCpu = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "maxCpu" );				// maxCpus
		DatatypeProperty dpVmCpuTime = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "cpuTime" );				// cpuTime
		vmComputeComponentIndividual.addProperty( dpVmComputeResourceURI, phyResourcePoolModel.createTypedLiteral(vmComputeComponentURI))
		.addProperty( dpVmComputeResourceName, phyResourcePoolModel.createTypedLiteral(vmComputeComponentName))
//		.addProperty( dpVmType, phyResourcePoolModel.createTypedLiteral(activeVmObj.getVmType()))
//		.addProperty( dpVmId, phyResourcePoolModel.createTypedLiteral(activeVmObj.getVmId()))
		
		.addProperty( dpVmMaxMemory, phyResourcePoolModel.createTypedLiteral(vmObj.getMaxMemory()))
		.addProperty( dpVmCurrentMemory, phyResourcePoolModel.createTypedLiteral(vmObj.getCurrentMemory()))
		.addProperty( dpVmVCpus, phyResourcePoolModel.createTypedLiteral(vmObj.getvCpus()))
		.addProperty( dpVmVCpuArch, phyResourcePoolModel.createTypedLiteral(vmObj.getCpuArch()))
		.addProperty( dpVmMachineOs, phyResourcePoolModel.createTypedLiteral(vmObj.getMachineOs()))
		.addProperty( dpVmMaxCpu, phyResourcePoolModel.createTypedLiteral(vmObj.getMaxCpus()))
		.addProperty( dpVmCpuTime, phyResourcePoolModel.createTypedLiteral(vmObj.getCpuTime()));
		vmIndividual.addProperty(hasComponent, vmComputeComponentIndividual);
		
		// vmIndividual			----hasComponent----> vmStorageComponentIndividual
		String vmStorageComponentName = vmResourceName +"_storage";
		String vmStorageComponentURI = vmResourceURI+"_STO";
		OntClass vmStorageComponentClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"StorageComponent");
		Individual vmStorageComponentIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+vmStorageComponentURI, vmStorageComponentClass);
		DatatypeProperty dpVmStorageResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
		DatatypeProperty dpVmStorageResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
		DatatypeProperty dpVmTotalStorage = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "totalStorage" );
		vmStorageComponentIndividual	.addProperty( dpVmStorageResourceURI, phyResourcePoolModel.createTypedLiteral(vmStorageComponentURI))
		.addProperty( dpVmStorageResourceName, phyResourcePoolModel.createTypedLiteral(vmStorageComponentName))
		.addProperty( dpVmTotalStorage, phyResourcePoolModel.createTypedLiteral(vmObj.getTotalStorage()));
		vmIndividual.addProperty(hasComponent, vmStorageComponentIndividual);

		//	vmIndividual		----hasComponent---->	switchingComponentIndividual
		String vmSwitchingComponentName = vmResourceName +"_switching";
		String vmSwitchingComponentURI = vmResourceURI +"_SWNG";
		OntClass vmSwitchingComponentClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"SwitchingComponent");
		Individual vmSwitchingComponentIndividual = phyResourcePoolModel.createIndividual(PRPOOL_NS+vmSwitchingComponentURI, vmSwitchingComponentClass);
		DatatypeProperty dpVmSwitchingeResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
		DatatypeProperty dpVmSwitchingResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
		DatatypeProperty dpVmNumberOfIntfs = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "numberOfIntfs" );	// numberOfIntfs
		vmSwitchingComponentIndividual.addProperty( dpVmSwitchingeResourceURI, phyResourcePoolModel.createTypedLiteral(vmSwitchingComponentURI))
		.addProperty( dpVmSwitchingResourceName, phyResourcePoolModel.createTypedLiteral(vmSwitchingComponentName))
		.addProperty( dpVmNumberOfIntfs, phyResourcePoolModel.createTypedLiteral(vmObj.getNumberOfIntfs()));
		vmIndividual.addProperty(hasComponent, vmSwitchingComponentIndividual);

		// vmIndividual			----hasInterface---->	interfaceIndividual
		if(vmObj.getIntfsObjArray() != null){
			ObjectProperty hasInterface = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasInterface");
			for (Intfs intfs : vmObj.getIntfsObjArray()) {
				Individual interfaceIndividual =  storeInterfaces (intfs);
				vmIndividual.addProperty(hasInterface, interfaceIndividual);
			}
		}
		
		// vmIndividual			-----hasStatues----->	active/defined
		ObjectProperty hasStatus = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasStatus");
		Individual nodeStatusIndividual = phyResourcePoolModel.getIndividual(PRPOOL_NS+status);
		vmIndividual.addProperty(hasStatus, nodeStatusIndividual);

		return vmIndividual;
	}

///===================================================================================================================================
///--------------------------------------------------------	storeInterfaces() --------------------------------------------------------
///===================================================================================================================================

	public static Individual storeInterfaces (Intfs intfsObj) throws FileNotFoundException{
		if(PRPOOL_UPDATE_DBG) System.out.println(intfsObj.getResourceName() + ", " + intfsObj.getMacAddress());

		String interfaceResourceURI = PRPOOL_NS +  intfsObj.getResourceURI();
		OntClass interfaceResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"Interface");
		Individual interfaceIndividual = phyResourcePoolModel.createIndividual(interfaceResourceURI, interfaceResourceClass);

		DatatypeProperty dpInterfaceResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
		DatatypeProperty dpInterfaceResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
		DatatypeProperty dpMacAddress = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "macAddress" );
		DatatypeProperty dpInterfaceType = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "interfaceType" );
		DatatypeProperty dpSourceNetwork = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "sourceNetwork" );
		DatatypeProperty dpNumPorts = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "numPorts" );
		DatatypeProperty dpTotalPorts = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "totalPorts" );
		DatatypeProperty dpIpAddress = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "ipAddress" );
		DatatypeProperty dpVirtualPortType = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "virtualPortType" );
		DatatypeProperty dpModel = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "model" );

		interfaceIndividual	.addProperty( dpInterfaceResourceURI, phyResourcePoolModel.createTypedLiteral(intfsObj.getResourceURI()))
		.addProperty( dpInterfaceResourceName, phyResourcePoolModel.createTypedLiteral(intfsObj.getResourceName()))
		.addProperty( dpMacAddress, phyResourcePoolModel.createTypedLiteral(intfsObj.getMacAddress()))
		.addProperty( dpInterfaceType, phyResourcePoolModel.createTypedLiteral(intfsObj.getInterfaceType()))
		.addProperty( dpSourceNetwork, phyResourcePoolModel.createTypedLiteral(intfsObj.getSourceNetwork()))
		.addProperty( dpNumPorts, phyResourcePoolModel.createTypedLiteral(intfsObj.getNumPorts()))
		.addProperty( dpTotalPorts, phyResourcePoolModel.createTypedLiteral(intfsObj.getTotalPorts()))
		.addProperty( dpIpAddress, phyResourcePoolModel.createTypedLiteral(intfsObj.getIpAddress()))
		.addProperty( dpVirtualPortType, phyResourcePoolModel.createTypedLiteral(intfsObj.getVirtualPortType()))
		.addProperty( dpModel, phyResourcePoolModel.createTypedLiteral(intfsObj.getModelType()));

		if(intfsObj.getPortObjArray() != null){
			ObjectProperty hasPort = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"hasPort");
			if(PRPOOL_UPDATE_DBG) System.out.println("getObjectProperty: hasPort-->"+hasPort);
			// For hosts, each interface creates an abstract port class instance. But for switches, each switch has one interface in most cases but each interface has multiple port indivisuals.
			for(Port portObj : intfsObj.getPortObjArray()){
				Individual portIndividual = storePorts (portObj);
				interfaceIndividual.addProperty(hasPort, portIndividual);
			}
		}
		return interfaceIndividual;
	}

///=================================================================================================================================
///--------------------------------------------------------	storePorts() -----------------------------------------------------------
///=================================================================================================================================
	public static Individual storePorts(Port portObj) throws FileNotFoundException{

		String portResoureURI = PRPOOL_NS + portObj.getResourceURI();
		OntClass portResourceClass = phyResourcePoolModel.getOntClass(PRPOOL_NS+"Port");
		Individual portIndividual = phyResourcePoolModel.createIndividual(portResoureURI, portResourceClass);

		DatatypeProperty dpPortResourceURI = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceURI" );
		DatatypeProperty dpPortResourceName = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "resourceName" );
		DatatypeProperty dpPortNumber = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "portNumber" );
		DatatypeProperty dpPortBuffer = phyResourcePoolModel.createDatatypeProperty( PRPOOL_NS + "portBuffer" );
		portIndividual	.addProperty( dpPortResourceURI, phyResourcePoolModel.createTypedLiteral(portObj.getResourceURI()))
		.addProperty( dpPortResourceName, phyResourcePoolModel.createTypedLiteral(portObj.getResourceName()))
		.addProperty( dpPortNumber, phyResourcePoolModel.createTypedLiteral(portObj.getPortNumber()))
		.addProperty( dpPortBuffer, phyResourcePoolModel.createTypedLiteral(portObj.getPortBuffer()));
		if(PRPOOL_UPDATE_DBG) System.out.println("storePorts()--> "+ portObj.getResourceURI() +"\t added to "+PRPOOL);
		return portIndividual;
	}

///=================================================================================================================================
///------------------------ storeConnectivityData() ------------------------- updateIntfsConnectivity() ----------------------------
///	SUMMARY: 	Two methods to build topology by adding connectedToIntfs object property to Interfaces
///				Interface connectivity information must be added after creating vmHost and Switch interfaces in the ontology.
///=================================================================================================================================
	public static void storeConnectivityData(Datapath[] datapathObjArray, VmHost[] vmHostObjArray){
		for(VmHost vmHostObj : vmHostObjArray)
			for(Intfs intfsObj : vmHostObj.getIntfsObjArray())
				if(intfsObj.getConnectedIntfsUriList().size() > 0 )
					updateIntfsConnectivity(intfsObj);

		for(Datapath dpObj : datapathObjArray)
			for(Intfs intfsObj : dpObj.getIntfsObjArray())
				if(intfsObj.getConnectedIntfsUriList().size() > 0 )
					updateIntfsConnectivity(intfsObj);
	}
	public static void updateIntfsConnectivity(Intfs intfsObj){	
		Individual interfaceIndividual = phyResourcePoolModel.getIndividual(PRPOOL_NS + intfsObj.getResourceURI());
		ObjectProperty connectedToIntfs = phyResourcePoolModel.getObjectProperty(PRPOOL_NS+"connectedToIntfs");
		for(String connectedIntfs : intfsObj.getConnectedIntfsUriList()){
			Individual connectedIntfsIndividual = phyResourcePoolModel.getIndividual(PRPOOL_NS+connectedIntfs);
			interfaceIndividual.addProperty(connectedToIntfs, connectedIntfsIndividual);
		}
	}
}
