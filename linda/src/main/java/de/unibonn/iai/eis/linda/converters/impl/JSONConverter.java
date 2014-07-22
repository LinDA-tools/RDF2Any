package de.unibonn.iai.eis.linda.converters.impl;

import java.util.HashMap;
import java.util.Map;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

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
		convert();
	}
	public void addResultSetRowToOutput(QuerySolution row){
		Map<String, Map<String,String>> bindingEntry = new HashMap<String, Map<String,String>>();
		for(int i=0;i<resultVars.size();i++){
			Map<String,String> columnEntry = new HashMap<String,String>();
			if(row.get(resultVars.get(i)) instanceof Literal ){
				RDFNode literal = row.get(resultVars.get(i));
				columnEntry.put("type", "literal");
				if(literal.toString().length() > 3){
					Integer languageIdentifierPoint = literal.toString().length()-3;
					if(literal.toString().charAt(languageIdentifierPoint) == '@'){
						columnEntry.put("xml:lang", literal.toString().substring(languageIdentifierPoint+1));
						columnEntry.put("value", literal.toString().substring(0,languageIdentifierPoint));
					}

				}
				else{
					columnEntry.put("value", literal.toString());

				}
			}
			else if(row.get(resultVars.get(i)) instanceof RDFNode ){
				RDFNode node = row.get(resultVars.get(i));
				columnEntry.put("type", "uri");
				columnEntry.put("value", node.toString());
			}
			bindingEntry.put(resultVars.get(i), columnEntry);
		}
		this.jsonOutput.results.bindings.add(bindingEntry);
	}
	public void convert(){
		while(rdfResults.hasNext()){
			QuerySolution row= rdfResults.next();
			addResultSetRowToOutput(row);
		}
	}
}
