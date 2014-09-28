/**
 * 
 */
package de.unibonn.iai.eis.linda.converters.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;

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
			String forClass) throws IOException {
		// TODO Auto-generated method stub
		
	}

}
