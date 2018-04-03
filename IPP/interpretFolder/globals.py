# global variables

def init():
    global instruction_num
    instruction_num = 0
    global input_filename
    input_filename = ""
    # parsed file - list of Instrucion instances
    global instructions
    instructions = []
    global DEBUG
    DEBUG = True
    global GF
    GF = dict()
    global TF
    TF = None # not initialized
    global LF
    LF = None  # not initialized
    # stack of frames, the stack top is frames_stack[-1]
    # push = append
    # pop = del [-1]
    global frames_stack
    frames_stack = []
    global data_stack
    data_stack = []
    # {"labelname": <instruction order>}
    global labels
    labels = {}
