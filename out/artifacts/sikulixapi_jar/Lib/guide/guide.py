from sikuli import *

from org.sikuli.script import Pattern
from org.sikuli.script import Region
from org.sikuli.basics import Debug

from org.sikuli.guide import Guide

from org.sikuli.guide import Visual
from org.sikuli.guide.Visual import Layout

from org.sikuli.guide import SxAnchor
from org.sikuli.guide import SxArea
from org.sikuli.guide import SxArrow
from org.sikuli.guide import SxBracket
from org.sikuli.guide import SxButton
from org.sikuli.guide import SxCallout
from org.sikuli.guide import SxCircle
from org.sikuli.guide import SxClickable
from org.sikuli.guide import SxFlag
from org.sikuli.guide import SxHotspot
from org.sikuli.guide import SxImage
from org.sikuli.guide import SxRectangle
from org.sikuli.guide import SxSpotlight
from org.sikuli.guide import SxText

_g = Guide()

#######################
#      Core API       #
#######################

#================
# Basic Elements
#================

def rectangle(target, **kwargs):
    comp = _g.rectangle()
    return _addComponentHelper(comp, target, side = 'over',  **kwargs)

def circle(target, **kwargs):
    comp = _g.circle()
    return _addComponentHelper(comp, target, side = 'over', **kwargs)

def text(target, txt, **kwargs):
    comp = _g.text(txt)
    s = kwargs.pop("side", 'right')
    f = kwargs.pop("fontsize", 16)
    return _addComponentHelper(comp, target, side = s, fontsize = f, **kwargs)

def tooltip(target, txt, **kwargs):
    return text(target, txt, fontsize = 8, **kwargs)

def flag(target, txt, **kwargs):
    comp = _g.flag(txt)
    s = kwargs.pop("side", 'right')
    f = kwargs.pop("fontsize", 12)
    return _addComponentHelper(comp, target, side = s, fontsize = f, **kwargs)

def callout(target, txt, **kwargs):
    comp = _g.callout(txt)
    s = kwargs.pop("side", 'right')
    f = kwargs.pop("fontsize", 16)
    return _addComponentHelper(comp, target, side = s, fontsize = f, **kwargs)

def image(target, img, **kwargs):
    comp = _g.image(img)
    return _addComponentHelper(comp, target, side = "over", **kwargs)

def bracket(target, **kwargs):
    comp = _g.bracket()
    s = kwargs.pop("side", 'right')
    return _addComponentHelper(comp, target, side = s, **kwargs)

def arrow(src, dest, **kwargs):
    comp = _g.arrow(src, dest)
    return _addComponentHelper(comp, None, **kwargs)

#----------- not yet checked

def spotlight(target, **kwargs):
    comp = SxSpotlight(None)
    shp = kwargs.pop("shape", 'circle')
    if shp == 'rectangle':
        comp.setShape(SxSpotlight.RECTANGLE)
    elif shp == 'circle':
        comp.setShape(SxSpotlight.CIRCLE)
    s = kwargs.pop("side", 'over')
    return _addComponentHelper(comp, target, side = s, **kwargs)

#=====================
# Interactive Elements
#=====================

def button(target, name, **kwargs):
    comp = _g.button(name)
    s = kwargs.pop("side", 'bottom')
    return _addComponentHelper(comp, target, side = s, **kwargs)

def addCloseButton(target):
    button(target, "Close", side="left", offset = (200,0))
def addNextButton(target):
    button(target, "Next", side="left", offset = (60,0))
def addPreviousButton(target):
    button(target, "Previous", side ="left", offset = (0,0))

#----------- not yet checked

def clickable(target, name = "", **kwargs):
    comp = SxClickable(None)
    comp.setName(name)
    return _addComponentHelper(comp, target, side = 'over', **kwargs)

def hotspot(target, message, side = 'right'):
    # TODO allow hotspot's positions to be automatically updated
    r = _getRegionFromTarget(target)
    txtcomp = SxCallout(message)
    r1 = Region(r)
    r1.x -= 10
    r1.w += 20
    _setLocationRelativeToRegion(txtcomp,r1,side)
    txtcomp.setShadow(10,2)
    comp = SxHotspot(r, txtcomp, _g)
    _g.addToFront(comp)
    return comp

#=====================
# Positioning Elements
#======================

