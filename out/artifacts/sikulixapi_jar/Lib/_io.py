"""This is based on  _pyio.py from CPython 2.7 which is Python implementation of
the io module. The upgrade from a 2.6-ish version accounts for the large
number of changes made all at once.

It is here to stand in for classes that should be provided by the Java
implementation of the _io module.  In CPython 2.7, when client code
imports io, that module imports a set of classes from _io and
re-exports them as its own. In Jython, io.py imports those things from
_io, which in turn imports from _jyio those so far implemented in
Java. Consequently _io implements the rest here using nearly the same
code as _pyio. (Previous to Jython 2.7.1, the import was reversed:
this specific Python-based module was named _jyio, and it imported
from org.python.modules.io._io; although reasonable enough for Jython
itself, we found that extant Python code expected that the _io module
was the one defining various classes and constants. See
http://bugs.jython.org/issue2368 for more background. If we ever get
around to rewriting this module completely to Java, which is doubtful,
this problem will go away.)

Some classes have gained an underscore to match their _io module names:
_IOBase, _RawIOBase, _BufferedIOBase, _TextIOBase.

As Jython implements more and more of _io in Java, the Python implementations here
will progressively be replaced with imports from _io. Eventually we should implement
all this in Java, remove this module and revert io.py to its CPython original.

"""

from __future__ import (print_function, unicode_literals)

import os
import abc
import codecs
import warnings
import errno
import array
# Import thread instead of threading to reduce startup cost
try:
    from thread import allocate_lock as Lock
except ImportError:
    from dummy_thread import allocate_lock as Lock

#import io
#from io import (__all__, SEEK_SET, SEEK_CUR, SEEK_END)
from errno import EINTR

__metaclass__ = type

# open() uses st_blksize whenever we can
from _jyio import DEFAULT_BUFFER_SIZE

# NOTE: Base classes defined here are registered with the "official" ABCs
# defined in io.py.


class BlockingIOError(IOError):

    """Exception raised when I/O would block on a non-blocking I/O stream."""

    def __init__(self, errno, strerror, characters_written=0):
        super(IOError, self).__init__(errno, strerror)
        if not isinstance(characters_written, (int, long)):
            raise TypeError("characters_written must be a integer")
        self.characters_written = characters_written


from _jyio import (open, UnsupportedOperation, _IOBase, _RawIOBase, FileIO)


class _BufferedIOBase(_IOBase):

    """Base class for buffered IO objects.

    The main difference with _RawIOBase is that the read() method
    supports omitting the size argument, and does not have a default
    implementation that defers to readinto().

    In addition, read(), readinto() and write() may raise
    BlockingIOError if the underlying raw stream is in non-blocking
    mode and not ready; unlike their raw counterparts, they will never
    return None.

    A typical implementation should not inherit from a _RawIOBase
    implementation, but wrap one.
    """

    def read(self, n=None):
        """Read and return up to n bytes.

        If the argument is omitted, None, or negative, reads and
        returns all data until EOF.

        If the argument is positive, and the underlying raw stream is
        not 'interactive', multiple raw reads may be issued to satisfy
        the byte count (unless EOF is reached first).  But for
        interactive raw streams (XXX and for pipes?), at most one raw
        read will be issued, and a short result does not imply that
        EOF is imminent.

        Returns an empty bytes array on EOF.

        Raises BlockingIOError if the underlying raw stream has no
        data at the moment.
        """
        self._unsupported("read")

    def read1(self, n=None):
        """Read up to n bytes with at most one read() system call."""
        self._unsupported("read1")

    def readinto(self, b):
        """Read up to len(b) bytes into b.

        Like read(), this may issue multiple reads to the underlying raw
        stream, unless the latter is 'interactive'.

        Returns the number of bytes read (0 for EOF).

        Raises BlockingIOError if the underlying raw stream has no
        data at the moment.
        """
        # XXX This ought to work with anything that supports the buffer API
        data = self.read(len(b))
        n = len(data)
        try:
            b[:n] = data
        except TypeError as err:
            import array
            if not isinstance(b, array.array):
                raise err
            b[:n] = array.array(b'b', data)
        return n

    def write(self, b):
        """Write the given buffer to the IO stream.

        Return the number of bytes written, which is never less than
        len(b).

        Raises BlockingIOError if the buffer is full and the
        underlying raw stream cannot accept more data at the moment.
        """
        self._unsupported("write")

    def detach(self):
        """
        Separate the underlying raw stream from the buffer and return it.

        After the raw stream has been detached, the buffer is in an unusable
        state.
        """
        self._unsupported("detach")


class _BufferedIOMixin(_BufferedIOBase):

    """A mixin implementation of _BufferedIOBase with an underlying raw stream.

    This passes most requests on to the underlying raw stream.  It
    does *not* provide implementations of read(), readinto() or
    write().
    """

    def __init__(self, raw):
        self._ok = False    # Jython: subclass __init__ must set when state valid
        self._raw = raw

    ### Positioning ###

    def seek(self, pos, whence=0):
        new_position = self.raw.seek(pos, whence)
        if new_position < 0:
            raise IOError("seek() returned an invalid position")
        return new_position

    def tell(self):
        pos = self.raw.tell()
        if pos < 0:
            raise IOError("tell() returned an invalid position")
        return pos

    def truncate(self, pos=None):
        # Flush the stream.  We're mixing buffered I/O with lower-level I/O,
        # and a flush may be necessary to synch both views of the current
        # file state.
        self.flush()

        if pos is None:
            pos = self.tell()
        # XXX: Should seek() be used, instead of passing the position
        # XXX  directly to truncate?
        return self.raw.truncate(pos)

    ### Flush and close ###

    def flush(self):
        if self.closed:
            raise ValueError("flush of closed file")
        self.raw.flush()

    def close(self):
        if self.raw is not None and not self.closed:
            try:
                # Jython difference: call super.close() which manages "closed to client" state,
                # and calls flush(), which may raise BlockingIOError or BrokenPipeError etc.
                super(_BufferedIOBase, self).close()
            finally:
                self.raw.close()

    def detach(self):
        if self.raw is None:
            raise ValueError("raw stream already detached")
        self.flush()
        raw = self._raw
        self._raw = None
        return raw

    ### Inquiries ###

    def seekable(self):
        self._checkInitialized()     # Jython: to forbid use in an invalid state
        return self.raw.seekable()

    def readable(self):
        self._checkInitialized()     # Jython: to forbid use in an invalid state
        return self.raw.readable()

    def writable(self):
        self._checkInitialized()     # Jython: to forbid use in an invalid state
        return self.raw.writable()

    @property
    def raw(self):
        return self._raw

    @property
    def closed(self):
        return self.raw.closed

    # Jython difference: emulate C implementation CHECK_INITIALIZED. This is for
    # compatibility, to pass test.test_io.CTextIOWrapperTest.test_initialization.
    def _checkInitialized(self):
        if not self._ok:
            if self.raw is None:
                raise ValueError("raw stream has been detached")
            else:
                raise ValueError("I/O operation on uninitialized object")

    @property
    def name(self):
        return self.raw.name

    @property
    def mode(self):
        return self.raw.mode

    def __repr__(self):
        clsname = self.__class__.__name__
        try:
            name = self.name
        except AttributeError:
            return "<_io.{0}>".format(clsname)
        else:
            return "<_io.{0} name={1!r}>".format(clsname, name)

    ### Lower-level APIs ###

    def fileno(self):
        return self.raw.fileno()

    def isatty(self):
        return self.raw.isatty()


