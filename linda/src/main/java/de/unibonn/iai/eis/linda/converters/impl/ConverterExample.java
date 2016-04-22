/**
 * 
 */
package de.unibonn.iai.eis.linda.converters.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

/**
 * @author jattard
 *
 */
public class ConverterExample implements Converter {

	/* (non-Javadoc)
	 * @see de.unibonn.iai.eis.linda.converters.Converter#convert(com.hp.hpl.jena.query.Dataset)
	 */



	public void convert(OutputStream output, ResultSet rdfResults) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
			RDFClass forClass) throws IOException {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
								Map<String, List<Object>> propTable) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
