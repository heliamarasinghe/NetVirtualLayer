package com.imaginelab.sdn_icf.compose;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FilesManipulation {
	public FilesManipulation(String fileName_trace, String fileName_Result,String fileName_close) throws IOException {
		super();
		FileName_trace = fileName_trace;
		FileName_Result = fileName_Result;
		FileName_close=fileName_close;
		//FileName_simulation_ana=fileName_analysis;
		 writer_trace = new BufferedWriter( new FileWriter(FileName_trace));
		 writer_simulation = new BufferedWriter( new FileWriter(FileName_Result));
		 writer_closeness = new BufferedWriter( new FileWriter(FileName_close));
		// writer_sim_monitor = new BufferedWriter( new FileWriter(FileName_simulation_ana));
		 
	}
	
	

	public FilesManipulation(String fileName_simulation_ana) throws IOException {
		super();
		FileName_simulation_ana = fileName_simulation_ana;
		 writer_sim_monitor = new BufferedWriter( new FileWriter(FileName_simulation_ana));
	}

	public FilesManipulation(int n,String Req_file) throws IOException {
		super();
		Req_FileName = Req_file;
		Req_writer_sim_monitor = new BufferedWriter( new FileWriter(Req_FileName));
	}

	public String getFileName_trace() {
		return FileName_trace;
	}
	public void setFileName_trace(String fileName_trace) {
		FileName_trace = fileName_trace;
	}
	public String getFileName_Result() {
		return FileName_Result;
	}
	public void setFileName_Result(String fileName_Result) {
		FileName_Result = fileName_Result;
	}
	String FileName_trace;
	String FileName_close;
	String FileName_Result;
	String FileName_simulation_ana;
	String Req_FileName;
	BufferedWriter writer_trace;
	BufferedWriter writer_simulation;
	BufferedWriter writer_closeness;
	BufferedWriter writer_sim_monitor;
	BufferedWriter Req_writer_sim_monitor;
	public void Close() throws IOException
	{
		if (writer_trace!=null)
			writer_trace.close();
		if (writer_simulation!=null)
			writer_simulation.close();
		if (writer_closeness!=null)
			writer_closeness.close();
		if (writer_sim_monitor!=null)
			writer_sim_monitor.close();
		if (Req_writer_sim_monitor!=null)
			Req_writer_sim_monitor.close();
	}
	public void Trace(String to_add) throws IOException
	{
		writer_trace.write(to_add+ "\r\n");
		writer_trace.write("-------------------------------------------------------------------------------- \r\n");
  	  
	}
	public void Save_Result(String to_Add) throws IOException
	{
		writer_simulation.write(to_Add+ "\t");
		//writer_simulation.write("-------------------------------------------------------------------------------- \r\n");
  	  
	}
	public void Save_Closeness(String to_Add) throws IOException
	{
		writer_closeness.write(to_Add+ "\t");
		//writer_simulation.write("-------------------------------------------------------------------------------- \r\n");
  	  
	}
	public void Create_request_file(String to_Add) throws IOException
	{
		Req_writer_sim_monitor.write(to_Add+ "\t");
		//writer_simulation.write("-------------------------------------------------------------------------------- \r\n");
  	  
	}
	public void Save_Simulation_monitoring(String to_Add) throws IOException
	{
		writer_sim_monitor.write(to_Add+ "\r\n");
		//writer_simulation.write("-------------------------------------------------------------------------------- \r\n");
  	  
	}
//	public static void main(String[] args) throws IOException 
//	   {
	   //   try 
	  //    {
	 //   	  BufferedReader br = new BufferedReader(new FileReader("C:/Assign4.txt"));
	 //   	  BufferedWriter writer = new BufferedWriter( new FileWriter("C:/target.txt"));
	      
	    	  // 	trial to read and write using file streaming
	    	  //   File fileIn  = new File("c:/Assign4.txt");  // existing file read
	       //   File fileOut = new File("c:/target.txt");   // created file as output write
	      //    FileInputStream streamIn   = new FileInputStream(fileIn);
	      //    FileOutputStream streamOut = new FileOutputStream(fileOut);
              //    streamOut.write("welcome to mine");
	       //  int c;
	       //  while ((c = streamIn.read()) != -1) 
	       //  {
	       //     streamOut.write(c);
	       //  }
           // 
	      //    streamIn.close();
	       //    streamOut.close();
	      //   }
	    //	  writer.write("ccccc \r\n");
	    	 
	    //	  writer.write("xxxxxx");
	    //	  writer.close();
	    	  // another trial using bufferreader and buffer writer
	    	/*  try {
	    	        int count = 1;  
	    	        StringBuilder sb = new StringBuilder();
	    	        String line = br.readLine();            
	    	        while (line != null) {
	    	            sb.append(count++);
	    	            sb.append(line);
	    	            sb.append("\n");
	    	            writer.write(line);
	    	            line = br.readLine();    
	    	        }
	    	    } finally {
	    	        br.close();
	    	        writer.close();
	    	    }*/
	//   }
}
