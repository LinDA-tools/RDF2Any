package de.unibonn.iai.eis.linda.querybuilder.objects;

import java.util.List;

import de.unibonn.iai.eis.linda.querybuilder.classes.RDFClassProperty;

/**
 * @author gsingharoy
 *
 *This class will contain pairs of predicates and objects
 **/
public class RDFObjectProperty {
	public RDFClassProperty predicate;
	public List<String> objects;
}
