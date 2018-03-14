
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
        if var.name in GF.keys():
            error("Variable " + var.name + " already defined", Err.semantic)
        GF[var.name] = Variable()
    elif var.frame == "TF":
        if var.name in TF.keys():
            error("Variable " + var.name + " already defined", Err.semantic)
        TF[var.name] = Variable()
        pass
    elif var.frame == "LF":
        pass  # TODO


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
    op1 = args[1]
    op2 = args[2]
    operation = args[3]
    res = 0

    set_val(var, Argument(_type=ArgType.int, val=res))