def anchor(target):
    if isinstance(target, Pattern):
        pattern = target
    elif isinstance(target, str):
        pattern = Pattern(target)
    comp = SxAnchor(pattern)
    _g.addToFront(comp)
    return comp

def area(targets):
    patterns = [Pattern(target) for target in targets]
    comp = SxArea()
    for pattern in patterns:
        anchor = SxAnchor(pattern)
        _g.addToFront(anchor)
        comp.addLandmark(anchor)
    _g.addToFront(comp)
    return comp


####################
# Helper functions #
####################

"""
positional parameters:
target    the element, that defines the place, where to show this element (region or other element)
txt       text to be shown in the element
name      the name of a button
src, dest the targets for the arrow: points from src to dest

optional keyword parameters (not every keyword might be supported by every element):
side = "layout"             where to place the element related to others
margin = value              space added around the element
offset = (x_value, y_value) offset added to the standard top left of the element
font = "fontname"
fontsize = value
width = value

color keywords
back = colorSpec  fill color for the element
frame = colorSpec color of a border
text = colorSpec  color of contained text
front = colorSpec color of element aspects other than back or frame

layout variants:
right  position to the right
left   position to the left
top    position above (towards upper screen edge)
bottom position below (towards lower screen edge)
over   position on top centered (eventually hiding all or parts of the target)

colorSpec:
(R,G,B) integer list as RGB spec (0 .. 255)
"""

def _addComponentHelper(comp, target, side = 'best', margin = 0, offset = (0,0), 
                        horizontalalignment = 'center', verticalalignment = 'center', 
                        font = None, fontsize = 0, width = 0,
                        shadow = 'default', front = None, back = None, frame = None, text = None):

    # set the component's colors
    comp.setColors(front, back, frame, text)
    
    # set the component's font
    comp.setFont(font, fontsize)
    
    # set the components width
    if width > 0: comp.setMaxWidth(width)
    
    # Margin
    if margin:
        if isinstance(margin, tuple):
            (dt,dl,db,dr) = margin
        else:
            (dt,dl,db,dr) = (margin,margin,margin,margin)
        comp.setMargin(dt,dl,db,dr)

    # Offset
    if offset:
        (x,y) = offset
        comp.setOffset(x,y)

    # Side
    sideConstant = None;
    if (side == 'right'):
        sideConstant = Layout.RIGHT
    elif (side == 'top'):
        sideConstant = Layout.TOP
    elif (side == 'bottom'):
        sideConstant = Layout.BOTTOM
    elif (side == 'left'):
        sideConstant = Layout.LEFT
    elif (side == 'inside'):
        sideConstant = Layout.INSIDE
    elif (side == 'over'):
        sideConstant = Layout.OVER

    # Alignment
#    if (horizontalalignment == 'left'):
#        comp.setHorizontalAlignmentWithRegion(r,0.0)
#    elif (horizontalalignment == 'right'):
#   if (verticalalignment == 'top'):
#       comp.setVerticalAlignmentWithRegion(r,0.0)
#   elif (verticalalignment == 'bottom'):
#        comp.setVerticalAlignmentWithRegion(r,1.0)

    # target and position
    if target:
      comp.setTarget(target)
    if sideConstant:
      comp.setLayout(sideConstant)

#    if isinstance(target, Region):
#        # absolute location wrt a Region
#        comp.setLocationRelativeToRegion(target, sideConstant)
#    elif isinstance(target, tuple):
#        # absolute location wrt a point (specified as (x,y))
#        comp.setLocationRelativeToRegion(Region(target[0], target[1],1,1), Layout.RIGHT)
#    else:
#        targetComponent = None
#        if isinstance(target, str):
#            # relative location to a string (image filename)
#            targetComponent = anchor(Pattern(target))
#            targetComponent.setOpacity(0)
#        elif isinstance(target, Pattern):
#            # relative location to a pattern
#            targetComponent = anchor(target)
#            targetComponent.setOpacity(0)
#        elif isinstance(target, Visual):
#            targetComponent = target
#        if targetComponent:
#            comp.setLocationRelativeToComponent(targetComponent, sideConstant)
#        else:
#            Debug.error("GuideComponentSetup: invalid target: ", target)
#            return None

    # set shadow, different sizes for different types of components
