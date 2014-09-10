package de.unibonn.iai.eis.linda.helper;
/*
 * This class will contain helper methods for Lucene
 * */
public class LuceneHelper {
	public static String homeDir(){
		return ".lucene_indexes";
	}
	
	public static String classPropertiesDir(String dataset){
		return homeDir() + "/class_properties/" + dataset.hashCode();
	}
}
