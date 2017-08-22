package org.opentripplanner.scripting.api;

import java.util.Date;

import org.opentripplanner.routing.core.RoutingRequest;

public class OtpsBatchRequest extends OtpsRoutingRequest {
	
	protected OtpsPopulation origins;
	protected OtpsPopulation destinations;
	protected boolean[] skipOrigins;
	protected boolean[] skipDestinations;
	protected Long maxTimeSec = null;
	protected int logProgress = 0;
	protected int threads = 1;

	boolean evalItineraries = true;
	public Object maxTime;

	protected OtpsBatchRequest(RoutingRequest req) {
		super(req);
	}
	
	/** every nth single routing step will be logged, 0 means no logging of progress */
	public void setLogProgress(int logProgress) {
		this.logProgress = logProgress;
	}

    /** The maximum slope of streets for wheelchair trips. */
    public void setMaxSlope(double maxSlope){
    	req.maxSlope = maxSlope;
    }
    
    /**
     * maximum time (in seconds) of pre-transit travel when using drive-to-transit (park and
     * ride or kiss and ride)
     */
    public void setMaxPreTransitTime(int maxPreTransitTime){
    	req.maxPreTransitTime = maxPreTransitTime;
    }
    
    /** maximum number of transfers*/
    public void setMaxTransfers(int transfers){
    	req.maxTransfers = transfers;
    }

    /** 
     * The maximum wait time in seconds the user is willing to delay trip start. 
     * Is subtracted from the elapsed time of the route, up to a clamp value specified in the request.
     * If the clamp value is set to -1 (default), no clamping will occur.
     * If the clamp value is set to 0, the initial wait time will not be subtracted out 
     */
    public void setClampInitialWait(long clampInitialWait) {
        req.clampInitialWait = clampInitialWait;
    }

    /** the origin points of the routes */
    public void setOrigins(OtpsPopulation origins){
    	this.origins = origins;
    	skipOrigins = new boolean[origins.size()];
    }

    /** the destination points of the routes */
    public void setDestinations(OtpsPopulation destinations){
    	this.destinations = destinations;
    	skipDestinations = new boolean[destinations.size()];
    }
    /*
    public void setSkipOrigins(boolean[] skipOrigins){
    	this.skipOrigins = skipOrigins;
    }
    
    public void setSkipDestinations(boolean[] skipDestinations){
    	this.skipDestinations = skipDestinations;
    }*/
    
    //public void set

    @Override
    public void setMaxTimeSec(long maxTimeSec) {
       this.maxTimeSec = maxTimeSec;
       setWorstTime();
    }    
    
    private void setWorstTime(){
    	if(this.maxTimeSec != null)
    		req.worstTime = req.dateTime + (req.arriveBy ? -maxTimeSec : maxTimeSec);
    }

    @Override
    public void setDateTime(int year, int month, int day, int hour, int min, int sec) {
        super.setDateTime(year, month, day, hour, min, sec);
        setWorstTime();
    }

    @Override
    public void setDateTime(Date dateTime) {
        super.setDateTime(dateTime);
        setWorstTime();
    }

    @Override
    public void setDateTime(long epochSec) {
        super.setDateTime(epochSec);
        setWorstTime();
    }    

    @Override
    public void setArriveBy(boolean arriveBy) {
        super.setArriveBy(arriveBy);
        setWorstTime();
    }
    
    public void setEvalItineraries(boolean evalItineraries) {
    	this.evalItineraries = evalItineraries;
    }
        
    public void setThreads(int threads){
    	this.threads = threads;
    }
}