#TODO shadow handling
    if shadow == 'default':
        if (isinstance(comp, SxCircle) or \
                isinstance(comp, SxRectangle) or \
                isinstance(comp, SxBracket)):
            comp.setShadow(5,2)
        elif not (isinstance(comp, SxSpotlight)):
            comp.setShadow(10,2)

    # add the component to guide
    comp.updateComponent()
#    _g.addToFront(comp)
    return comp

#=====================
# Show the Elements
#=====================

def show(arg = None, timeout = 5):
    global _g
    cmd = ""
    # show a list of steps
    if isinstance(arg, list) or isinstance(arg, tuple):
        _show_steps(arg, timeout)
    # show a single step
    elif callable(arg):
        arg()
        cmd = _g.showNow(timeout)
    # show for some period of time
    elif isinstance(arg, float) or isinstance(arg, int):
        cmd = _g.showNow(arg)
    # show
    else:
        cmd = _g.showNow()
    _g = Guide()
    return cmd

def setDefaultTimeout(timeout):
    _g.setDefaultTimeout(timeout)

# showing steps, that are defined in a list of functions
def _show_steps(steps, timeout = None):
    # only keep callables
    steps = filter(lambda x: callable(x), steps)
    print steps
    n = len(steps)
    i = 0
    while True:
        step = steps[i]
        step()
        msg = "Step %d of %d" % (i+1, n)
        a = rectangle(Region(100,100,0,0))
        text((10,50), msg, fontsize = 10)
        if n == 1: # only one step
            addCloseButton(a)
        elif i == 0: # first step
            addNextButton(a)
            addCloseButton(a)
        elif i < n - 1: # between
            addPreviousButton(a)
            addNextButton(a)
            addCloseButton(a)
        elif i == n - 1: # final step
            addPreviousButton(a)
            addCloseButton(a)
        ret = _g.showNow()
        if (ret == "Previous" and i > 0):
            i = i - 1
        elif (ret == "Next" and i < n - 1):
            i = i + 1
        elif (ret == None and i < n - 1): # timeout
            i = i + 1
        elif (ret == "Close"):
            return
        else:
            # some other transitions happened
            if (i < n - 1):
                i = i + 1
            else:
                return

#########################
# Cursor Enhancement    #
#########################

def beam(target):
    r = s.getRegionFromTarget(target)
    c = _g.addBeam(r)
    return c

def magnet(arg):
    m = Magnet(_g)
    def addTarget(x):
        if (isinstance(x, Pattern)):
            pattern = x
        elif (isinstance(x, str)):
            pattern = Pattern(x)
        m.addTarget(pattern)
    if isinstance(arg, list) or isinstance(arg, tuple):
        for x in arg:
            addTarget(x)
    else:
        addTarget(x)
    _g.addTransition(m)


def _setLocationRelativeToRegion(comp, r_, side='left', offset=(0,0), expand=(0,0,0,0), \
                                 horizontalalignment = 'center', \
                                 verticalalignment = 'center'):
    r = Region(r_)
    # Offset
    (dx,dy) = offset
    r.x += dx
    r.y += dy
    # Side
    if (side == 'right'):
        comp.setLocationRelativeToRegion(r, Layout.RIGHT);
    elif (side == 'top'):
        comp.setLocationRelativeToRegion(r, Layout.TOP);
    elif (side == 'bottom'):
        comp.setLocationRelativeToRegion(r, Layout.BOTTOM);
    elif (side == 'left'):
        comp.setLocationRelativeToRegion(r, Layout.LEFT);
    elif (side == 'inside'):
        comp.setLocationRelativeToRegion(r, Layout.INSIDE);
    # Alignment
    if (horizontalalignment == 'left'):
        comp.setHorizontalAlignmentWithRegion(r,0.0)
    elif (horizontalalignment == 'right'):
        comp.setHorizontalAlignmentWithRegion(r,1.0)
    if (verticalalignment == 'top'):
        comp.setVerticalAlignmentWithRegion(r,0.0)
    elif (verticalalignment == 'bottom'):
        comp.setVerticalAlignmentWithRegion(r,1.0)

def _getRegionFromTarget(target):
    if isinstance(target, Visual):
        return Region(target.getBounds())
    else:
        return Screen().getRegionFromTarget(target)