class BytesIO(_BufferedIOBase):

    """Buffered I/O implementation using an in-memory bytes buffer."""

    def __init__(self, initial_bytes=None):
        buf = bytearray()
        if initial_bytes is not None:
            buf.extend(initial_bytes)
        self._buffer = buf
        self._pos = 0

    # Jython: modelled after bytesio.c::bytesio_getstate
    def __getstate__(self):
        d = getattr(self, '__dict__', None)
        if d is not None :
            d = d.copy()
        return (self.getvalue(), self._pos, d)

    # Jython: modelled after bytesio.c::bytesio_setstate
    def __setstate__(self, state):

        if not isinstance(state, tuple) or len(state) < 3 :
            fmt = "%s.__setstate__ argument should be 3-tuple got %s"
            raise TypeError( fmt % (type(self), type(state)) )

        # Reset the object to its default state. This is only needed to handle
        # the case of repeated calls to __setstate__. */
        self._buffer = bytearray()
        self._pos = 0

        # Set the value of the internal buffer. If state[0] does not support the
        # buffer protocol, bytesio_write will raise the appropriate TypeError. */
        self.write(state[0]);

        # Carefully set the position value. Alternatively, we could use the seek
        # method instead of modifying self._pos directly to better protect the
        # object internal state against erroneous (or malicious) inputs. */
        p = state[1]
        if not isinstance(p, (int, long)) :
            fmt = "second item of state must be an integer, got %s"
            raise TypeError( fmt % type(p) )
        elif p < 0 :
            raise ValueError("position value cannot be negative")
        self._pos = p

        # Set the dictionary of the instance variables. */
        d = state[2]
        if not d is None :
            if isinstance(d, dict) :
                self.__dict__ = d
            else :
                fmt = "third item of state should be a dict, got %s"
                raise TypeError( fmt % type(d) )

    def getvalue(self):
        """Return the bytes value (contents) of the buffer
        """
        if self.closed:
            raise ValueError("getvalue on closed file")
        return bytes(self._buffer)

    def read(self, n=None):
        if self.closed:
            raise ValueError("read from closed file")
        if n is None:
            n = -1
        if not isinstance(n, (int, long)):
            raise TypeError("integer argument expected, got {0!r}".format(
                type(n)))
        if n < 0:
            n = len(self._buffer)
        if len(self._buffer) <= self._pos:
            return b""
        newpos = min(len(self._buffer), self._pos + n)
        b = self._buffer[self._pos : newpos]
        self._pos = newpos
        return bytes(b)

    def read1(self, n):
        """This is the same as read.
        """
        return self.read(n)

    def write(self, b):
        if self.closed:
            raise ValueError("write to closed file")
        if isinstance(b, unicode):
            raise TypeError("can't write unicode to binary stream")
        n = len(b)
        if n == 0:
            return 0
        pos = self._pos
        if pos > len(self._buffer):
            # Inserts null bytes between the current end of the file
            # and the new write position.
            padding = b'\x00' * (pos - len(self._buffer))
            self._buffer += padding
        self._buffer[pos:pos + n] = b
        self._pos += n
        return n

    def seek(self, pos, whence=0):
        if self.closed:
            raise ValueError("seek on closed file")
        try:
            pos.__index__
        except AttributeError:
            raise TypeError("an integer is required")
        if whence == 0:
            if pos < 0:
                raise ValueError("negative seek position %r" % (pos,))
            self._pos = pos
        elif whence == 1:
            self._pos = max(0, self._pos + pos)
        elif whence == 2:
            self._pos = max(0, len(self._buffer) + pos)
        else:
            raise ValueError("invalid whence value")
        return self._pos

    def tell(self):
        if self.closed:
            raise ValueError("tell on closed file")
        return self._pos

    def truncate(self, pos=None):
        if self.closed:
            raise ValueError("truncate on closed file")
        if pos is None:
            pos = self._pos
        else:
            try:
                pos.__index__
            except AttributeError:
                raise TypeError("an integer is required")
            if pos < 0:
                raise ValueError("negative truncate position %r" % (pos,))
        del self._buffer[pos:]
        return pos

    def readable(self):
        self._checkClosed()
        return True

    def writable(self):
        self._checkClosed()
        return True

    def seekable(self):
        self._checkClosed()
        return True


