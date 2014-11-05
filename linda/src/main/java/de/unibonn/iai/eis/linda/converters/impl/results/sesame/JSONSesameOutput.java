package de.unibonn.iai.eis.linda.converters.impl.results.sesame;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.ResultSet;

public class JSONSesameOutput {
	public Map<String, String> sparql;
	


	public JSONSesameOutput(ResultSet rdfResultSet) {
		// TODO Auto-generated constructor stub
		this.sparql = new HashMap<String,String>();
		this.sparql.put("@xmlns", "http://www.w3.org/2005/sparql-results#");
	}
}
