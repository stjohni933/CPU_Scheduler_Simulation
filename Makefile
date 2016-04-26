JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	CPU.java \
	Event.java \
	Proc.java \
	ProcGenerator.java \
	SchedulerSimulation.java \
	TrialDriver.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class