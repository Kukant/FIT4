#!/usr/bin/php5.6
<?php 

## FUNCTIONS
function printHelp(){
    echo "This program creates an XML file out of IPPcode2018\n";
    echo "Author: Tomas Kukan, 12. 1. 2018\n";
    echo "Options: --help : prints help.\n";
}

function GetArguments($argv){
    $shortopts  = "";
    $longopts = array("help");

    $options = getopt($shortopts, $longopts);
    if (isset($options["help"]))
    {
        printHelp();
        exit(0);
    } elseif (count($argv) > 1) {
        exit(10);
    }
}

function ProcessInput(){
    
    $line = ProcessOneLine(readline());
    if (count($line) != 1 || $line[0] != ".IPPcode18")
        exit(21);

    while ($line = readline())
    {
        $parts = ProcessOneLine($line);
        var_dump($parts);
    }
}

# removes comments, returns an array of lexems
function ProcessOneLine($line){
    $noComments = explode("#", $line)[0];
    $split = preg_split('/\s+/', $noComments);
    # get rid of "" in retur na array
    return array_filter($split, function($s) {return $s!="";});
}


## MAIN
GetArguments($argv);

ProcessInput();


?>