# make an exact copy of ElementTree's namespace here to support even
# private API usage
from xml.etree.ElementTree import (
    Comment, Element, ElementPath, ElementTree, PI, ParseError,
    ProcessingInstruction, QName, SubElement, TreeBuilder, VERSION, XML, XMLID,
    XMLParser, XMLTreeBuilder, _Element, _ElementInterface, _SimpleElementPath,
    __all__, __doc__, __file__, __name__, __package__, _encode,
    _escape_attrib, _escape_cdata, _namespace_map,
    _raise_serialization_error, dump, fromstring, fromstringlist,
    iselement, iterparse, parse, re, register_namespace, sys, tostring,
    tostringlist)
