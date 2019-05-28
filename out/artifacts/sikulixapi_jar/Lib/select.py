# dispatches to _socket for actual implementation

from _socket import (
    POLLIN,
    POLLOUT,
    POLLPRI,
    POLLERR,
    POLLHUP,
    POLLNVAL,
    error,
    poll,
    select)

# backwards compatibility with Jython 2.5
cpython_compatible_select = select

__all__ = [
    "POLLIN", "POLLOUT", "POLLPRI", "POLLERR", "POLLHUP", "POLLNVAL", 
    "error", "poll", "select", "cpython_compatible_select"]
