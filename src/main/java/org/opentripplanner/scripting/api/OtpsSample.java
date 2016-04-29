package org.opentripplanner.scripting.api;

import java.util.Date;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.core.State;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.GraphPath;
import org.opentripplanner.routing.spt.ShortestPathTree;

public class OtpsSample extends Sample {
	
	public OtpsSample(Vertex v0, int d0, Vertex v1, int d1) {
		super(v0, d0, v1, d1);
	}	

	public OtpsSample(Sample sample) {
		super(sample.v0, sample.d0, sample.v1, sample.d1);
	}
	
	private interface Evaluation{
		long evaluate(final Vertex v, final ShortestPathTree spt);
	}	
	
	private Long evaluate(final ShortestPathTree spt, Evaluation ev){

		State s0 = spt.getState(v0);   
		State s1 = spt.getState(v1);
		
		if ( s0 == null && s1 == null)
			return null;

		long value0 = Long.MAX_VALUE;
		long value1 = Long.MAX_VALUE;
		if ( s0 != null) value0 = ev.evaluate(v0, spt);
		if ( s1 != null) value1 = ev.evaluate(v1, spt); 
		
		return (value0 < value1) ? value0 : value1;
	}	

    private static long startTime(final Vertex v, final ShortestPathTree spt){
    	GraphPath path = spt.getPath(v, true);
    	return path.getStartTime();
    }    

    private static long arrivalTime(final Vertex v, final ShortestPathTree spt){
    	if(spt.options.arriveBy){
    		GraphPath path = spt.getPath(v, true);
    		return path.getEndTime();
    	}
    	State s = spt.getState(v);
        return s.getTimeSeconds(); 
    }
	
    public Date evalStartTime(final ShortestPathTree spt){    	
		Long value = evaluate(spt, (v, s) -> startTime(v,s));
		return new Date((long)value * 1000);
    }
    
    public Date evalArrivalTime(final ShortestPathTree spt){
		Long value = evaluate(spt, (v, s) -> arrivalTime(v,s));
		return new Date((long)value * 1000);
    }
}
