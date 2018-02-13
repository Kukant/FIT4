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
        switch($parts[0]) {
            # 0 operands
            case "CREATEFRAME":
            case "PUSHFRAME":
            case "POPFRAME":
            case "RETURN":
            case "BREAK":
                if (count($parts) != 1)
                    Error("instr", $line);

                xmlwriter_text($xw, $parts[0]);
                xmlwriter_end_attribute($xw); # opcode
                break;
            # 1 operand
            case "CALL": # l
            case "JUMP": # l
            case "DEFVAR": # v
            case "POPS": # v
            case "PUSHS": # s
            case "WRITE": # s
            case "LABEL": # s
            case "DPRINT": # s
                if (count($parts) != 2)
                     Error("instr", $line);
                xmlwriter_text($xw, $parts[0]);
                xmlwriter_end_attribute($xw); # opcode
                XmlAddArgument(ProcessArgument($parts[1]), $xw, 1);
                break;
            # 2 operands
            case "MOVE":
            case "INT2CHAR":
            case "READ":
            case "STRLEN":
            case "TYPE":
                if (count($parts) != 3)
                    Error("instr", $line);
                xmlwriter_text($xw, $parts[0]);
                xmlwriter_end_attribute($xw); # opcode
                XmlAddArgument(ProcessArgument($parts[1]), $xw, 1);
                XmlAddArgument(ProcessArgument($parts[2]), $xw, 2);
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
            case "SETCHAR":
            case "JUMPIFEQ":
            case "JUMPIFNEQ":
                if (count($parts) != 4)
                    Error("instr", $line);
                xmlwriter_text($xw, $parts[0]);
                xmlwriter_end_attribute($xw); # opcode
                XmlAddArgument(ProcessArgument($parts[1]), $xw, 1);
                XmlAddArgument(ProcessArgument($parts[2]), $xw, 2);
                XmlAddArgument(ProcessArgument($parts[3]), $xw, 3);
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

function XmlAddArgument($argList, $xw, $argIndex) {
    foreach ($argList as $key => $value) {
        xmlwriter_start_element($xw, "arg${argIndex}");
            xmlwriter_start_attribute($xw, "type");
            xmlwriter_text($xw, $key);
            xmlwriter_end_attribute($xw);
            xmlwriter_text($xw, $value);    
        xmlwriter_end_element($xw);
    }
}

function GetMyType($inStr){
    switch ($inStr) {
        case ($inStr == "LT" || $inStr == "TF" || $inStr == "GF"):
            return "var";
        case ($inStr == "int" || $inStr == "bool" || $inStr == "string"):
            return "${inStr}";
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
            echo "ERROR";
            break;
    }
    exit (21);
}

# removes comments, returns an array of lexems
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


/** returns an array of two items, [0] = ramec, typ [1] = nazev promenne, hodnota*/
# handles errors on its own
function ProcessArgument($arg) {
    $split = explode("@", $arg, 2);
    if (count($split) == 1)
        return array ("label" => $arg);
    else {
        ## constant or variable
        switch ($split[0]) {
            case "int":
            case "bool":
            case "string":
                return array($split[0] => $split[1]);

            case "GF":
            case "LF":
            case "TF":
                return array("var" => $arg);

            default:
                Error("arg", $arg);
        }
    }
}

## MAIN
GetArguments($argv);
ProcessInput();
?>