<?php 
require_once( "sparqllib.php" );

function cleanString($str){
	$snip = str_replace("\n", ' ', $str); // remove new lines
	$snip = str_replace("\r", ' ', $snip); // remove carriage returns
	$snip = str_replace("\"", "\\\"", $snip); // remove new lines
	return $snip;
}

function uriParameter($datasetURI) {

	$uri = "http://localhost:3000/query/builder?";
	$dataset = "dataset=".$datasetURI;
	$classURI = "&classURI=";
	$classLabel = "&classLabel=";
	$optionals = "&optionals=";
	$filters = "&filters=";

	$db = sparql_connect( "http://localhost:8080/openrdf-sesame/repositories/QueryRepository" );
	if( !$db ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }
	sparql_ns( "rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
	sparql_ns( "cqo","http://example.com/cqo#" );
	sparql_ns( "prov","http://www.w3.org/ns/prov#" );
	sparql_ns( "sp","http://spinrdf.org/sp#" );

	//get classURI
	$_CURI = "";

	$class_sparql = "SELECT DISTINCT ?type WHERE {
		 ?query sp:where/rdf:rest*/rdf:first ?clauses .
		 ?clause sp:predicate rdf:type .
		 ?clause sp:object ?type .
	}";

	$result = sparql_query( $class_sparql ); 
	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	while( $row = sparql_fetch_array( $result ) )
	{	
		$_CURI .= $row['type'] ;
	}

	$classURI .= $_CURI;

	//get classLabel
	$classLabel .= substr($_CURI,strrpos($_CURI,"/") + 1);

	$labelsArray = getLabels($datasetURI,$_CURI);

	//get optionals
	$optionals_sparql = "SELECT DISTINCT ?optional_predicate WHERE
	{ ?op a sp:Optional .
	  ?op sp:elements/rdf:rest*/rdf:first ?optionals .
	  ?optionals sp:predicate ?optional_predicate .
	}";

	$result = sparql_query( $optionals_sparql ); 
	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	while( $row = sparql_fetch_array( $result ) )
	{	
		$optionals .= $row['optional_predicate'] . ";";
	}
	$optionals = trim($optionals, ";");

	//get filters
	$filters_sparql = "SELECT DISTINCT ?varName ?type ?value ?appliedOn where {
	  ?query sp:where/rdf:rest*/rdf:first ?clauses . 
	  ?clause sp:object ?clauseObject . 
	  ?clauseObject sp:varName ?varName .
	  ?clause sp:predicate ?appliedOn .
  
	  { ?filter a sp:ne .} 
	  UNION
	  { ?filter a sp:lt .}
	  UNION
	  { ?filter a sp:eq .}
	  UNION
	  { ?filter a sp:gt .}
	  UNION
	  { ?filter a sp:ge .}
	  UNION
	  { ?filter a sp:le .}
	
	  ?filter a ?type .
	  ?filter sp:arg1 ?lhsarg .
	  ?lhsarg sp:varName ?varName .
	  ?filter sp:arg2 ?value .
	}";

	//&filters=<appliedOn>;<label>;<type eg. ne>;labelV1;val1;labelV2;val2;val2*<appliedOn2>...
	$result = sparql_query( $filters_sparql ); 
	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	$firstTime = true;
	while( $row = sparql_fetch_array( $result ) )
	{
		$appliedOn =  $row['appliedOn'];
		if (!(strpos($filters,appliedOn) !== false)){
			//appliedOn not already added
	 		$appliedOn = $row['appliedOn'];

			if (firstTime) $filters .= $row['appliedOn'] . ";"; 
			else $filters .= "*".$row['appliedOn'] . ";"; 
			$filters .= $labelsArray[$appliedOn] . ";";
			$filters .= $row['type'] . ";"; 
		}
		$value =  cleanString($row['value']);
		if ($labelsArray[$value] == null) $filters .= value .";";
		else $filters .= $labelsArray[$value]  .";";
		$filters .= $value . ";"; 
	}
	$filters = trim($filters, ";");

	$uri .= $dataset.$classURI.$classLabel.$optionals.$filters;
	
	return $uri;
}



function getLabels($dataset,$class){
	$jsonurl = "http://localhost:8081/rdf2any/v1.0/builder/properties?dataset=".urlencode($dataset)."&class=".urlencode($class);
	$json = file_get_contents($jsonurl);
	$results = json_decode($json, true);

	$returnArray = array ();

	foreach($results as $obj){
		$uri = $obj['properties']['uri'];
		$label = $obj['properties']['label'];
		$returnArray[$label] = $uri;
	}

	return $returnArray;
}

function getClassLabel($dataset,$class){
	$jsonurl = "http://localhost:8081/rdf2any/v1.0/builder/classes?dataset=".urlencode($dataset)."&class=".urlencode($class);
	$json = file_get_contents($jsonurl);
	$results = json_decode($json, true);

	$label = "";

	foreach($results['searched_items'] as $obj){
		$label = $obj['labels']['en'];
	}

	return $label;
}

$db = sparql_connect( "http://localhost:8080/openrdf-sesame/repositories/QueryRepository" );
if( !$db ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }
sparql_ns( "rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
sparql_ns( "cqo","http://example.com/cqo#" );
sparql_ns( "prov","http://www.w3.org/ns/prov#" );
 	
$sparql = "SELECT DISTINCT ?transformation ?query ?queryString ?initialDataset ?formatinitial ?resultset ?formatresult ?execTime WHERE 
{ ?transformation rdf:type cqo:Transformation . 
?transformation cqo:hasQuery ?query . 
?query cqo:hasQueryString ?queryString .
?transformation cqo:executedOn ?initialDataset . 
?initialDataset cqo:hasSerialisation ?formatinitial .
?transformation cqo:resultsIn ?resultset . 
?resultset cqo:hasSerialisation ?formatresult .
?transformation cqo:executionTime ?execTime .
 }";

//?transformation prov:wasAssociatedWith ?agent . 

 
$result = sparql_query( $sparql ); 
if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }
 
$fields = sparql_field_array( $result );
 

$json = "{ \"items\" : [";
while( $row = sparql_fetch_array( $result ) )
{
	$item = "{";
	foreach( $fields as $field )
	{
		$item .= "\"" . cleanString($field) ."\" : \"" . cleanString($row[$field]) . "\",";
	}
	$item .= "\"uri\" : \"" .uriParameter($row['initialDataset']). "\"";
	$item .= "},";
	$json .= $item;
}
$json = trim($json, ",");
$json .= "] }";

header('Content-Type: application/json');
echo $json;
//print $json;
