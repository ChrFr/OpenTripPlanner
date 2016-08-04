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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentripplanner.analyst.batch.BasicPopulation;
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.analyst.request.SampleFactory;
import org.opentripplanner.api.model.Itinerary;
import org.opentripplanner.api.model.Leg;
import org.opentripplanner.routing.graph.Graph;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * A set of results, used for wrapping results for aggregations/accumulations
 * 
 * @author Christoph Franke
 */
public class OtpsResultSet{		

	protected ResultSet resultSet = null;
	public static enum AggregationMode { THRESHOLD_SUM_AGGREGATOR, WEIGHTED_AVERAGE_AGGREGATOR, THRESHOLD_CUMMULATIVE_AGGREGATOR, DECAY_AGGREGATOR }
	public static enum AccumulationMode { DECAY_ACCUMULATOR, THRESHOLD_ACCUMULATOR }
	
	protected OtpsPopulation population;
	protected OtpsResult[] evaluations;
	
	private AggregationMode aggregationMode = AggregationMode.THRESHOLD_SUM_AGGREGATOR;
	private AccumulationMode accumulationMode = AccumulationMode.THRESHOLD_ACCUMULATOR;
	private OtpsIndividual source;

	protected OtpsResultSet(OtpsPopulation population){
		this.population = population;
		evaluations = new OtpsResult[population.size()];
	}	
	
	private static ResultSet createBasicResultSet(OtpsPopulation population, double[] results){
		Population basicPop = new BasicPopulation();
		for(OtpsIndividual individual: population){
			OtpsLatLon pos = individual.getLocation();	
			Individual ind = new Individual("", pos.getLon(), pos.getLat(), 0);
			basicPop.addIndividual(ind);
		}
		basicPop.setup(); // doesn't do anything useful, but needed to init basicPop.skip[] with False		
		return new ResultSet(basicPop, results);
	}	
	
	// before aggregating or accumulating the results have to be set in the native ResultSet
	// Acc./Agg. work with this values
	private void setTimesToResults(){
		double[] results = new double[evaluations.length];
		for (int i = 0; i < evaluations.length; i++){
			OtpsResult eval = evaluations[i];	
			// -1 is treated as invalid route by aggregators and accumulators
			double result = (eval != null && eval.time < Long.MAX_VALUE) ? eval.time : -1; 
			results[i] = result;
		}
		resultSet = createBasicResultSet(population, results);
	}
		
	protected void evaluate(ShortestPathTree spt, SampleFactory sampleFactory, boolean evalItineraries){
		
		Graph sptGraph = spt.getOptions().getRoutingContext().graph;
		int i = -1;
		for (OtpsIndividual individual: population){
		    i++;
			evaluations[i] = null;
			
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
			evaluations[i] = evaluation;
		    evaluation.time = time;
		    OtpsSample sample = new OtpsSample(individual.cachedSample);
		    
		    evaluation.boardings = sample.evalBoardings(spt);
		    evaluation.walkDistance = sample.evalWalkDistance(spt);      
		    evaluation.timeToItinerary = (int) (sample.evalDistanceToItinerary(spt) / spt.getOptions().walkSpeed);   
		    
		    if(evalItineraries){
		        Itinerary itinerary = sample.evalItinerary(spt);
		        
		        evaluation.startTime = itinerary.startTime.getTime();	 
		        evaluation.arrivalTime = itinerary.endTime.getTime();
		        evaluation.waitingTime = itinerary.waitingTime;
		        evaluation.elevationGained = itinerary.elevationGained;
		        evaluation.elevationLost = itinerary.elevationLost;
		        
		        Set<String> uniqueModes = new HashSet<>();
		        
		        for (Leg leg: itinerary.legs)
		        	uniqueModes.add(leg.mode);
		        evaluation.modes = uniqueModes.toString();
		    }
		}
    }

	public OtpsIndividual getSource() {
		return source;
	}

	public void setSource(OtpsIndividual source) {
		this.source = source;
	}

	private void setInput(String inputField){
		Double[] inputs = new Double[population.size()];
		for(OtpsIndividual individual: population){
			Double input = individual.getFloatData(inputField);	
			if (input == null)
				throw new IllegalArgumentException("Field " + inputField + " not found");
		}	
		int i = 0;	
		for(Individual individual: resultSet.population){
			individual.input = inputs[i];
			i++;
		}
	}
	
