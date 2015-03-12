/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

/**
 * @author Franke, Christoph <franke@ggr-planung.de>
 */

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
import org.opentripplanner.routing.vertextype.OnboardDepartVertex;

public class Result {
	private double[] values;
	private String[] strings;	

	/**
	 * all available types of results, that can be calculated and written as outputs
	 */
    public static final String[] AVAILABLERESULTTYPES = new String[]{
    	"TRAVELTIME", "BOARDINGS", "STARTTIME", "ARRIVALTIME", "WALKINGDISTANCE"
    };
        
    private static String resultTypes = "TRAVELTIME";
    
    private static String bestResultType = "TRAVELTIME";
    
    /**
	 * all available types of results, that can be calculated and written as outputs
	 * 
	 * @param
	 * @
	 */
    public static Map<String, Result> newResults(final Population population, final ShortestPathTree spt){    	
    	Map<String, Result> resultMap = new HashMap<String, Result>();
    	
    	List<String> modelist = Arrays.asList(AVAILABLERESULTTYPES);     	
    	for (String mode : resultTypes.split(",")) {
    		mode = mode.trim();
            if (mode.length() == 0 || !modelist.contains(mode)) {
                continue;
            }
            resultMap.put(mode, new Result(population.size()));
        }    	
    	int i = 0;
        for (Individual individual : population){
        	Sample s = individual.sample;
        	Vertex best = evaluate(s, spt);
            for(String mode: resultMap.keySet()){
            	
            	double value;
                if (best == null)
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
    
    public static Result newResult(final String mode, final Population population, final ShortestPathTree spt){    	
    	Result result = new Result(population.size());
    	
    	List<String> modelist = Arrays.asList(AVAILABLERESULTTYPES); 
    	if (mode.length() == 0 || !modelist.contains(mode))
    		return result;
    	
    	int i = 0;
        for (Individual individual : population){
        	Sample s = individual.sample;
        	Vertex best = evaluate(s, spt);
        	
        	double value;
            if (best == null)
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
     * get the "best" close route vertex to a destination 
     * depending on what is defined as bestResult
     */
    public static Vertex evaluate(final Sample s, final ShortestPathTree spt){
    	if(s == null) return null;
    	
    	State s0 = spt.getState(s.v0);   
    	State s1 = spt.getState(s.v1);
    	//no states near sample -> no adjacent route
    	if ( s0 == null && s1 == null)
    		return null;
    	
    	double value0 = Double.MAX_VALUE;
    	double value1 = Double.MAX_VALUE;
    	if ( s0 != null) value0 = getValue(bestResultType, s.v0, spt);
    	if ( s1 != null) value1 = getValue(bestResultType, s.v1, spt); 
    	
    	return (value0 < value1) ? s.v0 : s.v1;
    }
    
    private static double getValue(final String mode, final Vertex v, final ShortestPathTree spt){
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
    
    private static String toString(final String mode, final double value){
    	String str = "-";
    	//TODO: are there values < 0? (atm indicating no route found)
    	//e.g. pre 1970 would be negative
    	if (value >= 0)
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

    private static long startTime(final Vertex v, final ShortestPathTree spt){
    	GraphPath path = spt.getPath(v, true);
    	return path.getStartTime();
    }
    
    private static long walkingDistance(final Vertex v, final ShortestPathTree spt){
    	State s = spt.getState(v);
    	return (long)s.getWalkDistance();
    }
    
    private static long arrivalTime(final Vertex v, final ShortestPathTree spt){
    	State s = spt.getState(v);
        return s.getTimeSeconds(); 
    }
    
    private static int boardings(final Vertex v, final ShortestPathTree spt) {
        State s = spt.getState(v);
        int boardings = s.getNumBoardings();
        //TODO: transfers or boardings? (transfers = boardings - 1 if not started onboard)
//    	GraphPath path = spt.getPath(v, false);
//        if (boardings > 0 && !(path.states.getFirst().getVertex() instanceof OnboardDepartVertex)) {
//        	boardings--;
//        }
        return boardings; 
    }
    
    private static long traveltime(final Vertex v, final ShortestPathTree spt) {
        State s = spt.getState(v);
        return s.getActiveTime();  //TODO + t
    }
    
    public Result(){
    	this.values = null;
    	this.strings = null;
    }
    
    public Result(final int size){
    	this.values = new double[size];
    	this.strings = new String[size];
    }
    
    public Result(final double[] results){
    	this.values = results;
    	for(int i = 0; i < results.length; i++)
    		this.strings[i] = Double.toString(results[i]);
    }
    
    public Result(final String mode, final double[] results){
    	this.values = results;
    	for(int i = 0; i < results.length; i++)
    		this.strings[i] = toString(mode, results[i]);
    }
    
    public Result(final double[] results, final String[] strings){
    	this.values = results;
    	this.strings = strings;
    }
    
    /**
     * set the types of results, that will be calculated and written to csv
     * 
     * @param rm The comma-seperated types, leading or trailing spaces don't matter e.g. "TRAVELTIME, ARRIVALTIME"
     */        
    public void setResultTypes(final String rm){
    	resultTypes = rm;
    }

	public void setBestResultType(final String mode) {
		bestResultType = mode;
	}

	public double[] getValues() {
		return values;
	}

	public void setResults(final double[] results) {
		this.values = results;
	}
	
	public void setResults(final int pos, final double value) {
		this.values[pos] = value;
	}

	public String[] getStrings() {
		return strings;
	}
	
	public String getString(final int pos) {
		return strings[pos];
	}

	public void setStrings(final String[] str) {
		this.strings = str;
	}
	
	public void setStrings(final int pos, final String str) {
		this.strings[pos] = str;
	}

}
