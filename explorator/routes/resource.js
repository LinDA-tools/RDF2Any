var express = require('express');
var router = express.Router();

module.exports = router;


/*
 * Handles redirects and content negotiation for resources
 */
router.get('/:resource', function(req, res, next) {	
	req.negotiate({
        	'text/html': function() {
            	res.redirect(303,'/page'+req.url+'.html');
        	},
	        'application/rdf+xml': function() {
				res.setHeader('content-type', 'application/rdf+xml');
	            res.redirect(303,'/data'+req.url);
	        },
			'text/turtle': function(){
				res.setHeader('content-type', 'text/turtle');
	            res.redirect(303,'/data'+req.url);
			},
			'application/trig': function(){
				res.setHeader('content-type', 'application/trig');
	            res.redirect(303,'/data'+req.url);
			},
			'application/n-triples': function(){
				res.setHeader('content-type', 'application/n-triples');
	            res.redirect(303,'/data'+req.url);
			},
			'application/n-quads': function(){
				res.setHeader('content-type', 'application/n-quads');
	            res.redirect(303,'/data'+req.url);
			},
			'text/plain': function(){
				res.setHeader('content-type', 'text/plain');
	            res.redirect(303,'/data'+req.url);
			},
			'application/ld+json': function(){
				res.setHeader('content-type', 'application/ld+json');
	            res.redirect(303,'/data'+req.url);
			}
	});
});