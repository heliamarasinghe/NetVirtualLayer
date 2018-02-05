package com.imaginelab.sdn_icf.discover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.security.cert.X509Certificate;

import static com.imaginelab.sdn_icf.main.Constants.*;


public class FlowvisorApiCall {
	
	
	private String uri;
	private String fvResponse;
	private HttpURLConnection connection = null;
	
	public String getFvResponse() {
		return fvResponse;
	}

	public void setFvResponse(String fvResponse) {
		this.fvResponse = fvResponse;
	}
	
///===========================================================================================================
///------------------------------------------ Constructors --------------------------------------------------
///===========================================================================================================
	public FlowvisorApiCall(String flowvisorUri){
		this.uri = flowvisorUri;
		ignoreCervificates();
		fvPasswordAuthentication();
	}
///===========================================================================================================	
///---------------------------------------- ignoreCervificates() ---------------------------------------------
///===========================================================================================================
	private static void ignoreCervificates(){
		try {
			if (FVCALL_DBG) System.out.println("Create a trust manager that does not validate certificate chains");
			TrustManager[] trustAllCerts = new TrustManager[] {
				new X509TrustManager() {
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
						return null;
					}
					public void checkClientTrusted(X509Certificate[] certs, String authType) {
					}
					public void checkServerTrusted(X509Certificate[] certs, String authType) {
					}
				}
			};
			//Install the all-trusting trust manager
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

			//Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			//Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (Exception e) {
			System.err.println("Exception caught from Ignore Certificate block");
            e.printStackTrace();
        }
	}
///===========================================================================================================
///------------------------------------	fvPasswordAuthentication() -------------------------------------------
///===========================================================================================================
	public void fvPasswordAuthentication(){
		//Flovisor Authentication
		if (FVCALL_DBG) System.out.println("Password Authentication into flowvisor");
    	Authenticator.setDefault(new Authenticator() {
    		protected PasswordAuthentication getPasswordAuthentication() { 
    			return new PasswordAuthentication ("fvadmin", "".toCharArray()); 
    		}
    	});
	}
	
	

	
///===========================================================================================================
///----------------------------------------	sendJsonRequest() ------------------------------------------------
///===========================================================================================================
	public void sendFvRequest(String requestBody) {
		if (FVCALL_DBG) System.out.println("\nFlowvisorAPICAll.sendFvRequest() --> Sending request to remote server");
    	try {
    		URL url = new URL(uri);
    		connection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
    		connection.setDoOutput(true);
    		connection.setRequestMethod("POST");
    		connection.setRequestProperty("Content-Type", "application/json");
    		connection.setRequestProperty("Accept", "application/json");
    		connection.setRequestProperty("Content-Length", Integer.toString(requestBody.getBytes().length));
    		connection.setUseCaches(true);
    		connection.setDoInput(true);
    		
    		if (FVCALL_DBG) System.out.println(connection.toString());
    		if (FVCALL_DBG) System.out.println(requestBody);
    		
    		OutputStream out = connection.getOutputStream();
    		out.write(requestBody.getBytes());
    		out.flush();
    		out.close();
    		
    	} catch (MalformedURLException malE) {
        	connection.disconnect();
			System.err.println("MalformedURLException caught at server-request block");
			malE.printStackTrace();
    	} catch (ProtocolException protoE) {
			connection.disconnect();
            System.err.println("ProtocolException caught at server-request block");
        	protoE.printStackTrace();
		} catch (IOException ioE) {
        	connection.disconnect();
            System.err.println("IOException caught at server-request block");
        	ioE.printStackTrace();
    	}
    	receiveFvResponse();
	}
///===========================================================================================================
///----------------------------------------	receiveJsonRpc() -------------------------------------------------
///===========================================================================================================
	public void receiveFvResponse(){		//this method called at the end of sendFvRequest()
		// Server-Response Block
		StringBuffer response = new StringBuffer();
    	try {
        	if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        		if (FVCALL_DBG) System.out.println("\nFlowvisorAPICAll.receiveFvResponse() --> getting responce from remote server");
        		InputStream is = connection.getInputStream();
        		BufferedReader rd = new BufferedReader(new InputStreamReader(is));
        		String line;
        		
        		while ((line = rd.readLine()) != null) {
            		response.append(line);
            		response.append('\r');
        		}
        		rd.close();
        		fvResponse = response.toString();
        		
        		if (FVCALL_DBG) System.out.println(fvResponse+"\n");
    		} else {
    			System.err.println("Bad Responce from server. Responce code = " + connection.getResponseCode());
    			connection.disconnect();
    			fvResponse = null;
    		}
		} catch (IOException ioE) {
			System.err.println("IOException caught at server-responce block");
    		ioE.printStackTrace();
		}
	}
}
