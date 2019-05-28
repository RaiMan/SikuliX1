import codecs
import re
from java.net import IDN
try:
    # import from jarjar-ed version if available
    from org.python.icu.text import StringPrep, StringPrepParseException
except ImportError:
    # dev version of Jython, so use extlibs
    from com.ibm.icu.text import StringPrep, StringPrepParseException


# IDNA section 3.1
dots = re.compile(u"[\u002E\u3002\uFF0E\uFF61]")


def nameprep(label):
    try:
        return StringPrep.getInstance(StringPrep.RFC3491_NAMEPREP).prepare(
            label, StringPrep.ALLOW_UNASSIGNED)
    except StringPrepParseException, e:
        raise UnicodeError("Invalid character")
    

def ToASCII(label):
    return IDN.toASCII(label)


def ToUnicode(label):
    return IDN.toUnicode(label)


# BELOW is the implementation shared with CPython. TODO we should merge.

### Codec APIs

class Codec(codecs.Codec):
    def encode(self,input,errors='strict'):

        if errors != 'strict':
            # IDNA is quite clear that implementations must be strict
            raise UnicodeError("unsupported error handling "+errors)

        if not input:
            return "", 0

        result = []
        labels = dots.split(input)
        if labels and len(labels[-1])==0:
            trailing_dot = '.'
            del labels[-1]
        else:
            trailing_dot = ''
        for label in labels:
            result.append(ToASCII(label))
        # Join with U+002E
        return ".".join(result)+trailing_dot, len(input)

    def decode(self,input,errors='strict'):

        if errors != 'strict':
            raise UnicodeError("Unsupported error handling "+errors)

        if not input:
            return u"", 0

        # IDNA allows decoding to operate on Unicode strings, too.
        if isinstance(input, unicode):
            labels = dots.split(input)
        else:
            # Must be ASCII string
            input = str(input)
            unicode(input, "ascii")
            labels = input.split(".")

        if labels and len(labels[-1]) == 0:
            trailing_dot = u'.'
            del labels[-1]
        else:
            trailing_dot = u''

        result = []
        for label in labels:
            result.append(ToUnicode(label))

        return u".".join(result)+trailing_dot, len(input)

class IncrementalEncoder(codecs.BufferedIncrementalEncoder):
    def _buffer_encode(self, input, errors, final):
        if errors != 'strict':
            # IDNA is quite clear that implementations must be strict
            raise UnicodeError("unsupported error handling "+errors)

        if not input:
            return ("", 0)

        labels = dots.split(input)
        trailing_dot = u''
        if labels:
            if not labels[-1]:
                trailing_dot = '.'
                del labels[-1]
            elif not final:
                # Keep potentially unfinished label until the next call
                del labels[-1]
                if labels:
                    trailing_dot = '.'

        result = []
        size = 0
        for label in labels:
            result.append(ToASCII(label))
            if size:
                size += 1
            size += len(label)

        # Join with U+002E
        result = ".".join(result) + trailing_dot
        size += len(trailing_dot)
        return (result, size)

class IncrementalDecoder(codecs.BufferedIncrementalDecoder):
    def _buffer_decode(self, input, errors, final):
        if errors != 'strict':
            raise UnicodeError("Unsupported error handling "+errors)

        if not input:
            return (u"", 0)

        # IDNA allows decoding to operate on Unicode strings, too.
        if isinstance(input, unicode):
            labels = dots.split(input)
        else:
            # Must be ASCII string
            input = str(input)
            unicode(input, "ascii")
            labels = input.split(".")

        trailing_dot = u''
        if labels:
            if not labels[-1]:
                trailing_dot = u'.'
                del labels[-1]
            elif not final:
                # Keep potentially unfinished label until the next call
                del labels[-1]
                if labels:
                    trailing_dot = u'.'

        result = []
        size = 0
        for label in labels:
            result.append(ToUnicode(label))
            if size:
                size += 1
            size += len(label)

        result = u".".join(result) + trailing_dot
        size += len(trailing_dot)
        return (result, size)

class StreamWriter(Codec,codecs.StreamWriter):
    pass

class StreamReader(Codec,codecs.StreamReader):
    pass

### encodings module API

def getregentry():
    return codecs.CodecInfo(
        name='idna',
        encode=Codec().encode,
        decode=Codec().decode,
        incrementalencoder=IncrementalEncoder,
        incrementaldecoder=IncrementalDecoder,
        streamwriter=StreamWriter,
        streamreader=StreamReader,
    )
