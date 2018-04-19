"""
Module where are located all functions that implements ippcode18 instructions.

Author: Tomas Kukan
Date: early 2018
"""

import globals as g
from other_functions import *

# OPERATOR FUNCTIONS


@args_check([ArgType.symb])
def WRITE(args):
    arg = args[0]
    if arg.type == ArgType.var:
        var = get_var(arg.frame, arg.name)
        to_print = {"type": var.type, "value": var.val}
    else:
        to_print = {"type": arg.type, "value": arg.val}

    if to_print["type"] is None or to_print["value"] is None:
        error("Varible to write is not defined", Err.in_varNotDefined)

    if to_print["type"] != ArgType.bool:
        print(to_print["value"])
    else:
        if to_print["value"]:
            print("true")
        else:
            print("false")


@args_check([ArgType.symb])
def DPRINT(args):
    arg = args[0]
    if arg.type == ArgType.var:
        var = get_var(arg.frame, arg.name)
        to_print = {"type": var.type, "value": var.val}
    else:
        to_print = {"type": arg.type, "value": arg.val}

    if to_print["type"] != ArgType.bool:
        print(to_print["value"], file=sys.stderr)
    else:
        if to_print["value"]:
            print("true", file=sys.stderr)
        else:
            print("false", file=sys.stderr)


@args_check([ArgType.var])
def DEFVAR(args):
    var = args[0]
    if var.type != "var":
        error("DEFVAR expected var, got: " + var.type, Err.lexOrSyn)

    if var.frame == "GF":
        if var.name in g.GF.keys():
            error("Variable " + var.name + " already defined in GF", Err.semantic)
        g.GF[var.name] = Variable()
    elif var.frame == "TF":
        if g.TF is None:
            error("Frame TF is not initialized.", Err.in_nonExistingFrame)
        if var.name in g.TF.keys():
            error("Variable " + var.name + " already defined in TF", Err.semantic)
        g.TF[var.name] = Variable()
    elif var.frame == "LF":
        if g.LF is None:
            error("Frame LF is not initialized.", Err.in_nonExistingFrame)
        if var.name in g.LF.keys():
            error("Variable " + var.name + " already defined in LF", Err.semantic)
        g.GF[var.name] = Variable()


@args_check([ArgType.var, ArgType.symb])
def MOVE(args):
    set_val(args[0], args[1])


@args_check([ArgType.var, ArgType.type])
def READ(args):
    var = input()
    _type = args[1].val
    if _type == "string":
        var = str(var)
    elif _type == "int":
        try:
            var = int(var)
        except ValueError:
            var = 0
    elif _type == "bool":
        var = True if str(var).upper() == "TRUE" else False

    set_val(args[0], Argument(_type=_type, val=var))


@args_check([ArgType.var, ArgType.symb, ArgType.symb, None])
def ARITMETIC(args):
    var = args[0]
    op1 = get_val(args[1], ArgType.int)
    op2 = get_val(args[2], ArgType.int)
    operator = args[3]
    res = 0
    if operator == "+":
        res = op1 + op2
    elif operator == "-":
        res = op1 - op2
    elif operator == "*":
        res = op1 * op2
    elif operator == "/":
        if op2 == 0:
            error("Zero division!", Err.in_zeroDivision)
        res = int(op1/op2)
    else:
        dbgp("Unknownd operator" + operator)

    del args[-1]
    set_val(var, Argument(_type=ArgType.int, val=res))


@args_check([ArgType.var, ArgType.symb, ArgType.symb, None])
def COMPARE(args):
    operands_type = None
    # get operands type
    if args[1].type == ArgType.var:
        var = get_var(args[1].frame, args[1].name)
        if var.type is None or var.val is None:
            error("Variable is not defined", Err.in_varNotDefined)
        else:
            operands_type = var.type
    else:
        operands_type = args[1].type

    if operands_type not in [ArgType.int, ArgType.string, ArgType.bool]:
        error("Unexpected operand type", Err.in_wrongOperand)

    op1 = get_val(args[1], operands_type)
    op2 = get_val(args[2], operands_type)
    operator = args[3]
    res = False
    if operator == "<":  # LT
        res = op1 < op2
    elif operator == ">":  # GT
        res = op1 > op2
    elif operator == "==":  # EQ
        res = op1 == op2
    else:
        dbgp("Unknown operator in COMPARE")

    del args[-1]
    set_val(args[0], Argument(_type=ArgType.bool, val=res))


@args_check([ArgType.var, ArgType.symb, ArgType.symb, None])
def AND_OR(args):
    var = args[0]
    op1 = get_val(args[1], ArgType.bool)
    op2 = get_val(args[2], ArgType.bool)
    operator = args[3]
    res = False
    if operator == "and":
        res = op1 and op2
    elif operator == "or":
        res = op1 or op2

    del args[-1]
    set_val(var, Argument(_type=ArgType.bool, val=res))

@args_check([ArgType.var, ArgType.symb])
def NOT(args):
    var = args[0]
    op1 = get_val(args[1], ArgType.bool)
    res = not op1
    set_val(var, Argument(_type=ArgType.bool, val=res))


@args_check([])
def CREATEFRAME(args):
    g.TF = {}

@args_check([])
def PUSHFRAME(args):
    g.frames_stack.append(g.TF)
    g.LF = g.TF
    g.TF = None

