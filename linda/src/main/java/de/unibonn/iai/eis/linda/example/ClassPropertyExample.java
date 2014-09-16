package de.unibonn.iai.eis.linda.example;

import java.util.regex.Pattern;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

public class ClassPropertyExample {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		//http://dbpedia.org/ontology/Person
		System.out.println("Starting ... ");
		RDFClass rdfClass = new RDFClass("http://dbpedia.org/sparql", "http://dbpedia.org/ontology/Person");
		rdfClass.generatePropertiesFromSPARQL();
		System.out.println(rdfClass.toString());
		System.out.println("Ended ... ");
	}

}