class BufferedReader(_BufferedIOMixin):

    """BufferedReader(raw[, buffer_size])

    A buffer for a readable, sequential BaseRawIO object.

    The constructor creates a BufferedReader for the given readable raw
    stream and buffer_size. If buffer_size is omitted, DEFAULT_BUFFER_SIZE
    is used.
    """

    def __init__(self, raw, buffer_size=DEFAULT_BUFFER_SIZE):
        """Create a new buffered reader using the given readable raw IO object.
        """
        if not raw.readable():
            raise IOError('"raw" argument must be readable.')

        _BufferedIOMixin.__init__(self, raw)
        if buffer_size <= 0:
            raise ValueError("invalid buffer size")
        self.buffer_size = buffer_size
        self._reset_read_buf()
        self._read_lock = Lock()
        self._ok = True     # Jython: to enable use now in a valid state

    def _reset_read_buf(self):
        self._read_buf = b""
        self._read_pos = 0

    def read(self, n=None):
        """Read n bytes.

        Returns exactly n bytes of data unless the underlying raw IO
        stream reaches EOF or if the call would block in non-blocking
        mode. If n is negative, read until EOF or until read() would
        block.
        """
        self._checkReadable()       # Jython: to forbid use in an invalid state
        if n is not None and n < -1:
            raise ValueError("invalid number of bytes to read")
        with self._read_lock:
            return self._read_unlocked(n)

    def _read_unlocked(self, n=None):
        nodata_val = b""
        empty_values = (b"", None)
        buf = self._read_buf
        pos = self._read_pos

        # Special case for when the number of bytes to read is unspecified.
        if n is None or n == -1:
            self._reset_read_buf()
            chunks = [buf[pos:]]  # Strip the consumed bytes.
            current_size = 0
            while True:
                # Read until EOF or until read() would block.
                try:
                    chunk = self.raw.read()
                except IOError as e:
                    if e.errno != EINTR:
                        raise
                    continue
                if chunk in empty_values:
                    nodata_val = chunk
                    break
                current_size += len(chunk)
                chunks.append(chunk)
            return b"".join(chunks) or nodata_val

        # The number of bytes to read is specified, return at most n bytes.
        avail = len(buf) - pos  # Length of the available buffered data.
        if n <= avail:
            # Fast path: the data to read is fully buffered.
            self._read_pos += n
            return buf[pos:pos+n]
        # Slow path: read from the stream until enough bytes are read,
        # or until an EOF occurs or until read() would block.
        chunks = [buf[pos:]]
        wanted = max(self.buffer_size, n)
        while avail < n:
            try:
                chunk = self.raw.read(wanted)
            except IOError as e:
                if e.errno != EINTR:
                    raise
                continue
            if chunk in empty_values:
                nodata_val = chunk
                break
            avail += len(chunk)
            chunks.append(chunk)
        # n is more then avail only when an EOF occurred or when
        # read() would have blocked.
        n = min(n, avail)
        out = b"".join(chunks)
        self._read_buf = out[n:]  # Save the extra data in the buffer.
        self._read_pos = 0
        return out[:n] if out else nodata_val

    def peek(self, n=0):
        """Returns buffered bytes without advancing the position.

        The argument indicates a desired minimal number of bytes; we
        do at most one raw read to satisfy it.  We never return more
        than self.buffer_size.
        """
        with self._read_lock:
            return self._peek_unlocked(n)

    def _peek_unlocked(self, n=0):
        want = min(n, self.buffer_size)
        have = len(self._read_buf) - self._read_pos
        if have < want or have <= 0:
            to_read = self.buffer_size - have
            while True:
                try:
                    current = self.raw.read(to_read)
                except IOError as e:
                    if e.errno != EINTR:
                        raise
                    continue
                break
            if current:
                self._read_buf = self._read_buf[self._read_pos:] + current
                self._read_pos = 0
        return self._read_buf[self._read_pos:]

    def read1(self, n):
        """Reads up to n bytes, with at most one read() system call."""
        # Returns up to n bytes.  If at least one byte is buffered, we
        # only return buffered bytes.  Otherwise, we do one raw read.
        self._checkReadable()       # Jython: to forbid use in an invalid state
        if n < 0:
            raise ValueError("number of bytes to read must be positive")
        if n == 0:
            return b""
        with self._read_lock:
            self._peek_unlocked(1)
            return self._read_unlocked(
                min(n, len(self._read_buf) - self._read_pos))

    def tell(self):
        return _BufferedIOMixin.tell(self) - len(self._read_buf) + self._read_pos

    def seek(self, pos, whence=0):
        if not (0 <= whence <= 2):
            raise ValueError("invalid whence value")
        with self._read_lock:
            if whence == 1:
                pos -= len(self._read_buf) - self._read_pos
            pos = _BufferedIOMixin.seek(self, pos, whence)
            self._reset_read_buf()
            return pos


class BufferedWriter(_BufferedIOMixin):

    """A buffer for a writeable sequential RawIO object.

    The constructor creates a BufferedWriter for the given writeable raw
    stream. If the buffer_size is not given, it defaults to
    DEFAULT_BUFFER_SIZE.
    """

    _warning_stack_offset = 2

    def __init__(self, raw,
                 buffer_size=DEFAULT_BUFFER_SIZE, max_buffer_size=None):
        if not raw.writable():
            raise IOError('"raw" argument must be writable.')

        _BufferedIOMixin.__init__(self, raw)
        if buffer_size <= 0:
            raise ValueError("invalid buffer size")
        if max_buffer_size is not None:
            warnings.warn("max_buffer_size is deprecated", DeprecationWarning,
                          self._warning_stack_offset)
        self.buffer_size = buffer_size
        self._write_buf = bytearray()
        self._write_lock = Lock()
        self._ok = True     # Jython: to enable use now in a valid state

    def write(self, b):
        self._checkWritable()       # Jython: to forbid use in an invalid state
        if self.closed:
            raise ValueError("write to closed file")
        if isinstance(b, unicode):
            raise TypeError("can't write unicode to binary stream")
        with self._write_lock:
            # XXX we can implement some more tricks to try and avoid
            # partial writes
            if len(self._write_buf) > self.buffer_size:
                # We're full, so let's pre-flush the buffer.  (This may
                # raise BlockingIOError with characters_written == 0.)
                self._flush_unlocked()
            before = len(self._write_buf)
            if isinstance(b, array.array):              # _pyio.py version fails on array.array
                self._write_buf.extend(b.tostring())    # Jython version works (while needed)
            else:
                self._write_buf.extend(b)
            written = len(self._write_buf) - before
            if len(self._write_buf) > self.buffer_size:
                try:
                    self._flush_unlocked()
                except BlockingIOError as e:
                    if len(self._write_buf) > self.buffer_size:
                        # We've hit the buffer_size. We have to accept a partial
                        # write and cut back our buffer.
                        overage = len(self._write_buf) - self.buffer_size
                        written -= overage
                        self._write_buf = self._write_buf[:self.buffer_size]
                        raise BlockingIOError(e.errno, e.strerror, written)
            return written

    def truncate(self, pos=None):
        with self._write_lock:
            self._flush_unlocked()
            if pos is None:
                pos = self.raw.tell()
            return self.raw.truncate(pos)

    def flush(self):
        with self._write_lock:
            self._flush_unlocked()

    def _flush_unlocked(self):
        if self.closed:
            raise ValueError("flush of closed file")
        self._checkWritable()       # Jython: to forbid use in an invalid state
        while self._write_buf:
            try:
                n = self.raw.write(self._write_buf)
            except BlockingIOError:
                raise RuntimeError("self.raw should implement _RawIOBase: it "
                                   "should not raise BlockingIOError")
            except IOError as e:
                if e.errno != EINTR:
                    raise
                continue
            if n is None:
                raise BlockingIOError(
                    errno.EAGAIN,
                    "write could not complete without blocking", 0)
            if n > len(self._write_buf) or n < 0:
                raise IOError("write() returned incorrect number of bytes")
            del self._write_buf[:n]

    def tell(self):
        return _BufferedIOMixin.tell(self) + len(self._write_buf)

    def seek(self, pos, whence=0):
        if not (0 <= whence <= 2):
            raise ValueError("invalid whence")
        with self._write_lock:
            self._flush_unlocked()
            return _BufferedIOMixin.seek(self, pos, whence)


