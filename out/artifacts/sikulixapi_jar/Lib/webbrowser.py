#! /usr/bin/env python
"""Interfaces for launching and remotely controlling Web browsers."""
# Rewritten for Jython from the orginal for CPython maintained by Georg Brandl.

import getopt
import sys
from java.awt import Desktop
from java.net import URI

__all__ = ["Error", "open", "open_new", "open_new_tab", "get", "register"]

class Error(Exception):
    pass

class AWTBrowser(object):
    def open(self, url, new=0, autoraise=1):    
        if not Desktop.isDesktopSupported():
            raise Error("webbrowswer.py not supported in your environment")
        try:
            Desktop.getDesktop().browse(URI(url))
            return True
        except IOError as e:
            raise Error(e)
  
    def open_new(self, url):
        return self.open(url, 1)

    def open_new_tab(self, url):
        return self.open(url, 2)


# singleton, since we only support one such browser anyway in Java AWT,
# despite get/register functions
AWTBrowser = AWTBrowser()


def get(using=None):
    """Return a browser launcher instance appropriate for the environment."""
    return AWTBrowser


def register(name, klass, instance=None, update_tryorder=1):
    """Register a browser connector and, optionally, connection."""
    pass  # ignored on Jython

open = AWTBrowser.open
open_new = AWTBrowser.open_new
open_new_tab = AWTBrowser.open_new_tab


def main():
    import getopt
    usage = """Usage: %s [-n | -t] url
    -n: open new window
    -t: open new tab""" % sys.argv[0]
    try:
        opts, args = getopt.getopt(sys.argv[1:], 'ntd')
    except getopt.error, msg:
        print >>sys.stderr, msg
        print >>sys.stderr, usage
        sys.exit(1)
    new_win = 0
    for o, a in opts:
        if o == '-n': new_win = 1
        elif o == '-t': new_win = 2
    if len(args) <> 1:
        print >>sys.stderr, usage
        sys.exit(1)

    url = args[0]

    open(url, new_win)
    print ('opened')

    print "\a"

if __name__ == "__main__":
    main()
