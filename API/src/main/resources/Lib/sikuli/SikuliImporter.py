# Copyright 2010-2014, Sikuli.org, sikulix.com
# Released under the MIT License.
# modified RaiMan 2013

import imp
import sys

import Sikuli
from org.sikuli.script import ImagePath
import org.sikuli.util.JythonHelper as JH
import os

def _stripPackagePrefix(module_name):
    pdot = module_name.rfind('.')
    if pdot >= 0:
        return module_name[pdot+1:]
    return module_name

class SikuliImporter:

    class SikuliLoader:
        def __init__(self, path):
            self.path = path
        
        def _load_module(self, fullname):
            try:
                (file, pathname, desc) =  imp.find_module(fullname)
            except:
                etype, evalue, etb = sys.exc_info()
                evalue = etype(fullname + ".sikuli has no " + fullname + ".py")
                raise etype, evalue, etb
              
            try:
                return imp.load_module(fullname, file, pathname, desc)
            except:
                etype, evalue, etb = sys.exc_info()
                evalue = etype("!!WHILE IMPORTING!! %s" % evalue)
                raise etype, evalue, etb
            finally:
                if file:
                    file.close()
        
        def load_module(self, module_name):
            module_name = JH.get().loadModulePrepare(module_name, self.path)
            return self._load_module(module_name)

    def _find_module(self, module_name, fullpath):
        fullpath = fullpath + "/" + module_name + ".sikuli"
        if os.path.exists(fullpath):
            return self.SikuliLoader(fullpath)
        return None

    def find_module(self, module_name, package_path):
        module_path = JH.get().findModule(module_name, package_path, sys.path)
        if not module_path: 
          return None
        else: 
          return self.SikuliLoader(module_path)

sys.meta_path.append(SikuliImporter())
del SikuliImporter
