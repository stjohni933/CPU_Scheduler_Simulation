/**
 * @(#)SchedulerSimulation.java
 * This is the  main class, with which the user will interact. This class collects the command line parameters, prints all collected 
 * statistics, contains the event processing loop, and generally drives the functionality of the simulation. 
 *
 *
 * @author Iain	St.	John --	CSC	432	Operating Systems -- Programming Project 2 CPU Scheduler Simulation
 * @version	1.00 2016/2/23
 */

import java.util.*;
import java.io.*;

public class SchedulerSimulation {
	//Simulation settings -- corresponding to command line parameters
	static boolean verboseModeEnabled; //verbose printing of runtime details. Default: off.
	static boolean batchOutputEnabled; //batch output of stats. Default: off.
	static boolean ioFaultsEnabled; //whether or not processes will require I/O service. Default: on. 
	static int quantum; //time quantum for process pre-emption on CPUs. Default: 0 (No pre-emption).
	static int switchCost; //simulated conext switch cost. Default: 0.
	static int numcpus; //the number of CPUs in this simulated system. Default: 1.
	static long simStopTime; //the time at which this simulation stops running. Default: -1, meaning that it will keep running until stopped at command-line, or halts.
	static String procgenFilename; //the name of the process generation file. Default: pg2.txt.
	static boolean preemptionEnabled;
		
	//Variables/Objects which provide simulation functionality
	static boolean anyIdleCPUs; //are any cpus available right now?
	static CPU[] cpus; //array of cpus in this system
	static ProcGenerator procgens[]; //array of process generators for each process type
	static long eventsMade; //number of created events
	
	//Event Stats
	static long eventsProcessed;
	static long finalEventQueueLength;
	static double avgEventQueueLength;
		
	//Proc stats
	static long finalReadyQueueLength;
	static double avgReadyQueueLength;
	
	//Queues for runtime
	static PriorityQueue<Event> eventQueue; //event queue. Priority is based on Event timestamps.
	static LinkedList<Proc> readyQueue; //process queue for processes waiting to get on a CPU.
	
	//legal event types
	static final String NEWPROC = "New Proc";
	static final String IOFAULT = "I/O Fault";
	static final String IODONE = "I/O Complete";
	static final String PROCDONE = "Proc Complete";
	static final String QUANTUM_UP = "Quantum Expired";
	
	public static void main(String args[]) {
		//initialize values for both simulation's settings and statistics
		initStatsAndSettings();
		
		//acquire our command line parameters. Validate and set simulation settings and variables accordingly.
		if(args.length == 0) {
			System.out.println("Invalid call. Call with --help or -h to see usage details.");
			return;
		}
		else {
			getAndSetParams(args);
		}
		
		//the only parameter that must be supplied is stop-time, make sure we have a valid one. Terminate if we don't
		if(simStopTime <= 0) {
			System.out.println("A simulation stop-time must be supplied to execute. Call with --help or -h to see details.");
			return;	
		}
		
		//if the user doesn't supply their own procgen file, use the default, pg2.txt
		if(procgenFilename == "") {
			procgenFilename = "pg2.txt";
		}
		
		//at this point, all parameters/settings should be acquired, validated, set, and stored.
		//create the event and ready queues
		eventQueue = new PriorityQueue<Event>();
		readyQueue = new LinkedList<Proc>();
		
		//create all system cpus
		createCPUs();
		
		//create process generators
		procgens = createProcGenerators();
		
		//create initial events for each process type, and add them to the event queue
		for(ProcGenerator pg : procgens) {
			//get a new random arrival time
			long nextArrival = pg.calcNextArrival();
			//create initial process creation events, with newly created proc
			Event e = new Event(nextArrival, (pg.generateProc(nextArrival)), "New Proc", eventsMade);
			//print verbose info if enabled
			if(verboseModeEnabled) {
				e.verboseInitEventPrint();
				e.verboseEventPrint();
			}
			//add that event to the event queue
			eventQueue.add(e);
			//increment event counter
			eventsMade++;	
		}
		
		long timeUnitsElapsed = 0; //we are starting at an arbitrary time of 0.
		int iteration = 0; //number of iterations
		long sumEventQ = 0; //sum of all iteration's event queue lengths
		long sumReadyQ = 0; //sum of all iteration's ready queue lengths
		//begin the simulation, and run it until the stop time is reached
		int lastLongestReady = 0;
		int lastLongestEvent = 0;
		while(timeUnitsElapsed <= simStopTime) {
			sumEventQ += eventQueue.size();
			
			//remove next event from event queue for processing
			Event next = eventQueue.poll();
			//processing info if verbose output is enabled
			if(verboseModeEnabled) {
				long timeSinceLast = (next.getTimestamp() - timeUnitsElapsed);
				System.out.println("****Event processing iteration " + iteration + " at time " + next.getTimestamp() + " (" + timeSinceLast + 
					" since last event)");
				System.out.println(next);
			}
			//update time units
			timeUnitsElapsed = next.getTimestamp();
			
			//event now does whatever it needs to do
			processEvent(next);
			eventsProcessed++;
			
			next = null;
			sumReadyQ += readyQueue.size();
			iteration++;
		}
		//timeUnitsElapsed has surpassed stopTime -- end of event processing loop. Gather/calculate last of statistics, and print them
		finalEventQueueLength = eventQueue.size();
		finalReadyQueueLength = readyQueue.size();
		avgReadyQueueLength = (sumReadyQ / (iteration*1.0));
		avgEventQueueLength = (sumEventQ / (iteration*1.0));
		printStats(timeUnitsElapsed);		
	}//	end	of main	method
	
