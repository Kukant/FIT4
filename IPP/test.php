#!/usr/bin/php5.6
<?php

function printHelp() {
    echo "This program tests parsing and interpreting IPPcode18\n";
    echo "Author: Tomas Kukan, 12. 1. 2018\n";
    echo "Options: --help : prints help.\n";
}

function GetArguments($argv) {

    for($i = 1; $i < count($argv); $i+=1){
        if ($argv[$i][0] != "-") {
            echo "Wrong arg(s).\n";    
            exit(10);
        }
    }

    $shortopts  = "";
    $longopts = array("help","directory:", "recursive", "parse-script:", "int-script:");

    $options = getopt($shortopts, $longopts);

    ## get unknown options
    array_walk($argv, function(&$value, $key) {
        // get rid of not opts
        $arg = false;
        while(preg_match('/^-.*/', $value)) {
            $value = substr($value, 1);
            $arg = true;  
        }
        if (!$arg) {
            $value = "";
        }
        
        if (strpos($value, "=") !== false)
            $value = explode("=", $value)[0];
    });
    $argv = array_filter($argv); 
    $unknow_options = array_diff($argv, array_values(array("help","directory", "recursive", "parse-script", "int-script")));
    if ( count($unknow_options) > 0) {
        echo "Unknown option(s).\n";
        var_dump($unknow_options);
        exit(10);
    }

    if (isset($options["help"])) {
        printHelp();
        exit(0);
    }

    return $options;
}

function rglob($pattern) {
    $files = glob($pattern); 
    foreach (glob(dirname($pattern).'/*', GLOB_ONLYDIR|GLOB_NOSORT) as $dir) {
        $files = array_merge($files, rglob($dir.'/'.basename($pattern)));
    }
    return $files;
}

function getFilePaths($opts) {
    $path = ".";
    if (isset($opts["directory"])) 
        $path = $opts["directory"];

    if (preg_match('/^.*\/$/', $path)) {
        $path = substr($directory_path, 0, -1);
    }

    $path = $path."/*.src";

    if (isset($opts["recursive"])) {
        return rglob($path);
    } else {
        return glob($path);
    }
}

$opts = GetArguments($argv);
print_r("options:");
print_r($opts);

$parser_path = "./parse.php";
if (isset($opts["parse-script"])){
$parser_path = $opts["parse-script"];
}

$interpret_path = "./interpret.py";
if (isset($opts["int-script"])) {
    $interpret_path = $opts["int-script"];
}

$srcFiles = getFilePaths($opts);
print_r("files:");
print_r($files);

?>