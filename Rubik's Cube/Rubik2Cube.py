'''Rubik2Cube.py
'''
#<METADATA>
QUIET_VERSION = "0.1"
PROBLEM_NAME = "Rubik2Cube"
PROBLEM_VERSION = "0.1"
PROBLEM_AUTHORS = ['Nitya Krishna Kumar']
PROBLEM_CREATION_DATE = "18-OCT-2019"
PROBLEM_DESC=\
'''This is the 2X2X2 Rubiks Cube. 
Modified from EightPuzzle.py
'''
# Sides
F = 0
B = 1
U = 2
D = 3
L = 4
R = 5

#Adjacencies
ADJACENCY = {F: [L,D,R,U],
             B: [R,D,L,U],
             U: [L,F,R,B],
             D: [L,B,R,F],
             L: [B,D,F,U],
             R: [F,D,B,U]}

#Four Cubes
UL = 0;
UR = 1;
LL = 2;
LR = 3;



class State:
  def __init__(self, b):
    self.b = b

  def __eq__(self,s2):
    for i in range(6):
      for j in range(4):
        if self.b[i][j] != s2.b[i][j]:
            return False
    return True

  def __str__(self):
    # Produces a textual description of a state.
    # Might not be needed in normal operation with GUIs.
    return str(self.b)

  def __hash__(self):
    return (self.__str__()).__hash__()

  def copy(self):
    # Performs an appropriately deep copy of a state,
    # for use by operators in creating new states.
    news = State({})
    news.b = [row[:] for row in self.b]
    return news

  def find_void_location(self):
    '''Return the (vi, vj) coordinates of the void.
    vi is the row index of the void, and vj is its column index.'''
    for i in range(3):
      for j in range(3):
        if self.b[i][j]==0:
          return (i,j)
    raise Exception("No void location in state: "+str(self))

  def can_move(self,dir):
    '''Checks whether it's legal to move a tile that is next
       to the void in the direction given.
       All moves okay for rubiks cube'''
       
    return True

  def move(self,dir):
    '''Assuming it's legal to make the move, this computes
       the new state resulting from moving a tile in the
       given direction, into the void.'''
    news = self.copy() # start with a deep copy.
    
    face = ''
    if dir == 'F':
        face = F
    if dir == 'B':
        face = B
    if dir == 'U':
        face = U
    if dir == 'D':
        face = D
    if dir == 'L':
        face = L
    if dir == 'R':
        face = R
    
    
    b = news.b[face]
    temp = b[0]
    b[0] = b[2]
    b[2] = b[3]
    b[3] = b[1]
    b[1] = temp
    news.b[face] = b
    
    adjacent = ADJACENCY[face]
    
    l_second = news.b[adjacent[0]][1]
    l_third = news.b[adjacent[0]][3]
    
    news.b[adjacent[0]][1] = news.b[adjacent[1]][0]
    news.b[adjacent[0]][3] = news.b[adjacent[1]][1]
    news.b[adjacent[1]][0] = news.b[adjacent[2]][2]
    news.b[adjacent[1]][1] = news.b[adjacent[2]][0]
    news.b[adjacent[2]][2] = news.b[adjacent[3]][3]
    news.b[adjacent[2]][0] = news.b[adjacent[3]][2]
    news.b[adjacent[3]][3] = l_second
    news.b[adjacent[3]][2] = l_third
        
    return news # return new state

  def edge_distance(self, s2):
    return 1.0  # Warning, this is only correct when
    # self and s2 are neighboring states.
    # We assume that is the case.  This method is
    # provided so that problems having all move costs equal to
    # don't have to be handled as a special case in the algorithms.
  
def goal_test(s):
  '''If all the b values are in order, then s is a goal state.'''
  for i in range(6):
      color = s.b[i][0]
      for j in range(1,4):
          curr_color = s.b[i][j]
          if color != curr_color:
              return False
  return True
      

def goal_message(s):
  return "You've got it right! Great!"

class Operator:
  def __init__(self, name, precond, state_transf):
    self.name = name
    self.precond = precond
    self.state_transf = state_transf

  def is_applicable(self, s):
    return self.precond(s)

  def apply(self, s):
    return self.state_transf(s)
#</COMMON_CODE>

#<INITIAL_STATE>
  # Use default, but override if new value supplied
             # by the user on the command line.
try:
  import sys
  init_state_string = sys.argv[2]
  print("Initial state as given on the command line: "+init_state_string)
  init_state_list = eval(init_state_string)
except:
  init_state_list = [['RED', 'YELLOW', 'RED', 'YELLOW'],
                     ['WHITE', 'ORANGE', 'WHITE', 'ORANGE'],
                     ['ORANGE', 'ORANGE','YELLOW','YELLOW'],
                     ['WHITE', 'WHITE', 'RED', 'RED'], 
                     ['BLUE', 'BLUE', 'BLUE', 'BLUE'],
                     ['GREEN', 'GREEN', 'GREEN', 'GREEN']]
  print("Using default initial state list: "+str(init_state_list))


CREATE_INITIAL_STATE = lambda: State(init_state_list)
#</INITIAL_STATE>

#<OPERATORS>
directions = ['F','B','U','D','L','R']
OPERATORS = [Operator("Move a tile "+str(dir)+" into the void",
                      lambda s,dir1=dir: s.can_move(dir1),
                      # The default value construct is needed
                      # here to capture the value of dir
                      # in each iteration of the list comp. iteration.
                      lambda s,dir1=dir: s.move(dir1) )
             for dir in directions]
#</OPERATORS>

#<GOAL_TEST> (optional)
GOAL_TEST = lambda s: goal_test(s)
#</GOAL_TEST>

#<GOAL_MESSAGE_FUNCTION> (optional)
GOAL_MESSAGE_FUNCTION = lambda s: goal_message(s)
#</GOAL_MESSAGE_FUNCTION>