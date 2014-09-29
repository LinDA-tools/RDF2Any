package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.message.internal.UriProvider;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.helper.CSVHelper;
import de.unibonn.iai.eis.linda.helper.RDBHelper;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObjectProperty;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObjectPropertyValue;

/**
 * @author gsingharoy
 * 
 *         This converts ResultSet to SQL file
 **/

public class RDBConverter extends MainConverter implements Converter {
	private String tableName;

	public RDBConverter() {
		this.tableName = "rdf_table";
	}

	public RDBConverter(String tableName) {
		this.tableName = tableName;
	}

	private String generateFileHeader() {
		String result = "DROP TABLE IF EXISTS " + this.tableName + ";\n\n";
		result += "CREATE TABLE " + this.tableName + "\n(\nID int";
		for (int i = 0; i < resultVars.size(); i++) {
			result += ",\n" + resultVars.get(i) + " varchar(1000)";
		}
		result += ",\nPRIMARY KEY ID\n);\n\n\n";
		return result;
	}

	private String generateFileResultRow(QuerySolution row, Integer primaryKey) {
		String result = "INSERT INTO " + this.tableName + " VALUES("
				+ primaryKey.toString();
		for (int i = 0; i < resultVars.size(); i++) {
			result += ",'"
					+ RDBHelper.getSQLReadyEntry(row.get(resultVars.get(i))
							.toString()) + "'";
		}
		result += ");\n";
		return result;
	}

