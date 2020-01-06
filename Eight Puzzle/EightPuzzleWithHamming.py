# -*- coding: utf-8 -*-
'''EightPuzzleWithHamming.py
by Nitya Krishna Kumar

UWNetID: nityak
Student number: 1725276

VERSION: 1.0 (new file)
DESCRIPTION: Assignment 3, in CSE 415, Autumn 2019.
Implements the Hamming heuristic for the Eight Puzzle
Counts number of tiles in the incorrect place
'''

from EightPuzzle import *

def h(s):
    count = 0
    for i in range (3):
        for j in range (3):
            tile_num = s.b[i][j]
            if (tile_num != 0):
                if (tile_num != (3 * i + j)):
                    count += 1
    return count