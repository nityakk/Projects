'''carbon_heuristic_1.py

AUTHORS = ['Nitya Krishna Kumar, Krishna Upadhyayula']
CREATION_DATE = "28-NOV-2019"

DESCRIPTION: Defines a hueristic to solve the carbon footprint problem
'''

from carbon_footprint import *

def h(s):
    COUNTRIES = ['USA', 'China']
    
    step = 0
    
    for country in COUNTRIES:
        step = s.b['world']['delta_t'] * s.b[country]['budget']
    return step