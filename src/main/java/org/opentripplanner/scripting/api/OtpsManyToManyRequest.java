package org.opentripplanner.scripting.api;

import java.util.Calendar;
import java.util.Date;

import org.opentripplanner.routing.core.RoutingRequest;

public class OtpsManyToManyRequest extends OtpsRoutingRequest {
	
	protected OtpsPopulation origins;
	protected OtpsPopulation destinations;
	protected boolean[] skipOrigins;
	protected boolean[] skipDestinations;
	protected Date cutoffTime = null;
	private Long maxTimeSec = null;
	protected int logProgress = 0;

	boolean evalItineraries = true;

	protected OtpsManyToManyRequest(RoutingRequest req) {
		super(req);
		// TODO Auto-generated constructor stub
	}
	
	public void reset(){
		
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

    public void setClampInitialWait(long clampInitialWait) {
        req.clampInitialWait = clampInitialWait;
    }
    
    public void setOrigins(OtpsPopulation origins){
    	this.origins = origins;
    	skipOrigins = new boolean[origins.size()];
    }
    
    public void setDestinations(OtpsPopulation destinations){
    	this.destinations = destinations;
    	skipDestinations = new boolean[destinations.size()];
    }
    
    public void setSkipOrigins(boolean[] skipOrigins){
    	this.skipOrigins = skipOrigins;
    }
    
    public void setSkipDestinations(boolean[] skipDestinations){
    	this.skipDestinations = skipDestinations;
    }

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
    
    /** maximum allowed departure (if not arriveby) resp. arrival time (if arriveby) */
    public void setCutoffTime(int year, int month, int day, int hour, int min, int sec) {
        Calendar cal = Calendar.getInstance(); // Use default timezone
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, sec);
        cal.set(Calendar.MILLISECOND, 0);
        cutoffTime = cal.getTime();
    }

    /** maximum allowed departure (if not arriveby) resp. arrival time (if arriveby) */
    public void setCutoffTime(Date dateTime) {
    	cutoffTime = dateTime;
    }

    /** maximum allowed departure (if not arriveby) resp. arrival time (if arriveby) */
    public void setCutoffTime(long epochSec) {
        cutoffTime = new Date(epochSec * 1000L);
    }
}
