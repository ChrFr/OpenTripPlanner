package org.opentripplanner.scripting.api;

import java.util.Calendar;
import java.util.Date;
/**
 * results of evaluation 
 * 
 * @author Christoph Franke
 *
 */
public class OtpsResult {

    protected long time = Long.MAX_VALUE;
    protected int boardings = 255;
    protected double walkDistance = Double.MAX_VALUE;	    
    protected Date startTime = null;    
    protected Date arrivalTime = null;   
    protected Date arrivalLastUsedTransit = null; 
	protected String modes = "unknown";
    protected Long waitingTime = null;
    protected Double elevationGained = null;
    protected Double elevationLost = null;
    protected OtpsIndividual individual;
    
	public OtpsIndividual getIndividual() {
		return individual;
	}
	public long getTime() {
		return time;
	}
	public int getBoardings() {
		return boardings;
	}
	public double getWalkDistance() {
		return walkDistance;
	}
	public Date getStartTime() {
		return startTime;
	}
	public Date getArrivalTime() {
		return arrivalTime;
	}
	public String getModes() {
		return modes;
	}
	public Long getWaitingTime() {
		return waitingTime;
	}
	public Double getElevationGained() {
		return elevationGained;
	}
	public Double getElevationLost() {
		return elevationLost;
	}   
    public Date getArrivalLastUsedTransit() {
		return arrivalLastUsedTransit;
	}
}
