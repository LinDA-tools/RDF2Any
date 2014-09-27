package de.unibonn.iai.eis.linda.querybuilder.objects;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;

/**
 * @author gsingharoy
 * 
 *         This class will contain an object node of the searched RDFObject and
 *         its properties
 **/
public class RDFObject {
	public RDFClass hasClass;
	public String name;
	public String uri;
	public List<RDFObjectProperty> properties;

	public RDFObject(RDFClass hasClass, String uri) {
		this.hasClass = hasClass;
		this.uri = uri;
		this.name = getName();

	}

	public RDFObject(RDFClass hasClass, String uri, String name) {
		this.hasClass = hasClass;
		this.uri = uri;
		this.name = name;
	}

	private String getName() {
		return SPARQLHandler.getLabelFromNode(this.hasClass.dataset, this.uri,
				"EN");
	}

	private String getName(RDFClass hasClass, String uri) {
		return SPARQLHandler.getLabelFromNode(hasClass.dataset, uri, "EN");
	}

	// this method generates the properties required from the RDFClass
	// properties
	public void generateProperties() {
		ResultSet propertiesResultSet = SPARQLHandler.executeQuery(
				this.hasClass.dataset, propertiesSPARQLQuery());
		Integer i = 0;
		String prevUri = "";
		RDFObjectProperty oProperty = null;
		while (propertiesResultSet.hasNext()) {
			QuerySolution row = propertiesResultSet.next();
			RDFNode predicate = row.get("predicate");
			String strPredicate = predicate.toString();
			if (!strPredicate.equals(prevUri)) {
				oProperty = new RDFObjectProperty(
						this.hasClass.getPropertyFromStringUri(strPredicate));
			}
			RDFNode object = row.get("object");
			String strObject = object.toString();
			oProperty.objects.add(new RDFObjectPropertyValue(strObject));
			i++;
		}
		System.out.println(i + " properties found for " + this.uri);
	}

	public String propertiesSPARQLQuery() {
		String query = SPARQLHandler.getPrefixes();
		query += " SELECT DISTINCT ?predicate ?object WHERE { <" + this.uri
				+ "> ?predicate ?object. FILTER(";
		Boolean firstProperty = true;
		for (RDFClassProperty property : this.hasClass.properties) {
			if (!firstProperty)
				query += " || ";
			query += " ?predicate = <" + property.uri + "> ";
			firstProperty = false;
		}
		query += ")} ORDER BY ?predicate";
		return query;
	}

	public String toString() {
		String result = "uri : " + this.uri + ", name : " + this.name + ", has class : "
				+ this.hasClass.label+"\nProperties : ";
		for(RDFObjectProperty p:this.properties){
			result +="\n"+p.toString();
		}
		return result;
	}
}
