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

import java.util.List;

import org.opentripplanner.analyst.batch.aggregator.Aggregator;
import org.opentripplanner.analyst.batch.aggregator.ThresholdCumulativeAggregator;
import org.opentripplanner.analyst.batch.aggregator.ThresholdSumAggregator;
import org.opentripplanner.analyst.batch.aggregator.WeightedAverageAggregator;

/**
 * class for aggregating resultSets
 * 
 * @author Christoph Franke
 */
public class OtpsAggregate {
	public static enum Mode { THRESHOLD_SUM_AGGREGATOR, WEIGHTED_AVERAGE_AGGREGATOR, THRESHOLD_CUMMULATIVE_AGGREGATOR }
	
	private Aggregator aggregator;
	
	public OtpsAggregate(Mode mode, Integer value){
		switch(mode){
			case THRESHOLD_SUM_AGGREGATOR:
				aggregator = new ThresholdSumAggregator();
				break;

			case WEIGHTED_AVERAGE_AGGREGATOR:
				aggregator = new WeightedAverageAggregator();
				break;
				
			case THRESHOLD_CUMMULATIVE_AGGREGATOR:
				if(value == null)
					throw new IllegalArgumentException("mode " + Mode.THRESHOLD_CUMMULATIVE_AGGREGATOR + " needs a value to be set");
				aggregator = new ThresholdCumulativeAggregator(value);
				break;
		}
	}
	
	public OtpsAggregate(Mode mode){
		this(mode, null);
	}
	
	public OtpsAggregate(Aggregator aggregator){
		this.aggregator = aggregator;
	}
	
	public double computeAggregate(OtpsResultSet resultSet){
		if(aggregator == null)
			throw new IllegalStateException();
		return aggregator.computeAggregate(resultSet.resultSet);
	}
	
	public double computeAggregate(List<OtpsEvaluatedIndividual> evaluatedIndividuals){
		OtpsResultSet resultSet = new OtpsResultSet(evaluatedIndividuals, "");
		return computeAggregate(resultSet);
	}
}
