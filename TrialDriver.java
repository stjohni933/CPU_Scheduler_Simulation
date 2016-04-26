import java.util.*;
/**
 * @(#)TrialDriver.java
 *
 *
 * @Iain St. John
 * CSC 431
 * Problem Set 5
 *
 * I finally got my act together and wrote a class that automates the calling of TimingSorts.java.
 * 
 * @version 1.00 2015/3/17
 */

public class TrialDriver {
	//arrays holding different cmd-line parameter values
	//study 1
	public static String qVals[] = {"0","2","5","10","15","20","30","40","50","60","70","80","90","100","125","150","175","200"}; //18
	
	//study 2
	public static String cVals[] = {"1","2","3","4","5","6","7","8","9","10","15","20"}; //12
	
	//final strings for parameter tags
	public static final String Q = "-q";
	public static final String T = "-t";
	public static final String C = "-c";
	public static final String W = "-w";
	public static final String N = "-n";
	public static final String B = "-b";
	
	//final strings for constant parameter values
	public static final String STOP = "10000000"; //10 million time units
	public static final String SWITCH = "10";
	public static final String QUANTUM = "10";
    
    //Run all trials for each study
    public static void main(String args[]) {
    	//build constant part of args string
    	//String params[] = new String[7]; //for study 1
    	String altParams[] = new String[9]; //for study 2
    	/*params[0] = T;
    	params[1] = STOP;
    	params[2] = B;
    	params[3] = W;
    	params[4] = SWITCH;
    	params[5] = Q;
    	//run all trials for study 1
    	for(int i = 0; i < 18; i++) { //each q
    		params[6] = qVals[i];
    		SchedulerSimulation.main(params);
    	}*/
    	
    	//build uniform parts for study 2
    	altParams[0] = T;
    	altParams[1] = STOP;
    	altParams[2] = B;
    	altParams[3] = Q;
    	altParams[4] = QUANTUM;
    	altParams[5] = W;
    	altParams[6] = SWITCH;
    	altParams[7] = C;
    	//run all trials for study 2
        for(int i = 0; i < 12; i++) { //each cpu val
    		altParams[8] = cVals[i];
        	SchedulerSimulation.main(altParams);
        }
        
        
    }//end of main
}//end of class
