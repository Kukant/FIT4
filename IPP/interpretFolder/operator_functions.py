
from other_functions import *
from globals import *

# OPERATOR FUNCTIONS


@args_check([ArgType.symb])
def WRITE(args):
    arg = args[0]
    if arg.type == ArgType.var:
        var = get_var(arg.frame, arg.name)
        to_print = {"type": var.type, "value": var.val}
    else:
        to_print = {"type": arg.type, "value": arg.val}

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
    global GF
    global TF
    global LF
    var = args[0]
    if var.type != "var":
        error("DEFVAR expected var, got: " + var.type, Err.lexOrSyn)

    if var.frame == "GF":
        if var.name in GF.keys():
            error("Variable " + var.name + " already defined in GF", Err.semantic)
        GF[var.name] = Variable()
    elif var.frame == "TF":
        if var.name in TF.keys():
            error("Variable " + var.name + " already defined in TF", Err.semantic)
        TF[var.name] = Variable()
    elif var.frame == "LF":
        if var.name in LF.keys():
            error("Variable " + var.name + " already defined in LF", Err.semantic)
        GF[var.name] = Variable()


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

    set_val(var, Argument(_type=ArgType.int, val=res))


@args_check([ArgType.var, ArgType.symb, ArgType.symb, None])
def COMPARE(args):
    var = args[0]
    operand_types = args[1].type
    if operand_types not in [ArgType.int, ArgType.string, ArgType.bool]:
        error("Unexpected operand type", Err.in_wrongOperand)
    op1 = get_val(args[1], operand_types)
    op2 = get_val(args[2], operand_types)
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

    set_val(var, Argument(_type=ArgType.bool, val=res))


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

    set_val(var, Argument(_type=ArgType.bool, val=res))

@args_check([ArgType.var, ArgType.symb])
def NOT(args):
    var = args[0]
    op1 = get_val(args[1], ArgType.bool)
    res = not op1
    set_val(var, Argument(_type=ArgType.bool, val=res))


@args_check([])
def CREATEFRAME(args):
    global TF
    TF = {}

@args_check([])
def PUSHFRAME(args):
    global TF
    global LF
    frames_stack.append(TF)
    LF = TF
    TF = None

@args_check([])
def POPFRAME(args):
    global TF
    global LF
    if len(frames_stack) < 1:
        error("Frame stack is empty.", Err.in_nonExistingFrame)
    TF = frames_stack[-1]
    del frames_stack[-1]
    LF = None if len(frames_stack) < 1 else frames_stack[-1]


@args_check([ArgType.symb])
def PUSHS(args):
    global data_stack
    data_stack.append(args[0])


@args_check([ArgType.var])
def POPS(args):
    global data_stack
    var = args[0]
    if len(data_stack) < 1:
        error("Data stack is empty.", Err.semantic)
    set_val(var, data_stack[-1])
    del data_stack[-1]