"""
# RaiMan currently not used
#
def _addSideComponentToTarget(comp, target, **kwargs):
    r = _getRegionFromTarget(target)
    _setLocationRelativeToRegion(comp,r,**kwargs)
    if isinstance(target, str):
        _g.addTracker(Pattern(target),r,comp)
    elif isinstance(target, Pattern):
        _g.addTracker(target,r,comp)
    elif isinstance(target, Visual):
        target.addFollower(comp)
    _g.addComponent(comp)
    return comp

def _addAraeComponentToTarget(comp_func, target, **kwargs):
    r = _getRegionFromTarget(target)
    r1 = _adjustRegion(r, **kwargs)
    comp = comp_func(r1)
    if isinstance(target, str):
        _g.addTracker(Pattern(target),r1,comp)
    elif isinstance(target, Pattern):
        _g.addTracker(target,r1,comp)
    elif isinstance(target, Visual):
        target.addFollower(comp)
    _g.addComponent(comp)
    return comp

def _adjustRegion(r_, offset = (0,0), expand=(0,0,0,0))
    r = Region(r_)
    # Offset
    (dx,dy) = offset
    r.x += dx
    r.y += dy
    # Expansion
    if isinstance(expand, tuple):
        (dt,dl,db,dr) = expand
    else:
        (dt,dl,db,dr) = (expand,expand,expand,expand)
    r.x -= dl
    r.y -= dt
    r.w = r.w + dl + dr
    r.h = r.h + dt + db
    return r

# RaiMan: seems not to be used anymore
#
# functions for showing
def _show_steps_old(steps, timeout = None):

    # only keep callables
    steps = filter(lambda x: callable(x), steps)
    print steps
    n = len(steps)
    i = 0

    while True:
        step = steps[i]
        step()

        d = TransitionDialog()

        text = "Step %d of %d" % (i+1, n)
        d.setText(text)

        if n == 1: # only one step
            d.addButton("Close")
        elif i == 0: # first step
            d.addButton("Next")
            d.addButton("Close")
        elif i < n - 1: # between
            d.addButton("Previous")
            d.addButton("Next")
            d.addButton("Close")
        elif i == n - 1: # final step
            d.addButton("Previous")
            d.addButton("Close")

        d.setLocationToUserPreferredLocation()
        if timeout:
            d.setTimeout(timeout*1000)

        _g.setTransition(d)
        ret = _g.showNow()

        if (ret == "Previous" and i > 0):
            i = i - 1
        elif (ret == "Next" and i < n - 1):
            i = i + 1
        elif (ret == None and i < n - 1): # timeout
            i = i + 1
        elif (ret == "Close"):
            return
        else:
            return

RaiMan: Temporarily switched off
#########################
# Experimental Features #
#########################

def portal(targets):
    p = Portal(_g)
    for target in targets:
        r = s.getRegionFromTarget(target)
        p.addEntry("",r)
    _g.addSingleton(p)

def magnifier(target):
    r = s.getRegionFromTarget(target)
    _g.addMagnifier(r)

def parse_model(gui, level=0):
    for i in range(0,level):
        print "----",
    n = gui[0]
    ps,name = n
    node_n = GUINode(Pattern(ps).similar(0.75))
    node_n.setName(name)
    print node_n
    children = gui[1:]
    for c in children:
        node_c = parse_model(c, level+1)
        node_n.add(node_c)
    return node_n


def do_search(guidefs, guide):
    root = GUINode(None)
    model = GUIModel(root)
    for guidef in guidefs:
        root.add(parse_model(guidef))

    search = TreeSearchDialog(guide, model)
    search.setLocationRelativeTo(None)
    search.setAlwaysOnTop(True)
    guide.setSearchDialog(search)
    guide.showNow()


h = dict()
def addEntry(target, keys):
    r = s.getRegionFromTarget(target)
    for k in keys:
        if isinstance(k, tuple):
            h[k[0]] = k[1]
            _g.addSearchEntry(k[0], r)
        else:
            _g.addSearchEntry(k, r)


def gui_search(guidefs, keyword):
    root = GUINode(None)
    model = GUIModel(root)
    for guidef in guidefs:
        root.add(parse_model(guidef))

    model.drawPathTo(_g, keyword);
    _g.showNow(3);


def search(model = None):
    if model:
        do_search(model, _g)
    else:
        ret = _g.showNow()
        if ret in h:
            h[ret]()
"""