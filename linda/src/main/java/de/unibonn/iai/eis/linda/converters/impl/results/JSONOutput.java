package de.unibonn.iai.eis.linda.converters.impl.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.ResultSet;




public class JSONOutput {
	Map<String, Collection<String>> head; 

	
	public JSONOutput(ResultSet rdfResults){
		this.head =  new HashMap<String, Collection<String>>();
		this.head.put("link", new ArrayList<String>());
		this.head.put("vars", rdfResults.getResultVars());

	}
}
