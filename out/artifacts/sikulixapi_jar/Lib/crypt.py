from jnr.posix import POSIXFactory
from org.python.modules.posix import PythonPOSIXHandler


__all__ = ["crypt"]
_posix = POSIXFactory.getPOSIX(PythonPOSIXHandler(), True)


def crypt(word, salt):
    return _posix.crypt(word, salt)
