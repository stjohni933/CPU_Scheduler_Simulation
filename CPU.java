/**
 * @(#)CPU.java
 *
 *
 * @author Iain St. John -- CSC 432 Operating Systems -- Programming Project 2 CPU Scheduler Simulation
 * @version 1.00 2016/2/23
 */

import java.lang.Math;

public class CPU {
	private boolean isIdle; //is this CPU currently available?
	private long switchCost; //the cost of switching processes on the CPU, the context switch cost
	private long quantum; //time quantum for pre-emption
	
	//Statistics per CPU
	private long rawActiveTime; //the time this CPU spends running processes in time units
	private long rawIdleTime; //the time this CPU spends waiting in time units
	private long rawSwitchTime; //the time this CPU spends switching contexts in time units
	private double percActive; //percent of total time spent running
	private double percIdle; //percent of total time spent idle
	private double percSwitch; //percent of total time spent switching contexts
	
	private long lastLeft; //the time at which the last proc left this cpu for any reason. Used to calculate idle times.

	
   /**Constructor which allows one to provide a context switch cost
    *
    *@param sc -- the context switch cost
    */ 
    public CPU(long q, long sc) {
    	isIdle = true;
    	quantum = q;
    	switchCost = sc;
    }
    
    //Getter methods
    public boolean getIdleStatus() {
    	return isIdle;
    }
    
    public long getSwitchTime() {
    	return rawSwitchTime;
    }
    
    public long getActiveTime() {
    	return rawActiveTime;
    }
    
    public long getIdleTime() {
    	return rawIdleTime;
    }
    
    public double getIdlePerc() {
    	return percIdle;
    }
    
    public double getActivePerc() {
    	return percActive;
    }
    
    public double getSwitchPerc() {
    	return percSwitch;
    }
    
    //method to reset a CPU's isIdle to true when a proc exits.
    public void procLeft() {
    	isIdle = true;	
    }
    
    //Calculates the percentages for each corresponding CPU stat
    public void calcPercentageStats(long timeUnits) {
    	percActive = (rawActiveTime*1.0) / (timeUnits*1.0);
    	percIdle = (rawIdleTime * 1.0) / (timeUnits * 1.0);
    	percSwitch = (rawSwitchTime * 1.0) / (timeUnits * 1.0);
    }
    
    //returns a printable string of CPU stats in batch form
    public String batchString() {
    	String s = rawActiveTime + " " + rawSwitchTime + " " + rawIdleTime + " ";
    	return s;
    }
    
    //returns a printable string of CPU stats for standard output
    public String toString() {
    	String a = String.format("%.3f",percActive);
    	String i = String.format("%.3f",percIdle);
    	String c = String.format("%.3f",percSwitch);
    	String s = (rawActiveTime + " active (" + a + "%), " + rawSwitchTime + " context switch (" + c + "%), " + 
    		rawIdleTime + " idle (" + i + "%).\n");
    	return s;
    }
    
    //The following methods are all the different variants of loadProc. loadProc loads a particular process into a CPU, and then creates and returns the corresponding event
    //A process will exit a CPU at the lowest time value. 
       
    //IO FAULTS AND PRE-EMPTION ENABLED
    public Event loadProc_IOandPreemption(Proc p, long timestamp, long eid) {
    	//idle time will be the time from when the last left, until the present proc arrives here
    	rawIdleTime += Math.abs(timestamp - lastLeft);
    	isIdle = false;
    	long burst = p.getBurstTime();
    	long length = p.getCPUtime();
    	Event ret = null;
    	
    	//possible event strings in this variant
    	String iofault = "I/O Fault";
		String procdone = "Proc Complete";
		String quantumUp = "Quantum Expired";
		
    	//quantum expires, update burst and service
    	if(quantum < burst && quantum < length) {
    		long t = timestamp + quantum + switchCost;
    		//active time is from when this process entered the cpu, until the time it's slated to leave
    		rawActiveTime += (t - timestamp);
    		//remember the last time at which a proc left the cpu. CPU is idle until the next arrives
    		lastLeft = t;
    		p.updateCPUtime(quantum);
    		p.updateBurstTime(quantum);
    		ret = new Event(t, p, quantumUp, eid);
    	}
    	//io fault, update service time. reset burst
    	else if(burst < quantum && burst < length) {
    		long t = timestamp + burst + switchCost;
    		lastLeft = t;
   			rawActiveTime += (t - timestamp);
   			p.updateCPUtime(burst);
   			p.resetBurst();
   			ret = new Event(t, p, iofault, eid);
   		}
   		//service time is lowest, proc completes
   		else {
   			long t = timestamp + length + switchCost;
   			lastLeft = t;
   			rawActiveTime += (t - timestamp);
   			ret = new Event(t, p, procdone, eid);
   		}
   		
   		//switch cost will never change. Each time this method is called, add the switchCost
    	rawSwitchTime += switchCost;
   		
   		return ret;
    }
    
