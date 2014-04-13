package de.unibonn.iai.eis.linda.helper;
/**
 * @author gsingharoy
 *
 *This class has helper methods for CSV
 **/
public class CSVHelper {
	public static String getCSVReadyEntry(String str){
		if(str.contains(","))
			return "\""+str +"\"";
		else
			return str;
	}
}
