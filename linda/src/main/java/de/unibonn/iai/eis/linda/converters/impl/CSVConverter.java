package de.unibonn.iai.eis.linda.converters.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;

/**
 * @author gsingharoy
 *
 *This converts ResultSet to CSV
 **/

public class CSVConverter implements Converter{

	private List<String> resultVars;
	
	private void generateResultVars(ResultSet rdfResultSets){
		this.resultVars = rdfResultSets.getResultVars();
	}
	private String generateFileHeader(){
		String result = "";
		for(int i=0;i<resultVars.size();i++){
			if(i>0)
				result+=",";
			result += resultVars.get(i);
		}
		result += "\n";
		return result;
	}
	private String generateFileResultRow(QuerySolution row) throws Exception{
		String result = "";
		for(int i=0;i<resultVars.size();i++){
			if(i>0)
				result+=",";
			result += row.get(resultVars.get(i)).toString();
		}
		result +="\n";
		return result;
	}
	public void convert(OutputStream outputStream, ResultSet rdfResults) throws IOException {
		generateResultVars(rdfResults);
		outputStream.write(generateFileHeader().getBytes(Charset.forName("UTF-8")));
		while(rdfResults.hasNext()){
			QuerySolution row= rdfResults.next();
			try {
				outputStream.write(generateFileResultRow(row).getBytes(Charset.forName("UTF-8")));
			} catch (Exception e) {
				//pass
			}

		}

	}

}