class BufferedRWPair(_BufferedIOBase):

    """A buffered reader and writer object together.

    A buffered reader object and buffered writer object put together to
    form a sequential IO object that can read and write. This is typically
    used with a socket or two-way pipe.

    reader and writer are _RawIOBase objects that are readable and
    writeable respectively. If the buffer_size is omitted it defaults to
    DEFAULT_BUFFER_SIZE.
    """

    # XXX The usefulness of this (compared to having two separate IO
    # objects) is questionable.

    def __init__(self, reader, writer,
                 buffer_size=DEFAULT_BUFFER_SIZE, max_buffer_size=None):
        """Constructor.

        The arguments are two RawIO instances.
        """
        if max_buffer_size is not None:
            warnings.warn("max_buffer_size is deprecated", DeprecationWarning, 2)

        if not reader.readable():
            raise IOError('"reader" argument must be readable.')

        if not writer.writable():
            raise IOError('"writer" argument must be writable.')

        self.reader = BufferedReader(reader, buffer_size)
        self.writer = BufferedWriter(writer, buffer_size)

    def read(self, n=None):
        if n is None:
            n = -1
        return self.reader.read(n)

    def readinto(self, b):
        return self.reader.readinto(b)

    def write(self, b):
        return self.writer.write(b)

    def peek(self, n=0):
        return self.reader.peek(n)

    def read1(self, n):
        return self.reader.read1(n)

    def readable(self):
        return self.reader.readable()

    def writable(self):
        return self.writer.writable()

    def flush(self):
        return self.writer.flush()

    def close(self):
        self.writer.close()
        self.reader.close()

    def isatty(self):
        return self.reader.isatty() or self.writer.isatty()

    @property
    def closed(self):
        return self.writer.closed


class BufferedRandom(BufferedWriter, BufferedReader):

    """A buffered interface to random access streams.

    The constructor creates a reader and writer for a seekable stream,
    raw, given in the first argument. If the buffer_size is omitted it
    defaults to DEFAULT_BUFFER_SIZE.
    """

    _warning_stack_offset = 3

    def __init__(self, raw,
                 buffer_size=DEFAULT_BUFFER_SIZE, max_buffer_size=None):
        raw._checkSeekable()
        BufferedReader.__init__(self, raw, buffer_size)
        BufferedWriter.__init__(self, raw, buffer_size, max_buffer_size)

    def seek(self, pos, whence=0):
        if not (0 <= whence <= 2):
            raise ValueError("invalid whence")
        self.flush()
        if self._read_buf:
            # Undo read ahead.
            with self._read_lock:
                self.raw.seek(self._read_pos - len(self._read_buf), 1)
        # First do the raw seek, then empty the read buffer, so that
        # if the raw seek fails, we don't lose buffered data forever.
        pos = self.raw.seek(pos, whence)
        with self._read_lock:
            self._reset_read_buf()
        if pos < 0:
            raise IOError("seek() returned invalid position")
        return pos

    def tell(self):
        if self._write_buf:
            return BufferedWriter.tell(self)
        else:
            return BufferedReader.tell(self)

    def truncate(self, pos=None):
        if pos is None:
            pos = self.tell()
        # Use seek to flush the read buffer.
        return BufferedWriter.truncate(self, pos)

    def read(self, n=None):
        if n is None:
            n = -1
        self.flush()
        return BufferedReader.read(self, n)

    def readinto(self, b):
        self.flush()
        return BufferedReader.readinto(self, b)

    def peek(self, n=0):
        self.flush()
        return BufferedReader.peek(self, n)

    def read1(self, n):
        self.flush()
        return BufferedReader.read1(self, n)

    def write(self, b):
        if self._read_buf:
            # Undo readahead
            with self._read_lock:
                self.raw.seek(self._read_pos - len(self._read_buf), 1)
                self._reset_read_buf()
        return BufferedWriter.write(self, b)


