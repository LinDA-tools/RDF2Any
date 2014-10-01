package de.unibonn.iai.eis.linda.helper;


/**
 * @author gsingharoy
 *
 *This class handles RDB functionality
 **/


public class RDBHelper {

	//This function returns a string which can be used in the INSERT
	//statment of an SQL query
	public static String getSQLReadyEntry(String str){
		String resultString = "";
		for(int i=0;i<str.length();i++){
			if((int)str.charAt(i) == 39){
				//Check for single quotes '
				resultString +="''";
			}
			else{
				resultString += str.charAt(i);
			}
		}
		return resultString;
	}
}
