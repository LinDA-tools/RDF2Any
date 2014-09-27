package de.unibonn.iai.eis.linda.example;


import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.lucene.queryparser.classic.ParseException;



import de.unibonn.iai.eis.linda.helper.CommonHelper;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;

public class ClassPropertyExample {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws IOException, ParseException {
		// TODO Auto-generated method stub
		//http://dbpedia.org/ontology/Person
		//System.out.println("Starting ... ");
		//RDFClass rdfClass = new RDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/Actor");
		//rdfClass.generatePropertiesFromSPARQL();
		
		//RDFClass rdfClass = RDFClass.searchRDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/Actor");
		//System.out.println(rdfClass.toString());
		//System.out.println("Ended ... ");
		//rdfClass.generateLuceneIndexes();
		//RDFClass.generateIndexesForDataset("http://dbpedia.org/sparql");
		
		//RDFClass s = RDFClass.searchRDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/Actor");
		//RDFClass s = new RDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/City");
		//System.out.println(s.uri.hashCode());
		//s.addLuceneValidatorDoc();
		
		//System.out.println(s);
		
		//System.out.println(CommonHelper.decode("c2VsZWN0IGRpc3RpbmN0ID9Db25jZXB0IHdoZXJlIHtbXSBhID9Db25jZXB0fSBMSU1JVCAxMDA"));
		RDFClass athlete = RDFClass.searchRDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/Athlete");
		//System.out.println(athlete);
		RDFObject alexTait = new RDFObject(athlete, "http://dbpedia.org/resource/Alex_Tait_(cricketer)" );
		alexTait.generateProperties();
		System.out.println(alexTait);
		System.out.println(athlete.getVariableName());
	}

}
