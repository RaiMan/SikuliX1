# -*- coding: utf-8 -*-

"""
XML Test Runner for PyUnit
"""

# Written by Sebastian Rittau <srittau@jroger.in-berlin.de> and placed in
# the Public Domain. With contributions by Paolo Borelli and others.

# downloaded from: http://www.rittau.org/python/xmlrunner.py
# modified by RaiMan at sikulix.com 
# removed self runnable and self test

from __future__ import unicode_literals

__version__ = "0.2"

import os.path
import re
import sys
import time
import traceback
import unittest
import unittest.util
from xml.sax.saxutils import escape

from io import StringIO, BytesIO


class _TestInfo(object):

    """Information about a particular test.
    
    Used by _XMLTestResult.
    
    """

    def __init__(self, test, time):
        (self._class, self._method) = test.id().rsplit(".", 1)
        self._time = time
        self._error = None
        self._failure = None

    @staticmethod
    def create_success(test, time):
        """Create a _TestInfo instance for a successful test."""
        return _TestInfo(test, time)

    @staticmethod
    def create_failure(test, time, failure):
        """Create a _TestInfo instance for a failed test."""
        info = _TestInfo(test, time)
        info._failure = failure
        return info

    @staticmethod
    def create_error(test, time, error):
        """Create a _TestInfo instance for an erroneous test."""
        info = _TestInfo(test, time)
        info._error = error
        return info

    def print_report(self, stream):
        """Print information about this test case in XML format to the
        supplied stream.

        """
        tag_template = ('  <testcase classname="{class_}" name="{method}" '
                        'time="{time:.4f}">')
        stream.write(tag_template.format(class_=self._class,
                                         method=self._method,
                                         time=self._time))
        if self._failure is not None:
            self._print_error(stream, 'failure', self._failure)
        if self._error is not None:
            self._print_error(stream, 'error', self._error)
        stream.write('</testcase>\n')

    @staticmethod
    def _print_error(stream, tag_name, error):
        """Print information from a failure or error to the supplied stream."""
        str_ = str if sys.version_info[0] >= 3 else unicode
        io_class = StringIO if sys.version_info[0] >= 3 else BytesIO
        text = escape(str_(error[1]))
        class_name = unittest.util.strclass(error[0])
        stream.write('\n')
        stream.write('    <{tag} type="{class_}">{text}\n'.format(
            tag=tag_name, class_= class_name, text=text))
        tb_stream = io_class()
        traceback.print_tb(error[2], None, tb_stream)
        tb_string = tb_stream.getvalue()
        if sys.version_info[0] < 3:
            tb_string = tb_string.decode("utf-8")
        stream.write(escape(tb_string))
        stream.write('    </{tag}>\n'.format(tag=tag_name))
        stream.write('  ')


def _clsname(cls):
    return cls.__module__ + "." + cls.__name__


class _XMLTestResult(unittest.TestResult):

    """A test result class that stores result as XML.

    Used by XMLTestRunner.

    """

    def __init__(self, class_name):
        unittest.TestResult.__init__(self)
        self._test_name = class_name
        self._start_time = None
        self._tests = []
        self._error = None
        self._failure = None

    def startTest(self, test):
        unittest.TestResult.startTest(self, test)
        self._error = None
        self._failure = None
        self._start_time = time.time()

    def stopTest(self, test):
        time_taken = time.time() - self._start_time
        unittest.TestResult.stopTest(self, test)
        if self._error:
            info = _TestInfo.create_error(test, time_taken, self._error)
        elif self._failure:
            info = _TestInfo.create_failure(test, time_taken, self._failure)
        else:
            info = _TestInfo.create_success(test, time_taken)
        self._tests.append(info)

    def addError(self, test, err):
        unittest.TestResult.addError(self, test, err)
        self._error = err

    def addFailure(self, test, err):
        unittest.TestResult.addFailure(self, test, err)
        self._failure = err

    def print_report(self, stream, time_taken, out, err):
        """Prints the XML report to the supplied stream.
        
        The time the tests took to perform as well as the captured standard
        output and standard error streams must be passed in.a

        """
        tag_template = ('<testsuite errors="{errors}" failures="{failures}" '
                        'name="{name}" tests="{total}" time="{time:.3f}">\n')
        stream.write(tag_template.format(name=self._test_name,
                                         total=self.testsRun,
                                         errors=len(self.errors),
                                         failures=len(self.failures),
                                         time=time_taken))
        for info in self._tests:
            info.print_report(stream)
        stream.write('  <system-out><![CDATA[{0}]]></system-out>\n'.format(
            out))
        stream.write('  <system-err><![CDATA[{0}]]></system-err>\n'.format(
            err))
        stream.write('</testsuite>\n')


class XMLTestRunner(object):

    """A test runner that stores results in XML format compatible with JUnit.

    XMLTestRunner(stream=None) -> XML test runner

    The XML file is written to the supplied stream. If stream is None, the
    results are stored in a file called TEST-<module>.<class>.xml in the
    current working directory (if not overridden with the path property),
    where <module> and <class> are the module and class name of the test class.

    """

    def __init__(self, stream=None):
        self._stream = stream
        self._path = "."

    def run(self, test):
        """Run the given test case or test suite."""
        class_ = test.__class__
        class_name = class_.__module__ + "." + class_.__name__
        if self._stream is None:
            filename = "TEST-{0}.xml".format(class_name)
            stream = open(os.path.join(self._path, filename), "w")
            stream.write('<?xml version="1.0" encoding="utf-8"?>\n')
        else:
            stream = self._stream

        result = _XMLTestResult(class_name)
        start_time = time.time()

        with _FakeStdStreams():
            test(result)
            try:
                out_s = sys.stdout.getvalue()
            except AttributeError:
                out_s = ""
            try:
                err_s = sys.stderr.getvalue()
            except AttributeError:
                err_s = ""

        time_taken = time.time() - start_time
        result.print_report(stream, time_taken, out_s, err_s)
        if self._stream is None:
            stream.close()

        return result

    def _set_path(self, path):
        self._path = path

    path = property(
        lambda self: self._path, _set_path, None,
        """The path where the XML files are stored.
            
        This property is ignored when the XML file is written to a file
        stream.""")


class _FakeStdStreams(object):

    def __enter__(self):
        self._orig_stdout = sys.stdout
        self._orig_stderr = sys.stderr
        sys.stdout = StringIO()
        sys.stderr = StringIO()

    def __exit__(self, exc_type, exc_val, exc_tb):
        sys.stdout = self._orig_stdout
        sys.stderr = self._orig_stderr
