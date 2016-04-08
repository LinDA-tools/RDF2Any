package de.unibonn.iai.eis.linda.querybuilder.classes;

import com.owlike.genson.annotation.JsonIgnore;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import de.unibonn.iai.eis.linda.helper.CommonHelper;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFClassProperty {
	public String uri;

	public String type;
	public String label;
	public Integer count;
	public RDFClassPropertyRange range;
	public Boolean multiplePropertiesForSameNode; // This variable determines if
													// there are multiple
													// properties like this. Its
													// important as this
													// determines how to
													// Normalize in RDF
													// conversions

	public String hintExample;

	public RDFClassProperty(String uri, String type, String label) {
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.multiplePropertiesForSameNode = false;
		this.hintExample = "";
	}

	public RDFClassProperty(String uri, String type, String label,
			RDFClassPropertyRange range) {
		this.uri = uri;
		this.count = 0;
		this.type = type;
		this.label = label;
		this.range = range;
		this.multiplePropertiesForSameNode = false;
		this.hintExample = "";
	}
	
	public void addHintExample(String endpoint){
		String query = "SELECT ?example { ?s <"+this.uri+"> ?example . } LIMIT 1";
		ResultSet rs = SPARQLHandler.executeQuery(endpoint, query);
		if (rs.hasNext()){
			RDFNode node = rs.next().get("example");
			if (node.isResource())
				hintExample = node.asResource().getURI();
			else 
				hintExample = node.asLiteral().getLexicalForm();
		}
	}

	public RDFClassProperty(String uri, String type, String label,
			Integer count, Boolean multiplePropertiesForSameNode,
			RDFClassPropertyRange range) {
		this.uri = uri;
		this.count = count;
		this.type = type;
		this.label = label;
		this.range = range;
		this.multiplePropertiesForSameNode = false;
	}

	
	public void addCountOfProperty(int count){
		this.count = count;
		System.out.println("generated count for " + this.uri + " ("+ this.count+ ")");
	}
	
	public void addRange(Resource range, Literal rangeLabel, String type){
		if ((range != null) && (rangeLabel != null)){
			this.range = new RDFClassPropertyRange(range.getURI(), rangeLabel.getValue().toString());
			this.type = type;
		} else if ((range != null) && (rangeLabel == null)){
			this.range = new RDFClassPropertyRange(range.getURI());
			this.range.generateRangeLabel();
			this.type = type;
		}
	}

	@JsonIgnore
	public String getPropertyUnderscoreVariableName(){
		String restOfTheUri = this.uri.replace(SPARQLHandler.getBaseUrl(this.uri),"");
		return CommonHelper.getUnderscoreStringFromCamelCase(restOfTheUri.substring(1,restOfTheUri.length()));
	}
	
	@JsonIgnore
	private String getRangeSPARQLQuery() {
		String query = SPARQLHandler.getPrefixes();
		query += "select distinct ?range where {<" + this.uri
				+ "> rdfs:range ?range}";
		return query;
	}
	
	@JsonIgnore
	public Boolean hasValidRange() {
		Boolean result = true;
		if (this.type.equals("object")) {
			if (this.range == null)
				result = false;
			else if (this.range.label == null || this.range.label.equals(""))
				result = false;
		}
		return result;
	}

	/*
	 * START RDB methods
	 */
	@JsonIgnore
	public String getTableAttributeType() {
		if (this.type.equalsIgnoreCase("object"))
			return "int";
		else {
			if (this.range.label.equalsIgnoreCase("integer") || this.range.label.equalsIgnoreCase("nonnegativeinteger"))
				return "int";
			else
				return "text";
		}
	}
	@JsonIgnore
	public String getTableAttributeName() {
		if (this.type.equalsIgnoreCase("object"))
			return CommonHelper.getVariableName(this.label, "") + "_id";
		else
			return CommonHelper.getVariableName(this.label, "");
	}

	@JsonIgnore
	public String getRangeTableName(){
		String result = "";
		if(this.type.equals("object")){
			result = CommonHelper.getVariableName(range.label, "thing")+"s";
		}
		return result;
	}
	@JsonIgnore
	public String getTableName(RDFClass rdfClass) {
		return rdfClass.getVariableName()
				+ CommonHelper.getVariableName(this.label, "thing", false)
				+ "s";
	}

	/*
	 * END RDB methods
	 */
	
	
	/*
	 * START CSV methods
	 * 
	 * */
	
	//this method returns the header attribute name
	@JsonIgnore
	public String getCSVHeaderAttributeName() {
		return this.label.trim().toLowerCase().replace(" ", "_");
	}
	/*
	 * END CSV methods
	 * 
	 * */
	
	
	@JsonIgnore
	public String toString() {
		return "uri : " + this.uri + ", type : " + this.type + ", label : "
				+ this.label + ", range : {" + this.range.toString()
				+ "}, count : " + this.count.toString()
				+ ", has multiple properties for the same node : "
				+ this.multiplePropertiesForSameNode.toString();
	}
	
	@JsonIgnore
	public boolean equals(Object otherObject){
		if (!(otherObject instanceof RDFClassProperty)) return false;
		
		RDFClassProperty _otherObject = (RDFClassProperty) otherObject;
		
		return (this.uri.equals(_otherObject.uri));
	}
	
	@JsonIgnore
	public int hashcode(){
		return this.uri.hashCode();
	}

}
