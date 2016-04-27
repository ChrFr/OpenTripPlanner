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

import java.util.List;

import org.opentripplanner.analyst.batch.BasicPopulation;
import org.opentripplanner.analyst.batch.ResultSet;

/**
 * A set of results, basically used for aggregations/accumulations
 * 
 * @author Christoph Franke
 */
public class OtpsResultSet extends ResultSet {

	// WARNING: results are empty
	public OtpsResultSet(OtpsPopulation population, String inputField, String idField) {
		super(new BasicPopulation());
		for(OtpsIndividual individual: population){
			OtpsLatLon pos = individual.getLocation();
			Individual bla = new Individual(individual.getStringData(idField), pos.getLon(), pos.getLat(), individual.getFloatData(inputField));
			this.population.addIndividual(bla);
		}
		this.results = new double[this.population.size()];
	}
	
	public OtpsResultSet(List<OtpsEvaluatedIndividual> evaluatedIndividuals, String inputField, String idField) {
		super(new BasicPopulation(), new double[evaluatedIndividuals.size()]);
		for(int i = 0; i < evaluatedIndividuals.size(); i++){
			OtpsEvaluatedIndividual eval = evaluatedIndividuals.get(i);
			OtpsIndividual individual = eval.getIndividual();
			OtpsLatLon pos = individual.getLocation();
			Individual bla = new Individual(individual.getStringData(idField), pos.getLon(), pos.getLat(), individual.getFloatData(inputField));
			this.population.addIndividual(bla);
			this.results[i] = eval.getTime();
		}
	}
}
