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

import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// store output outside individuals so populations can be reused
public class ResultSet {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSet.class);    
    
    public Population population;
    public double[] results;
    
    private Map<String, Result> resultMap;    
    private Individual origin;

    public final static String DEFAULTRESULT = "TRAVELTIME";

    public static ResultSet newResultSet(Individual origin, Population population, ShortestPathTree spt) {
    	Map<String, Result> resultMap = Result.newResults(population, spt);   
    	ResultSet set = new ResultSet(population, resultMap);
    	set.setOrigin(origin);
    	return set;
    } 
    
    public Map<String, Result> getResults(){
    	return resultMap;
    }
   
    public Result getResult(String mode){
    	if(resultMap == null)
    		return null;
    	return resultMap.get(mode);
    }
        
    public ResultSet() {
        this.population = null;
        this.results = null;
        this.resultMap = new HashMap<String, Result>();
    }
    
    public ResultSet(Population population, Map<String, Result> resultMap) {
        this.population = population;
        this.resultMap = resultMap;
        this.results = getResult(DEFAULTRESULT).getValues();
    }
    
    public ResultSet(Population population, double[] results) {
        this.population = population;
        this.results = results;
        resultMap.put(DEFAULTRESULT, new Result(DEFAULTRESULT, results));
    }
    
    protected ResultSet(Population population) {
        this.population = population;
        this.results = new double[population.size()];
        resultMap = new HashMap<String, Result>();
        resultMap.put(DEFAULTRESULT, new Result(population.size()));
    }

    public void writeAppropriateFormat(String outFileName) {
        population.writeAppropriateFormat(outFileName, this);
    }

	public Individual getOrigin() {
		return origin;
	}

	public void setOrigin(Individual origin) {
		this.origin = origin;
	}
    
}
