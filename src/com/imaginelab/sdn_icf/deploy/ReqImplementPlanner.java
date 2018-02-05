package com.imaginelab.sdn_icf.deploy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.jena.atlas.io.IndentedWriter;
import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.ConnectAuthDefault;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Multimap;
import com.google.common.collect.ArrayListMultimap;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.imaginelab.sdn_icf.containers.AcceptedReq;
import com.imaginelab.sdn_icf.containers.Datapath;
import com.imaginelab.sdn_icf.containers.Intfs;
import com.imaginelab.sdn_icf.containers.PrintContainerValues;
import com.imaginelab.sdn_icf.containers.VM;
import com.imaginelab.sdn_icf.containers.VSw;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import static com.imaginelab.sdn_icf.main.Constants.BDLINK_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.COMMIT_DEPLOYMENT;
import static com.imaginelab.sdn_icf.main.Constants.CTRL1_IP;
import static com.imaginelab.sdn_icf.main.Constants.CTRL2_IP;
import static com.imaginelab.sdn_icf.main.Constants.CTRL3_IP;
import static com.imaginelab.sdn_icf.main.Constants.DISK_IMG_FLDR;
import static com.imaginelab.sdn_icf.main.Constants.DOMAINXML_FLDR;
import static com.imaginelab.sdn_icf.main.Constants.DOMAINXML_TYPE_A;
import static com.imaginelab.sdn_icf.main.Constants.DOMAINXML_TYPE_B;
import static com.imaginelab.sdn_icf.main.Constants.DOMAINXML_TYPE_C;
import static com.imaginelab.sdn_icf.main.Constants.HOST_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_1;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_2;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_3;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_4;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_5;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_6;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_7;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_8;
import static com.imaginelab.sdn_icf.main.Constants.HYPER_9;
import static com.imaginelab.sdn_icf.main.Constants.IMPLE_PERF_FILE;
import static com.imaginelab.sdn_icf.main.Constants.KVM1_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM2_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM3_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM4_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM5_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM6_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM7_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM8_IP;
import static com.imaginelab.sdn_icf.main.Constants.KVM9_IP;
import static com.imaginelab.sdn_icf.main.Constants.LUBUNTU_ISO;
import static com.imaginelab.sdn_icf.main.Constants.NL;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.PRPOOL_QUERY_PREFIX;
import static com.imaginelab.sdn_icf.main.Constants.REQ_IMPL_DBG;
import static com.imaginelab.sdn_icf.main.Constants.SWCH_PRFX;
import static com.imaginelab.sdn_icf.main.Constants.VMHOST_PWD;
import static com.imaginelab.sdn_icf.main.Constants.VMHOST_SUPWD;
import static com.imaginelab.sdn_icf.main.Constants.VMHOST_UNAME;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_NS;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_QUERY_PREFIX;



public class ReqImplementPlanner {
	private static OntModel vrPoolModel;
	private static OntModel prPoolModel;
	
	// ============================================================================================================================== //
	// ------------------------------------------------- createVMsForAcceptedRequests() --------------------------------------------- //
	// ============================================================================================================================== //
	public static void createVMsForAcceptedRequests(){
		
		vrPoolModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM_RDFS_INF);
		vrPoolModel.read(VRPOOL, "RDF/XML");
		
		prPoolModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_MICRO_RULE_INF);
		prPoolModel.read(PRPOOL, "RDF/XML");
		
		// query VR_Pool for accepted IaasRequest and re-create IaaSRequest object 
		PriorityQueue<AcceptedReq> reqQueue = getAcceptedReqFromVrPool();	// query for request id and 
		
		HashMap<String, VM> rsvdVmIdToObjMap = getRservedVMsFromVrPool();	// query for all reserved VMs and create VM objects 
		HashMap<String, VSw> rsvdSwIdToObjMap = getVSwFromVrPool();	// query for all reserved Switches and create VSw
		
		List<String> availableOpenflowCtrlList = getAvailableControllers();
		Iterator<String> controlerIterator = availableOpenflowCtrlList.iterator();
		HashMap<String, Datapath> datapathIdToObjMap = getDatapathsFromPrPool();	// query for all reserved Switches and create VSw
		//ArrayList<String> datapthList = new ArrayList<String>();
		HashMap <String, String> allocVResMap = new HashMap<String, String>();
		
		if(reqQueue.size()<1) System.err.println("ReqImplementPlanner.createVMsForAcceptedRequests: No accepted but unallocated requests detected");
		if(rsvdVmIdToObjMap.size()<1) System.err.println("ReqImplementPlanner.createVMsForAcceptedRequests: No reserved virtual machines detedted");
		
		
		//for(String reqURI : acceptedReqMap.keySet()){												// for each request
		for(int reqPqItr = 0; reqPqItr<reqQueue.size(); reqPqItr++){
			//AcceptedReq acceptedReq = acceptedReqMap.get(reqURI);
			AcceptedReq acceptedReq = reqQueue.poll();
			
			PrintContainerValues.acceptedReqObj(acceptedReq);
			String reqURI = acceptedReq.getReqUri();
			long diskCreateTimeForReq = 0L;
			long vmCreateTimeForReq = 0L;
			//String remoteExecTime = "";
			
			
			HashMap<String, VM> allocVmMapForReq = new HashMap<String, VM>();
			for (String rsvdVmResUri : acceptedReq.getRsvdVmResList()){											// for each VM							
					System.out.println("Creating VM "+rsvdVmResUri);
					VM vmObj = rsvdVmIdToObjMap.remove(rsvdVmResUri);											// Shrink the sze of the rsvdVmIdToObjMap for scalability
					
					if(REQ_IMPL_DBG)PrintContainerValues.printVmObj(vmObj);
					
						String vmHost = vmObj.getResLocation();	
						String vmUri = vmObj.getResourceURI();
						String vmName = vmObj.getResourceName();
						
						
						Document document = null;
						try {
							DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
							DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
					
							if(vmObj.getPriorityClass().equals("class_a")) document = documentBuilder.parse(DOMAINXML_FLDR + DOMAINXML_TYPE_A);
							else if(vmObj.getPriorityClass().equals("class_b"))	document = documentBuilder.parse(DOMAINXML_FLDR + DOMAINXML_TYPE_B);
							else document = documentBuilder.parse(DOMAINXML_FLDR + DOMAINXML_TYPE_C);
							
						} catch (ParserConfigurationException pce) {
							pce.printStackTrace();
						} catch (IOException ioe) {
							ioe.printStackTrace();
						} catch (SAXException sae) {
							sae.printStackTrace();
						}
						List<String> remoteExecCmdList = new ArrayList<String>();
						
						//Create Remote disk.img file
						String diskImageFile = DISK_IMG_FLDR + vmName+".img";
						int diskImageSize = (int) vmObj.getTotalStorage();
						String createDiskImgCommand = "sudo -S -p '' qemu-img create "+diskImageFile +" "+ diskImageSize;
						remoteExecCmdList.add(createDiskImgCommand);
						
						
						String remoteHostIp = null;
						String hypervisorUri = null;
						

						switch(vmHost){
							case "H-1":		remoteHostIp = KVM1_IP;		hypervisorUri = HYPER_1;  break;
							case "H-2":		remoteHostIp = KVM2_IP;		hypervisorUri = HYPER_2;  break;
							case "H-3":		remoteHostIp = KVM3_IP;		hypervisorUri = HYPER_3;  break;
							case "H-4":		remoteHostIp = KVM4_IP;		hypervisorUri = HYPER_4;  break;
							case "H-5":		remoteHostIp = KVM5_IP;		hypervisorUri = HYPER_5;  break;
							case "H-6":		remoteHostIp = KVM6_IP;		hypervisorUri = HYPER_6;  break;
							case "H-7":		remoteHostIp = KVM7_IP;		hypervisorUri = HYPER_7;  break;
							case "H-8":		remoteHostIp = KVM8_IP;		hypervisorUri = HYPER_8;  break;
							case "H-9":		remoteHostIp = KVM9_IP;		hypervisorUri = HYPER_9;  break;
							/*case "H-10":	remoteHostIp = KVM10_IP;	hypervisorUri = HYPER_10; break;
							case "H-11":	remoteHostIp = KVM11_IP;	hypervisorUri = HYPER_11; break;
							case "H-12":	remoteHostIp = KVM12_IP;	hypervisorUri = HYPER_12; break;
							case "H-13":	remoteHostIp = KVM13_IP;	hypervisorUri = HYPER_13; break;
							case "H-14":	remoteHostIp = KVM14_IP;	hypervisorUri = HYPER_14; break;
							case "H-15":	remoteHostIp = KVM15_IP;	hypervisorUri = HYPER_15; break;
							case "H-16":	remoteHostIp = KVM16_IP;	hypervisorUri = HYPER_16; break;
							case "H-17":	remoteHostIp = KVM17_IP;	hypervisorUri = HYPER_17; break;
							case "H-18":	remoteHostIp = KVM18_IP;	hypervisorUri = HYPER_18; break;
							case "H-19":	remoteHostIp = KVM19_IP;	hypervisorUri = HYPER_19; break;
							case "H-20":	remoteHostIp = KVM20_IP;	hypervisorUri = HYPER_20; break;
							case "H-21":	remoteHostIp = KVM21_IP;	hypervisorUri = HYPER_21; break;
							case "H-22":	remoteHostIp = KVM22_IP;	hypervisorUri = HYPER_22; break;
							case "H-23":	remoteHostIp = KVM23_IP;	hypervisorUri = HYPER_23; break;
							case "H-24":	remoteHostIp = KVM24_IP;	hypervisorUri = HYPER_24; break;*/
							default: System.err.println("ReqImplementPlanner.createDomainXmlForVM: Host not recognized");
						}
						
						//---------------------------Send VM MAC and VLAN to Hypervisor-------------------------------
						//System.out.println("Append VM MAC and VLAN-id to macToVlan.txt file in "+remoteHostIp+". Command--> \""+createDiskImgCommand+"\"");
						//String appendMacVlan = "echo '"+vmObj.getIntfsObjArray()[0].getMacAddress()+","+acceptedReq.getTargetVlan()+"' >> ~/pox/macToVlan.txt";
						//remoteExecCmdList.add(appendMacVlan);
						
						System.out.println("Create disk image on "+remoteHostIp+". Command--> \""+createDiskImgCommand+"\"");
						long createVmDiskST = System.currentTimeMillis();
						//remoteExecTime = execCmdRemoteHost(remoteHostIp, remoteExecCmdList);
						if(COMMIT_DEPLOYMENT) execCmdRemoteHost(remoteHostIp, remoteExecCmdList);
						long createVmDiskET = System.currentTimeMillis();
						// To delete all disk image files created in kvm hosts and slices from flowvisor, execute script "NetVirtualLayer/shellScripts/cleanHyperPhysical.sh"
						diskCreateTimeForReq += (createVmDiskET-createVmDiskST);
						
						long createVmST = System.currentTimeMillis();
						
						
						//Create Domain.xml file from VM object
						String domainXml = createDomainXmlForVM(vmObj, acceptedReq.getTargetVlan(), diskImageFile,  document);
						
						
						if(COMMIT_DEPLOYMENT){
							if(REQ_IMPL_DBG)System.out.println("\n\nPrinting domainXml\n"+domainXml);
							try {
								
								System.out.println("\nDefining domain for vRes "+vmUri);
								ConnectAuth connectAuth = new ConnectAuthDefault();
								Connect connect = new Connect(hypervisorUri, connectAuth, 0);
								Domain domain = connect.domainDefineXML(domainXml);
								/*Domain domain = null;
								if(connect.domainLookupByName(vmName) != null){
									System.err.println("Domain "+vmName+" already exist in "+vmHost);
									domain = connect.domainLookupByName(vmName);
								}
								else{
									System.out.println("\nCreating Domain by XML\n");
									domain = connect.domainDefineXML(domainXml);
									System.out.println("\n\tVM "+domain.getName()+" created successfully with UUID "+domain.getUUIDString()+"\n\n");
								}
								*/
								System.out.println("\tVM "+domain.getName()+" created successfully with UUID "+domain.getUUIDString()+"\n");
							} catch (LibvirtException libEx) {
								System.err.println("ReqImplementPlanner.createVMsForAcceptedRequests: LibvirtException caught");
								libEx.printStackTrace();
							}
						}
						else System.out.println("\t\t\t\t Domain definition for  "+vmUri+"  NOT commited");
						long createVmET = System.currentTimeMillis();
						
						vmCreateTimeForReq += (createVmET - createVmST);
						
						allocVmMapForReq.put(vmUri, vmObj);											// Removed vmObj from rsvdVmIdToObjMap is put to allocVmMapForReq
						allocVResMap.put(vmUri, reqURI);											// Used to update VrPool
			
						
			}//for (String rsvdVResUri : rsvdVResCollection)
			
			
			//if(reqURI.equals("REQ-0"))														// Testing only
			String controller_url = "";
			if(controlerIterator.hasNext()){
				controller_url = controlerIterator.next();
				controlerIterator.remove();
			}
			
			long createSliceST = System.currentTimeMillis();	
			int threadSleepTime = CreateSliceForReq.buildSlice(acceptedReq, allocVmMapForReq, rsvdSwIdToObjMap, datapathIdToObjMap, controller_url);
			long createSliceET = System.currentTimeMillis();	
			
			try{
				PrintWriter printwriter = new PrintWriter(new FileWriter(IMPLE_PERF_FILE, true));
				printwriter.println(acceptedReq.getReqId() +"\t"+ diskCreateTimeForReq +"\t"+/* remoteExecTime +"\t"+*/ vmCreateTimeForReq +"\t"+ Long.toString(createSliceET-createSliceST) +"\t"+ acceptedReq.getRsvdVsResList().size() +"\t"+ threadSleepTime);
				printwriter.close();
				} catch(IOException ioEx){
					System.err.println("InfrastructureComposer.compositionEventManager: File writer exception caught when writing performance results");
					ioEx.printStackTrace();
				}
			
		}//for(String reqURI : icReqPropertyMap.keySet())
		
		System.out.println("allocVResMap.size = "+allocVResMap.size());
		for(String vRes : allocVResMap.keySet()){
			System.out.println("\t"+vRes+"\t-\t"+allocVResMap.get(vRes));
		}
		changeVResState(allocVResMap);					
	}
	// ============================================================================================================================== //
	// --------------------------------------------------- getAcceptedReqFromVrPool() ----------------------------------------------- //
	// ============================================================================================================================== //
	public static PriorityQueue<AcceptedReq> getAcceptedReqFromVrPool(){
		
		LinkedHashMap<String, AcceptedReq> acceptedReqMap = new LinkedHashMap<String, AcceptedReq>(); 
		HashMap<String, Multimap<String, String>> icReqPropertyMap = new HashMap<String, Multimap<String, String>>();	// Hashed MultiMaps used organize quey output
		
		if(REQ_IMPL_DBG)System.out.println("\n Querying for accepted Infrastructure Composition Requests");
		String selectString = "SELECT DISTINCT *";
		String whereString = "WHERE { ?reqUri a cloud_ont:IaaS_Request. ?reqUri ?dpName ?dpValue. "
				+ "?reqUri cloud_ont:hasReqState ?state. ?state rdf:type cloud_ont:Accepted.}";
		Query icReqQry = QueryFactory.create(VRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		if(REQ_IMPL_DBG) icReqQry.serialize(new IndentedWriter(System.out,true));
		QueryExecution icReqQryExec = QueryExecutionFactory.create(icReqQry, vrPoolModel) ;
		ResultSet icReqRsltset = icReqQryExec.execSelect() ;
		// reading result-set and 
		while (icReqRsltset.hasNext()){
			QuerySolution vmHostQrySolution = icReqRsltset.nextSolution();
			String reqUri = vmHostQrySolution.get("reqUri").asResource().toString().split("#")[1];
			String dpName = vmHostQrySolution.get("dpName").asResource().toString().split("#")[1];
			String dpValue= vmHostQrySolution.get("dpValue").isLiteral() ? vmHostQrySolution.get("dpValue").asLiteral().getString() : vmHostQrySolution.get("dpValue").asResource().toString().split("#")[1];
			if (icReqPropertyMap.get(reqUri) != null){
				Multimap<String, String> dpMapOfRes = icReqPropertyMap.remove(reqUri);
				dpMapOfRes.put(dpName, dpValue);
				icReqPropertyMap.put(reqUri, dpMapOfRes);
			}
			else{
				Multimap<String, String> innerPropertyMMap = ArrayListMultimap.create();
				innerPropertyMMap.put(dpName, dpValue);
				icReqPropertyMap.put(reqUri, innerPropertyMMap);
			}
		}
		icReqQryExec.close();
		
		if(REQ_IMPL_DBG) 
		for(String reqURI : icReqPropertyMap.keySet()){
			System.out.println("\n \tProperties of Request:  "+reqURI);
			Multimap<String, String> innerPropertyMMap = icReqPropertyMap.get(reqURI);
			for(String dpName : innerPropertyMMap.keySet()){
				Collection<String> propertyValCollection = innerPropertyMMap.get(dpName);
				System.out.println("\t\t"+dpName);
				for(String propertyVal : propertyValCollection)
					System.out.println("\t\t\t"+propertyVal);	
			}
		}
		
		//OrderReqByArrival orderInst = OrderReqByArrival.INSTANCE;
		PriorityQueue<AcceptedReq> reqQueue = new PriorityQueue<AcceptedReq>(10, OrderReqByArrival.INSTANCE);
			
		// Creating and populating AcceptedReq type objects
		for(String reqURI : icReqPropertyMap.keySet()){
			Multimap<String, String> innerPropertyMMap = icReqPropertyMap.get(reqURI);
			int reqId = Integer.parseInt(innerPropertyMMap.get("reqId").iterator().next());
			int reqArrival = Integer.parseInt(innerPropertyMMap.get("reqArrival").iterator().next());
			AcceptedReq acceptedReq = new AcceptedReq(reqURI, reqArrival);
			acceptedReq.setReqId(reqId);
			acceptedReq.setReqName(innerPropertyMMap.get("reqName").iterator().next());
			acceptedReq.setReqDuration(Integer.parseInt(innerPropertyMMap.get("reqDuration").iterator().next()));
			acceptedReq.setPriorityCls(innerPropertyMMap.get("haspriority").iterator().next());
			acceptedReq.setReqState(innerPropertyMMap.get("hasReqState").iterator().next());
			acceptedReq.setTargetVlan(1000+reqId);
			
			System.out.println("innerPropertyMMap.get(\"hasRsvdRes\").size() "+innerPropertyMMap.get("hasRsvdRes").size());
			for(String rsvdRes : innerPropertyMMap.get("hasRsvdRes")){
				System.out.println("rsvdRes = "+rsvdRes);
				
				if(rsvdRes.split("-")[0].equals(HOST_PRFX))	acceptedReq.addToRsvdVmResList(rsvdRes);
				else if(rsvdRes.split("-")[0].equals(SWCH_PRFX))	acceptedReq.addToRsvdVsResList(rsvdRes);
				else if(rsvdRes.split("-")[0].equals(BDLINK_PRFX))	acceptedReq.addToRsvdVlResList(rsvdRes);
				else System.err.println("ReqImplementPlanner.getAcceptedReqFromVrPool: Reserved resource not recognized");
			}
			for(String rsvdRes : innerPropertyMMap.get("hasAllocRes")){
				if(rsvdRes.split("-")[0].equals(HOST_PRFX))	acceptedReq.addToAlocVmResList(rsvdRes);
				else if(rsvdRes.split("-")[0].equals(SWCH_PRFX))	acceptedReq.addToAlocVsResList(rsvdRes);
				else if(rsvdRes.split("-")[0].equals(BDLINK_PRFX))	acceptedReq.addToAlocVlResList(rsvdRes);
				else System.err.println("ReqImplementPlanner.getAcceptedReqFromVrPool: Allocated resource not recognized");
			}
			acceptedReqMap.put(reqURI, acceptedReq);
			reqQueue.offer(acceptedReq);
			
			//PriorityQueue<Data> queue = new PriorityQueue<Data>(10, OrderReqByArrival.INSTANCE);
			
			
		}
		
		System.out.println("\t\tRequest deployment order = "+reqQueue);
		
		
		
		return reqQueue;
	}
	
	
	
	// ============================================================================================================================== //
	// ---------------------------------------------------- getRservedVMsFromVrPool() ----------------------------------------------- //
	// ============================================================================================================================== //	
	public static HashMap<String, VM> getRservedVMsFromVrPool(){
		HashMap<String, VM> rsvdVmIdToObjMap = new HashMap<String, VM>();
		HashMap<String, HashMap<String, String>> rsvdVmPropertyMap = new HashMap<String, HashMap<String, String>>();
		String selectString = "SELECT DISTINCT ?vr ?property ?value";
		String whereString = "WHERE { ?vr a cloud_ont:VR. ?vr ?property ?value. "
				+ "?vr cloud_ont:has_state ?state. ?state rdf:type cloud_ont:Reserved. "
				+ "?vr cloud_ont:provideservice ?service. ?service rdf:type cloud_ont:Compute_service.}";
		Query rsvdVmQry = QueryFactory.create(VRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		if(REQ_IMPL_DBG) rsvdVmQry.serialize(new IndentedWriter(System.out,true));
		QueryExecution rsvdVmQryExec = QueryExecutionFactory.create(rsvdVmQry, vrPoolModel) ;
		ResultSet rsvdVmRsltset = rsvdVmQryExec.execSelect() ;
		while (rsvdVmRsltset.hasNext()){
			QuerySolution vmHostQrySolution = rsvdVmRsltset.nextSolution();
			String vr = vmHostQrySolution.get("vr").asResource().toString().split("#")[1];
			String property = vmHostQrySolution.get("property").asResource().toString().split("#")[1];
			String value= vmHostQrySolution.get("value").isLiteral() ? vmHostQrySolution.get("value").asLiteral().getString() : vmHostQrySolution.get("value").asResource().toString().split("#")[1];

			if (rsvdVmPropertyMap.get(vr) != null){
				HashMap<String, String> propertyToValMap = rsvdVmPropertyMap.remove(vr);
				propertyToValMap.put(property, value);
				rsvdVmPropertyMap.put(vr, propertyToValMap);
			}
			else{
				HashMap<String, String> propertyToValMap = new HashMap<String, String>();
				propertyToValMap.put(property, value);
				rsvdVmPropertyMap.put(vr, propertyToValMap);
			}
		}
		rsvdVmQryExec.close();
		
		if(REQ_IMPL_DBG) for(String vmURI : rsvdVmPropertyMap.keySet()){
			System.out.println("\n \tProperties of VM:  "+vmURI);
			HashMap<String, String> propertyToValMap = rsvdVmPropertyMap.get(vmURI);
			for(String dpName : propertyToValMap.keySet()){
				System.out.println("\t\t"+dpName+"\t\t"+propertyToValMap.get(dpName));
			}
		}
		

		for(String vmURI : rsvdVmPropertyMap.keySet()){
			VM vmObj = new VM();
			
			HashMap<String, String> propertyToValMap = rsvdVmPropertyMap.get(vmURI);
			vmObj.setResourceURI(vmURI);
			vmObj.setResourceName(propertyToValMap.get("isRsvdResof")+"_"+vmURI);
			vmObj.setVmType("kvm");
			vmObj.setResLocation(propertyToValMap.get("location"));
			vmObj.setPriorityClass(propertyToValMap.get("haspriority"));
			vmObj.setUuid(UUID.randomUUID().toString());
			vmObj.setMaxMemory((long)Double.parseDouble(propertyToValMap.get("memory"))*1024*1024);
			vmObj.setCurrentMemory((long)Double.parseDouble(propertyToValMap.get("memory"))*1024*1024);
			vmObj.setvCpus((int)Double.parseDouble(propertyToValMap.get("n_cores")));
			vmObj.setCpuArch("x86_64");
			vmObj.setMachineOs("pc-i440fx-trusty");
			vmObj.setMaxCpus((int)Double.parseDouble(propertyToValMap.get("n_cores")));
			vmObj.setTotalStorage(Double.parseDouble(propertyToValMap.get("storage")));
			vmObj.setNumberOfIntfs(1);
			
			Intfs[] intfsObjArray = new Intfs[1];
			/*
			 	private String interfaceType = "empty";				// bridge
				private String virtualPortType = "none";			// <virtualport type='openvswitch'>
				private String macAddress = "empty";				// guest interface mac address
				private String sourceNetwork = "empty";				// only in definedDomain XML <source network='ovsbr0'/> 
				private String targetOvsIntfs = "empty";				// only in definedDomain XML <source network='ovsbr0'/>
				private String modelType = "none";					// <model type='virtio'/> Only available for defined domains
				private int targetVlanTagId = 3999;					//  to stick the VM to one vlan on the openvswitch
			 */
			intfsObjArray[0] = new Intfs(vmURI+"_I-0");
			intfsObjArray[0].setResourceName(vmObj.getResourceName()+"_I-0");
			intfsObjArray[0].setInterfaceType("bridge");
			intfsObjArray[0].setVirtualPortType("openvswitch");
			intfsObjArray[0].setMacAddress(randomMACAddress());
			intfsObjArray[0].setSourceNetwork("ovsbr0");
			intfsObjArray[0].setTargetOvsIntfs("to"+vmURI);
			intfsObjArray[0].setModelType("virtio");
			
			vmObj.setIntfsObjArray(intfsObjArray);
			
			rsvdVmIdToObjMap.put(vmURI, vmObj);
		}
		return rsvdVmIdToObjMap;
	}
	
	// ============================================================================================================================== //
	// -------------------------------------------------------- getVSwFromVrPool() -------------------------------------------------- //
	// ============================================================================================================================== //	
	public static HashMap<String, VSw> getVSwFromVrPool(){
		System.out.println("Reading vSwitches from VrPool");
		HashMap<String, VSw> vSwIdToObjMap = new HashMap<String, VSw>();
		HashMap<String, Multimap<String, String>> vSwPropertyMap = new HashMap<String, Multimap<String, String>>();
		String selectString = "SELECT DISTINCT ?vr ?property ?value";
		String whereString = "WHERE { ?vr a cloud_ont:VR. ?vr ?property ?value. "
				+ "?vr cloud_ont:has_state ?state. ?state rdf:type cloud_ont:free. "
				+ "?vr cloud_ont:provideservice ?service. ?service rdf:type cloud_ont:Switching.}";
		Query vSwQry = QueryFactory.create(VRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		if(REQ_IMPL_DBG) vSwQry.serialize(new IndentedWriter(System.out,true));
		QueryExecution vSwQryExec = QueryExecutionFactory.create(vSwQry, vrPoolModel) ;
		ResultSet vSwRsltset = vSwQryExec.execSelect() ;
		while (vSwRsltset.hasNext()){
			QuerySolution vSwQrySolution = vSwRsltset.nextSolution();
			String vr = vSwQrySolution.get("vr").asResource().toString().split("#")[1];
			String property = vSwQrySolution.get("property").asResource().toString().split("#")[1];
			String value= vSwQrySolution.get("value").isLiteral() ? vSwQrySolution.get("value").asLiteral().getString() : vSwQrySolution.get("value").asResource().toString().split("#")[1];

			if (vSwPropertyMap.get(vr) != null){
				Multimap<String, String> propertyToValMap = vSwPropertyMap.remove(vr);
				propertyToValMap.put(property, value);
				vSwPropertyMap.put(vr, propertyToValMap);
			}
			else{
				Multimap<String, String> propertyToValMap =  ArrayListMultimap.create();
				propertyToValMap.put(property, value);
				vSwPropertyMap.put(vr, propertyToValMap);
			}
		}
		vSwQryExec.close();
		
		if(REQ_IMPL_DBG) 
			for(String reqURI : vSwPropertyMap.keySet()){
				System.out.println("\n \tProperties of vSw:  "+reqURI);
				Multimap<String, String> innerPropertyMMap = vSwPropertyMap.get(reqURI);
				for(String dpName : innerPropertyMMap.keySet()){
					Collection<String> propertyValCollection = innerPropertyMMap.get(dpName);
					System.out.println("\t\t"+dpName);
					for(String propertyVal : propertyValCollection)
						System.out.println("\t\t\t"+propertyVal);	
				}
			}

		for(String vswURI : vSwPropertyMap.keySet()){
			VSw vswObj = new VSw();
			
			Multimap<String, String> propertyToValMap = vSwPropertyMap.get(vswURI);
			vswObj.setResourceURI(vswURI);
			vswObj.setResourceName(vswURI);
			vswObj.setLocation(propertyToValMap.get("location").iterator().next());
			vswObj.setBw(Double.parseDouble(propertyToValMap.get("bw").iterator().next()));
			vswObj.setCpu(Double.parseDouble(propertyToValMap.get("cpu").iterator().next()));
			vswObj.setMemory(Double.parseDouble(propertyToValMap.get("memory").iterator().next()));
			vswObj.setN_cores(Double.parseDouble(propertyToValMap.get("n_cores").iterator().next()));
			vswObj.setLoss_rate(Double.parseDouble(propertyToValMap.get("loss_rate").iterator().next()));
			vswObj.setDelay(Double.parseDouble(propertyToValMap.get("delay").iterator().next()));
			vswObj.setStorage(Double.parseDouble(propertyToValMap.get("storage").iterator().next()));
			
			Collection<String> connectedResColection = propertyToValMap.get("connectedto");
			for (String connectedRes : connectedResColection)
				vswObj.addToAlocVmResList(connectedRes);
			vSwIdToObjMap.put(vswURI, vswObj);
		}
		return vSwIdToObjMap;
	}
	
	// ============================================================================================================================== //
	// ----------------------------------------------------- getDatapathsFromPrPool() ----------------------------------------------- //
	// ============================================================================================================================== //	
	private static HashMap<String, Datapath> getDatapathsFromPrPool(){
		
		System.out.println("Reading Datapaths from VrPool");
		HashMap<String, Multimap<String, String>> dpPropertyMap = new HashMap<String, Multimap<String, String>>();
		String selectString = "SELECT DISTINCT ?dp ?property ?value";
		String whereString = "WHERE { ?dp a prpool:Switch. ?dp ?property ?value. }";
		Query dpQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		if(REQ_IMPL_DBG) dpQry.serialize(new IndentedWriter(System.out,true));
		QueryExecution dpQryExec = QueryExecutionFactory.create(dpQry, prPoolModel) ;
		ResultSet dpRsltset = dpQryExec.execSelect() ;
		while (dpRsltset.hasNext()){
			QuerySolution dpQrySolution = dpRsltset.nextSolution();
			String vr = dpQrySolution.get("dp").asResource().toString().split("#")[1];
			String property = dpQrySolution.get("property").asResource().toString().split("#")[1];
			String value= dpQrySolution.get("value").isLiteral() ? dpQrySolution.get("value").asLiteral().getString() : dpQrySolution.get("value").asResource().toString().split("#")[1];

			if (dpPropertyMap.get(vr) != null){
				Multimap<String, String> propertyToValMap = dpPropertyMap.remove(vr);
				propertyToValMap.put(property, value);
				dpPropertyMap.put(vr, propertyToValMap);
			}
			else{
				Multimap<String, String> propertyToValMap =  ArrayListMultimap.create();
				propertyToValMap.put(property, value);
				dpPropertyMap.put(vr, propertyToValMap);
			}
		}
		dpQryExec.close();
		
		
		//TODO: Read Datapath Switching and compute components and populate Datapath object
		
		
		System.out.println("Reading Datapath Interfaces from PrPool");
		HashMap<String, Multimap<String, String>> dpIntfsPropertyMap = new HashMap<String, Multimap<String, String>>();
		selectString = "SELECT DISTINCT ?interface ?property ?value";
		whereString = "WHERE { ?interface rdf:type prpool:Interface. ?interface ?property ?value . "
				+ "?interface prpool:isInterfaceOf ?node. ?node rdf:type prpool:Switch.}";
				//+ "?interface prpool:isInterfaceOf ?node. ?node rdf:type prpool:Switch.}";
		Query dpIntfsQry = QueryFactory.create(PRPOOL_QUERY_PREFIX + NL + selectString + NL + whereString);
		if(REQ_IMPL_DBG) dpIntfsQry.serialize(new IndentedWriter(System.out,true));
		QueryExecution dpIntfsQryExec = QueryExecutionFactory.create(dpIntfsQry, prPoolModel) ;
		ResultSet dpIntfsRsltset = dpIntfsQryExec.execSelect() ;
		while (dpIntfsRsltset.hasNext()){
			QuerySolution dpIntfsQrySolution = dpIntfsRsltset.nextSolution();
			String vr = dpIntfsQrySolution.get("interface").asResource().toString().split("#")[1];
			String property = dpIntfsQrySolution.get("property").asResource().toString().split("#")[1];
			String value= dpIntfsQrySolution.get("value").isLiteral() ? dpIntfsQrySolution.get("value").asLiteral().getString() : dpIntfsQrySolution.get("value").asResource().toString().split("#")[1];

			if (dpIntfsPropertyMap.get(vr) != null){
				Multimap<String, String> propertyToValMap = dpIntfsPropertyMap.remove(vr);
				propertyToValMap.put(property, value);
				dpIntfsPropertyMap.put(vr, propertyToValMap);
			}
			else{
				Multimap<String, String> propertyToValMap =  ArrayListMultimap.create();
				propertyToValMap.put(property, value);
				dpIntfsPropertyMap.put(vr, propertyToValMap);
			}
		}
		dpIntfsQryExec.close();
		
		// Printing Hashed Multimaps
		if(REQ_IMPL_DBG) 
			for(String dpURI : dpPropertyMap.keySet()){
				System.out.println("\n \tProperties of Datapath:  "+dpURI);
				Multimap<String, String> innerPropertyMMap = dpPropertyMap.get(dpURI);
				for(String propertyName : innerPropertyMMap.keySet()){
					Collection<String> propertyValCollection = innerPropertyMMap.get(propertyName);
					System.out.println("\t\t"+propertyName);
					for(String propertyVal : propertyValCollection)
						System.out.println("\t\t\t"+propertyVal);	
				}
			}
			
		if(REQ_IMPL_DBG) 
			for(String dpIntfsURI : dpIntfsPropertyMap.keySet()){
				System.out.println("\n \tProperties of Interface:  "+dpIntfsURI);
				Multimap<String, String> innerPropertyMMap = dpIntfsPropertyMap.get(dpIntfsURI);
				for(String propertyName : innerPropertyMMap.keySet()){
					Collection<String> propertyValCollection = innerPropertyMMap.get(propertyName);
					System.out.println("\t\t"+propertyName);
					for(String propertyVal : propertyValCollection)
						System.out.println("\t\t\t"+propertyVal);	
				}
			}
		
		HashMap<String, Intfs> intfsIdMap = new HashMap<String, Intfs>();
		for(String dpIntfsURI : dpIntfsPropertyMap.keySet()){
			Intfs inatfsObj = new Intfs(dpIntfsURI);
			Multimap<String, String> propertyToValMap = dpIntfsPropertyMap.get(dpIntfsURI);
			inatfsObj.setResourceName(propertyToValMap.get("resourceName").iterator().next());
			inatfsObj.setMacAddress(propertyToValMap.get("macAddress").iterator().next());
			inatfsObj.setInterfaceType(propertyToValMap.get("interfaceType").iterator().next());
			inatfsObj.setSourceNetwork(propertyToValMap.get("sourceNetwork").iterator().next());
			inatfsObj.setNumPorts(Integer.parseInt(propertyToValMap.get("numPorts").iterator().next()));
			inatfsObj.setTotalPorts(Integer.parseInt(propertyToValMap.get("totalPorts").iterator().next()));
			inatfsObj.setIpAddress(propertyToValMap.get("ipAddress").iterator().next());
			inatfsObj.setVirtualPortType(propertyToValMap.get("virtualPortType").iterator().next());
			inatfsObj.setModelType(propertyToValMap.get("model").iterator().next());
			Collection<String> propertyValCollection = propertyToValMap.get("connectedToIntfs");
			for(String connectedIntfs : propertyValCollection)
				inatfsObj.setConnectedIntfsURI(connectedIntfs);
			
			intfsIdMap.put(dpIntfsURI, inatfsObj);
		}
		
		// Creating Datapath objects and populating
		HashMap<String, Datapath> dpIdToObjMap = new HashMap<String, Datapath>();
		for(String dpURI : dpPropertyMap.keySet()){
			Datapath dpObj = new Datapath();
			Multimap<String, String> propertyToValMap = dpPropertyMap.get(dpURI);
			dpObj.setResourceURI(dpURI);
			dpObj.setResourceName(propertyToValMap.get("resourceName").iterator().next());
			dpObj.setResLocation(propertyToValMap.get("resLocation").iterator().next());
			dpObj.setResModel(propertyToValMap.get("resModel").iterator().next());
			dpObj.setSwitchNum(Integer.parseInt(propertyToValMap.get("resIdNum").iterator().next()));
			Collection<String> intfsCollection = propertyToValMap.get("hasInterface");
			Intfs[] intfsObjArray = new Intfs[intfsCollection.size()];
			for(int i = 0; i<intfsCollection.size(); i++){
				intfsObjArray[i] = intfsIdMap.get(intfsCollection.iterator().next());
			}
			dpObj.setIntfsObjArray(intfsObjArray);
			dpObj.setDpid(intfsObjArray[0].getMacAddress());
			dpIdToObjMap.put(dpURI, dpObj);
		}

		
		return dpIdToObjMap;
	}
	
	
	private static List<String> getAvailableControllers(){
		List<String> availableOpenflowCtrlList = new ArrayList<String>();
		
		//TODO: Put available controllers into Pr_Pool
		//TODO: Query for available controllers from Pr_Pool
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6633");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6633");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6633");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6634");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6634");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6634");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6635");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6635");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6635");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6636");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6636");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6636");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6637");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6637");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6637");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6638");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6638");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6638");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6639");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6639");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6639");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6640");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6640");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6640");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6641");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6641");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6641");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6642");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6642");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6642");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6643");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6643");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6643");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6644");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6644");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6644");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6645");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6645");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6645");
		availableOpenflowCtrlList.add("tcp:"+CTRL1_IP+":6646");
		availableOpenflowCtrlList.add("tcp:"+CTRL2_IP+":6646");
		availableOpenflowCtrlList.add("tcp:"+CTRL3_IP+":6646");
		
		return availableOpenflowCtrlList;
	}
	
	
	// ============================================================================================================================== //
	// ------------------------------------------------------- randomMACAddress() --------------------------------------------------- //
	// ============================================================================================================================== //
	private static String randomMACAddress(){
	    Random rand = new Random();
	    byte[] macAddr = new byte[6];
	    rand.nextBytes(macAddr);
	    macAddr[0] = (byte)(macAddr[0] & (byte)254);  //zeroing last 2 bytes to make it unicast and locally adminstrated
	    StringBuilder stringBuffer = new StringBuilder(18);
	    for(byte b : macAddr){
	        if(stringBuffer.length() > 0)
	            stringBuffer.append(":");
	        stringBuffer.append(String.format("%02x", b));
	    }
	    return stringBuffer.toString();
	}
	
	
	// ============================================================================================================================== //
		// --------------------------------------------------- createDiskImgInRemoteHost() ---------------------------------------------- //
		//	Jsch (java secure channel) is used here to connect remote kvm host and create sparse file to use as disk image for new VM	  //
		//	For more information, http://www.jcraft.com/jsch/																			  //
		// ============================================================================================================================== //
		public static void execCmdRemoteHost(String remoteHostIp, List<String> remoteExecCmdList){

			//String remoteExecTime = "";
			
			
			try{
				//long connectST = System.currentTimeMillis();
				java.util.Properties config = new java.util.Properties(); 
				config.put("StrictHostKeyChecking", "no");
				JSch jsch = new JSch();
				Session session=jsch.getSession(VMHOST_UNAME, remoteHostIp, 22);
				session.setPassword(VMHOST_PWD);
				session.setConfig(config);
				session.connect();
				System.out.println("Connected");

				Channel channel=session.openChannel("exec");
				String commandToExecute = "";
				for (String eachCmd: remoteExecCmdList){
					commandToExecute +=eachCmd+"\n";
				}
				//long connectET = System.currentTimeMillis();
				
				System.out.println("command = "+commandToExecute);
				
				//long commandST = System.currentTimeMillis();
				((ChannelExec)channel).setCommand(commandToExecute);
				channel.setInputStream(null);
				((ChannelExec)channel).setErrStream(System.err);

				InputStream in=channel.getInputStream();
				OutputStream out=channel.getOutputStream();
				channel.connect();
				out.write((VMHOST_SUPWD+"\n").getBytes());
				out.flush();
				//long commandET = System.currentTimeMillis();
				
				byte[] tmp=new byte[1024];
				while(true){
					while(in.available()>0){
						int i=in.read(tmp, 0, 1024);
						if(i<0)break;
						System.out.print(new String(tmp, 0, i));
					}
					if(channel.isClosed()){
						System.out.println("exit-status: "+channel.getExitStatus());
						break;
					}
					try{Thread.sleep(1000);}catch(Exception ee){}
				}
				channel.disconnect();

				session.disconnect();
				System.out.println("DONE");
				
				//remoteExecTime = Long.toString(connectET-connectST)+"\t"+Long.toString(commandET-commandST);
				
			}catch(Exception e){
				e.printStackTrace();
			}
			//return remoteExecTime;
		}
	
	
	
	
	// ============================================================================================================================== //
	// ----------------------------------------------------- createDomainXmlForVM() ------------------------------------------------- //
	// ============================================================================================================================== //
	
	public static String createDomainXmlForVM(VM vmObj, int targetVlan, String diskImageFile, Document domainDoc) {
		
		String vmHost = vmObj.getResLocation();	
		new File(DOMAINXML_FLDR+vmHost).mkdir();
		String saveToDomainXml = DOMAINXML_FLDR+vmHost+"/"+vmObj.getResourceURI()+".xml";
		
		domainDoc.getElementsByTagName("name").item(0).setTextContent(vmObj.getResourceName());
		domainDoc.getElementsByTagName("uuid").item(0).setTextContent(vmObj.getUuid());
		domainDoc.getElementsByTagName("memory").item(0).setTextContent(Long.toString(vmObj.getMaxMemory()));
		domainDoc.getElementsByTagName("currentMemory").item(0).setTextContent(Long.toString(vmObj.getCurrentMemory()));
		domainDoc.getElementsByTagName("vcpu").item(0).setTextContent(Integer.toString(vmObj.getMaxCpus()));
		
		// set cdrom as boot device
		Element os0Element = (Element) domainDoc.getElementsByTagName("os").item(0);
		Element boot0Element = (Element) os0Element.getElementsByTagName("boot").item(0);
		boot0Element.setAttribute("dev", "cdrom");
		
		// set disk type file and set source to disk image file
		Element devices0Element = (Element) domainDoc.getElementsByTagName("devices").item(0);
		Element disk0Element = (Element)devices0Element.getElementsByTagName("disk").item(0);
		disk0Element.setAttribute("type", "file");
		Element source0Element = (Element)disk0Element.getElementsByTagName("source").item(0);
		if(source0Element.hasAttribute("dev")) source0Element.removeAttribute("dev");
		source0Element.setAttribute("file", diskImageFile);
		
		// set bootable .iso image to cdrom
		Element disk1Element = (Element)devices0Element.getElementsByTagName("disk").item(1);
		disk1Element.setAttribute("type", "file");
		Element disk1source0Element = (Element)disk1Element.getElementsByTagName("source").item(0);
		disk1source0Element.setAttribute("file", LUBUNTU_ISO);
		
		// set randomly generated MAC address
		Element interface0Element = (Element)devices0Element.getElementsByTagName("interface").item(0);
		Element mac0Element = (Element)interface0Element.getElementsByTagName("mac").item(0);
		mac0Element.setAttribute("address", vmObj.getIntfsObjArray()[0].getMacAddress());
		Element target0Element = (Element)interface0Element.getElementsByTagName("target").item(0);
		target0Element.setAttribute("dev", vmObj.getIntfsObjArray()[0].getTargetOvsIntfs());
		
		//vlan tagging -- fail-safe in the absence of tagging controller
		Element vlan0Element = (Element)interface0Element.getElementsByTagName("vlan").item(0);
		Element tag0Element = (Element)vlan0Element.getElementsByTagName("tag").item(0);
		tag0Element.setAttribute("id", Integer.toString(targetVlan));
		
		
		// limit bandwidth consumption in VMs -- this can be set to network bandwidth ceiling
		/*<interface type='network'>
		    <source network='default'/>
		    <target dev='vnet0'/>
		    <bandwidth>
		      <inbound average='1000' peak='5000' floor='200' burst='1024'/>
		      <outbound average='128' peak='256' burst='256'/>
		    </bandwidth>
		  </interface>
		*/

		// create String domainXml and write the DOM object to a file
		String domainXml = null;
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		try {
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(domainDoc);
			StreamResult streamResult = new StreamResult(new File(saveToDomainXml));
			transformer.transform(domSource, streamResult);														// print domainXml into file 
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(domSource, new StreamResult(writer));
			domainXml = writer.getBuffer().toString();
		} catch (TransformerException tfe) {
			tfe.printStackTrace();
		} 
		return domainXml;
	}
	
	// ============================================================================================================================== //
	// -------------------------------------------------------- changeVResState() --------------------------------------------------- //
	// ============================================================================================================================== //
	public static void changeVResState(Map<String, String> allocVResMap){
		
		System.out.println("Changing states of resources in VR_Pool");
		
		ObjectProperty VR_status = vrPoolModel.getObjectProperty(VRPOOL_NS + "has_state");
		ObjectProperty isRsvdResof = vrPoolModel.getObjectProperty(VRPOOL_NS + "isRsvdResof");
		ObjectProperty hasRsvdRes = vrPoolModel.getObjectProperty(VRPOOL_NS + "hasRsvdRes");
		ObjectProperty isAllocResOf = vrPoolModel.getObjectProperty(VRPOOL_NS + "isAllocResOf");
		ObjectProperty hasAllocRes = vrPoolModel.getObjectProperty(VRPOOL_NS + "hasAllocRes");
		Individual reservedIndividual = vrPoolModel.getIndividual(VRPOOL_NS + "reserved");
		Individual allocatedIndividual = vrPoolModel.getIndividual(VRPOOL_NS + "allocated1");
		

		
		for(String vRes : allocVResMap.keySet()){
			System.out.println("state of "+vRes+" changed to \"allocated\"");
			Individual vResIndividual = vrPoolModel.getIndividual(VRPOOL_NS + vRes);
			vResIndividual.removeProperty(VR_status, reservedIndividual);
			vResIndividual.addProperty(VR_status, allocatedIndividual);
			
			Individual reqIndividual = vrPoolModel.getIndividual(VRPOOL_NS + allocVResMap.get(vRes));
			vResIndividual.removeProperty(isRsvdResof, reqIndividual);
			vResIndividual.addProperty(isAllocResOf, reqIndividual);
			reqIndividual.removeProperty(hasRsvdRes, vResIndividual);
			reqIndividual.addProperty(hasAllocRes, vResIndividual);
		}
		
		try {
			PrintStream printStream = new PrintStream(VRPOOL);
			vrPoolModel.write(printStream, "RDF/XML");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
		


}