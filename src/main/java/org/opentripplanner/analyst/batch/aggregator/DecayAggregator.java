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

package org.opentripplanner.analyst.batch.aggregator;

import org.opentripplanner.analyst.batch.Individual;
import org.opentripplanner.analyst.batch.ResultSet;

/**
 * 
 * 
 * @author Christoph Franke
 */
public class DecayAggregator implements Aggregator {

    private int thresholdSeconds = 60 * 60;
    private double lambda; 

    public DecayAggregator(int thresholdSeconds, double lambda) {
    	this.thresholdSeconds = thresholdSeconds;
    	this.lambda = lambda;
    }

    @Override
    public double computeAggregate(ResultSet rs) {
        double aggregate = 0;
        int i = 0;
        for (Individual indiv : rs.population) {
            double t = rs.results[i];
            if (t > 0 && t < thresholdSeconds){
            	double decay = Math.exp(lambda * (t / 60));
                double decayed = indiv.input * decay;
                aggregate += decayed;
            }
            i++;
        }
        return aggregate;
    }

}
