<?php 
require_once( "sparqllib.php" );

// ini_set('display_errors', 'On');
// error_reporting(E_ALL);
	
function cleanString($str){
	$snip = str_replace("\n", ' ', $str); // remove new lines
	$snip = str_replace("\r", ' ', $snip); // remove carriage returns
	$snip = str_replace("\"", "\\\"", $snip); // remove new lines
	$snip = str_replace("<", "&lt;", $snip); //replace <
	$snip = str_replace(">", "&gt;", $snip); //replace >
	return $snip;
}

$titlePattern = "Get All <strong>[%classname%]</strong> [%showing%] [%where%]";
$showingPattern = "Showing [%optionals%]";
$wherePattern = "Where [%filters%]";

$tempTitle = "";

function uriParameter($datasetURI, $queryURI) {
	$uri = "http://localhost:3000/query/builder?";
	$dataset = "dataset=".$datasetURI;
	$classURI = "&classURI=";
	$classLabel = "&classLabel=";
	$optionals = "&optionals=";
	$filters = "&filters=";
	
	global $titlePattern;
	global $wherePattern;
	global $tempTitle;
	global $showingPattern;

	$db = sparql_connect( "http://localhost:8080/openrdf-sesame/repositories/QueryRepository" );
	if( !$db ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }
	sparql_ns( "rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#" );
	sparql_ns( "cqo","http://example.com/cqo#" );
	sparql_ns( "prov","http://www.w3.org/ns/prov#" );
	sparql_ns( "sp","http://spinrdf.org/sp#" );

	//get classURI
	$_CURI = "";

	$class_sparql = "SELECT DISTINCT ?type WHERE {
		 <%%queryURI%%> sp:where/rdf:rest*/rdf:first ?clause .
		 ?clause sp:predicate rdf:type .
		 ?clause sp:object ?type .
	}";
	$class_sparql = str_replace("%%queryURI%%", $queryURI, $class_sparql);

	$result = sparql_query( $class_sparql ); 
	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	while( $row = sparql_fetch_array( $result ) )
	{	
		$_CURI .= $row['type'] ;
	}

	$classURI .= $_CURI;

	//get classLabel
	$_cl = substr($_CURI,strrpos($_CURI,"/") + 1);
	$classLabel .= substr($_CURI,strrpos($_CURI,"/") + 1);
	$tempTitle .= $titlePattern;
	$tempTitle = str_replace("[%classname%]", $_cl, $tempTitle);

	$labelsArray = getLabels($datasetURI,$_CURI);

	//get optionals
	$optionals_sparql = "SELECT DISTINCT ?optional_predicate WHERE
	{ <%%queryURI%%> sp:where/rdf:rest*/rdf:first ?op .
		?op a sp:Optional .
	  ?op sp:elements/rdf:rest*/rdf:first ?optionals .
	  ?optionals sp:predicate ?optional_predicate .
	}";
	$optionals_sparql = str_replace("%%queryURI%%", $queryURI, $optionals_sparql);

	$result = sparql_query( $optionals_sparql ); 
	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	$tempShowing = "";
	$tempShowing .= $showingPattern;
	$_op = "";
	while( $row = sparql_fetch_array( $result ) )
	{	
		$optionals .= $row['optional_predicate'] . ";";	
		$_op .= $labelsArray[$row['optional_predicate']].", ";
	}
	
	$optionals = trim($optionals, ";");
  $_op =  trim($_op, ", ");
	if ($_op != "") $tempShowing = str_replace("[%optionals%]", $_op, $tempShowing) ;
	
	$filters_sparql = "SELECT DISTINCT * where {
		 <%%queryURI%%> sp:where ?where .
 	   ?where rdf:rest*/rdf:first ?clauses .
   	 ?clauses rdf:type sp:Filter .
 	   ?clauses sp:expression ?expression .
 	   ?expression ?arg ?argument .
 	   FILTER ( ?arg != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ) .
  
     ?argument (sp:arg1|sp:arg2)* ?filters .
   	 {?filters a sp:eq} UNION {?filters a sp:gt} UNION
   	 {?filters a sp:ne} UNION {?filters a sp:ge} UNION
   	 {?filters a sp:lt} UNION {?filters a sp:le} 
      
   	 ?filters a ?type .
 	   ?filters sp:arg1 ?lhsarg .
 	   ?lhsarg sp:varName ?varName .
 	   ?filters sp:arg2 ?value 
	}";
	
	$filters_sparql = str_replace("%%queryURI%%", $queryURI, $filters_sparql);

	//&filters=<appliedOn>;<label>;<type eg. ne>;labelV1;val1;labelV2;val2;val2*<appliedOn2>...
	$result = sparql_query( $filters_sparql ); 

	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	$firstTime = true;
	$tempWhere = "";
	$tempWhere .= $wherePattern;

	while( $row = sparql_fetch_array( $result ) )
	{
		$tempWhere = str_replace("[%filters%]", "", $tempWhere); // this means that we have some filters
		$_appOnTitle = "";
		$_equalityTitle = "";
		
		$appliedOn =  getAppliedOn($queryURI,$row['varName']);

		if (!(strpos($filters,$appliedOn) !== false)){
			if ($firstTime){
				 $filters .= getAppliedOn($queryURI,$row['varName']) . ";"; 
				 $filters .= $labelsArray[$appliedOn] . ";";
				 $firstTime = false;
			 }
			else { 
				$filters = trim($filters, ";");
				$filters .= "$".getAppliedOn($queryURI,$row['varName']). ";"; 
			  $filters .= $labelsArray[$appliedOn] . ";";
			}
			
			$_appOnTitle = $labelsArray[$appliedOn];
			 
			$type = $row['type'];
			$type = str_replace("http://spinrdf.org/","",$type);
			$type = str_replace("#",":",$type);
			
			if ($type == "sp:eq") $_equalityTitle = "=";
			if ($type == "sp:ne") $_equalityTitle = "!=";
			if ($type == "sp:lt") $_equalityTitle = "&lt;";
			if ($type == "sp:gt") $_equalityTitle = "&gt;";
			if ($type == "sp:ge") $_equalityTitle = "&gt;=";
			if ($type == "sp:le") $_equalityTitle = "&lt;=";

			$filters .= $type . ";"; 
		}
				
		$value =  cleanString($row['value']);
		
		$_labelVal = "";
		if ($labelsArray[$value] == null) {
			//we dont have a label
			$_lv = $value;
			if (strpos($_lv,"http://") !== FALSE) $_lv = substr($_lv,strrpos($_lv,"/") + 1);
			
			
			$_labelVal = $_lv;
			
			$filters .= $_lv .";";
		}
		else { 
			$filters .= $labelsArray[$value]  .";";
			$_labelVal = $labelsArray[$value];
		}
		
		$filters .= $value . ";"; 
		
		$tempWhere .= "<strong>".$_appOnTitle."</strong> ".$_equalityTitle." ".$_labelVal." & " ;
	}

	$tempWhere = trim($tempWhere, " & ");
	$filters = trim($filters, ";");

	$uri .= $dataset.$classURI.$classLabel.$optionals.$filters;

	if (strpos($tempWhere,"[%filters%]") !== FALSE){
		$tempTitle = str_replace("[%where%]", "", $tempTitle);
	} else {
		$tempTitle = str_replace("[%where%]", $tempWhere, $tempTitle); 
	}
	
	if (strpos($tempShowing,"[%optionals%]") !== FALSE){
		$tempTitle = str_replace("[%showing%]", "", $tempTitle);
	} else {
		$tempTitle = str_replace("[%showing%]", $tempShowing, $tempTitle); 
	}

	return $uri;
}


