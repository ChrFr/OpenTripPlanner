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

import java.util.Date;
import java.util.List;

import org.opentripplanner.analyst.batch.BasicPopulation;
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.analyst.batch.aggregator.Aggregator;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * A set of results, used for wrapping results for aggregations/accumulations
 * 
 * @author Christoph Franke
 */
public class OtpsResultSet{	

	public static enum AggregationMode { THRESHOLD_SUM_AGGREGATOR, WEIGHTED_AVERAGE_AGGREGATOR, THRESHOLD_CUMMULATIVE_AGGREGATOR, DECAY_AGGREGATOR }
	public static enum AccumulationMode { DECAY_ACCUMULATOR, THRESHOLD_ACCUMULATOR }
	
	protected ResultSet resultSet;
	private List<OtpsEvaluatedIndividual> evaluatedIndividuals;
	private Date[] startTimes;
	private Date[] arrivalTimes;
	private Integer[] boardings;
	private Double[] walkDistances;
	private AggregationMode aggregationMode = AggregationMode.THRESHOLD_SUM_AGGREGATOR;
	private AccumulationMode accumulationMode = AccumulationMode.THRESHOLD_ACCUMULATOR;
	
	protected OtpsResultSet(List<OtpsEvaluatedIndividual> evaluatedIndividuals, String inputField) {
		this.evaluatedIndividuals = evaluatedIndividuals;
		Population basicPop = new BasicPopulation();
		double[] results = new double[evaluatedIndividuals.size()];
		startTimes = new Date[evaluatedIndividuals.size()];
		arrivalTimes = new Date[evaluatedIndividuals.size()];
		boardings = new Integer[evaluatedIndividuals.size()];
		walkDistances = new Double[evaluatedIndividuals.size()];
		for(int i = 0; i < evaluatedIndividuals.size(); i++){			
			OtpsEvaluatedIndividual evaluatedIndividual = evaluatedIndividuals.get(i);		
			results[i] = Double.MAX_VALUE;	
			startTimes[i] = null;	
			arrivalTimes[i] = null;
			Individual ind = null;
			if (evaluatedIndividual != null){				
				OtpsIndividual individual = evaluatedIndividual.getIndividual();
				OtpsLatLon pos = individual.getLocation();	
				
				Double input = (inputField != null && inputField.length() > 0)? individual.getFloatData(inputField): new Double(0);	
				if (input == null)
					throw new IllegalArgumentException("Field " + inputField + " not found");
							
				ind = new Individual("", pos.getLon(), pos.getLat(), input);
				Long time = evaluatedIndividual.getTime();
				if (time != null)
					results[i] = evaluatedIndividual.getTime() ;
				boardings[i] = evaluatedIndividual.getBoardings();
				walkDistances[i] = evaluatedIndividual.getWalkDistance();
			}
			basicPop.addIndividual(ind);
		}
		basicPop.setup(); // doesn't do anything useful, but needed to init basicPop.skip[] with False
		this.resultSet = new ResultSet(basicPop, results);
	}
	
	public void setInput(String inputField){
		int i = 0;
		for(Individual individual: resultSet.population){

			Double input = evaluatedIndividuals.get(i).getIndividual().getFloatData(inputField);	
			if (input == null)
				throw new IllegalArgumentException("Field " + inputField + " not found");
			individual.input = input;
		}		
	}
	
	public double aggregate(Double[] params){
        OtpsAggregate aggregator = new OtpsAggregate(aggregationMode, params);           
        return aggregator.computeAggregate(this);  
	}
	
	public double aggregate(){     
        return aggregate(null);  
	}
	
	public void setAggregationMode(AggregationMode mode){
		aggregationMode = mode;
	}
	
	public void setAggregationMode(String mode){
		setAggregationMode(AggregationMode.valueOf(mode));
	}

	public void setAggregationMode(int mode){
		setAggregationMode(AggregationMode.values()[mode]);
	}
	
	public void accumulate(OtpsResultSet accumulated, double amount, Double[] params){
		OtpsAccumulate accumulator = new OtpsAccumulate(accumulationMode, params);           
        accumulator.computeAccumulate(this, accumulated, amount);
	}
	
	public double accumulate(OtpsResultSet accumulated, double amount){     
        return accumulate(accumulated, amount);  
	}
	
	public void setAccumulationMode(AccumulationMode mode){
		accumulationMode = mode;
	}
	
	public void setAccumulationnMode(String mode){
		setAccumulationMode(AccumulationMode.valueOf(mode));
	}

	public void setAccumulationnMode(int mode){
		setAccumulationMode(AccumulationMode.values()[mode]);
	}
	
    /**
     * @return The actual times, the trips to the indivduals started
	 *
     */
	public Date[] getStartTimes() {
		return startTimes;
	}

    /**
     * @return The actual times, the individuals were visited
	 *
     */
	public Date[] getArrivalTimes() {
		return arrivalTimes;
	}

	public double[] getTimes(){
		return resultSet.results;
	}
	
	public Integer[] getBoardings(){
		return boardings;
	}

	public Double[] getWalkDistances(){
		return walkDistances;
	}
	
	public double[] getInputs(){
		return null;//resultSet.population;
	}
	
	public String[] getStringData(String dataName){
		// ToDo: handle null pointers in list
		String[] data = new String[evaluatedIndividuals.size()];
		for(int i = 0; i < evaluatedIndividuals.size(); i++)
			data[i] = evaluatedIndividuals.get(i).getIndividual().getStringData(dataName);
		return data;
	}
}
