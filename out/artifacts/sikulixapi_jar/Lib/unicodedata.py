import java.lang.Character
try:
    # import from jarjar-ed version
    from org.python.icu.text import Normalizer
    from org.python.icu.lang import UCharacter, UProperty
    from org.python.icu.util import VersionInfo
    from org.python.icu.lang.UCharacter import EastAsianWidth, DecompositionType
    from org.python.icu.lang.UCharacterEnums import ECharacterCategory, ECharacterDirection
except ImportError:
    # development version of Jython, so use extlibs
    from com.ibm.icu.text import Normalizer
    from com.ibm.icu.lang import UCharacter, UProperty
    from com.ibm.icu.util import VersionInfo
    from com.ibm.icu.lang.UCharacter import EastAsianWidth, DecompositionType
    from com.ibm.icu.lang.UCharacterEnums import ECharacterCategory, ECharacterDirection


__all__ = (
    "bidirectional", "category", "combining", "decimal", "decomposition", "digit", "east_asian_width",
    "lookup", "mirrored", "name", "normalize", "numeric", "unidata_version")


_forms = {
    'NFC':  Normalizer.NFC,
    'NFKC': Normalizer.NFKC,
    'NFD':  Normalizer.NFD,
    'NFKD': Normalizer.NFKD
}

Nonesuch = object()   # to distinguish from None, which is a valid return value for some functions


def _validate_unichr(unichr):
    if not(isinstance(unichr, unicode)):
        raise TypeError("must be unicode, not {}".format(type(unichr).__name__))
    if len(unichr) > 1 or len(unichr) == 0:
        raise TypeError("need a single Unicode character as parameter")


def _get_codepoint(unichr):
    _validate_unichr(unichr)
    return ord(unichr)


def name(unichr, default=Nonesuch):
    # handle None
    n = UCharacter.getName(_get_codepoint(unichr))
    if n is None:
        if default is not Nonesuch:
            return default
        else:
            raise ValueError("no such name")
    return n


def lookup(name):
    codepoint = UCharacter.getCharFromName(name)
    if codepoint == -1:
        raise KeyError("undefined character name '{}".format(name))
    return unichr(codepoint)


def digit(unichr, default=Nonesuch):
    d = UCharacter.digit(_get_codepoint(unichr))
    if d == -1:
        if default is not Nonesuch:
            return default
        else:
            raise ValueError("not a digit")
    return d


def decimal(unichr, default=Nonesuch):
    d = UCharacter.getNumericValue(_get_codepoint(unichr))
    if d < 0 or d > 9:
        if default is not Nonesuch:
            return default
        else:
            raise ValueError("not a decimal")
    return d


def numeric(unichr, default=Nonesuch):
    n = UCharacter.getUnicodeNumericValue(_get_codepoint(unichr))
    if n == UCharacter.NO_NUMERIC_VALUE:
        if default is not Nonesuch:
            return default
        else:
            raise ValueError("not a numeric")
    return n


_decomp = {
    DecompositionType.CANONICAL: "canonical",
    DecompositionType.CIRCLE: "circle",
    DecompositionType.COMPAT: "compat", 
    DecompositionType.FINAL: "final", 
    DecompositionType.FONT: "font",
    DecompositionType.FRACTION: "fraction",
    DecompositionType.INITIAL: "initial",
    DecompositionType.ISOLATED: "isolated",
    DecompositionType.MEDIAL: "medial",
    DecompositionType.NARROW: "narrow",
    DecompositionType.NOBREAK: "nobreak",
    DecompositionType.NONE: None,
    DecompositionType.SMALL: "small",
    DecompositionType.SQUARE: "square",
    DecompositionType.SUB: "sub",
    DecompositionType.SUPER: "super",
    DecompositionType.VERTICAL: "vertical", 
    DecompositionType.WIDE: "wide"
}

def _get_decomp_type(unichr):
    if unichr == u"\u2044":  # FRACTION SLASH
        # special case this for CPython compatibility even though this returns as not being combining, eg, see
        # http://www.fileformat.info/info/unicode/char/2044/index.htm
        return "fraction"
    else:
        return _decomp[UCharacter.getIntPropertyValue(ord(unichr), UProperty.DECOMPOSITION_TYPE)]

def decomposition(unichr):
    _validate_unichr(unichr)
    d = Normalizer.decompose(unichr, True)
    decomp_type = None
    if len(d) == 1:
        decomp_type = _get_decomp_type(unichr)
    else:
        for c in d:
            decomp_type = _get_decomp_type(c)
            # print "Got a decomp_type %r %r %r" % (c, d, decomp_type)
            if decomp_type is not None:
                break
    hexed = " ".join(("{0:04X}".format(ord(c)) for c in d))
    if decomp_type:
        return "<{}> {}".format(decomp_type, hexed)
    elif len(d) == 1:
        return ""
    else:
        return hexed


# To map from ICU4J enumerations for category, bidirection, and
# east_asian_width to the underlying property values that Python uses
# from UnicodeData.txt required a manual mapping between the following
# two files:
#
# http://icu-project.org/apiref/icu4j/constant-values.html
# http://www.unicode.org/Public/6.3.0/ucd/PropertyValueAliases.txt

