package org.opentripplanner.analyst.batch;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class Result {
	private double[] values;
	private String[] strings;	

    public static final String[] AVAILABLERESULTS = new String[]{
    	"TRAVELTIME" , "BOARDINGS", "STARTTIME", "ARRIVALTIME"
    };
        
    private static String resultModes = "TRAVELTIME";

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
                Result result = resultMap.get(mode);
                double value = getValue(mode, s, spt);
                String str = toString(mode, value);
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
            double value = getValue(mode, s, spt);
            String str = toString(mode, value);
            result.setResults(i, value);
            result.setStrings(i, str);
            i++;
        }
    	return result;
    }
    
    private static double getValue(String mode, Sample s, ShortestPathTree spt){
    	double value = Long.MAX_VALUE;
        if (s == null)
        	value = -2;
        else
        	if(mode.equals("STARTTIME"))
        		value = s.startTime(spt);
        	else if(mode.equals("ARRIVALTIME"))
        		value = s.arrivalTime(spt);
        	else if(mode.equals("TRAVELTIME"))
        		value = s.eval(spt);
        	else if(mode.equals("BOARDINGS"))
        		value = s.evalBoardings(spt);        	
        if (value == Long.MAX_VALUE)
        	value = -1;
        return value;
    }
    
    private static String toString(String mode, double value){
    	String str = null;
    	if(mode.equals("STARTTIME") || mode.equals("ARRIVALTIME")){
    		Date date = new Date((long)value);
    		str = date.toString();
    	}
    	else if(mode.equals("BOARDINGS"))
    		str = Double.toString(value);
    	else if(mode.equals("TRAVELTIME")){   
    		int hours = (int)value / 3600;
    		int rest = (int)value % 3600; 
    		int minutes = rest / 60; 
    		int seconds = rest % 60; 

    		str = ( (hours < 10 ? "0" : "") + hours 
    				+ ":" + (minutes < 10 ? "0" : "") + minutes 
    				+ ":" + (seconds < 10 ? "0" : "") + seconds );     		 
        }
    	return str;
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
