#!/usr/bin/python3.6

"""
    Interpret for IPPcode18 created by: Tomas Kukan 18. 2. 2018

"""

from operator_functions import *


def processInstructions():
    global instructions
    for instruction in instructions:
        op = instruction.opcode
        args = instruction.args
        if op == "CREATEFRAME":
            CREATEFRAME(args)
        elif op == "PUSHFRAME":
            PUSHFRAME(args)
        elif op == "POPFRAME":
            POPFRAME(args)
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
            WRITE(args)
        elif op == "DPRINT":
            DPRINT(args)

        # 2 operands
        elif op == "READ":
            READ(args)
        elif op == "MOVE":
            MOVE(args) # v s
        elif op == "INT2CHAR":
            pass  # v s
        elif op == "STRLEN":
            pass  # v s
        elif op == "TYPE":
            pass  # v s
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


#  MAIN

def main():
    get_opts()
    parse_xml_file()
    # import pprint
    # pprint.pprint(instructions)
    processInstructions()


if __name__ == "__main__":
    main()
    exit(0)
