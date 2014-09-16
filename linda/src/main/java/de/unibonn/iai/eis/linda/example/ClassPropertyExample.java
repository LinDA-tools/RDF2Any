package de.unibonn.iai.eis.linda.example;

import java.io.IOException;
import java.util.regex.Pattern;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

public class ClassPropertyExample {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		//http://dbpedia.org/ontology/Person
		System.out.println("Starting ... ");
		RDFClass rdfClass = new RDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/Actor");
		rdfClass.generatePropertiesFromSPARQL();
		//System.out.println(rdfClass.toString());
		System.out.println("Ended ... ");
		rdfClass.generateLuceneIndexes();
	}

}