class _TextIOBase(_IOBase):

    """Base class for text I/O.

    This class provides a character and line based interface to stream
    I/O. There is no readinto method because Python's character strings
    are immutable. There is no public constructor.
    """

    def read(self, n=-1):
        """Read at most n characters from stream.

        Read from underlying buffer until we have n characters or we hit EOF.
        If n is negative or omitted, read until EOF.
        """
        self._unsupported("read")

    def write(self, s):
        """Write string s to stream."""
        self._unsupported("write")

    def truncate(self, pos=None):
        """Truncate size to pos."""
        self._unsupported("truncate")

    def readline(self):
        """Read until newline or EOF.

        Returns an empty string if EOF is hit immediately.
        """
        self._unsupported("readline")

    def detach(self):
        """
        Separate the underlying buffer from the _TextIOBase and return it.

        After the underlying buffer has been detached, the TextIO is in an
        unusable state.
        """
        self._unsupported("detach")

    @property
    def encoding(self):
        """Subclasses should override."""
        return None

    @property
    def newlines(self):
        """Line endings translated so far.

        Only line endings translated during reading are considered.

        Subclasses should override.
        """
        return None

    @property
    def errors(self):
        """Error setting of the decoder or encoder.

        Subclasses should override."""
        return None


class IncrementalNewlineDecoder(codecs.IncrementalDecoder):
    r"""Codec used when reading a file in universal newlines mode.  It wraps
    another incremental decoder, translating \r\n and \r into \n.  It also
    records the types of newlines encountered.  When used with
    translate=False, it ensures that the newline sequence is returned in
    one piece.
    """
    def __init__(self, decoder, translate, errors='strict'):
        codecs.IncrementalDecoder.__init__(self, errors=errors)
        self.translate = translate
        self.decoder = decoder
        self.seennl = 0
        self.pendingcr = False

    def decode(self, input, final=False):
        # decode input (with the eventual \r from a previous pass)
        if self.decoder is None:
            output = input
        else:
            output = self.decoder.decode(input, final=final)
        if self.pendingcr and (output or final):
            output = "\r" + output
            self.pendingcr = False

        # retain last \r even when not translating data:
        # then readline() is sure to get \r\n in one pass
        if output.endswith("\r") and not final:
            output = output[:-1]
            self.pendingcr = True

        # Record which newlines are read
        crlf = output.count('\r\n')
        cr = output.count('\r') - crlf
        lf = output.count('\n') - crlf
        self.seennl |= (lf and self._LF) | (cr and self._CR) \
                    | (crlf and self._CRLF)

        if self.translate:
            if crlf:
                output = output.replace("\r\n", "\n")
            if cr:
                output = output.replace("\r", "\n")

        return output

    def getstate(self):
        if self.decoder is None:
            buf = b""
            flag = 0
        else:
            buf, flag = self.decoder.getstate()
        flag <<= 1
        if self.pendingcr:
            flag |= 1
        return buf, flag

    def setstate(self, state):
        buf, flag = state
        self.pendingcr = bool(flag & 1)
        if self.decoder is not None:
            self.decoder.setstate((buf, flag >> 1))

    def reset(self):
        self.seennl = 0
        self.pendingcr = False
        if self.decoder is not None:
            self.decoder.reset()

    _LF = 1
    _CR = 2
    _CRLF = 4

    @property
    def newlines(self):
        return (None,
                "\n",
                "\r",
                ("\r", "\n"),
                "\r\n",
                ("\n", "\r\n"),
                ("\r", "\r\n"),
                ("\r", "\n", "\r\n")
               )[self.seennl]


def _check_decoded_chars(chars):
    """Check decoder output is unicode"""
    if not isinstance(chars, unicode):
        raise TypeError("decoder should return a string result, not '%s'" %
                        type(chars))

def _check_buffered_bytes(b, context="read"):
    """Check buffer has returned bytes"""
    if not isinstance(b, str):
        raise TypeError("underlying %s() should have returned a bytes object, not '%s'" %
                        (context, type(b)))



