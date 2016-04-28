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

import java.util.List;

import org.opentripplanner.analyst.batch.BasicPopulation;
import org.opentripplanner.analyst.batch.ResultSet;

/**
 * A set of results, used for wrapping results for aggregations/accumulations
 * 
 * @author Christoph Franke
 */
public class OtpsResultSet{
	
	protected ResultSet resultSet;
	
	private static void addOtpsIndividual(Population population, OtpsIndividual individual, String labelField, String inputField){
		OtpsLatLon pos = individual.getLocation();	
		
		Double input = (inputField != null)? individual.getFloatData(inputField): new Double(0);	
		if (input == null)
			throw new IllegalArgumentException("Field " + inputField + " not found");
		
		String label = individual.getStringData(labelField);			
		Individual bla = new Individual((label != null)? label: "not found", pos.getLon(), pos.getLat(), input);
		population.addIndividual(bla);
	}

	// WARNING: results are empty
	public OtpsResultSet(OtpsPopulation population, String labelField, String inputField) {
		Population basicPop = new BasicPopulation();
		for(OtpsIndividual individual: population){
			addOtpsIndividual(basicPop, individual, labelField, inputField);
		}
		double[] results = new double[basicPop.size()];		
		basicPop.setup(); // doesn't do anything useful, but needed to init basicPop.skip[] with False
		this.resultSet = new ResultSet(basicPop, results);
	}
	
	public OtpsResultSet(List<OtpsEvaluatedIndividual> evaluatedIndividuals, String labelField, String inputField) {
		Population basicPop = new BasicPopulation();
		double[] results = new double[evaluatedIndividuals.size()];
		for(int i = 0; i < evaluatedIndividuals.size(); i++){
			OtpsEvaluatedIndividual evaluatedIndividual = evaluatedIndividuals.get(i);
			OtpsIndividual individual = evaluatedIndividual.getIndividual();
			addOtpsIndividual(basicPop, individual, labelField, inputField);
			results[i] = evaluatedIndividual.getTime();
		}
		basicPop.setup(); // doesn't do anything useful, but needed to init basicPop.skip[] with False
		this.resultSet = new ResultSet(basicPop, results);
	}
	
	public OtpsResultSet(List<OtpsEvaluatedIndividual> evaluatedIndividuals, String labelField) {
		this(evaluatedIndividuals, labelField, null);
	}		
}
