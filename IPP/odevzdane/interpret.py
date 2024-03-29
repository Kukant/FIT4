#!/usr/bin/python3.6

"""
    Interpret for IPPcode18 created by: Tomas Kukan 18. 2. 2018

"""

import globals as g
from operator_functions import *


def processInstructions():
    while g.instruction_num < len(g.instructions):
        op = g.instructions[g.instruction_num].opcode
        args = g.instructions[g.instruction_num].args

        if op == "CREATEFRAME":
            CREATEFRAME(args)
        elif op == "PUSHFRAME":
            PUSHFRAME(args)
        elif op == "POPFRAME":
            POPFRAME(args)
        elif op == "RETURN":
            RETURN(args)
        elif op == "BREAK":
            BREAK(args)
    
        # 1 operand
        elif op == "CALL":
            CALL(args)
        elif op == "JUMP":
            JUMP(args)
        elif op == "LABEL":
            pass # labels are registered during file parsing
        elif op == "DEFVAR":
            DEFVAR(args)
        elif op == "POPS":
            POPS(args)
        elif op == "PUSHS":
            PUSHS(args)
        elif op == "WRITE":
            WRITE(args)
        elif op == "DPRINT":
            DPRINT(args)

        # 2 operands
        elif op == "READ":
            READ(args)
        elif op == "MOVE":
            MOVE(args) # v s
        elif op == "INT2CHAR":
            INT2CHAR(args)
        elif op == "STRLEN":
            STRLEN(args)
        elif op == "TYPE":
            TYPE(args)
        elif op == "NOT":
            NOT(args)
        
        # 3 operands
        elif op == "ADD":
            args.append("+")
            ARITMETIC(args)
        elif op == "SUB":
            args.append("-")
            ARITMETIC(args)
        elif op == "MUL":
            args.append("*")
            ARITMETIC(args)
        elif op == "IDIV":
            args.append("/")
            ARITMETIC(args)
        elif op == "LT":
            args.append("<")
            COMPARE(args)
        elif op == "GT":
            args.append(">")
            COMPARE(args)
        elif op == "EQ":
            args.append("==")
            COMPARE(args)
        elif op == "AND":
            args.append("and")
            AND_OR(args)
        elif op == "OR":
            args.append("or")
            AND_OR(args)
        elif op == "STRI2INT":
            STRI2INT(args)
        elif op == "CONCAT":
            CONCAT(args)
        elif op == "GETCHAR":
            GETCHAR(args)
        elif op == "SETCHAR":
            SETCHAR(args)
        elif op == "JUMPIFEQ":
            args.append("EQ")
            JUMPIF(args)
        elif op == "JUMPIFNEQ":
            args.append("NEQ")
            JUMPIF(args)
        else:
            error("ERROR: pass unknown operator '" + op + "'", Err.lexOrSyn)

        g.instruction_num += 1


#  MAIN

def main():
    g.init()
    get_opts()
    parse_xml_file()
    #import pprint
    #pprint.pprint(g.instructions)
    processInstructions()


if __name__ == "__main__":
    main()
    exit(0)
