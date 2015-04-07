package de.unibonn.iai.eis.linda.helper;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.hp.hpl.jena.query.ResultSet;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.converters.impl.JSONConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDBConverter;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

/**
 * @author gsingharoy
 * 
 *         This class handles output Stream of a SPARQL query
 * 
 **/
public class OutputStreamHandler {
	public static StreamingOutput getConverterStreamingOutput(
			final Converter converter, final String dataset,
			final String queryString) {

		String converterType = "";
		if (converter instanceof CSVConverter) converterType = "CSV";
		else if (converter instanceof RDBConverter) converterType = "RDB";
		else if (converter instanceof JSONConverter) converterType = "JSON";
		else converterType = "userdefined";
		
		InstanceExporter.exporter(queryString, dataset, converterType);
		
		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				try {

					ResultSet results = SPARQLHandler.executeQuery(dataset,
							queryString);
					converter.convert(output, results);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}

			}

		};
	}

	public static StreamingOutput getConverterStreamingOutput(
			final Converter converter, final String dataset,
			final String queryString, final String forClass,
			final String properties) {
		
		String converterType = "";
		if (converter instanceof CSVConverter) converterType = "CSV";
		else if (converter instanceof RDBConverter) converterType = "RDB";
		else if (converter instanceof JSONConverter) converterType = "JSON";
		else converterType = "userdefined";
		
		InstanceExporter.exporter(queryString, dataset, converterType);

		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				try {

					ResultSet results = SPARQLHandler.executeQuery(dataset,
							queryString);
					RDFClass rdfClass = RDFClass.searchRDFClass(dataset,
							forClass);

					if (rdfClass != null) {

						if (properties != null) {
							rdfClass.filterProperties(properties);
						}

						converter.convert(output, results, rdfClass);
					} else
						converter.convert(output, results);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}

			}

		};
	}
}
