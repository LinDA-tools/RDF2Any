package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.helper.CSVHelper;
import de.unibonn.iai.eis.linda.helper.RDBHelper;

/**
 * @author gsingharoy
 *
 *This converts ResultSet to SQL file
 **/

public class RDBConverter extends MainConverter implements Converter{
	private String tableName;
	
	public RDBConverter() {
		this.tableName = "rdf_table";
	}
	public RDBConverter(String tableName) {
		this.tableName = tableName;
	}
	
	private String generateFileHeader(){
		String result = "DROP TABLE IF EXISTS "+this.tableName+";\n\n"; 
		result += "CREATE TABLE "+this.tableName+"\n(\nID int";
		for(int i=0;i<resultVars.size();i++){
			result +=",\n"+ resultVars.get(i)+" varchar(1000)";
		}
		result += ",\nPRIMARY KEY ID\n);\n\n\n";
		return result;
	}
	
	private String generateFileResultRow(QuerySolution row, Integer primaryKey){
		String result = "INSERT INTO "+this.tableName+" VALUES("+primaryKey.toString();
		for(int i=0;i<resultVars.size();i++){
			result += ",'"+RDBHelper.getSQLReadyEntry(row.get(resultVars.get(i)).toString())+"'";
		}
		result +=");\n";
		return result; 
	}
	
	public void convert(OutputStream output, ResultSet rdfResults)
			throws IOException {
		super.generateResultVars(rdfResults);
		Integer counter = 1;
		output.write(generateFileHeader().getBytes(Charset.forName("UTF-8")));
		while(rdfResults.hasNext()){
			QuerySolution row= rdfResults.next();
			output.write(generateFileResultRow(row,counter).getBytes(Charset.forName("UTF-8")));
			counter++;
		}
		
	}

}
