var express = require('express');
var router = express.Router();
var http = require('http');
var request = require('request');
var syncrequest = require('sync-request');


var SparqlClient = require('sparql-client');
var SparqlParser = require('sparqljs').Parser;


module.exports = router;


/*
 * Retrieves all data from the endpoint
 */
router.get('/retrieve/exhibit/all', function(req, res, next) {
		var endpoint = req.db;

		var client = new SparqlClient(endpoint);
		//
		var selectAll = "SELECT DISTINCT ?transformation ?query ?queryString ?initialDataset ?formatinitial ?resultset ?formatresult ?execTime WHERE { ?transformation a <http://example.com/cqo#Transformation> . ?transformation <http://example.com/cqo#hasQuery> ?query . ?query <http://example.com/cqo#hasQueryString> ?queryString .?transformation <http://example.com/cqo#executedOn> ?initialDataset . ?initialDataset <http://example.com/cqo#hasSerialisation> ?formatinitial . ?transformation <http://example.com/cqo#resultsIn> ?resultset . ?resultset <http://example.com/cqo#hasSerialisation> ?formatresult . ?transformation <http://example.com/cqo#executionTime> ?execTime . }";

		_endPointQuery(endpoint, selectAll, function(error, results) {
					if (error != null){
						console.log(error);
					}
					else {
						json = {};
						items = [];

						for(binding in results['results']['bindings']){
							transformation = {};
							transformation['transformation'] = results['results']['bindings'][binding]['transformation']['value'];
							transformation['query'] = results['results']['bindings'][binding]['query']['value'];
							transformation['queryString'] = results['results']['bindings'][binding]['queryString']['value'].replace(/</g,"&lt;").replace(/>/g,"&gt;");
							transformation['initialDataset'] = results['results']['bindings'][binding]['initialDataset']['value'];
							transformation['formatinitial'] = results['results']['bindings'][binding]['formatinitial']['value'];
							transformation['resultset'] = results['results']['bindings'][binding]['resultset']['value'];
							transformation['formatresult'] = results['results']['bindings'][binding]['formatresult']['value'];
							transformation['execTime'] = results['results']['bindings'][binding]['execTime']['value'];


							queryString = results['results']['bindings'][binding]['queryString']['value'];
							dset = results['results']['bindings'][binding]['initialDataset']['value'];

							var SparqlParser = require('sparqljs').Parser;
							var parser = new SparqlParser();
							var parsedQuery = parser.parse(queryString);

							classesAndFilters = getQueryClassesAndFilters(parsedQuery);


							transformation['classes'] = getClasses(classesAndFilters['classes']);
						 	title = createTitle(parsedQuery,dset,classesAndFilters['classes']) ;

							opt = getOptional(parsedQuery, dset, classesAndFilters);
							if (opt['optionals'].length > 0){
								title = title + " " + opt['labels'];
							}

							classesAndFilters = opt['optionals'];

							filt = getFilters(parsedQuery, dset, classesAndFilters);
							theFilters = filt['filters'];

							classesAndFilters['filters'] = theFilters;

							if (theFilters['isEmpty'] == false){
								title += " " + filt['labels'];
							}


							transformation['title'] = title;
							transformation['uri'] = buildUri(dset, classesAndFilters);
							 //buildUri(dset,classesAndFilters['classes'],opt['optionals'],filt['filters'], classesAndFilters['otherProps']);
							transformation['download'] = createDownloadLink(dset,transformation['queryString'],transformation['formatresult']);

							items.push(transformation)
						}
						json["items"] = items
						res.send(json);
					}
		});
});


function createTitle(parsedQuery, datasetURI, classes){
	title = "Get All <strong>[%%classes%%]</strong>";

	classLabel =  "";

	for (var idx in classes) {
		classLabel += classes[idx]['name'] + ","
	}
	classLabel = classLabel.substring(0,classLabel.lastIndexOf(","))

	title = title.replace("[%%classes%%]",classLabel);


	return (title.replace("[%%classes%%]",classLabel));
}

function getClasses(classes){
	clss = [];
	for (key in classes){
		clss.push(classes[key]);
	}
	return clss;
}

function getQueryClassesAndFilters(parsedQuery){
	classes = [];
	filters = [];
	otherProps = [];

	for(index in parsedQuery["where"]){
		whereType = parsedQuery["where"][index]['type'];
		triplesBlock = parsedQuery["where"][index]["triples"];
		for (triple in triplesBlock){
			predicate = triplesBlock[triple]["predicate"];
			object = triplesBlock[triple]["object"];
			subject = triplesBlock[triple]["subject"];

			if (predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type"){
				result = rdf2AnyQuery("classes/label",{dataset: dset, class: object });
				title = JSON.parse(result)['label']
				classes.push({s: subject, o:object, name:title});
			}
			else {
				if (whereType ==  'bgp'){
					otherProps.push({s: subject, p: predicate, o:object})
					//otherProps.push(predicate)
				} else {
					filters[object] = {p: predicate, s: subject}
				}
			}
		}

		if (whereType ==  'filter') {
		 filters[object] = {p: predicate, s: subject}
	  }
	}

	return {classes: classes, filters: filters, otherProps: otherProps};
}


