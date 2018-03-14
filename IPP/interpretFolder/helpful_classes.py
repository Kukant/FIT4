from enum import Enum

class ArgType:
    var = "var"
    string = "string"
    int = "int"
    bool = "bool"
    symb = [var, string, bool, int]
    label = "label"
    type = "type"


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


class Instruction:
    def __init__(self, order, opcode):
        self.order = order
        self.opcode = opcode
        self.args = []  # Argument list

    def args_len(self):
        return len(self.args)

    def __repr__(self):
        string = str(self.order) + ". "
        string += "opcode: " + str(self.opcode) + ", "
        string += "args: " + self.args.__repr__() + "\n"
        return string


class Argument:
    """
    Can be constant or variable or label.
    """
    def __init__(self, _type, val=None, frame=None, name=None):
        self.type = _type
        self.val = val
        self.frame = frame
        self.name = name

    def __repr__(self):
        string = "type: " + str(self.type) + ", "
        string += "val: " + str(self.val) + ", "
        string += "frame: " + str(self.frame) + ", "
        string += "name: " + str(self.name) + "\n"
        return string


class Variable:
    """
    For storing in frames, therefore it does not need name as it is its key.
    """
    def __init__(self, _type=None, val=None):
        self.type = _type
        self.val = val
