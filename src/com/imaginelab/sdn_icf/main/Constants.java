package com.imaginelab.sdn_icf.main;

public class Constants {
/* ======================================================================= Input Constants ========================================================================== */
	
	public static final String FV_IP 				= "192.168.0.200";
	public static final String CTRL1_IP 			= "192.168.0.201";
	public static final String CTRL2_IP 			= "192.168.0.202";
	public static final String CTRL3_IP 			= "192.168.0.203";
	// Use following commands to automate ssh access (password-less) by putting public-key into VM host.  
	// 	man-user@man-host$ 	ssh-keygen -t rsa
	// 	man-user@man-host$ 	ssh kvm-user@kvm-host mkdir -p .ssh
	// 	man-user@man-host$ 	cat .ssh/id_rsa.pub | ssh kvm-user@kvm-host 'cat >> .ssh/authorized_keys'
/*	public static final String KVM1_IP 				= "192.168.0.221";
	public static final String KVM2_IP 				= "192.168.0.222";
	public static final String KVM3_IP 				= "192.168.0.223";
	public static final String KVM4_IP 				= "192.168.0.224";
	public static final String KVM5_IP 				= "192.168.0.225";
	public static final String KVM6_IP 				= "192.168.0.226";
	public static final String KVM7_IP 				= "192.168.0.227";
	public static final String KVM8_IP 				= "192.168.0.228";
	public static final String KVM9_IP 				= "192.168.0.229";*/
	public static final String KVM1_IP 				= "192.168.0.101";
	public static final String KVM2_IP 				= "192.168.0.102";
	public static final String KVM3_IP 				= "192.168.0.103";
	public static final String KVM4_IP 				= "192.168.0.104";
	public static final String KVM5_IP 				= "192.168.0.105";
	public static final String KVM6_IP 				= "192.168.0.106";
	public static final String KVM7_IP 				= "192.168.0.107";
	public static final String KVM8_IP 				= "192.168.0.108";
	public static final String KVM9_IP 				= "192.168.0.109";
	public static final String KVM10_IP 			= "192.168.0.110";
	public static final String KVM11_IP 			= "192.168.0.111";
	public static final String KVM12_IP 			= "192.168.0.112";
	public static final String KVM13_IP 			= "192.168.0.113";
	public static final String KVM14_IP 			= "192.168.0.114";
	public static final String KVM15_IP 			= "192.168.0.115";
	public static final String KVM16_IP 			= "192.168.0.116";
	public static final String KVM17_IP 			= "192.168.0.117";
	public static final String KVM18_IP 			= "192.168.0.118";
	public static final String KVM19_IP 			= "192.168.0.119";
	public static final String KVM20_IP 			= "192.168.0.120";
	public static final String KVM21_IP 			= "192.168.0.121";
	public static final String KVM22_IP 			= "192.168.0.122";
	public static final String KVM23_IP 			= "192.168.0.123";
	public static final String KVM24_IP 			= "192.168.0.124";
	public static final String KVM25_IP 			= "192.168.0.125";
	public static final String KVM26_IP 			= "192.168.0.126";
	public static final String KVM27_IP 			= "192.168.0.127";
	public static final String KVM28_IP 			= "192.168.0.128";
	public static final String KVM29_IP 			= "192.168.0.129";
	public static final String KVM30_IP 			= "192.168.0.130";
	public static final String KVM31_IP 			= "192.168.0.131";
	public static final String KVM32_IP 			= "192.168.0.132";

	
	// ssh access to create disk image file in "/var/lib/libvirt/images/" is not automated
	public static final String VMHOST_UNAME 		= "openstack";
	public static final String VMHOST_PWD 			= "Folsom3.0";
	public static final String VMHOST_SUPWD			= "Folsom3.0";
	public static final String DISK_IMG_FLDR		= "/var/lib/libvirt/images/";
	public static final String UBUNTU_ISO			= "/var/lib/libvirt/images/ubuntu-14.04-server-amd64.iso";
	public static final String TINYCORE_ISO			= "/var/lib/libvirt/images/TinyCore-current.iso";
	public static final String LUBUNTU_ISO			= "/var/lib/libvirt/images/lubuntu-14.04.2-desktop-i386.iso";				// Runs in live mode without installing OS
	
