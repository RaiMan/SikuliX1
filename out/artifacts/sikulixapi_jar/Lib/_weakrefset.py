# Access WeakSet through the weakref module.
# This code is separated-out because it is needed
# by abc.py to load everything else at startup.

from java.util import WeakHashMap
from java.util.Collections import newSetFromMap, synchronizedMap
from jythonlib import set_builder, MapMaker

__all__ = ['WeakSet']


class WeakSet(set):

    def __new__(cls, data=None):
        def _build():
            # Although not specified in the docs, WeakSet supports equality on
            # keys, as seen in test_weakset and its use of testing class
            # SomeClass. Therefore we cannot use MapMaker in this case.
            return newSetFromMap(synchronizedMap(WeakHashMap()))

        return set_builder(_build, cls)(data)
