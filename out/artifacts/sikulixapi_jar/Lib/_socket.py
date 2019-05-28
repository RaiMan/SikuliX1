import array
import encodings.idna
import errno
import jarray
import logging
import numbers
import pprint
import struct
import sys
import time
import _google_ipaddr_r234
from collections import namedtuple, Iterable
from contextlib import contextmanager
from functools import partial, wraps
from itertools import chain
from jythonlib import MapMaker, dict_builder
from numbers import Number
from StringIO import StringIO
from threading import Condition, Lock
from types import MethodType, NoneType

import java
from java.io import IOException, InterruptedIOException
from java.lang import Thread, ArrayIndexOutOfBoundsException, IllegalStateException
from java.net import InetAddress, InetSocketAddress
from java.nio.channels import ClosedChannelException
from java.security.cert import CertificateException
from java.util import NoSuchElementException
from java.util.concurrent import (
    ArrayBlockingQueue, CopyOnWriteArrayList, CountDownLatch, LinkedBlockingQueue,
    ExecutionException, RejectedExecutionException, ThreadFactory,
    TimeoutException, TimeUnit)
from java.util.concurrent.atomic import AtomicBoolean, AtomicLong
from javax.net.ssl import SSLPeerUnverifiedException, SSLException, SSLHandshakeException

try:
    # jarjar-ed version
    from org.python.netty.bootstrap import Bootstrap, ChannelFactory, ServerBootstrap
    from org.python.netty.buffer import PooledByteBufAllocator, Unpooled
    from org.python.netty.channel import ChannelException as NettyChannelException, ChannelInboundHandlerAdapter, ChannelInitializer, ChannelOption
    from org.python.netty.channel.nio import NioEventLoopGroup
    from org.python.netty.channel.socket import DatagramPacket
    from org.python.netty.channel.socket.nio import NioDatagramChannel, NioSocketChannel, NioServerSocketChannel
    from org.python.netty.handler.ssl import NotSslRecordException

except ImportError:
    # dev version from extlibs
    from io.netty.bootstrap import Bootstrap, ChannelFactory, ServerBootstrap
    from io.netty.buffer import PooledByteBufAllocator, Unpooled
    from io.netty.channel import ChannelException as NettyChannelException, ChannelInboundHandlerAdapter, ChannelInitializer, ChannelOption
    from io.netty.channel.nio import NioEventLoopGroup
    from io.netty.channel.socket import DatagramPacket
    from io.netty.channel.socket.nio import NioDatagramChannel, NioSocketChannel, NioServerSocketChannel
    from io.netty.handler.ssl import NotSslRecordException

log = logging.getLogger("_socket")
log.setLevel(level=logging.WARNING)


def _debug():
    FORMAT = '%(asctime)-15s %(threadName)s %(levelname)s %(funcName)s %(message)s %(sock)s'
    debug_sh = logging.StreamHandler()
    debug_sh.setFormatter(logging.Formatter(FORMAT))
    log.addHandler(debug_sh)
    log.setLevel(level=logging.DEBUG)

# _debug()  # UNCOMMENT to get logging of socket activity


# Constants
###########

has_ipv6 = True # IPV6 FTW!
_GLOBAL_DEFAULT_TIMEOUT = object()
_EPHEMERAL_ADDRESS = InetSocketAddress(0)
_BOUND_EPHEMERAL_ADDRESS = object()

# FIXME most constants should come from JNR if possible; they may be
# arbitrary for the implementation of socket/ssl/select purposes, but
# some misbehaved code may want to use the arbitrary numbers

SHUT_RD   = 0
SHUT_WR   = 1
SHUT_RDWR = 2

AF_UNSPEC = 0
AF_INET   = 2
AF_INET6  = 23

AI_PASSIVE     = 1
AI_CANONNAME   = 2
AI_NUMERICHOST = 4
AI_V4MAPPED    = 8
AI_ALL         = 16
AI_ADDRCONFIG  = 32
AI_NUMERICSERV = 1024

EAI_NONAME     = -2
EAI_SERVICE    = -8
EAI_ADDRFAMILY = -9

NI_NUMERICHOST              = 1
NI_NUMERICSERV              = 2
NI_NOFQDN                   = 4
NI_NAMEREQD                 = 8
NI_DGRAM                    = 16
NI_MAXSERV                  = 32
NI_IDN                      = 64
NI_IDN_ALLOW_UNASSIGNED     = 128
NI_IDN_USE_STD3_ASCII_RULES = 256
NI_MAXHOST                  = 1025

SOCK_DGRAM     = 1
SOCK_STREAM    = 2
SOCK_RAW       = 3 # not supported
SOCK_RDM       = 4 # not supported
SOCK_SEQPACKET = 5 # not supported

SOL_SOCKET = 0xFFFF

IPPROTO_AH       =  51 # not supported
IPPROTO_DSTOPTS  =  60 # not supported
IPPROTO_ESP      =  50 # not supported
IPPROTO_FRAGMENT =  44 # not supported
IPPROTO_GGP      =   3 # not supported
IPPROTO_HOPOPTS  =   0 # not supported
IPPROTO_ICMP     =   1 # not supported
IPPROTO_ICMPV6   =  58 # not supported
IPPROTO_IDP      =  22 # not supported
IPPROTO_IGMP     =   2 # not supported
IPPROTO_IP       =   0
IPPROTO_IPV4     =   4 # not supported
IPPROTO_IPV6     =  41 # not supported
IPPROTO_MAX      = 256 # not supported
IPPROTO_ND       =  77 # not supported
IPPROTO_NONE     =  59 # not supported
IPPROTO_PUP      =  12 # not supported
IPPROTO_RAW      = 255 # not supported
IPPROTO_ROUTING  =  43 # not supported
SOL_TCP = IPPROTO_TCP = 6
IPPROTO_UDP      =  17

SO_ACCEPTCONN  = 1
SO_BROADCAST   = 2
SO_ERROR       = 4
SO_KEEPALIVE   = 8
SO_LINGER      = 16
SO_OOBINLINE   = 32
SO_RCVBUF      = 64
SO_REUSEADDR   = 128
SO_SNDBUF      = 256
SO_TIMEOUT     = 512
SO_TYPE        = 1024

# Options with negative constants are not supported
# They are being added here so that code that refers to them
# will not break with an AttributeError

SO_DEBUG            = -1
SO_DONTROUTE        = -1
SO_RCVLOWAT         = -16
SO_RCVTIMEO         = -32
SO_REUSEPORT        = -64
SO_SNDLOWAT         = -128
SO_SNDTIMEO         = -256
SO_USELOOPBACK      = -512

TCP_NODELAY    = 2048

INADDR_ANY = "0.0.0.0"
INADDR_BROADCAST = "255.255.255.255"

IN6ADDR_ANY_INIT = "::"

POLLIN   = 1
POLLOUT  = 2
POLLPRI  = 4   # Ignored - not supportable on Java
POLLERR  = 8
POLLHUP  = 16
POLLNVAL = 32  # Polled when not open - no Netty channel


# Specific constants for socket-reboot:

# Keep the highest possible precision for converting from Python's use
# of floating point for durations to Java's use of both a long
# duration and a specific unit, in this case TimeUnit.NANOSECONDS
_TO_NANOSECONDS = 1000000000

_PEER_CLOSED = object()


# Event loop management
#######################

_NUM_THREADS = 10

# Use daemon threads for the event loop group. This is just fine
# because these threads only handle ephemeral data, such as performing
# SSL wrap/unwrap.


class DaemonThreadFactory(ThreadFactory):

    thread_count = AtomicLong()

    def __init__(self, label):
        self.label = label

    def newThread(self, runnable):
        t = Thread(runnable)
        t.daemon = True
        t.name = self.label % (self.thread_count.getAndIncrement())
        return t


NIO_GROUP = NioEventLoopGroup(_NUM_THREADS, DaemonThreadFactory("Jython-Netty-Client-%s"))


def _check_threadpool_for_pending_threads(group):
    pending_threads = []
    for t in group:
        pending_count = t.pendingTasks()
        if pending_count > 0:
            pending_threads.append((t, pending_count))
    log.debug("Pending threads in Netty pool: %s", pprint.pformat(pending_threads),  extra={"sock": "*"})
    return pending_threads


def _shutdown_threadpool():
    log.debug("Shutting down thread pool...", extra={"sock": "*"})
    # FIXME this timeout probably should be configurable; for client
    # usage that have completed this probably only produces scary
    # messages at worst, but TBD; in particular this may because we
    # are seeing closes both in SSL and at the socket level

    NIO_GROUP.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS)
    log.debug("Shut down thread pool", extra={"sock": "*"})

# Ensure deallocation of thread pool if PySystemState.cleanup is
# called; this includes in the event of sigterm

sys.registerCloser(_shutdown_threadpool)


# Error management
##################

class error(IOError): pass
class herror(error): pass
class gaierror(error): pass
class timeout(error): pass
class SSLError(error): pass   # FIXME import from ssl, solving the usual mutual import schema

SSL_ERROR_SSL = 1
SSL_ERROR_WANT_READ = 2
SSL_ERROR_WANT_WRITE = 3
SSL_ERROR_WANT_X509_LOOKUP = 4
SSL_ERROR_SYSCALL = 5
SSL_ERROR_ZERO_RETURN = 6
SSL_ERROR_WANT_CONNECT = 7
SSL_ERROR_EOF = 8
SSL_ERROR_INVALID_ERROR_CODE = 9
SSL_UNKNOWN_PROTOCOL = 10   # FIXME check code from OpenSSL


def _add_exception_attrs(exc):
    exc.errno = exc[0]
    exc.strerror = exc[1]
    return exc


def _unmapped_exception(exc):
    return _add_exception_attrs(error(-1, 'Unmapped exception: %s' % exc))


