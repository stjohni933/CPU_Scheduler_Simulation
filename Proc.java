/**
 * @(#)Proc.java
 * This class represents a process within our system. What processes, and their details is specified within procgen files.
 *
 * @author Iain St. John -- CSC 432 Operating Systems -- Programming Project 2 CPU Scheduler Simulation
 * @version 1.00 2016/2/23
 */
 
import java.util.Random;

public class Proc {
	private long pid; //the process id
	private String procType; //a process' type
	private int typeInd; //a process type's index in a procgen file
	private long cpuTime; //this process' time requirement to complete on the CPU
	private long burstTime; //the time for which this process will run between I/O Faults
	private long arrivalTime; //the time this process arrived/will arrive in the system
	private long completionTime;
	private long ioTime; //time required to service an I/O fault for this process
	private int cpuInd; //the index in which this process is running
	
	//variables which track how much time a process has left to complete a particular function after being removed from a CPU.
	private long burstLeft;
	private long ioLeft;
	private long serviceLeft;
	
    public Proc(String type, int ind, long id, long cpu, long burst, long io, long arrival) {
    	pid = id;
    	procType = type;
    	typeInd = ind;
    	cpuTime = cpu;
    	burstTime = burst;
    	ioTime = io;
    	arrivalTime = arrival;
    	burstLeft = burst;
    	ioLeft = io;
    	serviceLeft = cpu;	
    }
    //Getter methods
    public long getpid() {
    	return pid;
    }
    
    public String getType() {
    	return procType;
    }
    
    public int getTypeInd() {
    	return typeInd;
    }
    
    public long getIOTime() {
    	return ioTime;
    }
    
    public long getCPUtime() {
    	return cpuTime;
    }
    
    public long getArrivalTime() {
    	return arrivalTime;
    }
    
    public long getBurstTime() {
    	return burstTime;
    }
    
    public void setCompletionTime(long c) {
    	completionTime = c;
    }
    
    public long getTurnaround() {
    	return (completionTime - arrivalTime);
    }
    
    public int getcpuInd() {
    	return cpuInd;
    }
    
    //Setter methods
    public void setcpuInd(int ind) {
    	cpuInd = ind;
    }
    
    //update burst time left until io fault if removed from cpu for quantum expiration 
    public void updateBurstTime(long lowest) {
    	burstLeft -= lowest;
    }
    
    //update cpu service time left until completion if removed from cpu for either an io fault or quantum expiration
    public void updateCPUtime(long lowest) {
    	serviceLeft -= lowest;
    }
    
    //reset burst time until io fault to original value when io fault is completed
    public void resetBurst() {
    	burstLeft = burstTime;
    }
    
    public boolean equals(Proc p) {
    	if(pid == p.getpid()){
    		return true;
    	}
    	else
    		return false;
    }
}