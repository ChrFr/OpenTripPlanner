package org.opentripplanner.scripting.api;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.common.model.GenericLocation;
import org.opentripplanner.routing.core.RoutingRequest;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.opentripplanner.standalone.Router;

/**
 *  
 * Batch-Processor for multithreaded evaluation of OtpsBatchRequests 
 *  
 * Example of code (python script):
 * <pre>
 *   # create a batch-processor
 *   batch_processor = self.otp.createBatchProcessor(router)
 *   # evaluate a batch-request with this batch-processor
 *   results = self.batch_processor.evaluate(batch_request)
 * </pre>
 * 
 * @author Christoph Franke
 *
 */
public class OtpsBatchProcessor {
	
	private OtpsPopulation roots;
	private OtpsPopulation individuals;
	private OtpsResultSet[] results;	
	private OtpsBatchRequest request;
	private OtpsRouter router;
	private static final Logger LOG = (Logger) LoggerFactory.getLogger(Router.class);
	
	
    public OtpsBatchProcessor(OtpsRouter router) {    	
    	this.router = router;
    }
    
    /**
     * evaluate the routes between origins and destinations as given by request
     * 
     * @param request The routing request
     * @return A list of result-sets, one result-set per origin (if not arriveby) 
     *         respectively destination (if arriveby), result-sets ordered as appearance 
     *         of origins/destinations in request
     */
    public OtpsResultSet[] evaluate(OtpsBatchRequest request){
    	
    	this.request = request;
    	int nThreads = request.threads;
    	
    	// Set up a thread pool to execute searches in parallel
        LOG.info("Number of threads: {}", nThreads);
        int av_proc = Runtime.getRuntime().availableProcessors();
    	if(nThreads > av_proc){
    		LOG.warn(String.format("Number of threads exceeds number of available processors (=%1$d)!", av_proc));
    	}
        ExecutorService threadPool = Executors.newFixedThreadPool(nThreads);
        // ECS enqueues results in the order they complete (unlike invokeAll, which blocks)
        CompletionService<Void> ecs = new ExecutorCompletionService<Void>(threadPool);
    	
    	// root nodes of the shortest path trees
    	roots = request.req.arriveBy? request.destinations: request.origins;
    	// leaves of the shortest path trees (same for all)
    	individuals = request.req.arriveBy? request.origins: request.destinations;

		SampleFactory sampleFactory = router.router.graph.getSampleFactory();
		
		// link the individuals to the graph
    	for (OtpsIndividual individual: individuals){
			if (!individual.isSampleSet){
				individual.cachedSample = sampleFactory.getSample(individual.lon, individual.lat);
				individual.isSampleSet = true;
			}
		 }	
    	
    	results = new OtpsResultSet[roots.size()];
    	
    	int nTasks = 0;
    	for (OtpsIndividual root: roots){
            ecs.submit(new EvaluationTask(nTasks, root), null);
            nTasks++;
        }

        String rootString = request.req.arriveBy? "destinations": "origins";
        int nCompleted = 0;
        try { // pull Futures off the queue as tasks are finished
            while (nCompleted < nTasks) {
                try {
                    ecs.take().get(); // call get to check for exceptions in the completed task
                    if (request.logProgress > 0 && (nCompleted + 1) % request.logProgress == 0){
                    	String progress = String.format("%d/%d", nCompleted + 1, nTasks);
                    	LOG.info("Processing: {} {} done", progress, rootString);
                    }
                } catch (ExecutionException e) {
                    LOG.error("exception in thread task: {}", e);
                }
                nCompleted++;
            }
        } catch (InterruptedException e) {
            LOG.warn("run was interrupted after {} tasks", nCompleted);
        }
        threadPool.shutdown();
        LOG.info("A total of {} {} processed", nCompleted, rootString);	
    	return results;
    }
    
	private class EvaluationTask implements Runnable {
        
        protected final int i;
        protected final OtpsIndividual root;
        
        public EvaluationTask(int i, OtpsIndividual root) {
            this.i = i;
            this.root = root;
        }
        
        @Override
        public void run() {
        	RoutingRequest req = request.req.clone();
        	//String rootString = request.req.arriveBy? "destinations": "origins";
			//TODO: skip (check if null!)
	    	GenericLocation rootLocation = new GenericLocation(root.lat, root.lon);
			if (request.req.arriveBy)
				req.to = rootLocation;
			else
				req.from = rootLocation;
			
			//TODO: synchronize this block? getting spt may add duplicate vertices
    		OtpsSPT spt = router.getSpt(req); 
			
			if (spt != null){
	    		OtpsResultSet res = evaluate(spt.spt, root);
	    		results[i] = res;
			}
        }        
    }    
    
    private OtpsResultSet evaluate(ShortestPathTree spt, OtpsIndividual root){
		OtpsResultSet res = new OtpsResultSet(root, individuals);
		
		int i = -1;
		for (OtpsIndividual individual: individuals){
			i++; // increment first, because some individuals may be skipped, if not reachable in time 
		    res.evaluations[i] = null;
			//if (skipIndividuals[i])
			//	continue;
			
		    if (individual.cachedSample == null)
		        continue;
		    
		    RoutingRequest req = spt.getOptions();

		    OtpsSample sample = new OtpsSample(individual.cachedSample);
		    long time = individual.cachedSample.eval(spt);
		    if (time == Long.MAX_VALUE) 
		        continue;

			OtpsResult evaluation = new OtpsResult();
			evaluation.individual = individual;
			res.evaluations[i] = evaluation;
		    evaluation.time = time;		    
		    
		    evaluation.boardings = sample.evalBoardings(spt);
		    evaluation.walkDistance = sample.evalWalkDistance(spt);      
		    int timeToItinerary = (int) (sample.evalDistanceToItinerary(spt) / req.walkSpeed);   
		    
		    if(request.evalItineraries){
		        Itinerary itinerary = sample.evalItinerary(spt);
		       
		        boolean arriveby = spt.getOptions().arriveBy;
	    		Calendar c = Calendar.getInstance();
		        Date startTime = itinerary.startTime.getTime();	 
		        Date arrivalTime = itinerary.endTime.getTime();
		        if (arriveby){
		    		c.setTime(startTime);
		    		c.add(Calendar.SECOND, -timeToItinerary);
		        }
		        else {
		    		c.setTime(arrivalTime);
		    		c.add(Calendar.SECOND, timeToItinerary);
		        }
		        evaluation.startTime = startTime;
		        evaluation.arrivalTime = arrivalTime;
		        
		        evaluation.waitingTime = itinerary.waitingTime;
		        evaluation.elevationGained = itinerary.elevationGained;
		        evaluation.elevationLost = itinerary.elevationLost;
		        
		        Set<String> uniqueModes = new HashSet<>();
		        long distance = 0;
		        for (Leg leg: itinerary.legs){
		        	distance += leg.distance;
		        	if(!leg.mode.equals("WALK") && !leg.mode.equals("CAR")){		        		
		        		evaluation.arrivalTransit = leg.endTime.getTime();
		        		if (evaluation.startTransit == null)
		        			evaluation.startTransit = leg.startTime.getTime();
		        	}
		        	uniqueModes.add(leg.mode);
		        }
		        evaluation.distance = distance;
		        evaluation.modes = uniqueModes.toString();
		    }
		}
		return res;
    }

}
