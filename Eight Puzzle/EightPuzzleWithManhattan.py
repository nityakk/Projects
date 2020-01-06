# -*- coding: utf-8 -*-
'''EightPuzzleWithManhattan.py
by Nitya Krishna Kumar

UWNetID: nityak
Student number: 1725276

VERSION: 1.0 (new file)
DESCRIPTION: Assignment 3, in CSE 415, Autumn 2019.
This file contains the Manhattan heuristic for
the Eight Puzzle. It counts how many rows + columns
the tile is away from the correct place.    
'''

from EightPuzzle import *

def h(s):
    '''Defines a heuristic for how to find the Manhattan
    distance a tile would need to move. T'''
    #Tile Value: [Row, Column]
    pos = {0:[0,0], 1:[0,1], 2:[0,2], 
           3:[1,0], 4:[1,1], 5:[1,2],
           6:[2,0], 7:[2,1], 8:[2,2]}
    
    count = 0
    for i in range (3):
        for j in range(3):
            tile_num = s.b[i][j]
            if tile_num != 0:
                count += abs(pos[tile_num][0] - i) + abs(pos[tile_num][1] - j)
    return count