def java_net_socketexception_handler(exc):
    if exc.message.startswith("Address family not supported by protocol family"):
        return _add_exception_attrs(
            error(errno.EAFNOSUPPORT,
                  'Address family not supported by protocol family: See http://wiki.python.org/jython/NewSocketModule#IPV6_address_support'))
    if exc.message.startswith('Address already in use'):
        return error(errno.EADDRINUSE, 'Address already in use')
    return _unmapped_exception(exc)


def would_block_error(exc=None):
    return _add_exception_attrs(
        error(errno.EWOULDBLOCK, 'The socket operation could not complete without blocking'))


_exception_map = {

    # javaexception : callable that raises the python equivalent exception, or None to stub out as unmapped

    IOException            : lambda x: error(errno.ECONNRESET, 'Software caused connection abort'),
    InterruptedIOException : lambda x: timeout(errno.ETIMEDOUT, 'timed out'),
    IllegalStateException  : lambda x: error(errno.EPIPE, 'Illegal state exception'),

    java.net.BindException            : lambda x: error(errno.EADDRINUSE, 'Address already in use'),
    java.net.ConnectException         : lambda x: error(errno.ECONNREFUSED, 'Connection refused'),
    java.net.NoRouteToHostException   : lambda x: error(errno.EHOSTUNREACH, 'No route to host'),
    java.net.PortUnreachableException : None,
    java.net.ProtocolException        : None,
    java.net.SocketException          : java_net_socketexception_handler,
    java.net.SocketTimeoutException   : lambda x: timeout(errno.ETIMEDOUT, 'timed out'),
    java.net.UnknownHostException     : lambda x: gaierror(errno.EGETADDRINFOFAILED, 'getaddrinfo failed'),

    java.nio.channels.AlreadyConnectedException       : lambda x: error(errno.EISCONN, 'Socket is already connected'),
    java.nio.channels.AsynchronousCloseException      : None,
    java.nio.channels.CancelledKeyException           : None,
    java.nio.channels.ClosedByInterruptException      : None,
    java.nio.channels.ClosedChannelException          : lambda x: error(errno.ECONNRESET, 'Socket closed'),
    java.nio.channels.ClosedSelectorException         : None,
    java.nio.channels.ConnectionPendingException      : None,
    java.nio.channels.IllegalBlockingModeException    : None,
    java.nio.channels.IllegalSelectorException        : None,
    java.nio.channels.NoConnectionPendingException    : None,
    java.nio.channels.NonReadableChannelException     : None,
    java.nio.channels.NonWritableChannelException     : None,
    java.nio.channels.NotYetBoundException            : None,
    java.nio.channels.NotYetConnectedException        : None,
    java.nio.channels.UnresolvedAddressException      : lambda x: gaierror(errno.EGETADDRINFOFAILED, 'getaddrinfo failed'),
    java.nio.channels.UnsupportedAddressTypeException : None,

    SSLPeerUnverifiedException: lambda x: SSLError(SSL_ERROR_SSL, x.message),
    # FIXME
    # CPython wraps with a message like so:
    #   ssl.SSLError: [SSL: UNKNOWN_PROTOCOL] unknown protocol (_ssl.c:590)
    # Currently this error handler produces this message:
    #   _socket.SSLError: [Errno 1] not an SSL/TLS record: 48692c2049276d206120636c69656e7421
    NotSslRecordException: lambda x: SSLError(SSL_UNKNOWN_PROTOCOL, x.message),
}


def _map_exception(java_exception):
    if isinstance(java_exception, NettyChannelException):
        java_exception = java_exception.cause  # unwrap
    if isinstance(java_exception, SSLException) or isinstance(java_exception, CertificateException):
        cause = java_exception.cause
        if cause:
            # netty is freaking backwards here. The original exception may be CertificateException
            # but netty wraps it in SSLHandshakeException, we need to unwrap to present the right message
            if isinstance(cause, SSLHandshakeException):
                if isinstance(cause.cause, CertificateException):
                    java_exception = cause.cause

            msg = "%s (%s)" % (java_exception.message, cause)
        else:
            msg = java_exception.message
        py_exception = SSLError(SSL_ERROR_SSL, msg)
    else:
        # Netty 4.1.6 or higher wraps the connection exception in a
        # private static class that inherits from ConnectException, so
        # need to work around.
        if isinstance(java_exception, java.net.ConnectException):
            mapped_exception = _exception_map.get(java.net.ConnectException)
        # Netty AnnotatedNoRouteToHostException extends NoRouteToHostException
        # so also needs work around.
        elif isinstance(java_exception, java.net.NoRouteToHostException):
            mapped_exception = _exception_map.get(java.net.NoRouteToHostException)
        else:
            mapped_exception = _exception_map.get(java_exception.__class__)
        if mapped_exception:
            py_exception = mapped_exception(java_exception)
        else:
            py_exception = error(-1, 'Unmapped exception: %s' % java_exception)
    py_exception.java_exception = java_exception
    return _add_exception_attrs(py_exception)


def raises_java_exception(method_or_function):
    """Maps java socket exceptions to the equivalent python exception.
    Also sets _last_error on socket objects so as to support SO_ERROR.
    """

    @wraps(method_or_function)
    def handle_exception(*args, **kwargs):
        is_socket = len(args) > 0 and isinstance(args[0], _realsocket)
        try:
            try:
                return method_or_function(*args, **kwargs)
            except java.lang.Exception, jlx:
                raise _map_exception(jlx)
        except error, e:
            if is_socket:
                args[0]._last_error = e[0]
            raise
        else:
            if is_socket:
                args[0]._last_error = 0
    return handle_exception


# select support
################

class _Select(object):

    def __init__(self, rlist, wlist, xlist):
        self.cv = Condition()
        self.rlist = frozenset(rlist)
        self.wlist = frozenset(wlist)
        self.xlist = frozenset(xlist)

    def _normalize_sockets(self, socks):
        # Get underlying socket, via fileno lookup
        _socks = []
        for sock in socks:
            try:
                _sock = sock.fileno()
                _sock._register_selector  # double check our API requirements
                _socks.append(_sock)
            except AttributeError:
                raise error(errno.EBADF, "Bad file descriptor: %s" % (sock,))
        return _socks

    def notify(self, sock, **_):
        with self.cv:
            self.cv.notify()

    def __str__(self):
        return "_Select(r={},w={},x={})".format(list(self.rlist), list(self.wlist), list(self.xlist))

    @contextmanager
    def _register_sockets(self, socks):
        socks = self._normalize_sockets(socks)
        for sock in socks:
            sock._register_selector(self)
        yield self
        for sock in socks:
            sock._unregister_selector(self)

    def __call__(self, timeout):
        started = time.time()
        with self.cv, self._register_sockets(chain(self.rlist, self.wlist, self.xlist)):
            while True:
                # Checking if sockets are ready (readable OR writable)
                # converts selection from detecting edges to detecting levels
                selected_rlist = set(sock for sock in self.rlist if sock.fileno()._readable())
                selected_wlist = set(sock for sock in self.wlist if sock.fileno()._writable())
                # FIXME add support for exceptions
                selected_xlist = []

                # As usual with condition variables, we need to ensure
                # there's not a spurious wakeup; this test also ensures
                # shortcircuiting if the socket was in fact ready for
                # reading/writing/exception before the select call
                if selected_rlist or selected_wlist:
                    completed = sorted(selected_rlist), sorted(selected_wlist), sorted(selected_xlist)
                    log.debug("Completed select %s", completed, extra={"sock": "*"})
                    return completed
                elif timeout is not None and time.time() - started >= timeout:
                    return [], [], []
                self.cv.wait(timeout)


# poll support
##############

_PollNotification = namedtuple(
    "_PollNotification",
    ["sock",  # the real socket
     "fd",    # could be the real socket (as returned by fileno) or a wrapping socket object
     "exception",
     "hangup"])


class poll(object):

    def __init__(self):
        self.queue = LinkedBlockingQueue()
        self.registered = dict()  # fd -> eventmask
        self.socks2fd = dict_builder(MapMaker().weakKeys().makeMap)()  # sock -> fd

    def notify(self, sock, exception=None, hangup=False):
        notification = _PollNotification(
            sock=sock,
            fd=self.socks2fd.get(sock),
            exception=exception,
            hangup=hangup)
        log.debug("Notify %s", notification, extra={"sock": "*"})

        self.queue.put(notification)

    def register(self, fd, eventmask=POLLIN|POLLPRI|POLLOUT):
        if not hasattr(fd, "fileno"):
            raise TypeError("argument must have a fileno() method")
        sock = fd.fileno()
        log.debug("Register fd=%s eventmask=%s", fd, eventmask, extra={"sock": sock})
        self.registered[fd] = eventmask
        self.socks2fd[sock] = fd
        sock._register_selector(self)
        self.notify(sock)  # Ensure we get an initial notification

    def modify(self, fd, eventmask):
        if not hasattr(fd, "fileno"):
            raise TypeError("argument must have a fileno() method")
        if fd not in self.registered:
            raise error(errno.ENOENT, "No such file or directory")
        self.registered[fd] = eventmask

    def unregister(self, fd):
        if not hasattr(fd, "fileno"):
            raise TypeError("argument must have a fileno() method")
        log.debug("Unregister socket fd=%s", fd, extra={"sock": fd.fileno()})
        del self.registered[fd]
        sock = fd.fileno()
        sock._unregister_selector(self)

    def _event_test(self, notification):
        # Performs standard edge vs event polling, except that we get
        # edges around errors and hangup
        if notification is None:
            return None, 0
        mask = self.registered.get(notification.fd, 0)   # handle if concurrently removed, by simply ignoring
        log.debug("Testing notification=%s mask=%s", notification, mask, extra={"sock": "*"})
        event = 0
        if mask & POLLIN and notification.sock._readable():
            event |= POLLIN
        if mask & POLLOUT and notification.sock._writable():
            event |= POLLOUT
        if mask & POLLERR and notification.exception:
            event |= POLLERR
        if mask & POLLHUP and (notification.hangup or not notification.sock.channel):
            event |= POLLHUP
        if mask & POLLNVAL and not notification.sock.peer_closed:
            event |= POLLNVAL
        log.debug("Tested notification=%s event=%s", notification, event, extra={"sock": "*"})
        return notification.fd, event

    def _handle_poll(self, poller):
        notification = poller()
        if notification is None:
            return []

        # Pull as many outstanding notifications as possible out
        # of the queue
        notifications = [notification]
        self.queue.drainTo(notifications)
        log.debug("Got notification(s) %s", notifications, extra={"sock": "MODULE"})
        result = []
        socks = set()

        # But given how we notify, it's possible to see possible
        # multiple notifications. Just return one (fd, event) for a
        # given socket
        for notification in notifications:
            if notification.sock not in socks:
                fd, event = self._event_test(notification)
                if event:
                    result.append((fd, event))
                    socks.add(notification.sock)

        # Repump sockets to pick up a subsequent level change
        for sock in socks:
            self.notify(sock)

        return result

    def poll(self, timeout=None):
        if not (timeout is None or isinstance(timeout, numbers.Real)):
            raise TypeError("timeout must be a number or None, got %r" % (timeout,))
        if timeout < 0:
            timeout = None
        log.debug("Polling timeout=%s", timeout, extra={"sock": "*"})
        if timeout is None:
            return self._handle_poll(self.queue.take)
        elif timeout == 0:
            return self._handle_poll(self.queue.poll)
        else:
            timeout = float(timeout) / 1000.  # convert from milliseconds to seconds
            while timeout > 0:
                started = time.time()
                timeout_in_ns = int(timeout * _TO_NANOSECONDS)
                result = self._handle_poll(partial(self.queue.poll, timeout_in_ns, TimeUnit.NANOSECONDS))
                if result:
                    return result
                timeout -= time.time() - started
                log.debug("Spurious wakeup, retrying with timeout=%s", timeout, extra={"sock": "*"})
            return []


