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
import org.opentripplanner.analyst.batch.ResultSet;
import org.opentripplanner.analyst.batch.ThresholdAccumulator;

/**
 * class for accumulating result-sets
 * 
 * Example of code (python script):
 * <pre>
 *   # Create an accumulator
 *   accumulator = OtpsAccumulate(accumulation_mode, params)
 * </pre>
 * 
 * @author Christoph Franke
 */
public class OtpsAccumulate {
	
	private Accumulator accumulator;
	private ResultSet accumulated;
	
	public static enum AccumulationMode { DECAY_ACCUMULATOR, THRESHOLD_ACCUMULATOR }

	public OtpsAccumulate(int mode, Double[] params){
		this(AccumulationMode.values()[mode], params);
	}

	public OtpsAccumulate(String mode, Double[] params){
		this(AccumulationMode.valueOf(mode), params);
	}
	
	public OtpsAccumulate(AccumulationMode mode, Double[] params){
		switch(mode){
			case DECAY_ACCUMULATOR:
				if(params == null || params.length == 0)
					throw new IllegalArgumentException("mode DECAY_ACCUMULATOR needs halflifeminutes as a parameter");
				accumulator = new DecayAccumulator();
				((DecayAccumulator)accumulator).setHalfLifeMinutes(params[0].intValue());
				break;
				
			case THRESHOLD_ACCUMULATOR:
				accumulator = new ThresholdAccumulator();
				if(params != null && params.length > 0)
					((ThresholdAccumulator)accumulator).setThresholdMinutes(params[0].intValue());
				break;
		}
	}
	
	public void accumulate(OtpsResultSet current, double amount){
		if (accumulated == null){			
			accumulated = OtpsResultSet.createEmptyResultSet(current.population);
		}
		ResultSet res = current.createResultSet();
		if(accumulator == null)
			throw new IllegalStateException("Accumulation-mode has not been set.");
		accumulator.accumulate(amount, res, accumulated);
	}
	
	public double[] getResults(){
		return accumulated.results;
	}
}
