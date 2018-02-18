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
            pass  # v
        elif op == "POPS":
            pass  # v
        
        elif op == "PUSHS":
            pass  # s
        elif op == "WRITE":
            pass  # s
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
        elif (op == "AND" or op == "OR" or op == "NOT"):
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


def main():
    getOpts()
    parseXMLFile()
    # import pprint
    # pprint.pprint(instructions)
    processInstructions()


if __name__ == "__main__":
    main()
    exit(0)