	//method that acquires,	validates, and sets	all	command-line parameters
	public static void getAndSetParams(String params[]) {
		for(int i = 0; i < params.length; i++) {
			int j = i+1;
			String arg = params[i];
			String next = "";
			if(j < params.length) {
				next = params[j];
			}
			switch(arg) {
				case "--stop-time": case "-t":
					simStopTime = Integer.parseInt(next);
					i++;
					break;
				case "--procgen-file": case "-f":
					procgenFilename = next;
					i++;
					break;
				case "--num-cpus": case "-c":
					numcpus = Integer.parseInt(next);
					i++;
					break;
				case "--quantum": case "-q":
					quantum = Integer.parseInt(next);
					i++;
					break;
				case "--switch-time": case "-w":
					switchCost = Integer.parseInt(next);
					i++;
					break;
				case "--no-io-faults": case "-n":
					ioFaultsEnabled = false;
					break;
				case "--batch": case "-b":
					batchOutputEnabled = true;
					break;
				case "--verbose": case "-v":
					verboseModeEnabled = true;
					break;
				case "--help": case "-h":
					printHelp();
					break;	
				default:
					break;
			} //end of switch statement
		}// end of for loop
		
		if(quantum > 0) {
			preemptionEnabled = true;
		}
		else {
			preemptionEnabled = false;
		}
		
	}
	
	//method which initializes all variables for this program.
	public static void initStatsAndSettings() {
		//initialize our simulation/scheduler settings
		verboseModeEnabled = false;
		batchOutputEnabled = false;
		ioFaultsEnabled = true;
		quantum = 0;
		preemptionEnabled = false;
		numcpus = 1;
		switchCost = 0;
		simStopTime = 0;
		procgenFilename = "";
		
		//initialize additional variables
		eventsMade = 0;
		anyIdleCPUs = true;
		
		//initialize the statistics this simulation will gather
		eventsProcessed = 0;
		finalEventQueueLength = 0;
		avgEventQueueLength = 0.0;
		finalReadyQueueLength = 0;
		avgReadyQueueLength = 0.0;
	}
	
	//This message will	be printed whenever	a user calls the program with the --help/-h	parameter.
	public static void printHelp() {
		System.out.println("Usage: ./SchedulerSimulation --stop-time t (Specify the number of time units for which this simultation will run.)\n" +
			"[-f, --procgen-file filename] (Supply your own process generation file. Program will run with pg2.txt by default.)\n" +
			"[-c, --num-cpus n] (Specify the number of CPUs in the simulated system.)\n" +
			"[-q, --quantum] (Enable pre-emption in this simulated scheduler by providing a time quantum for processes running on CPUs)\n" +
			"[-w, --switch-time] (Specify the time it takes for this simulated system to complete a context switch.)\n" +
			"[-n, --no-io-faults] (Disable I/O Faults within this system. Processes will never need to leave a CPU for I/O service.)\n" +
			"[-v, --verbose] (Enable verbose output as the simulation runs, allowing the user to see what's going on.)" +
			"[-b, --batch] (This will print the simulation's statistics output in one parseable batch.)\n" +
			"[-h, --help] (This option will re-print this message.)\n");
	}

