###
#
# Copyright Alan Kennedy.
#
# You may contact the copyright holder at this uri:
#
# http://www.xhaus.com/contact/modjy
#
# The licence under which this code is released is the Apache License v2.0.
#
# The terms and conditions of this license are listed in a file contained
# in the distribution that also contained this file, under the name
# LICENSE.txt.
#
# You may also read a copy of the license at the following web address.
#
# http://modjy.xhaus.com/LICENSE.txt
#
###

#
# This code adapted from the socket._fileobject class
#

import jarray

class modjy_input_object(object):

    def __init__(self, servlet_inputstream, bufsize=8192):
        self.istream = servlet_inputstream
        self.buffer_size = bufsize
        self.buffer = ""

    def istream_read(self, n):
        data = jarray.zeros(n, 'b')
        m = self.istream.read(data)
        if m == -1: # indicates EOF has been reached, so we just return the empty string
            return ""
        elif m <= 0:
            return ""
        if m < n:
            data = data[:m]
        return data.tostring()

    def read(self, size=-1):
        data = self.buffer
        if size < 0:
            # Read until EOF
            buffers = []
            if data:
                buffers.append(data)
            self.buffer = ""
            recv_size = self.buffer_size
            while True:
                data = self.istream_read(recv_size)
                if not data:
                    break
                buffers.append(data)
            return "".join(buffers)
        else:
            # Read until size bytes or EOF seen, whichever comes first
            buf_len = len(data)
            if buf_len >= size:
                self.buffer = data[size:]
                return data[:size]
            buffers = []
            if data:
                buffers.append(data)
            self.buffer = ""
            while True:
                left = size - buf_len
                recv_size = max(self.buffer_size, left)
                data = self.istream_read(recv_size)
                if not data:
                    break
                buffers.append(data)
                n = len(data)
                if n >= left:
                    self.buffer = data[left:]
                    buffers[-1] = data[:left]
                    break
                buf_len += n
            return "".join(buffers)

    def readline(self, size=-1):
        data = self.buffer
        if size < 0:
            # Read until \n or EOF, whichever comes first
            nl = data.find('\n')
            if nl >= 0:
                nl += 1
                self.buffer = data[nl:]
                return data[:nl]
            buffers = []
            if data:
                buffers.append(data)
            self.buffer = ""
            while True:
                data = self.istream_read(self.buffer_size)
                if not data:
                    break
                buffers.append(data)
                nl = data.find('\n')
                if nl >= 0:
                    nl += 1
                    self.buffer = data[nl:]
                    buffers[-1] = data[:nl]
                    break
            return "".join(buffers)
        else:
            # Read until size bytes or \n or EOF seen, whichever comes first
            nl = data.find('\n', 0, size)
            if nl >= 0:
                nl += 1
                self.buffer = data[nl:]
                return data[:nl]
            buf_len = len(data)
            if buf_len >= size:
                self.buffer = data[size:]
                return data[:size]
            buffers = []
            if data:
                buffers.append(data)
            self.buffer = ""
            while True:
                data = self.istream_read(self.buffer_size)
                if not data:
                    break
                buffers.append(data)
                left = size - buf_len
                nl = data.find('\n', 0, left)
                if nl >= 0:
                    nl += 1
                    self.buffer = data[nl:]
                    buffers[-1] = data[:nl]
                    break
                n = len(data)
                if n >= left:
                    self.buffer = data[left:]
                    buffers[-1] = data[:left]
                    break
                buf_len += n
            return "".join(buffers)

    def readlines(self, sizehint=0):
        total = 0
        list = []
        while True:
            line = self.readline()
            if not line:
                break
            list.append(line)
            total += len(line)
            if sizehint and total >= sizehint:
                break
        return list

    # Iterator protocols

    def __iter__(self):
        return self

    def next(self):
        line = self.readline()
        if not line:
            raise StopIteration
        return line
