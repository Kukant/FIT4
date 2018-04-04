#!/usr/bin/php5.6
<?php
$errParam = 10;
$errInFile = 11;
$errOutFile = 12;
$GLOBALS['parserTestsOut'] = array();
$GLOBALS['interpretTestsOut'] = array();
$GLOBALS['interpretInputs'] = array();

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

    if (preg_match('/^.*\/$/', $path))
        $path = substr($directory_path, 0, -1);

    $path = $path."/*.src";

    if (isset($opts["recursive"])) {
        return rglob($path);
    } else {
        return glob($path);
    }
}

function createUnexistingFiles($srcFiles, &$inFiles, &$outFiles, &$rcFiles) {
    foreach($srcFiles as $srcFile) {
        $inFile = substr($srcFile,0 ,count($srcFile) - 5).".in";
        $outFile = substr($srcFile,0 ,count($srcFile) - 5).".out";
        $rcFile = substr($srcFile,0 ,count($srcFile) - 5).".rc";
        
        if (!file_exists($inFile)) file_put_contents($inFile, "");
        if (!file_exists($outFile)) file_put_contents($outFile, "");
        if (!file_exists($rcFile)) file_put_contents($rcFile, 0);

        array_push($inFiles, $inFile);
        array_push($outFiles, $outFile);
        array_push($rcFiles, $rcFile);
    }
}

function testParser($srcFiles, $parser_path, $rcFiles) {
    for($i = 0; $i < count($srcFiles); $i+=1 ){ 
        $expectedRet = file_get_contents($rcFiles[$i]);
        if ($expectedRet === False) {
            echo "Cannot open file ".$rcFiles[$i];
            exit(11);
        }
        
        $expectedRet = intval($expectedRet);

        $outputArr = array();
        exec("php ".$parser_path." < ".$srcFiles[$i], $outputArr, $ret);
        if ($ret != 0){
            if ($ret != $expectedRet) {
                // test failed
                array_push($GLOBALS['parserTestsOut'], "Unexpected return value"); 
            } else {
                array_push($GLOBALS['parserTestsOut'], "OK"); 
            }
            array_push($GLOBALS['interpretInputs'], "fail"); // interpret will not be tested
            continue;
        }
        $xmlFile = "";
        foreach($outputArr as $line){
            $xmlFile = $xmlFile.$line."\n";
        }

        array_push($GLOBALS['interpretInputs'], array("input" => $xmlFile,
                                                      "retcode" => $expectedRet));
        array_push($GLOBALS['parserTestsOut'], "OK"); 
        
    }
}

function testInterpreter($inFiles, $interpret_path, $outFiles) {
    // $GLOBALS['interpretInputs']
    for($i = 0; $i < count($inFiles); $i+=1 ){ 
        if (count($GLOBALS['interpretInputs'][$i]) != 2) {
            // parser did not end well
            continue;
        }
        $input = $GLOBALS['interpretInputs'][$i]["input"];
        $retcode = $GLOBALS['interpretInputs'][$i]["retcode"];

        if (file_put_contents('xkukan00temp.xml', $input) == False){ #creating temporary xmlfile
            echo "Cannot create temporary file";
            exit(12);
        }

        exec("python3.6 ".$interpret_path." --source=xkukan00temp.xml <".$inFiles[$i]. "> xkukan00tempout",
             $out, $ret);
        
        if ($ret != 0) {
            if ($ret != $retcode) {
                array_push($GLOBALS['interpretTestsOut'], "Unexpected return code: ".(string)$ret); 
            } else {
                array_push($GLOBALS['interpretTestsOut'], "OK");
            }
            continue;
        }

        exec("diff xkukan00tempout ".$outFiles[$i], $out, $ret);
        if ($ret != 0) {
            array_push($GLOBALS['interpretTestsOut'], "Diff is not OK.");
            continue; 
        }

        array_push($GLOBALS['interpretTestsOut'], "OK"); 

    }

    exec("rm xkukan00tempout xkukan00temp.xml");
}

function generateHTML($srcFiles) {
    echo '<!DOCTYPE html>
            <html>
                <body>';
    echo '<h3>Parser Tests: </h3>';
    parserTable($srcFiles);
    echo '<h3>Interpreter Tests: </h3>';
    interpretTable($srcFiles);
    echo '      </body>
            </html>';
}

function parserTable($srcFiles) {
    echo '<table>';
    echo '<tr>
            <th>Test source file</th>
            <th>Result</th> 
            <th>Error message</th>
          </tr>';
    for ($i=0; $i<count($srcFiles); $i+=1) {
        $result="";
        $errMessage="";
        if ($GLOBALS['parserTestsOut'][$i] == "OK") {
            $result = "OK";
        } else {
            $result = "FAIL";
            $errMessage = $GLOBALS['parserTestsOut'][$i];
        }

        echo '<tr>
            <td>'.$srcFiles[$i].'</td>
            <td>'.$result.'</td> 
            <td>'.$errMessage.'</td>
          </tr>';
    }

    echo '</table>';
}

function interpretTable($srcFiles) {
    echo '<table>';
    echo '<tr>
            <th>Test source file</th>
            <th>Result</th> 
            <th>Error message</th>
          </tr>';
    for ($i=0; $i<count($srcFiles); $i+=1) {
        $result="";
        $errMessage="";
        if ($GLOBALS['interpretTestsOut'][$i] == "OK") {
            $result = "OK";
        } else {
            $result = "FAIL";
            $errMessage = $GLOBALS['interpretTestsOut'][$i];
        }

        echo '<tr>
            <td>'.$srcFiles[$i].'</td>
            <td>'.$result.'</td> 
            <td>'.$errMessage.'</td>
          </tr>';
    }

    echo '</table>';
}

$opts = GetArguments($argv);
//print_r("options:");
//print_r($opts);

$parser_path = "./parse.php";
if (isset($opts["parse-script"])){
$parser_path = $opts["parse-script"];
}

//print_r("\nparser_path:");
//print_r($parser_path);

$interpret_path = "./interpret.py";
if (isset($opts["int-script"])) {
    $interpret_path = $opts["int-script"];
}
//print_r("\ninterpret:");
//print_r($interpret_path);

$srcFiles = getFilePaths($opts);
//print_r("\nfiles:");
//print_r($srcFiles);

$inFiles = Array();
$outFiles = Array();
$rcFiles = Array();
createUnexistingFiles($srcFiles, $inFiles, $outFiles, $rcFiles);
// //print_r("\ninFiles\n");
//     //print_r($inFiles);
//     //print_r("\noutFiles\n");
//     //print_r($outFiles);
//     //print_r("\nrcFiles\n");
//     //print_r($rcFiles);
# call parser.php
testParser($srcFiles, $parser_path, $rcFiles);
# call interpret.py
testInterpreter($inFiles, $interpret_path, $outFiles);

//print_r($GLOBALS['parserTestsOut']); 
//print_r($GLOBALS['interpretTestsOut']); 
////print_r($GLOBALS['interpretInputs']); 
generateHTML($srcFiles);
?>