	public void convert(OutputStream output, ResultSet rdfResults)
			throws IOException {
		super.generateResultVars(rdfResults);
		Integer counter = 1;
		output.write(generateFileHeader().getBytes(Charset.forName("UTF-8")));
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			output.write(generateFileResultRow(row, counter).getBytes(
					Charset.forName("UTF-8")));
			counter++;
		}

	}

	// This convert implementation is when a class is queried from the query
	// builder
	@Override
	public void convert(OutputStream output, ResultSet rdfResults,
			RDFClass forClass) throws IOException {
		// TODO Auto-generated method stub
		output.write((forClass.getTableCreationScript(true) + "\n\n")
				.getBytes(Charset.forName("UTF-8")));
		List<String> tableNames = forClass.getTableNames();
		Map<String, Integer> tableCounters = new HashMap<String, Integer>();
		for (String tableName : tableNames) {
			tableCounters.put(tableName, 0);
		}
		Map<String, Integer> uriPrimaryKeyLookup = new HashMap<String, Integer>();
		Integer mainTableCounter = 0;
		String mainTableName = forClass.getTableName();
		while (rdfResults.hasNext()) {
			mainTableCounter++;
			QuerySolution row = rdfResults.next();
			System.out.println(mainTableCounter
					+ ".##################################");
			try {
				RDFNode object = row.get("concept");
				if (object != null) {
					RDFNode objectName = row.get("label");
					RDFObject rdfObject = null;
					if (objectName != null) {
						rdfObject = new RDFObject(forClass, object.toString(),
								SPARQLHandler.getLabelName(objectName));
					} else {
						rdfObject = new RDFObject(forClass, object.toString());
					}
					output.write(("\n\n" + rdfObject
							.getInsertRowScript(mainTableCounter))
							.getBytes(Charset.forName("UTF-8")));
					rdfObject.generateProperties();
					for (RDFObjectProperty objectProperty : rdfObject.properties) {
						if (!objectProperty.predicate.multiplePropertiesForSameNode) {
							// section for one object of the same property
							if (objectProperty.predicate.type.equals("data")) {
								if (objectProperty.predicate
										.getTableAttributeType().equals("int"))
									output.write(("\nUPDATE "
											+ mainTableName
											+ " SET "
											+ objectProperty.predicate
													.getTableAttributeName()
											+ " = "
											+ RDBHelper
													.getSQLReadyEntry(objectProperty.objects
															.get(0).value)
											+ " WHERE ID=" + mainTableCounter + ";")
											.getBytes(Charset.forName("UTF-8")));
								else {
									if (objectProperty.predicate.range
											.isLanguageLiteral())
										output.write(("\nUPDATE "
												+ mainTableName
												+ " SET "
												+ objectProperty.predicate
														.getTableAttributeName()
												+ " = '"
												+ RDBHelper
														.getSQLReadyEntry(objectProperty.objects
																.get(0).value)
												+ "', "
												+ objectProperty.predicate
														.getTableAttributeName()
												+ "Lang = '"
												+ objectProperty.objects.get(0).additionalValue
														.toUpperCase()
												+ "' WHERE ID="
												+ mainTableCounter + ";")
												.getBytes(Charset
														.forName("UTF-8")));
									else
										output.write(("\nUPDATE "
												+ mainTableName
												+ " SET "
												+ objectProperty.predicate
														.getTableAttributeName()
												+ " = '"
												+ RDBHelper
														.getSQLReadyEntry(objectProperty.objects
																.get(0).value)
												+ "' WHERE ID="
												+ mainTableCounter + ";")
												.getBytes(Charset
														.forName("UTF-8")));

								}
							} else {
								if (objectProperty.predicate.hasValidRange()) {
									Integer foreignKey = uriPrimaryKeyLookup
											.get(objectProperty.objects.get(0).value
													.toLowerCase());
									if (foreignKey == null) {
										foreignKey = tableCounters
												.get(objectProperty.predicate
														.getRangeTableName()) + 1;
										tableCounters.put(
												objectProperty.predicate
														.getRangeTableName(),
												foreignKey);
										output.write(("\nINSERT INTO "
												+ objectProperty.predicate
														.getRangeTableName()
												+ "(ID, uri , name) VALUES ("
												+ foreignKey
												+ ", '"
												+ RDBHelper
														.getSQLReadyEntry(objectProperty.objects
																.get(0).value)
												+ "', '"
												+ RDBHelper
														.getSQLReadyEntry(SPARQLHandler
																.getLabelFromNode(
																		forClass.dataset,
																		objectProperty.objects
																				.get(0).value,
																		"EN")) + "');")
												.getBytes(Charset
														.forName("UTF-8")));
										uriPrimaryKeyLookup
												.put(objectProperty.objects
														.get(0).value
														.toLowerCase(),
														foreignKey);
									}
									output.write(("\nUPDATE "
											+ mainTableName
											+ " SET "
											+ objectProperty.predicate
													.getTableAttributeName()
											+ " = " + foreignKey + " WHERE ID="
											+ mainTableCounter + ";")
											.getBytes(Charset.forName("UTF-8")));

								}
							}
						} else {
							// section for multiple objects of the same property
							if (objectProperty.predicate.type.equals("data")) {
								String objectPropertyTableName = objectProperty.predicate
										.getTableName(forClass);
								for (RDFObjectPropertyValue objectPropertyValue : objectProperty.objects) {
									Integer objectPropertyPrimaryKey = tableCounters
											.get(objectPropertyTableName) + 1;
									tableCounters.put(objectPropertyTableName,
											objectPropertyPrimaryKey);
									if (objectProperty.predicate
											.getTableAttributeType().equals(
													"int"))
										output.write(("\nINSERT INTO "
												+ objectPropertyTableName
												+ "(ID,"
												+ forClass.getVariableName()
												+ "ID,"
												+ objectProperty.predicate
														.getTableAttributeName()
												+ ") VALUES("
												+ objectPropertyPrimaryKey
												+ ", "
												+ mainTableCounter
												+ ", "
												+ RDBHelper
														.getSQLReadyEntry(objectPropertyValue.value) + ");")
												.getBytes(Charset
														.forName("UTF-8")));
									else {
										if (objectProperty.predicate.range
												.isLanguageLiteral())
											output.write(("\nINSERT INTO "
													+ objectPropertyTableName
													+ "(ID,"
													+ forClass
															.getVariableName()
													+ "ID,"
													+ objectProperty.predicate
															.getTableAttributeName()
													+ ", "
													+ objectProperty.predicate
															.getTableAttributeName()
													+ "Lang) VALUES("
													+ objectPropertyPrimaryKey
													+ ", "
													+ mainTableCounter
													+ ", '"
													+ RDBHelper
															.getSQLReadyEntry(objectPropertyValue.value)
													+ "', '"
													+ RDBHelper
															.getSQLReadyEntry(objectPropertyValue.additionalValue
																	.toUpperCase()) + "');")
													.getBytes(Charset
															.forName("UTF-8")));
										else
											output.write(("\nINSERT INTO "
													+ objectPropertyTableName
													+ "(ID,"
													+ forClass
															.getVariableName()
													+ "ID,"
													+ objectProperty.predicate
															.getTableAttributeName()
													+ ") VALUES("
													+ objectPropertyPrimaryKey
													+ ", "
													+ mainTableCounter
													+ ", '"
													+ RDBHelper
															.getSQLReadyEntry(objectPropertyValue.value) + "');")
													.getBytes(Charset
															.forName("UTF-8")));
									}

								}
							} else {
								if (objectProperty.predicate.hasValidRange()) {
									String objectPropertyTableName = objectProperty.predicate
											.getRangeTableName();
									for (RDFObjectPropertyValue objectPropertyValue : objectProperty.objects) {
										Integer foreignKey = uriPrimaryKeyLookup
												.get(objectPropertyValue.value
														.toLowerCase());
										if (foreignKey == null) {
											foreignKey = tableCounters
													.get(objectPropertyTableName) + 1;
											tableCounters.put(
													objectPropertyTableName,
													foreignKey);
											output.write(("\nINSERT INTO "
													+ objectPropertyTableName
													+ "(ID, uri, name) VALUES ("
													+ foreignKey
													+ ", '"
													+ RDBHelper
															.getSQLReadyEntry(objectPropertyValue.value)
													+ "', '"
													+ RDBHelper
															.getSQLReadyEntry(SPARQLHandler
																	.getLabelFromNode(
																			forClass.dataset,
																			objectPropertyValue.value,
																			"EN")) + "');")
													.getBytes(Charset
															.forName("UTF-8")));
											uriPrimaryKeyLookup.put(
													objectPropertyValue.value
															.toLowerCase(),
													foreignKey);
										}
										Integer objectPropertyPrimaryKey = tableCounters
												.get(objectPropertyTableName) + 1;
										tableCounters.put(
												objectPropertyTableName,
												objectPropertyPrimaryKey);
										output.write(("\nINSERT INTO "
												+ objectProperty.predicate
														.getTableName(forClass)
												+ "(ID,"
												+ forClass.getVariableName()
												+ "ID,"
												+ objectProperty.predicate
														.getTableAttributeName()
												+ ") VALUES("
												+ objectPropertyPrimaryKey
												+ ", " + mainTableCounter
												+ ", " + foreignKey + ");")
												.getBytes(Charset
														.forName("UTF-8")));

									}
								}
							}
						}
					}

				}
			} catch (Exception e) {
				System.out.println("Error happened for one object ... ");
			}

		}
		System.out.println("Finished RDB Conversion ...... ");
	}

}
