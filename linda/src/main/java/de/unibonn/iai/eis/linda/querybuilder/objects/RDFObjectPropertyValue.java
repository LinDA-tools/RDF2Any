package de.unibonn.iai.eis.linda.querybuilder.objects;

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

					}
				}
			}
		} else {
			this.value = object.toString();
		}

	}

	public String toString() {
		String result = this.value;
		if (this.additionalValue != null && !this.additionalValue.equals(""))
			result += " [" + this.additionalValue + "]";
		return result;
	}

}
