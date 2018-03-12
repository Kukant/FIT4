
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