function getOptional(parsedQuery, datasetURI, classesAndFilters){
	optional = "SHOWING [%optionals%]";

	var classes = classesAndFilters['classes'];
	classesAndFilters['optionals'] = [];

	optionalsLabel = "";
	for(index in parsedQuery["where"]){
		whereType = parsedQuery["where"][index]['type'];
		if (whereType == "optional"){
			propertiesOptional = {};
			for (pattern in parsedQuery["where"][index]["patterns"]){
				triplesBlock = parsedQuery["where"][index]["patterns"][pattern];
				for (idx in triplesBlock['triples']){
					predicate = triplesBlock['triples'][idx]["predicate"];
					subject = triplesBlock['triples'][idx]["subject"];
					class_ = 	""
					for (var idx in classes) {
						if (classes[idx]['s'] == subject) class_ = classes[idx]['o'];
					}

					result = rdf2AnyQuery("properties/label",{dataset: dset, class: class_, property:predicate});
					optionalsLabel += JSON.parse(result)['label'] + ", ";

					classesAndFilters['optionals'].push(predicate);
				}
			}
		}
	}
	optionalsLabel = optionalsLabel.substring(0,optionalsLabel.lastIndexOf(","))


	return {optionals: classesAndFilters, labels: optional.replace("[%optionals%]",optionalsLabel)}
}


function getFilters(parsedQuery, datasetURI, classesAndFilters){
	
	console.log("parsed query: " + JSON.stringify(parsedQuery))
	
	filterString = "WITH FILTERS ON [%filters%]";
// classes: [{"s":"?_actor","o":"http://dbpedia.org/ontology/Actor","name":"Actor"}]
// otherProps":[{"s":"?_actor","p":"http://dbpedia.org/ontology/Person/height","o":"?height_cm_"}]
	classes = classesAndFilters['classes'];
	otherProps = classesAndFilters['otherProps'];
	filters = [];
	filterLabel = "";

	for(index in parsedQuery["where"]){
		whereType = parsedQuery["where"][index]['type'];
		if (whereType == "filter"){
			expression = parsedQuery["where"][index]['expression'];
			if (expression['type'] == "operation"){
				args = expression['args'];
				filter = {};
				filter['args'] = [];
				
				console.log("args: " + JSON.stringify(args));
				
				if (args[0]["args"]!= null){
					
					if (args.length > 1) filter['args'].push(expression['operator']);

					for (idx in args){
						arg = {}
						arg['op'] = args[idx]['operator'];
						arg['lhs'] = args[idx]['args'][0];
						arg['rhs'] = args[idx]['args'][1];
						filter['args'].push(arg);

						theProperty = "";
						theVariable = "";
						for(p in otherProps){
							if (otherProps[p]['o'] == arg['lhs']){
								theProperty = otherProps[p]['p'];
								theVariable = otherProps[p]['s'];
								break;
							}
						}

						theClass = "";
						for (c in classes){
							if (classes[c]['s'] == theVariable){
								theClass = classes[c]['o'];
								break;
							}
						}

						theLabel = JSON.parse(rdf2AnyQuery("properties/label",{dataset: dset, class: theClass, property: theProperty }))['label'];
						filterLabel += theLabel + ", "
					}
				
			}//end null args
			else{
				arg = {}
				arg['op'] = expression['operator'];
				arg['lhs'] = args[0];
				arg['rhs'] = args[1];
				filter['args'].push(arg);

				theProperty = "";
				theVariable = "";
				for(p in otherProps){
					if (otherProps[p]['o'] == arg['lhs']){
						theProperty = otherProps[p]['p'];
						theVariable = otherProps[p]['s'];
						break;
					}
				}

				theClass = "";
				for (c in classes){
					if (classes[c]['s'] == theVariable){
						theClass = classes[c]['o'];
						break;
					}
				}
				
				theLabel = JSON.parse(rdf2AnyQuery("properties/label",{dataset: dset, class: theClass, property: theProperty }))['label'];
				filterLabel += theLabel + ", "
			};
			filters.push(filter);
			}

		}
	}

	if (filterLabel != ""){
		filters['isEmpty'] = false;
	}

	filterLabel = filterLabel.substring(0,filterLabel.lastIndexOf(","));

	theLbl = filterString.replace("[%filters%]",filterLabel);
	return {filters: filters, labels: theLbl};
}


