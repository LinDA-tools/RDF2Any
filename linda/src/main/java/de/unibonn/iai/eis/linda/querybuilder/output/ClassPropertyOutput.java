package de.unibonn.iai.eis.linda.querybuilder.output;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import de.unibonn.iai.eis.linda.querybuilder.classes.MultipleRDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

@JsonInclude(Include.NON_NULL)
public class ClassPropertyOutput {
	public RDFClass rdfClass = null;
	public MultipleRDFClass multiRdfClass = null;
	
	public ClassPropertyOutput(RDFClass rdfClass){
		this.rdfClass = rdfClass;
	}
	
	public ClassPropertyOutput(MultipleRDFClass multiRdfClass){
		this.multiRdfClass = multiRdfClass;
	}
}