_cat = {
    ECharacterCategory.COMBINING_SPACING_MARK: "Mc",
    ECharacterCategory.CONNECTOR_PUNCTUATION: "Pc",
    ECharacterCategory.CONTROL: "Cc",
    ECharacterCategory.CURRENCY_SYMBOL: "Sc",
    ECharacterCategory.DASH_PUNCTUATION: "Pd",
    ECharacterCategory.DECIMAL_DIGIT_NUMBER: "Nd",
    ECharacterCategory.ENCLOSING_MARK: "Me",
    ECharacterCategory.END_PUNCTUATION: "Pe",
    ECharacterCategory.FINAL_PUNCTUATION: "Pf",
    ECharacterCategory.FORMAT: "Cf",
    # per http://icu-project.org/apiref/icu4j/com/ibm/icu/lang/UCharacterEnums.ECharacterCategory.html#GENERAL_OTHER_TYPES
    # - no characters in [UnicodeData.txt] have this property
    ECharacterCategory.GENERAL_OTHER_TYPES: "Cn Not Assigned",
    ECharacterCategory.INITIAL_PUNCTUATION: "Pi",
    ECharacterCategory.LETTER_NUMBER: "Nl",
    ECharacterCategory.LINE_SEPARATOR: "Zl",
    ECharacterCategory.LOWERCASE_LETTER: "Ll",
    ECharacterCategory.MATH_SYMBOL: "Sm",
    ECharacterCategory.MODIFIER_LETTER: "Lm",
    ECharacterCategory.MODIFIER_SYMBOL: "Sk",
    ECharacterCategory.NON_SPACING_MARK: "Mn",
    ECharacterCategory.OTHER_LETTER: "Lo",
    ECharacterCategory.OTHER_NUMBER: "No",
    ECharacterCategory.OTHER_PUNCTUATION: "Po",
    ECharacterCategory.OTHER_SYMBOL: "So",
    ECharacterCategory.PARAGRAPH_SEPARATOR: "Zp",
    ECharacterCategory.PRIVATE_USE: "Co",
    ECharacterCategory.SPACE_SEPARATOR: "Zs",
    ECharacterCategory.START_PUNCTUATION: "Ps",
    ECharacterCategory.SURROGATE: "Cs",
    ECharacterCategory.TITLECASE_LETTER: "Lt",
    ECharacterCategory.UNASSIGNED: "Cn",
    ECharacterCategory.UPPERCASE_LETTER: "Lu",
}

def category(unichr):
    return _cat[UCharacter.getType(_get_codepoint(unichr))]


_dir = {
    ECharacterDirection.ARABIC_NUMBER: "An",
    ECharacterDirection.BLOCK_SEPARATOR: "B",
    ECharacterDirection.BOUNDARY_NEUTRAL: "BN",
    ECharacterDirection.COMMON_NUMBER_SEPARATOR: "CS",
    ECharacterDirection.DIR_NON_SPACING_MARK: "NSM",
    ECharacterDirection.EUROPEAN_NUMBER: "EN",
    ECharacterDirection.EUROPEAN_NUMBER_SEPARATOR: "ES",
    ECharacterDirection.EUROPEAN_NUMBER_TERMINATOR: "ET",
    ECharacterDirection.FIRST_STRONG_ISOLATE: "FSI",
    ECharacterDirection.LEFT_TO_RIGHT: "L",
    ECharacterDirection.LEFT_TO_RIGHT_EMBEDDING: "LRE",
    ECharacterDirection.LEFT_TO_RIGHT_ISOLATE: "LRI",
    ECharacterDirection.LEFT_TO_RIGHT_OVERRIDE: "LRO",
    ECharacterDirection.OTHER_NEUTRAL: "ON",
    ECharacterDirection.POP_DIRECTIONAL_FORMAT: "PDF",
    ECharacterDirection.POP_DIRECTIONAL_ISOLATE: "PDI",
    ECharacterDirection.RIGHT_TO_LEFT: "R",
    ECharacterDirection.RIGHT_TO_LEFT_ARABIC: "AL",
    ECharacterDirection.RIGHT_TO_LEFT_EMBEDDING: "RLE",
    ECharacterDirection.RIGHT_TO_LEFT_ISOLATE: "RLI",
    ECharacterDirection.RIGHT_TO_LEFT_OVERRIDE: "RLO",
    ECharacterDirection.SEGMENT_SEPARATOR: "S",
    ECharacterDirection.WHITE_SPACE_NEUTRAL: "WS"
}

def bidirectional(unichr):
    return _dir[UCharacter.getDirection(_get_codepoint(unichr))]


def combining(unichr):
    return UCharacter.getCombiningClass(_get_codepoint(unichr))


def mirrored(unichr):
    return UCharacter.isMirrored(_get_codepoint(unichr))


_eaw = {
    # http://www.unicode.org/reports/tr11/
    EastAsianWidth.AMBIGUOUS : "A",
    EastAsianWidth.COUNT     : "?",  # apparently not used, see above TR
    EastAsianWidth.FULLWIDTH : "F",
    EastAsianWidth.HALFWIDTH : "H", 
    EastAsianWidth.NARROW    : "Na",
    EastAsianWidth.NEUTRAL   : "N",
    EastAsianWidth.WIDE      : "W"
}

def east_asian_width(unichr):
    return _eaw[UCharacter.getIntPropertyValue(_get_codepoint(unichr), UProperty.EAST_ASIAN_WIDTH)]


def normalize(form, unistr):
    """
    Return the normal form 'form' for the Unicode string unistr.  Valid
    values for form are 'NFC', 'NFKC', 'NFD', and 'NFKD'.
    """

    try:
        normalizer_form = _forms[form]
    except KeyError:
        raise ValueError('invalid normalization form')

    return Normalizer.normalize(unistr, normalizer_form)


def get_icu_version():
    versions = []
    for k in VersionInfo.__dict__.iterkeys():
        if k.startswith("UNICODE_"):
            v = getattr(VersionInfo, k)
            versions.append((v.getMajor(), v.getMinor(), v.getMilli()))
    return ".".join(str(x) for x in max(versions))


unidata_version = get_icu_version()
