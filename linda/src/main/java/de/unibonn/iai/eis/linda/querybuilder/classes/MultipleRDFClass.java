package de.unibonn.iai.eis.linda.querybuilder.classes;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class MultipleRDFClass {

	@JsonIgnore private List<RDFClass> rdfClasses = new ArrayList<RDFClass>();
	public List<RDFClassProperty> rdfProperties = new ArrayList<RDFClassProperty>();
	
	
	public void addRDFClass(RDFClass rdfClass){
		rdfClasses.add(rdfClass);
	}
	
	public void generatePropertyVector(){
		// Get Initial Set
		RDFClass class1 = rdfClasses.get(0);
		RDFClass class2 = rdfClasses.get(1);
		
		List<RDFClassProperty> intersection = new ArrayList<RDFClassProperty>(class1.properties); 
		List<RDFClassProperty> set2 = new ArrayList<RDFClassProperty>(class2.properties); 
		intersection.retainAll(set2); // intersection between 2 property lists
	
		int i = 2;
		while (i < rdfClasses.size()){
			List<RDFClassProperty> _otherSet = new ArrayList<RDFClassProperty>(rdfClasses.get(i).properties);
			intersection.retainAll(_otherSet);
			i++;
		}
		
		rdfProperties.addAll(intersection);
	}
	
	
}
