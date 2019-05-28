'''
Import of this module is triggered by org.python.core.imp.import_next
on first import of setuptools.command. It essentially restores a
Jython specific fix for OSX shebang line via monkeypatching.

See http://bugs.jython.org/issue2570
Related: http://bugs.jython.org/issue1112
'''

from setuptools.command import easy_install as ez

_as_header = ez.CommandSpec.as_header

def _jython_as_header(self):
    '''Workaround Jython's sys.executable being a .sh (an invalid
    shebang line interpreter)
    '''
    if not ez.is_sh(self[0]):
        return _as_header(self)

    if self.options:
        # Can't apply the workaround, leave it broken
        log.warn(
            "WARNING: Unable to adapt shebang line for Jython,"
            " the following script is NOT executable\n"
            "         see http://bugs.jython.org/issue1112 for"
            " more information.")
        return _as_header(self)

    items = ['/usr/bin/env'] + self + list(self.options)
    return self._render(items)

ez.CommandSpec.as_header = _jython_as_header
