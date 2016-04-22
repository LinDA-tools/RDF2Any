package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.helper.CSVHelper;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;

/**
 * @author gsingharoy
 * 
 *         This converts ResultSet to CSV
 **/

public class CSVConverter extends MainConverter implements Converter {
	private String generateFileHeader() {
		String result = "rowID";
		for (int i = 0; i < resultVars.size(); i++) {
			result += ",";
			result += resultVars.get(i);
		}
		result += "\n";
		return result;
	}

	private String generateFileHeader(RDFClass forClass) {
		StringBuilder header = new StringBuilder();
		header.append("rowID");
		header.append(",");
		header.append(forClass.label + "('"+forClass.uri+"')");
		header.append(",");
		for (RDFClassProperty property : forClass.properties) {
			header.append(property.getCSVHeaderAttributeName() + "('"+property.uri+"')");
			header.append(",");
		}
		header.append("\n");
		return header.toString();
	}
	
	private String generateFileHeader(Map<String,List<Object>> propTable) {
		List<RDFClass> rdfClasses = new ArrayList<RDFClass>();
		
		for (String key : propTable.keySet()){
			List<Object> obj = propTable.get(key);
			for (Object val : obj){
				if (val instanceof RDFClass){
					rdfClasses.add((RDFClass) val);
				}
			}
		}
		
		StringBuilder header = new StringBuilder();
		header.append("rowID");
		header.append(",");
		
		for (String var : propTable.keySet()){
			List<Object> objVal = propTable.get(var);
//			for (Object value : objVal){
			Object value = objVal.get(0); // we only need one value
				if (value instanceof RDFClass){
					RDFClass c = (RDFClass) value;
					header.append(c.label + "('" + c.uri + "')");
					header.append(",");
				} else {
					String propURI = (String) value;
					for (RDFClass c : rdfClasses){
						RDFClassProperty _tmp = new RDFClassProperty(propURI, "", "");
						if (c.properties.contains(_tmp)){
							RDFClassProperty p = c.properties.get(c.properties.indexOf(_tmp));
							header.append(p.label + "('" + p.uri + "')");
							header.append(",");
						}
					}
				}
//			}
		}
		header.append("\n");
		return header.toString();
	}

	private String generateFileResultRow(QuerySolution row, Long rowCounter) throws Exception {
			String result = rowCounter +",";
		for (int i = 0; i < resultVars.size(); i++) {
			if (i > 0)
				result += ",";
			RDFNode node = row.get(resultVars.get(i));
			if (node != null) result += CSVHelper.getCSVReadyEntry(node.toString());
		}
		result += "\n";
		return result;		
	}

	public void convert(OutputStream outputStream, ResultSet rdfResults) throws IOException {
		generateResultVars(rdfResults);
		outputStream.write(generateFileHeader().getBytes(
				Charset.forName("UTF-8")));
		Long rowCounter = (long) 1;
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			try {
				outputStream.write(generateFileResultRow(row, rowCounter).getBytes(
						Charset.forName("UTF-8")));
				rowCounter++;
			} catch (Exception e) {
				System.out.println("Error : " + e.toString());
			}

		}

	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults, RDFClass forClass) throws IOException {
		generateResultVars(rdfResults);

		Double startMilliseconds = (double) System.currentTimeMillis();
		output.write(generateFileHeader(forClass).getBytes(Charset.forName("UTF-8")));
		Long rowCounter = (long) 1;
		List<String> vars = rdfResults.getResultVars();
		StringBuilder sb = new StringBuilder();
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			try {
				sb.append(rowCounter);
				sb.append(',');
				for (String v : vars){
					RDFNode val = row.get(v);
					if (val != null){
						sb.append(CSVHelper.getCSVReadyEntry(val.toString()));
						sb.append(',');
					} else {
						sb.append(',');
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append('\n');
				rowCounter++;
			} catch (Exception e) {
				System.out.println("Error : " + e.toString());
			}
		}
		output.write((sb.toString()).getBytes(Charset.forName("UTF-8")));
		Double endMilliseconds = (double) System.currentTimeMillis();
		System.out.println("Time taken : "+((endMilliseconds - startMilliseconds) / 1000)+" seconds");

	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults, Map<String,List<Object>> propTable) throws IOException {

		Double startMilliseconds = (double) System.currentTimeMillis();
		output.write(generateFileHeader(propTable).getBytes(Charset.forName("UTF-8")));
		Long rowCounter = (long) 1;
		StringBuilder sb = new StringBuilder();
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			try {
				sb.append(rowCounter);
				sb.append(',');
				for (String v : propTable.keySet()){
					RDFNode val = row.get(v);
					if (val != null){
						sb.append(CSVHelper.getCSVReadyEntry(val.toString()));
						sb.append(',');
					} else {
						sb.append(',');
					}
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append('\n');
				rowCounter++;
			} catch (Exception e) {
				System.out.println("Error : " + e.toString());
			}
		}
		output.write((sb.toString()).getBytes(Charset.forName("UTF-8")));
		Double endMilliseconds = (double) System.currentTimeMillis();
		System.out.println("Time taken : "+((endMilliseconds - startMilliseconds) / 1000)+" seconds");

	}
}
