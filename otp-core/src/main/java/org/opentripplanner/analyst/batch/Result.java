package org.opentripplanner.analyst.batch;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class Result {
	private double[] values;
	private String[] strings;	

    public static final String[] AVAILABLERESULTS = new String[]{
    	"TRAVELTIME", "BOARDINGS", "STARTTIME", "ARRIVALTIME", "WALKINGDISTANCE"
    };
        
    private static String resultModes = "TRAVELTIME";
    
    private static String bestMode = "TRAVELTIME";

    public static Map<String, Result> newResults(Population population, ShortestPathTree spt){    	
    	Map<String, Result> resultMap = new HashMap<String, Result>();
    	
    	List<String> modelist = Arrays.asList(AVAILABLERESULTS);     	
    	for (String mode : resultModes.split(",")) {
    		mode = mode.trim();
            if (mode.length() == 0 || !modelist.contains(mode)) {
                continue;
            }
            resultMap.put(mode, new Result(population.size()));
        }    	
    	int i = 0;
        for (Individual individual : population){
            for(String mode: resultMap.keySet()){
            	Sample s = individual.sample;
            	Vertex best = evaluate(s, spt);
            	
            	double value;
                if (s == null)
                	value = -2;
                else
                	value = getValue(mode, best, spt);          	
                if (value == Long.MAX_VALUE)
                	value = -1;
                
                String str = toString(mode, value);

                Result result = resultMap.get(mode);
                result.setResults(i, value);
                result.setStrings(i, str);
            }
            i++;
        }
    	return resultMap;
    }
    
    public static Result newResult(String mode, Population population, ShortestPathTree spt){    	
    	Result result = new Result(population.size());
    	
    	List<String> modelist = Arrays.asList(AVAILABLERESULTS); 
    	if (mode.length() == 0 || !modelist.contains(mode))
    		return result;
    	
    	int i = 0;
        for (Individual individual : population){
        	Sample s = individual.sample;
        	Vertex best = evaluate(s, spt);
        	
        	double value;
            if (s == null)
            	value = -2;
            else
            	value = getValue(mode, best, spt);          	
            if (value == Long.MAX_VALUE)
            	value = -1;
            
            String str = toString(mode, value);
            result.setResults(i, value);
            result.setStrings(i, str);
            i++;
        }
    	return result;
    }
    
    /**
     * get the "best" close vertex to a destination depending on what is defined
     * as bestResult
     */
    public static Vertex evaluate(Sample s, ShortestPathTree spt){
    	double value0 = getValue(bestMode, s.v0, spt);
    	double value1 = getValue(bestMode, s.v1, spt);    	
    	return (value0 < value1) ? s.v0 : s.v1;
    }
    
    private static double getValue(String mode, Vertex v, ShortestPathTree spt){
    	double value = Long.MAX_VALUE;
    	if(mode.equals("STARTTIME"))
    		value = startTime(v, spt);
    	else if(mode.equals("ARRIVALTIME"))
    		value = arrivalTime(v, spt);
    	else if(mode.equals("TRAVELTIME"))
    		value = traveltime(v, spt);
    	else if(mode.equals("BOARDINGS"))
    		value = boardings(v, spt);  
    	else if(mode.equals("WALKINGDISTANCE"))
    		value = walkingDistance(v, spt);     
        return value;
    }
    
    private static String toString(String mode, double value){
    	String str = null;
    	if(mode.equals("STARTTIME") || mode.equals("ARRIVALTIME")){
    		Date date = new Date((long)value * 1000);
    		str = date.toString();
    	}
    	else if(mode.equals("TRAVELTIME")){   
    		int hours = (int)value / 3600;
    		int rest = (int)value % 3600; 
    		int minutes = rest / 60; 
    		int seconds = rest % 60; 

    		str = ( (hours < 10 ? "0" : "") + hours 
    				+ ":" + (minutes < 10 ? "0" : "") + minutes 
    				+ ":" + (seconds < 10 ? "0" : "") + seconds );     		 
        }
    	else
    		str = Double.toString(value);
    	return str;
    }    

    private static long startTime(Vertex v, ShortestPathTree spt){
    	GraphPath path = spt.getPath(v, true);
    	return path.getStartTime();
    }
    
    private static long walkingDistance(Vertex v, ShortestPathTree spt){
    	State s = spt.getState(v);
    	return (long)s.getWalkDistance();
    }
    
    private static long arrivalTime(Vertex v, ShortestPathTree spt){
    	State s = spt.getState(v);
        return s.getTimeSeconds(); 
    }
    
    private static byte boardings(Vertex v, ShortestPathTree spt) {
        State s = spt.getState(v);
        return (byte) (s.getNumBoardings()); 
    }
    
    private static long traveltime(Vertex v, ShortestPathTree spt) {
        State s = spt.getState(v);
        return s.getActiveTime();  //TODO + t
    }
    
    public Result(){
    	this.values = null;
    	this.strings = null;
    }
    
    public Result(int size){
    	this.values = new double[size];
    	this.strings = new String[size];
    }
    
    public Result(double[] results){
    	this.values = results;
    	for(int i = 0; i < results.length; i++)
    		this.strings[i] = Double.toString(results[i]);
    }
    
    public Result(String mode, double[] results){
    	this.values = results;
    	for(int i = 0; i < results.length; i++)
    		this.strings[i] = toString(mode, results[i]);
    }
    
    public Result(double[] results, String[] strings){
    	this.values = results;
    	this.strings = strings;
    }
        
    public void setResultModes(String rm){
    	resultModes = rm;
    }

	public void setBestMode(String mode) {
		bestMode = mode;
	}

	public double[] getValues() {
		return values;
	}

	public void setResults(double[] results) {
		this.values = results;
	}
	
	public void setResults(int pos, double value) {
		this.values[pos] = value;
	}

	public String[] getStrings() {
		return strings;
	}
	
	public String getString(int pos) {
		return strings[pos];
	}

	public void setStrings(String[] str) {
		this.strings = str;
	}
	
	public void setStrings(int pos, String str) {
		this.strings[pos] = str;
	}

}
