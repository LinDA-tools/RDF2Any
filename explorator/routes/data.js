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

							opt = getOptional(parsedQuery, dset, classesAndFilters['classes']);
							if (opt['optionals'].length > 0){
								title = title + " " + opt['labels'];
							}

							filt = getFilters(parsedQuery, dset, classesAndFilters['classes'], classesAndFilters['filters']);
							theFilters = filt['filters'];
							if (theFilters['isEmpty'] == false){
								title += " " + filt['labels'];
							}
							transformation['title'] = title;
							transformation['uri'] = buildUri(dset,classesAndFilters['classes'],opt['optionals'],filt['filters']);
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
	for (var key in classes) {
		result = rdf2AnyQuery("classes/label",{dataset: dset, class: classes[key] });
		classLabel += JSON.parse(result)['label'] + ","
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
	filters = []
	for(index in parsedQuery["where"]){
		triplesBlock = parsedQuery["where"][index]["triples"];
		for (triple in triplesBlock){
			predicate = triplesBlock[triple]["predicate"];
			object = triplesBlock[triple]["object"];
			subject = triplesBlock[triple]["subject"];

			if (predicate == "http://www.w3.org/1999/02/22-rdf-syntax-ns#type")
				classes[subject] = object;
			else {
				filters[object] = {p: predicate, s: subject}
			}
		}
	}
	return {classes: classes, filters: filters};
}

function getOptional(parsedQuery, datasetURI, classes){
	optional = "SHOWING [%optionals%]";

	optionalsLabel = "";
	propertiesOptional = [];
	for(index in parsedQuery["where"]){
		whereType = parsedQuery["where"][index]['type'];
		if (whereType == "optional"){
			for (pattern in parsedQuery["where"][index]["patterns"]){
				triplesBlock = parsedQuery["where"][index]["patterns"][pattern];
				for (idx in triplesBlock['triples']){
					predicate = triplesBlock['triples'][idx]["predicate"];
					subject = triplesBlock['triples'][idx]["subject"];

					result = rdf2AnyQuery("properties/label",{dataset: dset, class: classes[subject], property:predicate});
					optionalsLabel += JSON.parse(result)['label'] + ", ";
					propertiesOptional.push(predicate);
				}
			}
		}
	}
	optionalsLabel = optionalsLabel.substring(0,optionalsLabel.lastIndexOf(","))


	return {optionals: propertiesOptional, labels: optional.replace("[%optionals%]",optionalsLabel)}
}


function getFilters(parsedQuery, datasetURI, classes, filters){
	filterString = "WITH FILTERS ON [%filters%]";

	filtersHM = [];

	for(index in parsedQuery["where"]){
		whereType = parsedQuery["where"][index]['type'];
		if (whereType == "filter"){
			expression = parsedQuery["where"][index]['expression'];
			if (expression['args']['type'] == "operation"){ //temp TODO fix
				exps = expression['args']
				for (idx in exps){
					appliedOn = filters[exps[idx]['args'][0]]['p'];
					if (!(appliedOn in filtersHM)){
						filtersHM[appliedOn] = {}
						filtersHM[appliedOn]['appliedOnLabel'] = JSON.parse(rdf2AnyQuery("properties/label",{dataset: dset, class: classes[filters[exps[idx]['args'][0]]['s']], property:appliedOn }))['label'];
						filtersHM[appliedOn]['expressions'] = []
					}
					exp = {};
					exp['lhs'] = exps[idx]['args'][0];
					exp['rhs'] = exps[idx]['args'][1];
					exp['op'] = exps[idx]['operator'];
					filtersHM[appliedOn]['expressions'].push(exp);
				}
			} else {
				appliedOn = filters[expression['args'][0]]['p'];
				if (!(appliedOn in filtersHM)){
					filtersHM[appliedOn] = {}
					filtersHM[appliedOn]['appliedOnLabel'] = JSON.parse(rdf2AnyQuery("properties/label",{dataset: dset, class: classes[filters[expression['args'][0]]['s']], property:appliedOn }))['label'];
					filtersHM[appliedOn]['expressions'] = []
				}
				exp = {};
				exp['lhs'] = expression['args'][0];
				exp['rhs'] = expression['args'][1];
				exp['op'] = expression['operator'];
				filtersHM[appliedOn]['expressions'].push(exp);
			}
		}
	}

	//get object type filters
	for (key in filters){
		if (key.indexOf("http://") === 0){
			appliedOn = filters[key]['p'];
			if (!(appliedOn in filtersHM)){
				filtersHM[appliedOn] = {}
				filtersHM[appliedOn]['appliedOnLabel'] = JSON.parse(rdf2AnyQuery("properties/label",{dataset: dset, class: classes[filters[key]['s']], property:appliedOn }))['label'];
				filtersHM[appliedOn]['expressions'] = []
			}

			exp = {};
			exp['op'] = "="; //unless its a minus TODO FIX
			exp['rhs'] = key;
			filtersHM[appliedOn]['expressions'].push(exp);
		}
	}

	//write label
	filterLabel = "";
	for (key in filtersHM){
		filterLabel += filtersHM[key]['appliedOnLabel'] + ",";
	}

	if (filterLabel != ""){
		filtersHM['isEmpty'] = false;
	}

	filterLabel = filterLabel.substring(0,filterLabel.lastIndexOf(","));

	theLbl = filterString.replace("[%filters%]",filterLabel);
	return {filters: filtersHM, labels: theLbl};
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
	link += "&query="+query;
	link += "&generatedOntology=false";
}


function buildUri(endpoint,classes,optional,filters){
	link = "http://localhost:3000/query/builder?";

	link += "dataset="+endpoint;
	link += "&classURI="
	for (key in classes){
		link += classes[key]+";"
	}
	link = link.substring(0,link.lastIndexOf(";"));

	link += "&classLabel="
	for (key in classes){
		link +=  JSON.parse(rdf2AnyQuery("classes/label",{dataset: dset, class: classes[key]}))['label'] + ";"
	}

	link = link.substring(0,link.lastIndexOf(";"));

	link += "&optionals="
	if (optional.length > 0){
		for (var i = 0; i < optional.length; i++){
			link += optional[i]+";"
		}
		link = link.substring(0,link.lastIndexOf(";"));
	}




	link += "&filters="
	if (filters['isEmpty'] == false){
		for (key in filters){
			filtLink = "";
			theFilter = filters[key];
			filtLink += key + ";";
			filtLink += theFilter['appliedOnLabel'] + ";";


			expr = theFilter['expressions'];
			for (e in expr){
				_expr = expr[e];
				op = _expr['op'];
				if (op == "=") filtLink += "sp:eq;";
				if (op == "!=") filtLink += "sp:ne;";
				if (op == "<") filtLink += "sp:lt;";
				if (op == ">") filtLink += "sp:gt;";
				if (op == "<=") filtLink += "sp:le;";
				if (op == ">=") filtLink += "sp:ge;";

				rhsValue = _expr['rhs'];
				rhsLabel = rhsValue;
				if (rhsValue.indexOf("http://") === 0){
					//its a resource
					rhsLabel = rhsLabel.substring(rhsLabel.lastIndexOf("/"), rhsLabel.length);
				}

				filtLink += rhsLabel + ";"
				filtLink += rhsValue + ";"
			}
			filtLink = filtLink.substring(0,filtLink.lastIndexOf(";"));
			link += filtLink+"*"
		}
		link = link.substring(0,link.lastIndexOf("*isEmpty;undefined"));
	}

	return link;
}
