/**
 * - 02/2008: Class created by Nicolas Richasse
 * - 03/2008: Class updated by Nicolas Richasse
 * 
 * Changelog:
 *-02/2008:
 * 	- class improved
 * 	- a dialog box is displayed when no executable is found instead of writing a console message
 * 	- on windows platforms, if iperf is not found into the system path, JPerf tries to search for it into the .\bin directory
 * 
 *-03/2008:
 * 	- the frame is now centered on screen at startup
 * 
 *-04/2009:
 * 	- URL and version updated
 * 
 *-05/2009:
 *	- System Look'n feel used under windows
 */
 /**
 *-07/2012:
 *	- Modified for iperf3-cygwin-gui
 */

package com.googlecode.iperf3cygwin;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.nlanr.jperf.JPerf;

import net.nlanr.jperf.ui.JPerfUI;
// import com.googlecode.iperf3cygwin.ui.Iperf3cygwinUI;


public class Iperf3cygwin extends JPerf
{
	public static final String IPERF3CYGWIN_VERSION = "3.0b4-03";
	public static final String IPERF3CYGWIN_URL = "http://code.google.com/p/iperf3-cygwin-gui/";
	
	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				String iperfCommand = "";
				String version = "";
				Process process;
				
				// set the locale to EN_US
				Locale.setDefault(Locale.ENGLISH);
				
				// if the OS is Windows, then we set the sytem Look'n Feel
				Properties sysprops = System.getProperties();
				String osName = ((String)sysprops.get("os.name")).toLowerCase();
				if (osName.matches(".*win.*") || osName.matches(".*dos.*") || osName.matches(".*microsoft.*"))
				{
					// check whether iperf.exe is available
					if (new File("bin/iperf3.exe").exists())
					{
						iperfCommand = "bin/iperf3.exe";
						try
						{
							process = Runtime.getRuntime().exec(iperfCommand+" -v");
						}
						catch(Exception ex)
						{
							JOptionPane.showMessageDialog(
									null, 
									"<html>Iperf3 not found</html>",
									"Iperf3 not found",
									JOptionPane.ERROR_MESSAGE);
							System.exit(1);
							return;
						}
					}
					else
					{
						JOptionPane.showMessageDialog(
								null, 
								"<html>Iperf3 not found</html>",
								"Iperf3 not found",
								JOptionPane.ERROR_MESSAGE);
						System.exit(1);
						return;
					}
					
					try 
					{
						UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
					}
					catch (Exception e)
					{
						// nothing
					}
					
				} // OS is Linux
				else
				{
					iperfCommand = "iperf3";
					try
					{
						process = Runtime.getRuntime().exec(iperfCommand+" -v");
					}
					catch(Exception ex)
					{
						JOptionPane.showMessageDialog(
								null, 
								"<html>Iperf3 not found</html>",
								"Iperf3 not found",
								JOptionPane.ERROR_MESSAGE);
						System.exit(1);
						return;
					}

				}
				
				////////////////
				// try to read the Iperf version on the standard output
				BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
				try
				{
					String line;
					line = input.readLine();
		
					while (line != null)
					{
						version = line;
						line = input.readLine();
					}
				}
				catch (IOException e)
				{
					// nothing
				}
				
				if (version == null || version.trim().equals(""))
				{
					// try to read the Iperf version on the error output
					input = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					try
					{
						String line;
						line = input.readLine();
		
						while (line != null)
						{
							version = line;
							line = input.readLine();
						}
					}
					catch (IOException e)
					{
						// nothing
					}
				}
				
				if (version == null || version.trim().equals(""))
				{
					version = "Iperf3 version 1.0.0";
					System.err.println("Failed to get Iperf3 version. Using '"+version+"' as default.");
				}
				////////////////
				
				// we start the user interface
				JPerfUI frame = new JPerfUI(iperfCommand, version);
				// Iperf3cygwinUI frame = new Iperf3cygwinUI(iperfCommand, version);
				
				centerFrameOnScreen(frame);
				frame.setVisible(true);
			}
		});
	}
	
}
