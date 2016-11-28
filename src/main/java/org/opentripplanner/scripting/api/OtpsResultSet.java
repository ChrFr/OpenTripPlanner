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

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;

import org.opentripplanner.analyst.batch.BasicPopulation;
import org.opentripplanner.analyst.batch.ResultSet;

/**
 * A set of results, used for wrapping results for aggregations/accumulations
 * 
 * @author Christoph Franke
 */
public class OtpsResultSet{		
	
	protected OtpsPopulation population;
	protected OtpsResult[] evaluations;
	
	private OtpsIndividual root;

	protected OtpsResultSet(OtpsIndividual root, OtpsPopulation population){
		this.root = root;
		this.population = population;
		evaluations = new OtpsResult[population.size()];
	}	
	
	protected static ResultSet createEmptyResultSet(OtpsPopulation population){
		Population basicPop = new BasicPopulation();
		double[] results = new double[population.size()];
		for(OtpsIndividual individual: population){			
			OtpsLatLon pos = individual.getLocation();	
			Individual ind = new Individual("", pos.getLon(), pos.getLat(), 0);
			basicPop.addIndividual(ind);	
		}	
		basicPop.setup(); // doesn't do anything useful, but needed to init basicPop.skip[] with False		
		return new ResultSet(basicPop, results);
	}
	
	protected ResultSet createResultSet(){
		return createResultSet(null);
	}	
	
	protected ResultSet createResultSet(String inputField){
		ResultSet resultSet = createEmptyResultSet(population);
		int i = 0;
		Iterator<Individual> indIterator = resultSet.population.iterator();		
		for(OtpsIndividual individual: population){
			OtpsResult eval = evaluations[i];	
			// -1 is treated as invalid route by aggregators and accumulators
			double result = (eval != null && eval.time < Long.MAX_VALUE) ? eval.time : -1; 
			resultSet.results[i] = result;
			
			if (inputField != null){
				Double input = individual.getFloatData(inputField);	
				Individual ind = indIterator.next();
				ind.input = input;		
			}
			i++;
		}	
		return resultSet;
	}	

	/**
	 * 
	 * @return the root of the evaluated route 
	 *         if not arriveby it's the origin, else the destination 
	 */
	public OtpsIndividual getRoot() {
		return root;
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
			throw new IllegalArgumentException("The sets are not based on the same population!");
		
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
		OtpsResultSet mergedResultSet = new OtpsResultSet(root, population);
		mergedResultSet.evaluations = bestResults;
		return mergedResultSet;
	}

	public OtpsResult[] getResults(){
		return evaluations;
	}	

	public OtpsResult getResult(int i){
		return evaluations[i];
	}
	

	/**
     * @return the first start time out of all results
	 *
     */
	public Date getMinStartTime(){
		Date minStartTime = null;
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			if (eval == null)
				continue;
			Date time = eval.getStartTime();
			if (time != null && (minStartTime == null || time.compareTo(minStartTime) < 0))
				minStartTime = time;
		}
		return minStartTime;
	}

	/**
     * @return the first arrival time out of all results
	 *
     */
	public Date getMinArrivalTime(){
		Date minArrivalTime = null;
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			if (eval == null)
				continue;
			Date time = eval.getArrivalTime();
			if (time != null && (minArrivalTime == null || time.compareTo(minArrivalTime) < 0))
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
	
	public int size(){
		return population.size();
	}
    
    public OtpsPopulation getPopulation(){
    	return population;
    }
	
	// updates this results with the results of given set (only valid routes, if given set contains invalid routes for certain individuals, keep existing result for those)
	public void update(OtpsResultSet resultSet){
		if (resultSet.population != population)
			throw new IllegalArgumentException("The sets are not based on the same population!");
		for (int i = 0; i < size(); i++){
			if (resultSet.evaluations[i] != null)
				evaluations[i] = resultSet.evaluations[i];
		}
	}
	
	//the value 0 if the argument Date is equal to this Date; a value less than 0 if this Date is before the Date argument; and a value greater than 0 if this Date is after the Date argument.
	//2 if this Date is Null (no route found)
	public int[] compareStartTime(Date compareTime){
		int[] comparisons = new int[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			if(eval == null)
				comparisons[i] = -2;
			else
				comparisons[i] = eval.getStartTime().compareTo(compareTime);			
		}
		return comparisons;
	}
	
	//the value 0 if the argument Date is equal to this Date; a value less than 0 if this Date is before the Date argument; and a value greater than 0 if this Date is after the Date argument.
	//2 if this Date is Null (no route found)
	public int[] compareArrivalTime(Date compareTime){
		int[] comparisons = new int[size()];
		for (int i = 0; i < size(); i++){
			OtpsResult eval = evaluations[i];
			if(eval == null)
				comparisons[i] = -2;
			else
				comparisons[i] = eval.getArrivalTime().compareTo(compareTime);			
		}
		return comparisons;
	}
	
	/**
	 * get an array of results only containing the results with the n best (=fastest) traveltimes 
	 * @param n
	 */
	public OtpsResult[] getBestResults(int n){
		OtpsResult[] bestResults = new OtpsResult[n];
		OtpsResult[] clone = evaluations.clone();
		Arrays.sort(clone, new Comparator<OtpsResult>(){
			@Override
			public int compare(OtpsResult arg0, OtpsResult arg1) {
				if (arg0 == null && arg1 == null)
					return 0;
				if (arg0 == null) 
					return 1;
				if (arg1 == null) 
					return -1;
				return arg0.compareTo(arg1);
			}			
		});
		
		for(int i = 0; i < n; i++){
			bestResults[i] = clone[i];
		}
		return bestResults;		
	}
}