# integration with Netty
########################

class PythonInboundHandler(ChannelInboundHandlerAdapter):

    def __init__(self, sock):
        self.sock = sock
        log.debug("Initializing inbound handler", extra={"sock": self.sock})

    def channelActive(self, ctx):
        log.debug("Channel is active", extra={"sock": self.sock})
        self.sock._notify_selectors()
        ctx.fireChannelActive()

    def channelRead(self, ctx, msg):
        log.debug("Channel read message %s", msg, extra={"sock": self.sock})
        msg.retain()  # bump ref count so it can be used in the blocking queue
        self.sock.incoming.put(msg)
        self.sock._notify_selectors()
        ctx.fireChannelRead(msg)

    def channelWritabilityChanged(self, ctx):
        log.debug("Channel ready for write", extra={"sock": self.sock})
        self.sock._notify_selectors()
        ctx.fireChannelWritabilityChanged()

    def exceptionCaught(self, ctx, cause):
        log.debug("Channel caught exception %s", cause, extra={"sock": self.sock})
        self.sock._notify_selectors(exception=cause)


class ChildSocketHandler(ChannelInitializer):

    def __init__(self, parent_socket):
        self.parent_socket = parent_socket

    def initChannel(self, child_channel):
        child = ChildSocket(self.parent_socket)
        log.debug("Initializing child %s", child, extra={"sock": self.parent_socket})

        child.proto = IPPROTO_TCP
        child._init_client_mode(child_channel)

        # Get most current options from the parent. This enables any
        # subsequent divergence.
        #
        # It's OK that this copy could occur without a mutex, given
        # that such iteration is guaranteed to be weakly consistent
        child.options = dict(((option, value) for option, value in self.parent_socket.options.iteritems()))
        if child.options:
            log.debug("Setting inherited options %s", child.options, extra={"sock": child})
            config = child_channel.config()
            for option, value in child.options.iteritems():
                _set_option(config.setOption, option, value)

        # Ensure that this handler will not block if the channel is
        # closed, otherwise this handler will simply sit idly as a
        # pending task in the Netty thread pool
        child_channel.closeFuture().addListener(child._make_active)

        # Parent socket is wrapping, so we know intent and we can
        # return as soon as handshaking is completed, if any
        if hasattr(self.parent_socket, "ssl_wrap_child_socket"):
            log.debug("Wrapping child socket for a wrapped parent=%s", self.parent_socket, extra={"sock": self})
            child._wrapper_socket = self.parent_socket.ssl_wrap_child_socket(child)
            child._handshake_future.sync()
            child._post_connect()
            self.parent_socket.child_queue.put(child)
            log.debug("Notifing listeners of parent socket %s", self.parent_socket, extra={"sock": child})
            self.parent_socket._notify_selectors()
            log.debug("Notified listeners of parent socket %s with queue %s",
                      self.parent_socket, self.parent_socket.child_queue, extra={"sock": child})
            return

        # Otherwise, must wait on this barrier until the child socket
        # is activated, as demonstrated by use or other setup
        # info. This is because the child may still be OPTIONALLY
        # wrapped with an SSL socket. Not blocking here will cause
        # corruption in send/recv data because it will overlap with
        # the handshaking in that case.
        with child._activation_cv:
            def wait_for_barrier():
                with child._activation_cv:
                    self.parent_socket.child_queue.put(child)
                    log.debug("Notifing listeners of parent socket %s", self.parent_socket, extra={"sock": child})
                    self.parent_socket._notify_selectors()
                    log.debug("Notified listeners of parent socket %s with queue %s",
                          self.parent_socket, self.parent_socket.child_queue, extra={"sock": child})
            self.parent_socket.parent_group.submit(wait_for_barrier)
            while not child._activated:
                log.debug("Waiting for optional wrapping", extra={"sock": child})
                child._activation_cv.wait()

        log.debug("Completed waiting for optional wrapping", extra={"sock": child})
        if hasattr(child, "ssl_wrap_self"):
            log.debug("Wrapping self", extra={"sock": child})
            child.ssl_wrap_self()
        log.debug("Activating child socket by adding inbound handler", extra={"sock": child})
        child._post_connect()
        child._channel_is_initialized = True


# FIXME raise exceptions for ops not permitted on client socket, server socket
UNKNOWN_SOCKET, CLIENT_SOCKET, SERVER_SOCKET, DATAGRAM_SOCKET = range(4)
_socket_types = {
    UNKNOWN_SOCKET:  "unknown",
    CLIENT_SOCKET:   "client",
    SERVER_SOCKET:   "server",
    DATAGRAM_SOCKET: "datagram"
}


def _identity(value):
    return value


def _set_option(setter, option, value):
    if option in (ChannelOption.SO_LINGER, ChannelOption.SO_TIMEOUT):
        # FIXME consider implementing these options. Note these are not settable
        # via config.setOption in any event:
        #
        # * SO_TIMEOUT does not work for NIO sockets, need to use
        #   IdleStateHandler instead
        #
        # * SO_LINGER does not work for nonblocking sockets, so need
        #   to emulate in calling close on the socket by attempting to
        #   send any unsent data (it's not clear this actually is
        #   needed in Netty however...)
        return
    else:
        setter(option, value)


# These are the only socket protocols we currently support, so it's easy to map as follows:

_socket_options = {
    IPPROTO_TCP: {
        (SOL_SOCKET,  SO_KEEPALIVE):   (ChannelOption.SO_KEEPALIVE, bool),
        (SOL_SOCKET,  SO_LINGER):      (ChannelOption.SO_LINGER, _identity),
        (SOL_SOCKET,  SO_RCVBUF):      (ChannelOption.SO_RCVBUF, int),
        (SOL_SOCKET,  SO_REUSEADDR):   (ChannelOption.SO_REUSEADDR, bool),
        (SOL_SOCKET,  SO_SNDBUF):      (ChannelOption.SO_SNDBUF, int),
        (SOL_SOCKET,  SO_TIMEOUT):     (ChannelOption.SO_TIMEOUT, int),
        (IPPROTO_TCP, TCP_NODELAY):    (ChannelOption.TCP_NODELAY, bool),
    },
    IPPROTO_UDP: {
        (SOL_SOCKET,  SO_BROADCAST):   (ChannelOption.SO_BROADCAST, bool),
        (SOL_SOCKET,  SO_RCVBUF):      (ChannelOption.SO_RCVBUF, int),
        (SOL_SOCKET,  SO_REUSEADDR):   (ChannelOption.SO_REUSEADDR, bool),
        (SOL_SOCKET,  SO_SNDBUF):      (ChannelOption.SO_SNDBUF, int),
        (SOL_SOCKET,  SO_TIMEOUT):     (ChannelOption.SO_TIMEOUT, int),
    }
}

def _socktuple(addr):
    port = addr.getPort()
    inet_addr = addr.getAddress()
    if isinstance(inet_addr, java.net.Inet6Address):
        return str(inet_addr.getHostAddress()), port, 0, inet_addr.getScopeId()
    else:
        return str(inet_addr.getHostAddress()), port

# actual socket support
#######################

