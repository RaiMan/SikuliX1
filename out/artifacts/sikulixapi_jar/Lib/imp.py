import os.path

from _imp import (C_BUILTIN, C_EXTENSION, IMP_HOOK, PKG_DIRECTORY, PY_COMPILED, PY_FROZEN, PY_SOURCE,
                  __doc__, acquire_lock, find_module, getClass, get_magic, get_suffixes,
                  is_builtin, is_frozen,
                  load_compiled, load_dynamic, load_module, load_source,
                  lock_held, new_module, release_lock, reload,
                  makeCompiledFilename as _makeCompiledFilename)


class NullImporter(object):

    def __init__(self, path):
        if os.path.isdir(path):
            raise ImportError()

    def find_module(self, fullname, path=None):
        return None
