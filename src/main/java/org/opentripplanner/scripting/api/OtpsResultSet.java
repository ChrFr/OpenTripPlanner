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

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.analyst.batch.Population;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opentripplanner.analyst.batch.BasicPopulation;
import org.opentripplanner.analyst.batch.ResultSet;

/**
 * A set of results, used for wrapping results for aggregations/accumulations
 * 
 * @author Christoph Franke
 */
public class OtpsResultSet{	

	public static enum AggregationMode { THRESHOLD_SUM_AGGREGATOR, WEIGHTED_AVERAGE_AGGREGATOR, THRESHOLD_CUMMULATIVE_AGGREGATOR, DECAY_AGGREGATOR }
	public static enum AccumulationMode { DECAY_ACCUMULATOR, THRESHOLD_ACCUMULATOR }
	
	protected ResultSet resultSet;
	protected List<OtpsEvaluatedIndividual> evaluatedIndividuals;
	private AggregationMode aggregationMode = AggregationMode.THRESHOLD_SUM_AGGREGATOR;
	private AccumulationMode accumulationMode = AccumulationMode.THRESHOLD_ACCUMULATOR;
	private OtpsIndividual source;

    /**
     * @return most likely the travel times, respectively accumulated values
	 *
     */
	protected OtpsResultSet(){
		this.evaluatedIndividuals = new ArrayList<>();
	}
	
	private static ResultSet createBasicResultSet(List<OtpsIndividual> individuals, double[] results){
		Population basicPop = new BasicPopulation();
		for(OtpsIndividual individual: individuals){
			OtpsLatLon pos = individual.getLocation();	
			Individual ind = new Individual("", pos.getLon(), pos.getLat(), 0);
			basicPop.addIndividual(ind);
		}
		basicPop.setup(); // doesn't do anything useful, but needed to init basicPop.skip[] with False		
		return new ResultSet(basicPop, results);
	}
	
	protected void setResults(List<OtpsIndividual> individuals, double[] results){
		if(individuals.size() != results.length)
			throw new IllegalArgumentException("Results and individuals have to be of same length.");
		this.resultSet = createBasicResultSet(individuals, results);
		// reset evaluated ind. with default values
		this.evaluatedIndividuals = new ArrayList<>();
		for(OtpsIndividual individual: individuals)
			evaluatedIndividuals.add(new OtpsEvaluatedIndividual(individual));		
	}
	
	protected void setResults(List<OtpsEvaluatedIndividual> evaluatedIndividuals) {
		this.evaluatedIndividuals = evaluatedIndividuals;
		double[] results = new double[evaluatedIndividuals.size()];
		List<OtpsIndividual> individuals = new ArrayList<>();
		for(int i = 0; i < evaluatedIndividuals.size(); i++){			
			OtpsEvaluatedIndividual evaluatedIndividual = evaluatedIndividuals.get(i);		
			results[i] = Double.MAX_VALUE;				
			OtpsIndividual individual = evaluatedIndividual.getIndividual();
			individuals.add(individual);
			Long time = evaluatedIndividual.getTime();
			if (time != null)
				results[i] = time;
		}
		this.resultSet = createBasicResultSet(individuals, results);
	}

	public OtpsIndividual getSource() {
		return source;
	}

	public void setSource(OtpsIndividual source) {
		this.source = source;
	}

	private void setInput(String inputField){
		int i = 0;
		for(Individual individual: resultSet.population){

			Double input = evaluatedIndividuals.get(i).getIndividual().getFloatData(inputField);	
			if (input == null)
				throw new IllegalArgumentException("Field " + inputField + " not found");
			individual.input = input;
		}		
	}
	
	public double aggregate(String inputField, Double[] params){
		setInput(inputField);
        OtpsAggregate aggregator = new OtpsAggregate(aggregationMode, params);           
        return aggregator.computeAggregate(this);  
	}
	
	public double aggregate(String inputField){     
        return aggregate(inputField, null);  
	}

    /**
     * merge this set with the given one, keeping the shortest trips (with smallest times)
     * both sets have to be of the same length 
     * 
     * @return merged set of results
	 *
     */	
	public OtpsResultSet merge(OtpsResultSet setToMerge){
		if (setToMerge.length() != length())
			throw new IllegalArgumentException("The length of the set to merge differs from the length of this set.");
		
		Long[] times = getTimes();
		Long[] timesToMerge = setToMerge.getTimes();
		OtpsEvaluatedIndividual bestIndividual;
		List<OtpsEvaluatedIndividual> evaluatedIndividuals = new ArrayList<>();
		for(int i = 0; i < length(); i++){			
			// take passed individual only if reachable and time is smaller
			if (timesToMerge[i] != null && (times[i] == null || timesToMerge[i] < times[i]))
				bestIndividual = setToMerge.evaluatedIndividuals.get(i);
			else
				bestIndividual = this.evaluatedIndividuals.get(i);
			evaluatedIndividuals.add(bestIndividual);			
		}
		OtpsResultSet mergedResultSet = new OtpsResultSet();
		mergedResultSet.setResults(evaluatedIndividuals);
		mergedResultSet.setSource(source);
		return mergedResultSet;
	}
	/*
	public OtpsResultSet append(OtpsResultSet setToAppend){
		OtpsResultSet newResultSet = new OtpsResultSet();
		newResultSet.setResults(evaluatedIndividuals);
		newResultSet.setSource(source);
		return newResultSet;
	}*/
	
