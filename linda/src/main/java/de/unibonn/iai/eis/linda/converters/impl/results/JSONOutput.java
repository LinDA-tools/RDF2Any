package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.ResultSet;


/**
 * @author gsingharoy
 *
 *
 **/

public class JSONOutput {
	public Map<String, Collection<String>> head; 
	public JSONResult results; 
	
	public JSONOutput(ResultSet rdfResults){
		this.head =  new HashMap<String, Collection<String>>();
		this.head.put("link", new ArrayList<String>());
		this.head.put("vars", rdfResults.getResultVars());
		this.results = new JSONResult();

	}
	public void setTimeTaken(Double timeTaken){
		this.results.setTimeTaken(timeTaken);
	}
}
