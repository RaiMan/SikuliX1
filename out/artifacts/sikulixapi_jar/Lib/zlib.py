"""
The functions in this module allow compression and decompression using the
zlib library, which is based on GNU zip.

adler32(string[, start]) -- Compute an Adler-32 checksum.
compress(string[, level]) -- Compress string, with compression level in 1-9.
compressobj([level]) -- Return a compressor object.
crc32(string[, start]) -- Compute a CRC-32 checksum.
decompress(string,[wbits],[bufsize]) -- Decompresses a compressed string.
decompressobj([wbits]) -- Return a decompressor object.

'wbits' is window buffer size.
Compressor objects support compress() and flush() methods; decompressor
objects support decompress() and flush().
"""
import array
import binascii
import jarray
import struct
import sys
from cStringIO import StringIO

from java.lang import Long, String, System
from java.util.zip import Adler32, CRC32, Deflater, Inflater, DataFormatException


class error(Exception):
    pass


DEFLATED = 8
MAX_WBITS = 15
DEF_MEM_LEVEL = 8
ZLIB_VERSION = "1.1.3"
Z_BEST_COMPRESSION = 9
Z_BEST_SPEED = 1

Z_FILTERED = 1
Z_HUFFMAN_ONLY = 2

Z_DEFAULT_COMPRESSION = -1
Z_DEFAULT_STRATEGY = 0

Z_NO_FLUSH = 0
Z_SYNC_FLUSH = 2
Z_FULL_FLUSH = 3
Z_FINISH = 4
_valid_flush_modes = (Z_NO_FLUSH, Z_SYNC_FLUSH, Z_FULL_FLUSH, Z_FINISH)

_zlib_to_deflater = {
    Z_NO_FLUSH: Deflater.NO_FLUSH,
    Z_SYNC_FLUSH: Deflater.SYNC_FLUSH,
    Z_FULL_FLUSH: Deflater.FULL_FLUSH
}


_ADLER_BASE = 65521  # largest prime smaller than 65536

def adler32(s, value=1):
    # Although Java has an implmentation in java.util.zip.Adler32,
    # this class does not allow for updating the value directly, as
    # required by this C-style API.
    #
    # ported from https://tools.ietf.org/html/rfc2960#page-132
    s1 = value & 0xffff
    s2 = (value >> 16) & 0xffff
    for c in s:
        s1 = (s1 + ord(c)) % _ADLER_BASE
        s2 = (s2 + s1)     % _ADLER_BASE
    # Support two's complement, to comply with the range specified for 2.6+;
    # for 3.x, simply return (s2 << 16) + s1
    high_bit = -2147483648 if (s2 & 0x8000) else 0
    remaining_high_word = s2 & 0x7fff
    return high_bit + (remaining_high_word << 16) + s1

def crc32(string, value=0):
    return binascii.crc32(string, value)

def compress(string, level=6):
    if level < Z_BEST_SPEED or level > Z_BEST_COMPRESSION:
        raise error, "Bad compression level"
    deflater = Deflater(level, 0)
    try:
        string = _to_input(string)
        deflater.setInput(string, 0, len(string))
        deflater.finish()
        return _get_deflate_data(deflater)
    finally:
        deflater.end()

def decompress(string, wbits=0, bufsize=16384):
    inflater = Inflater(wbits < 0)
    try:
        inflater.setInput(_to_input(string))
        data = _get_inflate_data(inflater)
        if not inflater.finished():
            raise error, "Error -5 while decompressing data: incomplete or truncated stream"
        return data
    finally:
        inflater.end()


# per zlib manual (http://www.zlib.net/manual.html):

# > windowBits can also be greater than 15 for optional gzip
# > encoding. Add 16 to windowBits to write a simple gzip header and
# > trailer around the compressed data instead of a zlib wrapper. The
# > gzip header will have no file name, no extra data, no comment, no
# > modification time (set to zero), no header crc, and the operating
# > system will be set to 255 (unknown). If a gzip stream is being
# > written, strm->adler is a crc32 instead of an adler32.

class compressobj(object):
    # All jython uses wbits for is in deciding whether to skip the
    # header if it's negative or to set gzip. But we still raise
    # ValueError to get full test compliance.

    GZIP_HEADER = "\x1f\x8b\x08\x00\x00\x00\x00\x00\x04\x03"

    # NB: this format is little-endian, not big-endian as we might
    # expect for network oriented protocols, as specified by RFCs;
    # CRC32.getValue() returns an unsigned int as a long, so cope
    # accordingly
    GZIP_TRAILER_FORMAT = struct.Struct("<Ii")  # crc32, size

    def __init__(self, level=6, method=DEFLATED, wbits=MAX_WBITS,
                       memLevel=0, strategy=0):
        if abs(wbits) & 16:
            if wbits > 0:
                wbits -= 16
            else:
                wbits += 16
            self._gzip = True
        else:
            self._gzip = False
        if abs(wbits) > MAX_WBITS or abs(wbits) < 8:
            raise ValueError, "Invalid initialization option: %s" % (wbits,)
        self.deflater = Deflater(level, wbits < 0 or self._gzip)
        self.deflater.setStrategy(strategy)
        self._ended = False
        self._size = 0
        self._crc32 = CRC32()

    def compress(self, string):
        if self._ended:
            raise error("compressobj may not be used after flush(Z_FINISH)")
        string = _to_input(string)
        self.deflater.setInput(string, 0, len(string))
        deflated = _get_deflate_data(self.deflater)
        self._size += len(string)
        self._crc32.update(string)
        if self._gzip:
            return self.GZIP_HEADER + deflated
        else:
            return deflated

    def flush(self, mode=Z_FINISH):
        if self._ended:
            raise error("compressobj may not be used after flush(Z_FINISH)")
        if mode not in _valid_flush_modes:
            raise ValueError, "Invalid flush option"
        if mode == Z_FINISH:
            self.deflater.finish()
        last = _get_deflate_data(self.deflater, mode)
        if mode == Z_FINISH:
            if self._gzip:
                last += self.GZIP_TRAILER_FORMAT.pack(
                    self._crc32.getValue(), self._size % sys.maxint)
            self.deflater.end()
            self._ended = True
        return last


