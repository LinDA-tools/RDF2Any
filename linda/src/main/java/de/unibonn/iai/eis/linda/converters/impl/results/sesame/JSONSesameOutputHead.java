package de.unibonn.iai.eis.linda.converters.impl.results.sesame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.ResultSet;

public class JSONSesameOutputHead {
	public List<Object> variable;
	
	public JSONSesameOutputHead(ResultSet rdfResultSet){
		this.variable = new ArrayList<Object>();
		for(String headVar:rdfResultSet.getResultVars()){
			Map<String, String> headVarHash = new HashMap<String,String>();
			headVarHash.put("@name", headVar);
			this.variable.add(headVarHash);
		}
	}
}
