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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.resource.GraphPathToTripPlanConverter;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.error.TrivialPathException;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * Extension of the OTP Sample class to get additional information about a sample 
 * 
 * @author Christoph Franke
 */
public class OtpsSample extends Sample {
	
	protected OtpsSample(Vertex v0, int d0, Vertex v1, int d1) {
		super(v0, d0, v1, d1);
	}	

	protected OtpsSample(Sample sample) {
		super(sample.v0, sample.d0, sample.v1, sample.d1);
	}
	
    /**
     * @param spt Tree of shortest paths
     * @return the index (0, 1) of the vertex( incl. walkingDistance) 
     * with the shortest path (by time)
     */
	private int bestIndex(final ShortestPathTree spt){
        State s0 = spt.getState(v0);
        State s1 = spt.getState(v1);
        long m0 = Long.MAX_VALUE;
        long m1 = Long.MAX_VALUE;
        double walkSpeed = spt.getOptions().walkSpeed;
        if (s0 != null)
            m0 = (int)(s0.getActiveTime() + d0 / walkSpeed);
        if (s1 != null)
            m1 = (int)(s1.getActiveTime() + d1 / walkSpeed);
        return (m0 < m1) ? 0 : 1; 
	}

    /**
     * evaluates both vertices of this sample in the given shortest path tree
     * and returns the smaller value of the two resulting ones
     * 
     * already contains the time needed to reach start point of shortest path on foot
     * 
     * @param spt Tree of shortest paths
     * @return shortest travel-time to vertex
     */
	public long eval(final ShortestPathTree spt){
		int idx = bestIndex(spt); 
		Vertex v = (idx == 0) ? v0: v1;
		State s = spt.getState(v);
		int d = (idx == 0) ? d0: d1;

        double walkSpeed = spt.getOptions().walkSpeed;
        if (s == null)
        	return Long.MAX_VALUE;
		long value = (long)(s.getActiveTime() + d / walkSpeed);
		
		return value;
	}	

	public double evalWalkDistance(final ShortestPathTree spt) {
		int idx = bestIndex(spt); 
		Vertex v = (idx == 0) ? v0: v1;
		State s = spt.getState(v);
		int d = (idx == 0) ? d0: d1;
		
        double m = s.getWalkDistance() + d;
        return m;
    }

    /**
     * evaluates the itinerary of the shortest path
     * doesn't take account of time needed to reach itinerary
     * 
     * @param spt Tree of shortest paths
     */
	protected Itinerary evalItinerary(final ShortestPathTree spt){
		int idx = bestIndex(spt); 
		Vertex v = (idx == 0) ? v0: v1;
		int d = (idx == 0) ? d0: d1;
		
		GraphPath path = spt.getPath(v, true);	
		Itinerary itinerary;
		double walkSpeed = spt.getOptions().walkSpeed;
		try{
			itinerary = GraphPathToTripPlanConverter.generateItinerary(path, true, false, new Locale("en"));
		}
		// paths with start = endpoint are trivial (no traverse)
		catch (TrivialPathException e){
			itinerary = new Itinerary();
			Date startDate = new Date(spt.getState(v).getTimeSeconds() * 1000);
			itinerary.startTime = new GregorianCalendar();
			itinerary.startTime.setTime(startDate);
			itinerary.endTime = new GregorianCalendar();
			itinerary.endTime.setTime(startDate);
			itinerary.waitingTime = 0;
			itinerary.endTime.add(Calendar.SECOND, (int) (d / walkSpeed));
		}
		return itinerary;
    }
    
	protected int evalDistanceToItinerary(final ShortestPathTree spt){
		int idx = bestIndex(spt); 
		int d = (idx == 0) ? d0: d1;
		return d;
    }
    
}
