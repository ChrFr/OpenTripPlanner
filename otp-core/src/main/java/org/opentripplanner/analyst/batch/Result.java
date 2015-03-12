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



package org.opentripplanner.analyst.batch;

import java.util.Arrays;
import java.util.Date;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * @author Franke, Christoph <franke@ggr-planung.de>
 */
public class Result {
	private double[] values;
	private String[] strings;	
	private String type;

	/**
	 * all available types of results, that can be calculated and written as outputs
	 */
    public static final String[] AVAILABLERESULTTYPES = new String[]{
    	"TRAVELTIME", "BOARDINGS", "STARTTIME", "ARRIVALTIME", "WALKINGDISTANCE"
    };
        
    public Result(final String type){
    	this.type = type;    	
    	this.values = new double[0];
    	this.strings = new String[0];
    }
    
    public Result(final String type, int size){
    	this.type = type;    	
    	this.values = new double[size];
    	Arrays.fill(values, -2);
    	this.strings = new String[size];
    	Arrays.fill(strings, "-");
    	
    }
    
    public Result(final String type, double[] values){
    	this.type = type;    	
    	this.values = values;
    	strings = new String[values.length];
    	for(int i = 0; i < values.length; i++){
    		strings[i] = valueToString(values[i]);
    	}
    }
    
    public Result(final String type, double[] values, String[] strings){
    	this.type = type;    	
    	this.values = values;
    	this.strings = strings;
    }
        
    public void insertVertex(final int pos, final Vertex v, final ShortestPathTree spt){ 
    	double value = evaluate(type, v, spt);
    	values[pos] = value;   	
    	strings[pos] = valueToString(value);
    }
        
    private static double evaluate(final String type, final Vertex v, final ShortestPathTree spt){
    	double value = Long.MAX_VALUE;
    	if(type.equals("STARTTIME"))
    		value = startTime(v, spt);
    	else if(type.equals("ARRIVALTIME"))
    		value = arrivalTime(v, spt);
    	else if(type.equals("TRAVELTIME"))
    		value = traveltime(v, spt);
    	else if(type.equals("BOARDINGS"))
    		value = boardings(v, spt);  
    	else if(type.equals("WALKINGDISTANCE"))
    		value = walkingDistance(v, spt); 
    	return value;
    }
    
    protected static Vertex evaluate(final String type, final Sample s, final ShortestPathTree spt){
		if(s == null) return null;
		
		State s0 = spt.getState(s.v0);   
		State s1 = spt.getState(s.v1);
		//no states near sample -> no adjacent route
		if ( s0 == null && s1 == null)
			return null;
		
		double value0 = Double.MAX_VALUE;
		double value1 = Double.MAX_VALUE;
		if ( s0 != null) value0 = evaluate(type, s.v0, spt);
		if ( s1 != null) value1 = evaluate(type, s.v1, spt); 
		
		return (value0 < value1) ? s.v0 : s.v1;
    }
    
    private String valueToString(final double value){
    	String str = "-";
    	//TODO: are there values < 0? (atm indicating no route found)
    	//e.g. pre 1970 would be negative
    	if (value >= 0)
	    	if(type.equals("STARTTIME") || type.equals("ARRIVALTIME")){
	    		Date date = new Date((long)value * 1000);
	    		str = date.toString();
	    	}
	    	else if(type.equals("TRAVELTIME")){   
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
    
    public void addVertices(Vertex[] vertices, final ShortestPathTree spt){
    	for(int i = 0; i < vertices.length; i++)
    		insertVertex(i, vertices[i], spt);
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