	public static final String FV_URI 				= "https://"+FV_IP+":8081";
	public static final String PRPOOL 				= "src/DataFiles/PrPool/physicalNet2.owl";
	public static final String PRPOOL_EMPTY 		= "src/DataFiles/PrPool/physicalNet2_empty.owl";
	public static final String VRPOOL 				= "src/DataFiles/VrPool/VrPool.owl";
	public static final String VRPOOL_EMPTY 		= "src/DataFiles/VrPool/VrPool_Empty_Structure.owl";						// Input template to replace before Construct VrPool
	public static final String VRPOOL_FREE	 		= "src/DataFiles/VrPool/VrPool_States_Free.owl";							// Input template to replace before Infrastructure Composer
	public static final String VRPOOL_RSVD	 		= "src/DataFiles/VrPool/VrPool_States_Rsvd.owl";							// Input template to replace before Implementation
	public static final String PRPOOL_NS 			= "http://www.semanticweb.org/root/ontologies/2014/9/physicalNetOnt#";
	public static final String VRPOOL_NS 			= "http://www.semanticweb.org/kmetw028/ontologies/2013/11/untitled-ontology-52#";
	
	public static final String HYPER_1 				= "qemu+ssh://openstack@"+KVM1_IP+"/system";
	public static final String HYPER_2 				= "qemu+ssh://openstack@"+KVM2_IP+"/system";
	public static final String HYPER_3 				= "qemu+ssh://openstack@"+KVM3_IP+"/system";
	public static final String HYPER_4 				= "qemu+ssh://openstack@"+KVM4_IP+"/system";
	public static final String HYPER_5 				= "qemu+ssh://openstack@"+KVM5_IP+"/system";
	public static final String HYPER_6 				= "qemu+ssh://openstack@"+KVM6_IP+"/system";
	public static final String HYPER_7 				= "qemu+ssh://openstack@"+KVM7_IP+"/system";
	public static final String HYPER_8 				= "qemu+ssh://openstack@"+KVM8_IP+"/system";
	public static final String HYPER_9 				= "qemu+ssh://openstack@"+KVM9_IP+"/system";
	public static final String HYPER_10 			= "qemu+ssh://openstack@"+KVM10_IP+"/system";
	public static final String HYPER_11 			= "qemu+ssh://openstack@"+KVM11_IP+"/system";
	public static final String HYPER_12 			= "qemu+ssh://openstack@"+KVM12_IP+"/system";
	public static final String HYPER_13 			= "qemu+ssh://openstack@"+KVM13_IP+"/system";
	public static final String HYPER_14 			= "qemu+ssh://openstack@"+KVM14_IP+"/system";
	public static final String HYPER_15 			= "qemu+ssh://openstack@"+KVM15_IP+"/system";
	public static final String HYPER_16 			= "qemu+ssh://openstack@"+KVM16_IP+"/system";
	public static final String HYPER_17 			= "qemu+ssh://openstack@"+KVM17_IP+"/system";
	public static final String HYPER_18 			= "qemu+ssh://openstack@"+KVM18_IP+"/system";
	public static final String HYPER_19 			= "qemu+ssh://openstack@"+KVM19_IP+"/system";
	public static final String HYPER_20 			= "qemu+ssh://openstack@"+KVM20_IP+"/system";
	public static final String HYPER_21 			= "qemu+ssh://openstack@"+KVM21_IP+"/system";
	public static final String HYPER_22 			= "qemu+ssh://openstack@"+KVM22_IP+"/system";
	public static final String HYPER_23 			= "qemu+ssh://openstack@"+KVM23_IP+"/system";
	public static final String HYPER_24 			= "qemu+ssh://openstack@"+KVM24_IP+"/system";
	public static final String HYPER_25 			= "qemu+ssh://openstack@"+KVM25_IP+"/system";
	public static final String HYPER_26 			= "qemu+ssh://openstack@"+KVM26_IP+"/system";
	public static final String HYPER_27 			= "qemu+ssh://openstack@"+KVM27_IP+"/system";
	public static final String HYPER_28 			= "qemu+ssh://openstack@"+KVM28_IP+"/system";
	public static final String HYPER_29 			= "qemu+ssh://openstack@"+KVM29_IP+"/system";
	public static final String HYPER_30 			= "qemu+ssh://openstack@"+KVM30_IP+"/system";
	public static final String HYPER_31 			= "qemu+ssh://openstack@"+KVM31_IP+"/system";
	public static final String HYPER_32 			= "qemu+ssh://openstack@"+KVM32_IP+"/system";
	public static final boolean ACTIVEVM 			= true;									// Flag used to distinguish the VM state at the Domain xml parser
	
	
	/* ====================================================== Constants used in ConstructVrPool and Composer ======================================================== */
	public static final String NL      				= System.getProperty("line.separator") ; 
	public static final String PRPOOL_QUERY_PREFIX 	= 	"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"+ NL + 
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+ NL + 
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+ NL + 
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+ NL + 
			"PREFIX prpool: <http://www.semanticweb.org/root/ontologies/2014/9/physicalNetOnt#>";
	
