package de.unibonn.iai.eis.linda.converters.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Joiner;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.servlet.Main;

import de.unibonn.iai.eis.linda.converters.Converter;
import de.unibonn.iai.eis.linda.helper.RDBHelper;
import de.unibonn.iai.eis.linda.helper.SPARQLHandler;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClass;
import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObject;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObjectProperty;
import de.unibonn.iai.eis.linda.querybuilder.objects.RDFObjectPropertyValue;

/**
 * @author gsingharoy
 * 
 *This converts ResultSet to SQL file
 **/

public class RDBConverter extends MainConverter implements Converter {
	private String tableName;

	public RDBConverter() {
		this.tableName = "things";
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
	
		String result = "INSERT INTO " + this.tableName + " VALUES("+ primaryKey.toString();
		for (int i = 0; i < resultVars.size(); i++) {
			result += ",'";
			RDFNode node = row.get(resultVars.get(i));
			if (node != null) result += RDBHelper.getSQLReadyEntry(node.toString()) + "'";
			else result += "'";
		}
		result += ");\n";
		return result;	
	}
	
	
	
	Map<String, Integer> tableCounters = new HashMap<String, Integer>();
	Map<String, RDFClass> varToClass = new HashMap<String, RDFClass>();
	Set<String> primary_foreign_Keys = new HashSet<String>();
	Map<String, RDFClassProperty> varToProperty = new HashMap<String, RDFClassProperty>();
	
	Map<String, Integer> countAttribs = new HashMap<String, Integer>(); //holds the number of attributes in the table
	
	private String generateFileHeader(Map<String,List<Object>> propTable) {
		List<RDFClass> rdfClasses = new ArrayList<RDFClass>();
		Map<RDFClass,String> filterProperties = new HashMap<RDFClass,String>();

		for (List<Object> obj : propTable.values()){
			for (Object val : obj){
				if (val instanceof RDFClass){
					RDFClass theClass = new RDFClass((RDFClass) val); //clone
					rdfClasses.add(theClass);
					filterProperties.put(theClass, "");
				}
			}
		}
		
		for (String var : propTable.keySet()){
			List<Object> objVal = propTable.get(var);
			for (Object value : objVal){
				if (!(value instanceof RDFClass)){
					String propURI = (String) value;
					for (RDFClass c : rdfClasses){
						RDFClassProperty _tmp = new RDFClassProperty(propURI, "", "");
						if (c.properties.contains(_tmp)){
							String s = filterProperties.get(c);
							s += s + propURI + ",";
							filterProperties.put(c, s);
							varToProperty.put(var,c.properties.get(c.properties.indexOf(_tmp)));
							
							if (c.properties.get(c.properties.indexOf(_tmp)).type.equalsIgnoreCase("object")){
								primary_foreign_Keys.add(var);
							}
						}
					}
				} else {
					primary_foreign_Keys.add(var);
					varToClass.put(var,(RDFClass)value);
				}
			}
		}
		
		for (RDFClass c : rdfClasses){
			c.filterProperties(filterProperties.get(c));
		}
		
		StringBuilder createTables = new StringBuilder();
		for (RDFClass c : rdfClasses){
			createTables.append(c.getTableCreationScript(true, new HashSet<String>(tableCounters.keySet())) + "\n\n");
			tableCounters.put(c.getTableName(), 0);
		}

		return createTables.toString();
	}

	public void convert(OutputStream output, ResultSet rdfResults) throws IOException {
		super.generateResultVars(rdfResults);
		Integer counter = 1;
		output.write(generateFileHeader().getBytes(Charset.forName("UTF-8")));
		while (rdfResults.hasNext()) {
			QuerySolution row = rdfResults.next();
			output.write(generateFileResultRow(row, counter).getBytes(Charset.forName("UTF-8")));
			counter++;
		}

	}