class _realsocket(object):

    def __init__(self, family=None, type=None, proto=0):
        # FIXME verify args are correct
        self.family = family
        self.type = type
        if not proto:
            if type == SOCK_STREAM:
                proto = IPPROTO_TCP
            elif type == SOCK_DGRAM:
                proto = IPPROTO_UDP
        self.proto = proto

        self._sock = self  # some Python code wants to see a socket
        self._last_error = 0  # supports SO_ERROR
        self.connected = False
        self.timeout = _defaulttimeout
        self.channel = None
        self.bind_addr = _EPHEMERAL_ADDRESS
        self.selectors = CopyOnWriteArrayList()
        self.options = {}  # deferred options until bootstrap
        self.peer_closed = False
        self.channel_closed = False

        # Reference count this underlying socket
        self.open_lock = Lock()
        self.open_count = 1

        if self.type == SOCK_DGRAM:
            self.socket_type = DATAGRAM_SOCKET
            self.incoming = LinkedBlockingQueue()  # list of read buffers
            self.incoming_head = None  # allows msg buffers to be broken up
            self.python_inbound_handler = None
            self._can_write = True
        else:
            self.socket_type = UNKNOWN_SOCKET

    def __repr__(self):
        return "<_realsocket at {:#x} type={} open_count={} channel={} timeout={}>".format(
            id(self), _socket_types[self.socket_type], self.open_count, self.channel, self.timeout)

    def _make_active(self):
        pass

    def _register_selector(self, selector):
        self._make_active()  # attempting to poll/select on a socket means waiting for wrap intent is done
        self.selectors.addIfAbsent(selector)

    def _unregister_selector(self, selector):
        try:
            return self.selectors.remove(selector)
        except ValueError:
            return None
        except ArrayIndexOutOfBoundsException:
            return None

    def _notify_selectors(self, exception=None, hangup=False):
        for selector in self.selectors:
            selector.notify(self, exception=exception, hangup=hangup)

    @raises_java_exception
    def _handle_channel_future(self, future, reason):
        # All differences between nonblocking vs blocking with optional timeouts
        # is managed by this method.
        #
        # All sockets can be selected on, regardless of blocking/nonblocking state.
        future.addListener(self._notify_selectors)
        if self.timeout is None:
            log.debug("Syncing on future %s for %s", future, reason, extra={"sock": self})
            return future.sync()
        elif self.timeout:
            self._handle_timeout(future.await, reason)
            if not future.isSuccess():
                log.debug("Got this failure %s during %s", future.cause(), reason, extra={"sock": self})
                raise future.cause()
            return future
        else:
            return future

    def setblocking(self, flag):
        if flag:
            self.settimeout(None)
        else:
            self.settimeout(0.0)

    def settimeout(self, timeout):
        self.timeout = _calctimeoutvalue(timeout)

    def gettimeout(self):
        return self.timeout

    def _handle_timeout(self, waiter, reason):
        timeout_in_ns = int(self.timeout * _TO_NANOSECONDS)
        log.debug("Waiting for up to %.2fs for %s", self.timeout, reason, extra={"sock": self})
        started = time.time()
        result = waiter(timeout_in_ns, TimeUnit.NANOSECONDS)
        log.debug("Completed in %.2fs",  time.time() - started, extra={"sock": self})
        if not result:
            # above predicate handles either the case the waiter is
            # returning a value or in the case ChannelFuture#await,
            # that the timeout expired, in which case False is
            # returned
            if self.timeout == 0:
                raise error(errno.ETIMEDOUT, "Connection timed out")
            else:
                raise timeout(errno.ETIMEDOUT, "timed out")
        return result

    def bind(self, address):
        # Netty 4 supports binding a socket to multiple addresses;
        # apparently this is the not the case for C API sockets
        self.bind_addr = _get_jsockaddr(address, self.family, self.type, self.proto, AI_PASSIVE)
        self._datagram_connect()  # as necessary

    # CLIENT METHODS
    # Calling connect/connect_ex means this is a client socket; these
    # in turn use _connect, which uses Bootstrap, not ServerBootstrap

    def _init_client_mode(self, channel=None):
        # this is client socket specific
        self.socket_type = CLIENT_SOCKET
        self.incoming = LinkedBlockingQueue()  # list of read buffers
        self.incoming_head = None  # allows msg buffers to be broken up
        self.python_inbound_handler = None
        self._can_write = True
        self.connect_handlers = []
        self.connected = False
        if channel:
            log.debug("Setting up channel %s", channel, extra={"sock": self})
            self.channel = channel
            self.python_inbound_handler = PythonInboundHandler(self)
            self.connect_handlers = [self.python_inbound_handler]
            self.connected = True

    def _connect(self, addr):
        log.debug("Begin connection to %s", addr, extra={"sock": self})
        addr = _get_jsockaddr(addr, self.family, self.type, self.proto, 0)
        self._init_client_mode()
        self.connected = True
        self.python_inbound_handler = PythonInboundHandler(self)
        bootstrap = Bootstrap().group(NIO_GROUP).channel(NioSocketChannel)
        for option, value in self.options.iteritems():
            _set_option(bootstrap.option, option, value)

        # FIXME really this is just for SSL handling, so make more
        # specific than a list of connect_handlers
        if self.connect_handlers:
            for handler in self.connect_handlers:
                bootstrap.handler(handler)
        else:
            bootstrap.handler(self.python_inbound_handler)

        if self.bind_addr:
            log.debug("Connect %s to %s", self.bind_addr, addr, extra={"sock": self})
            bind_future = bootstrap.bind(self.bind_addr).sync()
            self._handle_channel_future(bind_future, "local bind")
            self.channel = bind_future.channel()
        else:
            log.debug("Connect to %s", addr, extra={"sock": self})
            self.channel = bootstrap.channel()

        self.connect_future = self.channel.connect(addr)
        self._handle_channel_future(self.connect_future, "connect")

    def _post_connect(self):
        # Post-connect step is necessary to handle SSL setup,
        # otherwise the read adapter can race in seeing encrypted
        # messages from the peer
        if self.connect_handlers:
            self.channel.pipeline().addLast(self.python_inbound_handler)

        def _peer_closed(x):
            log.debug("Peer closed channel %s", x, extra={"sock": self})
            self.channel_closed = True
            self.incoming.put(_PEER_CLOSED)
            self._notify_selectors(hangup=True)

        log.debug("Add _peer_closed to channel close", extra={"sock": self}) 
        self.channel.closeFuture().addListener(_peer_closed)

    def connect(self, addr):
        # Unwrapped sockets can immediately perform the post-connect step
        if self.socket_type == DATAGRAM_SOCKET:
            self._datagram_connect(addr)
            log.debug("Completed datagram connection to %s", addr, extra={"sock": self})
        else:
            self._connect(addr)
            self._post_connect()
            log.debug("Completed connection to %s", addr, extra={"sock": self})

    def connect_ex(self, addr):
        was_connecting = self.connected  # actually means self.connecting if
                                         # not blocking
        if not self.connected:
            try:
                self.connect(addr)
            except error as e:
                return e.errno
        if not self.connect_future.isDone():
            if was_connecting:
                try:
                    # Timing is based on CPython and was empirically
                    # guesstimated. Of course this means user code is
                    # polling, so the the best we can do is wait like
                    # this in supposedly nonblocking mode without
                    # completely busy waiting!
                    self.connect_future.get(1500, TimeUnit.MICROSECONDS)
                except ExecutionException:
                    # generally raised if closed; pick up the state
                    # when testing for success
                    pass
                except TimeoutException:
                    # more than 1.5ms, will report EALREADY below
                    pass

        if not self.connect_future.isDone():
            if was_connecting:
                return errno.EALREADY
            else:
                return errno.EINPROGRESS
        elif self.connect_future.isSuccess():
            # from socketmodule.c
            # if (res == EISCONN)
            #   res = 0;
            # but http://bugs.jython.org/issue2428
            return errno.EISCONN
        else:
            return errno.ENOTCONN

    # SERVER METHODS
    # Calling listen means this is a server socket
    @raises_java_exception
    def listen(self, backlog):
        self.socket_type = SERVER_SOCKET
        self.child_queue = ArrayBlockingQueue(backlog)
        self.accepted_children = 1  # include the parent as well to simplify close logic

        b = ServerBootstrap()
        try:
            self.parent_group = NioEventLoopGroup(_NUM_THREADS, DaemonThreadFactory("Jython-Netty-Parent-%s"))
            self.child_group = NioEventLoopGroup(_NUM_THREADS, DaemonThreadFactory("Jython-Netty-Child-%s"))
        except IllegalStateException:
            raise error(errno.EMFILE, "Cannot allocate thread pool for server socket")
        b.group(self.parent_group, self.child_group)
        b.channel(NioServerSocketChannel)
        b.option(ChannelOption.SO_BACKLOG, backlog)
        for option, value in self.options.iteritems():
            _set_option(b.option, option, value)
            # Note that child options are set in the child handler so
            # that they can take into account any subsequent changes,
            # plus have shadow support

        self.child_handler = ChildSocketHandler(self)
        b.childHandler(self.child_handler)

        self.bind_future = b.bind(self.bind_addr.getAddress(), self.bind_addr.getPort())
        self._handle_channel_future(self.bind_future, "listen")
        self.channel = self.bind_future.channel()
        log.debug("Bound server socket to %s", self.bind_addr, extra={"sock": self})

    def accept(self):
        if self.timeout is None:
            log.debug("Blocking indefinitely for child on queue %s", self.child_queue, extra={"sock": self})
            child = self.child_queue.take()
        elif self.timeout:
            log.debug("Timed wait for child on queue %s", self.child_queue, extra={"sock": self})
            child = self._handle_timeout(self.child_queue.poll, "accept")
        else:
            log.debug("Polling for child on queue %s", self.child_queue, extra={"sock": self})
            child = self.child_queue.poll()
            if child is None:
                raise error(errno.EWOULDBLOCK, "Resource temporarily unavailable")
        peername = child.getpeername() if child else None
        log.debug("Got child %s connected to %s", child, peername, extra={"sock": self})
        child.accepted = True
        with self.open_lock:
            self.accepted_children += 1
        return child, peername

    # DATAGRAM METHODS

    def _datagram_connect(self, addr=None):
        # FIXME raise exception if not of the right family
        if addr is not None:
            addr = _get_jsockaddr(addr, self.family, self.type, self.proto, 0)

        if not self.connected and self.socket_type == DATAGRAM_SOCKET:
            log.debug("Binding datagram socket to %s", self.bind_addr, extra={"sock": self})
            self.connected = True
            self.python_inbound_handler = PythonInboundHandler(self)
            bootstrap = Bootstrap().group(NIO_GROUP).channel(NioDatagramChannel)
            bootstrap.handler(self.python_inbound_handler)
            for option, value in self.options.iteritems():
                _set_option(bootstrap.option, option, value)

            future = bootstrap.register()
            self._handle_channel_future(future, "register")
            self.channel = future.channel()
            self._handle_channel_future(self.channel.bind(self.bind_addr), "bind")

        if addr is not None:
            # Handles the relatively rare case that this is a
            # CONNECTED datagram socket, which Netty does not
            # support in its bootstrap setup.
            log.debug("Connecting datagram socket to %s", addr, extra={"sock": self})
            future = self.channel.connect(addr)
            self._handle_channel_future(future, "connect")

    def sendto(self, string, arg1, arg2=None):
        # Unfortunate arg overloading
        if arg2 is not None:
            flags = arg1
            address = arg2
        else:
            flags = None
            address = arg1

        address =  _get_jsockaddr(address, self.family, self.type, self.proto, 0)

        log.debug("Sending datagram to %s <<<{!r:.20}>>>".format(string), address, extra={"sock": self})
        self._datagram_connect()
        packet = DatagramPacket(Unpooled.wrappedBuffer(string), address)
        future = self.channel.writeAndFlush(packet)
        self._handle_channel_future(future, "sendto")
        return len(string)

    def recvfrom_into(self, buffer, nbytes=0, flags=0):
        if nbytes == 0:
            nbytes = len(buffer)
        data, remote_addr = self.recvfrom(nbytes, flags)
        buffer[0:len(data)] = data
        return len(data), remote_addr

    def recv_into(self, buffer, nbytes=0, flags=0):
        if nbytes == 0:
            nbytes = len(buffer)
        data = self.recv(nbytes, flags)
        buffer[0:len(data)] = data
        return len(data)

    # GENERAL METHODS

    def close(self):
        with self.open_lock:
            self.open_count -= 1
            if self.open_count > 0:
                log.debug("Open count > 0, so not closing underlying socket", extra={"sock": self})
                return

            if self.channel is None:
                return

            close_future = self.channel.close()
            close_future.addListener(self._finish_closing)

    def _finish_closing(self, _):
        if self.socket_type == SERVER_SOCKET:
            log.debug("Shutting down server socket parent group", extra={"sock": self})
            self.parent_group.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS)
            self.accepted_children -= 1
            while True:
                child = self.child_queue.poll()
                if child is None:
                    break
                log.debug("Closed child socket %s not yet accepted", child, extra={"sock": self})
                child.close()
        else:
            msgs = []
            self.incoming.drainTo(msgs)
            for msg in msgs:
                if msg is not _PEER_CLOSED:
                    msg.release()

        log.debug("Closed socket", extra={"sock": self})

    def shutdown(self, how):
        log.debug("Got request to shutdown socket how=%s", how, extra={"sock": self})
        self._verify_channel()
        if how & SHUT_RD or how & SHUT_RDWR:
            try:
                self.channel.pipeline().remove(self.python_inbound_handler)
            except NoSuchElementException:
                pass  # already removed, can safely ignore (presumably)
            except AttributeError:
                pass  # inbound handler never set up, also ignore
        if how & SHUT_WR or how & SHUT_RDWR:
            self._can_write = False

    def _readable(self):
        if self.socket_type == CLIENT_SOCKET or self.socket_type == DATAGRAM_SOCKET:
            log.debug("Incoming head=%s queue=%s", self.incoming_head, self.incoming, extra={"sock": self})
            return bool(
                (self.incoming_head is not None and self.incoming_head.readableBytes()) or
                self.incoming.peek())
        elif self.socket_type == SERVER_SOCKET:
            return bool(self.child_queue.peek())
        else:
            return False

    def _pending(self):
        # Used by ssl.py for an undocumented function used in tests
        # and of course some user code. Note that with Netty,
        # readableBytes() in incoming or incoming_head are guaranteed
        # to be plaintext because of the way pipelines work.  However
        # this is a terrible function to call because it's trying to
        # do something synchronous in the async setting of sockets.
        if self.socket_type == CLIENT_SOCKET or self.socket_type == DATAGRAM_SOCKET:
            if self.incoming_head is not None:
                pending = self.incoming_head.readableBytes()
            else:
                pending = 0
            for msg in self.incoming:
                pending += msg.readableBytes()
            return pending
        return 0

    def _writable(self):
        return self.channel_closed or (self.channel and self.channel.isActive() and self.channel.isWritable())

    can_write = _writable

    def _verify_channel(self):
        if self.channel is None:
            log.debug("Channel is not connected or setup", extra={"sock": self})
            raise error(errno.ENOTCONN, "Socket is not connected")

    @raises_java_exception
    def send(self, data, flags=0):
        # FIXME this almost certainly needs to chunk things
        self._verify_channel()
        if isinstance(data, memoryview):
            data = data.tobytes()
        data = str(data)  # FIXME temporary fix if data is of type buffer
        log.debug("Sending data <<<{!r:.20}>>>".format(data), extra={"sock": self})

        if self.socket_type == DATAGRAM_SOCKET:
            packet = DatagramPacket(Unpooled.wrappedBuffer(data), self.channel.remoteAddress())
            future = self.channel.writeAndFlush(packet)
            self._handle_channel_future(future, "send")
            return len(data)

        if not self._can_write:
            raise error(errno.ENOTCONN, 'Socket not connected')

        bytes_writable = self.channel.bytesBeforeUnwritable()
        if bytes_writable > len(data):
            bytes_writable = len(data)

        sent_data = data[:bytes_writable]

        future = self.channel.writeAndFlush(Unpooled.wrappedBuffer(sent_data))
        self._handle_channel_future(future, "send")
        log.debug("Sent data <<<{!r:.20}>>>".format(sent_data), extra={"sock": self})

        return len(sent_data)

    sendall = send   # FIXME see note above!

    def _get_incoming_msg(self, reason):
        log.debug("head=%s incoming=%s" % (self.incoming_head, self.incoming), extra={"sock": self})
        if self.incoming_head is None:
            if self.timeout is None:
                if self.peer_closed:
                    return None
                self.incoming_head = self.incoming.take()
            elif self.timeout:
                if self.peer_closed:
                    return None
                self.incoming_head = self._handle_timeout(self.incoming.poll, reason)
            else:
                self.incoming_head = self.incoming.poll()  # Could be None
                if self.incoming_head is None:
                    # FIXME FIXME C socket semantics return a '' after the first EAGAIN (not certain if this gets reset or not)
                    log.debug("No data yet for socket", extra={"sock": self})
                    raise error(errno.EAGAIN, "Resource temporarily unavailable")

        msg = self.incoming_head
        if msg is _PEER_CLOSED:
            # Only return _PEER_CLOSED once
            self.incoming_head = None
            self.peer_closed = True
        return msg

    @raises_java_exception
    def _get_message(self, bufsize, reason):
        self._datagram_connect()
        self._verify_channel()
        msg = self._get_incoming_msg(reason)

        if self.socket_type == DATAGRAM_SOCKET:
            if msg is None:
                return None, None
            elif msg is _PEER_CLOSED:
                return "", None
        else:
            if msg is None:
                return None, self.channel.remoteAddress()
            elif msg is _PEER_CLOSED:
                return "", self.channel.remoteAddress()

        if self.socket_type == DATAGRAM_SOCKET:
            content = msg.content()
            sender = msg.sender()
        else:
            content = msg
            sender = self.channel.remoteAddress()
        msg_length = content.readableBytes()
        buf = jarray.zeros(min(msg_length, bufsize), "b")
        content.readBytes(buf)
        if content.readableBytes() == 0:
            msg.release()  # return msg ByteBuf back to Netty's pool
            self.incoming_head = None
        return buf.tostring(), sender

    def recv(self, bufsize, flags=0):
        self._verify_channel()
        log.debug("Waiting on recv", extra={"sock": self})
        # For obvious reasons, concurrent reads on the same socket
        # have to be locked; I don't believe it is the job of recv to
        # do this; in particular this is the policy of SocketChannel,
        # which underlies Netty's support for such channels.
        data, _ = self._get_message(bufsize, "recv")
        log.debug("Received <<<{!r:.20}>>>".format(data), extra={"sock": self})
        return data

    def recvfrom(self, bufsize, flags=0):
        self._verify_channel()
        data, sender = self._get_message(bufsize, "recvfrom")
        remote_addr = sender.getHostString(), sender.getPort()
        log.debug("Received from sender %s <<<{!r:20}>>>".format(data), remote_addr, extra={"sock": self})
        return data, remote_addr

    def fileno(self):
        return self

    @raises_java_exception
    def setsockopt(self, level, optname, value):
        try:
            option, cast = _socket_options[self.proto][(level, optname)]
        except KeyError:
            raise error(errno.ENOPROTOOPT, "Protocol not available")

        cast_value = cast(value)
        self.options[option] = cast_value
        log.debug("Setting option %s to %s", optname, value, extra={"sock": self})
        if self.channel:
            _set_option(self.channel.config().setOption, option, cast_value)

    @raises_java_exception
    def getsockopt(self, level, optname, buflen=None):
        # Pseudo options for interrogating the status of this socket
        if level == SOL_SOCKET:
            if optname == SO_ACCEPTCONN:
                if self.socket_type == SERVER_SOCKET:
                    return 1
                elif self.type == SOCK_STREAM:
                    return 0
                else:
                    raise error(errno.ENOPROTOOPT, "Protocol not available")
            if optname == SO_TYPE:
                return self.type
            if optname == SO_ERROR:
                last_error = self._last_error
                self._last_error = 0
                return last_error

        # Normal options
        try:
            option, _ = _socket_options[self.proto][(level, optname)]
        except KeyError:
            raise error(errno.ENOPROTOOPT, "Protocol not available")
        log.debug("Shadow option settings %s", self.options, extra={"sock": self})
        return self.options.get(option, 0)

    def getsockname(self):
        if self.channel is None:
            if self.bind_addr == _EPHEMERAL_ADDRESS:
                raise error(errno.ENOTCONN, "Socket is not connected")
            else:
                return _socktuple(self.bind_addr)
        if hasattr(self, "bind_future"):
            self.bind_future.sync()
        local_addr = self.channel.localAddress()
        if local_addr.getAddress().isAnyLocalAddress():
            # Netty 4 will default to an IPv6 "any" address from a
            # channel even if it was originally bound to an IPv4 "any"
            # address so, as a workaround, let's construct a new "any"
            # address using the port information gathered above
            if type(self.bind_addr.getAddress()) != type(local_addr.getAddress()):
                return _socktuple(java.net.InetSocketAddress(
                    self.bind_addr.getAddress(), local_addr.getPort()))
        return _socktuple(local_addr)

    def getpeername(self):
        self._verify_channel()
        remote_addr = self.channel.remoteAddress()
        if remote_addr is None:
            raise error(errno.ENOTCONN, "Socket is not connected")
        return _socktuple(remote_addr)