    //IO FAULTS DISABLED PRE-EMPTION ENABLED
    public Event loadProc_noIO(Proc p, long timestamp, long eid) {
    	rawIdleTime += Math.abs(timestamp - lastLeft);
    	isIdle = false;
    	long length = p.getCPUtime();
    	
    	Event ret = null; //returning event
    	
    	//possible event strings in this variant
		String procdone = "Proc Complete";
		String quantumUp = "Quantum Expired";
    	
    	//quantum expires, update service time
    	if(quantum < length) {
    		long t = timestamp + quantum + switchCost;
    		rawActiveTime += (t - timestamp);
    		lastLeft = t;
    		p.updateCPUtime(quantum);
    		ret = new Event(t, p, quantumUp, eid);
    		
    	}
    	//process completes
    	else {
    		long t = timestamp + length + switchCost;
    		rawActiveTime += (t - timestamp);
    		lastLeft = t;
    		ret = new Event(t, p, procdone, eid);
    		isIdle = true;
    	}
    	
    	//switch cost will never change. Each time this method is called, add the switchCost
    	rawSwitchTime += switchCost;
    	
    	return ret;	
    }
    
    //IO FAULTS ENABLED PRE-EMPTION DISABLED
    public Event loadProc_noPreemption(Proc p, long timestamp, long eid) {
    	rawIdleTime += Math.abs(timestamp - lastLeft);
    	isIdle = false;
    	long burst = p.getBurstTime();
    	long length = p.getCPUtime();
    	
    	Event ret = null; //returning Event
    	
    	//possible event strings in this variant
    	String iofault = "I/O Fault";
		String procdone = "Proc Complete";
    	
    	//io fault, update service time, and reset burst
    	if(burst < length) {
    		long t = timestamp + burst + switchCost;
    		//t is when the proc leaves the cpu, while timestamp is when it entered.  The active time of the CPU is the difference of those times.
    		rawActiveTime += (t - timestamp);
    		lastLeft = t;
    		p.updateCPUtime(burst);
    		p.resetBurst();
    		ret = new Event(t, p, iofault, eid);
    	}
    	//proc completes on cpu
    	else {
    		long t = timestamp + length + switchCost;
    		rawActiveTime += (t - timestamp);
    		lastLeft = t;
    		ret = new Event(t, p, procdone, eid);
    	}
    	
    	//switch cost will never change. Each time this method is called, add the switchCost
    	rawSwitchTime += switchCost;
    	
    	return ret;
    }
    
    //IO FAULTS AND PRE-EMPTION DISABLED
    public Event loadProc_noIOnoPreemption(Proc p, long timestamp, long eid) {
    	
    	Event ret = null;
    	
    	rawIdleTime += Math.abs(timestamp - lastLeft);
    	isIdle = false;
    	
    	//get proc's service time
    	long length = p.getCPUtime();
    	
    	//possible event strings in this variant
		String procdone = "Proc Complete";
    	
    	long t = timestamp + length + switchCost;
    	rawActiveTime += (t - timestamp);
    	lastLeft = t;
    	ret = new Event(t, p, procdone, eid);
    	
    	//switch cost will never change. Each time this method is called, add the switchCost
    	rawSwitchTime += switchCost;
    	
    	return ret;
    }
}