	public void setAggregationMode(AggregationMode mode){
		aggregationMode = mode;
	}
	
	public void setAggregationMode(String mode){
		setAggregationMode(AggregationMode.valueOf(mode));
	}

	public void setAggregationMode(int mode){
		setAggregationMode(AggregationMode.values()[mode]);
	}
	
	public void accumulate(OtpsResultSet accumulated, String inputField, Double[] params){ 
		double amount = source.getFloatData(inputField);
		OtpsAccumulate accumulator = new OtpsAccumulate(accumulationMode, params);           
        accumulator.computeAccumulate(this, accumulated, amount);
	}
	
	public void accumulate(OtpsResultSet accumulated, String inputField){    
        accumulate(accumulated, inputField, null);  
	}
	
	public void setAccumulationMode(AccumulationMode mode){
		accumulationMode = mode;
	}
	
	public void setAccumulationMode(String mode){
		setAccumulationMode(AccumulationMode.valueOf(mode));
	}

	public void setAccumulationMode(int mode){
		setAccumulationMode(AccumulationMode.values()[mode]);
	}
	
    /**
     * @return The actual times, the trips to the indivduals started
	 *
     */
	public Date[] getStartTimes() {
		Date[] startTimes = new Date[length()];
		for (int i = 0; i < length(); i++)
			startTimes[i] = evaluatedIndividuals.get(i).getStartTime();
		return startTimes;
	}

	/**
     * @return the first start time out of all results
	 *
     */
	public Date getMinStartTime(){
		Date minStartTime = new Date(Long.MAX_VALUE);
		for (int i = 0; i < length(); i++){
			Date time = evaluatedIndividuals.get(i).getStartTime();
			if (time != null && time.compareTo(minStartTime) < 0)
				minStartTime = time;
		}
		return minStartTime;
	}

	/**
     * @return the first start time out of all results
	 *
     */
	public Date getMinArrivalTime(){
		Date minArrivalTime = new Date(Long.MAX_VALUE);
		for (int i = 0; i < length(); i++){
			Date time = evaluatedIndividuals.get(i).getArrivalTime();
			if (time.compareTo(minArrivalTime) < 0)
				minArrivalTime = time;
		}
		return minArrivalTime;
	}
	
    /**
     * @return The actual times, the individuals were visited
	 *
     */
	public Date[] getArrivalTimes() {
		Date[] arrivalTimes = new Date[length()];
		for (int i = 0; i < length(); i++)
			arrivalTimes[i] = evaluatedIndividuals.get(i).getArrivalTime();
		return arrivalTimes;
	}

    /**
     * @return travel times in seconds
	 *
     */
	public Long[] getTimes(){
		Long[] times = new Long[length()];
		for (int i = 0; i < length(); i++)
			times[i] = evaluatedIndividuals.get(i).getTime();
		return times;
	}	

    /**
     * @return most likely the travel times, respectively accumulated values
	 *
     */
	public double[] getResults(){
		return resultSet.results;
	}
	
	public Integer[] getBoardings(){
		Integer[] boardings = new Integer[length()];
		for (int i = 0; i < length(); i++)
			boardings[i] = evaluatedIndividuals.get(i).getBoardings();
		return boardings;
	}

	public Double[] getWalkDistances(){
		Double[] walkDistances = new Double[length()];
		for (int i = 0; i < length(); i++)
			walkDistances[i] = evaluatedIndividuals.get(i).getWalkDistance();
		return walkDistances;
	}
	
	public String[] getStringData(String dataName){
		// ToDo: handle null pointers in list
		String[] data = new String[evaluatedIndividuals.size()];
		for(int i = 0; i < evaluatedIndividuals.size(); i++)
			data[i] = evaluatedIndividuals.get(i).getIndividual().getStringData(dataName);
		return data;
	}
	
	public String[] getTraverseModes(){
		String[] modes = new String[length()];
		for (int i = 0; i < length(); i++)
			modes[i] = evaluatedIndividuals.get(i).getModes();
		return modes;
	}
	
	public Long[] getWaitingTimes(){
		Long[] waitingTimes = new Long[length()];
		for (int i = 0; i < length(); i++)
			waitingTimes[i] = evaluatedIndividuals.get(i).getWaitingTime();
		return waitingTimes;
	}
	
	public Double[] getElevationGained(){
		Double[] elevationGained = new Double[length()];
		for (int i = 0; i < length(); i++)
			elevationGained[i] = evaluatedIndividuals.get(i).getElevationGained();
		return elevationGained;
	}
	
	public Double[] getElevationLost(){
		Double[] elevationLost = new Double[length()];
		for (int i = 0; i < length(); i++)
			elevationLost[i] = evaluatedIndividuals.get(i).getElevationLost();
		return elevationLost;
	}
	
	public int length(){
		return evaluatedIndividuals.size();
	}
}
