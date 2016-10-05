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
import java.util.HashSet;
import java.util.Set;

import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.algorithm.AStar;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.error.VertexNotFoundException;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.standalone.Router;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

/**
 * A router, as returned by the getRouter function of the OTP script entry point.
 * 
 * Example of code (python script):
 * <pre>
 *   # Get the default router
 *   defRouter = otp.getRouter()
 *   # Get the router of ID 'paris'
 *   parisRouter = otp.getRouter('paris')
 * </pre>
 * 
 * @author laurent
 */
public class OtpsRouter {
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(Router.class);

    private Router router;

    public OtpsRouter(Router router) {
        this.router = router;
    }

    /**
     * Plan a route on the router given the various options.
     * 
     * @param req The routing request options (date/time, modes, etc...)
     * @return A Shortest-path-tree (a time+various states for each vertices around the
     *         origin/destination).
     */
    public OtpsSPT plan(OtpsRoutingRequest req) {
        RoutingRequest req2 = req.req.clone();
        return getSpt(req2);
    }

    private OtpsSPT getSpt(RoutingRequest req){
        try {
        	req.setRoutingContext(router.graph);
            // TODO verify that this is indeed the intended behavior.
            ShortestPathTree spt = new AStar().getShortestPathTree(req);
            return new OtpsSPT(spt, router.graph.getSampleFactory());
        } catch (VertexNotFoundException e) {
            // Can happen, not really an error
            return null;
        }
    }

    /**
     * 
     * 
     * @param 
     * @return 
     */
    public OtpsResultSet[] plan(OtpsManyToManyRequest req) {
    	
    	// root of the shortest path tree
    	OtpsPopulation roots = req.req.arriveBy? req.destinations: req.origins;
    	boolean[] skipRoots = req.req.arriveBy? req.skipDestinations: req.skipOrigins;
    	String rootString = req.req.arriveBy? "destinations": "origins";
    	OtpsResultSet[] results = new OtpsResultSet[roots.size()];
    	
    	// leafs of the shortest path tree
    	OtpsPopulation individuals = req.req.arriveBy? req.origins: req.destinations;
    	boolean[] skipIndividuals = req.req.arriveBy? req.skipOrigins: req.skipDestinations;
    	
    	
    	int i = -1;
    	for (OtpsIndividual root: roots){
    		i++;
        	RoutingRequest request = req.req.clone();
    		//TODO: skip (check if null!)
        	GenericLocation rootLocation = new GenericLocation(root.lat, root.lon);
    		if (req.req.arriveBy)
    			request.to = rootLocation;
    		else
    			request.from = rootLocation;
    		
    		OtpsSPT spt = getSpt(request); 
    		
    		if (spt != null){
	    		OtpsResultSet res = evaluate(spt.spt, individuals, skipIndividuals, req.evalItineraries, req.cutoffTime);
	    		res.setSource(root);
	    		results[i] = res;
    		}

            if (req.logProgress > 0 && (i + 1) % req.logProgress == 0)
            	LOG.info("Processing: {} {} processed", i + 1, rootString);
    	}    	
        LOG.info("A total of {} {} processed", i + 1, rootString);	
    	return results;
    }
    
    private OtpsResultSet evaluate(ShortestPathTree spt, OtpsPopulation individuals, boolean[] skipIndividuals, boolean evalItineraries){
    	return evaluate(spt, individuals, skipIndividuals, evalItineraries, null);
    }
    
    private OtpsResultSet evaluate(ShortestPathTree spt, OtpsPopulation individuals, boolean[] skipIndividuals, boolean evalItineraries, Date cutoffTime){
		OtpsResultSet res = new OtpsResultSet(individuals);
		
		Graph sptGraph = spt.getOptions().getRoutingContext().graph;
		int i = -1;
		SampleFactory sampleFactory = router.graph.getSampleFactory();
		for (OtpsIndividual individual: individuals){
			i++; // increment first, because some individuals may be skipped, if not reachable in time 
		    res.evaluations[i] = null;
			if (skipIndividuals[i])
				continue;
			
			if (!individual.isSampleSet || individual.graph != sptGraph) {
				individual.cachedSample = sampleFactory.getSample(individual.lon, individual.lat);
		        // Note: sample can be null here
				individual.graph = sptGraph;
				individual.isSampleSet = true;
		    }		
			
		    if (individual.cachedSample == null)
		        continue;
		    
		    long time = individual.cachedSample.eval(spt);
		    if (time == Long.MAX_VALUE)
		        continue;

			OtpsResult evaluation = new OtpsResult();
			res.evaluations[i] = evaluation;
		    evaluation.time = time;
		    OtpsSample sample = new OtpsSample(individual.cachedSample);
		    
		    evaluation.boardings = sample.evalBoardings(spt);
		    evaluation.walkDistance = sample.evalWalkDistance(spt);      
		    int timeToItinerary = (int) (sample.evalDistanceToItinerary(spt) / spt.getOptions().walkSpeed);   
		    
		    if(evalItineraries){
		        Itinerary itinerary = sample.evalItinerary(spt);
		       
		        boolean arriveby = spt.getOptions().arriveBy;
	    		Calendar c = Calendar.getInstance();
		        Date startTime = itinerary.startTime.getTime();	 
		        Date arrivalTime = itinerary.endTime.getTime();
		        if (arriveby){
		        	if (cutoffTime != null && arrivalTime.compareTo(cutoffTime) > 0)
		        		continue;
		    		c.setTime(startTime);
		    		c.add(Calendar.SECOND, -timeToItinerary);
		        }
		        else {
		        	if (cutoffTime != null && startTime.compareTo(cutoffTime) > 0)
		        		continue;
		    		c.setTime(arrivalTime);
		    		c.add(Calendar.SECOND, timeToItinerary);
		        }
		        evaluation.startTime = startTime;
		        evaluation.arrivalTime = arrivalTime;
		        
		        evaluation.waitingTime = itinerary.waitingTime;
		        evaluation.elevationGained = itinerary.elevationGained;
		        evaluation.elevationLost = itinerary.elevationLost;
		        
		        Set<String> uniqueModes = new HashSet<>();
		        
		        Date arrivalLastUsedTransit = null;
		        for (Leg leg: itinerary.legs){
		        	uniqueModes.add(leg.mode);
		        }
		        evaluation.modes = uniqueModes.toString();
		    }
		}
		return res;
    }
}