class TextIOWrapper(_TextIOBase):

    r"""Character and line based layer over a _BufferedIOBase object, buffer.

    encoding gives the name of the encoding that the stream will be
    decoded or encoded with. It defaults to locale.getpreferredencoding.

    errors determines the strictness of encoding and decoding (see the
    codecs.register) and defaults to "strict".

    newline can be None, '', '\n', '\r', or '\r\n'.  It controls the
    handling of line endings. If it is None, universal newlines is
    enabled.  With this enabled, on input, the lines endings '\n', '\r',
    or '\r\n' are translated to '\n' before being returned to the
    caller. Conversely, on output, '\n' is translated to the system
    default line separator, os.linesep. If newline is any other of its
    legal values, that newline becomes the newline when the file is read
    and it is returned untranslated. On output, '\n' is converted to the
    newline.

    If line_buffering is True, a call to flush is implied when a call to
    write contains a newline character.
    """

    _CHUNK_SIZE = 2048

    def __init__(self, buffer, encoding=None, errors=None, newline=None,
                 line_buffering=False):
        self._ok = False    # Jython: to forbid use in an invalid state
        if newline is not None and not isinstance(newline, basestring):
            raise TypeError("illegal newline type: %r" % (type(newline),))
        if newline not in (None, "", "\n", "\r", "\r\n"):
            raise ValueError("illegal newline value: %r" % (newline,))
        if encoding is None:
            try:
                import locale
            except ImportError:
                # Importing locale may fail if Python is being built
                encoding = "ascii"
            else:
                encoding = locale.getpreferredencoding()

        if not isinstance(encoding, basestring):
            raise ValueError("invalid encoding: %r" % encoding)

        if errors is None:
            errors = "strict"
        else:
            if not isinstance(errors, basestring):
                raise ValueError("invalid errors: %r" % errors)

        self._buffer = buffer
        self._line_buffering = line_buffering
        self._encoding = encoding
        self._errors = errors
        self._readuniversal = not newline
        self._readtranslate = newline is None
        self._readnl = newline
        self._writetranslate = newline != ''
        self._writenl = newline or os.linesep
        self._encoder = None
        self._decoder = None
        self._decoded_chars = ''  # buffer for text returned from decoder
        self._decoded_chars_used = 0  # offset into _decoded_chars for read()
        self._snapshot = None  # info for reconstructing decoder state
        self._seekable = self._telling = self.buffer.seekable()

        self._ok = True     # Jython: to enable use now in a valid state

        if self._seekable and self.writable():
            position = self.buffer.tell()
            if position != 0:
                try:
                    self._get_encoder().setstate(0)
                except LookupError:
                    # Sometimes the encoder doesn't exist
                    pass

    # self._snapshot is either None, or a tuple (dec_flags, next_input)
    # where dec_flags is the second (integer) item of the decoder state
    # and next_input is the chunk of input bytes that comes next after the
    # snapshot point.  We use this to reconstruct decoder states in tell().

    # Naming convention:
    #   - "bytes_..." for integer variables that count input bytes
    #   - "chars_..." for integer variables that count decoded characters

    def __repr__(self):
        try:
            name = self.name
        except AttributeError:
            return "<_io.TextIOWrapper encoding='{0}'>".format(self.encoding)
        else:
            return "<_io.TextIOWrapper name={0!r} encoding='{1}'>".format(
                name, self.encoding)

    @property
    def encoding(self):
        return self._encoding

    @property
    def errors(self):
        return self._errors

    @property
    def line_buffering(self):
        return self._line_buffering

    @property
    def buffer(self):
        return self._buffer

    def seekable(self):
        self._checkInitialized()    # Jython: to forbid use in an invalid state
        self._checkClosed()
        return self._seekable

    def readable(self):
        self._checkInitialized()    # Jython: to forbid use in an invalid state
        return self.buffer.readable()

    def writable(self):
        self._checkInitialized()    # Jython: to forbid use in an invalid state
        return self.buffer.writable()

    def flush(self):
        self._checkInitialized()    # Jython: to forbid use in an invalid state
        self.buffer.flush()
        self._telling = self._seekable

    def close(self):
        if self.buffer is not None and not self.closed:
            try:
                # Jython difference: flush and close via super.
                # Sets __closed for quick _checkClosed().
                super(TextIOWrapper, self).close()
            finally:
                self.buffer.close()

    @property
    def closed(self):
        return self.buffer.closed

    # Jython difference: emulate C implementation CHECK_INITIALIZED. This is for
    # compatibility, to pass test.test_io.CTextIOWrapperTest.test_initialization.
    def _checkInitialized(self):
        if not self._ok:
            if self.buffer is None:
                raise ValueError("underlying buffer has been detached")
            else:
                raise ValueError("I/O operation on uninitialized object")

    @property
    def name(self):
        return self.buffer.name

    def fileno(self):
        return self.buffer.fileno()

    def isatty(self):
        return self.buffer.isatty()

    def write(self, s):
        self._checkWritable()       # Jython: to forbid use in an invalid state
        if self.closed:
            raise ValueError("write to closed file")
        if not isinstance(s, unicode):
            raise TypeError("can't write %s to text stream" %
                            s.__class__.__name__)
        length = len(s)
        haslf = (self._writetranslate or self._line_buffering) and "\n" in s
        if haslf and self._writetranslate and self._writenl != "\n":
            s = s.replace("\n", self._writenl)
        encoder = self._encoder or self._get_encoder()
        # XXX What if we were just reading?
        b = encoder.encode(s)
        self.buffer.write(b)
        if self._line_buffering and (haslf or "\r" in s):
            self.flush()
        self._snapshot = None
        if self._decoder:
            self._decoder.reset()
        return length

    def _get_encoder(self):
        make_encoder = codecs.getincrementalencoder(self._encoding)
        self._encoder = make_encoder(self._errors)
        return self._encoder

    def _get_decoder(self):
        make_decoder = codecs.getincrementaldecoder(self._encoding)
        decoder = make_decoder(self._errors)
        if self._readuniversal:
            decoder = IncrementalNewlineDecoder(decoder, self._readtranslate)
        self._decoder = decoder
        return decoder

    # The following three methods implement an ADT for _decoded_chars.
    # Text returned from the decoder is buffered here until the client
    # requests it by calling our read() or readline() method.
    def _set_decoded_chars(self, chars):
        """Set the _decoded_chars buffer."""
        self._decoded_chars = chars
        self._decoded_chars_used = 0

    def _get_decoded_chars(self, n=None):
        """Advance into the _decoded_chars buffer."""
        offset = self._decoded_chars_used
        if n is None:
            chars = self._decoded_chars[offset:]
        else:
            chars = self._decoded_chars[offset:offset + n]
        self._decoded_chars_used += len(chars)
        return chars

    def _rewind_decoded_chars(self, n):
        """Rewind the _decoded_chars buffer."""
        if self._decoded_chars_used < n:
            raise AssertionError("rewind decoded_chars out of bounds")
        self._decoded_chars_used -= n

    def _read_chunk(self):
        """
        Read and decode the next chunk of data from the BufferedReader.
        """

        # The return value is True unless EOF was reached.  The decoded
        # string is placed in self._decoded_chars (replacing its previous
        # value).  The entire input chunk is sent to the decoder, though
        # some of it may remain buffered in the decoder, yet to be
        # converted.

        if self._decoder is None:
            raise ValueError("no decoder")

        if self._telling:
            # To prepare for tell(), we need to snapshot a point in the
            # file where the decoder's input buffer is empty.

            dec_buffer, dec_flags = self._decoder.getstate()
            # Given this, we know there was a valid snapshot point
            # len(dec_buffer) bytes ago with decoder state (b'', dec_flags).

        # Read a chunk, decode it, and put the result in self._decoded_chars.
        input_chunk = self.buffer.read1(self._CHUNK_SIZE)
        _check_buffered_bytes(input_chunk, "read1")

        eof = not input_chunk
        decoded_chunk = self._decoder.decode(input_chunk, eof)
        _check_decoded_chars(decoded_chunk)
        self._set_decoded_chars(decoded_chunk)

        if self._telling:
            # At the snapshot point, len(dec_buffer) bytes before the read,
            # the next input to be decoded is dec_buffer + input_chunk.
            self._snapshot = (dec_flags, dec_buffer + input_chunk)

        return not eof

    def _pack_cookie(self, position, dec_flags=0,
                           bytes_to_feed=0, need_eof=0, chars_to_skip=0):
        # The meaning of a tell() cookie is: seek to position, set the
        # decoder flags to dec_flags, read bytes_to_feed bytes, feed them
        # into the decoder with need_eof as the EOF flag, then skip
        # chars_to_skip characters of the decoded result.  For most simple
        # decoders, tell() will often just give a byte offset in the file.
        return (position | (dec_flags<<64) | (bytes_to_feed<<128) |
               (chars_to_skip<<192) | bool(need_eof)<<256)

    def _unpack_cookie(self, bigint):
        rest, position = divmod(bigint, 1<<64)
        rest, dec_flags = divmod(rest, 1<<64)
        rest, bytes_to_feed = divmod(rest, 1<<64)
        need_eof, chars_to_skip = divmod(rest, 1<<64)
        return position, dec_flags, bytes_to_feed, need_eof, chars_to_skip

    def tell(self):
        if not self._seekable:
            raise IOError("underlying stream is not seekable")
        if not self._telling:
            raise IOError("telling position disabled by next() call")
        self.flush()
        position = self.buffer.tell()
        decoder = self._decoder
        if decoder is None or self._snapshot is None:
            if self._decoded_chars:
                # This should never happen.
                raise AssertionError("pending decoded text")
            return position

        # Skip backward to the snapshot point (see _read_chunk).
        dec_flags, next_input = self._snapshot
        position -= len(next_input)

        # How many decoded characters have been used up since the snapshot?
        chars_to_skip = self._decoded_chars_used
        if chars_to_skip == 0:
            # We haven't moved from the snapshot point.
            return self._pack_cookie(position, dec_flags)

        # Starting from the snapshot position, we will walk the decoder
        # forward until it gives us enough decoded characters.
        saved_state = decoder.getstate()
        try:
            # Note our initial start point.
            decoder.setstate((b'', dec_flags))
            start_pos = position
            start_flags, bytes_fed, chars_decoded = dec_flags, 0, 0
            need_eof = 0

            # Feed the decoder one byte at a time.  As we go, note the
            # nearest "safe start point" before the current location
            # (a point where the decoder has nothing buffered, so seek()
            # can safely start from there and advance to this location).
            for next_byte in next_input:
                bytes_fed += 1
                chars_decoded += len(decoder.decode(next_byte))
                dec_buffer, dec_flags = decoder.getstate()
                if not dec_buffer and chars_decoded <= chars_to_skip:
                    # Decoder buffer is empty, so this is a safe start point.
                    start_pos += bytes_fed
                    chars_to_skip -= chars_decoded
                    start_flags, bytes_fed, chars_decoded = dec_flags, 0, 0
                if chars_decoded >= chars_to_skip:
                    break
            else:
                # We didn't get enough decoded data; signal EOF to get more.
                chars_decoded += len(decoder.decode(b'', final=True))
                need_eof = 1
                if chars_decoded < chars_to_skip:
                    raise IOError("can't reconstruct logical file position")

            # The returned cookie corresponds to the last safe start point.
            return self._pack_cookie(
                start_pos, start_flags, bytes_fed, need_eof, chars_to_skip)
        finally:
            decoder.setstate(saved_state)

    def truncate(self, pos=None):
        self.flush()
        if pos is None:
            pos = self.tell()
        return self.buffer.truncate(pos)

    def detach(self):
        if self.buffer is None:
            raise ValueError("buffer is already detached")
        self.flush()
        self._ok = False    # Jython: to forbid use in an invalid state
        buffer = self._buffer
        self._buffer = None
        return buffer

    def seek(self, cookie, whence=0):
        if self.closed:
            raise ValueError("tell on closed file")
        if not self._seekable:
            raise IOError("underlying stream is not seekable")
        if whence == 1: # seek relative to current position
            if cookie != 0:
                raise IOError("can't do nonzero cur-relative seeks")
            # Seeking to the current position should attempt to
            # sync the underlying buffer with the current position.
            whence = 0
            cookie = self.tell()
        if whence == 2: # seek relative to end of file
            if cookie != 0:
                raise IOError("can't do nonzero end-relative seeks")
            self.flush()
            position = self.buffer.seek(0, 2)
            self._set_decoded_chars('')
            self._snapshot = None
            if self._decoder:
                self._decoder.reset()
            return position
        if whence != 0:
            raise ValueError("invalid whence (%r, should be 0, 1 or 2)" %
                             (whence,))
        if cookie < 0:
            raise ValueError("negative seek position %r" % (cookie,))
        self.flush()

        # The strategy of seek() is to go back to the safe start point
        # and replay the effect of read(chars_to_skip) from there.
        start_pos, dec_flags, bytes_to_feed, need_eof, chars_to_skip = \
            self._unpack_cookie(cookie)

        # Seek back to the safe start point.
        self.buffer.seek(start_pos)
        self._set_decoded_chars('')
        self._snapshot = None

        # Restore the decoder to its state from the safe start point.
        if cookie == 0 and self._decoder:
            self._decoder.reset()
        elif self._decoder or dec_flags or chars_to_skip:
            self._decoder = self._decoder or self._get_decoder()
            self._decoder.setstate((b'', dec_flags))
            self._snapshot = (dec_flags, b'')

        if chars_to_skip:
            # Just like _read_chunk, feed the decoder and save a snapshot.
            input_chunk = self.buffer.read(bytes_to_feed)
            _check_buffered_bytes(input_chunk)
            decoded_chunk = self._decoder.decode(input_chunk, need_eof)
            _check_decoded_chars(decoded_chunk)
            self._set_decoded_chars(decoded_chunk)

            self._snapshot = (dec_flags, input_chunk)

            # Skip chars_to_skip of the decoded characters.
            if len(self._decoded_chars) < chars_to_skip:
                raise IOError("can't restore logical file position")
            self._decoded_chars_used = chars_to_skip

        # Finally, reset the encoder (merely useful for proper BOM handling)
        try:
            encoder = self._encoder or self._get_encoder()
        except LookupError:
            # Sometimes the encoder doesn't exist
            pass
        else:
            if cookie != 0:
                encoder.setstate(0)
            else:
                encoder.reset()
        return cookie

    def read(self, n=None):
        self._checkReadable()
        if n is None:
            n = -1
        decoder = self._decoder or self._get_decoder()
        try:
            n.__index__
        except AttributeError:
            raise TypeError("an integer is required")
        if n < 0:
            # Read everything.
            input_chunk = self.buffer.read()
            # Jython difference: CPython textio.c omits:
            _check_buffered_bytes(input_chunk)
            decoded_chunk = decoder.decode(input_chunk, final=True)
            _check_decoded_chars(decoded_chunk)
            result = self._get_decoded_chars() + decoded_chunk
            self._set_decoded_chars('')
            self._snapshot = None
            return result
        else:
            # Keep reading chunks until we have n characters to return.
            eof = False
            result = self._get_decoded_chars(n)
            while len(result) < n and not eof:
                eof = not self._read_chunk()
                result += self._get_decoded_chars(n - len(result))
            return result

    def next(self):
        self._telling = False
        line = self.readline()
        if not line:
            self._snapshot = None
            self._telling = self._seekable
            raise StopIteration
        return line

    def readline(self, limit=None):
        if self.closed:
            raise ValueError("read from closed file")
        if limit is None:
            limit = -1
        elif not isinstance(limit, (int, long)):
            raise TypeError("limit must be an integer")

        # Grab all the decoded text (we will rewind any extra bits later).
        line = self._get_decoded_chars()

        start = 0
        # Make the decoder if it doesn't already exist.
        if not self._decoder:
            self._get_decoder()

        pos = endpos = None
        while True:
            if self._readtranslate:
                # Newlines are already translated, only search for \n
                pos = line.find('\n', start)
                if pos >= 0:
                    endpos = pos + 1
                    break
                else:
                    start = len(line)

            elif self._readuniversal:
                # Universal newline search. Find any of \r, \r\n, \n
                # The decoder ensures that \r\n are not split in two pieces

                # In C we'd look for these in parallel of course.
                nlpos = line.find("\n", start)
                crpos = line.find("\r", start)
                if crpos == -1:
                    if nlpos == -1:
                        # Nothing found
                        start = len(line)
                    else:
                        # Found \n
                        endpos = nlpos + 1
                        break
                elif nlpos == -1:
                    # Found lone \r
                    endpos = crpos + 1
                    break
                elif nlpos < crpos:
                    # Found \n
                    endpos = nlpos + 1
                    break
                elif nlpos == crpos + 1:
                    # Found \r\n
                    endpos = crpos + 2
                    break
                else:
                    # Found \r
                    endpos = crpos + 1
                    break
            else:
                # non-universal
                pos = line.find(self._readnl)
                if pos >= 0:
                    endpos = pos + len(self._readnl)
                    break

            if limit >= 0 and len(line) >= limit:
                endpos = limit  # reached length limit
                break

            # No line ending seen yet - get more data'
            while self._read_chunk():
                if self._decoded_chars:
                    break
            if self._decoded_chars:
                line += self._get_decoded_chars()
            else:
                # end of file
                self._set_decoded_chars('')
                self._snapshot = None
                return line

        if limit >= 0 and endpos > limit:
            endpos = limit  # don't exceed limit

        # Rewind _decoded_chars to just after the line ending we found.
        self._rewind_decoded_chars(len(line) - endpos)
        return line[:endpos]

    @property
    def newlines(self):
        return self._decoder.newlines if self._decoder else None


