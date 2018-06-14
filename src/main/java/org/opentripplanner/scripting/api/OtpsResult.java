package org.opentripplanner.scripting.api;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * results of evaluation 
 * 
 * @author Christoph Franke
 *
 */
public class OtpsResult implements Comparable<OtpsResult> {

    protected long time = Long.MAX_VALUE;
    protected int boardings = 255;
    protected double walkDistance = Double.MAX_VALUE;	    
    protected Date startTime = null;    
    protected Date arrivalTime = null;   
    protected Date arrivalTransit = null; 
    protected Date startTransit = null; 
    protected long distance = 0;
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
	public String getStartTime(String format) {
		return formatDate(startTime, format);
	}
	public Date getArrivalTime() {
		return arrivalTime;
	}
	public String getArrivalTime(String format) {
		return formatDate(arrivalTime, format);
	}
    public Date getStartTransit() {
		return startTransit;
	}
    public String getStartTransit(String format) {
		return formatDate(startTransit, format);
	}
    public Date getArrivalTransit() {
		return arrivalTransit;
	}
    public long getDistance() {
		return distance;
	}
    public String getArrivalTransit(String format) {
		return formatDate(arrivalTransit, format);
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
    public Long getTransitTime(){
    	if (startTransit == null || arrivalTransit == null)
    		return null;
    	return (arrivalTransit.getTime() - startTransit.getTime()) / 1000;
    }
	@Override
	public int compareTo(OtpsResult arg0) {		
		return (int) (time - arg0.time);
	}
	
	private String formatDate(Date date, String format){
		if (date == null)
			return "";
		return new SimpleDateFormat(format).format(date);
	}
    
}
