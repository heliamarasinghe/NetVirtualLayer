//	Date	: September 2014
//	Author	: Heli Amarasinghe
//	Class	: UpdateVrPoolIT.java
//	
//	Class Description 
//	Connects libvirt daemons in each hypervisor and retrieve total and available resource informatio and store in VMHost.java container object
//	VMHost.java object has following attributes (with default values)
//	 String hostName = "empty";
//	 String model = "empty";
//	 String hyperType = "empty";
//	 int hyperVer = 0;
//	 String uri = "empty";
//	 int cpuSockets = 0;
//	 int activeCpus = 0;
//	 int coresPerSocket = 0;
//	 int threadsPerCore = 0;
//	 int cpuFrequency = 0;
//	 int memory = 0;
//	 HashMap <String, String> interfaces;
//	 HashMap <String, String> defInterfaces;
//	 HashMap <String, String> networks;
//	 HashMap <String, String> defNetworks;
//	 VM[] activeVmObjArray = null;		// container object array which stores active VM attributes
//	 VM[] definedVmObjArray = null;		// container object array which stores defined VM attributes

package com.imaginelab.sdn_icf.discover;

import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.ConnectAuthDefault;
import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.Interface;
import org.libvirt.LibvirtException;
import org.libvirt.Network;
import org.libvirt.NodeInfo;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;

import com.imaginelab.sdn_icf.containers.Intfs;
import com.imaginelab.sdn_icf.containers.Port;
import com.imaginelab.sdn_icf.containers.PrintContainerValues;
import com.imaginelab.sdn_icf.containers.VM;
import com.imaginelab.sdn_icf.containers.VmHost;

import static com.imaginelab.sdn_icf.main.Constants.ALL_CLASS_C;
import static com.imaginelab.sdn_icf.main.Constants.DOMAIN_XML_TO_FILE;
import static com.imaginelab.sdn_icf.main.Constants.HOST_DBG;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_1;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_2;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_3;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_4;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_5;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_6;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_7;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_8;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_9;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_10;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_11;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_12;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_13;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_14;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_15;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_16;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_17;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_18;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_19;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_20;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_21;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_22;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_23;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_24;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_25;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_26;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_27;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_28;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_29;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_30;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_31;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_32;
import static com.imaginelab.sdn_icf.main.Constants.MININET_VBOX_HOSTS;
import static com.imaginelab.sdn_icf.main.Constants.PRINT_VM_HOST;
import static com.imaginelab.sdn_icf.main.Constants.SET_MIN_CORES_ONE;
import static com.imaginelab.sdn_icf.main.Constants.STATIC_HOST_CLASS;
import static com.imaginelab.sdn_icf.main.Constants.VM_DBG;


public class ReadPhysicalIT {

	private static VmHost[] vmHostObjArray = null;

