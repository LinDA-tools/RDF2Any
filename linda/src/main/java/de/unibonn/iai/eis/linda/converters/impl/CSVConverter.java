package de.unibonn.iai.eis.linda.converters.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;

public class CSVConverter implements Converter{


	public void convert(OutputStream outputStream, ResultSet rdfResults) throws IOException {
		String strResult = "subject,label\n"; 
		outputStream.write(strResult.getBytes(Charset.forName("UTF-8")));
		while(rdfResults.hasNext()){
			QuerySolution row= rdfResults.next();
			RDFNode subject= row.get("subject");
			Literal label= row.getLiteral("label");
			strResult = subject.toString()+","+label.toString()+"\n";
			outputStream.write(strResult.getBytes(Charset.forName("UTF-8")));

		}

	}

}