	public static final String VRPOOL_QUERY_PREFIX 	= "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" +
			"PREFIX owl: <http://www.w3.org/2002/07/owl#>"+
			"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"+
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
			"PREFIX cloud_ont: <http://www.semanticweb.org/kmetw028/ontologies/2013/11/untitled-ontology-52#>";
	
	public static final int INFINITY 				= 9999;
	//public static final String CONNECTIVITY_FILE 	= "src/DataFiles/connectivity.txt";
	public static final String CLOSENESS_FILE 		= "src/DataFiles/closeness.txt";
	public static final String LINKVR_SERFILE 		= "src/DataFiles/VLinks.ser";
	
	/* ================================================================== Request Options ========================================================================= */
	public static final boolean SINGLE_TEST_REQ 	= false;
	public static final int NUM_OF_REQ 				= 18;
	public static final int VMS_PER_REQ 			= 5;
	//public static final String NUM_OF_REQ 				= 150;
	
 	public static final String REQ_TXT_FOLDR 		= "src/DataFiles/Requests/"+VMS_PER_REQ+"ReqTxt";
	public static final String REQ_SER_FOLDR 		= "src/DataFiles/Requests/"+VMS_PER_REQ+"ReqSer";
	public static final String REQ_SNET_FOLDR 		= "src/DataFiles/Requests/"+VMS_PER_REQ+"ReqSNet";
	
	public static final String TEST_REQ_TXTF 		= "src/DataFiles/Requests/"+VMS_PER_REQ+"ReqTxt";
	public static final String TEST_REQ_SERF 		= "src/DataFiles/Requests/"+VMS_PER_REQ+"ReqSer";
	public static final String TEST_REQ_SNETF 		= "src/DataFiles/Requests/"+VMS_PER_REQ+"ReqSNet";
	

	
	public static final String ACPT_REQ_FOLDR 		= "src/DataFiles/Requests/AcceptedReq";
	
	public static final String DOMAINXML_FLDR   	= "src/DataFiles/DomainXMLs/";
	public static final String DOMAINXML_TYPE_A 	= "Type-A_domainXml.xml";
	public static final String DOMAINXML_TYPE_B 	= "Type-B_domainXml.xml";
	public static final String DOMAINXML_TYPE_C 	= "Type-C_domainXml.xml";
	
	public static final String UDLINK_PRFX 			= "UDL";
	public static final String BDLINK_PRFX 			= "BDL";
	public static final String HOST_PRFX 			= "H";
	public static final String SWCH_PRFX 			= "S";
	public static final String REQ_PRFX 			= "REQ";
	
	public static final int VLAN_ID_FLOOR 			= 1000;
	
/* =============================================================== Physical resource Over-subscription ============================================================== */
	
	
	
	public static final double CPU_OVRSUB_FACTOR 	= 2.0;							//3		// Physical VM-host CPU over-subscription factor max = 4
	public static final boolean SET_MIN_CORES_ONE 	= true;
	
	public static final double MEM_OVRSUB_FACTOR 	= 2.0;							//2		// Physical VM-host Memory over-subscription factor max = 2
	public static final double STR_OVRSUB_FACTOR 	= 2.0;							//2		// Physical VM-host Storage over-subscription factor max = 2
	public static final double BW_OVRSUB_FACTOR 	= 16.0;							//16	// Physical link bandwidth over-subscription factor max; access = 8, agg = 16, core = 32
	
/* =================================================================== Constants used for testing =================================================================== */
	public static final boolean ALL_CLASS_C 		= true;
	public static final boolean STATIC_HOST_CLASS 	= false;								// if true; NodeClass of kvm-host3 = A, kvm-host2 = B, kvm-host1 = C, kvm-host4 = C
	public static final boolean MININET_VBOX_HOSTS 	= false;									// else if true; Mininet emulated network will be used with virtualbox created VMs as hosts
																							// else both false; NodeClss is assigned based of totalMemory
	
	
	
