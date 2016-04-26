/**
 * @(#)Event.java
 * Class representing an event within our simulation. Whenever anything happens to a process, or a process does anything, that is an event.
 * Types: new process, I/O fault, I/O complete, process complete. 
 *
 * @author Iain St. John -- CSC 432 Operating Systems -- Programming Project 2 CPU Scheduler Simulation
 * @version 1.00 2016/2/23
 */

import java.util.*;

public class Event implements Comparable<Event> {
	private long eid; //event id of this event
	private Proc myProc; //the process with which an event is associated
	private String procType; //process type name
	private String eventType; //short descriptor of what generated this event
	private long timestamp; //the timestamp (in time units) for this event
	
   /**
    *Constructor method which takes all information necessary to create an Event
    *
    *@param time -- the timestamp for this new event
    *@param proc -- the associated process
    *@param type -- short descriptor for what is generating this event
    *@param event -- this event's id
    *@param verbose
    *@return Event -- a new Event with its data set to the parameters
    */
    public Event(long time, Proc proc, String type, long event) {
    	timestamp = time;
    	myProc = proc;
    	eid = event;
    	eventType = type;
    }
    	
    //Getter methods
    public long getpid() {
    	return myProc.getpid();
    }
    
    public long geteid() {
    	return eid;
    }
    
    public long getTimestamp() {
    	return timestamp;
    }
    
    public String getType() {
    	return eventType;
    }
    
    public Proc getProc() {
    	return myProc;
    }
    
    //VERBOSE MODE -- print creation message for a new process.
    public void verboseCreateProc() {
    	String s = ("Created Proc(" + myProc.getpid() + ") Type: " + myProc.getType());
    	s += (" at time: " + timestamp + " CPU service time: " + myProc.getCPUtime() + " Burst time: " + myProc.getBurstTime());
    	s += (" I/O time: " + myProc.getIOTime());
    	System.out.println(s);
    }

    //VERBOSE MODE -- prints confirmation of Event creation
    public void verboseEventPrint() {
    	System.out.println("New Event: EventID(" + eid + "), Type: " + eventType + " of type " + myProc.getType() + "(" + myProc.getTypeInd() +
    		"), Timestamp: " + timestamp);	
    }
    
    //VERBOSE MODE -- prints confirmation of initial Event creation at the start of the simulation
    public void verboseInitEventPrint() {
    	System.out.println("Added initial process generation event for type " + myProc.getType() + "(" + myProc.getTypeInd() + ") at time " + timestamp);
    }
    
    public String toString() {
    	String s = ("Event ID:" +eid + ", Type: " + eventType + ", Process: " + myProc.getpid() + ", Timestamp: " + timestamp);
    	return s;
    }
    
    //comparison method to complete Comparable implementation. Events are stored in a priority queue, and should be ordered by their timestamps
    @Override
    public int compareTo(Event other) {
    	return (int)(timestamp - other.timestamp);
    }
    
}