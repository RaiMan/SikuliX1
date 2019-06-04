# implements a factory to create codec instances for a given java charset

import codecs

from array import array
from functools import partial
from java.lang import StringBuilder
from java.nio import ByteBuffer, CharBuffer
from java.nio.charset import Charset, IllegalCharsetNameException
from StringIO import StringIO


python_to_java = {
    'cp932': 'cp942',
    'iso2022_jp': 'ISO-2022-JP',
    'iso2022_jp_2': 'ISO-2022-JP-2',
    'iso2022_kr': 'ISO-2022-KR',
    'shift_jisx0213': 'x-SJIS_0213',
}



def _java_factory(encoding):
    encoding = python_to_java.get(encoding, encoding)

    supported = False
    try:
        supported = Charset.isSupported(encoding)
    except IllegalCharsetNameException:
        pass
    if not supported:
        return None, set()

    charset = Charset.forName(encoding)  # FIXME should we return this canonical name? could be best... TBD
    entry = codecs.CodecInfo(
        name=encoding,
        encode=Codec(encoding).encode,
        decode=Codec(encoding).decode,
        incrementalencoder=partial(IncrementalEncoder, encoding=encoding),
        incrementaldecoder=partial(IncrementalDecoder, encoding=encoding),
        streamreader=partial(StreamReader, encoding=encoding),
        streamwriter=partial(StreamWriter, encoding=encoding)
    )
    return entry, charset.aliases()


class Codec(object):  # (codecs.Codec):

    def __init__(self, encoding):
        self.encoding = encoding

    def decode(self, input, errors='strict', final=True):
        error_function = codecs.lookup_error(errors)
        input_buffer = ByteBuffer.wrap(array('b', input))
        decoder = Charset.forName(self.encoding).newDecoder()
        output_buffer = CharBuffer.allocate(min(max(int(len(input) / 2), 256), 1024))
        builder = StringBuilder(int(decoder.averageCharsPerByte() * len(input)))

        while True:
            result = decoder.decode(input_buffer, output_buffer, False)
            pos = output_buffer.position()
            output_buffer.rewind()
            builder.append(output_buffer.subSequence(0, pos))
            if result.isUnderflow():
                if final:
                    _process_incomplete_decode(self.encoding, input, error_function, input_buffer, builder)
                break
            _process_decode_errors(self.encoding, input, result, error_function, input_buffer, builder)

        return builder.toString(), input_buffer.position()

    def encode(self, input, errors='strict'):
        error_function = codecs.lookup_error(errors)
        # workaround non-BMP issues - need to get the exact count of chars, not codepoints
        input_buffer = CharBuffer.allocate(StringBuilder(input).length())
        input_buffer.put(input)
        input_buffer.rewind()
        encoder = Charset.forName(self.encoding).newEncoder()
        output_buffer = ByteBuffer.allocate(min(max(len(input) * 2, 256), 1024))
        builder = StringIO()

        while True:
            result = encoder.encode(input_buffer, output_buffer, True)
            pos = output_buffer.position()
            output_buffer.rewind()
            builder.write(output_buffer.array()[0:pos].tostring())
            if result.isUnderflow():
                break
            _process_encode_errors(self.encoding, input, result, error_function, input_buffer, builder)

        return builder.getvalue(), len(input)


class NonfinalCodec(Codec):

    def decode(self, input, errors='strict'):
        return Codec.decode(self, input, errors, final=False)


class IncrementalEncoder(codecs.IncrementalEncoder):

    def __init__(self, errors='strict', encoding=None):
        assert encoding
        self.encoding = encoding
        self.errors = errors
        self.encoder = Charset.forName(self.encoding).newEncoder()
        self.output_buffer = ByteBuffer.allocate(1024)

    def encode(self, input, final=False):
        error_function = codecs.lookup_error(self.errors)
        # workaround non-BMP issues - need to get the exact count of chars, not codepoints
        input_buffer = CharBuffer.allocate(StringBuilder(input).length())
        input_buffer.put(input)
        input_buffer.rewind()
        self.output_buffer.rewind()
        builder = StringIO()

        while True:
            result = self.encoder.encode(input_buffer, self.output_buffer, final)
            pos = self.output_buffer.position()
            self.output_buffer.rewind()
            builder.write(self.output_buffer.array()[0:pos].tostring())
            if result.isUnderflow():
                break
            _process_encode_errors(self.encoding, input, result, error_function, input_buffer, builder)

        return builder.getvalue()


