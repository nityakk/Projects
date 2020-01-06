''' AStar.py
by  Nitya Krishna Kumar

UWNetID: nityak
Student number: 1725276

VERSION: 3.0 (modified from UCS.py)
DESCRIPTION: Assignment 3, in CSE 415, Autumn 2019.
Implements the AStar Search Algorithm.That finds a "best" solution
while also not expanding all of the nodes exhaustively.

This file Includes a priority queue implementation by 
 S. Tanimoto, Univ. of Washington.
Paul G. Allen School of Computer Science and Engineering
'''

VERBOSE = True  # Set to True to see progress; but it slows the search.

import sys

if sys.argv==[''] or len(sys.argv)<2:
  import EightPuzzleWithHamming as Problem
else:
  import importlib
  Problem = importlib.import_module(sys.argv[1])

h = Problem.h
print("\nWelcome to AStar, by Nitya Krishna Kumar!")

COUNT = None # Number of nodes expanded.
MAX_OPEN_LENGTH = None # How long OPEN ever gets.
SOLUTION_PATH = None # List of states from initial to goal, along lowest-cost path.
TOTAL_COST = None # Sum of edge costs along the lowest-cost path.
BACKLINKS = {} # Predecessor links, used to recover the path.

# The value g(s) represents the cost along the best path found so far
# from the initial state to state s.
g = {} # We will use a global hash table to associate g values with states.
f = {} # will be the cost after including 

class My_Priority_Queue:
  def __init__(self):
    self.q = [] # Actual data goes in a list.

  def __contains__(self, elt):
    '''If there is a (state, priority) pair on the list
    where state==elt, then return True.'''
    #print("In My_Priority_Queue.__contains__: elt= ", str(elt))
    for pair in self.q:
      if pair[0]==elt: 
          return True
    return False

  def delete_min(self):
    ''' Standard priority-queue dequeuing method.'''
    if self.q==[]: 
        return [] # Simpler than raising an exception.
    temp_min_pair = self.q[0]
    temp_min_value = temp_min_pair[1]
    temp_min_position = 0
    for j in range(1, len(self.q)):
      if self.q[j][1] < temp_min_value:
        temp_min_pair = self.q[j]
        temp_min_value = temp_min_pair[1]  
        temp_min_position = j
    del self.q[temp_min_position]
    return temp_min_pair

  def insert(self, state, priority):
    '''We do not keep the list sorted, in this implementation.'''
    #print("calling insert with state, priority: ", state, priority)

    if self[state] != -1:
      print("Error: You're trying to insert an element into a My_Priority_Queue instance,")
      print(" but there is already such an element in the queue.")
      return
    self.q.append((state, priority))

  def __len__(self):
    '''We define length of the priority queue to be the
    length of its list.'''
    return len(self.q)

  def __getitem__(self, state):
    '''This method enables Pythons right-bracket syntax.
    Here, something like  priority_val = my_queue[state]
    becomes possible. Note that the syntax is actually used
    in the insert method above:  self[state] != -1  '''
    for (S,P) in self.q:
      if S==state: 
          return P
    return -1  # This value means not found.

  def __delitem__(self, state):
    '''This method enables Python's del operator to delete
    items from the queue.'''
    #print("In MyPriorityQueue.__delitem__: state is: ", str(state))
    for count, (S,P) in enumerate(self.q):
      if S==state:
        del self.q[count]
        return

  def __str__(self):
    txt = "My_Priority_Queue: ["
    for (s,p) in self.q:
        txt += '('+str(s)+','+str(p)+') '
    txt += ']'
    return txt

def runAStar():
  '''This is an encapsulation of some setup before running
  AStar, plus running it and then printing some stats.'''
  initial_state = Problem.CREATE_INITIAL_STATE()
  print("Initial State:")
  print(initial_state)
  global COUNT, BACKLINKS, MAX_OPEN_LENGTH, SOLUTION_PATH
  COUNT = 0
  BACKLINKS = {}
  MAX_OPEN_LENGTH = 0
  SOLUTION_PATH = AStar(initial_state)
  print(str(COUNT)+" states expanded.")
  print('MAX_OPEN_LENGTH = '+str(MAX_OPEN_LENGTH))

def AStar(initial_state):
  '''Implements the actual A* Search Algorithm.
  Takes in a variable initial_state that contains the initial state
  of the puzzle. This state is either the default or one specified 
  by the user'''
  global g, COUNT, BACKLINKS, MAX_OPEN_LENGTH, CLOSED, TOTAL_COST
  CLOSED = My_Priority_Queue()
  BACKLINKS[initial_state] = None
  
# STEP 1a. Put the start state on a priority queue called OPEN
  OPEN = My_Priority_Queue()
  f[initial_state] = h(initial_state)
  OPEN.insert(initial_state, f[initial_state])
  
# STEP 1b. Assign g=0 to the start state.
  g[initial_state]=0.0

# STEP 2. If OPEN is empty, output "DONE" and stop.
  while (len(OPEN) > 0): 
    if VERBOSE: 
        report(OPEN, CLOSED, COUNT)
    if len(OPEN) > MAX_OPEN_LENGTH: 
        MAX_OPEN_LENGTH = len(OPEN)

# STEP 3. Select the state on OPEN having lowest priority value and call it S.
    (S,P) = OPEN.delete_min()
    CLOSED.insert(S,P)

    if Problem.GOAL_TEST(S):
        TOTAL_COST = g[S]
        path = backtrace(S)
        print(Problem.GOAL_MESSAGE_FUNCTION(S))
        # Print number of solution edges and total cost of the solution
        print('Length of solution path: '+str(len(path) - 1) + ' edges')
        print('Total cost: '+str(TOTAL_COST))
        return path
    COUNT += 1

    gs = g[S]
    #fs = g[S] + h(S)
    for operator in Problem.OPERATORS:
        if operator.precond(S):
            #new state
            news = operator.state_transf(S)
            edge_cost = S.edge_distance(news)
            new_g = gs + edge_cost #was total_cost before
            #cost value with heuristcs
            new_f = new_g + h(news)

            if (news in CLOSED):
              if (new_f < f[news]):
                  CLOSED.remove(news)
              else:
                  del news
                  continue #skip to top of for loop

            if news in OPEN:
                P = OPEN[news]
                if new_f < P:
                    del OPEN[news]
                    OPEN.insert(news, new_f)
                else:
                    del news
                    continue#skip to top of for loop
            else:
                OPEN.insert(news, new_f)

            BACKLINKS[news] = S
            # update state
            g[news] = new_g
            f[news] = new_f

  # STEP 6. Go to Step 2.
  return None  # No more states on OPEN, and no goal reached.

def print_state_queue(name, q):
  print(name+" is now: ",end='')
  print(str(q))

def backtrace(S):
  global BACKLINKS
  path = []
  while S:
    path.append(S)
    S = BACKLINKS[S]
  path.reverse()
  print("Solution path: ")
  for s in path:
    print(s)
  print (TOTAL_COST)
  return path
  
def report(open, closed, count):
  print("len(OPEN)="+str(len(open)), end='; ')
  print("len(CLOSED)="+str(len(closed)), end='; ')
  print("COUNT = "+str(count))

if __name__=='__main__':
  runAStar()