_socketmethods = (
    'bind', 'connect', 'connect_ex', 'fileno', 'listen',
    'getpeername', 'getsockname', 'getsockopt', 'setsockopt',
    'sendall', 'setblocking',
    'settimeout', 'gettimeout', 'shutdown')


# All the method names that must be delegated to either the real socket
# object or the _closedsocket object.
# For socket-reboot, this also means anything used by _Select

_delegate_methods = (
    "recv", "recvfrom", "recv_into", "recvfrom_into",
    "send", "sendto", "fileno")

class _closedsocket(object):

    def close(self):
        pass  # Should be able to close repeatedly

    def _dummy(*args):
        raise error(errno.EBADF, 'Bad file descriptor')

    # All _delegate_methods must also be initialized here.
    fileno = send = recv = recv_into = sendto = recvfrom = recvfrom_into = _dummy

    __getattr__ = _dummy


# Wrapper around platform socket objects. This implements
# a platform-independent dup() functionality. The
# implementation currently relies on reference counting
# to close the underlying socket object.
class _socketobject(object):

    __doc__ = _realsocket.__doc__

    def __init__(self, family=AF_INET, type=SOCK_STREAM, proto=None, _sock=None):
        if _sock is None:
            _sock = _realsocket(family, type, proto)
        self._sock = _sock
        for method in _delegate_methods:
            setattr(self, method, getattr(_sock, method))

    def close(self, _closedsocket=_closedsocket,
              _delegate_methods=_delegate_methods, setattr=setattr):
        # This function should not reference any globals. See issue #808164.
        self._sock.close()
        self._sock = _closedsocket()
        dummy = self._sock._dummy
        for method in _delegate_methods:
            setattr(self, method, dummy)
    close.__doc__ = _realsocket.close.__doc__

    def fileno(self):
        return self._sock

    def accept(self):
        sock, addr = self._sock.accept()
        return _socketobject(_sock=sock), addr
    accept.__doc__ = _realsocket.accept.__doc__

    def dup(self):
        """dup() -> socket object

        Return a new socket object connected to the same system resource."""

        if isinstance(self._sock, _closedsocket):
            return _socketobject(_sock=_closedsocket())
        with self._sock.open_lock:
            self._sock.open_count += 1
            return _socketobject(_sock=self._sock)

    def makefile(self, mode='r', bufsize=-1):
        """makefile([mode[, bufsize]]) -> file object

        Return a regular file object corresponding to the socket.  The mode
        and bufsize arguments are as for the built-in open() function."""

        if isinstance(self._sock, _closedsocket):
            return _fileobject(_closedsocket(), mode, bufsize, close=True)
        with self._sock.open_lock:
            self._sock.open_count += 1
            return _fileobject(self._sock, mode, bufsize, close=True)

    family = property(lambda self: self._sock.family, doc="the socket family")
    type = property(lambda self: self._sock.type, doc="the socket type")
    proto = property(lambda self: self._sock.proto, doc="the socket protocol")