class IncrementalDecoder(codecs.IncrementalDecoder):

    def __init__(self, errors='strict', encoding=None,):
        assert encoding
        self.encoding = encoding
        self.errors = errors
        self.decoder = Charset.forName(self.encoding).newDecoder()
        self.output_buffer = CharBuffer.allocate(1024)
        self.buffer = ''

    def decode(self, input, final=False):
        error_function = codecs.lookup_error(self.errors)
        input_array = array('b', self.buffer + str(input))
        input_buffer = ByteBuffer.wrap(input_array)
        builder = StringBuilder(int(self.decoder.averageCharsPerByte() * len(input)))
        self.output_buffer.rewind()

        while True:
            result = self.decoder.decode(input_buffer, self.output_buffer, final)
            pos = self.output_buffer.position()
            self.output_buffer.rewind()
            builder.append(self.output_buffer.subSequence(0, pos))
            if result.isUnderflow():
                if not final:
                    # Keep around any remaining input for next call to decode
                    self.buffer = input_array[input_buffer.position():input_buffer.limit()].tostring()
                else:
                    _process_incomplete_decode(self.encoding, input, error_function, input_buffer, builder)
                break
            _process_decode_errors(self.encoding, input, result, error_function, input_buffer, builder)

        return builder.toString()

    def reset(self):
        self.buffer = ""
        self.decoder.reset()

    def getstate(self):
        # No way to extract the internal state of a Java decoder.
        return self.buffer or "", 0

    def setstate(self, state):
        self.buffer, _ = state or ("", 0)
        # No way to restore: reset possible EOF state.
        self.decoder.reset()


class StreamWriter(NonfinalCodec, codecs.StreamWriter):

    def __init__(self, stream, errors='strict', encoding=None, ):
        NonfinalCodec.__init__(self, encoding)
        codecs.StreamWriter.__init__(self, stream, errors)


class StreamReader(NonfinalCodec, codecs.StreamReader):

    def __init__(self, stream, errors='strict', encoding=None, ):
        NonfinalCodec.__init__(self, encoding)
        codecs.StreamReader.__init__(self, stream, errors)


def _process_decode_errors(encoding, input, result, error_function, input_buffer, builder):
    if result.isError():
        e = UnicodeDecodeError(
            encoding,
            input, 
            input_buffer.position(),
            input_buffer.position() + result.length(),
            'illegal multibyte sequence')
        replacement, pos = error_function(e)
        if not isinstance(replacement, unicode):
            raise TypeError()
        pos = int(pos)
        if pos < 0:
            pos = input_buffer.limit() + pos
        if pos > input_buffer.limit():
            raise IndexError()
        builder.append(replacement)
        input_buffer.position(pos)


def _process_incomplete_decode(encoding, input, error_function, input_buffer, builder):
    if input_buffer.position() < input_buffer.limit():
        e = UnicodeDecodeError(
            encoding,
            input, 
            input_buffer.position(),
            input_buffer.limit(),
            'illegal multibyte sequence')
        replacement, pos = error_function(e)
        if not isinstance(replacement, unicode):
            raise TypeError()
        pos = int(pos)
        if pos < 0:
            pos = input_buffer.limit() + pos
        if pos > input_buffer.limit():
            raise IndexError()
        builder.append(replacement)
        input_buffer.position(pos)


def _get_unicode(input_buffer, result):
    return input_buffer.subSequence(0, result.length()).toString()


def _process_encode_errors(encoding, input, result, error_function, input_buffer, builder):
    if result.isError():
        e = UnicodeEncodeError(
            encoding,
            input, 
            input_buffer.position(),
            input_buffer.position() + result.length(),
            'illegal multibyte sequence')
        replacement, pos = error_function(e)
        if not isinstance(replacement, unicode):
            raise TypeError()
        pos = int(pos)
        if pos < 0:
            pos = input_buffer.limit() + pos
        if pos > input_buffer.limit():
            raise IndexError()
        builder.write(str(replacement))
        input_buffer.position(pos)
