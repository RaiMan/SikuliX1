import pyautogui
import os, subprocess
from time import sleep
from random import randint, choice
from math import ceil
from tools import remove_dups, draw_points
from multiprocessing import Process

#WORKING DIRECTORY
CWD = os.path.dirname(os.path.realpath(__file__))

#pyautogui.MINIMUM_DURATION = 0.01


# def real_click():
#   '''This function clicks the mouse with realistic errors:
#       occasional accidental right click
#       occasional double click
#       occasional no click
#   '''
#   if randint(1, 19) != 1:
#     sleep(93 / randint(83,201))
#     pyautogui.click()
#   else:
#     tmp_rand = randint(1, 3)
#     if tmp_rand == 1:
#       #double click
#       pyautogui.click()
#       sleep(randint(43, 113) / 1000)
#       pyautogui.click()
#     elif tmp_rand == 2:
#       pyautogui.click(button = 'right')


# def move_to_img(img_name, deviation, speed):
#   '''
#   This function takes the name of an input image (excluding file extension)
#   and moves the mouse to a random pixel on that image.
#
#   This advanced function saves the xdotool commands to a temporary file
#   'mouse.sh' in ./tmp/ then executes them from the shell to give clean curves
#
#   This function is very slow because it must identify the image first. It is
#   highly recommended to find the coordinates of the image in a separate thread
#   and feed this into the move() function
#   '''
#   loc = list(pyautogui.locateAllOnScreen(CWD + '/img/' + img_name + '.png'))
#   init_pos = pyautogui.position()
#
#   if loc:
#     loc = choice(loc)
#   #pick a random one from the list of all occurrences. If there is one occurence, choose that one
#   if loc:
#     x_bounds = loc[0] + randint(0, loc[2])
#     y_bounds = loc[1] + randint(0, loc[3])
#
#     if speed ==  0:
#       os.system('xdotool mousemove ' + str(x_bounds) + ' ' + str(y_bounds))
#       sleep(randint(2,9) / 100)
#       pyautogui.click()
#     else:
#       move(mouse_bez(init_pos, (x_bounds, y_bounds), deviation, speed))
#
#     return True
#   else:
#     print("Can't find location")
#     return False


def move_to_area(x, y, width, height, deviation, speed):
  '''
  Arguments same as pyautogui.locateAllOnScreen format: x and y are top left corner

  This advanced function saves the xdotool commands to a temporary file
  'mouse.sh' in ./tmp/ then executes them from the shell to give clean curves
  '''

  init_pos = pyautogui.position()

  x_coord = x + randint(0, width)
  y_coord = y + randint(0, height)

  move(mouse_bez(init_pos, (x_coord, y_coord), deviation, speed))

def mouse_bez(init_pos, fin_pos, deviation, speed):
  '''
  GENERATE BEZIER CURVE POINTS
  Takes init_pos and fin_pos as a 2-tuple representing xy coordinates
      variation is a 2-tuple representing the
      max distance from fin_pos of control point for x and y respectively
      speed is an int multiplier for speed. The lower, the faster. 1 is fastest.

  '''

  #time parameter
  ts = [t/(speed * 100.0) for t in range(speed * 101)]

  #bezier centre control points between (deviation / 2) and (deviaion) of travel distance, plus or minus at random
  control_1 = (init_pos[0] + choice((-1, 1)) * abs(ceil(fin_pos[0]) - ceil(init_pos[0])) * 0.01 * randint(deviation / 2, deviation),
               init_pos[1] + choice((-1, 1)) * abs(ceil(fin_pos[1]) - ceil(init_pos[1])) * 0.01 * randint(deviation / 2, deviation)
               )
  control_2 = (init_pos[0] + choice((-1, 1)) * abs(ceil(fin_pos[0]) - ceil(init_pos[0])) * 0.01 * randint(deviation / 2, deviation),
               init_pos[1] + choice((-1, 1)) * abs(ceil(fin_pos[1]) - ceil(init_pos[1])) * 0.01 * randint(deviation / 2, deviation)
               )

  xys = [init_pos, control_1, control_2, fin_pos]
  bezier = make_bezier(xys)
  points = bezier(ts)

  return points

