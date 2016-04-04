var express = require('express');
var router = express.Router();

module.exports = router;

router.get('/:resource', function(req, res, next) {	
	var sparql = req.db;
	_getNTriples(req.url.replace('/','').replace('.html',''), sparql, function(triples){
		res.render('page', {
 			triples: triples
			});
	});
});

_getNTriples = function(resource, endpoint, callback){
    endpoint.node(GLOBAL.namespace+resource, function(err, node){
		callback(node);
	})
};