@args_check([])
def POPFRAME(args):
    if len(g.frames_stack) < 1:
        error("Frame stack is empty.", Err.in_nonExistingFrame)
    g.TF = g.frames_stack[-1]
    del g.frames_stack[-1]
    LF = None if len(g.frames_stack) < 1 else g.frames_stack[-1]


@args_check([ArgType.symb])
def PUSHS(args):
    if args[0].type == ArgType.var:
        var = get_var(args[0].frame, args[0].name)
        to_push = var
    else:
        to_push = args[0]

    g.data_stack.append(Argument(_type=to_push.type, val=to_push.val))


@args_check([ArgType.var])
def POPS(args):
    var = args[0]
    if len(g.data_stack) < 1:
        error("Data stack is empty.", Err.semantic)
    set_val(var, g.data_stack[-1])
    del g.data_stack[-1]

@args_check([ArgType.label])
def JUMP(args):
    if args[0].val in g.labels.keys():
        g.instruction_num = g.labels[args[0].val] - 1 # 1 will be added in the end of switch
    else:
        error("Label " + args[0].val + " is not defined.", Err.semantic)

@args_check([ArgType.label, ArgType.symb, ArgType.symb, None])
def JUMPIF(args):
    operands_type = None
    # get operands type
    if args[1].type == ArgType.var:
        var = get_var(args[1].frame, args[1].name)
        if var.type is None or var.val is None:
            error("Variable is not defined", Err.in_varNotDefined)
        else:
            operands_type = var.type
    else:
        operands_type = args[1].type

    if operands_type not in [ArgType.int, ArgType.string, ArgType.bool]:
        error("Unexpected operand type", Err.in_wrongOperand)

    op1 = get_val(args[1], operands_type)
    op2 = get_val(args[2], operands_type)
    res = op1 == op2

    if (res and args[3] == "EQ") or \
       (not res and args[3] == "NEQ"):
        JUMP([args[0]])
    del args[-1]


@args_check([ArgType.label])
def CALL(args):
    g.call_stack.append(g.instruction_num)
    JUMP(args)

@args_check([])
def RETURN(args):
    if len(g.call_stack) > 0:
        g.instruction_num = g.call_stack[-1]
        del g.call_stack[-1]
    else:
        error("Callstack is empty.", Err.semantic)

@args_check([])
def BREAK(args):
    debug_print()

@args_check([ArgType.var, ArgType.symb])
def INT2CHAR(args):
    val = get_val(args[1], ArgType.int)

    try:
        val = chr(val)
    except ValueError:
        error("Int cannot be converted to char.", Err.in_wrongStringHandling)

    set_val(args[0], Argument(_type=ArgType.string, val=val))

@args_check([ArgType.var, ArgType.symb, ArgType.symb])
def STRI2INT(args):
    # get string
    string = get_val(args[1], ArgType.string)
    # get index
    index = get_val(args[2], ArgType.int)
    # set char
    val = None
    if len(string) <= index:
        error("Index out of string range.", Err.in_wrongStringHandling)
    else:
        val = ord(string[index])

    set_val(args[0], Argument(_type=ArgType.int, val=val))

@args_check([ArgType.var, ArgType.symb])
def STRLEN(args):
    val = get_val(args[1], ArgType.string)
    set_val(args[0], Argument(_type=ArgType.int, val=len(val)))


@args_check([ArgType.var, ArgType.symb])
def TYPE(args):

    if args[1].type == ArgType.var:
        var = get_var(args[1].frame, args[1].name)
        if var.type is None or var.val is None:
            val = ArgType.none_type
        else:
            val = var.type
    else:
        val = args[1].type

    if val == ArgType.string:
        val = "string"
    elif val == ArgType.int:
        val = "int"
    elif val == ArgType.bool:
        val = "bool"
    elif val == ArgType.none_type:
        val = ""

    set_val(args[0], Argument(_type=ArgType.string, val=val))


@args_check([ArgType.var, ArgType.symb, ArgType.symb])
def CONCAT(args):
    str1 = get_val(args[1], ArgType.string)
    str2 = get_val(args[2], ArgType.string)
    set_val(args[0], Argument(_type=ArgType.string, val=str1+str2))


@args_check([ArgType.var, ArgType.symb, ArgType.symb])
def GETCHAR(args):
    # get string
    string = get_val(args[1], ArgType.string)
    # get index
    index = get_val(args[2], ArgType.int)
    if len(string) <= index:
        error("Index out of string range.", Err.in_wrongStringHandling)

    set_val(args[0], Argument(_type=ArgType.string, val=string[index]))


@args_check([ArgType.var, ArgType.symb, ArgType.symb])
def SETCHAR(args):
    string = ""
    arg = args[0]
    var = get_var(arg.frame, arg.name)
    if var.type is None or var.val is None:
        error("Variable is not defined.", Err.in_varNotDefined)
    if var.type != ArgType.string:
        error("Unexpected value type.", Err.in_wrongStringHandling)
    else:
        string = var.val

    index = get_val(args[1], ArgType.int)
    char = get_val(args[2], ArgType.string)
    if len(char) < 1:
        error("Char cannot be empty", Err.in_wrongStringHandling)

    char = char[0]

    if len(string) <= index:
        error("Index out of string range.", Err.in_wrongStringHandling)

    strlist = list(string)
    strlist[index] = char
    string = "".join(strlist)
    set_val(args[0], Argument(_type=ArgType.string, val=string))

