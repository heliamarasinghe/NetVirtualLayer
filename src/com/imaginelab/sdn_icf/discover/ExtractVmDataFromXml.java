package com.imaginelab.sdn_icf.discover;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.imaginelab.sdn_icf.containers.Intfs;
import com.imaginelab.sdn_icf.containers.Port;
import com.imaginelab.sdn_icf.containers.VM;

public class ExtractVmDataFromXml extends DefaultHandler{

	boolean isMac = false;
	boolean isVmName = false;
	boolean isUuid = false;
	boolean isMaxMemory = false;
	boolean isCurrentMemory = false;
	boolean isVCpu = false;
	boolean isInterface = false;

	private VM vmObj = null;
	private List <Intfs> intfsList = null;
	Intfs intfsobj = null;

	boolean isActiveVM = false;
	String vmResourceURI = "vmUriFrmParser";
	String vmResourceName = "vmNameFrmParser";
	int numPorts = 1;																// numPorts = 1 for each interface
	int intfsItr = 0;	
	int startCounter = 0;

	public ExtractVmDataFromXml(String vmResourceURI, boolean isActiveVM){
		super();
		this.vmResourceURI = vmResourceURI;
		this.isActiveVM = isActiveVM;
	}

	// startDocument() called when the parser started reading the document
	@Override
	public void startDocument() throws SAXException {
		vmObj = new VM();
		intfsList = new ArrayList<Intfs>();
	}	

	// startElement() called every time when the parser detect starting xml tag <...>
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		if(qName.equals("domain")) {
			vmObj.setResourceURI(vmResourceURI);
			vmObj.setVmType(atts.getValue(0));																					// vmType
			if(isActiveVM) vmObj.setVmId(Integer.parseInt(atts.getValue(1)));													// vmId
			else vmObj.setVmId(-1);
		}
		if(qName.equals("name")) isVmName = true;
		if(qName.equals("uuid")) isUuid = true;
		if(qName.equals("memory")) isMaxMemory = true;
		if(qName.equals("currentMemory")) isCurrentMemory = true;
		if(qName.equals("vcpu")) isVCpu = true;
		if(qName.equals("type")) {
			vmObj.setCpuArch(atts.getValue(0));																					// cupArch
			vmObj.setMachineOs(atts.getValue(1));																				// machineOs
		}
		if(qName.equals("interface")){
			String intfsResourceURI = vmResourceURI+"_I-"+intfsItr;
			intfsobj = new Intfs(intfsResourceURI);
			String intfsResourceName = vmResourceName+"_Intfs_"+intfsItr;
			//intfsobj.setResourceURI(intfsResourceURI);
			intfsobj.setResourceName(intfsResourceName);
			intfsobj.setInterfaceType(atts.getValue(0));
			intfsobj.setNumPorts(numPorts);
			Port[] portObjArray = new Port[numPorts];
			portObjArray[0] = new Port();
			String portResourceURI = intfsResourceURI+"_P-0";
			String portResourceName = intfsResourceName+"_Port";
			portObjArray[0].setResourceURI(portResourceURI);
			portObjArray[0].setResourceName(portResourceName);
			portObjArray[0].setPortNumber(0);
			intfsobj.setPortObjArray(portObjArray);
			isInterface = true;
		}
		if(qName.equals("mac"))						intfsobj.setMacAddress(atts.getValue(0)); 
		if(qName.equals("virtualport"))				intfsobj.setVirtualPortType(atts.getValue(0)); // "virtualport" only for defined VMs
		if(qName.equals("model"))					intfsobj.setModelType(atts.getValue(0)); // "model" only for active VMs. ex: virtio	
		if(qName.equals("source") && isInterface)	intfsobj.setSourceNetwork(atts.getValue(0)); 
	}

	// endElement() called every time when the parser detect ending xml tag </...>
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equals("name")) isVmName = false;
		if(qName.equals("uuid")) isUuid = false;
		if(qName.equals("memory")) isMaxMemory = false;
		if(qName.equals("currentMemory")) isCurrentMemory = false;
		if(qName.equals("vcpu")) isVCpu = false;
		if(qName.equals("interface")){
			isInterface = false;
			intfsList.add(intfsobj);
			intfsItr++;
		}     
	}

	// characters() called every time when the parser detect content in between xml tags <..> characters </...>
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (isVmName){
			vmResourceName = new String(ch, start, length);
			vmObj.setResourceName(vmResourceName); 																				// vmName
		}
		if (isUuid) vmObj.setUuid(new String(ch, start, length)); 																// uuid
		if (isMaxMemory) vmObj.setMaxMemory( Long.parseLong(new String(ch, start, length))/1024);								// maxMemory in Mb
		if (isCurrentMemory) vmObj.setCurrentMemory( Long.parseLong(new String(ch, start, length))/1024); 						// currentMemory in Mb
		if (isVCpu) vmObj.setvCpus(Integer.parseInt(new String(ch, start, length))); 											// vCpus
	}

	// endDocument() called when the parser reach end of the document
	@Override
	public void endDocument() throws SAXException {
		vmObj.setNumberOfIntfs(intfsItr);
		Intfs[] intfsObjArray = intfsList.toArray(new Intfs[intfsList.size()]);
		vmObj.setIntfsObjArray(intfsObjArray);
	}

	// getter to return data container
	public VM getVmObj(){
		return vmObj;
	}

}
