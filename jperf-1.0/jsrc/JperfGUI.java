//Notes:
//-I put the command line options into the tooltips because all the
// Iperf documentation is based on them.  When (if?) we ever get some
// reasonable documentation specific to Jperf (this GUI), then we can
// get rid of those (DC)
//-To avoid the situation where an error pops up a blank graph, I just
// check to see if the vector passed in is empty.  This may not catch
// all errors (DC)
//-Um...the graphs suck...help... (DC)
//-If I have time, I'll try to throw together a help file.  I doubt I'll
// be able to pull it off though since I only have 2 weeks left.  If you
// want to do one, I recommend putting in (1) description (duh), (2) the
// command-line option equivalent, (3) what other options are necessary
// for this option to be used (ex: UDP bandwidth requires client and UDP),
// (4) default values.  Anyway, I'll try to at least start it (DC)
//-Since I'm planning to have the default values in the boxes, it seems
// reasonable to have a "restore defaults" button somewhere.  Also, it
// might be good to make default values appear in a different color to
// set them apart (DC)
//-The output window needs to scroll as the output goes beyond the end
// of it (DC)

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.text.NumberFormat;
import java.text.Format;
import java.lang.System;
import java.io.*;
import javax.swing.plaf.metal.*;
import java.util.Vector;
import java.util.regex.*;

//JFreeChart stuff
import com.jrefinery.chart.JFreeChart;
import com.jrefinery.chart.ChartFactory;
import com.jrefinery.chart.ChartPanel;
import com.jrefinery.chart.axis.ValueAxis;
import com.jrefinery.chart.plot.XYPlot;
import com.jrefinery.data.XYSeries;
import com.jrefinery.data.XYSeriesCollection;
import com.jrefinery.ui.ApplicationFrame;
import com.jrefinery.ui.RefineryUtilities;
import com.jrefinery.chart.ChartFrame;

public class JperfGUI implements ActionListener, ItemListener{

  private JFrame frame;
  private JFrame aboutFrame;

  //Menu stuff
  private JMenuBar menuBar;
  private JMenu menu;
  private JMenuItem menuItem;


  //Panels 
  private JPanel connectionInfo;
  private JSplitPane mainPanel;
  private JTabbedPane tabbedPane;
  private ImageIcon icon;
  private static JScrollPane outputPane;
  private JTextArea output;

  //parameters
  private JTextField dest, port;
  private static JRadioButton server, client;
  private JFormattedTextField buflen;
  private JFormattedTextField winsize;
  private JFormattedTextField mss;
  private JFormattedTextField udp_bw;
  private JFormattedTextField TTL;
  private JCheckBox noDelay;
  private JTextField bindhost;
  private JCheckBox printMSS;
  private JTextField numConn;
  private JTextField transFile;
  private static JTextField trans;
  private JComboBox formatList;
  private static JTextField interval;
  private JComboBox tos;
  private ButtonGroup type;
  private ButtonGroup protocol;
  private static JRadioButton tcpButton, udpButton;
  private static JButton run, stop;
  private static JRadioButton numbuff, transTime;
  private JCheckBox v6;
  private IperfThread iperf;
  private static JCheckBox clear, bw_graph, jitter_graph;
  private JButton jitterB, bwB, clearB, saveB;
  private JCheckBox compMode;
  private JTextField numClients, LP;
  private JRadioButton dualMode, tradeMode;
  private JButton browse;
  private JPanel settings;
  private float iperfV;
  private boolean pthreads;

  //labels only for disabling stuff
  private JLabel lb_mss,lb_udpbw, lb_dest, lb_limit, lb_trans, lb_winsize, 
    lb_numConn, lb_tos, lb_TTL, lb_transFile,lb_bufflen,lb_udplen, lb_mode, 
    lb_compMode, lb_LP,lb_numClients, lb_servConn, lb_udpsize;

  //other
  private String[] outputFormat = {"Adaptive Bits","Adaptive Bytes", 
				   "Bits", "Bytes",  "Gbits", "GBytes", "Kbits",
				   "KBytes","Mbits", "MBytes"};
  private String[] tos_options = {"", "Low Cost", "Low Delay", "Reliability", 
				  "Throughput"};

  private Insets labelInsets = new Insets(0,0,5,0);
  private Insets compInsets = new Insets(0,0,5,10);
  

  //to run iperf
  String options;
  String version;

  public JperfGUI(String v){
    
    //Setting up the Jperf GUI theme
    MetalLookAndFeel.setCurrentTheme(new JperfTheme());
    UIManager.installLookAndFeel("Jperf", "JperfLooknFeel");
    try {
      UIManager.setLookAndFeel("JperfLooknFeel");
    }
    catch(ClassNotFoundException e) {}
    catch(InstantiationException e) {}
    catch(IllegalAccessException e) {}
    catch(UnsupportedLookAndFeelException e) {}
    

    //When the jperf gui is first started up, the version is obtained. It is passed when
    //creating an JperfGUI
    //we use the version number to disable or enable some features
    version = v;

    //set current version
    String[] version_split = v.split(" ");
    String vers = version_split[2].replace('.','-');
    String[] version_num = vers.split("-");
    iperfV = (new Float(version_num[1])).floatValue();
    iperfV /= 10.0;
    iperfV += (new Float(version_num[0])).floatValue();

    
    //create title of frame
    frame = new JFrame("Jperf Measurement Tool");
  
    //set up our menu
    menuBar = new JMenuBar();
    menu = new JMenu("Help");
    menu.setMnemonic(KeyEvent.VK_H);
    menuItem = new JMenuItem("About...");
    menuItem.setActionCommand("About");
    menuItem.addActionListener(this);
    menu.add(menuItem);
    menuBar.add(menu);
    frame.setJMenuBar(menuBar);

    //set up main panels
    createConnectionPanel();
    tabbedPane = new JTabbedPane();
    settings = new JPanel();
    settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
    mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,connectionInfo,tabbedPane);
    mainPanel.setOneTouchExpandable(true);
    mainPanel.setDividerLocation(115);
    mainPanel.setDividerSize(0);
    icon = new ImageIcon("images/middle.gif");
    tabbedPane.addTab("Settings", icon, settings, "Jperf Settings");
    createOutputPanel();    
    createSettingsPanel();

