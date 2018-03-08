#!/usr/bin/python3.6

"""
    Interpret for IPPcode18 created by: Tomas Kukan 18. 2. 2018

"""

import getopt
import sys
from enum import Enum
import xml.etree.ElementTree as ET


class Err(Enum):
    args = 10
    openingFile = 11
    parsingFile = 31
    lexOrSyn = 32
    semantic = 52
    in_wrongOperand = 53
    in_nonExistingVar = 54
    in_nonExistingFrame = 55
    in_varNotDefined = 56
    in_zeroDivision = 57
    in_wrongStringHandling = 58

# global variables
inputFilename = ""
# parsed file
instructions = []

GF = dict()
TF = dict()


def error(msg, exitCode):
    print("Error: ", msg)
    exit(exitCode.value)


def printHelp():
    print(
        "This program interprets IPPcode2018 structured into XML file\n",
        "Author: Tomas Kukan, 12. 1. 2018\n",
        "Options: --help : prints help.\n",
        "         --file=<filename> : input XML file -required"
        )


def getOpts():

    try:
        opts, args = getopt.getopt(sys.argv[1:], "hf:", ["help", "file="])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(10)

    if len(args) > 0 or len(opts) != 1:
        error("Wrong parameters.", Err.args);

    for o, a in opts:
        if o in ("-h", "--help"):
            printHelp()
            sys.exit()
        elif o in ("-f", "--file"):
            global inputFilename
            inputFilename = a
        else:
            assert False, "unhandled option"


def parseXMLFile():
    global inputFilename
    global instructions
    tree = None
    try:
        tree = ET.parse(inputFilename)
    except ET.ParseError:
        error("Exception while parsing file: " + inputFilename, Err.parsingFile)
    except Exception as e:
        error("Exception while opening file: " + inputFilename, Err.openingFile)

    root = tree.getroot()
    # test root
    if root.tag != "program":
        error("Root's tag is not program", Err.parsingFile)
    rootKeys = tuple(root.attrib.keys())
    if not (rootKeys == tuple(["name", "description", "language"]) or
            rootKeys == tuple(["language"]) or
            rootKeys == tuple(["name", "language"]) or
            rootKeys == tuple(["language", "description"])):
        error("'program' - root element has incorrect attributes", Err.parsingFile)

    if root.attrib["language"] != "IPPcode18":
        error("Wrong program-language: " + root.attrib["language"] + " expected IPPcode18", Err.parsingFile)

    for instruction in root:
        # test instruction
        if instruction.tag != "instruction":
            error("Expected tag instruction, got: " + instruction.tag, Err.parsingFile)
        if tuple(instruction.attrib.keys()) != tuple(["order", "opcode"]):
            error("Instruction attributes are not valid." + tuple(instruction.attrib.keys()).__str__(), Err.parsingFile)

        instructions.append({"order": instruction.attrib["order"],
                             "opcode": instruction.attrib["opcode"],
                             "args": []
                             })
        # test argument
        for i, arg in enumerate(instruction):
            if "arg"+str(i + 1) != arg.tag:
                error("Unexpected argument tag: " + arg.tag, Err.parsingFile)
            if tuple(arg.attrib.keys()) != tuple(["type"]):
                error("Argument attributes are not valid.", Err.parsingFile)

            instructions[-1]["args"].append({
                                             "type": arg.attrib["type"],
                                             "val": arg.text
                                             })


