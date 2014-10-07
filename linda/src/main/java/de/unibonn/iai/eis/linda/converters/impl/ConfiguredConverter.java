package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

public class ConfiguredConverter extends MainConverter implements Converter {

	@Override
	public void convert(OutputStream output, ResultSet rdfResults)
			throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
			RDFClass forClass) throws IOException {
		output.write(( "Configured download working properly !!! \n\n")
				.getBytes(Charset.forName("UTF-8")));
		
	}

}
