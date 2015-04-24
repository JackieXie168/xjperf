# JPerf 2.0.2 #

**The source code has been moved to the IPerf's sourceforge SVN ([http://iperf.sourceforge.net](http://iperf.sourceforge.net)), but you can still download the release binaries here.**

**To download the latest sources, simply run:
svn co https://iperf.svn.sourceforge.net/svnroot/iperf/jperf/trunk**


This project gives a better UI and new functionalities to the initial **[JPerf 1.0](http://dast.nlanr.net/projects/jperf/)** project based on the **iperf** network bandwidth measurement tool.

This project uses the following libraries:
  * [JFreeChart](http://www.jfree.org/jfreechart/)
  * [SwingX from swinglabs](http://swinglabs.org/)
  * [Forms](http://www.jgoodies.com/)


## List of all changes on the 2.0.2 version ##
  * import/export feature implemented ([issue #4](https://code.google.com/p/xjperf/issues/detail?id=#4))
  * WaitWindow behavior and UI improved (the one displayed when stopping iperf)
  * System Look'n feel used under windows
  * UI improvements
  * Code improvements
  * SwingX library updated to 0.9.6
  * Instructions for running and compiling added ([issue #6](https://code.google.com/p/xjperf/issues/detail?id=#6))


## List of all changes on the 2.0.1 version ##
  * UI improvements
  * It is now possible to use bits and bytes units (usefull for UDP packet size) ([issue #9](https://code.google.com/p/xjperf/issues/detail?id=#9))


## List of all changes on the 2.0.0 version ##

  * The project is compiled for the JRE 1.5, so that we have benefit of enumerations, generics, etc.
  * Complete UI refactoring
    * the iperf command-line reflecting the selected configuration is displayed and updated live while changing a parameter into the UI
    * use java libraries such as SwingX and Forms
    * network options are associated by layer
    * the new "Quick Start" panel contains all essential parameters for a quick start
    * New charts look and feel
    * real time update of JFreeChart charts
    * the output panel is scrolling automatically
    * "Load default configuration" button
  * Better management of iperf processes
  * Improvements on the "About JPerf" dialog box
  * Some bug fixes
  * Code refactoring

Screenshots of the project:

## User interface in JPerf 2.0.2 on Windows ##
|![http://xjperf.googlecode.com/files/jperf-2.0.2-windows.png](http://xjperf.googlecode.com/files/jperf-2.0.2-windows.png)|
|:------------------------------------------------------------------------------------------------------------------------|

## User interface in JPerf 2.0.2 ##
|![http://xjperf.googlecode.com/files/jperf-2.0.2.png](http://xjperf.googlecode.com/files/jperf-2.0.2.png)|
|:--------------------------------------------------------------------------------------------------------|

## The server mode ##
|![http://xjperf.googlecode.com/files/jperf-1.jpg](http://xjperf.googlecode.com/files/jperf-1.jpg)|
|:------------------------------------------------------------------------------------------------|

## The client mode ##
|![http://xjperf.googlecode.com/files/jperf-2.jpg](http://xjperf.googlecode.com/files/jperf-2.jpg)|
|:------------------------------------------------------------------------------------------------|



---

