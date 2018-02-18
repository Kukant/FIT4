#!/usr/bin/php5.6
<?php 

## FUNCTIONS
function printHelp() {
    echo "This program creates an XML file out of IPPcode2018\n";
    echo "Author: Tomas Kukan, 12. 1. 2018\n";
    echo "Options: --help : prints help.\n";
}

function GetArguments($argv) {
    $shortopts  = "";
    $longopts = array("help");

    $options = getopt($shortopts, $longopts);

    if (count($argv) > 1) {
        exit(10);
    } elseif (isset($options["help"])) {
        printHelp();
        exit(0);
    } 
}

function ProcessInput() {
    
    $line = ProcessOneLine(readline());
    if (count($line) != 1 || strtoupper($line[0]) != ".IPPCODE18")
        Error("file");
    $i = 1;
    $xw = xmlwriter_open_memory();
    xmlwriter_set_indent($xw, 1);
    xmlwriter_set_indent_string($xw, ' ');
    xmlwriter_start_document($xw, '1.0', 'UTF-8');
    xmlwriter_start_element($xw, 'program'); # program
        xmlwriter_start_attribute($xw, 'language');
            xmlwriter_text($xw, 'IPPcode18');
        xmlwriter_end_attribute($xw);
    while ($line = fgets(STDIN) ) {
        $parts = ProcessOneLine($line);
        if (count($parts) == 0) # empty line
            continue;
        xmlwriter_start_element($xw, 'instruction'); # instruction
        xmlwriter_start_attribute($xw, 'order');
            xmlwriter_text($xw, $i);
        xmlwriter_end_attribute($xw);

        xmlwriter_start_attribute($xw, 'opcode');
        $args = array_slice($parts, 1); # slice off the instruction

        # put there the opperation code now, if there is an error, there will be no output neverteless
        xmlwriter_text($xw, $parts[0]);
        xmlwriter_end_attribute($xw); # opcode
        switch($parts[0]) {
            # 0 operands
            case "CREATEFRAME":
            case "PUSHFRAME":
            case "POPFRAME":
            case "RETURN":
            case "BREAK":
                if (count($parts) != 1)
                    Error("instr", $line);
                break;
            # 1 operand
            case "CALL": # l
            case "JUMP": # l
            case "LABEL": # l
                XmlAddArgument(ProcessArguments($args, array("label")), $xw);
                break;
            case "DEFVAR": # v
            case "POPS": # v
                XmlAddArgument(ProcessArguments($args, array("var")), $xw);
                break;
            case "PUSHS": # s
            case "WRITE": # s
            case "DPRINT": # s
                XmlAddArgument(ProcessArguments($args, array("symb")), $xw);
                break;
            # 2 operands
            case "READ": # v t
                XmlAddArgument(ProcessArguments($args, array("var", "type")), $xw);
                break;
            case "MOVE": # v s
            case "INT2CHAR": # v s
            case "STRLEN": # v s
            case "TYPE": # v s
                XmlAddArgument(ProcessArguments($args, array("var", "symb")), $xw);
                break;
            # 3 operands
            case "ADD":
            case "SUB":
            case "MUL":
            case "IDIV":
            case ($parts[0] == "LG" || $parts[0] == "GT" || $parts[0] == "EQ"):
            case ($parts[0] == "AND" || $parts[0] == "OR" || $parts[0] == "NOT"):
            case "STRI2INT":
            case "CONCAT":
            case "GETCHAR":
            case "SETCHAR": # pocud v s s
                XmlAddArgument(ProcessArguments($args, array("var", "symb", "symb")), $xw);
                break;
            case "JUMPIFEQ": # l s s
            case "JUMPIFNEQ":
                XmlAddArgument(ProcessArguments($args, array("label", "symb", "symb")), $xw);
                break;
            # default
            default:
                echo "ERROR: unknown operator '${parts[0]}'\n";
                exit(21);
                break;

        }
        xmlwriter_end_element($xw); #instruction
        $i+=1;
    }
    xmlwriter_end_element($xw); #program
    xmlwriter_end_document($xw);
    # print whole xml file
    echo xmlwriter_output_memory($xw);
}