	public static final int PHY_HOST_ID_MIN 		= 1001;
	public static final int PHY_HOST_ID_MAX 		= 9999;
	

	
/* ======================================================================== Output Constants ======================================================================== */
	
	public static final int OK = 200;
	public static final int BAD_REQUEST = 400;
	
/* ====================================================================== Operational Switches ====================================================================== */
	
	/*-------------------------------------------------- Main Switches ---------------------------------------------*/
	public static final boolean TEST_CONNECTIVITY 	= false;									/* Sub-Module I		*/
	public static final boolean GEN_IC_REQ			= false; 									/* Sub-Module II	*/
	
	public static final boolean CONSTRUCT_PRPOOL 	= false; 									/* Module 1			*/
	//public static final boolean CONST_PR_MININET 	= false;
	public static final boolean CONSTRUCT_VRPOOL 	= false; 									/* Module 2			*/
	public static final boolean COMPOSITION 		= true; 									/* Module 3			*/
	
	public static final boolean DEPLOYMENT_PLAN 	= false; 									/* Module 4			*/
	public static final boolean COMMIT_DEPLOYMENT	= false;

	/*-------------------------------------------------- Main Switches ---------------------------------------------*/
	
	
	public static final boolean RMVE_INDVL_PRPOOL 	= true;									// Needs  CONSTRUCT_PRPOOL  to be "true"
	public static final boolean RMVE_INDVL_VRPOOL 	= true;									// Needs  CONSTRUCT_VRPOOL  to be "true"
	public static final boolean VRPOOL_RPLCE_FREE  	= true;
	public static final boolean VRPOOL_RPLCE_RSVD  	= true;
	
	public static final boolean READ_PHYSICAL_NET 	= true;									// Make flowvisor JSON requests to populate Switch and Link object arrays
	public static final boolean READ_PHYSICAL_IT 	= true;									// Make libvirt API calls to populate VmHost object array
	public static final boolean DOMAIN_XML_TO_FILE 	= true;
	
	public static final boolean UPDATE_OWL_NET 		= true;									// Update Physical Resource Ontology (PrPool)  with data stored in Switch and Link object arrays
	public static final boolean UPDATE_OWL_IT 		= true;									// Update Physical Resource Ontology (PrPool)  with data stored in VmHost object array
	
	public static final boolean GET_CONFIG			= false;								// Get current FlowVisor Configurations
	public static final boolean SET_CONFIG			= false;								// Send configurations to FlowVisor
	
	
	
/* ========================================================================== Debug Switches ======================================================================== */
	
	//	READ_PHYSICAL_NET must be true 
	public static final boolean SWITCH_DBG 			= false;			
	public static final boolean LINK_DBG 			= false;				 		
	public static final boolean FVCALL_DBG 			= false;			    
	
	//	READ_PHYSICAL_IT must be true
	public static final boolean PRINT_VM_HOST 		= false;
	public static final boolean HOST_DBG 			= false;
	public static final boolean VM_DBG 				= false;
	
	public static final boolean INTFS_CONNECT_DBG 	= false;
	
	public static final boolean PRPOOL_UPDATE_DBG 	= false;
	public static final boolean PRPOOL_READ_DBG 	= false;
	public static final boolean VRPOOL_CONST_DBG 	= false;
	public static final boolean VRPOOL_UPDATE_DBG 	= false;
	
	public static final boolean COMPO_INIT_DBG 		= false;
	public static final boolean COMPO_EVENT_MAN_DBG = false;
	public static final boolean DISCVR_ALGO_DBG 	= false;
	public static final boolean COMPOS_ALGO_DBG 	= false;
	
	
	public static final boolean REQ_IMPL_DBG 		= true;
	public static final boolean BUILD_SLICE_DBG 	= false;
	public static final boolean SLICE_CREATE_CHECK 	= false;
	
/* ====================================================================== Log Performance Analysis ===================================================================== */
	public static final boolean LOG_PERFORMANCE 	= true;
	
	public static final String BLDPR_PERF_FILE	=  "src/DataFiles/Results/buildPrPool.txt";	// Build PR_POOL performance analysis result file
	public static final String BLDVR_PERF_FILE	=  "src/DataFiles/Results/buildVrPool.txt";	// Build VR_POOL performance analysis result file
	public static final String COMPO_PERF_FILE	=  "src/DataFiles/Results/composition.txt";	// Composition performance analysis result file
	public static final String IMPLE_PERF_FILE	=  "src/DataFiles/Results/implementation.txt";	// Implementation performance analysis result file
}
