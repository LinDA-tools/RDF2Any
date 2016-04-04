var express = require('express');
var router = express.Router();
var js2xmlparser = require("js2xmlparser");
var negotiate = require('express-negotiate');
var SparqlParser = require('sparqljs').Parser;

var rdfstore = require('rdfstore');

module.exports = router;

router.get('/', function(req, res, next) {
	queryString = req.query;

	console.log('SPARQL query request: '+queryString['query']);

	//query
	if(queryString['query'] != null){

		var parser = new SparqlParser();
		valid = true;
		error = "";
		try{
			var parsedQuery = parser.parse(queryString['query']);
		} catch (e){
			error = e;
			valid = false;
		}

		if (valid){
			_query(queryString['query'], req.db, function(results){
					req.negotiate({
				        'application/sparql-results+json': function() {
							res.setHeader('content-type', 'application/sparql-results+json');
							var sparql = req.db;
							_JSONStringResult(results, function(JSONresult){
								res.send(JSONresult);
							});
				        },
						'application/sparql-results+xml' : function() {
							res.setHeader('content-type', 'application/sparql-results+xml');
							var sparql = req.db;
							_XMLStringResult(results, function(XMLresult){
								res.send(XMLresult);
							});
			        	},
						'text/html' : function() {
							var sparql = req.db;
							_JSONStringResult(results, function(JSONresult){
								results = JSONresult;
								tblHeader = results['head']['vars'];
								objs = [];
								resObj = results['results']['bindings'];
								resObj.forEach(function(obj){
									row = {}
									for(var key in obj){
										row[key] = obj[key]['value'];
									}
									objs.push(row);
								});
								res.render('sparql', {displayResults: true, tblHeader: tblHeader, tblBody: objs , error:false });
							});
			        	}
					});
			});
		} else {
			req.negotiate({
				'application/sparql-results+json': function() {
						res.send(err);
				},
				'application/sparql-results+xml': function() {
						res.send(err);
				},
				'text/html' : function() {
						res.render('sparql', {displayResults: false, errorMsg: error, error: true  });
				}
			});
		}

	} else {
		req.negotiate({
			'application/sparql-results+json': function() {
					res.status(404).send('missing query parameter ... ');
        	},
			'application/sparql-results+xml': function() {
					res.status(404).send('missing query parameter ... ');
        	},
			'text/html' : function() {
					res.render('sparql', {displayResults: false});
			}
		});
	}

});



_query = function(query, endpoint, callback){
	endpoint.execute(query, function(err, results){
		callback(results);
	});
};



_JSONStringResult = function(results, callback){
	var JSONStringResult = JSON.stringify(results);
	JSONStringResult = JSONStringResult.replace(/"type":/g, '"datatype":');
	JSONStringResult = JSONStringResult.replace(/"token":/g, '"type":');
	JSONStringResult = JSONStringResult.replace(/"lang":/g, '"xml:lang":');
	JSONStringResult = JSONStringResult.replace(/"blank"/g, '"bnode"');


	//get vars
	keys = {};
    for(var r in results){
		for(var key in results[r]){
			keys['"' + key + '"'] = true;
		}
    }

	keyList = Object.keys(keys);


	//Add head
	headString = "\"head\" : { \"vars\": ["+ keyList +"] },";
	JSONStringResult = "\"results\" : { \"bindings\": " + JSONStringResult +"} ";

	finalString = "{" + headString + JSONStringResult + "}";

	callback(JSON.parse(finalString));
};




// _XMLStringResult = function(results,callback){
// 	_JSONStringResult(results, function(jsonResults){
// 		xml = js2xmlparser("sparql",JSON.stringify(jsonResults));
// 		console.log(xml);
// 		callback(xml);
// 	})
// }

//application/sparql-results+xml srx
//application/sparql-results+json srj
