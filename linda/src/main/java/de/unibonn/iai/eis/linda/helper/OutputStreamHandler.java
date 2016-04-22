package de.unibonn.iai.eis.linda.helper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.lucene.queryparser.classic.ParseException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.TriplePath;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementOptional;
import com.hp.hpl.jena.sparql.syntax.ElementPathBlock;
import com.hp.hpl.jena.vocabulary.RDF;
import com.servlet.Main;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.converters.impl.CSVConverter;
import de.unibonn.iai.eis.linda.converters.impl.JSONConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDBConverter;
import de.unibonn.iai.eis.linda.converters.impl.RDFConverter;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

/**
 * @author gsingharoy
 * 
 *         This class handles output Stream of a SPARQL query
 * 
 **/
public class OutputStreamHandler {
//	public static StreamingOutput getConverterStreamingOutput(
//			final Converter converter, final String dataset,
//			final String queryString, final Boolean generateOntology) {
//
//		String converterType = "";
//		if (converter instanceof CSVConverter) converterType = "CSV";
//		else if (converter instanceof RDBConverter) converterType = "RDB";
//		else if (converter instanceof JSONConverter) converterType = "JSON";
//		else if (converter instanceof RDFConverter){
//			((RDFConverter) converter).setQuery(queryString);
//			converterType = "RDF";
//		}
//		else converterType = "userdefined";
//		
//		if (generateOntology) InstanceExporter.exporter(queryString, dataset, converterType);
//		
//		return new StreamingOutput() {
//			public void write(OutputStream output) throws IOException,
//					WebApplicationException {
//				try {
//
//					ResultSet results = SPARQLHandler.executeQuery(dataset,
//							queryString, true);
//					converter.convert(output, results);
//				} catch (Exception e) {
//					throw new WebApplicationException(e);
//				}
//
//			}
//
//		};
//	}
	
	private static String _dataset = "";

	public static StreamingOutput getConverterStreamingOutput(
			final Converter converter, final String dataset,
			final String queryString, final String forClass,
			final String properties, final Boolean generateOntology) {
		
		_dataset = dataset;
		String converterType = "";
		if (converter instanceof CSVConverter) converterType = "CSV";
		else if (converter instanceof RDBConverter) converterType = "RDB";
		else if (converter instanceof JSONConverter) converterType = "JSON";
		else if (converter instanceof RDFConverter){
			((RDFConverter) converter).setQuery(queryString);
			converterType = "RDF";
		}
		else converterType = "userdefined";
		
		if (generateOntology) InstanceExporter.exporter(queryString, dataset, converterType);

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



	public static StreamingOutput getConverterStreamingOutput(
			final Converter converter, final String dataset,
			final String queryString, final RDFClass forClass,
			final String properties, final Boolean generateOntology) {
		
		_dataset = dataset;

		String converterType = "";
		if (converter instanceof CSVConverter) converterType = "CSV";
		else if (converter instanceof RDBConverter) converterType = "RDB";
		else if (converter instanceof JSONConverter) converterType = "JSON";
		else if (converter instanceof RDFConverter){
			((RDFConverter) converter).setQuery(queryString);
			converterType = "RDF";
		}
		else converterType = "userdefined";
		
		if (generateOntology) InstanceExporter.exporter(queryString, dataset, converterType);
		

		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				try {

					ResultSet results = SPARQLHandler.executeQuery(dataset,	queryString);
					RDFClass rdfClass = new RDFClass(forClass);
					
					if (properties != null) {
						rdfClass.filterProperties(properties);
					}

					converter.convert(output, results, rdfClass);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};
	}
	
	public static StreamingOutput getConverterStreamingOutput(
			final Converter converter, final String dataset,
			final String queryString, final Boolean generateOntology) {
		
		_dataset = dataset;

		String converterType = "";
		if (converter instanceof CSVConverter) converterType = "CSV";
		else if (converter instanceof RDBConverter) converterType = "RDB";
		else if (converter instanceof JSONConverter) converterType = "JSON";
		else if (converter instanceof RDFConverter){
			((RDFConverter) converter).setQuery(queryString);
			converterType = "RDF";
		}
		else converterType = "userdefined";
		
		if (generateOntology) InstanceExporter.exporter(queryString, dataset, converterType);
		

		return new StreamingOutput() {
			public void write(OutputStream output) throws IOException,
					WebApplicationException {
				try {

					ResultSet results = SPARQLHandler.executeQuery(dataset,	queryString);
					Map<String,List<Object>> propTable = varToPropertyTable(queryString);
					converter.convert(output, results, propTable);
				} catch (Exception e) {
					throw new WebApplicationException(e);
				}
			}
		};
	}
	
	
	private static Map<String,List<Object>> varToPropertyTable(String query) throws ParseException{
		Map<String,List<Object>> mapping = new HashMap<String,List<Object>>();
		Query q = QueryFactory.create(query) ;
		
		List<Element> elements = ((ElementGroup) q.getQueryPattern()).getElements();
		
		for (Element elem : elements){
			mapping = something(elem, mapping);			
		}
		return mapping;
	}
	
	private static Map<String,List<Object>> something(Element element, Map<String,List<Object>> _map) throws ParseException{
		Map<String,List<Object>> mapping = new HashMap<String,List<Object>>(_map);
		

		if(element instanceof ElementPathBlock){
        	ElementPathBlock pathBlock = (ElementPathBlock) element;
            for(TriplePath triple : pathBlock.getPattern().getList()){
            	if (triple.getPredicate().getURI() == RDF.type.getURI()){
            		List<Object> obj = null;
            		if (mapping.containsKey(triple.getSubject().toString().replace("?", "")))
            			obj = mapping.get(triple.getSubject().toString().replace("?", ""));
            		else 
            			obj = new ArrayList<Object>();
            		
            		RDFClass rdfClass = Main.addOrGetCachedClass(triple.getObject().getURI(), null);
            		if ((rdfClass == null) || (rdfClass.properties.size() == 0)){
         				rdfClass = Main.addOrGetCachedClass(triple.getObject().getURI(),RDFClass.searchRDFClass(_dataset, triple.getObject().getURI()));
         			}
            		
            		obj.add(rdfClass);
         			mapping.put(triple.getSubject().toString().replace("?", ""), obj);
            	} else {
            		List<Object> obj = null;
            		if (!(triple.getObject().toString().startsWith("?"))) continue; // if it is not a variable then skip
            		if (mapping.containsKey(triple.getObject().toString().replace("?", "")))
            			obj = mapping.get(triple.getObject().toString().replace("?", ""));
            		else 
            			obj = new ArrayList<Object>();
            		
            		obj.add(triple.getPredicate().getURI());
            		mapping.put(triple.getObject().toString().replace("?", ""), obj);
            	}
            }
        } else if (element instanceof ElementOptional) {
        	Element _oE = ((ElementOptional) element).getOptionalElement();
    		List<Element> elements = ((ElementGroup) _oE).getElements();
    		for (Element elem : elements){
    			mapping.putAll(something(elem, mapping));			
    		}
        }  
		return mapping;
	}
	
	
}
