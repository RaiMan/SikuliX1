"""distutils.jythoncompiler

Jython does not support extension libraries. This CCompiler simply
raises CCompiler exceptions.
"""

from distutils.ccompiler import CCompiler
import warnings

class JythonCompiler(CCompiler):

    """Refuses to compile C extensions on Jython"""

    compiler_type = 'jython'
    executables = {}

    def refuse_compilation(self, *args, **kwargs):
        """Refuse compilation"""
        warnings.warn('Compiling extensions is not supported on Jython')
        return []

    preprocess = compile = create_static_lib = link = refuse_compilation