def meth(name,self,*args):
    return getattr(self._sock,name)(*args)


for _m in _socketmethods:
    p = partial(meth,_m)
    p.__name__ = _m
    p.__doc__ = getattr(_realsocket,_m).__doc__
    m = MethodType(p,None,_socketobject)
    setattr(_socketobject,_m,m)


socket = SocketType = _socketobject


# FIXME handshake_future - gates all requests. should be cheap (comparable to the old self.active)

class ChildSocket(_realsocket):

    def __init__(self, parent_socket):
        super(ChildSocket, self).__init__(type=parent_socket.type)
        self.parent_socket = parent_socket
        self._activation_cv = Condition()
        self._activated = False
        self.accepted = False
        self.timeout = parent_socket.timeout

    def _make_active(self, *ignore):  # ignore result arg when used as a listener on a future
        if self._activated:
            return
        with self._activation_cv:
            self._activated = True
            self._activation_cv.notify()
        log.debug("Child socket is now activated", extra={"sock": self})

    # FIXME raise exception for accept, listen, bind, connect, connect_ex

    # All ops that allow us to characterize the mode of operation of
    # this socket as being either Start TLS or SSL when
    # connected. These should be ops that send/receive/change
    # connection, not metadata.

    def send(self, data):
        self._make_active()
        return super(ChildSocket, self).send(data)

    sendall = send

    def recv(self, bufsize, flags=0):
        self._make_active()
        return super(ChildSocket, self).recv(bufsize, flags)

    def recvfrom(self, bufsize, flags=0):
        self._make_active()
        return super(ChildSocket, self).recvfrom(bufsize, flags)

    def setblocking(self, mode):
        self._make_active()
        return super(ChildSocket, self).setblocking(mode)

    def close(self):
        self._make_active()
        super(ChildSocket, self).close()
        if self.open_count > 0:
            return
        if self.accepted:
            with self.parent_socket.open_lock:
                self.parent_socket.accepted_children -= 1
                if self.parent_socket.open_count == 0 and self.parent_socket.accepted_children == 0:
                    log.debug("Shutting down child group for parent socket=%s accepted_children=%s",
                              self.parent_socket, self.parent_socket.accepted_children, extra={"sock": self})
                    self.parent_socket.child_group.shutdownGracefully(0, 100, TimeUnit.MILLISECONDS)

    def shutdown(self, how):
        self._make_active()
        super(ChildSocket, self).shutdown(how)

    def __del__(self):
        # Required in the case this child socket never becomes active.
        # This cleanup will ensure that the pending thread for the
        # handler is released when a GC happens, not necessarily
        # before shutdown of course.  Naturally no extra work will be
        # done in setting up the channel.
        self._make_active()
        self.close()


# EXPORTED constructors

def select(rlist, wlist, xlist, timeout=None):
    for lst in (rlist, wlist, xlist):
        if not isinstance(lst, Iterable):
            raise TypeError("arguments 1-3 must be sequences")
    if not(timeout is None or isinstance(timeout, Number)):
        raise TypeError("timeout must be a float or None")
    if timeout is not None and timeout < 0:
        raise error(errno.EINVAL, "Invalid argument")
    return _Select(rlist, wlist, xlist)(timeout)


def create_connection(address, timeout=_GLOBAL_DEFAULT_TIMEOUT,
                      source_address=None):
    """Connect to *address* and return the socket object.

    Convenience function.  Connect to *address* (a 2-tuple ``(host,
    port)``) and return the socket object.  Passing the optional
    *timeout* parameter will set the timeout on the socket instance
    before attempting to connect.  If no *timeout* is supplied, the
    global default timeout setting returned by :func:`getdefaulttimeout`
    is used.  If *source_address* is set it must be a tuple of (host, port)
    for the socket to bind as a source address before making the connection.
    An host of '' or port 0 tells the OS to use the default.
    """

    host, port = address
    err = None
    for res in getaddrinfo(host, port, 0, SOCK_STREAM):
        af, socktype, proto, canonname, sa = res
        sock = None
        try:
            sock = socket(af, socktype, proto)
            if timeout is not _GLOBAL_DEFAULT_TIMEOUT:
                sock.settimeout(timeout)
            if source_address:
                sock.bind(source_address)
            sock.connect(sa)
            return sock

        except error as _:
            err = _
            if sock is not None:
                sock.close()

    if err is not None:
        raise err
    else:
        raise error("getaddrinfo returns an empty list")


# MISCELLANEOUS module level functions

_defaulttimeout = None

def _calctimeoutvalue(value):
    if value is None:
        return None
    try:
        floatvalue = float(value)
    except:
        raise TypeError('Socket timeout value must be a number or None')
    if floatvalue < 0.0:
        raise ValueError("Socket timeout value cannot be negative")
    return floatvalue

def getdefaulttimeout():
    return _defaulttimeout

def setdefaulttimeout(timeout):
    global _defaulttimeout
    _defaulttimeout = _calctimeoutvalue(timeout)


# Define data structures to support IPV4 and IPV6.

class _ip_address_t(tuple):
    pass


class _ipv4_address_t(_ip_address_t):

    jaddress = None

    def __new__(cls, sockaddr, port, jaddress):
        ntup = tuple.__new__(cls, (sockaddr, port))
        ntup.jaddress = jaddress
        return ntup

class _ipv6_address_t(_ip_address_t):

    jaddress = None

    def __new__(cls, sockaddr, port, jaddress):
        ntup = tuple.__new__(cls, (sockaddr, port, 0, jaddress.scopeId))
        ntup.jaddress = jaddress
        return ntup


def _get_jsockaddr(address_object, family, sock_type, proto, flags):
    if family is None:
        family = AF_UNSPEC
    if sock_type is None:
        sock_type = 0
    if proto is None:
        proto = 0
    addr = _get_jsockaddr2(address_object, family, sock_type, proto, flags)
    log.debug("Address %s for %s", addr, address_object, extra={"sock": "*"})
    return addr

