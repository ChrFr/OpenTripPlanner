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

import org.opentripplanner.analyst.batch.Accumulator;
import org.opentripplanner.analyst.batch.DecayAccumulator;
import org.opentripplanner.analyst.batch.ThresholdAccumulator;
import org.opentripplanner.analyst.batch.aggregator.ThresholdSumAggregator;

/**
 * class for accumulating resultSets
 * 
 * @author Christoph Franke
 */
public class OtpsAccumulate {
	
	private Accumulator accumulator;
	
	public OtpsAccumulate(OtpsResultSet.AccumulationMode mode, Double[] params){
		switch(mode){
			case DECAY_ACCUMULATOR:
				if(params == null || params.length < 2)
					throw new IllegalArgumentException("mode DECAY_ACCUMULATOR needs halflifeminutes and a lambda as parameters");
				accumulator = new DecayAccumulator();
				((DecayAccumulator)accumulator).setHalfLifeMinutes(params[0].intValue());
				((DecayAccumulator)accumulator).lambda = params[1].intValue();
				break;
				
			case THRESHOLD_ACCUMULATOR:
				accumulator = new ThresholdAccumulator();
				if(params != null && params.length > 0)
					((ThresholdAccumulator)accumulator).thresholdSeconds = params[0].intValue();
				break;
		}
	}
	
	public void computeAccumulate(OtpsResultSet current, OtpsResultSet accumulated, double amount){
		if(accumulator == null)
			throw new IllegalStateException();
		if(current.resultSet.population.size() != 0)
			accumulator.accumulate(amount, current.resultSet, accumulated.resultSet);
	}
}