function getAppliedOn($queryURI, $varfilter){
	$applied_on = "SELECT DISTINCT * WHERE { 
		   <%%queryURI%%> sp:where ?whereClause .
		   ?whereClause rdf:rest*/rdf:first ?clauses .
		   
			 {?clauses rdf:rest*/rdf:first ?clause .
		   ?clause sp:object ?clauseObject .
		   ?clauseObject sp:varName ?filter .
		   ?clause sp:predicate ?appliedOn .
		   FILTER (?filter = \"%%filter%%\") .}
			 
		   UNION {
		   ?clauses sp:object ?clauseObject .
		   ?clauseObject sp:varName ?filter .
		   ?clauses sp:predicate ?appliedOn .
		   FILTER (?filter = \"%%filter%%\") .}
			 
	}";

	$applied_on = str_replace("%%queryURI%%", $queryURI, $applied_on);
	$applied_on = str_replace("%%filter%%", $varfilter, $applied_on);
	
	$result = sparql_query( $applied_on ); 

	if( !$result ) { print sparql_errno() . ": " . sparql_error(). "\n"; exit; }

	$retStr = "";
	while( $row = sparql_fetch_array( $result ) )
	{
		$retStr = $row['appliedOn'];
	}
	
	return $retStr;
}

function getLabels($dataset,$class){
	$jsonurl = "http://localhost:8081/rdf2any/v1.0/builder/properties?dataset=".urlencode($dataset)."&class=".urlencode($class);


	$json = file_get_contents($jsonurl);
	$results = json_decode($json, true);
	
	$returnArray = array();

	foreach($results['rdfClass']['properties'] as $obj){
		$uri = $obj['uri'];
		$label = $obj['label'];
		$returnArray[$uri] = str_replace(" ","",$label);
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
	$tempTitle = "";
	$item = "{";
	foreach( $fields as $field )
	{
		$item .= "\"" . cleanString($field) ."\" : \"" . cleanString($row[$field]) . "\",";
	}
	$item .= "\"uri\" : \"" .uriParameter($row['initialDataset'],$row['query']). "\",";
	$item .= "\"title\" : \"" .$tempTitle. "\"";
	$item .= "},";
	$json .= $item;
}
$json = trim($json, ",");
$json .= "] }";

header('Content-Type: application/json');
echo $json;
//print $json;