  /**Events	perform	specific functions,	(create	subsequent events, manipulate their	procs, etc.) depending on their	type
	*This event	is called on the event most	recenlty polled	from the event queue in	the	event processing loop in main.
	*Possible Events: New proc,	I/O	fault, I/O completed, Proc Completed, Quantum Expired
	*The loadProc()	variants create	events for I/O fault, Quantum Expired, and Proc	Complete, so don't create I/O faults event here, just send them	to the CPU,	and	the	CPU	will create	it.
	*If	Event is io	completion,	quantum	expiration,	or a new proc, proc	will want to get on	a CPU. 
	*If	NewProc, create	another	NewProc	event for that same	type.
	*If	I/O	Fault, create event	for	I/O	completion
	*If	ProcComplete
	*/
	public static void processEvent(Event e) {
		long id = e.geteid();
		long timestamp = e.getTimestamp();
		String type = e.getType();
		Proc eProc = e.getProc();
		int procind = eProc.getTypeInd();
		ProcGenerator pg = procgens[procind];
		
		//get the Proc that's next in line in the ready queue
		Proc nextInLine = null;
		
		if(!readyQueue.isEmpty()) {
			nextInLine = readyQueue.pop();
		}
		
		Event res; //a resulting Event from a loadProc variant call
		Event next; //the next event to be generated without going to the cpu
		
		//if processing an event in which a proc left a cpu, reset that cpu's idle status
		if(type.equals(QUANTUM_UP) || type.equals(PROCDONE) || type.equals(IOFAULT)) {
			int cpu = eProc.getcpuInd();
			cpus[cpu].procLeft();
		}
		
		//NewProc, I/O complete, and Quantum Expiration events will want to have process try to get on a CPU
		if(type.equals(NEWPROC) || type.equals(IODONE) || type.equals(QUANTUM_UP)) {
			if(verboseModeEnabled) {
				e.verboseCreateProc();
			}
			
			//try to find an idle CPU
			int idle = findIdleCPU();
			//if there are no idle CPUs, proc is added to ready queue
			if(idle <  0) {
				readyQueue.add(eProc);
				if(verboseModeEnabled) {
					System.out.println("Adding Proc(" + eProc.getpid() + ") to ready queue.");
				}
			}
			else {
				//if there's an idle CPU and this event's proc is next in the ready queue, send eProc to cpu to generate the next event
				if(nextInLine == null || eProc.equals(nextInLine)) {
					if(verboseModeEnabled) {
					System.out.println("Assigning Proc(" + eProc.getpid() + ") to CPU " + idle + " at time " + timestamp);
					}
				
					if(ioFaultsEnabled && preemptionEnabled) {
						res = cpus[idle].loadProc_IOandPreemption(eProc, timestamp, eventsMade);
						eProc.setcpuInd(idle);
						eventQueue.add(res);
		    			eventsMade++;
						if(verboseModeEnabled) {
							res.verboseEventPrint();
						}
					}
					else if(ioFaultsEnabled && !preemptionEnabled) {
						res = cpus[idle].loadProc_noPreemption(eProc, timestamp, eventsMade);
						eProc.setcpuInd(idle);
						eventQueue.add(res);
		    			eventsMade++;
						if(verboseModeEnabled) {
							res.verboseEventPrint();
						}
					}
					else if(!ioFaultsEnabled && preemptionEnabled) {
		    			res = cpus[idle].loadProc_noIO(eProc, timestamp, eventsMade);
		    			eProc.setcpuInd(idle);
		    			eventQueue.add(res);
		    			eventsMade++;
		    			if(verboseModeEnabled) {
		    				res.verboseEventPrint();
		    			}
					}
		    		else { //both disabled
		    			res = cpus[idle].loadProc_noIOnoPreemption(eProc, timestamp, eventsMade);
		    			eProc.setcpuInd(idle);
		    			eventQueue.add(res);
		    			eventsMade++;
		    			if(verboseModeEnabled) {
		    				res.verboseEventPrint();
		    			}
	    			}
				}//end if nextInLine == null OR nextInLine == eProc
				
				else { //eProc wasn't next in the ready queue. Add it to ready queue, and create an event for the proc from the ready queue
					readyQueue.add(eProc);
					if(verboseModeEnabled) {
						System.out.println("Adding Proc(" + eProc.getpid() + ") to ready queue.");
					}
				
					if(ioFaultsEnabled && preemptionEnabled) {
						res = cpus[idle].loadProc_IOandPreemption(nextInLine, timestamp, eventsMade);
						nextInLine.setcpuInd(idle);
						eventQueue.add(res);
		    			eventsMade++;
						if(verboseModeEnabled) {
							res.verboseEventPrint();
						}
					}
					else if(ioFaultsEnabled && !preemptionEnabled) {
						res = cpus[idle].loadProc_noPreemption(nextInLine, timestamp, eventsMade);
						nextInLine.setcpuInd(idle);
						eventQueue.add(res);
		    			eventsMade++;
						if(verboseModeEnabled) {
							res.verboseEventPrint();
						}
					}
					else if(!ioFaultsEnabled && preemptionEnabled) {
		    			res = cpus[idle].loadProc_noIO(nextInLine, timestamp, eventsMade);
		    			nextInLine.setcpuInd(idle);
		    			eventQueue.add(res);
		    			eventsMade++;
		    			if(verboseModeEnabled) {
		    				res.verboseEventPrint();
		    			}
					}
		    		else { //both disabled
		    			res = cpus[idle].loadProc_noIOnoPreemption(nextInLine, timestamp, eventsMade);
		    			nextInLine.setcpuInd(idle);
		    			eventQueue.add(res);
		    			eventsMade++;
		    			if(verboseModeEnabled) {
		    				res.verboseEventPrint();
		    			}
		    		}
				} // end of else block (corresponding to if nextInLine == null or eProc == nextInLine			
			}// end else block (corresponding to the if idle < 0)		
		
			//NewProc events also create Event for the next process of the same type entering the system
	    	if(type.equals(NEWPROC)) {
	    		//next arrival time for the system is the current time plus a random arrival time
	    		long nextArrival = (pg.calcNextArrival() + e.getTimestamp());
	    		next = new Event(nextArrival, pg.generateProc(nextArrival), NEWPROC, eventsMade);
	    		eventQueue.add(next);
	    		eventsMade++;
	    	}
			
		}// end if NEWPROC || IODONE || QUANTUM_UP block
		
		//I/O fault events necessite a new event for when I/O servicing is complete
		else if(type.equals(IOFAULT)) {
			long timeDone = (e.getTimestamp() + eProc.getIOTime());
			next = new Event(timeDone, eProc, IODONE, eventsMade);
			eventQueue.add(next);
			eventsMade++;
			if(verboseModeEnabled) {
				next.verboseEventPrint();
			}
		}
		//process completes its job on cpu and exits the system. No new events here, just update stats
		else { // .equals(PROCDONE)
			procgens[procind].incComplete();
			eProc.setCompletionTime(e.getTimestamp());
			procgens[procind].setLastTurnaround(eProc.getTurnaround());
			procgens[procind].tryLongestTurnaround(eProc.getTurnaround());	
		}
   }// end of processEvent method
	
