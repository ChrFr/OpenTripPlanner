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

package org.opentripplanner.scripting.api;

import java.util.Date;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * Extension of the OTP Sample class to get additional information about a sample 
 * 
 * @author Christoph Franke
 */
public class OtpsSample extends Sample {
	
	public OtpsSample(Vertex v0, int d0, Vertex v1, int d1) {
		super(v0, d0, v1, d1);
	}	

	public OtpsSample(Sample sample) {
		super(sample.v0, sample.d0, sample.v1, sample.d1);
	}
	
	private interface Evaluation{
		long evaluate(final Vertex v, final int d, final ShortestPathTree spt);
	}	

    /**
     * evaluates both vertices of this sample in the given shortest path tree
     * with the given Evaluation and returns the smaller value of the two resulting ones
     * 
     */
	private Long evaluate(final ShortestPathTree spt, Evaluation ev){

		State s0 = spt.getState(v0);   
		State s1 = spt.getState(v1);
		
		if ( s0 == null && s1 == null)
			return null;

		long value0 = Long.MAX_VALUE;
		long value1 = Long.MAX_VALUE;
		if ( s0 != null) value0 = ev.evaluate(v0, d0, spt);
		if ( s1 != null) value1 = ev.evaluate(v1, d1, spt); 
		
		return (value0 < value1) ? value0 : value1;
	}	

    private static long startTime(final Vertex v, final int d, final ShortestPathTree spt){
    	if(spt.options.arriveBy){
    		State s = spt.getState(v);
            return s.getTimeSeconds() - (long)(d / spt.getOptions().walkSpeed); 
    	}
    	GraphPath path = spt.getPath(v, true);
    	return path.getStartTime();
    }        

    private static long arrivalTime(final Vertex v, final int d, final ShortestPathTree spt){
    	if(spt.options.arriveBy){
    		GraphPath path = spt.getPath(v, true);
    		return path.getEndTime();
    	}
    	State s = spt.getState(v);
        return s.getTimeSeconds() + (long)(d / spt.getOptions().walkSpeed); 
    }

    // there is a bug in Sample.evalWalkDistance: comparisons with Double.NaN can never be true (so always m1 is taken, even if NaN)
    public double evalWalkDistance(final ShortestPathTree spt) {
        State s0 = spt.getState(v0);
        State s1 = spt.getState(v1);
        double m0 = Double.MAX_VALUE;
        double m1 = Double.MAX_VALUE;
        if (s0 != null)
            m0 = (s0.getWalkDistance() + d0);
        if (s1 != null)
            m1 = (s1.getWalkDistance() + d1);
        return (m0 < m1) ? m0 : m1;
    }
    
//    public String evalTrips(final ShortestPathTree spt){
//    	GraphPath path = spt.getPath(v0, true);
//    	if (path == null) return "[]";
//    	List<AgencyAndId> trips = path.getTrips();
//    	return trips.toString();
//    }
    
//    public String evalModes(final ShortestPathTree spt){
//    	// TODO: iterate States of path, State.stateData.nonTransitMode? (seems to be always the same by reading the code)
//    	GraphPath path = spt.getPath(v0, true);
//    	if(path == null)
//    		return "";
//    	HashSet<TraverseMode> modes = new HashSet<>();
//    	for(State state: path.states){
//    		modes.add(state.getNonTransitMode());
//    	}
//    	return Arrays.toString(modes.toArray());
//    }

    /**
     * @param spt the ShortestPathTree with respect to which this sample will be evaluated
     * @return the start time of the trip to reach this Sample point from the SPT's origin
     */
    public Date evalStartTime(final ShortestPathTree spt){    
    	double distanceFactor = spt.options.arriveBy? 1. / spt.getOptions().walkSpeed: 0;
		Long value = evaluate(spt, (v, d, s) -> startTime(v, d, s));
		return new Date((long)value * 1000);
    }

    /**
     * @param spt the ShortestPathTree with respect to which this sample will be evaluated
     * @return the arrival time of the trip to reach this Sample point from the SPT's origin
     */
    public Date evalArrivalTime(final ShortestPathTree spt){
    	double distanceFactor = spt.options.arriveBy? -1. / spt.getOptions().walkSpeed: 1. / spt.getOptions().walkSpeed;
		Long value = evaluate(spt, (v, d, s) -> arrivalTime(v, d, s));
		return new Date((long)value * 1000);
    }
}