	public double aggregate(String inputField, Double[] params){
		setTimesToResults();
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
		if (setToMerge.population != population)
			throw new IllegalArgumentException("The populations of the sets to merge are not the same!");
		
		Long[] times = getTimes();
		Long[] timesToMerge = setToMerge.getTimes();
		OtpsResult bestResult;
		OtpsResult[] bestResults = new OtpsResult[population.size()];
		for(int i = 0; i < size(); i++){			
			// take passed individual only if reachable and time is smaller
			if (timesToMerge[i] != null && (times[i] == null || timesToMerge[i] < times[i]))
				bestResult = setToMerge.evaluations[i];
			else
				bestResult = this.evaluations[i];
			bestResults[i] = bestResult;			
		}
		OtpsResultSet mergedResultSet = new OtpsResultSet(population);
		mergedResultSet.evaluations = bestResults;
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
		setTimesToResults();
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
	
	public OtpsResult[] getResults(){
		return evaluations;
	}
	
    /**
     * @return The actual times, the trips to the indivduals started
	 *
     */
	public Date[] getStartTimes() {
		Date[] startTimes = new Date[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			startTimes[i] = (eval != null) ? eval.getStartTime(): null;
		}
		return startTimes;
	}
	
    /**
     * @return The actual times, the individuals were visited
	 *
     */
	public Date[] getArrivalTimes() {
		Date[] arrivalTimes = new Date[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			arrivalTimes[i] = (eval != null) ? eval.getArrivalTime(): null;
		}
		return arrivalTimes;
	}

	/**
     * @return the first start time out of all results
	 *
     */
	public Date getMinStartTime(){
		Date minStartTime = new Date(Long.MAX_VALUE);
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			if (eval == null)
				continue;
			Date time = eval.getStartTime();
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
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			if (eval == null)
				continue;
			Date time = eval.getArrivalTime();
			if (time != null && time.compareTo(minArrivalTime) < 0)
				minArrivalTime = time;
		}
		return minArrivalTime;
	}

    /**
     * @return travel times in seconds
	 *
     */
	public Long[] getTimes(){
		Long[] times = new Long[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			times[i] = (eval != null) ? eval.getTime(): null;
		}
		return times;
	}	

//    /**
//     * @return most likely the travel times, respectively accumulated values
//	 *
//     */
//	public double[] getResults(){
//		return resultSet.results;
//	}
	
	public Integer[] getBoardings(){
		Integer[] boardings = new Integer[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			boardings[i] = (eval != null) ? eval.getBoardings(): null;
		}
		return boardings;
	}

	public Double[] getWalkDistances(){
		Double[] walkDistances = new Double[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			walkDistances[i] = (eval != null) ? eval.getWalkDistance(): null;
		}
		return walkDistances;
	}
	
	public String[] getStringData(String dataName){
		// ToDo: handle null pointers in list
		String[] data = new String[size()];
		int i = 0;
		for(OtpsIndividual individual: population){
			data[i] = individual.getStringData(dataName);
			i++;
		}
		return data;
	}
	
	public String[] getTraverseModes(){
		String[] modes = new String[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			modes[i] = (eval != null) ? eval.getModes(): null;
		}
		return modes;
	}
	
	public Long[] getWaitingTimes(){
		Long[] waitingTimes = new Long[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			waitingTimes[i] = (eval != null) ? eval.getWaitingTime(): null;
		}
		return waitingTimes;
	}
	
	public Double[] getElevationGained(){
		Double[] elevationGained = new Double[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			elevationGained[i] = (eval != null) ? eval.getElevationGained(): null;
		}
		return elevationGained;
	}
	
	public Double[] getElevationLost(){
		Double[] elevationLost = new Double[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			elevationLost[i] = (eval != null) ? eval.getElevationLost(): null;
		}
		return elevationLost;
	}	

	public Integer[] getTimesToItineraries(){
		Integer[] timesToItinerary = new Integer[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			timesToItinerary[i] = (eval != null) ? eval.getTimeToItinerary(): null;
		}
		return timesToItinerary;
	}
	
	// start time including time needed to reach itinerary from sampled vertex
	public Date[] getSampledStartTimes(){
		Date[] sampledStartTimes = new Date[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			sampledStartTimes[i] = (eval != null) ? eval.getSampledStartTime(): null;
		}
		return sampledStartTimes;
	}
	
	public int size(){
		return population.size();
	}
	
	// updates the results (only if the individuals were not ignored)
	public void update(){
		 
	}
}