	// This convert implementation is when a class is queried from the querybuilder
	@Override
	public void convert(OutputStream output, ResultSet rdfResults, RDFClass forClass) throws IOException {
		Double startMilliseconds = (double) System.currentTimeMillis();
		
		//output.write((forClass.getTableCreationScript(true) + "\n\n").getBytes(Charset.forName("UTF-8")));
		
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
			System.out.println(mainTableCounter + ".##################################");
			try {
				output.write(("\n\n--"+mainTableCounter+". ########################################").getBytes(Charset.forName("UTF-8")));
				RDFNode object = row.get("concept");
				if (object != null) {
					RDFNode objectName = row.get("label");
					RDFObject rdfObject = null;
					if (objectName != null) {
						rdfObject = new RDFObject(forClass, object.toString(), SPARQLHandler.getLabelName(objectName));
					} else {
						rdfObject = new RDFObject(forClass, object.toString());
					}
					output.write(("\n" + rdfObject.getInsertRowScript(mainTableCounter)).getBytes(Charset.forName("UTF-8")));
					rdfObject.generateProperties();
					for (RDFObjectProperty objectProperty : rdfObject.properties) {
						if (!objectProperty.predicate.multiplePropertiesForSameNode) {
							// section for one object of the same property
							if (objectProperty.predicate.type.equals("data")) {
								if (objectProperty.predicate.getTableAttributeType().equals("int"))
									output.write(("\nUPDATE "+ mainTableName+ " SET "+ objectProperty.predicate.getTableAttributeName() + " = "
											+ RDBHelper.getSQLReadyEntry(objectProperty.objects.get(0).value)
											+ " WHERE id=" + mainTableCounter + ";").getBytes(Charset.forName("UTF-8")));
								else {
									if (objectProperty.predicate.range.isLanguageLiteral())
										output.write(("\nUPDATE "+ mainTableName+ " SET "+ objectProperty.predicate.getTableAttributeName()
												+ " = '"+ RDBHelper.getSQLReadyEntry(objectProperty.objects.get(0).value)
												+ "', "+ objectProperty.predicate.getTableAttributeName() + "Lang = '"
												+ objectProperty.objects.get(0).additionalValue.toUpperCase()
												+ "' WHERE id="+ mainTableCounter + ";").getBytes(Charset.forName("UTF-8")));
									else
										output.write(("\nUPDATE "+ mainTableName+ " SET "+ objectProperty.predicate.getTableAttributeName()
												+ " = '"+ RDBHelper.getSQLReadyEntry(objectProperty.objects.get(0).value)
												+ "' WHERE id="+ mainTableCounter + ";").getBytes(Charset.forName("UTF-8")));

								}
							} else {
								if (objectProperty.predicate.hasValidRange()) {
									Integer foreignKey = uriPrimaryKeyLookup.get(objectProperty.objects.get(0).value.toLowerCase());
									if (foreignKey == null) {
										foreignKey = tableCounters.get(objectProperty.predicate.getRangeTableName()+"@"+objectProperty.predicate.getRangeTableName()) + 1;
										tableCounters.put(objectProperty.predicate.getRangeTableName(),foreignKey);
										output.write(("\nINSERT INTO "+ objectProperty.predicate.getRangeTableName()
												+ "(id, uri , name) VALUES ("+ foreignKey+ ", '"+ RDBHelper.getSQLReadyEntry(objectProperty.objects.get(0).value)
												+ "', '"+ RDBHelper.getSQLReadyEntry(SPARQLHandler.getLabelFromNode(forClass.dataset,objectProperty.objects.get(0).value,"EN")) + "');")
												.getBytes(Charset.forName("UTF-8")));
										uriPrimaryKeyLookup.put(objectProperty.predicate.getRangeTableName()+"@"+objectProperty.objects.get(0).value.toLowerCase(),foreignKey);
									}
									output.write(("\nUPDATE "+ mainTableName+ " SET "+ objectProperty.predicate.getTableAttributeName()
											+ " = " + foreignKey + " WHERE id="+ mainTableCounter + ";").getBytes(Charset.forName("UTF-8")));
								}
							}
						} else {
							// section for multiple objects of the same property
							if (objectProperty.predicate.type.equals("data")) {
								String objectPropertyTableName = objectProperty.predicate.getTableName(forClass);
								for (RDFObjectPropertyValue objectPropertyValue : objectProperty.objects) {
									Integer objectPropertyPrimaryKey = tableCounters.get(objectPropertyTableName) + 1;
									tableCounters.put(objectPropertyTableName,objectPropertyPrimaryKey);
									
									if (objectProperty.predicate.getTableAttributeType().equals("int"))
										output.write(("\nINSERT INTO "+ objectPropertyTableName+ "(id,"+ forClass.getVariableName()+ "_id,"
												+ objectProperty.predicate.getTableAttributeName()+ ") VALUES("+ objectPropertyPrimaryKey
												+ ", "+ mainTableCounter+ ", "+ RDBHelper.getSQLReadyEntry(objectPropertyValue.value) + ");")
												.getBytes(Charset.forName("UTF-8")));
									else {
										if (objectProperty.predicate.range.isLanguageLiteral())
											output.write(("\nINSERT INTO "+ objectPropertyTableName+ "(id,"+ forClass.getVariableName()
													+ "_id,"+ objectProperty.predicate.getTableAttributeName()+ ", "+ objectProperty.predicate.getTableAttributeName()
													+ "Lang) VALUES("+ objectPropertyPrimaryKey+ ", "
													+ mainTableCounter+ ", '"+ RDBHelper.getSQLReadyEntry(objectPropertyValue.value)+ "', '"
													+ RDBHelper.getSQLReadyEntry(objectPropertyValue.additionalValue.toUpperCase()) + "');")
													.getBytes(Charset.forName("UTF-8")));
										else
											output.write(("\nINSERT INTO "+ objectPropertyTableName+ "(id,"+ forClass.getVariableName()
													+ "_id,"+ objectProperty.predicate.getTableAttributeName()+ ") VALUES("+ objectPropertyPrimaryKey
													+ ", "+ mainTableCounter+ ", '"+ RDBHelper.getSQLReadyEntry(objectPropertyValue.value) + "');")
													.getBytes(Charset.forName("UTF-8")));
									}

								}
							} else {
								if (objectProperty.predicate.hasValidRange()) {
									String objectPropertyTableName = objectProperty.predicate.getRangeTableName();
									for (RDFObjectPropertyValue objectPropertyValue : objectProperty.objects) {
										Integer foreignKey = uriPrimaryKeyLookup.get(objectProperty.predicate.getRangeTableName()+"@"+objectPropertyValue.value.toLowerCase());
										if (foreignKey == null) {
											foreignKey = tableCounters.get(objectPropertyTableName) + 1;
											tableCounters.put(objectPropertyTableName,foreignKey);
											output.write(("\nINSERT INTO "+ objectPropertyTableName+ "(id, uri, name) VALUES ("
													+ foreignKey+ ", '"+ RDBHelper.getSQLReadyEntry(objectPropertyValue.value)
													+ "', '"+ RDBHelper.getSQLReadyEntry(SPARQLHandler.getLabelFromNode(forClass.dataset,objectPropertyValue.value,"EN")) + "');")
													.getBytes(Charset.forName("UTF-8")));
											uriPrimaryKeyLookup.put(objectProperty.predicate.getRangeTableName()+"@"+objectPropertyValue.value.toLowerCase(),foreignKey);
										}
										Integer objectPropertyPrimaryKey = tableCounters.get(objectProperty.predicate.getTableName(forClass)) + 1;
										tableCounters.put(objectProperty.predicate.getTableName(forClass),objectPropertyPrimaryKey);
										output.write(("\nINSERT INTO "+ objectProperty.predicate.getTableName(forClass)
												+ "(id,"+ forClass.getVariableName()+ "_id,"+ objectProperty.predicate.getTableAttributeName()
												+ ") VALUES("+ objectPropertyPrimaryKey+ ", " + mainTableCounter+ ", " + foreignKey + ");")
												.getBytes(Charset.forName("UTF-8")));
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
		Double endMilliseconds = (double) System.currentTimeMillis();
		System.out.println("Time taken : "+((endMilliseconds - startMilliseconds) / 1000)+" seconds");
		System.out.println("Finished RDB Conversion ...... ");

	}

	@Override
	public void convert(OutputStream output, ResultSet rdfResults, Map<String,List<Object>> propTable) throws IOException {
		Double startMilliseconds = (double) System.currentTimeMillis();
		output.write((this.generateFileHeader(propTable)).getBytes(Charset.forName("UTF-8")));

		
		Map<String, Integer> uriPrimaryKeyLookup = new HashMap<String, Integer>();
		Integer keyCounter = 1;
		
		Integer mainTableCounter = 0;
		try{		
			StringBuilder out = new StringBuilder();
			while(rdfResults.hasNext()){
				mainTableCounter++;
				QuerySolution row = rdfResults.next();
				
				Map<String, RDFNode> mapRow = new HashMap<String,RDFNode>();
				
				for (String v : propTable.keySet()){
					RDFNode val = row.get(v);
					if (val != null)
						mapRow.put(v, val);
					else
						mapRow.put(v, null);
				}
				
				for (String v : propTable.keySet()){
					if (mapRow.get(v) == null) continue;
					
					String sb = "";
					String tableName = "";
					RDFClass theClass = null;
					
					if (varToClass.get(v) == null){
						theClass = Main.addOrGetCachedClass(varToProperty.get(v).range.uri, null);
					} else
						theClass = varToClass.get(v);
					
					tableName = theClass.getTableName();
					sb = "INSERT IGNORE INTO " + tableName + " ( %%rows%% ) VALUES ( %%values%% )";

					RDFNode val = mapRow.get(v);
					if (val != null){
						List<Object> lst_ = theClass.getPropertiesForRDB();
						countAttribs.put(tableName, lst_.size());

						for(Object o : lst_){
							if (o instanceof String){
								String _s = (String)o;
								sb = sb.replace("%%rows%%",_s+",%%rows%%");
								sb = sb.replace("%%values%%", "<"+_s+">,%%values%%");
							} else {
								String _p = ((RDFClassProperty)o).getTableAttributeName();
								sb = sb.replace("%%rows%%",_p+",%%rows%%");
								sb = sb.replace("%%values%%","<"+_p+">,%%values%%");
							}
						}
						sb = sb.replace(",%%values%%", "");
						sb = sb.replace(",%%rows%%", "");
						
						for(Object o : lst_){
							String rdbValue = "";
							String propVal = "";
							
							if (o instanceof String){
								if (o.equals("id")){
									if (uriPrimaryKeyLookup.containsKey(val.asResource().getURI())){
										rdbValue = uriPrimaryKeyLookup.get(val.asResource().getURI()).toString();
									} else {
										rdbValue = keyCounter.toString();
										uriPrimaryKeyLookup.put(val.asResource().getURI(),keyCounter);
										keyCounter++;
									}
									propVal = "<id>";
								} else if (o.equals("uri")){
									rdbValue = "'"+val.asResource().getURI().replace("'", "\'")+"'";
									propVal = "<uri>";
								}
							} else {
								RDFClassProperty prop = (RDFClassProperty)o;
								propVal = "<"+prop.getTableAttributeName()+">";
								
								//get other variables
								String theOtherVar = "";
								for (String _otherVar : propTable.keySet()){
									if (!(_otherVar.equals(v))){
										if (!(varToProperty.containsKey(_otherVar))) continue;
										if (varToProperty.get(_otherVar).uri.equals(prop.uri)){
											theOtherVar = _otherVar;
											break;
										}
	//									if (varToClass.get(_otherVar).uri.equals(prop.range.uri)){
	//										theOtherVar = _otherVar;
	//										break;
	//									}
									}
								}
								RDFNode theValue = mapRow.get(theOtherVar);
								if(primary_foreign_Keys.contains(theOtherVar)){
									if (theValue == null){
										rdbValue = "null";
									} else {
										if (uriPrimaryKeyLookup.containsKey(theValue.asResource().getURI())){
											rdbValue = uriPrimaryKeyLookup.get(theValue.asResource().getURI()).toString();
										} else {
											rdbValue = keyCounter.toString();
											uriPrimaryKeyLookup.put(theValue.asResource().getURI(),keyCounter);
											keyCounter++;
										}
									}
								} else {
									if (theValue == null){
										rdbValue = "null";
									} else {
										if (theValue.isURIResource()){
											rdbValue = "'"+theValue.toString().replace("'", "\'")+"'";
										} else if (theValue instanceof Literal){
											Literal l = theValue.asLiteral();
											if (l.getDatatype() != null){
												if (l.getDatatype().getJavaClass() == String.class){
													rdbValue = RDBHelper.getSQLReadyEntry("'"+theValue.toString()+"'");
												} else{
													rdbValue = theValue.toString().replace("'", "\'");
												}
											} else {
												rdbValue = RDBHelper.getSQLReadyEntry("'"+theValue.toString()+"'");
											}
										}
									}
								}
							}
							sb = sb.replace(propVal, rdbValue);
						}
					}
					out.append(sb.toString()+";");
					out.append(System.lineSeparator());
				}			
			}
			
			Set<String> s = new HashSet<String>(Arrays.asList(out.toString().split(System.lineSeparator())));
			List<String> sortedList = new ArrayList<String>(s);
			Collections.sort(sortedList);
			
			
			ValueComparator vc = new ValueComparator(countAttribs);
			Map<String, Integer> treeMap = new TreeMap<String, Integer>(vc);
			treeMap.putAll(countAttribs);
			
			List<String> orderedList = new ArrayList<String>();
			
			for(String k : treeMap.keySet()){
				for (String _s : sortedList){
					if (_s.startsWith("INSERT IGNORE INTO " + k)){
						orderedList.add(_s);
					}
				}
			}
			
			String _output = "";
			for (String _out : orderedList){
				_output += _out;
				_output += System.lineSeparator();
			}
			
			output.write(_output.getBytes(Charset.forName("UTF-8")));
		}catch (Exception e){
			e.printStackTrace();
		}
		
		Double endMilliseconds = (double) System.currentTimeMillis();
		System.out.println("Time taken : "+((endMilliseconds - startMilliseconds) / 1000)+" seconds");
		System.out.println("Finished RDB Conversion ...... ");
		
		tableCounters.clear();
	}
}

class ValueComparator implements Comparator<String>{
	 
	HashMap<String, Integer> map = new HashMap<String, Integer>();
 
	public ValueComparator(Map<String, Integer> countAttribs){
		this.map.putAll(countAttribs);
	}
 
	@Override
	public int compare(String o1, String o2) {
		if(map.get(o1) <= map.get(o2)){
			return -1;
		}else{
			return 1;
		}	
	}
}
