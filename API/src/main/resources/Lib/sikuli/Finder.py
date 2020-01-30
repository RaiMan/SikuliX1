#  Copyright (c) 2010-2020, sikuli.org, sikulix.com - MIT license

from org.sikuli.script import Finder as JFinder

class Finder(JFinder):

# TODO make as Python  (support for with)
   
    def __init__(self):
        pass
    
    def __enter__(self):
        return super
    
    def __exit__(self, type, value, trackback):
        super.destroy()
    
    def __del__(self, type, value, trackback):
        super.destroy()