def _get_jsockaddr2(address_object, family, sock_type, proto, flags):
    # Is this an object that was returned from getaddrinfo? If so, it already contains an InetAddress
    if isinstance(address_object, _ip_address_t):
        return java.net.InetSocketAddress(address_object.jaddress, address_object[1])
    # The user passed an address tuple, not an object returned from getaddrinfo
    # So we must call getaddrinfo, after some translations and checking
    if address_object is None:
        address_object = ("", 0)
    error_message = "Address must be a 2-tuple (ipv4: (host, port)) or a 4-tuple (ipv6: (host, port, flow, scope))"
    if not isinstance(address_object, tuple) or \
       ((family == AF_INET and len(address_object) != 2) or \
        (family == AF_INET6 and len(address_object) not in [2,4] )) or \
       not isinstance(address_object[0], (basestring, NoneType)) or \
       not isinstance(address_object[1], (int, long)):
        raise TypeError(error_message)
    if len(address_object) == 4 and not isinstance(address_object[3], (int, long)):
        raise TypeError(error_message)
    hostname = address_object[0]
    if hostname is not None:
        hostname = hostname.strip()
    port = address_object[1]
    if family == AF_INET and sock_type == SOCK_DGRAM and hostname == "<broadcast>":
        hostname = INADDR_BROADCAST
    if hostname in ["", None]:
        if flags & AI_PASSIVE:
            hostname = {AF_INET: INADDR_ANY, AF_INET6: IN6ADDR_ANY_INIT}[family]
        else:
            hostname = "localhost"
    if isinstance(hostname, unicode):
        hostname = encodings.idna.ToASCII(hostname)
    addresses = getaddrinfo(hostname, port, family, sock_type, proto, flags)
    if len(addresses) == 0:
        raise gaierror(errno.EGETADDRINFOFAILED, 'getaddrinfo failed')
    return java.net.InetSocketAddress(addresses[0][4].jaddress, port)


def _is_ip_address(addr, version=None):
    try:
        _google_ipaddr_r234.IPAddress(addr, version)
        return True
    except ValueError:
        return False


def is_ipv4_address(addr):
    return _is_ip_address(addr, 4)


def is_ipv6_address(addr):
    return _is_ip_address(addr, 6)


def is_ip_address(addr):
    return _is_ip_address(addr)


# Workaround for this (predominantly windows) issue
# http://wiki.python.org/jython/NewSocketModule#IPV6_address_support

_ipv4_addresses_only = False

def _use_ipv4_addresses_only(value):
    global _ipv4_addresses_only
    _ipv4_addresses_only = value


def _getaddrinfo_get_host(host, family, flags):
    if not isinstance(host, basestring) and host is not None:
        raise TypeError("getaddrinfo() argument 1 must be string or None")
    if flags & AI_NUMERICHOST:
        if not is_ip_address(host):
            raise gaierror(EAI_NONAME, "Name or service not known")
        if family == AF_INET and not is_ipv4_address(host):
            raise gaierror(EAI_ADDRFAMILY, "Address family for hostname not supported")
        if family == AF_INET6 and not is_ipv6_address(host):
            raise gaierror(EAI_ADDRFAMILY, "Address family for hostname not supported")
    if isinstance(host, unicode):
        host = encodings.idna.ToASCII(host)
    return host


def _getaddrinfo_get_port(port, flags):
    if isinstance(port, basestring):
        try:
            int_port = int(port)
        except ValueError:
            if flags & AI_NUMERICSERV:
                raise gaierror(EAI_NONAME, "Name or service not known")
            # Lookup the service by name
            try:
                int_port = getservbyname(port)
            except error:
                raise gaierror(EAI_SERVICE, "Servname not supported for ai_socktype")
    elif port is None:
        int_port = 0
    elif not isinstance(port, (int, long)):
        raise error("Int or String expected")
    else:
        int_port = int(port)
    return int_port % 65536


@raises_java_exception
def getaddrinfo(host, port, family=AF_UNSPEC, socktype=0, proto=0, flags=0):
    if family is None:
        family = AF_UNSPEC
    if socktype is None:
        socktype = 0
    if not family in [AF_INET, AF_INET6, AF_UNSPEC]:
        raise gaierror(errno.EIO, 'ai_family not supported')
    host = _getaddrinfo_get_host(host, family, flags)
    port = _getaddrinfo_get_port(port, flags)
    if socktype not in [0, SOCK_DGRAM, SOCK_STREAM]:
        raise error(errno.ESOCKTNOSUPPORT, "Socket type is not supported")
    filter_fns = []
    filter_fns.append({
        AF_INET:   lambda x: isinstance(x, java.net.Inet4Address),
        AF_INET6:  lambda x: isinstance(x, java.net.Inet6Address),
        AF_UNSPEC: lambda x: isinstance(x, java.net.InetAddress),
    }[family])
    if host in [None, ""]:
        if flags & AI_PASSIVE:
             hosts = {AF_INET: [INADDR_ANY], AF_INET6: [IN6ADDR_ANY_INIT], AF_UNSPEC: [INADDR_ANY, IN6ADDR_ANY_INIT]}[family]
        else:
             hosts = ["localhost"]
    else:
        hosts = [host]
    results = []
    for h in hosts:
        try:
            all_by_name = java.net.InetAddress.getAllByName(h)
        except java.net.UnknownHostException:
            raise gaierror(errno.ENOEXEC, 'nodename nor servname provided, or not known')

        for a in all_by_name:
            if len([f for f in filter_fns if f(a)]):
                family = {java.net.Inet4Address: AF_INET, java.net.Inet6Address: AF_INET6}[a.getClass()]
                if flags & AI_CANONNAME:
                    canonname = str(a.getCanonicalHostName())
                else:
                    canonname = ""
                sockaddr = str(a.getHostAddress())
                # TODO: Include flowinfo and scopeid in a 4-tuple for IPv6 addresses
                sock_tuple = {AF_INET : _ipv4_address_t, AF_INET6 : _ipv6_address_t}[family](sockaddr, port, a)
                if socktype == 0:
                    socktypes = [SOCK_DGRAM, SOCK_STREAM]
                else:
                    socktypes = [socktype]
                for result_socktype in socktypes:
                    result_proto = {SOCK_DGRAM: IPPROTO_UDP, SOCK_STREAM: IPPROTO_TCP}[result_socktype]
                    if proto in [0, result_proto]:
                        # The returned socket will only support the result_proto
                        # If this does not match the requested proto, don't return it
                        results.append((family, result_socktype, result_proto, canonname, sock_tuple))
    return results



def htons(x): return x
def htonl(x): return x
def ntohs(x): return x
def ntohl(x): return x

@raises_java_exception
def inet_pton(family, ip_string):
    if family == AF_INET:
        if not is_ipv4_address(ip_string):
            raise error("illegal IP address string passed to inet_pton")
    elif family == AF_INET6:
        if not is_ipv6_address(ip_string):
            raise error("illegal IP address string passed to inet_pton")
    else:
        raise error(errno.EAFNOSUPPORT, "Address family not supported by protocol")
    ia = java.net.InetAddress.getByName(ip_string)
    bytes = []
    for byte in ia.getAddress():
        if byte < 0:
            bytes.append(byte+256)
        else:
            bytes.append(byte)
    return "".join([chr(byte) for byte in bytes])

@raises_java_exception
def inet_ntop(family, packed_ip):
    jByteArray = array.array("b", packed_ip)
    if family == AF_INET:
        if len(jByteArray) != 4:
            raise ValueError("invalid length of packed IP address string")
    elif family == AF_INET6:
        if len(jByteArray) != 16:
            raise ValueError("invalid length of packed IP address string")
    else:
        raise ValueError("unknown address family %s" % family)
    ia = java.net.InetAddress.getByAddress(jByteArray)
    return ia.getHostAddress()

def inet_aton(ip_string):
    return inet_pton(AF_INET, ip_string)

def inet_ntoa(packed_ip):
    return inet_ntop(AF_INET, packed_ip)



# Various toplevel functions for the socket module
##################################################

def _gethostbyaddr(name):
    # This is as close as I can get; at least the types are correct...
    addresses = InetAddress.getAllByName(gethostbyname(name))
    names = []
    addrs = []
    for addr in addresses:
        names.append(str(addr.getHostName()))
        addrs.append(str(addr.getHostAddress()))
    return names, addrs

@raises_java_exception
def getfqdn(name=None):
    """
    Return a fully qualified domain name for name. If name is omitted or empty
    it is interpreted as the local host.  To find the fully qualified name,
    the hostname returned by gethostbyaddr() is checked, then aliases for the
    host, if available. The first name which includes a period is selected.
    In case no fully qualified domain name is available, the hostname is retur
    New in version 2.0.
    """
    if not name:
        name = gethostname()
    names, addrs = _gethostbyaddr(name)
    for a in names:
        if a.find(".") >= 0:
            return a
    return name

@raises_java_exception
def gethostname():
    return str(InetAddress.getLocalHost().getHostName())

@raises_java_exception
def gethostbyname(name):
    return str(InetAddress.getByName(name).getHostAddress())

#
# Skeleton implementation of gethostbyname_ex
# Needed because urllib2 refers to it
#

@raises_java_exception
def gethostbyname_ex(name):
    return name, [], gethostbyname(name)

@raises_java_exception
def gethostbyaddr(name):
    names, addrs = _gethostbyaddr(name)
    return names[0], names, addrs


try:
    from jnr.netdb import Service, Protocol

    def getservbyname(service_name, protocol_name=None):
        service = Service.getServiceByName(service_name, protocol_name)
        if service is None:
            raise error('service/proto not found')
        return service.getPort()

    def getservbyport(port, protocol_name=None):
        service = Service.getServiceByPort(port, protocol_name)
        if service is None:
            raise error('port/proto not found')
        return service.getName()

    def getprotobyname(protocol_name=None):
        proto = Protocol.getProtocolByName(protocol_name)
        if proto is None:
            raise error('protocol not found')
        return proto.getProto()

except ImportError:
    def getservbyname(service_name, protocol_name=None):
        return None

    def getservbyport(port, protocol_name=None):
        return None

    def getprotobyname(protocol_name=None):
        return None


