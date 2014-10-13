package de.unibonn.iai.eis.linda.querybuilder.objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.helper.SPARQLHandler;

public class RDFObjectPropertyValue {
	public String value;
	public String additionalValue;

	public RDFObjectPropertyValue(String value) {
		this.value = value;
		this.additionalValue = "";
	}

	public RDFObjectPropertyValue(String value, String additionalValue) {
		this.value = value;
		this.additionalValue = additionalValue;
	}

	public RDFObjectPropertyValue(RDFObjectProperty predicate, RDFNode object) {
		this.additionalValue = "";
		if (object.isLiteral()) {
			if (predicate.predicate.range.isLanguageLiteral()) {
				this.value = SPARQLHandler.getLabelName(object);
				this.additionalValue = SPARQLHandler.getLabelLanguage(object);
			} else {
				this.value = object.toString().replace(
						"^^" + predicate.predicate.range.uri, "");
				this.additionalValue = "";
				/*
				if (predicate.predicate.type.equals("data")
						&& predicate.predicate.range.label
								.equalsIgnoreCase("integer")
						|| predicate.predicate.range.label
								.equalsIgnoreCase("nonnegativeinteger")) {

					if(this.value.equals(object.toString())){
						if(predicate.predicate.range.label
								.equalsIgnoreCase("integer"))
							this.value = object.toString().replace("^^"+SPARQLHandler.getXMLSchemaURI()+"#nonNegativeInteger","");
						else if(predicate.predicate.range.label
								.equalsIgnoreCase("nonnegativeinteger"))
							this.value = object.toString().replace("^^"+SPARQLHandler.getXMLSchemaURI()+"#integer","");
						else
							this.value = object.toString().split("\\^\\^")[0];
					}
				}*/
				if (predicate.predicate.type.equals("data") && this.value.equals(object.toString())){
					this.value = object.toString().split("\\^\\^")[0];
				}
			}
		} else {
			this.value = object.toString();
		}

	}

	@JsonIgnore
	public String toString() {
		String result = this.value;
		if (this.additionalValue != null && !this.additionalValue.equals(""))
			result += " [" + this.additionalValue + "]";
		return result;
	}

}
