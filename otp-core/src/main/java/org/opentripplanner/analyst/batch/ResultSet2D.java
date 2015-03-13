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

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.graph.Vertex;
import org.opentripplanner.routing.spt.ShortestPathTree;

/**
 * extended ResultSet, use it to store additional results
 * 
 * @author Franke, Christoph <franke@ggr-planung.de>
 *
 */
public class ResultSet2D extends ResultSet{
    
    private Result[] results2D;    
    
    private Individual origin;

    private static String defaultResult = "TRAVELTIME";

	private static String resultTypes = defaultResult;
    
    private static String bestResultType = defaultResult;
    
    /**
     * factory for creating a new resultset holding all results defined by the resultTypes
     * for the given population in the shortest path tree
     */  
    public static ResultSet newResultSet2D(final Individual origin, final Population population, final ShortestPathTree spt) {
    	Result[] results = newResults(population, spt);   
    	ResultSet2D set = new ResultSet2D(population, results);
    	set.setOrigin(origin);
    	return set;
    } 
    
    private static Result[] newResults(final Population population, final ShortestPathTree spt){    
    	
    	String[] types =  resultTypes.split(",");
    	Result[] results = new Result[types.length];   
    	
    	for(int i = 0; i < types.length; i++){
    		results[i] = new Result(types[i].trim(), population.size());
    	}    	
    	
    	int i = 0;
        for (Individual individual : population){
        	Sample s = individual.sample;
        	Vertex best = Result.evaluate(bestResultType, s, spt);
        	if (best != null)
	            for(Result result: results)
	            	result.insertVertex(i, best, spt); 
            i++;
        }
    	return results;
    }    
    
    /**
     * all stored results
     */        
    public Result[] getResults(){
    	return results2D;
    }
   
    /**
     * a type-specific result
     * 
     * @param type The type of the result you are looking for
     */  
    public Result getResult(final String type){
    	Result ret = null;
    	for(Result result: results2D)
    		if(result.getType().equals(type)){
    			ret = result;
    			break;
    		}
    	return ret;
    }
        
    public ResultSet2D() {
    	super(null, null);
        this.results2D = new Result[0];
    }
    
    public ResultSet2D(final Population population, Result[] results) {
    	super(population);
        this.results2D = results;
        this.results = getResult(defaultResult).getValues();
    }
    
    public ResultSet2D(final Population population, double[] results) {
    	super(population, results);
        results2D = new Result[1];
        results2D[0] = new Result(defaultResult, results);
    }
    
    protected ResultSet2D(final Population population) {
    	super(population);
        results = new double[population.size()];
        results2D = new Result[1];
        results2D[0] = new Result(defaultResult, results);
    }

    @Override
    public void writeAppropriateFormat(final String outFileName) {
        population.writeAppropriateFormat(outFileName, this);
    }

    /**
     * set the types of results, that will be calculated and written to csv
     * 
     * @param rm The comma-seperated types, leading or trailing spaces don't matter e.g. "TRAVELTIME, ARRIVALTIME"
     */        
    public void setResultTypes(final String rm){
    	resultTypes = rm;
    }

    /**
     * set the type of the result, that will be taken to determine the best close vertex out of two
     * 
     * @param type 
     */    
	public void setBestResultType(final String type) {
		bestResultType = type;
	}

	public Individual getOrigin() {
		return origin;
	}

	public void setOrigin(final Individual origin) {
		this.origin = origin;
	}
    
    public void setDefaultResult(final String type) {
		defaultResult = type;
	}

}
