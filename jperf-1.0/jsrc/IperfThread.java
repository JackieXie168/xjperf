
//Notes:
//-The ParseLine results variable still ends up with a blank 0th string
// which may or may not ever matter (DC)

import java.awt.*;
import java.awt.event.*;
import java.lang.*;
import java.io.*;
import javax.swing.*;
import java.util.Vector;
import java.util.regex.*;


public class IperfThread extends Thread {
  
  String command;
  String output;
  Process process; 
  JFrame frame;
  JTextArea text;
  Vector FinalResults;
  int streamId;

  public IperfThread(String x, JFrame mainframe, JTextArea o) {
    command = x;
    frame = mainframe;
    text = o;
    FinalResults = new Vector();
    text.append(x);
    text.append("\n");
  }

  public void run( ) {
    
    //try to run Iperf, if we get an error, display it to the user.
    try {
      process = Runtime.getRuntime().exec(command);
    }
    catch(IOException e) {
      JOptionPane.showMessageDialog(frame, e.getMessage());
      return;
    }
    
    //read in the output from Iperf
    //BufferedReader output = new BufferedReader(new OutputStreamReader(process.getOutputStream()));
    BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
    BufferedReader errors = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    
    try {
      String input_line;
      input_line = input.readLine();
      while(input_line != null) {
	parseLine(input_line);
	text.append(input_line);
	text.append("\n");
	input_line = input.readLine();
      }

      String error_line;
      error_line = errors.readLine();
      while(error_line != null) {
	text.append(error_line);
	text.append("\n");
	error_line = errors.readLine();
      }
      process = null;
      JperfGUI.changeState(true,false);
      JperfGUI.createBWGraph(FinalResults);
      JperfGUI.createJitterGraph(FinalResults);
      text.append("Done.\n");
    }

    catch(IOException e) {
      //don't do anything?
      text.append("\nError in Iperf thread.\n");
    }

  }
  
  public void quit( ) {
    if( process != null )
      process.destroy();
    JperfGUI.changeState(true,false);
    
  }

  public void parseLine(String line) {
    //only want the actual output lines
    if(line.matches("\\[[ \\d]+\\]  [\\d]+.*")) {

      //Pattern p = Pattern.compile("[ ]+");
      Pattern p = Pattern.compile("[-\\[\\]\\s]+");
      //ok now break up the line into id#, interval, amount transfered, format transferred, bandwidth, and format of  bandwidth
      String[] results = p.split(line);

      
      //get the ID # for the stream
      Integer temp = new Integer(results[1].trim());
      int id = temp.intValue();

      //temp for now.. need to figure out how to do this like I do in C++ with find
      boolean found = false;
      JperfStreamResult streamResult = 	new JperfStreamResult(id);
      for(int i=0; i < FinalResults.size(); ++i) {
	if(((JperfStreamResult)FinalResults.elementAt(i)).getID() == id) {
	  streamResult = (JperfStreamResult)FinalResults.elementAt(i);
	  found = true;
	  break;
	}
      }

      if(!found)
	FinalResults.add(streamResult);

      //this is TCP or Client UDP
      if(results.length == 9) {
	Double start = new Double(results[2].trim());
	Double end = new Double(results[3].trim());
	Double bw = new Double(results[7].trim());
	
	Measurement M = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[8]);
	streamResult.addBW(M);
      }
      else if(results.length == 14) {

	//results[2] = results[2].substring(0,results[2].lastIndexOf("-"));
	Double start = new Double(results[2].trim());
	Double end = new Double(results[3].trim());
	Double bw = new Double(results[7].trim());
	
	Measurement M = new Measurement(start.doubleValue(), end.doubleValue(), bw.doubleValue(), results[7]);
	streamResult.addBW(M);
	
	Double jitter = new Double(results[9].trim());
	M = new Measurement(start.doubleValue(), end.doubleValue(), jitter.doubleValue(), results[10]);
	streamResult.addJitter(M);

	
      } 
    }
  }
}