def _getnameinfo_get_host(address, flags):
    if not isinstance(address, basestring):
        raise TypeError("getnameinfo() address 1 must be string, not None")
    if isinstance(address, unicode):
        address = encodings.idna.ToASCII(address)
    jia = InetAddress.getByName(address)
    result = jia.getCanonicalHostName()
    if flags & NI_NAMEREQD:
        if is_ip_address(result):
            raise gaierror(EAI_NONAME, "Name or service not known")
    elif flags & NI_NUMERICHOST:
        result = jia.getHostAddress()
    # Ignoring NI_NOFQDN for now
    if flags & NI_IDN:
        result = encodings.idna.ToASCII(result)
    return result

def _getnameinfo_get_port(port, flags):
    if not isinstance(port, (int, long)):
        raise TypeError("getnameinfo() port number must be an integer")
    if flags & NI_NUMERICSERV:
        return port
    proto = None
    if flags & NI_DGRAM:
        proto = "udp"
    return getservbyport(port, proto)

@raises_java_exception
def getnameinfo(sock_addr, flags):
    if not isinstance(sock_addr, tuple) or len(sock_addr) < 2:
        raise TypeError("getnameinfo() argument 1 must be a tuple")
    host = _getnameinfo_get_host(sock_addr[0], flags)
    port = _getnameinfo_get_port(sock_addr[1], flags)
    return (host, port)



class _fileobject(object):
    """Faux file object attached to a socket object."""

    default_bufsize = 8192
    name = "<socket>"

    __slots__ = ["mode", "bufsize", "softspace",
                 # "closed" is a property, see below
                 "_sock", "_rbufsize", "_wbufsize", "_rbuf", "_wbuf", "_wbuf_len",
                 "_close"]

    def __init__(self, sock, mode='rb', bufsize=-1, close=False):
        self._sock = sock
        self.mode = mode # Not actually used in this version
        if bufsize < 0:
            bufsize = self.default_bufsize
        self.bufsize = bufsize
        self.softspace = False
        # _rbufsize is the suggested recv buffer size.  It is *strictly*
        # obeyed within readline() for recv calls.  If it is larger than
        # default_bufsize it will be used for recv calls within read().
        if bufsize == 0:
            self._rbufsize = 1
        elif bufsize == 1:
            self._rbufsize = self.default_bufsize
        else:
            self._rbufsize = bufsize
        self._wbufsize = bufsize
        # We use StringIO for the read buffer to avoid holding a list
        # of variously sized string objects which have been known to
        # fragment the heap due to how they are malloc()ed and often
        # realloc()ed down much smaller than their original allocation.
        self._rbuf = StringIO()
        self._wbuf = [] # A list of strings
        self._wbuf_len = 0
        self._close = close

    def _getclosed(self):
        return self._sock is None
    closed = property(_getclosed, doc="True if the file is closed")

    def close(self):
        try:
            if self._sock:
                self.flush()
        finally:
            if self._close:
                self._sock.close()
            self._sock = None

    def __del__(self):
        try:
            self.close()
        except:
            # close() may fail if __init__ didn't complete
            pass

    def flush(self):
        if self._wbuf:
            data = "".join(self._wbuf)
            self._wbuf = []
            self._wbuf_len = 0
            buffer_size = max(self._rbufsize, self.default_bufsize)
            data_size = len(data)
            write_offset = 0
            # FIXME apparently this doesn't yet work on jython,
            # despite our work on memoryview/buffer support
            view = data # memoryview(data)
            try:
                while write_offset < data_size:
                    chunk = view[write_offset:write_offset+buffer_size]
                    self._sock.sendall(chunk)
                    write_offset += buffer_size
            finally:
                if write_offset < data_size:
                    remainder = data[write_offset:]
                    del view, data  # explicit free
                    self._wbuf.append(remainder)
                    self._wbuf_len = len(remainder)

    def fileno(self):
        return self._sock.fileno()

    def write(self, data):
        data = str(data) # XXX Should really reject non-string non-buffers
        if not data:
            return
        self._wbuf.append(data)
        self._wbuf_len += len(data)
        if (self._wbufsize == 0 or
            (self._wbufsize == 1 and '\n' in data) or
            (self._wbufsize > 1 and self._wbuf_len >= self._wbufsize)):
            self.flush()

    def writelines(self, list):
        # XXX We could do better here for very long lists
        # XXX Should really reject non-string non-buffers
        lines = filter(None, map(str, list))
        self._wbuf_len += sum(map(len, lines))
        self._wbuf.extend(lines)
        if (self._wbufsize <= 1 or
            self._wbuf_len >= self._wbufsize):
            self.flush()

    def read(self, size=-1):
        # Use max, disallow tiny reads in a loop as they are very inefficient.
        # We never leave read() with any leftover data from a new recv() call
        # in our internal buffer.
        rbufsize = max(self._rbufsize, self.default_bufsize)
        # Our use of StringIO rather than lists of string objects returned by
        # recv() minimizes memory usage and fragmentation that occurs when
        # rbufsize is large compared to the typical return value of recv().
        buf = self._rbuf
        buf.seek(0, 2)  # seek end
        if size < 0:
            # Read until EOF
            self._rbuf = StringIO()  # reset _rbuf.  we consume it via buf.
            while True:
                try:
                    data = self._sock.recv(rbufsize)
                except error, e:
                    if e.args[0] == errno.EINTR:
                        continue
                    raise
                if not data:
                    break
                buf.write(data)
            return buf.getvalue()
        else:
            # Read until size bytes or EOF seen, whichever comes first
            buf_len = buf.tell()
            if buf_len >= size:
                # Already have size bytes in our buffer?  Extract and return.
                buf.seek(0)
                rv = buf.read(size)
                self._rbuf = StringIO()
                self._rbuf.write(buf.read())
                return rv

            self._rbuf = StringIO()  # reset _rbuf.  we consume it via buf.
            while True:
                left = size - buf_len
                # recv() will malloc the amount of memory given as its
                # parameter even though it often returns much less data
                # than that.  The returned data string is short lived
                # as we copy it into a StringIO and free it.  This avoids
                # fragmentation issues on many platforms.
                try:
                    data = self._sock.recv(left)
                except error, e:
                    if e.args[0] == errno.EINTR:
                        continue
                    raise
                if not data:
                    break
                n = len(data)
                if n == size and not buf_len:
                    # Shortcut.  Avoid buffer data copies when:
                    # - We have no data in our buffer.
                    # AND
                    # - Our call to recv returned exactly the
                    #   number of bytes we were asked to read.
                    return data
                if n == left:
                    buf.write(data)
                    del data  # explicit free
                    break
                assert n <= left, "recv(%d) returned %d bytes" % (left, n)
                buf.write(data)
                buf_len += n
                del data  # explicit free
                #assert buf_len == buf.tell()
            return buf.getvalue()

    def readline(self, size=-1):
        buf = self._rbuf
        buf.seek(0, 2)  # seek end
        if buf.tell() > 0:
            # check if we already have it in our buffer
            buf.seek(0)
            bline = buf.readline(size)
            if bline.endswith('\n') or len(bline) == size:
                self._rbuf = StringIO()
                self._rbuf.write(buf.read())
                return bline
            del bline
        if size < 0:
            # Read until \n or EOF, whichever comes first
            if self._rbufsize <= 1:
                # Speed up unbuffered case
                buf.seek(0)
                buffers = [buf.read()]
                self._rbuf = StringIO()  # reset _rbuf.  we consume it via buf.
                data = None
                recv = self._sock.recv
                while True:
                    try:
                        while data != "\n":
                            data = recv(1)
                            if not data:
                                break
                            buffers.append(data)
                    except error, e:
                        # The try..except to catch EINTR was moved outside the
                        # recv loop to avoid the per byte overhead.
                        if e.args[0] == errno.EINTR:
                            continue
                        raise
                    break
                return "".join(buffers)

            buf.seek(0, 2)  # seek end
            self._rbuf = StringIO()  # reset _rbuf.  we consume it via buf.
            while True:
                try:
                    data = self._sock.recv(self._rbufsize)
                except error, e:
                    if e.args[0] == errno.EINTR:
                        continue
                    raise
                if not data:
                    break
                nl = data.find('\n')
                if nl >= 0:
                    nl += 1
                    buf.write(data[:nl])
                    self._rbuf.write(data[nl:])
                    del data
                    break
                buf.write(data)
            return buf.getvalue()
        else:
            # Read until size bytes or \n or EOF seen, whichever comes first
            buf.seek(0, 2)  # seek end
            buf_len = buf.tell()
            if buf_len >= size:
                buf.seek(0)
                rv = buf.read(size)
                self._rbuf = StringIO()
                self._rbuf.write(buf.read())
                return rv
            self._rbuf = StringIO()  # reset _rbuf.  we consume it via buf.
            while True:
                try:
                    data = self._sock.recv(self._rbufsize)
                except error, e:
                    if e.args[0] == errno.EINTR:
                        continue
                    raise
                if not data:
                    break
                left = size - buf_len
                # did we just receive a newline?
                nl = data.find('\n', 0, left)
                if nl >= 0:
                    nl += 1
                    # save the excess data to _rbuf
                    self._rbuf.write(data[nl:])
                    if buf_len:
                        buf.write(data[:nl])
                        break
                    else:
                        # Shortcut.  Avoid data copy through buf when returning
                        # a substring of our first recv().
                        return data[:nl]
                n = len(data)
                if n == size and not buf_len:
                    # Shortcut.  Avoid data copy through buf when
                    # returning exactly all of our first recv().
                    return data
                if n >= left:
                    buf.write(data[:left])
                    self._rbuf.write(data[left:])
                    break
                buf.write(data)
                buf_len += n
                #assert buf_len == buf.tell()
            return buf.getvalue()

    def readlines(self, sizehint=0):
        total = 0
        list = []
        while True:
            line = self.readline()
            if not line:
                break
            list.append(line)
            total += len(line)
            if sizehint and total >= sizehint:
                break
        return list

    # Iterator protocols

    def __iter__(self):
        return self

    def next(self):
        line = self.readline()
        if not line:
            raise StopIteration
        return line