_endPointQuery = function(endpoint, _query, callback){
	request({
			url: endpoint,
			qs: {query: _query},
			method: 'GET',
			headers: {
					'Content-Type': 'application/x-www-form-urlencoded',
					'Accept': 'application/sparql-results+json'
			}
	}, function(error, response, body){
			if(error) {
					callback(error, null);
			} else {
					callback(null,JSON.parse(body));
			}
	});
}

function rdf2AnyQuery(path, params){
	var res = syncrequest('GET',"http://localhost:8081/rdf2any/v1.0/builder/"+path, { 'qs': params });
	return (res.getBody());
}




function createDownloadLink(endpoint,query,format){
	link = "http://localhost:8081/rdf2any/v1.0/convert/"

	if (format == "CSV") link += "csv-converter.csv?";
	if (format == "RDB") link += "rdb-converter.sql?";
	if (format == "RDF") link += "rdf-converter.ttl?";
	if (format == "json") link += "json?";

	link += "dataset="+endpoint;
	link += "&query="+encodeURIComponent(query);
	link += "&generatedOntology=false";

	return link
}


function buildUri(endpoint, classesAndFilters){
	link = "http://localhost:3000/query/builder?";
	link += "dataset="+endpoint;
	link += "&query="+encodeURIComponent(JSON.stringify(classesAndFilters));
	link += "&propertylabels="+encodeURIComponent(JSON.stringify(propertyLabels(endpoint, classesAndFilters)));

	return link;
}

function propertyLabels(dset, classesAndFilters){
	var res = {};
	var opts = classesAndFilters['otherProps']
	var classes = classesAndFilters['classes']
	for(var idx in opts){
		var predicate = opts[idx]['p'];
		var theClassVar = opts[idx]['s'];
		var theClass = "";
		for (var i in classes){
			var curr = classes[i]['s'];
			if (curr == theClassVar){
				theClass = classes[i]['o'];
				break;
			}
		}

		result = rdf2AnyQuery("properties/label",{dataset: dset, class: theClass, property:predicate});
		theLabel = JSON.parse(result)['label'];
		res[predicate] = theLabel;
	}

	return res;
}
// function buildUri(endpoint,classes,optional,filters, otherProps){
// 	link = "http://localhost:3000/query/builder?";
//
// 	link += "dataset="+endpoint;
// 	link += "&classURI="
// 	for (key in classes){
// 		link += classes[key]+";"
// 	}
// 	link = link.substring(0,link.lastIndexOf(";"));
//
// 	link += "&classLabel="
// 	for (key in classes){
// 		link +=  JSON.parse(rdf2AnyQuery("classes/label",{dataset: dset, class: classes[key]}))['label'] + ";"
// 	}
//
// 	link = link.substring(0,link.lastIndexOf(";"));
//
// 	link += "&optionals="
// 	if (optional.length > 0){
// 		for (var i = 0; i < optional.length; i++){
// 			link += optional[i]+";"
// 		}
// 		link = link.substring(0,link.lastIndexOf(";"));
// 	}
//
// 	link += "&filters="
// 	if (filters['isEmpty'] == false){
// 		for (key in filters){
// 			filtLink = "";
// 			theFilter = filters[key];
// 			filtLink += key.replace("^^http://www.w3.org/2001/XMLSchema#integer",'') + ";";
// 			filtLink += theFilter['appliedOnLabel'] + ";";
//
//
// 			expr = theFilter['expressions'];
// 			for (e in expr){
// 				_expr = expr[e];
// 				op = _expr['op'];
// 				if (op == "=") filtLink += "sp:eq;";
// 				if (op == "!=") filtLink += "sp:ne;";
// 				if (op == "<") filtLink += "sp:lt;";
// 				if (op == ">") filtLink += "sp:gt;";
// 				if (op == "<=") filtLink += "sp:le;";
// 				if (op == ">=") filtLink += "sp:ge;";
//
// 				rhsValue = _expr['rhs'];
// 				rhsLabel = rhsValue;
// 				if (rhsValue.indexOf("http://") === 0){
// 					//its a resource
// 					rhsLabel = rhsLabel.substring(rhsLabel.lastIndexOf("/"), rhsLabel.length);
// 				}
//
// 				filtLink += rhsLabel.replace("^^http://www.w3.org/2001/XMLSchema#integer",'') + ";"
// 				filtLink += rhsValue.replace("^^http://www.w3.org/2001/XMLSchema#integer",'') + ";"
// 			}
// 			filtLink = filtLink.substring(0,filtLink.lastIndexOf(";"));
// 			link += filtLink+"*"
// 		}
// 		link = link.substring(0,link.lastIndexOf("*isEmpty;undefined"));
// 	}
//
// 	link += "&otherProps=" + otherProps.join(';');
// 	link = link.replace("^^http://www.w3.org/2001/XMLSchema#integer",'');
// 	return link;
// }