class StringIO(TextIOWrapper):
    """Text I/O implementation using an in-memory buffer.

    The initial_value argument sets the value of object.  The newline
    argument is like the one of TextIOWrapper's constructor.
    """

    def __init__(self, initial_value="", newline="\n"):

        # Newline mark needs to be in bytes: convert if not already so
        if isinstance(newline, unicode) :
            newline = newline.encode("utf-8")

        super(StringIO, self).__init__(BytesIO(),
                                       encoding="utf-8",
                                       errors="strict",
                                       newline=newline)
        # Issue #5645: make universal newlines semantics the same as in the
        # C version, even under Windows.
        if newline is None:
            self._writetranslate = False
        # An initial value may have been supplied (and must be unicode)
        if initial_value is not None:
            if not isinstance(initial_value, unicode) :
                fmt = "initial value should be unicode or None, got %s"
                raise TypeError( fmt % type(initial_value) )
            if initial_value:
                self.write(initial_value)
                self.seek(0)

    # Jython: modelled after stringio.c::stringio_getstate
    def __getstate__(self):
        d = getattr(self, '__dict__', None)
        if d is not None :
            d = d.copy()
        return (self.getvalue(), self._readnl, self.tell(), d)

    # Jython: modelled after stringio.c:stringio_setstate
    def __setstate__(self, state):
        self._checkClosed()

        if not isinstance(state, tuple) or len(state) < 4 :
            fmt = "%s.__setstate__ argument should be 4-tuple got %s"
            raise TypeError( fmt % (type(self), type(state)) )

        # Initialize the object's state, but empty
        self.__init__(None, state[1])

        # Write the buffer, bypassing end-of-line translation.
        value = state[0]
        if value is not None:
            if not isinstance(value, unicode) :
                fmt = "ivalue should be unicode or None, got %s"
                raise TypeError( fmt % type(value) )
            encoder = self._encoder or self._get_encoder()
            b = encoder.encode(state[0])
            self.buffer.write(b)

        # Reset the object to its default state. This is only needed to handle
        # the case of repeated calls to __setstate__.
        self.seek(0)

        # Set the position value using seek. A long is tolerated (e.g from pickle).
        p = state[2]
        if not isinstance(p, (int, long)) :
            fmt = "third item of state must be an integer, got %s"
            raise TypeError( fmt % type(p) )
        elif p < 0 :
            raise ValueError("position value cannot be negative")
        self.seek(p)

        # Set the dictionary of the instance variables. */
        d = state[3]
        if not d is None :
            if isinstance(d, dict) :
                self.__dict__ = d
            else :
                fmt = "fourth item of state should be a dict, got %s"
                raise TypeError( fmt % type(d) )

    def getvalue(self):
        self.flush()
        return self.buffer.getvalue().decode(self._encoding, self._errors)

    def __repr__(self):
        # TextIOWrapper tells the encoding in its repr. In StringIO,
        # that's a implementation detail.
        return object.__repr__(self)

    @property
    def errors(self):
        return None

    @property
    def encoding(self):
        return None

    def detach(self):
        # This doesn't make sense on StringIO.
        self._unsupported("detach")