    //window listener for frame
    frame.addWindowListener(new JperfWindowListen());
    
    frame.setSize(650,550);
    frame.getContentPane().add(mainPanel);
    frame.setVisible(true);
 
    //createBWGraph();
   
  }
 

  //This creates the top connection panel that lets the user select client/server mode
  //tcp or udp, and the server/port. Of course it has the usual run/stop buttons.
  public void createConnectionPanel() {
    
    connectionInfo = new JPanel();
    //set up Connection Information
    GridBagLayout gbag = new GridBagLayout();
    GridBagConstraints constraints = new GridBagConstraints();
    connectionInfo.setLayout(gbag);


    //radio buttons for selecting client or server
    JPanel typeButtons = new JPanel();
    typeButtons.add(new JLabel("Type:"));
    client = new JRadioButton("Client");
    client.setSelected(true);
    client.setActionCommand("client");
    client.addActionListener(this);
    client.setToolTipText("Run Iperf as a client   (command line: -c)");
    typeButtons.add(client);
    server = new JRadioButton("Server");
    server.setActionCommand("server");
    server.addActionListener(this);
    server.setToolTipText("Run Iperf as a server   (command line: -s)");
    typeButtons.add(server);
    type = new ButtonGroup();
    type.add(client);
    type.add(server);
    setGrid(constraints,0,0,2,1);
    addComponent(typeButtons,connectionInfo,gbag,constraints);

    //radio buttons for TCP/UDP
    JPanel protocolButtons = new JPanel();
    protocolButtons.add(new JLabel("Protocol:"));
    tcpButton = new JRadioButton("TCP");
    tcpButton.setSelected(true);
    tcpButton.setActionCommand("TCP");
    tcpButton.addActionListener(this);
    tcpButton.setToolTipText("Use TCP Protocol   (command line: default)");
    protocolButtons.add(tcpButton);
    udpButton = new JRadioButton("UDP");
    udpButton.setActionCommand("UDP");
    udpButton.addActionListener(this);
    udpButton.setToolTipText("Use UDP Protocol   (command line: -u)");
    protocolButtons.add(udpButton); 
    protocol = new ButtonGroup();
    protocol.add(tcpButton);
    protocol.add(udpButton);
    setGrid(constraints,3,0,2,1);
    addComponent(protocolButtons,connectionInfo,gbag,constraints);
    

    //add server/client host
    setGrid(constraints,0,1,1,1);
    constraints.insets = labelInsets;
    lb_dest = new JLabel( "Server: ");
    lb_dest.setToolTipText("Specify what server the Iperf client should connect to   (command line: -c)");
    lb_limit = new JLabel("Client Limit: ");
    lb_limit.setToolTipText("Specify a host for Iperf server to only accept connections from");
    addComponent(lb_dest, connectionInfo,gbag,constraints);
    addComponent(lb_limit, connectionInfo,gbag,constraints);
    lb_limit.setVisible(false);
    dest = new JTextField(30);
    setGrid(constraints,1,1,3,1);
    constraints.insets = compInsets;
    addComponent(dest,connectionInfo,gbag,constraints);

    //add port
    JPanel portPanel = new JPanel();
    setGrid(constraints,3,1,1,1);
    constraints.insets = labelInsets;
    JLabel lb_port = new JLabel("Port: ");
    portPanel.add(lb_port);
    lb_port.setToolTipText("Specify port   (command line: -p)");
    port = new JTextField("5001",5);
    portPanel.add(port);
    constraints.anchor = GridBagConstraints.WEST;
    setGrid(constraints,4,1,1,1);
    constraints.insets = compInsets;
    addComponent(portPanel, connectionInfo,gbag, constraints);
       
    //run button
    JPanel butins = new JPanel();
    setGrid(constraints,4,2,2,1);
    run = new JButton("Run");
    run.setToolTipText("Run Iperf!");
    run.setActionCommand("Run");
    run.addActionListener(this);
    butins.add(run);

    //add stop button
    
    stop = new JButton("Stop");
    stop.setToolTipText("Stop Iperf");
    stop.setActionCommand("Stop");
    stop.addActionListener(this);
    stop.setEnabled(false);
    butins.add(stop);
    addComponent(butins,connectionInfo,gbag,constraints);
  }

  public void createSettingsPanel() {
  //new panel for settings
    
    GridBagLayout gridbag = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    
    //Reporting Options
    TitledBorder t1 = new TitledBorder("General Options");
    JPanel general = new JPanel();
    //general.setBorder(t1);
    general.setPreferredSize(new Dimension(600,175));
    general.setLayout(gridbag);
    settings.add(general);
    
    //Compatibility mode?
    setGrid(c,0,0,2,1);
    c.insets = compInsets;
    compMode = new JCheckBox("Compatibility Mode");
    compMode.setToolTipText("Compatibility mode allows for use with older version of iperf   (command line: -C)");
    compMode.setSelected(false);
    if(iperfV < 1.7)
      compMode.setEnabled(false);
    compMode.addItemListener(this);
    addComponent(compMode,general,gridbag,c);
    
    //testing mode
    setGrid(c,3,0,3,1);
    c.insets = labelInsets;
    JPanel mode = new JPanel();
    lb_mode = new JLabel("Testing Mode: ");
    dualMode = new JRadioButton("Dual");
    dualMode.setToolTipText("Cause the server to connect back to the client immediately and run tests simultaneously   (command line: -d)"); 
    tradeMode = new JRadioButton("Trade");
    tradeMode.setToolTipText("Cause the server to connect back to the client following termination of the client   (command line: -r)");
    mode.add(lb_mode);
    mode.add(dualMode);
    mode.add(tradeMode);
    if(iperfV < 1.7) {
      lb_mode.setEnabled(false);
      dualMode.setEnabled(false);
      tradeMode.setEnabled(false);
    }    
    
    lb_LP = new JLabel("Port: ");
    lb_LP.setToolTipText("This specifies the port that the server will connect back to the client on   (command line: -L)");
    c.insets = compInsets;
    LP = new JTextField("5001",4);
    mode.add(lb_LP);
    mode.add(LP);
    addComponent(mode,general,gridbag,c);
    if(iperfV < 1.7) {
      lb_LP.setEnabled(false);
      LP.setEnabled(false);
    }
      

    //set window size
    setGrid(c,0,1,1,1);
    c.anchor = GridBagConstraints.EAST;
    c.insets = labelInsets;
    lb_winsize = new JLabel("TCP Window Size: ");
    lb_winsize.setToolTipText("Set TCP window size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -w)");
    addComponent(lb_winsize,general,gridbag,c);
    lb_udpsize = new JLabel("UDP Buffer Size: ");
    lb_udpsize.setToolTipText("Set UDP buffer size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -w)");
    addComponent(lb_udpsize,general,gridbag,c);
    lb_udpsize.setVisible(false);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    winsize = new JFormattedTextField();
    winsize.setColumns(4);
    setGrid(c,1,1,1,1);
    addComponent(winsize,general,gridbag,c);
    
    
    //buffer length
    setGrid(c,2,1,1,1);
    c.anchor = GridBagConstraints.EAST;
    c.insets = labelInsets;
    lb_bufflen = new JLabel("Buffer Length: ");
    lb_bufflen.setToolTipText("Read/Write buffer length. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -l)");
    addComponent(lb_bufflen,general,gridbag,c);
    lb_udplen = new JLabel( "UDP Packet Size: ");
    lb_udplen.setToolTipText("Set UDP datagram buffer size. Use 'K' or 'M' for kilo/mega bytes. (i.e 1470)   (command line: -l)");
    lb_udplen.setVisible(false);
    addComponent(lb_udplen,general,gridbag,c);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    buflen = new JFormattedTextField("8K");
    buflen.setColumns(4);
    setGrid(c,3,1,1,1);
    addComponent(buflen,general,gridbag,c);


    //bandwidth
    setGrid(c,4,2,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    lb_udpbw = new JLabel("UDP Bandwidth: ");
    lb_udpbw.setEnabled(false);
    lb_udpbw.setToolTipText("Set bandwidth to send in bits/sec. Use 'K' or 'M' for kilo/mega bits. (i.e 8K)   (command line: -b)");
    addComponent(lb_udpbw,general,gridbag,c);
    udp_bw = new JFormattedTextField("1M");
    udp_bw.setEnabled(false);
    udp_bw.setColumns(4);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    setGrid(c,5,2,1,1);
    addComponent(udp_bw,general,gridbag,c);
    

    //Num Connections
    setGrid(c,0,2,1,1);
    c.insets = labelInsets;
    numConn = new JTextField("1",4);
    lb_numConn = new JLabel( "Parallel Streams: ");
    lb_servConn = new JLabel("Num Connections: ");
    lb_servConn.setToolTipText("The number of connections to handle by the server before closing. Default is 0 (handle forever)   (command line: -P)");
    lb_numConn.setToolTipText("The number of simultaneous connections to make to the server. Default is 1.   (command line: -P)");
    c.anchor = GridBagConstraints.EAST;
    addComponent(lb_numConn,general,gridbag,c);
    addComponent(lb_servConn,general,gridbag,c);
    lb_servConn.setVisible(false);
    setGrid(c,1,2,1,1);
    c.anchor = GridBagConstraints.WEST;
    c.insets = compInsets;
    addComponent(numConn,general,gridbag,c);


    //num connections server will handle
    //lb_numClients = new JLabel("Client Limit: ");
    //setGrid(c,2,2,1,1);
    //c.insets = labelInsets;
    //c.anchor = GridBagConstraints.EAST;
    //lb_numClients.setToolTipText("Limit the connections that Iperf will accept to the host specified.   (command line: -c)");
    //lb_numClients.setEnabled(false);
    //addComponent(lb_numClients,general,gridbag,c);
    //numClients = new JTextField(4);
    //numClients.setEnabled(false);
    //setGrid(c,3,2,1,1);
    //c.insets = compInsets;
    //c.anchor = GridBagConstraints.WEST;
    //addComponent(numClients,general,gridbag,c);
    //if(iperfV < 1.7) {
    //  lb_numClients.setEnabled(false);
    //  numClients.setEnabled(false);
    //}
      


    //attempt to set MSS
    setGrid(c,4,1,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    lb_mss = new JLabel("Max Segment Size: ");
    lb_mss.setToolTipText("Attempt to set max segment size. Use 'K' or 'M' for kilo/mega bytes. (i.e 8K)   (command line: -M)");
    addComponent(lb_mss,general,gridbag,c);
    mss = new JFormattedTextField();
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    mss.setColumns(4);
    setGrid(c,5,1,1,1);
    addComponent(mss,general,gridbag,c);


    //TTL
    setGrid(c,2,5,1,1);
    c.insets = labelInsets;
    TTL = new JFormattedTextField("1");
    TTL.setColumns(4);
    lb_TTL = new JLabel("TTL: ");
    lb_TTL.setToolTipText("Set time to live (number of hops). Default is 1.   (command line: -T)");
    c.anchor = GridBagConstraints.EAST;
    addComponent(lb_TTL,general,gridbag,c);
    setGrid(c,3,5,1,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    addComponent(TTL,general,gridbag,c);
    lb_TTL.setEnabled(false);
    TTL.setEnabled(false);

    //no delay?
    noDelay = new JCheckBox("TCP No Delay");
    noDelay.setSelected(false);
    noDelay.setToolTipText("Disable Nagle's algorithm   (command line: -N)");
    c.insets = compInsets;
    c.anchor = GridBagConstraints.EAST;
    setGrid(c,3,5,2,1);
    addComponent(noDelay,general,gridbag,c);
    
    


    //bind to a specific host
    setGrid(c,0,3,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    bindhost = new JTextField(18);
    JLabel lb_bhost = new JLabel("Bind to Host: ");
    lb_bhost.setToolTipText("Bind to host, one of this machine's addresses. For multihomed hosts.   (command line: -B)");
    addComponent(lb_bhost,general,gridbag,c);
    setGrid(c,1,3,3,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    addComponent(bindhost,general,gridbag,c);


    //bind to IPv6 address
    setGrid(c,4,3,1,1);
    v6 = new JCheckBox("IPv6");
    v6.setToolTipText("Bind to an IPv6 address   (command line: -V)");
    v6.setSelected(false);
    c.anchor = GridBagConstraints.WEST;
    c.insets = compInsets;
    addComponent(v6,general,gridbag,c);
    if(iperfV < 1.6)
      v6.setEnabled(false);
    v6.addItemListener(this);


    //file to transmit
    setGrid(c,0,4,1,1);
    c.insets = labelInsets;
    lb_transFile = new JLabel("Representative File: ");
    lb_transFile.setToolTipText("Use a representative stream to measure bandwidth   (command line: -F)");
    c.anchor = GridBagConstraints.EAST;
    addComponent(lb_transFile,general,gridbag,c);
    transFile = new JTextField(18);
    c.anchor = GridBagConstraints.WEST;
    c.insets = compInsets;
    setGrid(c,1,4,3,1);
    addComponent(transFile,general,gridbag,c);

    //add browse button
    browse = new JButton("Browse");
    browse.setActionCommand("Browse");
    browse.addActionListener(this);
    setGrid(c,4,4,2,1);
    addComponent(browse,general,gridbag,c);
    if(iperfV < 1.2) {
      transFile.setEnabled(false);
      lb_transFile.setEnabled(false);
      browse.setEnabled(false);
    }

    //TOS
    setGrid(c,0,5,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    lb_tos = new JLabel("Type of Service: ");
    lb_tos.setToolTipText("The type-of-service for outgoing packets. (Many routers ignore the TOS field)   (command line: -S)"); 
    addComponent(lb_tos,general,gridbag,c);
    tos = new JComboBox(tos_options);
    tos.setSelectedIndex(0);
    setGrid(c,1,5,2,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    addComponent(tos,general,gridbag,c);
    

    //Reporting Options
    TitledBorder t = new TitledBorder("Reporting Format");
    JPanel report = new JPanel();
    GridBagLayout g = new GridBagLayout();
    report.setBorder(t);
    report.setLayout(g);
    report.setPreferredSize(new Dimension(600,150));
    settings.add(report);
    
    //num buffers to transmit
    setGrid(c,0,0,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    lb_trans = new JLabel("Transmit: ");
    lb_trans.setToolTipText("Time to transmit, or number of buffers to transmit. Default is 10secs   (command line: -t, -n)");
    addComponent(lb_trans,report,g,c);
    trans = new JTextField("10",5);
    setGrid(c,1,0,1,1);
    c.anchor = GridBagConstraints.WEST;
    addComponent(trans,report,g,c);
    
    //are we sending a specific number of bytes or for a certain amount of time
    JPanel transp = new JPanel();
    numbuff = new JRadioButton("Number of Bytes");
    transTime = new JRadioButton("Time in Seconds");
    ButtonGroup btrans = new ButtonGroup();
    transTime.setSelected(true);
    btrans.add(transTime);
    btrans.add(numbuff);
    transp.add(transTime);
    transp.add(numbuff);
    setGrid(c,2,0,4,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    addComponent(transp,report,g,c);

    
    //output format
    setGrid(c,0,1,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    JLabel lb_format = new JLabel("Output Format: ");
    lb_format.setToolTipText("Format to print bandwidth numbers in. Adaptive formats choose between kilo- and mega-   (command line: -f)");
    addComponent(lb_format,report,g,c);
    formatList = new JComboBox(outputFormat);
    formatList.setSelectedIndex(6);
    setGrid(c,1,1,2,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    addComponent(formatList,report,g,c);

    //interval of reports
    setGrid(c,3,1,1,1);
    c.insets = labelInsets;
    c.anchor = GridBagConstraints.EAST;
    JLabel lb_int = new JLabel("Report Interval: ");
    lb_int.setToolTipText("Sets the interval time (secs) between periodic bandwidth, jitter, and loss reports   (command line: -i)");
    addComponent(lb_int,report,g,c);
    interval = new JTextField("1",4);
    setGrid(c,4,1,1,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.WEST;
    addComponent(interval,report,g,c);


    //should we print MSS?
    printMSS = new JCheckBox("Print MSS");
    printMSS.setToolTipText("Print out TCP maximum segment size   (command line: -m)"); 
    printMSS.setSelected(false);
    setGrid(c,5,1,1,1);
    c.insets = compInsets;
    c.anchor = GridBagConstraints.EAST;
    addComponent(printMSS,report,g,c);

      //graph stuff
    c.anchor = GridBagConstraints.WEST;
    clear = new JCheckBox("Clear Output for new Iperf Run");
    clear.setToolTipText("Always clear Iperf output between runs.");
    clear.setSelected(false);
    setGrid(c,0,2,3,1);
    c.insets = compInsets;
    addComponent(clear,report,g,c);

    //pop up bandwidth graph
    bw_graph = new JCheckBox("Bandwidth Graph PopUp");
    bw_graph.setSelected(true);
    bw_graph.setToolTipText("Always pop up bandwidth graph.");
    setGrid(c,3,2,3,1);
    addComponent(bw_graph,report,g,c);
    
    //pop up jitter
    jitter_graph = new JCheckBox("Jitter Graph PopUp");
    jitter_graph.setToolTipText("Always pop up jitter graph.");
    jitter_graph.setSelected(false);
    jitter_graph.setEnabled(false);
    setGrid(c,0,3,3,1);
    addComponent(jitter_graph,report,g,c);
    
    //create about frame and contents
    createAbout();

  }


  public void createOutputPanel() {
    
    output = new JTextArea(50,50);
    outputPane = new JScrollPane(output);
    JPanel graphButtons = new JPanel();
    JSplitPane outPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,outputPane,graphButtons);
    outPanel.setDividerLocation(300);
    outPanel.setDividerSize(0);
    tabbedPane.addTab("Output", icon, outPanel, "Iperf Output");
    tabbedPane.setSelectedIndex(0);
   
    //add graph buttons
    clearB = new JButton("Clear");
    clearB.setActionCommand("Clear");
    clearB.addActionListener(this);
    clearB.setToolTipText("Clears output from Iperf run");
    saveB = new JButton("Save");
    saveB.setActionCommand("Save");
    saveB.addActionListener(this);
    saveB.setToolTipText("Save output to a file");
    
    bwB = new JButton("Bandwidth");
    bwB.setActionCommand("Bandwidth");
    bwB.addActionListener(this);
    bwB.setToolTipText("Pops up Bandwidth Graph");
    jitterB = new JButton("Jitter");
    jitterB.setEnabled(false);
    jitterB.setActionCommand("Jitter");
    jitterB.addActionListener(this);
    jitterB.setToolTipText("Pops up Jitter Graph");
    graphButtons.add(clearB);
    graphButtons.add(bwB);
    graphButtons.add(jitterB);
    graphButtons.add(saveB);

  }

  public void itemStateChanged(ItemEvent e) {
    Object source = e.getItemSelectable();

    if (source == compMode) {
      if(compMode.isSelected()) {
	lb_mode.setEnabled(false);
	dualMode.setEnabled(false);
	tradeMode.setEnabled(false);
	LP.setEnabled(false);
	lb_LP.setEnabled(false);
      }
      else {
	if(!server.isSelected()) {
	  lb_mode.setEnabled(true);
	  dualMode.setEnabled(true);
	  tradeMode.setEnabled(true);
	  LP.setEnabled(true);
	  lb_LP.setEnabled(true);
	}
      }
	
    }
    if(source == v6) {
      if(v6.isSelected()) {
	if(udpButton.isSelected())
	  buflen.setText("1450");
      }
      else if(udpButton.isSelected())
	buflen.setText("1470");
    }
  }

  public void actionPerformed(ActionEvent e) {
    String command = e.getActionCommand();
    
    if(command == "TCP") {
      lb_winsize.setVisible(true);
      lb_udpsize.setVisible(false);
      printMSS.setEnabled(true);
      mss.setEnabled(true);
      lb_mss.setEnabled(true);
      noDelay.setEnabled(true);
      udp_bw.setEnabled(false);
      lb_udpbw.setEnabled(false);
      lb_udplen.setVisible(false);
      lb_bufflen.setVisible(true);
      jitter_graph.setEnabled(false);
      jitterB.setEnabled(false);
      lb_TTL.setEnabled(false);
      TTL.setEnabled(false);
      buflen.setText("8K");
    }
    else if(command == "UDP") {
      if(v6.isSelected())
	buflen.setText("1450");
      else
	buflen.setText("1470");
      printMSS.setEnabled(false);
      mss.setEnabled(false);
      lb_mss.setEnabled(false);
      noDelay.setEnabled(false);
      if(!server.isSelected()) {
	udp_bw.setEnabled(true);
	lb_udpbw.setEnabled(true);
	lb_TTL.setEnabled(true);
	TTL.setEnabled(true);
      }
      else {
	jitter_graph.setEnabled(true);
	jitterB.setEnabled(true);
      }
      lb_udplen.setVisible(true);
      lb_bufflen.setVisible(false);
      lb_winsize.setVisible(false);
      lb_udpsize.setVisible(true);
    }
    else if(command == "Run") {
      stop.setEnabled(true);
      run.setEnabled(false);
      boolean optionsReady = formOptions();
      
      if(optionsReady) {
	tabbedPane.setSelectedIndex(1);
	if(clear.isSelected())
	  output.setText("");
	iperf = new IperfThread(options,frame,output);
	iperf.start();
      }
      else {
	stop.setEnabled(false);
	run.setEnabled(true);
      }
	
    }
    else if(command == "Stop") {
      iperf.quit();
    }
    else if(command == "Clear") {
      iperf.quit();
      output.setText("");
    }
    else if(command == "server") {
      //disable client specific stuff, and enable server specific stuff
      if(udpButton.isSelected()) {
	jitter_graph.setEnabled(true);
	jitterB.setEnabled(true);
      }
      dest.setEnabled(false);
      lb_dest.setEnabled(false);
      lb_udpbw.setEnabled(false);
      udp_bw.setEnabled(false);
      numbuff.setEnabled(false);
      lb_trans.setEnabled(false);
      transTime.setEnabled(false);
      lb_tos.setEnabled(false);
      tos.setEnabled(false);
      lb_TTL.setEnabled(false);
      TTL.setEnabled(false);
      lb_transFile.setEnabled(false);
      transFile.setEnabled(false);
      browse.setEnabled(false);
      lb_mode.setEnabled(false);
      dualMode.setEnabled(false);
      tradeMode.setEnabled(false);
      LP.setEnabled(false);
      lb_LP.setEnabled(false);
      
      if(iperfV >= 1.7) {
	//lb_numClients.setEnabled(true);
	//numClients.setEnabled(true);
        lb_dest.setVisible(false);
        lb_limit.setVisible(true);
        dest.setEnabled(true);
        lb_numConn.setVisible(false);
        lb_servConn.setVisible(true);
        numConn.setText("0");
      } else {
        lb_numConn.setEnabled(false);
        numConn.setEnabled(false);
      }

    }
    else if(command == "client") {
      //enable everything that could have been disabled when we switched to server mode
      dest.setEnabled(true);
      lb_limit.setVisible(false);
      lb_dest.setVisible(true);
      lb_dest.setEnabled(true);
      jitter_graph.setEnabled(false);
      jitterB.setEnabled(false);

      if(udpButton.isSelected()) {
	lb_udpbw.setEnabled(true);
	udp_bw.setEnabled(true);
	lb_TTL.setEnabled(true);
	TTL.setEnabled(true);
      }
      transTime.setEnabled(true);
      numbuff.setEnabled(true);
      lb_trans.setEnabled(true);
      trans.setEnabled(true);
      lb_tos.setEnabled(true);
      tos.setEnabled(true);
      lb_numConn.setVisible(true);
      lb_numConn.setEnabled(true);
      lb_servConn.setVisible(false);
      numConn.setEnabled(true);
      numConn.setText("1");
      //lb_numClients.setEnabled(false);
      //numClients.setEnabled(false);
      if(iperfV >= 1.2) {
        lb_transFile.setEnabled(true);
        transFile.setEnabled(true);
        browse.setEnabled(true);
      }
      if(!compMode.isSelected() && iperfV >= 1.7) {
	lb_mode.setEnabled(true);
	dualMode.setEnabled(true);
	tradeMode.setEnabled(true);
	LP.setEnabled(true);
	lb_LP.setEnabled(true);
	
      }
      
    }
    else if(command == "About")
      aboutFrame.setVisible(true);
    else if(command == "Browse") {
      JFileChooser fc = new JFileChooser();
      int returnVal = fc.showDialog(frame,"Select");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
	File file = fc.getSelectedFile();
	transFile.setText(file.getAbsolutePath());
      }
    }
    else if(command == "Save") {
      JFileChooser fc = new JFileChooser();
      int returnVal = fc.showDialog(frame,"Save");
      if (returnVal == JFileChooser.APPROVE_OPTION) {
	File file = fc.getSelectedFile();
	

	//write output of textarea to file
	String text = new String(output.getText());
	
	try {
          FileWriter fw = new FileWriter(file);   
          fw.write(text);
          fw.close();
	}
        catch (IOException fe) {
	  JOptionPane.showMessageDialog(frame, "Error Saving Output: " + fe.getMessage());
        }
      }
    }
  }

  void addComponent(Component comp, JPanel panel, GridBagLayout gbag, GridBagConstraints c) {
    gbag.setConstraints(comp,c);
    panel.add(comp);
  }

  void setInset(Insets i, int top, int bottom, int right, int left) {
    i.top = top;
    i.bottom = bottom;
    i.right = right;
    i.left = left;
  }

  void setGrid(GridBagConstraints c, int gridx, int gridy, int gridwidth, int gridheight) {
    c.gridx = gridx;
    c.gridy = gridy;
    c.gridwidth = gridwidth;
    c.gridheight = gridheight;
  }

  boolean formOptions() {
    
    //deal with graph pop ups
    if(bw_graph.isSelected()) {
      Integer i;
      if(interval.getText().length() > 0)
	i = new Integer(interval.getText());
      else
	i = new Integer(0);
      
      if(i.intValue() < 1) {
	JOptionPane.showMessageDialog(frame, "For interesting Bandwidth graphs, please enter an interval to be 1 or greater.");
      return false;
      }

      //can not have adaptive bits
      int v = formatList.getSelectedIndex();
      if(v ==0 || v==1) {
	JOptionPane.showMessageDialog(frame, "For Bandwidth graphs, adaptive format can not be selected");
	return false;
      }
    }
    if(jitter_graph.isSelected() && jitter_graph.isEnabled()) {
      Integer i;
      if(interval.getText().length() > 0)
	i = new Integer(interval.getText());
      else
	i = new Integer(0);

      if(i.intValue() < 1) {
	JOptionPane.showMessageDialog(frame, "For interesting Jitter graphs, please enter an interval to be 1 or greater.");
	return false;
      }
      //can not have adaptive bits
      int v = formatList.getSelectedIndex();
      if(v ==0 || v==1) {
	JOptionPane.showMessageDialog(frame, "For Bandwidth graphs, adaptive format can not be selected");
	return false;
      }
    }

    //form options string, but return if we are missing stuff
    //clear it first
    options = "iperf";

    //determine if its a client or server
    if(server.isSelected()) {
      options += " -s";
      if(dest.getText().length() > 0 && dest.isEnabled())
	options += " -c " + dest.getText();
    }
    else {
      if(dest.getText().length() > 0) {
	options += " -c " + dest.getText();
      }
      else {
	JOptionPane.showMessageDialog(frame, "Error: Please enter host to connect to.");

	return false;
	//having issues with setting focus
      }
    }

    //these options are the same for server and client
    if(udpButton.isSelected())
      options += " -u";
    if(numConn.getText().length() > 0 && numConn.isEnabled())
      options += " -P " + numConn.getText();
    if(interval.getText().length() > 0)
      options += " -i " + interval.getText();
    if(printMSS.isSelected() && printMSS.isEnabled())
      options += " -m";
    if(port.getText().length() > 0)
      options += " -p " + port.getText();
    if(winsize.getText().length() > 0 && winsize.isEnabled())
      options += " -w " + winsize.getText();
    if(bindhost.getText().length() > 0)
      options += " -B " + bindhost.getText();
    if(mss.getText().length() > 0 && mss.isEnabled())
      options += " -M " + mss.getText();
    if(noDelay.isSelected() && noDelay.isEnabled())
      options += " -N";
    if(v6.isSelected() && v6.isEnabled())
      options += " -V";
    if(buflen.getText().length() > 0)
      options += " -l " + buflen.getText();
    if(compMode.isSelected() && compMode.isEnabled())
      options += " -C";

    //do formart
    options += " -f ";
    switch(formatList.getSelectedIndex()) {
    case 0:
      options += "a";
      break;
    case 1:
      options += "A";
      break;
    case 2:
      options += "b";
      break;
    case 3:
      options += "B";
      break;
    case 4:
      options += "g";
      break;
    case 5:
      options += "G";
      break;
    case 6:
      options += "k";
      break;
    case 7:
      options += "K";
      break;
    case 8:
      options += "m";
      break;
    case 9:
      options += "M";
      break;
    default:
      options += "A";

    }

    if(udp_bw.getText().length() > 0 && udp_bw.isEnabled())
	options += " -b " + udp_bw.getText();
      
    if(trans.getText().length() > 0 && trans.isEnabled()) {
	if(numbuff.isSelected())
	  options += " -n " + trans.getText();
	else if(transTime.isSelected())
	  options += " -t " + trans.getText();
    }
      
    if(dualMode.isSelected() && dualMode.isEnabled())
      options += " -d";
    if(tradeMode.isSelected() && tradeMode.isEnabled())
      options += " -r";
    if(LP.getText().length() > 0 && LP.isEnabled())
      options += " -L " + LP.getText();

    if(TTL.getText().length() > 0 && TTL.isEnabled())
      options += " -T " + TTL.getText();
    if(tos.getSelectedIndex() != 0) {
      options += " -S ";
      if(tos.getSelectedIndex() == 1)
        options += "0x02";
      else if(tos.getSelectedIndex() == 2)
        options += "0x10";
      else if(tos.getSelectedIndex() == 3)
        options += "0x04";
      else
        options += "0x08";
    }
    //check if file exists
    if(transFile.getText().length() > 0 && transFile.isEnabled()) {
      File f = new File(transFile.getText());
      if(!f.exists()) {
        JOptionPane.showMessageDialog(frame, "Error: The file you selected does not exist.");
        return false;
      }
      options += " -F " + transFile.getText();
    }
    return true;
  }

  void createAbout() {
    aboutFrame = new JFrame("About Iperf");
    JPanel top = new JPanel();
    top.setLayout(new FlowLayout(FlowLayout.LEFT));
    JTabbedPane bottom = new JTabbedPane();
    JSplitPane sPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,top,bottom);
    sPane.setDividerLocation(105);
    sPane.setDividerSize(0);
    
    //print out name, print out version, print out copyright (short statement), and web link

    //Iperf graphic
    ImageIcon icon = new ImageIcon(this.getClass().getResource("/buttons/Iperf-words.jpg"));
    JLabel pic = new JLabel(icon);
    top.add(pic);


    JPanel info = new JPanel();
    info.setLayout(new BoxLayout(info,BoxLayout.Y_AXIS));

    //Make this bold!
    JLabel name = new JLabel("Iperf");
    name.setAlignmentX(Component.CENTER_ALIGNMENT);
    info.add(name);
    
    JLabel lb_version = new JLabel(version);
    lb_version.setAlignmentX(Component.CENTER_ALIGNMENT);
    info.add(lb_version); 

    JLabel author = new JLabel("NLANR Distributed Applications Support Team");
    author.setAlignmentX(Component.CENTER_ALIGNMENT);
    info.add(author);

    JLabel webpage = new JLabel("http://dast.nlanr.net/Projects/Iperf");
    webpage.setAlignmentX(Component.CENTER_ALIGNMENT);
    info.add(webpage);
    top.add(info);
    
    //set up tabbed pane
    ImageIcon i = new ImageIcon("test");
    
    //add developers information
    JPanel devPanel = new JPanel();
    devPanel.setPreferredSize(new Dimension(300,200));
    devPanel.setLayout(new BoxLayout(devPanel, BoxLayout.Y_AXIS));
    JLabel dev = new JLabel("Mark Gates");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("Ajay Tirumala");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("Jim Ferguson");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("Jon Dugan");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("Feng Qin");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("Kevin Gibbs");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("Tanya Brethour");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("National Laboratory for Applied Network Research (NLANR)");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("National Center for Supercomputing Applications (NCSA)");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("University of Illinois at Urbana-Champaign (UIUC)");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    dev = new JLabel("http://www.ncsa.uiuc.edu");
    dev.setAlignmentX(Component.CENTER_ALIGNMENT);
    devPanel.add(dev);
    bottom.addTab("Developers",i, devPanel);
    
    JPanel ackPanel = new JPanel();
    JTextArea ack = new JTextArea("Thanks to Mark Gates (NLANR), Alex Warshavsky (NLANR) and Justin Pietsch (University of Washington) who were responsible for the 1.1.x releases of Iperf. For this release, we would like to thank Bill Cerveny (Internet2), Micheal Lambert (PSC), Dale Finkelson (UNL) and Matthew Zekauskas (Internet2) for help in getting access to IPv6 networks / machines. Special thanks to Matthew Zekauskas (Internet2) for helping out in the FreeBSD implementation. Also, thanks to Kraemer Oliver (Sony) for providing an independent implementation of IPv6 version of Iperf, which provided a useful comparison for testing our features. ");
    ack.setColumns(35);
    ack.setBackground(new Color(220,239,206));
    ack.setEditable(false);
    ack.setLineWrap(true);
    ack.setWrapStyleWord(true);
    ackPanel.add(ack);
    bottom.addTab("Acknowledgements",i,ackPanel);


    JTextArea license = new JTextArea();
    JScrollPane licensePanel = new JScrollPane(license);
    bottom.addTab("License",i,licensePanel);
    license.setEditable(false);
    try {
      InputStream inIS = this.getClass().getResourceAsStream("/license.txt");
      BufferedReader in = new BufferedReader(new InputStreamReader(inIS));
      String line = in.readLine();
      while(line != null) {
	license.append(line);
	license.append("\n");
	line = in.readLine();
      }
    }
    catch (FileNotFoundException f) {
      license.append("Error: " + f.getMessage());
    }
    catch(IOException e) {
      license.append("Error: " + e.getMessage());
    }

    aboutFrame.getContentPane().add(sPane);
    aboutFrame.setSize(450,350);
  }

  public static void createBWGraph(Vector bw) {

    if(!bw_graph.isSelected()) return;//they don't want the graph!
    if(bw.isEmpty()) return;//no data to graph
  
    XYSeriesCollection dataset = new XYSeriesCollection();
    String units = "Kbytes";

    for(int i=0; i< bw.size(); ++i) {
      JperfStreamResult stream = (JperfStreamResult)bw.get(i);
      String ID = new String(Integer.toString(stream.getID()));
      XYSeries series1 = new XYSeries("Stream" + ID, true);
      Vector measurements = stream.getBW();
      for(int j=0; j< measurements.size(); ++j) {
	Measurement m = (Measurement)measurements.get(j);
	Integer start = new Integer((int)m.startTime());
	Integer end = new Integer((int)m.endTime());
	Double value = new Double(m.getValue());
	if(start.intValue() == 0) {
	  series1.add(start, new Double(0));
	  units = m.getUnits();
	}
	series1.add(end, value);
      }
      dataset.addSeries(series1);
    }

    
     JFreeChart chart = ChartFactory.createAreaXYChart("Bandwidth",
                                                          "Time", "Value (" + units + ")",
                                                          dataset,
                                                          true,  // legend
                                                          false,  // tool tips
                                                          false  // URLs
                                                          );

     XYPlot plot = chart.getXYPlot();
     plot.setOutlinePaint(Color.black);
     plot.setForegroundAlpha(0.65f);
     
     ValueAxis domainAxis = plot.getDomainAxis();
     domainAxis.setTickMarkPaint(Color.black);
     domainAxis.setLowerMargin(0.0);
     domainAxis.setUpperMargin(0.0);
     ValueAxis rangeAxis = plot.getRangeAxis();
     rangeAxis.setTickMarkPaint(Color.black);
     ChartPanel chartPanel = new ChartPanel(chart);
     chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));



    ChartFrame frame = new ChartFrame("First", chart);
    frame.setTitle("Bandwidth");
    frame.setDefaultLookAndFeelDecorated(true);
    frame.pack();
    frame.setContentPane(chartPanel);
    frame.setVisible(true);
    
  }

  public static void createJitterGraph(Vector bw) {
    if(!jitter_graph.isSelected()) return;//they don't want the graph!
    if(bw.isEmpty()) return;//nothing to graph!
    
    if(!udpButton.isSelected() || !server.isSelected())//they can't have the graph!
      return;
    XYSeriesCollection dataset = new XYSeriesCollection();
    

    for(int i=0; i< bw.size(); ++i) {
      JperfStreamResult stream = (JperfStreamResult)bw.get(i);
      String ID = new String(Integer.toString(stream.getID()));
      XYSeries series1 = new XYSeries("Stream" + ID, true);
      Vector measurements = stream.getJitter();
      for(int j=0; j< measurements.size(); ++j) {
	Measurement m = (Measurement)measurements.get(j);
	Integer start = new Integer((int)m.startTime());
	Integer end = new Integer((int)m.endTime());
	Double value = new Double(m.getValue());
	if(start.intValue() == 0) {
	  series1.add(start, new Double(0));
	  
	}
	series1.add(end, value);
      }
      dataset.addSeries(series1);
    }

    
     JFreeChart chart = ChartFactory.createAreaXYChart("Jitter",
                                                          "Time (sec)", "Value (ms)",
                                                          dataset,
                                                          true,  // legend
                                                          false,  // tool tips
                                                          false  // URLs
                                                          );

     XYPlot plot = chart.getXYPlot();
     plot.setOutlinePaint(Color.black);
     plot.setForegroundAlpha(0.65f);
     
     ValueAxis domainAxis = plot.getDomainAxis();
     domainAxis.setTickMarkPaint(Color.black);
     domainAxis.setLowerMargin(0.0);
     domainAxis.setUpperMargin(0.0);
     ValueAxis rangeAxis = plot.getRangeAxis();
     rangeAxis.setTickMarkPaint(Color.black);
     ChartPanel chartPanel = new ChartPanel(chart);
     chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));



    ChartFrame frame = new ChartFrame("First", chart);
    frame.setTitle("Jitter");
    frame.pack();
    frame.setContentPane(chartPanel);
    frame.setVisible(true);
    
  }

  public static void changeState(boolean r, boolean s) {
    run.setEnabled(r);
    stop.setEnabled(s);
    outputPane.revalidate();
  }

}


