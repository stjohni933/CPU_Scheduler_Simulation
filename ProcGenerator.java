/**
 * @(#)ProcGenerator.java
 * This class is in charge of creating processes of each type specified within a procgen file.
 *
 * @author Iain St. John -- CSC 432 Operating Systems -- Programming Project 2 CPU Scheduler Simulation
 * @version 1.00 2016/2/23
 */

import java.util.Random;
import java.lang.Math;

public class ProcGenerator {
	private String procType; //name/label for a type of process
	private int procInd; //index for a process type in a procgen file
	private long avgBurst; //average burst time for processes of one type
	private long avgCPUtime; //average time required on the cpu for processes of one type
	private long avgInterarrival; //average arrival time for processes of one type
	private long avgIOtime; //average time to complete i/o service for processes of one type
	
	//statistics for each process type
	private long procsMade; //the number of processes a generator has created
	private long procsCompleted; //number of processes that finish and exit the system
	private long lastTurnaround; //last turnaround time
	private long longestTurnaround; //longest turnaround time
	private long averageTurnaround; //average turnaround time
	private long throughput; //throughput. This is the number of completed processes divided by the simulation runtime in time units
	private long turnaroundSum;
	
   /**Specific constructor method which takes all necessary info from a procgen file
    *
    *@param type -- name of process type
    *@param ind -- index of process type in the procgen file
    *@param cj -- average time required on cpu
    *@param bj -- average burst time
    *@param aj -- average interarrival time
    *@param ij -- average i/o fault service time
    */
    public ProcGenerator(String type, int ind, long cj, long bj, long aj, long ij) {
    	procType = type;
    	procInd = ind;
    	avgBurst = bj;
    	avgCPUtime = cj;
    	avgInterarrival = aj;
    	avgIOtime = ij;	
    		
    	//initialize
    	lastTurnaround = 0;
    	averageTurnaround = 0;
    	throughput = 0;
    	turnaroundSum = 0;
    	longestTurnaround = 0;
    }
    
    public String getType() {
    	return procType;
    }
    
    public int getTypeInd() {
    	return procInd;
    }
    
   //generates a uniformly distributed random number (only used for burst times). 
   private long uniformRand(long val) {
   		long burst = 0;
   		while(burst == 0) {
   			int range = ((int) val * 2);
   			Random r = new Random();
   			burst = (long) r.nextInt(range);
   		}
   		return burst;
   }
   
   //generates an exponentially distributed random value for process cpuTime, ioTime, and arrival time.
   private long exponentialRand(long val) {
   		long time = 0;
   		
   		while(time == 0) {
   			Random r = new Random();
   			double u = r.nextDouble();
   			time = (long) ((Math.log(1.0 - u))/-(1.0/val));
   		}
   		
   		return time;
   }
   
   //uses exponentialRand function to generate the next arrival time.
   //we want the arrival time to be visible to the simulation, which is why this is its own separate function
   public long calcNextArrival() {
   		return exponentialRand(avgInterarrival);
   }
   
   //Method that creates a new process with randomized data. Takes the arrival time as a parameter
   public Proc generateProc(long arrival) {
   		long id = procsMade;
   		long cj = exponentialRand(avgCPUtime);
   		long ij = exponentialRand(avgIOtime);
   		long bj = uniformRand(avgBurst);
    	Proc newProc = new Proc(procType, procInd, id, cj, bj, ij, arrival);
    	procsMade++;
    	return newProc;
   }
   
   public void setLastTurnaround(long t) {
   		lastTurnaround = t;
   		turnaroundSum += lastTurnaround;
   }
   
   public void tryLongestTurnaround(long t) {
   		if(t > longestTurnaround) {
   			longestTurnaround = t;
   		}
   }
   
   public long getLongestTurnaround() {
   		return longestTurnaround;
   }
   
   public long getLastTurnaround() {
   		return lastTurnaround;
   }
   
   public double calcAvgTurnaround() {
   		//will be 0 if no processes finish, which will result in a divide by 0. Avoid this case by switching to 1.
   		if(procsCompleted == 0) {
   			procsCompleted = 1;
   		}
   		return ((turnaroundSum *1.0) / (procsCompleted * 1.0));
   }
   
   public double calcThroughput(long elapsed) {
   		return (procsCompleted / (elapsed*1.0));
   }
   
   //increment completed processed
   public void incComplete() {
   		procsCompleted++;
   }
   
   public long getCompleted() {
        return procsCompleted;
   }
    
    //to string method for this object's creation details with verbose mode enabled
    public String toString() {
    	String s = "Created Proc Generator for '" + procType + "' processes.\n";
    	s += ("   Average time required on CPU: " +	avgCPUtime + "\n");
    	s += ("   Average burst time: " +	avgBurst + "\n");
    	s += ("   Average Interarrival time: " + avgInterarrival + "\n");
    	s += ("   Average I/O Fault service time: " + avgIOtime + "\n");
    	return s;
    }
    
    
}