function XmlAddArgument($argLists, $xw) {
    for ($i = 0; $i < count($argLists); $i+=1) {
        $argList = $argLists[$i];
        foreach ($argList as $key => $value) {
            $argNum = $i + 1;
            xmlwriter_start_element($xw, "arg${argNum}");
                xmlwriter_start_attribute($xw, "type");
                xmlwriter_text($xw, $key);
                xmlwriter_end_attribute($xw);
                xmlwriter_text($xw, $value);    
            xmlwriter_end_element($xw);
        }
    }
}

function GetMyType($inStr){
    switch ($inStr) {
        case ($inStr == "LT" || $inStr == "TF" || $inStr == "GF"):
            return "var";
        case ($inStr == "int" || $inStr == "bool" || $inStr == "string"):
            return "${inStr}";
        default:
            Error("unknown type");
    }
}

function Error($type, $arg = "") {
    switch ($type) {
        case "instr": 
            echo "ERROR: instruction invalid: ${arg}";
            break;
        case "arg":
            echo "ERROR: invalid argument: ${arg}";
            break;

        case "file":
            echo "ERROR: wrong file format";
        default:
            echo "Error: ".$type;
            break;
    }
    exit (21);
}

# removes comments from one line
function DeleteComments($inStr) {
    return explode("#", $inStr)[0];
}

function ProcessOneLine($line) {
    $noComments = DeleteComments($line);
    $split = preg_split('/\s+/', $noComments);
    # get rid of "" in retur na array
    $nonEmpty =  array_filter($split, function($s) {return $s != "";} );
    if (count($nonEmpty) > 0)
        $nonEmpty[0] = strtoupper($nonEmpty[0]);

    return $nonEmpty;
}


/** returns an array of two items, [0] = ramec, typ [1] = nazev promenne, hodnota
 * expected types={var, symb, type, label} */

# handles errors on its own
function ProcessArguments($args, $expectedTypes) {
    if (count($args) != count($expectedTypes))
        Error("instr", $args);
    $returnArr = array();
    for ($i = 0; $i < count($args); $i+=1){
        $expectedType = $expectedTypes[$i];
        $arg = $args[$i];
        $split = explode("@", $arg, 2);
        if (count($split) == 1) {
            if ($arg == "string" || $arg == "int" || $arg == "bool"){ # type
                if ($expectedType != "type")
                    Error("Unexpected arg type '${expectedType}' != type");
                $returnArr[$i] = array("type" => $arg);
            } else { # label
                if ($expectedType != "label")
                    Error("Unexpected arg type '${expectedType}' != label");
                $returnArr[$i] = array ("label" => $arg);
            }
        }
        else {
            ## constant or variable
            switch ($split[0]) {
                case "bool":
                    if ($split[1] != "true" || $split[1] != "false")
                        Error("Unexpected bool value: ".$split[1]."\n");
                case "int":
                case "string":
                    if ($split[0] == "string")
                        for ($ind = 0; $ind < strlen($split[1]); $ind = $ind + 1){
                            if ($split[1][$ind] == "\\"){
                                if (strlen($split[1]) < $ind + 3 + 1 )
                                    Error("wrong format of string: ".$split[1]);
                                if (ctype_digit(substr($split[1], $ind + 1, 3 )) == false)
                                    Error("wrong format of string: ".$split[1]);
                            }    
                        }
                    if ($expectedType != "symb")
                        Error("Unexpected arg type '${expectedType}' != symb");
                    $returnArr[$i] = array($split[0] => $split[1]);
                    break;

                case "GF":
                case "LF":
                case "TF":
                    if ($expectedType != "symb" && $expectedType != "var")
                        Error("Unexpected arg type '${expectedType}' != var || symb");
                    $returnArr[$i] = array("var" => $arg);
                    break;

                default:
                    Error("arg", $arg);
            }
        }
    }
    return $returnArr;
}

## MAIN
GetArguments($argv);
ProcessInput();
?>