def make_bezier(xys):
  # xys should be a sequence of 2-tuples (Bezier control points)
  n = len(xys)
  combinations = pascal_row(n - 1)
  def bezier(ts):
    # This uses the generalized formula for bezier curves
    # http://en.wikipedia.org/wiki/B%C3%A9zier_curve#Generalization
    result = []
    for t in ts:
      tpowers = (t**i for i in range(n))
      upowers = reversed([(1-t)**i for i in range(n)])
      coefs = [c*a*b for c, a, b in zip(combinations, tpowers, upowers)]
      result.append(
        list(sum([coef*p for coef, p in zip(coefs, ps)]) for ps in zip(*xys)))
    return result
  return bezier

def pascal_row(n):
  # This returns the nth row of Pascal's Triangle
  result = [1]
  x, numerator = 1, n
  for denominator in range(1, n//2+1):
    # print(numerator,denominator,x)
    x *= numerator
    x /= denominator
    result.append(x)
    numerator -= 1
  if n&1 == 0:
    # n is even
    result.extend(reversed(result[:-1]))
  else:
    result.extend(reversed(result))
  return result

def connected_bez(coord_list, deviation, speed):

  '''
  Connects all the coords in coord_list with bezier curve
  and returns all the points in new curve

  ARGUMENT: DEVIATION (INT)
      deviation controls how straight the lines drawn my the cursor
      are. Zero deviation gives straight lines
      Accuracy is a percentage of the displacement of the mouse from point A to
      B, which is given as maximum control point deviation.
      Naturally, deviation of 10 (10%) gives maximum control point deviation
      of 10% of magnitude of displacement of mouse from point A to B,
      and a minimum of 5% (deviation / 2)
  '''

  i = 1
  points = []

  points.append('click')
  while i < len(coord_list):
    points += mouse_bez(coord_list[i - 1], coord_list[i], deviation, speed)
    points.append('click')
    i += 1

  return points

def move(mouse_points, draw = False, rand_err = True):
  '''
  Moves mouse in accordance with a list of points (continuous curve)
  Input these as a list of points (2-tuple or another list)

  Generates file (mouse.sh) in ./tmp/ and runs it as bash file

  If you want a click at a particular point, write 'click' for that point in
  mouse_points

  This advanced function saves the xdotool commands to a temporary file
  'mouse.sh' in ./tmp/ then executes them from the shell to give clean curves

  You may wish to generate smooth bezier curve points to input into this
  function. In this case, take mouse_bez(init_pos, fin_pos, deviation, speed)
  as the argument.

  PARAMETERS:
      mouse_points
          list of 2-tuples or lists of ints or floats representing xy coords
      draw
          a boolean deciding whether or not to draw the curve the mouse makes
          to a file in /tmp/
  '''

  fname = 'mouse.sh'

  outfile = open(CWD + '/tmp/' + fname, 'w')
  os.system('chmod +x ' + CWD + '/tmp/' + fname)
  outfile.write('#!/bin/bash')
  outfile.write('\n\n')

  #draw coords to file in ./tmp/
  if draw == True:
    drawpoints = [(v[0] - REL_ORIGIN[0], v[1] - REL_ORIGIN[1]) for v in mouse_points if type(v) is not str]
    draw_points(drawpoints, width = 754, height = 503)


    #round floats to ints
  mouse_points = [[round(v) for v in x] if type(x) is not str else x for x in mouse_points]
  for coord in mouse_points:
    if coord == 'click':
      if rand_err:
        tmp = randint(1,39)
        if tmp == 1:
          outfile.write('xdotool click 3 \n')
        elif tmp == 2:
          outfile.write('xdotool click --repeat 2 1 \n')
        elif tmp in range(4, 40):   #if tmp == 4, write nothing
          outfile.write('xdotool click 1 \n') #normal click
      else:
        outfile.write('xdotool click 1 \n')
    else:
      outfile.write('xdotool mousemove ' + str(coord[0]) + ' ' + str(coord[1]) + '\n')

  outfile.close()
  subprocess.call([CWD + '/tmp/' + fname])
