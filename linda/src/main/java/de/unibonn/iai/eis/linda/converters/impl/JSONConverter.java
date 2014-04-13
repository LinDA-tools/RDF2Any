package de.unibonn.iai.eis.linda.converters.impl;

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
	}
}
