package com.imaginelab.sdn_icf.compose;

import java.util.ArrayList;
import java.util.HashMap;

import com.imaginelab.sdn_icf.containers.VResource;

public class CompositionResult {
	private HashMap <String, HashMap <String, VResource>> partitionedLinkVResForReq;
	private ArrayList<VResource> reservedVResListForReq;
	
	public HashMap <String, HashMap <String, VResource>> getPartitionedLinkVResForReq() {
		return partitionedLinkVResForReq;
	}
	public void setPartitionedLinkVResForReq(
			HashMap <String, HashMap <String, VResource>> partitionedLinkVResForReq) {
		this.partitionedLinkVResForReq = partitionedLinkVResForReq;
	}
	public ArrayList<VResource> getreservedVResListForReq() {
		return reservedVResListForReq;
	}
	public void setReservedVResListForReq(ArrayList<VResource> reservedVResListForReq) {
		this.reservedVResListForReq = reservedVResListForReq;
	}
	
	
}
