'''carbon_footprint.py
'''
#<METADATA>
QUIET_VERSION = "0.1"
PROBLEM_NAME = "Carbon Footprint"
PROBLEM_VERSION = "0.1"
PROBLEM_AUTHORS = ['Nitya Krishna Kumar, Krishna Upadhyayula']
PROBLEM_CREATION_DATE = "24-NOV-2019"
PROBLEM_DESC=\
'''
This is a problem formulation for the carbon footprint wicked problem. It
primarily concentrates on actions that the US and China can take to reduce
their carbon dioxide emmisions and also decrease overall surface temperature
of the earth.
'''
#</METADATA>

#<COMMON_DATA>
FACTORS = ['carbon', 'delta_t', 'budget']

ACTIONS = ['plant trees',
           'implement CO2 direct capture techniques', 
           'implement composting policy',
           'convert to renewable energy source',
           'start export policy',
           'end import policy']

COUNTRIES = ['USA', 'China']
#</COMMON_DATA>

#<COMMON_CODE>
class State:
    def default(self):
        new_state = {}
        default_vals = {}
        
        # source: https://www.climate.gov
        default_vals['carbon'] = 400 # measured in ppm
        
        # increase in average surface temerature since 1880
        # source: https://www.climate.gov/
        default_vals['delta_t'] = 0.8
        new_state['world'] = default_vals
        for country in COUNTRIES:
            USA_country_vals = {}
            China_country_vals = {}
            if country == 'USA':
                USA_country_vals['budget'] = 21000 # in billions
                new_state['USA'] = USA_country_vals
            if country == 'China':
                China_country_vals['budget'] = 13000
                new_state['China'] = China_country_vals
        return new_state
    
    def __init__(self, b):
        # default state
        news = self.default()
        # check if default or not
        default = False
        if len(b) == len(COUNTRIES):
            news['world']['carbon'] = b[0][0]
            news['world']['delta_t'] = b[0][1]
            
            for country in COUNTRIES:
                if len(b[0]) == len(FACTORS) and len(b[1]) == len(FACTORS):
                    for i in range(len(FACTORS)):
                        if i < 3:
                            news['world'][FACTORS[i]] = b[0][i]
                        else:
                            if country == 'USA':
                                news[country][FACTORS[i]] = b[0][i]
                            else:
                                news[country][FACTORS[i]] = b[1][i]
                else:
                    default = True
        else:
            default = True
        
        if not default:
            self.b = news
        else:
            self.b = self.default()

    def __eq__(self,s2):
        for i in self.b:
            if self.b[i] != s2.b[i]:
                return False
            for j in self.b[i]:
                if self.b[i][j] != s2.b[i][j]: 
                    return False
        return True
    
    def __str__(self):
        txt = "\n["
        for i in self.b:
            txt += str(self.b[i])+"\n "
        return txt[:-2]+"]"

    def __hash__(self):
        return (self.__str__()).__hash__()
    
    def copy(self):
        # Performs an appropriately deep copy of a state,
        # for use by operators in creating new states.
        news = State([[]])
        news.b['world']['carbon'] = self.b['world']['carbon']
        news.b['world']['delta_t'] = self.b['world']['delta_t']
        #news.b['world']['time_left'] = self.b['world']['time_left']
        for country in COUNTRIES:
            news.b[country]['budget'] = self.b[country]['budget']
            #news.b[country]['land'] = self.b[country]['land']
            #news.b[country]['political_stability'] = self.b[country]['political_stability']
        return news
    
    def get_action_choice(self, action):
        action_choice = 0
        if action == 'plant trees':
            action_choice = 1
        elif action == 'implement CO2 direct capture techniques':
            action_choice = 2
        elif action == 'implement composting policy':
            action_choice = 3
        elif action == 'convert to renewable energy source':
            action_choice = 4
        elif action == 'start export policy':
            action_choice = 5
        elif action == 'end import policy':
            action_choice = 6
        
        return action_choice
    
    def can_move(self, move):
        '''Tests whether it's legal to move a tile that is next
        to the void in the direction given.'''
        
        move_values = move.split(' ')
        country = move_values[0]
        action = move_values[1:len(move_values)]
        action = ' '.join(action)
        
        action_choice =self.get_action_choice(action)
        
        if action_choice == 0:
            return False
        
        delta_t = self.b['world']['delta_t']
        carbon_level = self.b['world']['carbon']
        budget = self.b[country]['budget']
        
        if action_choice == 1:
            if carbon_level < 350 and delta_t <= 1 and budget <= 10000:
                    return True
        if action_choice == 2:
            if ((carbon_level >= 350 and carbon_level < 450 and budget > 10000) 
            or (carbon_level >= 450 and delta_t >= 1 and budget > 10000)):
                    return True
        if action_choice == 3:
            if ((carbon_level <= 300 or carbon_level >= 450) 
            and delta_t > 1 and budget <= 10000):
                    return True
        if action_choice == 4:
            if ((carbon_level < 350 and budget > 10000) 
            or (carbon_level >= 450 and delta_t <= 1 and budget > 10000)):
                    return True
        if action_choice == 5:
            if (carbon_level >= 350 and carbon_level < 450 
                and delta_t > 1 and budget <= 10000):
                    return True
        if action_choice == 6:
            if (carbon_level >= 350 and carbon_level < 450 
                and delta_t <= 1 and budget <= 10000):
                    return True
        return False
        
        
    def move(self, move):
        news = self.copy() # start with a deep copy.
        
        move_values = move.split(' ')
        country = move_values[0]
        action = move_values[1:len(move_values)]
        action = ' '.join(action)
        
        action_choice = self.get_action_choice(action)
        if action_choice == 1:
            news.b['world']['carbon'] -= 50
            news.b['world']['delta_t'] -= 0.2
            news.b[country]['budget'] -= 3000
        elif action_choice == 2:
            news.b['world']['carbon'] -= 150
            news.b['world']['delta_t'] -= 0.2
            news.b[country]['budget'] -= 8000 
        elif action_choice == 3:
            news.b['world']['carbon'] -= 20
            news.b['world']['delta_t'] -= 0.1
        elif action_choice == 4:
            news.b['world']['carbon'] -= 100
            news.b['world']['delta_t'] -= 0.4
            news.b[country]['budget'] -= 4500
        elif action_choice == 5:
            news.b['world']['carbon'] += 80
            news.b['world']['delta_t'] += 0.1
            news.b[country]['budget'] += 2000
        elif action_choice == 6:
            news.b['world']['carbon'] -= 50
            news.b[country]['budget'] += 3000   
        return news # return new state

    def edge_distance(self, s2):
        return 1.0

