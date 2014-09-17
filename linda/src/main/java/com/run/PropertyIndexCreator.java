package com.run;

import java.io.IOException;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;

public class PropertyIndexCreator {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String dataset = args.length > 0 ? args[0] : "";
		if(!dataset.equals("")){
			System.out.println("Starting to create indexes for dataset "+dataset);
			RDFClass.generateIndexesForDataset(dataset);
		}

	}

}
