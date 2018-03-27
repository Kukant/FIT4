# global variables
input_filename = ""
# parsed file - list of Instrucion instances
instructions = []
DEBUG = True
GF = dict()
TF = dict()
# stack of frames, the stack top is frames_stack[-1]
# push = append
# pop = del [-1]
frames_stack = []

data_stack = []
