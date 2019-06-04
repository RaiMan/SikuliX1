#!/usr/bin/env python

from sys import float_info
import math


mant_dig = float_info.mant_dig
etiny = float_info.min_exp - mant_dig

def fsum(iterable):
    """Full precision summation.  Compute sum(iterable) without any
    intermediate accumulation of error.  Based on the 'lsum' function
    at http://code.activestate.com/recipes/393090/

    """
    tmant, texp = 0, 0
    for x in iterable:
        mant, exp = math.frexp(x)
        mant, exp = int(math.ldexp(mant, mant_dig)), exp - mant_dig
        if texp > exp:
            tmant <<= texp-exp
            texp = exp
        else:
            mant <<= exp-texp
        tmant += mant

    # Round tmant * 2**texp to a float.  The original recipe
    # used float(str(tmant)) * 2.0**texp for this, but that's
    # a little unsafe because str -> float conversion can't be
    # relied upon to do correct rounding on all platforms.
    tail = max(len(bin(abs(tmant)))-2 - mant_dig, etiny - texp)
    if tail > 0:
        h = 1 << (tail-1)
        tmant = tmant // (2*h) + bool(tmant & h and tmant & 3*h-1)
        texp += tail
    return math.ldexp(tmant, texp)