	//Method to	print out all the statistics collected during the simulation
	public static void printStats(long elapsed) {
		String r = String.format("%.3f",avgReadyQueueLength);
		String e = String.format("%.3f",avgEventQueueLength);
		
		if(batchOutputEnabled) {
			String s = " ";
			System.out.print(elapsed + s + eventsProcessed + s + finalEventQueueLength + s + e + s +
				finalReadyQueueLength+ s + r + s);
			
		}
		else {
	    	System.out.println("********************************************************************");
	    	System.out.println("Simulation completed execution at time " + elapsed);
	    	System.out.println(eventsProcessed + " events processed");
	    	System.out.println("Event Queue final: " + finalEventQueueLength + " average: " + e);
	    	System.out.println("Ready Queue final: " + finalReadyQueueLength + " average: " + r);
		}
		printProcessStats(elapsed);
		printcpuStats(elapsed);
	}
	
   //This method creates a new ProcessGenerator	for	each process type within the procgen file, and returns all of them in an array.
	public static ProcGenerator[] createProcGenerators() {
		ProcGenerator pgs[];
		int numProcs = 0; //number of processes
		String type = ""; //proc name
		int ind = 0; //proc index in procgen file
		long cj, bj, aj, ij; //average times per type
		Scanner s;
		try {
			s = new Scanner(new FileReader(procgenFilename));
		}
		catch(FileNotFoundException e) {
			System.err.println(e);
			return null;
		}
		
		//first entry in the file will be the number of processes included within it
		numProcs = s.nextInt();
		pgs = new ProcGenerator[numProcs];
		int ctr = 0;
		while(ctr < numProcs) {
			//order for elements is procName, avg cputime, avg burst time, avg interarrival time, and avg io service time
			type = s.next();
			ind = ctr;
			cj = s.nextLong();
			bj = s.nextLong();
			aj = s.nextLong();
			ij = s.nextLong();
			ProcGenerator pg = new ProcGenerator(type, ind, cj, bj, ij, aj);
			pgs[ctr] = pg;
			ctr++;
			//VERBOSE MODE -- print creation confirmation of ProcGenerator with its details.
			if(verboseModeEnabled) {
				System.out.println(pg);
			}
		}
		return pgs;
		
	}
	
