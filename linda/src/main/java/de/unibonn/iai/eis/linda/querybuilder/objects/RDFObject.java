package de.unibonn.iai.eis.linda.querybuilder.objects;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

/**
 * @author gsingharoy
 *
 *This class will contain an object node of the searched RDFObject and its properties
 **/
public class RDFObject {
	public RDFClass hasClass;
	public String name;
	public String uri;
	public RDFObject(RDFClass hasClass, String uri){
		this.hasClass = hasClass;
		this.uri = uri;
		this.name = getName();
		
	}
	
	public RDFObject(RDFClass hasClass, String uri, String name){
		this.hasClass = hasClass;
		this.uri = uri;
		this.name = name;
	}
	
	public String getName(){
		return SPARQLHandler.getLabelFromNode(this.hasClass.dataset, this.uri, "EN");
	}
	public String getName(RDFClass hasClass, String uri){
		return SPARQLHandler.getLabelFromNode(hasClass.dataset, uri, "EN");
	}
}
