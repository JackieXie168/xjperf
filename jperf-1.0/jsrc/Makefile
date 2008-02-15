SRCS = $(wildcard *.java) 
OBJS = $(SRCS:.java=.class)

JAR = ../jperf.jar

all: $(JAR)

Measurement.class: Measurement.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar Measurement.java

JperfStreamResult.class: JperfStreamResult.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar JperfStreamResult.java

IperfThread.class: IperfThread.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar IperfThread.java

JperfGUI.class: JperfGUI.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar JperfGUI.java

Jperf.class: Jperf.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar Jperf.java

JperfLooknFeel.class: JperfLooknFeel.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar JperfLooknFeel.java

JperfTheme.class: JperfTheme.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar JperfTheme.java

JperfWindowListen.class: JperfWindowListen.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar JperfWindowListen.java

buttonUI.class: buttonUI.java
	javac -classpath ./:../jfreechart-0.9.6.jar:../jcommon-0.7.2.jar buttonUI.java

$(JAR): $(OBJS)
	jar -cmvf Manifest $(JAR) $(OBJS) buttons/ license.txt

clean:
	-$(RM) -f $(SRCS:.java=.class) $(JAR)
