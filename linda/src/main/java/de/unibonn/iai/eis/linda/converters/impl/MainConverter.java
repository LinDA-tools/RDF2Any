package de.unibonn.iai.eis.linda.converters.impl;

import java.util.List;

import com.hp.hpl.jena.query.ResultSet;

/**
 * @author gsingharoy
 *
 *This is the main converter class
 *All the other converter classes will extend this class
 *
 **/

public class MainConverter {
	
	public List<String> resultVars;
	
	public void generateResultVars(ResultSet rdfResultSets){
		this.resultVars = rdfResultSets.getResultVars();
	}
}