	public static void printProcessStats(long elapsed) {
		
	    
		if(batchOutputEnabled) {
			String s = " ";
			for(ProcGenerator pg : procgens) {
				double avg = pg.calcAvgTurnaround();
	    		double thru = pg.calcThroughput(elapsed);
	    		String a = String.format("%.3f",avg);
	    		String t = String.format("%.3f",thru);
				System.out.print(pg.getCompleted() + s + pg.getTypeInd() + s + t + s + pg.getLastTurnaround() + s +
					pg.getLongestTurnaround() + s + a + s);
			}
		}
		else {
	    	for(ProcGenerator pg : procgens) {
	    		double avg = pg.calcAvgTurnaround();
	    		double thru = pg.calcThroughput(elapsed);
	    		String a = String.format("%.3f",avg);
	    		String t = String.format("%.3f",thru);
	    		System.out.println(pg.getCompleted() + " processes of type " + pg.getType() + " completed.");
	    		System.out.println("Throughput: " + t);
	    		System.out.println("Turnaround times: last: " + pg.getLastTurnaround() + ", longest: " + pg.getLongestTurnaround() + ", " +
	    			"average: " + a);
	    	}
		}
	}
	
	//Create all the cpus included in this system
	public static void createCPUs() {
		cpus = new CPU[numcpus];
		for(int i = 0; i < numcpus; i++) {
			cpus[i] = new CPU(quantum, switchCost);
		}
	}
	
   /**Method that searches for the "closest" available CPU,	and	returns	its	index.
	*
	*@return int --	the	index of the first idle	CPU	in the cpus	array
	*/ 
	public static int findIdleCPU() {
		for(int i = 0; i < cpus.length; i++) {
			if(cpus[i].getIdleStatus()) {
				return i;
			}
		}
		//if we don't find an index, return -1, indicating that there are no available CPUS.
		return -1;
	}
	
	public static void printcpuStats(long elapsed) {
		if(batchOutputEnabled) {
			String s = " ";
			int i = 0;
			for(CPU cpu : cpus) {
				System.out.print(cpu.batchString());
			}
			System.out.println("");
		}
		else {
	    	for(int i = 0; i < cpus.length; i++) {
	    		cpus[i].calcPercentageStats(elapsed);
	    		System.out.println("CPU#"+i+": " + cpus[i].toString());
	    	}
		}
	}
	
}