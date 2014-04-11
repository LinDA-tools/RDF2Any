package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;

/**
 * @author gsingharoy
 *
 *This converts ResultSet to SQL file
 **/

public class RDBConverter implements Converter{
	public String tableName;
	
	public RDBConverter() {
		this.tableName = "rdf_table";
	}
	public RDBConverter(String tableName) {
		this.tableName = tableName;
	}
	
	public void convert(OutputStream output, ResultSet rdfResults)
			throws IOException {
		Integer counter = 1;
		String strResult = "DROP TABLE IF EXISTS "+this.tableName+";\n\n"; 
		strResult += "CREATE TABLE "+this.tableName+"\n(\n";
		strResult += "ID int,\nsubject varchar(255),\nlabel varchar(255),\nPRIMARY KEY ID\n);\n\n\n";
		output.write(strResult.getBytes(Charset.forName("UTF-8")));
		
		while(rdfResults.hasNext()){
			QuerySolution row= rdfResults.next();
			RDFNode subject= row.get("subject");
			Literal label= row.getLiteral("label");
			strResult = "INSERT INTO "+this.tableName+" VALUES("+counter.toString()+", '"+subject.toString()+"', '"+label.toString()+"');\n";
			output.write(strResult.getBytes(Charset.forName("UTF-8")));
			counter++;
		}
		
	}

}
