'''carbon_heuristic_2.py

AUTHORS = ['Nitya Krishna Kumar, Krishna Upadhyayula']
CREATION_DATE = "28-NOV-2019"

DESCRIPTION: Defines a hueristic to solve the carbon footprint problem
'''

from carbon_footprint import *

def h(s):
    COUNTRIES = ['USA', 'China']
    
    step = 0
    
    for country in COUNTRIES:
        step = 1000 * s.b['world']['delta_t'] 
        + 100 * s.b['world']['carbon'] 
        + s.b[country]['budget']
    return step