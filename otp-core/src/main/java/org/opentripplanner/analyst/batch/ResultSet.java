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

package org.opentripplanner.analyst.batch;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// store output outside individuals so populations can be reused
public class ResultSet {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSet.class);
    enum Set { TRAVELTIME, HOPS };
    private static Set set;

    public Population population;
    //public double[] results;
    @Getter public double[] results = getTraveltime();
    public Map<String, double[]> resultMap;
    
    public static ResultSet forTravelTimes(Population population, ShortestPathTree spt) {
        double[] results = new double[population.size()];
        int i = 0;
        for (Individual indiv : population) {
            Sample s = indiv.sample;
            long t = Long.MAX_VALUE;
            if (s == null)
                t = -2;
            else
                t = s.eval(spt);
            if (t == Long.MAX_VALUE)
                t = -1;
            results[i] = t;
            i++;
        }
        return new ResultSet(population, results);
    }
    
    public static ResultSet newResultSet(Population population, ShortestPathTree spt) {

    	Map<String, double[]> resultMap = new HashMap<String, double[]>();
    	
    	switch (set) {
	        case TRAVELTIME:
	            break;
	        case HOPS:
	            break;
	        default:
	        	break;
        }
    	
    	return new ResultSet(population, resultMap);
    }
   
    public double[] getTraveltime(){
    	return resultMap.get("TRAVELTIME");
    }
    
    public ResultSet(Population population, Map<String, double[]> resultMap) {
        this.population = population;
        this.resultMap = resultMap;
    }
    
    public ResultSet(Population population, double[] results) {
        this.population = population;
        //this.results = results;
        resultMap = new HashMap<String, double[]>();
        resultMap.put("TRAVELTIME", results);
    }
    
    protected ResultSet(Population population) {
        this.population = population;
        //this.results = new double[population.size()];
        resultMap = new HashMap<String, double[]>();
        resultMap.put("TRAVELTIME", new double[population.size()]);
    }

    public void writeAppropriateFormat(String outFileName) {
        population.writeAppropriateFormat(outFileName, this);
    }
    
}
