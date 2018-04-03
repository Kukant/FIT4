
import getopt
import sys
from helpful_classes import *
import globals as g
import xml.etree.ElementTree as ET
import re


def dbgp(msg):
    if g.DEBUG:
        print("DEBUG" + str(msg))

def args_check(types):
    """
    Decorator for checking args len
    :param types: array of expected types, put there None for variable argument
    :return:
    """
    def real_decorator(f):
        def wrapper(args):
            n = len(types)
            if len(args) != n:
                error("Unexpected arguments len. in " + f.__name__ + "() expected "
                      + str(n) + " got: " + str(len(args)), Err.lexOrSyn)
            for i, type in enumerate(types):
                if type is None:
                    continue
                if type == ArgType.symb:
                    if args[i].type not in ArgType.symb:
                        error("Unexpected arg type in " + f.__name__ + "() expected symb, got: " +
                              args[i].type, Err.lexOrSyn)
                elif type != args[i].type:
                    error("Unexpected arg type in " + f.__name__ + "() expected " + type +
                          ", got: " + args[i].type, Err.lexOrSyn)
            f(args)
        return wrapper
    return real_decorator


def error(msg, exitCode):
    print("Error: ", msg)
    exit(exitCode.value)


def print_help():
    print(
        "This program interprets IPPcode2018 structured into XML file\n",
        "Author: Tomas Kukan, 12. 1. 2018\n",
        "Options: --help : prints help.\n",
        "         --file=<filename> : input XML file -required"
        )


def get_opts():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hf:", ["help", "file="])
    except getopt.GetoptError as err:
        print(err)
        sys.exit(10)

    if len(args) > 0 or len(opts) != 1:
        error("Wrong parameters.", Err.args)

    for o, a in opts:
        if o in ("-h", "--help"):
            print_help()
            sys.exit()
        elif o in ("-f", "--file"):
            g.input_filename = a
        else:
            assert False, "unhandled option"


def parse_xml_file():
    tree = None
    try:
        tree = ET.parse(g.input_filename)
    except ET.ParseError:
        error("Exception while parsing file: " + g.input_filename, Err.parsingFile)
    except FileNotFoundError:
        error("Exception while opening file: " + g.input_filename, Err.openingFile)

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

    instruction_count = 1
    for instruction in root:
        # test instruction
        if instruction.tag != "instruction":
            error("Expected tag instruction, got: " + instruction.tag, Err.parsingFile)
        if tuple(instruction.attrib.keys()) != tuple(["order", "opcode"]):
            error("Instruction attributes are not valid." + tuple(instruction.attrib.keys()).__str__(), Err.parsingFile)


        try:
            order = int(instruction.attrib["order"])
        except ValueError:
            error("Order is not integer!", Err.parsingFile)

        if instruction_count != order:
            error("Wrong instruction order number!", Err.parsingFile)
        instruction_count += 1
        to_append = Instruction(order=order, opcode=instruction.attrib["opcode"])

        # test argument
        for i, arg in enumerate(instruction):
            if "arg"+str(i + 1) != arg.tag:
                error("Unexpected argument tag: " + arg.tag, Err.parsingFile)
            if tuple(arg.attrib.keys()) != tuple(["type"]):
                error("Argument attributes are not valid.", Err.parsingFile)

            to_append.args.append(parse_arg(_type=arg.attrib["type"], val=arg.text))

        g.instructions.append(to_append)
        # handle labels
        last = g.instructions[-1]
        if last.opcode == "LABEL":
            if len(last.args) != 1:
                error("LABEL got unexpected args number", Err.lexOrSyn)
            if last.args[0].type != ArgType.label:
                error("LABEL got unexpected arg type", Err.lexOrSyn)
            g.labels[last.args[0].val] = last.order




## OTHER FUNCTIONS


def parse_arg(_type, val):
    """
    Returns Argument instance
    :param _type:
    :param val:
    :return:
    """
    if _type == "var":
        return parse_var(val)
    elif _type in ["string", "int", "bool", "type", "label"]:
        return Argument(_type=_type, val=parse_val(val, _type))
    else:
        error("unexpected param type: " + _type, Err.lexOrSyn)


def parse_var(var):
    split = var.split("@", 1)
    if len(split) != 2:
        error("Wrong variable format.", Err.lexOrSyn)

    frame = split[0]
    name = split[1]

    if frame != "GF" and frame != "LF" and frame != "TF":
        error("Unknown variable frame: " + frame, Err.lexOrSyn)

    return Argument(frame=frame, name=name, _type=ArgType.var)


def get_var(frame, name):
    if frame == "GF":
        if name in g.GF.keys():
            return g.GF[name]
    elif frame == "TF":
        if g.TF is None:
            error("Frame TF is not initialized.", Err.in_nonExistingFrame)
        if name in g.TF.keys():
            return g.TF[name]
    elif frame == "LF":
        if g.LF is None:
            error("Frame LF is not initialized.", Err.in_nonExistingFrame)
        if name in g.LF.keys():
            return g.LF[name]
    else:
        error("Unknown variable frame: " + frame, Err.lexOrSyn)

    error("Variable does not exist.", Err.in_nonExistingVar)


def get_val(arg, expected_type):
    if arg.type == "var":
        var = get_var(arg.frame, arg.name)
        if var.type is None or var.val is None:
            error("Variable is not defined.", Err.in_varNotDefined)
        if var.type != expected_type:
            error("Unexpected value type.", Err.in_wrongOperand)
        else:
            return var.val
    else:
        if arg.type != expected_type:
            error("Unexpected value type.", Err.in_wrongOperand)
        else:
            return arg.val

def set_val(dst, src):
    dest_var = get_var(dst.frame, dst.name)
    if src.type == ArgType.var:
        src = get_var(src.frame, src.name)
        if src.type is None or src.val is None:
            error("Variable is not defined.", Err.in_varNotDefined)

    if dest_var.type is None: # was not defined yet
        dest_var.type = src.type
        dest_var.val = src.val
    elif dest_var.type != src.type:
        error("Cannot move value of type " + src.type + " to type " + dest_var.type, Err.in_wrongOperand)
    else:
        dest_var.val = src.val


def parse_val(val, type):
    if not re.match(r'^[^\s#]*$', val):
        error("Value of argument is not valid: " + val, Err.lexOrSyn)

    if type == ArgType.string:
        if not re.match(r'^([^\\]|\\[0-9]{3})*$', val):
            error("String is not valid: " + val, Err.lexOrSyn)
        for match in set(re.findall(r'(?<=\\)[0-9]{3}', val)):
            val = val.replace("\\" + str(match), chr(int(match)))
    elif type == ArgType.bool:
        val = True if str(val).upper() == "TRUE" else False
    elif type == ArgType.int:
        val = int(val)
    elif type == ArgType.label:
        pass
    elif type == ArgType.type:
        if val not in ["int", "string", "bool"]:
            error("Type not valid " + val, Err.lexOrSyn)
    else:
        dbgp("parse_val got unknown type " + type)

    return val



