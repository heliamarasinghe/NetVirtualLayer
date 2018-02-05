/*
 resource_id
 resource_name
 port_buffer
 port_number
 */

package com.imaginelab.sdn_icf.containers;

public class Port {
	
	private String resourceURI = "Empty_From_Container";		// For IT resources = H-0_VM-0_I-0_P-0 , For NET resources = S-0_I-0_P-0 
	private String resourcetName = "none";
	private double portBuffer = 0.1;
	private int portNumber = 0;
	
///===========================================================================================================
///---------------------------------------- getters and setters ----------------------------------------------
///===========================================================================================================
	public String getResourceURI() {
		return resourceURI;
	}
	public void setResourceURI(String resourceURI) {
		this.resourceURI = resourceURI;
	}
	public String getResourceName() {
		return resourcetName;
	}
	public void setResourceName(String resourcetName) {
		this.resourcetName = resourcetName;
	}
	public double getPortBuffer() {
		return portBuffer;
	}
	public void setPortBuffer(double portBuffer) {
		this.portBuffer = portBuffer;
	}
	public int getPortNumber() {
		return portNumber;
	}
	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

}
