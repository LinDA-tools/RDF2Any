package de.unibonn.iai.eis.linda.helper;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

/*
 * This class will contain helper methods for Lucene
 * */
public class LuceneHelper {
	public static final Version LUCENE_VERSION = Version.LATEST;
	public static String homeDir(){
		return ".lucene_indexes";
	}
	
	public static String classPropertiesDir(String dataset){
		return homeDir() + "/class_properties/properties/" + dataset.hashCode();
	}

	public static StandardAnalyzer getAnalyzer(){
		return new StandardAnalyzer(
				LuceneHelper.LUCENE_VERSION);
	}
	
	public static String classPropertiesValidatorDir(String dataset){
		return homeDir() + "/class_properties/classes/" + dataset.hashCode();
	}
	
	public static String getUriFromIndexEntry(String entry){
		return entry.substring(1, entry.length()-1);
	}
	
	public static IndexReader getClassPropertiesIndexReader(String dataset) throws IOException{
		File indexPath = new File(
				LuceneHelper.classPropertiesDir(dataset));
		Directory index = new SimpleFSDirectory(indexPath);
		IndexReader reader;

		reader = DirectoryReader.open(index);
		return reader;
	}
	public static IndexReader getClassPropertiesValidatorIndexReader(String dataset) throws IOException{
		File indexPath = new File(
				LuceneHelper.classPropertiesValidatorDir(dataset));
		Directory index = new SimpleFSDirectory(indexPath);
		IndexReader reader;

		reader = DirectoryReader.open(index);
		return reader;
	}
}
