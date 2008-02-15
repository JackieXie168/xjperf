import java.lang.System;
import java.awt.event.*;
import java.io.*;

public class Jperf {

  public static void main( String[] args ) {

    String version = "";
    Process process;
   
    //get version of Iperf
    try {
      process = Runtime.getRuntime().exec("iperf -v");
    }
    catch(IOException e) {
      System.out.println(e.getMessage() + ". (Iperf is probably not in your path.)");
      return;
    }

    BufferedReader input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    try {
      String line;
      line = input.readLine();
      
      while(line != null) {
	version = line;
	line = input.readLine();
      } 

      
    }
    catch(IOException e) {
      version = "Unknown Version";
     
    }
  
    new JperfGUI("a iperf 2.2.0");
  } 
   
}



