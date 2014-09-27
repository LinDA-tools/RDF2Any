package de.unibonn.iai.eis.linda.querybuilder.objects;

public class RDFObjectPropertyValue {
	public String value;
	public String additionalValue;
	
	public RDFObjectPropertyValue(String value){
		this.value = value;
		this.additionalValue = "";
	}
	
	public RDFObjectPropertyValue(String value, String additionalValue){
		this.value = value;
		this.additionalValue = additionalValue;
	}
}
