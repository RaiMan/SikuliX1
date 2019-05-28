import os.path
import sys
from warnings import warn

try:
    _console = sys._jy_console
    _reader = _console.reader
except AttributeError:
    raise ImportError("Cannot access JLine2 setup")

try:
    # jarjar-ed version
    from org.python.jline.console.history import MemoryHistory
except ImportError:
    # dev version from extlibs
    from jline.console.history import MemoryHistory


__all__ = ['add_history', 'clear_history', 'get_begidx', 'get_completer',
           'get_completer_delims', 'get_current_history_length',
           'get_endidx', 'get_history_item', 'get_history_length',
           'get_line_buffer', 'insert_text', 'parse_and_bind',
           'read_history_file', 'read_init_file', 'redisplay',
           'remove_history_item', 'set_completer', 'set_completer_delims',
           'set_history_length', 'set_pre_input_hook', 'set_startup_hook',
           'write_history_file']

_history_list = None

# The need for the following warnings should go away once we update
# JLine. Choosing ImportWarning as the closest warning to what is
# going on here, namely this is functionality not yet available on
# Jython.

class NotImplementedWarning(ImportWarning):
    """Not yet implemented by Jython"""

class SecurityWarning(ImportWarning):
    """Security manager prevents access to private field"""


def parse_and_bind(string):
    pass

def get_line_buffer():
    return str(_reader.cursorBuffer.buffer)

def insert_text(string):
    _reader.putString(string)

def read_init_file(filename=None):
    warn("read_init_file: %s" % (filename,), NotImplementedWarning, "module", 2)

def read_history_file(filename="~/.history"):
    expanded = os.path.expanduser(filename)
    with open(expanded) as f:
        _reader.history.load(f)

def write_history_file(filename="~/.history"):
    expanded = os.path.expanduser(filename)
    with open(expanded, 'w') as f:
        for line in _reader.history.entries():
            f.write(line.value().encode("utf-8"))
            f.write("\n")

def clear_history():
    _reader.history.clear()

def add_history(line):
    _reader.history.add(line)

def get_history_length():
    return _reader.history.maxSize

def set_history_length(length):
    _reader.history.maxSize = length

def get_current_history_length():
    return _reader.history.size()

def get_history_item(index):
    # JLine indexes from 0 while readline indexes from 1 (at least in test_readline)
    if index>0:
        return _reader.history.get(index-1)
    else:
        return None

def remove_history_item(pos):
    _reader.history.remove(pos)

def replace_history_item(pos, line):
    _reader.history.set(pos, line)

def redisplay():
    _reader.redrawLine()

def set_startup_hook(function=None):
    _console.startupHook = function

def set_pre_input_hook(function=None):
    warn("set_pre_input_hook %s" % (function,), NotImplementedWarning, stacklevel=2)

_completer_function = None

def set_completer(function=None):
    """set_completer([function]) -> None
    Set or remove the completer function.
    The function is called as function(text, state),
    for state in 0, 1, 2, ..., until it returns a non-string.
    It should return the next possible completion starting with 'text'."""

    global _completer_function
    _completer_function = function

    def complete_handler(buffer, cursor, candidates):
        start = _get_delimited(buffer, cursor)[0]
        delimited = buffer[start:cursor]

        try:
            sys.ps2
            have_ps2 = True
        except AttributeError:
            have_ps2 = False

        if (have_ps2 and _reader.prompt == sys.ps2) and (not delimited or delimited.isspace()):
            # Insert tab (as expanded to 4 spaces), but only if if
            # preceding is whitespace/empty and in console
            # continuation; this is a planned featue for Python 3 per
            # http://bugs.python.org/issue5845
            #
            # Ideally this would not expand tabs, in case of mixed
            # copy&paste of tab-indented code, however JLine2 gets
            # confused as to the cursor position if certain, but not
            # all, subsequent editing if the tab is backspaced
            candidates.add(" " * 4)
            return start

        # TODO: if there are a reasonably large number of completions
        # (need to get specific numbers), CPython 3.4 will show a
        # message like so:
        # >>>
        # Display all 186 possibilities? (y or n)
        # Currently Jython arbitrarily limits this to 100 and displays them
        for state in xrange(100):
            completion = None
            try:
                completion = function(delimited, state)
            except:
                pass
            if completion:
                candidates.add(completion)
            else:
                break
        return start

    _reader.addCompleter(complete_handler)


def get_completer():
    return _completer_function

def _get_delimited(buffer, cursor):
    start = cursor
    for i in xrange(cursor-1, -1, -1):
        if buffer[i] in _completer_delims:
            break
        start = i
    return start, cursor

def get_begidx():
    return _get_delimited(str(_reader.cursorBuffer.buffer), _reader.cursorBuffer.cursor)[0]

def get_endidx():
    return _get_delimited(str(_reader.cursorBuffer.buffer), _reader.cursorBuffer.cursor)[1]

def set_completer_delims(string):
    global _completer_delims, _completer_delims_set
    _completer_delims = string
    _completer_delims_set = set(string)

def get_completer_delims():
    return _completer_delims

set_completer_delims(' \t\n`~!@#$%^&*()-=+[{]}\\|;:\'",<>/?')
