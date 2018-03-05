#!/usr/bin/php5.6
<?php

function printHelp() {
    echo "This program tests parsing and interpreting IPPcode18\n";
    echo "Author: Tomas Kukan, 12. 1. 2018\n";
    echo "Options: --help : prints help.\n";
}

function GetArguments($argv) {
    $shortopts  = "";
    $longopts = array("help","directory:", "recursive", "parse-script:", "int-script:");

    $options = getopt($shortopts, $longopts);

    ## get unknown options
    array_walk($argv, function(&$value, $key) {
        // get rid of not opts
        if(preg_match('/^-.*/', $value))
            $value = str_replace('-', '', $value);    
        else
            $value = '';
        
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


$args = GetArguments($argv);
print_r($args);

?>