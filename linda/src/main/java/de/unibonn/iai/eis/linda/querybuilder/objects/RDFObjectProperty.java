package de.unibonn.iai.eis.linda.querybuilder.objects;

import java.util.ArrayList;
import java.util.List;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;

/**
 * @author gsingharoy
 *
 *This class will contain pairs of predicates and objects
 **/
public class RDFObjectProperty {
	public RDFClassProperty predicate;
	public List<RDFObjectPropertyValue> objects;
	
	public RDFObjectProperty(RDFClassProperty predicate){
		this.predicate = predicate;
		this.objects = new ArrayList<RDFObjectPropertyValue>();
	}
	
	public String toString(){
		String result = "property : "+this.predicate.uri+", values : ";
		for(Integer i=0;i<objects.size();i++){
			if(i>0)
				result +=", ";
			result +=objects.get(i).toString();
		}
		return result;
	}
}