def processInstructions():
    global instructions
    for instruction in instructions:
        op = instruction["opcode"]
        args = instruction["args"]
        if op == "CREATEFRAME":
            pass
        elif op == "PUSHFRAME":
            pass
        elif op == "POPFRAME":
            pass
        elif op == "RETURN":
            pass
        elif op == "BREAK":
            pass
    
        # 1 operand
        elif op == "CALL":
            pass  # l
        elif op == "JUMP":
            pass  # l
        elif op == "LABEL":
            pass  # l
        elif op == "DEFVAR":
            DEFVAR(args)
        elif op == "POPS":
            pass  # v
        
        elif op == "PUSHS":
            pass  # s
        elif op == "WRITE":
            WRITE(args)  # s
        elif op == "DPRINT":
            pass  # s
        
        # 2 operands
        elif op == "READ":
            pass  # v t
        
        elif op == "MOVE":
            pass  # v s
        elif op == "INT2CHAR":
            pass  # v s
        elif op == "STRLEN":
            pass  # v s
        elif op == "TYPE":
            pass  # v s
        elif op == "NOT":
            pass  # v s
        
        # 3 operands
        elif op == "ADD":
            pass
        elif op == "SUB":
            pass
        elif op == "MUL":
            pass
        elif op == "IDIV":
            pass
        elif (op == "LG" or op == "GT" or op == "EQ"):
            pass
        elif (op == "AND" or op == "OR"):
            pass
        elif op == "STRI2INT":
            pass
        elif op == "CONCAT":
            pass
        elif op == "GETCHAR":
            pass
        elif op == "SETCHAR":
            pass  # pocud v s s
        
        elif op == "JUMPIFEQ":
            pass  # l s s
        elif op == "JUMPIFNEQ":
            pass
        else:
            error("ERROR: pass unknown operator '" + op + "'", Err.lexOrSyn)


## OPERATOR FUNCTIONS

def WRITE(args):
    if len(args) != 1:
        error("Unexpected arguments len in WRITE", Err.lexOrSyn)

    param = parse_param(args[0])
    if param["type"] == "var":
        var = get_var(param["frame"], param["name"])
        to_print = {"type": var["type"], "value": var["value"]}
    else:
        to_print = param  # returned value from parse_params() has the same format

    if to_print["type"] != "bool":
        print(to_print["value"])
    else:
        if to_print["value"]:
            print("true")
        else:
            print("false")


def DEFVAR(args):
    if len(args) != 1:
        error("Unexpected arguments len in DEFVAR", Err.lexOrSyn)

    var = parse_param(args[1])
    if var["type"] != "var":
        error("DEFVAR expected var, got: " + var["type"], Err.lexOrSyn)

    if var["frame"] == "GF":
        if var["name"] in GF.keys():
            error("Variable " + var["name"] + " already defined", Err.semantic)
        GF[var["name"]] = {"value": None, "type": None}
    elif var["frame"] == "TF":
        if var["name"] in TF.keys():
            error("Variable " + var["name"] + " already defined", Err.semantic)
        TF[var["name"]] = {"value": None, "type": None}
        pass
    elif var["frame"] == "LF":
        pass  # TODO


## OTHER FUNCTIONS


def parse_param(param):
    if param["type"] == "var":
        return parse_var(param["value"])
    elif param["type"] == "string":
        return {"type": "string", "value": param["value"]}
    elif param["type"] == "int":
        return {"type": "int", "value": param["value"]}
    elif param["type"] == "label":
        return {"type": "label", "value": param["value"]}
    elif param["type"] == "bool":
        return {"type": "bool", "value": param["value"]}
    else:
        error("unexpected param type: " + param["type"], Err.lexOrSyn)


def parse_var(var):
    splitted = var.split("@", 1)
    if len(splitted != 2):
        error("Wrong variable format.", Err.lexOrSyn)

    frame = splitted[0]
    name = splitted[1]

    if frame != "GF" and frame != "LF" and frame != "TF":
        error("Unknown variable frame: " + frame, Err.lexOrSyn)

    return {"frame": frame, "name": name, "type": "var"}

def get_var(frame, name):
    if frame == "GF":
        if name in GF.keys():
            return GF[name]
        else:
            error("Variable not defined", Err.in_varNotDefined)
    elif frame == "TF":
        if name in TF.keys():
            return TF[name]
        else:
            error("Variable not defined", Err.in_varNotDefined)
    elif frame == "LF":
        pass  # TODO
    else:
        error("Unknown variable frame: " + frame, Err.lexOrSyn)

## MAIN

def main():
    getOpts()
    parseXMLFile()
    # import pprint
    # pprint.pprint(instructions)
    processInstructions()


if __name__ == "__main__":
    main()
    exit(0)