	public static boolean updateITState(){
		//System.setProperty("jna.library.path", "C:\\Program Files (x86)\\Libvirt\\bin");

		String[] hypervisorUriArray = {HYPER_1, HYPER_2, HYPER_3, HYPER_4, HYPER_5, HYPER_6, HYPER_7, HYPER_8, HYPER_9/*, HYPER_10, HYPER_11, HYPER_12,
										HYPER_13, HYPER_14, HYPER_15, HYPER_16, HYPER_17, HYPER_18, HYPER_19, HYPER_20, HYPER_21, HYPER_22, HYPER_23, HYPER_24,
										HYPER_25, HYPER_26, HYPER_27, HYPER_28, HYPER_29, HYPER_30, HYPER_31, HYPER_32*/};
		//String[] hypervisorUriArray = {HYPER_1};
		
		int numOfHypervisors = hypervisorUriArray.length;
		vmHostObjArray = new VmHost[numOfHypervisors];
		

		for(int hostItr = 0; hostItr<numOfHypervisors; hostItr++){
			Long availableMemInVmHost = (long) 0;
			Long allocateMemToActiveVMs = (long) 0;
			Long allocateMemToDefinedVMs = (long) 0;
			int totalCoresInVmHost = 0;
			int availableCoresInVmHost = 0;
			int allocatedCoresToActiveVMs = 0;
			int allocatedCoresToDefinedVMs = 0;
			vmHostObjArray[hostItr] = new VmHost();
			String hostResourceURI = "empty";
			String hostResourceName = "empty";

			
			try{
				System.out.println("Connecting to Hypervisor  "+ hypervisorUriArray[hostItr]+" .....");		// Connecting to Hypervisor
				ConnectAuth ca = new ConnectAuthDefault();
				Connect conn = new Connect(hypervisorUriArray[hostItr], ca, 0);
				NodeInfo nodeInfo = conn.nodeInfo();
				
				if(HOST_DBG){
					System.out.println("\t getFreeMemory -->"+conn.getFreeMemory());
					System.out.println("\t numOfNetworkFilters -->"+conn.numOfNetworkFilters());
					//System.out.println("\t getMaxVcpus -->"+conn.getMaxVcpus());
					//System.out.println("\t getCapabilities -->"+conn.getCapabilities());
				}
				
				// Populate VmHost data-structure with hypervisor parameters
				if(HOST_DBG) System.out.println("Obtaining parameters from  "+ conn.getHostName());					
				
				hostResourceURI = "H-"+(hostItr+1);
				hostResourceName = conn.getHostName();
				int coresPerSocket = nodeInfo.cores;								// Host Cores per socket Multiplied for testing
				long totalMemory = nodeInfo.memory/1024;							// Host Total memory Multiplied for testing
				if(hostResourceName.equals("kvm-host1") || hostResourceName.equals("kvm-host9")) {
					System.err.println("ReadPhysicalIT.updateITState: "+hostResourceName+" total memory has been increased for testing");
					totalMemory += 2048;
					
				}
				
				vmHostObjArray[hostItr].setResourceURI(hostResourceURI);								//	host resourceIds; H-1, H-2, H-3, ... and VM resourceIds; H-1_VM-1, H-1_VM-2,.. H-2_VM-1, H-2_VM-2,.. 
				vmHostObjArray[hostItr].setResourceName(hostResourceName);								//	hostname
				vmHostObjArray[hostItr].setVmHostNum(hostItr+1);										//	nonUniqId = 1,2,3 ...
				vmHostObjArray[hostItr].setResModel(nodeInfo.model);										//	model
				vmHostObjArray[hostItr].setHyperType(conn.getType());									//	type
				vmHostObjArray[hostItr].setHyperVer("hv_"+conn.getVersion());							//	version
				vmHostObjArray[hostItr].setUri(conn.getURI());											//	url
				vmHostObjArray[hostItr].setCpuSockets(nodeInfo.sockets);								//	cpuSockets
				vmHostObjArray[hostItr].setActiveCpus(nodeInfo.cpus);									//	activeCpus
				vmHostObjArray[hostItr].setCoresPerSocket(coresPerSocket);								//	coresPerSocket
				vmHostObjArray[hostItr].setThreadsPerCore(nodeInfo.threads);							//	threadsPerCore
				vmHostObjArray[hostItr].setCpuFrequency(nodeInfo.mhz);									//	cpuFrequency
				vmHostObjArray[hostItr].setTotalMemory(totalMemory);									//	totalMemory in Mb
				//vmHostObjArray[hostItr].setAvailableMemory(conn.getFreeMemory());						//	availableMemory
				
				int numOfIntfs = conn.numOfInterfaces();
				
				vmHostObjArray[hostItr].setNumOfInterfaces(numOfIntfs);						//  numOfInterfacePorts
				vmHostObjArray[hostItr].setNumOfVms(conn.numOfDomains());								//  numOfVms
				
				

				if(HOST_DBG)System.out.println("\t numOfInterfaces -->"+numOfIntfs);								//	interfaces
				
				Intfs[] intfsObjArray = new Intfs[numOfIntfs];
				String[] interfaces = conn.listInterfaces(); 
				for(int intfsItr = 0;  intfsItr<interfaces.length; intfsItr++) {
					Interface intf = conn.interfaceLookupByName(interfaces[intfsItr]);
					String intfsResourceURI = hostResourceURI+"_I-"+intfsItr;
					intfsObjArray[intfsItr] = new Intfs(intfsResourceURI);
					String intfsResourceName = interfaces[intfsItr];
					//intfsObjArray[intfsItr].setResourceURI(intfsResourceURI);
					intfsObjArray[intfsItr].setResourceName(intfsResourceName);
					intfsObjArray[intfsItr].setMacAddress(intf.getMACString());
					int numPorts = 1;																// numPorts = 1 for each interface
					intfsObjArray[intfsItr].setNumPorts(numPorts);
					Port[] portObjArray = new Port[numPorts];
						portObjArray[0] = new Port();
						String portResourceURI = intfsResourceURI+"_P-0";
						String portResourceName = "Port0";
						portObjArray[0].setResourceURI(portResourceURI);
						portObjArray[0].setResourceName(portResourceName);
						portObjArray[0].setPortNumber(0);
						intfsObjArray[intfsItr].setPortObjArray(portObjArray);
					if(HOST_DBG)System.out.println("\tInterfaces\t"+interfaces[intfsItr]+":\t"+intf.getMACString());
				}
				//vmHostObjArray[hostItr].setNumOfInterfacePorts(intfsObjArray.length);												// 	numOfInterfacePorts
				vmHostObjArray[hostItr].setIntfsObjArray(intfsObjArray);
				//tmpHashMap.clear();
				
				// Temporary Hashmap				
				HashMap <String, String> tmpHashMap =  new HashMap<String, String>();
				
				if(HOST_DBG)System.out.println("\t numOfDefinedInterfaces -->"+conn.numOfDefinedInterfaces());					//	definedInterfaces
				String[] definedInterfaces = conn.listDefinedInterfaces();
				for(String defIntfs: definedInterfaces) {
					Interface intf = conn.interfaceLookupByName(defIntfs);
					tmpHashMap.put(defIntfs, intf.getMACString());
					if(HOST_DBG)System.out.println("\tDef Interfaces\t"+defIntfs+":\t"+intf.getMACString());
				}
				vmHostObjArray[hostItr].setDefInterfaces(tmpHashMap);
				tmpHashMap.clear();

				if(HOST_DBG)System.out.println("\t numOfNetworks -->"+conn.numOfNetworks());									//	networks
				String[] networks = conn.listNetworks();
				for(String nets: networks) {
					Network network = conn.networkLookupByName(nets);
					tmpHashMap.put(network.getName(), network.getBridgeName());
					if(HOST_DBG)System.out.println("\tNetworks\t"+network.getName()+":\t"+network.getBridgeName());
				}
				vmHostObjArray[hostItr].setNumOfOvs(tmpHashMap.size());									//	numOfOvs
				vmHostObjArray[hostItr].setNetworks(tmpHashMap);
				tmpHashMap.clear();

				if(HOST_DBG)System.out.println("\t numOfDefinedNetworks -->"+conn.numOfDefinedNetworks());						//	definedNetworks
				String[] definedNetworks = conn.listDefinedNetworks();
				for(String defNets: definedNetworks) {
					Network network = conn.networkLookupByName(defNets);
					tmpHashMap.put(network.getName(), network.getBridgeName());
					if(HOST_DBG)System.out.println("\tDef Networks\t"+network.getName()+":\t"+network.getBridgeName());
				}
				vmHostObjArray[hostItr].setDefNetworks(tmpHashMap);
				tmpHashMap.clear();
				if(HOST_DBG){
					if(HOST_DBG)System.out.println("\t numOfDefinedStoragePools -->"+conn.numOfDefinedStoragePools());
					String [] definedStoragePools = conn.listDefinedStoragePools();
					for(String stringItr: definedStoragePools) System.out.println("\t definedStoragePools -->"+stringItr);
					if(HOST_DBG)System.out.println("\t numOfStoragePools -->"+conn.numOfStoragePools());
					String [] storagePools = conn.listStoragePools();
					for(String stringItr: storagePools) System.out.println("\t storagePools -->"+stringItr);
					if(HOST_DBG)System.out.println("\t numOfDevices -->"+conn.numOfDevices("dd"));
					String [] devices = conn.listDevices("dd");
					for(String stringItr: devices) System.out.println("\t devices -->"+stringItr);
				}
				
				int numOfActiveVMs = conn.numOfDomains();
				if(VM_DBG)System.out.println("\t numOfDomains -->"+numOfActiveVMs);
				if(numOfActiveVMs>0){
					int[] activeDomainIds = conn.listDomains(); 
					if(VM_DBG)System.out.println("Reading Active VMs of "+hostResourceName);
					VM[] activeVmObjArray = new VM[activeDomainIds.length];
					
					int activeVmItr = 0;
					for(int domainIdItr: activeDomainIds){
						Domain domain = conn.domainLookupByID(domainIdItr);
						DomainInfo domainInfo = domain.getInfo();
						//DomainInfo domainInfo1 = domain.getJobInfo()
						String vmResourceURI = hostResourceURI+"_VM-"+activeVmItr;
						
						//String vmResourceName = domain.getName(); 						// set inside the xml parser
						
						//	SAX xml parser creates vmObj with extracted information from Domain.xml.		
						activeVmObjArray[activeVmItr] = parseDomainXml(domain.getXMLDesc(0), vmResourceURI, true);			//second parameter "true" for activeDomains
						
						activeVmObjArray[activeVmItr].setResLocation(hostResourceURI);
						activeVmObjArray[activeVmItr].setMaxCpus(domain.getMaxVcpus());
						activeVmObjArray[activeVmItr].setCpuTime(domainInfo.cpuTime);		
						//if(VM_DBG) printVmInfor(activeVmObjArray[vmItr]);
						allocateMemToActiveVMs += activeVmObjArray[activeVmItr].getMaxMemory();
						allocatedCoresToActiveVMs += activeVmObjArray[activeVmItr].getMaxCpus();
						activeVmItr++; 
					}
					vmHostObjArray[hostItr].setActiveVmObjArray(activeVmObjArray);
				}
				
				int numOfDefinedVMs = conn.numOfDefinedDomains();
				if(VM_DBG)System.out.println("\t numOfDefinedDomains -->"+conn.numOfDefinedDomains());
				if(numOfDefinedVMs>0){
					String[] definedDomainNames = conn.listDefinedDomains();
					//System.out.println("\nNumber of Defined(Inactive) Virtual Machines = "+definedDomainNames.length);
					if(VM_DBG)System.out.println("Reading Defined VMs of "+hostResourceName);
					VM[] definedVmObjArray = new VM[definedDomainNames.length];
					
					for(int defVmItr = 0; defVmItr<numOfDefinedVMs; defVmItr++){
						Domain domain = conn.domainLookupByName(definedDomainNames[defVmItr]);
						DomainInfo domainInfo = domain.getInfo();
						String vmResourceURI = hostResourceURI+"_VM-"+(numOfActiveVMs+defVmItr);
						//System.out.println("\tdomain xml for defined vm: "+definedDomainNames[defVmItr]+" \n"+domain.getXMLDesc(0));
						definedVmObjArray[defVmItr] = parseDomainXml(domain.getXMLDesc(0), vmResourceURI, false);		//third parameter "false" for definedDomains 
						definedVmObjArray[defVmItr].setMaxCpus(domainInfo.nrVirtCpu);	// Defined VMs give LibvirtException		
						definedVmObjArray[defVmItr].setCpuTime(domainInfo.cpuTime);
						allocateMemToDefinedVMs += definedVmObjArray[defVmItr].getMaxMemory();
						allocatedCoresToDefinedVMs += definedVmObjArray[defVmItr].getMaxCpus();
						
						
						if(DOMAIN_XML_TO_FILE){
							Domain domainToGetNative = conn.domainLookupByName(definedDomainNames[0]);
							String domainXml = domainToGetNative.getXMLDesc(0);
							//String kvmNativeDomainXml = conn.domainXMLToNative("qemu-argv", domainXml, 0);
							PrintWriter kvmNativeDomainXmlWriter = new PrintWriter("src/DataFiles/DomainXMLs/DomainXml_"+vmResourceURI+".xml", "UTF-8");
							kvmNativeDomainXmlWriter.println(domainXml);
							kvmNativeDomainXmlWriter.close();
						}
					}
					vmHostObjArray[hostItr].setDefinedVmObjArray(definedVmObjArray);
					
					
					
				}
				
				Long totalMemInVmHost = vmHostObjArray[hostItr].getTotalMemory();
				availableMemInVmHost = vmHostObjArray[hostItr].getTotalMemory() - (allocateMemToActiveVMs + allocateMemToDefinedVMs);
				totalCoresInVmHost = vmHostObjArray[hostItr].getCpuSockets()*vmHostObjArray[hostItr].getCoresPerSocket();
				availableCoresInVmHost = totalCoresInVmHost - (allocatedCoresToActiveVMs + allocatedCoresToDefinedVMs);
				if(HOST_DBG){System.out.println("\n"+hostResourceName+"\nvmHostObjArray[hostItr].getTotalMemory() ="+ vmHostObjArray[hostItr].getTotalMemory());
					System.out.println("allocateMemToActiveVMs ="+ allocateMemToActiveVMs);
					System.out.println("allocateMemToDefinedVMs ="+ allocateMemToDefinedVMs);
					System.out.println("availableMemInVmHost ="+ availableMemInVmHost);
				}
				vmHostObjArray[hostItr].setAvailableMemory(availableMemInVmHost);
				
				if(availableCoresInVmHost<1 && SET_MIN_CORES_ONE) availableCoresInVmHost = 1;		
				
				
				vmHostObjArray[hostItr].setAvailableCores(availableCoresInVmHost);
				
				if(ALL_CLASS_C) vmHostObjArray[hostItr].setNodeClass("C");
					
				else if(STATIC_HOST_CLASS){																		// Set vmHost Class
					switch (hostResourceName){
						case "kvm-host1": vmHostObjArray[hostItr].setNodeClass("C"); break;
						case "kvm-host2": vmHostObjArray[hostItr].setNodeClass("B"); break;
						case "kvm-host3": vmHostObjArray[hostItr].setNodeClass("B"); break;
						case "kvm-host4": vmHostObjArray[hostItr].setNodeClass("C"); break;
						case "kvm-host5": vmHostObjArray[hostItr].setNodeClass("C"); break;
						case "kvm-host6": vmHostObjArray[hostItr].setNodeClass("C"); break;
						case "kvm-host7": vmHostObjArray[hostItr].setNodeClass("C"); break;
						case "kvm-host8": vmHostObjArray[hostItr].setNodeClass("A"); break;
						case "kvm-host9": vmHostObjArray[hostItr].setNodeClass("A"); break;
						default: System.err.println("ReadPhysicalIT.updateITState: Host name \""+hostResourceName+"\" not recognized to assign host class"); 
							vmHostObjArray[hostItr].setNodeClass("C"); break;
					}
//					if(hostResourceName.equals("kvm-host3")) vmHostObjArray[hostItr].setNodeClass("C");
//					else if(hostResourceName.equals("kvm-host4") || hostResourceName.equalsIgnoreCase("kvm-host1")) vmHostObjArray[hostItr].setNodeClass("C");
//					else vmHostObjArray[hostItr].setNodeClass("B");
				}
				
				else if(MININET_VBOX_HOSTS){
					switch (vmHostObjArray[hostItr].getVmHostNum()%3){
					case 1:	vmHostObjArray[hostItr].setNodeClass("C"); break;
					case 2:	vmHostObjArray[hostItr].setNodeClass("B"); break;
					case 0:	vmHostObjArray[hostItr].setNodeClass("A"); break;
					}
				}
				else {
					if(totalMemInVmHost > 16000) vmHostObjArray[hostItr].setNodeClass("A");
					else if (totalMemInVmHost > 8000) vmHostObjArray[hostItr].setNodeClass("B");
					else vmHostObjArray[hostItr].setNodeClass("C");
				}
				conn.close();
			} catch(LibvirtException libEx) {
				System.err.println("Libvirt Exception thrown from  UpdatePrPoolIT.updateITState()");
				System.out.println("Please veryfy the libvirt deamon running in vm host.");
				System.out.println(" You can start deamon by \"$ sudo /etc/init.d/libvirt-bin start \"");
				libEx.printStackTrace();
				System.exit(0);
			} catch(Exception Ex) {
				System.err.println("General Exception thrown from  UpdatePrPoolIT.updateITState()");
				Ex.printStackTrace();
				System.exit(0);
			}
			if(PRINT_VM_HOST)  PrintContainerValues.printVmHostObj(vmHostObjArray[hostItr]);
		}

		
		if (vmHostObjArray != null)
			return true;
		else
			return false;
		
	}// End of method updateITState()
	
	
	
	
	///============================================================================================================
	///-------------------------------------------- parseDomainXml() ----------------------------------------------
	//
	// Method that create SAX parser and initiate xml handler.
	// Takes both Active abd Defined VM Domain.xml as inlut and forward to handler "ExtractVmDataFromXml()"
	// ExtractVmDataFromXml() returns a VM type object which contains information obtained from Domain.xml
	///============================================================================================================


	public static VM parseDomainXml(String domainXml, String vmResourceURI, boolean isActiveVM){  
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();  
		ExtractVmDataFromXml handler = null;
		try {
			SAXParser saxParser = saxParserFactory.newSAXParser();
			handler = new ExtractVmDataFromXml(vmResourceURI, isActiveVM);		// boolean isActiveVM; "true" for active and "false" for defined
			saxParser.parse(new InputSource(new StringReader(domainXml)), handler);

		} catch (SAXParseException saxEx) {
			System.err.println("SAXParseException caught at UpdatePrPoolIT.parseDomainXml()");
			saxEx.printStackTrace();
		} catch (Exception genEx) {
			System.err.println("General Exception caught at UpdatePrPoolIT.parseDomainXml()");
			genEx.printStackTrace();
		}
		return handler.getVmObj();
	}

	
	public static VmHost[] getVmHostObjArray(){
		return vmHostObjArray;
	}





}// End of class UpdatePrPoolIT 