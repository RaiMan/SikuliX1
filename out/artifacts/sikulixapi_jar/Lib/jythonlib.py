from _jythonlib import *
import _bytecodetools as bytecodetools

# Convenience imports, since this is the most common case for using
# jythonlib, especially with MapMaker

try:
    # jarjar-ed version
    import org.python.google.common as guava
    from org.python.google.common.collect import MapMaker
    from org.python.google.common.cache import CacheBuilder, CacheLoader
except ImportError:
    # dev version from extlibs
    import com.google.common as guava
    from com.google.common.collect import MapMaker
    from com.google.common.cache import CacheBuilder, CacheLoader

