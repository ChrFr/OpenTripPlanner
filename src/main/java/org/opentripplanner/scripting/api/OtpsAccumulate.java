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

import org.opentripplanner.analyst.batch.Accumulator;
import org.opentripplanner.analyst.batch.DecayAccumulator;
import org.opentripplanner.analyst.batch.ThresholdAccumulator;

/**
 * class for accumulating resultSets
 * 
 * @author Christoph Franke
 */
public class OtpsAccumulate {
	public static enum Mode { DECAY_ACCUMULATOR, THRESHOLD_ACCUMULATOR }
	
	private Accumulator accumulator;
	
	public OtpsAccumulate(Mode mode, Integer value){
		switch(mode){
			case DECAY_ACCUMULATOR:
				if(value == null)
					throw new IllegalArgumentException("mode " + Mode.THRESHOLD_ACCUMULATOR + " needs a value to be set");
				accumulator = new DecayAccumulator();
				((DecayAccumulator)accumulator).setHalfLifeMinutes(value);
				break;
				
			case THRESHOLD_ACCUMULATOR:
				accumulator = new ThresholdAccumulator();
				break;
		}
	}
	
	public OtpsAccumulate(String mode, Integer value){
		this(Mode.valueOf(mode), value);
	}
	
	public OtpsAccumulate(Mode mode){
		this(mode, null);
	}
	
	public OtpsAccumulate(String mode){
		this(Mode.valueOf(mode));
	}
	
	private OtpsAccumulate(Accumulator accumulator){
		this.accumulator = accumulator;
	}
	
	public void computeAccumulate(OtpsResultSet current, OtpsResultSet accumulated, int value){
		if(accumulator == null)
			throw new IllegalStateException();
		if(current.resultSet.population.size() != 0)
			accumulator.accumulate(value, current.resultSet, accumulated.resultSet);
	}
}
