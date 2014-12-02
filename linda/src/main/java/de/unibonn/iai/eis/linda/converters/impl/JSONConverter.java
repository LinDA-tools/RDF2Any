package de.unibonn.iai.eis.linda.converters.impl;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.impl.results.JSONObjectsOutput;
import de.unibonn.iai.eis.linda.converters.impl.results.JSONOutput;
import de.unibonn.iai.eis.linda.converters.impl.results.sesame.JSONSesameOutput;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

/**
 * @author gsingharoy
 * 
 * 
 **/

public class JSONConverter extends MainConverter {

	public ResultSet rdfResults;
	public JSONOutput jsonOutput;
	public JSONObjectsOutput jsonObjectsOutput;
	public JSONSesameOutput jsonSesameOutput;
	public String outputFormat;
	public RDFClass forClass;

	public JSONConverter(ResultSet rdfResultSet) {
		this.rdfResults = rdfResultSet;
		super.generateResultVars(rdfResultSet);
		this.jsonOutput = new JSONOutput(rdfResultSet);
		this.forClass = null;
		this.jsonObjectsOutput = null;
		this.jsonSesameOutput = null;
		this.outputFormat = "virtuoso";
		convert();
	}

	public JSONConverter(ResultSet rdfResultSet, String outputFormat) {
		this.rdfResults = rdfResultSet;
		super.generateResultVars(rdfResultSet);
		if (outputFormat.equalsIgnoreCase("sesame"))
			this.outputFormat = "sesame";
		else
			this.outputFormat = "virtuoso";
		if (isSesameConvert()) {
			this.jsonOutput = null;
			this.jsonSesameOutput = new JSONSesameOutput(rdfResultSet);
		} else {
			this.jsonOutput = new JSONOutput(rdfResultSet);
			this.jsonSesameOutput = null;
		}
		this.forClass = null;
		this.jsonObjectsOutput = null;
		convert();
	}

	public JSONConverter(ResultSet rdfResultSet, RDFClass forClass) {
		this.rdfResults = rdfResultSet;
		// super.generateResultVars(rdfResultSet);
		this.jsonOutput = null;
		this.forClass = forClass;
		this.jsonObjectsOutput = new JSONObjectsOutput(forClass);
		this.jsonSesameOutput = null;
		this.outputFormat = "";
		convert();
	}

	public void addResultSetRowToOutput(QuerySolution row) {
		Map<String, Map<String, String>> bindingEntry = new HashMap<String, Map<String, String>>();
		for (int i = 0; i < resultVars.size(); i++) {
			Map<String, String> columnEntry = new HashMap<String, String>();
			if (row.get(resultVars.get(i)) instanceof Literal) {
				RDFNode literal = row.get(resultVars.get(i));
				
				if (literal.toString().length() > 3) {
					Integer languageIdentifierPoint = literal.toString()
							.length() - 3;
					if (literal.toString().charAt(languageIdentifierPoint) == '@') {
						columnEntry.put("type", "literal");
						columnEntry.put("xml:lang", literal.toString()
								.substring(languageIdentifierPoint + 1));
						columnEntry.put(
								"value",
								literal.toString().substring(0,
										languageIdentifierPoint));
					} else {
						columnEntry.put("type", "typed-literal");
						columnEntry.put("value",
								SPARQLHandler.getLiteralValue(literal));
						columnEntry.put("datatype", SPARQLHandler.getLiteralDataType(literal));
					}

				} else {
					columnEntry.put("value", literal.toString());

				}
			} else if (row.get(resultVars.get(i)) instanceof RDFNode) {
				RDFNode node = row.get(resultVars.get(i));
				columnEntry.put("type", "uri");
				columnEntry.put("value", node.toString());
			}
			bindingEntry.put(resultVars.get(i), columnEntry);
		}
		this.jsonOutput.results.bindings.add(bindingEntry);
	}

	public void addResultSetRowToSesameOutput(QuerySolution row) {
		this.jsonSesameOutput.addResultBinding(row, super.resultVars);
	}

	public void convert() {
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			if (this.forClass == null) {
				if (isVistuosoConvert()) {
					addResultSetRowToOutput(row);
				} else if (isSesameConvert()) {
					addResultSetRowToSesameOutput(row);
				}
			} else {
				objectConvert(row);
			}
		}
		if (isSesameConvert()) {
			this.jsonSesameOutput.setFinalResult();
		}

	}

	public void objectConvert(QuerySolution row) {
		try {
			RDFNode object = row.get("concept");
			if (object != null) {
				RDFNode objectName = row.get("label");
				RDFObject rdfObject = null;
				if (objectName != null) {
					rdfObject = new RDFObject(forClass, object.toString(),
							SPARQLHandler.getLabelName(objectName));
				} else {
					rdfObject = new RDFObject(forClass, object.toString());
				}
				rdfObject.generateProperties();
				this.jsonObjectsOutput.addObject(rdfObject);
			}
		} catch (Exception e) {
			System.out.println("Error occured :  " + e.toString());
		}
	}

	public Boolean isVistuosoConvert() {
		if (this.outputFormat.equalsIgnoreCase("virtuoso"))
			return true;
		else
			return false;
	}

	public Boolean isSesameConvert() {
		if (this.outputFormat.equalsIgnoreCase("sesame"))
			return true;
		else
			return false;
	}

	public void setTimeTaken(Double timeTaken) {
		if (forClass == null) {
			if (isVistuosoConvert())
				this.jsonOutput.setTimeTaken(timeTaken);
		}
	}
}