def goal_test(s):
    '''If all the b values are in order, then s is a goal state.'''
    if s.b['world']['carbon'] <= 280 and s.b['world']['delta_t'] <= 0.05:
        return True
    return False

def goal_message(s):
  return "You have successfully decreased the US + China Carbon Footprint!"

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
    init_state_list = [[400, 0.8, 21000], [400, 0.8, 13000]]
    print("Using default initial state list:" + str(init_state_list))
    print(" (To use a specific initial state, enter it on the command line, " \
             "with parameters in the following order: ['carbon', 'delta_t', 'budget']. " \
             "Note that carbon and delta_t should be the same for both countries. e.g.,")
    print("python3 UCS.py carbon_footprint '[[400, 0.8, 21000], [400, 0.8, 13000]]'")

CREATE_INITIAL_STATE = lambda: State(init_state_list)
#</INITIAL_STATE>

#<OPERATORS>
ACTION_SPACE = [(country + " " + action) for country in COUNTRIES for action in ACTIONS]
OPERATORS = [Operator(a,
                      lambda s, a1=a: s.can_move(a1),
                      # The default value construct is needed
                      # here to capture the value of dir
                      # in each iteration of the list comp. iteration.
                      lambda s, a1=a: s.move(a1))
             for a in ACTION_SPACE]
#</OPERATORS>

#<GOAL_TEST>
GOAL_TEST = lambda s: goal_test(s)
#</GOAL_TEST>

#<GOAL_MESSAGE_FUNCTION>
GOAL_MESSAGE_FUNCTION = lambda s: goal_message(s)
#</GOAL_MESSAGE_FUNCTION>

