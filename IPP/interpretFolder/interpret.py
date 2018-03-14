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
