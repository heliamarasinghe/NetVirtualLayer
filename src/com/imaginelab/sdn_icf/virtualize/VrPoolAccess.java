package com.imaginelab.sdn_icf.virtualize;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Collection;

import com.google.common.collect.Multimap;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.imaginelab.sdn_icf.containers.VResource;

import static com.imaginelab.sdn_icf.main.Constants.VRPOOL;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_NS;
import static com.imaginelab.sdn_icf.main.Constants.VRPOOL_UPDATE_DBG;


public class VrPoolAccess {
	static OntModel virtResourcePoolModel;
	
	// ==========================================================================================================================================================
	// ------------------------------------------------------------------- updateVrPool() -----------------------------------------------------------------------
	// ==========================================================================================================================================================
	public static void updateVrPool(String[][] conn_matrix, Multimap<String, VResource> phyToVirtResMap) {
		virtResourcePoolModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM_RDFS_INF);
		try{
			virtResourcePoolModel.read(VRPOOL,"RDF/XML"); 
			addVResourceIndividuals(phyToVirtResMap);
			addConnectedToObjProperty(conn_matrix, phyToVirtResMap);
			PrintStream printStream = new PrintStream(VRPOOL);
			virtResourcePoolModel.write(printStream, "RDF/XML");
		}catch (FileNotFoundException no_file) {
			System.err.println("Unable to access ontology.");
			System.out.println("Make sure you have \""+VRPOOL+"\" and restart the program.");
			no_file.printStackTrace();
			System.exit(0);
		}catch (JenaException je) {
			System.err.println("JenaException has been generated at updateVrPool");
			je.printStackTrace();
		}catch (Exception e) {
			System.err.println("General Exception has been generated at updateVrPool");
			e.printStackTrace();
		}
	}

	// ==========================================================================================================================================================
	// --------------------------------------------------------------- addVResourceIndividuals() ----------------------------------------------------------------
	// DESCRIPTION:	First method invoked from ConstructVrPool.updateVrPool(), which adds virtual resources and their data and object properties to VR_Pool
	// ==========================================================================================================================================================
	public static void addVResourceIndividuals(Multimap<String, VResource> phyToVirtResMap) throws FileNotFoundException{
		OntClass VR_Cls = virtResourcePoolModel.getOntClass(VRPOOL_NS + "VR");
		ObjectProperty VR_status = virtResourcePoolModel.getObjectProperty(VRPOOL_NS + "has_state");
		//ObjectProperty connected_to = virtResourcePoolModel1.getObjectProperty(VR_POOL_NS + "connectedto");
		ObjectProperty provideservices = virtResourcePoolModel.getObjectProperty(VRPOOL_NS + "provideservice");
		ObjectProperty has_interface = virtResourcePoolModel.getObjectProperty(VRPOOL_NS + "hasinterface");
		ObjectProperty haspriority = virtResourcePoolModel.getObjectProperty(VRPOOL_NS + "haspriority");

		Individual free = virtResourcePoolModel.getIndividual(VRPOOL_NS + "free1");
		Individual service_compute = virtResourcePoolModel.getIndividual(VRPOOL_NS + "compute1");
		Individual service_storage = virtResourcePoolModel.getIndividual(VRPOOL_NS + "storage1");
		Individual service_switching = virtResourcePoolModel.getIndividual(VRPOOL_NS + "switching");
		Individual service_linking = virtResourcePoolModel.getIndividual(VRPOOL_NS + "linking");
		Individual priority_a = virtResourcePoolModel.getIndividual(VRPOOL_NS + "class_a");
		Individual priority_b = virtResourcePoolModel.getIndividual(VRPOOL_NS + "class_b");
		Individual priority_c = virtResourcePoolModel.getIndividual(VRPOOL_NS + "class_c");
		Individual interf_Eth = virtResourcePoolModel.getIndividual(VRPOOL_NS + "Ether1");

		//VResource vResource;
		//System.out.println("Adding individuals to VR_Pool");
		int numOfIndividuals = 0;
		for(String vResourceId : phyToVirtResMap.keySet()){
			Collection<VResource> vResCollection = phyToVirtResMap.get(vResourceId);
			if(VRPOOL_UPDATE_DBG) System.out.println("\nAdding VR's in "+vResourceId+" -->");
			for(VResource vResource : vResCollection){
				String VR_name=VRPOOL_NS +vResource.getResUri();   
				Individual new_VR = virtResourcePoolModel.createIndividual(VR_name, VR_Cls );
	
				// create datatype properties
				DatatypeProperty cpu = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "cpu" );
				DatatypeProperty memory = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "memory" );
				DatatypeProperty storage = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "storage" );
				DatatypeProperty bw = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "bw" );
				DatatypeProperty delay = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "delay" );
				DatatypeProperty loss_rate = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "loss_rate" );
				DatatypeProperty location = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "location" );
				DatatypeProperty n_cores = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "n_cores" );
				DatatypeProperty VMM = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "VMM" );
				DatatypeProperty res_id = virtResourcePoolModel.createDatatypeProperty( VRPOOL_NS + "res_id" );
	
				new_VR	.addProperty( cpu, virtResourcePoolModel.createTypedLiteral( vResource.getCpu() ) )
						.addProperty( memory, virtResourcePoolModel.createTypedLiteral( vResource.getMem() ) ) 
						.addProperty( storage, virtResourcePoolModel.createTypedLiteral(vResource.getStr() ) ) 
						.addProperty( bw, virtResourcePoolModel.createTypedLiteral( vResource.getBw()) ) 
						.addProperty( delay, virtResourcePoolModel.createTypedLiteral( vResource.getDelay() ) ) 
						.addProperty( loss_rate, virtResourcePoolModel.createTypedLiteral( vResource.getLoss_rate() ) ) 
						.addProperty( n_cores, virtResourcePoolModel.createTypedLiteral(vResource.getN_cores() ) ) 
						.addProperty( location, virtResourcePoolModel.createLiteral(vResource.getLoc() ) ) 
						.addProperty( VMM, virtResourcePoolModel.createLiteral(vResource.getVmm() ) ) 
						.addProperty( res_id, virtResourcePoolModel.createLiteral( vResource.getResUri() ) );
	
				new_VR.addProperty(VR_status,free);
				
				switch(vResource.getPriorityClass()){
					case "none": break;		// disregard priority class for switches and links
					case "class_a": new_VR.addProperty(haspriority, priority_a); break;
					case "class_b": new_VR.addProperty(haspriority, priority_b); break;
					case "class_c": new_VR.addProperty(haspriority, priority_c); break;
					default: System.err.println("VrPoolAccess.addVResourceIndividuals: Unrecognized priority class - "+vResource.getPriorityClass());
				}
				
				
				switch(vResource.getRes_type()){
					case "compute": 	new_VR.addProperty(provideservices, service_compute); 	break;
					case "storage": 	new_VR.addProperty(provideservices, service_storage); 	break;
					case "switching": 	new_VR.addProperty(provideservices, service_switching); break;
					case "linking": 	new_VR.addProperty(provideservices, service_linking); 	break;
					default: System.err.println("VrPoolAccess.addVResourceIndividuals: Unrecognized resource type - "+vResource.getRes_type());
				}
	
				new_VR.addProperty(has_interface,interf_Eth);
				if(VRPOOL_UPDATE_DBG) System.out.println("\t\t "+vResource.getResUri());
				numOfIndividuals++;
			}
		}
		System.out.println(numOfIndividuals+" Individuals added to VrPool");
	}
	
	// ==========================================================================================================================================================
	// ----------------------------------------------------------- addConnectedToObjProperty() ------------------------------------------------------------------
	// DESCRIPTION:	Second method invoked from ConstructVrPool.updateVrPool(), which adds "connectedto" object property to VR_Pool
	// ==========================================================================================================================================================
	public static void addConnectedToObjProperty(String[][] conn_matrix, Multimap<String, VResource> phyToVirtResMap) throws FileNotFoundException{
	
		
		ObjectProperty connected_to = virtResourcePoolModel.getObjectProperty(VRPOOL_NS + "connectedto");
		int conn_matSize = conn_matrix.length;
		for(int row = 1; row<conn_matSize; row++)
			for(int col = 1; col<conn_matSize; col++)
				if(!conn_matrix[row][col].equals("0")){
					String srcPhyResId = conn_matrix[row][0];
					String dstPhyResId = conn_matrix[0][col];
					Individual linkIndividual = null;
					Collection <VResource> linkVResCollection = phyToVirtResMap.get(conn_matrix[row][col]);
					//System.out.println("linkVResCollection.size = "+linkVResCollection.size());
					for(VResource linkVRes : linkVResCollection){
						linkIndividual = virtResourcePoolModel.getIndividual(VRPOOL_NS + linkVRes.getResUri());
						//System.out.println("linkIndividual = "+linkIndividual);
					}
					if(VRPOOL_UPDATE_DBG) System.out.println("\nlinkId = "+conn_matrix[row][col]+"\t srcPResId = "+srcPhyResId+"\t dstPResId = "+dstPhyResId);
					Collection <VResource> srcVResCollection = phyToVirtResMap.get(srcPhyResId);
					Collection <VResource> dstVResCollection = phyToVirtResMap.get(dstPhyResId);
					for(VResource srcVRes : srcVResCollection){
						Individual srcResIndividual = virtResourcePoolModel.getIndividual(VRPOOL_NS + srcVRes.getResUri());
						//System.out.println("srcResIndividual = "+srcResIndividual);
						for(VResource dstVRes : dstVResCollection){
							Individual dstResIndividual = virtResourcePoolModel.getIndividual(VRPOOL_NS + dstVRes.getResUri());
							//System.out.println("dstResIndividual = "+dstResIndividual);
							srcResIndividual.addProperty(connected_to, dstResIndividual);
							// TODO: Only "connected_to" property is insufficient to represent unidirectional links
							// Following lines will result in duplicated links due to impropper transformation of unidirectional to bidirectional 
							linkIndividual.addProperty(connected_to, srcResIndividual);		
							linkIndividual.addProperty(connected_to, dstResIndividual);
							
							if(VRPOOL_UPDATE_DBG)
								System.out.println("\tUnidirectional link from \""+srcVRes.getResUri()+"\" to \""+dstVRes.getResUri()+"\" added via connectedto object property");
						}
					}
				}
		
		System.out.println("VNode connectivity information added to VR_Pool");
	}	
	
	
	
	
	
}
