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

import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.analyst.batch.aggregator.Aggregator;
import org.opentripplanner.analyst.batch.aggregator.DecayAggregator;
import org.opentripplanner.analyst.batch.aggregator.ThresholdCumulativeAggregator;
import org.opentripplanner.analyst.batch.aggregator.ThresholdSumAggregator;
import org.opentripplanner.analyst.batch.aggregator.WeightedAverageAggregator;

/**
 * class for aggregating resultSets
 * 
 * @author Christoph Franke
 */
public class OtpsAggregate {
	
	private Aggregator aggregator;
	public static enum AggregationMode { THRESHOLD_SUM_AGGREGATOR, WEIGHTED_AVERAGE_AGGREGATOR, THRESHOLD_CUMMULATIVE_AGGREGATOR, DECAY_AGGREGATOR }
	
	public OtpsAggregate(int mode, Double[] params){
		this(AggregationMode.values()[mode], params);
	}

	public OtpsAggregate(String mode, Double[] params){
		this(AggregationMode.valueOf(mode), params);
	}
	
	public OtpsAggregate(AggregationMode mode, Double[] params){
		switch(mode){
			case THRESHOLD_SUM_AGGREGATOR:
				aggregator = new ThresholdSumAggregator();
				if(params != null && params.length > 0)
					((ThresholdSumAggregator)aggregator).threshold = params[0].intValue();
				break;

			case WEIGHTED_AVERAGE_AGGREGATOR:
				aggregator = new WeightedAverageAggregator();
				break;
				
			case THRESHOLD_CUMMULATIVE_AGGREGATOR:
				if(params == null || params.length == 0)
					throw new IllegalArgumentException("mode THRESHOLD_CUMMULATIVE_AGGREGATOR needs a threshold as a parameter");
				aggregator = new ThresholdCumulativeAggregator(params[0].intValue());
				break;
				
			case DECAY_AGGREGATOR:
				if(params == null || params.length < 2)
					throw new IllegalArgumentException("mode DECAY_AGGREGATOR needs a threshold and a lambda as parameters");
				aggregator = new DecayAggregator(params[0].intValue(), params[1]);
				break;
		}
	}
	
	protected OtpsAggregate(Aggregator aggregator){
		this.aggregator = aggregator;
	}	
	
	public double aggregate(OtpsResultSet resultSet, String inputField){
		if(aggregator == null)
			throw new IllegalStateException();
		if(resultSet.population.size() == 0)
			return 0;
		ResultSet res = resultSet.createResultSet(inputField);
		return aggregator.computeAggregate(res);
	}	
}
