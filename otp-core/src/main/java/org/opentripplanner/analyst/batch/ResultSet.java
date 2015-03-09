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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;

import org.opentripplanner.analyst.core.Sample;
import org.opentripplanner.routing.spt.ShortestPathTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// store output outside individuals so populations can be reused
public class ResultSet {

    private static final Logger LOG = LoggerFactory.getLogger(ResultSet.class);    
    
    public static final String[] AVAILABLERESULTS = new String[]{
    	"TRAVELTIME" , "TRANSFERS", "BLA", "TEST"
    };
    
    public final static String DEFAULTRESULT = "TRAVELTIME";
    
    public static String resultModes = DEFAULTRESULT;

    public Population population;
    //public double[] results;
    public Map<String, double[]> resultMap;    

    public double[] results;
    
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
    	Map<String, double[]> resultMap = computeResults(population, spt); 
    	
    	return new ResultSet(population, resultMap);
    }   
    
    private static Map<String, double[]> computeResults(Population population, ShortestPathTree spt){    	
    	Map<String, double[]> resultMap = new HashMap<String, double[]>();
    	
    	List<String> modelist = Arrays.asList(AVAILABLERESULTS);     	
    	for (String mode : resultModes.split(",")) {
    		mode = mode.trim();
            if (mode.length() == 0 || !modelist.contains(mode)) {
                continue;
            }
            resultMap.put(mode, new double[population.size()]);
        }
    	
    	int i = 0;
        for (Individual individual : population){
            for(String key: resultMap.keySet()){
            	double result = 0;
            	if(key.equals("TRAVELTIME"))
            		result = travelTime(individual, spt);
            	else if(key.equals("TRANSFERS"))
            		result = transferCount(individual, spt);
            	resultMap.get(key)[i] = result;
            }
            i++;
        }
    	return resultMap;
    }
    
    private static double travelTime(Individual individual, ShortestPathTree spt){
    	Sample s = individual.sample;
        long t = Long.MAX_VALUE;
        if (s == null)
            t = -2;
        else
            t = s.eval(spt);
        if (t == Long.MAX_VALUE)
            t = -1;
        return t;
    }
    
    private static double transferCount(Individual individual, ShortestPathTree spt){
        return 0;
    }
    
    public void setResultModes(String rm){
    	resultModes = rm;
    }
   
    public double[] getResults(){
    	if(resultMap == null)
    		return null;
    	return resultMap.get(DEFAULTRESULT);
    }
    
    public ResultSet() {
        this.population = null;
        this.results = null;
        this.resultMap = new HashMap<String, double[]>();;
    }
    
    public ResultSet(Population population, Map<String, double[]> resultMap) {
        this.population = population;
        this.resultMap = resultMap;
        this.results = getResults();
    }
    
    public ResultSet(Population population, double[] results) {
        this.population = population;
        this.results = results;
        resultMap = new HashMap<String, double[]>();
        resultMap.put(DEFAULTRESULT, results);
    }
    
    protected ResultSet(Population population) {
        this.population = population;
        this.results = new double[population.size()];
        resultMap = new HashMap<String, double[]>();
        resultMap.put(DEFAULTRESULT, new double[population.size()]);
    }

    public void writeAppropriateFormat(String outFileName) {
        population.writeAppropriateFormat(outFileName, this);
    }
    
}
