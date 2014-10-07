package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

public class ConfiguredConverter extends MainConverter implements Converter {

	public String header;
	public String body;
	public String footer;
	private Map<String, String> variableDictionary;

	public ConfiguredConverter(String variableDictionary, String header,
			String body, String footer) {
		this.header = header;
		this.body = body;
		this.footer = footer;
		this.variableDictionary = new HashMap<String, String>();
		if (variableDictionary != null
				&& !variableDictionary.equalsIgnoreCase("")) {
			for (String item : variableDictionary.split(",")) {
				this.variableDictionary.put(item.split(":")[0], item.split(":")[1]);
			}
		}
	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults)
			throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
			RDFClass forClass) throws IOException {
		// printing the header to the file
		output.write((this.header + "\n").getBytes(Charset.forName("UTF-8")));
		// printing the footer to the file
		output.write((this.footer).getBytes(Charset.forName("UTF-8")));

	}

}
