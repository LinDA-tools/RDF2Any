package de.unibonn.iai.eis.linda.converters.impl;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.converters.impl.results.JSONOutput;

/**
 * @author gsingharoy
 *
 *
 **/

public class JSONConverter extends MainConverter {

	public ResultSet rdfResults;
	public JSONOutput jsonOutput;

	public JSONConverter(ResultSet rdfResultSet){
		this.rdfResults = rdfResultSet;
		super.generateResultVars(rdfResultSet);
		this.jsonOutput = new JSONOutput(rdfResultSet);
		convert();
	}
	public void addResultSetRowToOutput(QuerySolution row){
		Map<String, Map<String,String>> bindingEntry = new HashMap<String, Map<String,String>>();
		for(int i=0;i<resultVars.size();i++){
			Map<String,String> columnEntry = new HashMap<String,String>();
			columnEntry.put("value", row.get(resultVars.get(i)).toString());
			bindingEntry.put(resultVars.get(i), columnEntry);
		}
		this.jsonOutput.results.bindings.add(bindingEntry);
	}
	public void convert(){
		while(rdfResults.hasNext()){
			QuerySolution row= rdfResults.next();
			addResultSetRowToOutput(row);
		}
	}
}