class decompressobj(object):

    def __init__(self, wbits=MAX_WBITS):

        # Jython only uses wbits to determine to skip the header if it's negative;
        # but apparently there are some tests around this that we do some bogus
        # param checking

        if abs(wbits) < 8:
            raise ValueError, "Invalid initialization option"
        if abs(wbits) > 16:  # NOTE apparently this also implies being negative in CPython/zlib
            wbits = -1

        self.inflater = Inflater(wbits < 0)
        self._ended = False
        self.unused_data = ""
        self.unconsumed_tail = ""
        self.gzip = wbits < 0
        self.gzip_header_skipped = False

    def decompress(self, string, max_length=0):
        if self._ended:
            raise error("decompressobj may not be used after flush()")

        # unused_data is always "" until inflation is finished; then it is
        # the unused bytes of the input;
        # unconsumed_tail is whatever input was not used because max_length
        # was exceeded before inflation finished.
        # Thus, at most one of {unused_data, unconsumed_tail} may be non-empty.

        self.unconsumed_tail = ""
        if not self.inflater.finished() and not (self.gzip and not self.gzip_header_skipped):
            self.unused_data = ""

        if max_length < 0:
            raise ValueError("max_length must be a positive integer")

        # Suppress gzip header if present and wbits < 0
        if self.gzip and not self.gzip_header_skipped:
            string = self.unused_data + string
            self.unused_data = ""
            try:
                string = _skip_gzip_header(string)
            except IndexError:
                # need more input!
                self.unused_data = string
                return ""
            self.gzip_header_skipped = True

        string = _to_input(string)

        self.inflater.setInput(string)
        inflated = _get_inflate_data(self.inflater, max_length)

        r = self.inflater.getRemaining()
        if r:
            if max_length and not self.inflater.finished():
                self.unconsumed_tail = string[-r:]
            else:
                self.unused_data += string[-r:]

        return inflated

    def flush(self, length=None):
        if self._ended:
            raise error("decompressobj may not be used after flush()")
        if length is None:
            length = 0
        elif length <= 0:
            raise ValueError('length must be greater than zero')
        last = _get_inflate_data(self.inflater, length)
        self.inflater.end()
        return last

def _to_input(s):
    if isinstance(s, unicode):
        return s.encode('ascii')
    if isinstance(s, array.array):
        return s.tostring()
    if isinstance(s, basestring) or isinstance(s, buffer) or isinstance(s, memoryview):
        return s
    else:
        raise TypeError('must be string or read-only buffer, not %s' % type(s))

def _get_deflate_data(deflater, mode=Z_NO_FLUSH):
    buflen = 1024
    buf = jarray.zeros(buflen, 'b')
    s = StringIO()
    while not deflater.finished():
        l = deflater.deflate(buf, 0, buflen, _zlib_to_deflater.get(mode, Deflater.NO_FLUSH))
        if l == 0:
            break
        s.write(String(buf, 0, 0, l))
    s.seek(0)
    return s.read()

def _get_inflate_data(inflater, max_length=0):
    buf = jarray.zeros(1024, 'b')
    s = StringIO()
    total = 0
    while not inflater.finished():
        try:
            if max_length:
                l = inflater.inflate(buf, 0, min(1024, max_length - total))
            else:
                l = inflater.inflate(buf)
        except DataFormatException, e:
            raise error(str(e))

        if l == 0:
            break

        total += l
        s.write(String(buf, 0, 0, l))
        if max_length and total == max_length:
            break
    s.seek(0)
    return s.read()



FTEXT = 1
FHCRC = 2
FEXTRA = 4
FNAME = 8
FCOMMENT = 16

def _skip_gzip_header(string):
    # per format specified in https://tools.ietf.org/html/rfc1952
    
    # could we use bytearray instead?
    s = array.array("B", string)

    id1 = s[0]
    id2 = s[1]

    # Check gzip magic
    if id1 != 31 or id2 != 139:
        return string

    cm = s[2]
    flg = s[3]
    mtime = s[4:8]
    xfl = s[8]
    os = s[9]

    # skip fixed header, then figure out variable parts
    s = s[10:]

    if flg & FEXTRA:
        # skip extra field
        xlen = s[0] + s[1] * 256  # MSB ordering
        s = s[2 + xlen:]
    if flg & FNAME:
        # skip filename
        s = s[s.find("\x00")+1:]
    if flg & FCOMMENT:
        # skip comment
        s = s[s.find("\x00")+1:]
    if flg & FHCRC:
        # skip CRC16 for the header - might be nice to check of course
        s = s[2:]
    
    return s.tostring()



