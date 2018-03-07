# Copyright 2010-2014, Sikuli.org, sikulix.com
# Released under the MIT License.
# created by RaiMan 2013
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
