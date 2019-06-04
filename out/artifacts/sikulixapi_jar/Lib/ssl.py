import base64
from collections import namedtuple
import errno
from java.security.cert import CertificateFactory
import uuid
from java.io import BufferedInputStream
from java.security import KeyStore, KeyStoreException
from java.security.cert import CertificateParsingException
from javax.naming.ldap import LdapName
from java.lang import IllegalArgumentException, System
import logging
import os
import textwrap
import time
import re
import threading

try:
    # jarjar-ed version
    from org.python.netty.channel import ChannelInitializer
    from org.python.netty.handler.ssl import SslHandler, SslProvider, SslContextBuilder, ClientAuth
    from org.python.netty.handler.ssl.util import SimpleTrustManagerFactory, InsecureTrustManagerFactory
    from org.python.netty.buffer import ByteBufAllocator

except ImportError:
    # dev version from extlibs
    from io.netty.channel import ChannelInitializer
    from io.netty.handler.ssl import SslHandler, SslProvider, SslContextBuilder, ClientAuth
    from io.netty.handler.ssl.util import SimpleTrustManagerFactory, InsecureTrustManagerFactory
    from io.netty.buffer import ByteBufAllocator

from _socket import (
    SSLError, raises_java_exception,
    SSL_ERROR_SSL,
    SSL_ERROR_WANT_READ,
    SSL_ERROR_WANT_WRITE,
    SSL_ERROR_WANT_X509_LOOKUP,
    SSL_ERROR_SYSCALL,
    SSL_ERROR_ZERO_RETURN,
    SSL_ERROR_WANT_CONNECT,
    SSL_ERROR_EOF,
    SSL_ERROR_INVALID_ERROR_CODE,
    SOL_SOCKET,
    SO_TYPE,
    SOCK_STREAM,
    socket,
    _socketobject,
    ChildSocket,
    error as socket_error)

from _sslcerts import _get_openssl_key_manager, _extract_cert_from_data, _extract_certs_for_paths, \
    _str_hash_key_entry, _get_ecdh_parameter_spec, CompositeX509TrustManagerFactory
from _sslcerts import SSLContext as _JavaSSLContext

from java.text import SimpleDateFormat
from java.util import ArrayList, Locale, TimeZone, NoSuchElementException
from java.util.concurrent import CountDownLatch
from javax.naming.ldap import LdapName
from javax.net.ssl import SSLException, SSLHandshakeException
from javax.security.auth.x500 import X500Principal
from org.ietf.jgss import Oid

try:
    # requires Java 8 or higher for this support
    from javax.net.ssl import SNIHostName, SNIMatcher
    HAS_SNI = True
except ImportError:
    HAS_SNI = False

log = logging.getLogger("_socket")


# Pretend to be OpenSSL
OPENSSL_VERSION = "OpenSSL 1.0.0 (as emulated by Java SSL)"
OPENSSL_VERSION_NUMBER = 0x1000000L
OPENSSL_VERSION_INFO = (1, 0, 0, 0, 0)
_OPENSSL_API_VERSION = OPENSSL_VERSION_INFO

CERT_NONE, CERT_OPTIONAL, CERT_REQUIRED = range(3)

_CERT_TO_CLIENT_AUTH = {CERT_NONE: ClientAuth.NONE,
                        CERT_OPTIONAL: ClientAuth.OPTIONAL,
                        CERT_REQUIRED: ClientAuth.REQUIRE}

# Do not support PROTOCOL_SSLv2, it is highly insecure and it is optional
_, PROTOCOL_SSLv3, PROTOCOL_SSLv23, PROTOCOL_TLSv1, PROTOCOL_TLSv1_1, PROTOCOL_TLSv1_2 = range(6)
_PROTOCOL_NAMES = {
    PROTOCOL_SSLv3: 'SSLv3',
    PROTOCOL_SSLv23: 'SSLv23',
    PROTOCOL_TLSv1: 'TLSv1',
    PROTOCOL_TLSv1_1: 'TLSv1.1',
    PROTOCOL_TLSv1_2: 'TLSv1.2'
}

OP_ALL, OP_NO_SSLv2, OP_NO_SSLv3, OP_NO_TLSv1 = range(4)
OP_SINGLE_DH_USE, OP_NO_COMPRESSION, OP_CIPHER_SERVER_PREFERENCE, OP_SINGLE_ECDH_USE = 1048576, 131072, 4194304, 524288

VERIFY_DEFAULT, VERIFY_CRL_CHECK_LEAF, VERIFY_CRL_CHECK_CHAIN, VERIFY_X509_STRICT = 0, 4, 12, 32

CHANNEL_BINDING_TYPES = []

# https://docs.python.org/2/library/ssl.html#ssl.HAS_ALPN etc...
HAS_ALPN, HAS_NPN, HAS_ECDH = False, False, True


# TODO not supported on jython yet
# Disable weak or insecure ciphers by default
# (OpenSSL's default setting is 'DEFAULT:!aNULL:!eNULL')
# Enable a better set of ciphers by default
# This list has been explicitly chosen to:
#   * Prefer cipher suites that offer perfect forward secrecy (DHE/ECDHE)
#   * Prefer ECDHE over DHE for better performance
#   * Prefer any AES-GCM over any AES-CBC for better performance and security
#   * Then Use HIGH cipher suites as a fallback
#   * Then Use 3DES as fallback which is secure but slow
#   * Disable NULL authentication, NULL encryption, and MD5 MACs for security
#     reasons
_DEFAULT_CIPHERS = (
    'ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:ECDH+HIGH:'
    'DH+HIGH:ECDH+3DES:DH+3DES:RSA+AESGCM:RSA+AES:RSA+HIGH:RSA+3DES:!aNULL:'
    '!eNULL:!MD5'
)

# TODO not supported on jython yet
# Restricted and more secure ciphers for the server side
# This list has been explicitly chosen to:
#   * Prefer cipher suites that offer perfect forward secrecy (DHE/ECDHE)
#   * Prefer ECDHE over DHE for better performance
#   * Prefer any AES-GCM over any AES-CBC for better performance and security
#   * Then Use HIGH cipher suites as a fallback
#   * Then Use 3DES as fallback which is secure but slow
#   * Disable NULL authentication, NULL encryption, MD5 MACs, DSS, and RC4 for
#     security reasons
_RESTRICTED_SERVER_CIPHERS = (
    'ECDH+AESGCM:DH+AESGCM:ECDH+AES256:DH+AES256:ECDH+AES128:DH+AES:ECDH+HIGH:'
    'DH+HIGH:ECDH+3DES:DH+3DES:RSA+AESGCM:RSA+AES:RSA+HIGH:RSA+3DES:!aNULL:'
    '!eNULL:!MD5:!DSS:!RC4'
)

_rfc2822_date_format = SimpleDateFormat("MMM dd HH:mm:ss yyyy z", Locale.US)
_rfc2822_date_format.setTimeZone(TimeZone.getTimeZone("GMT"))

_ldap_rdn_display_names = {
    # list from RFC 2253
    "CN": "commonName",
    "E": "emailAddress",
    "L": "localityName",
    "ST": "stateOrProvinceName",
    "O": "organizationName",
    "OU": "organizationalUnitName",
    "C": "countryName",
    "STREET": "streetAddress",
    "DC": "domainComponent",
    "UID": "userid"
}

_cert_name_types = [
    # Fields documented in
    # http://docs.oracle.com/javase/7/docs/api/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()
    "other",
    "rfc822",
    "DNS",
    "x400Address",
    "directory",
    "ediParty",
    "uniformResourceIdentifier",
    "ipAddress",
    "registeredID"]

def _str_or_unicode(s):
    try:
        return s.encode('ascii')
    except UnicodeEncodeError:
        return s
    except AttributeError:
        return str(s)

class CertificateError(ValueError):
    pass


# TODO for now create these exceptions here to conform with API
class SSLZeroReturnError(SSLError):
    pass

class SSLWantReadError(SSLError):
    pass

class SSLWantWriteError(SSLError):
    pass

class SSLSyscallError(SSLError):
    pass

class SSLEOFError(SSLError):
    pass


def _dnsname_match(dn, hostname, max_wildcards=1):
    """Matching according to RFC 6125, section 6.4.3

    http://tools.ietf.org/html/rfc6125#section-6.4.3
    """
    pats = []
    if not dn:
        return False

    pieces = dn.split(r'.')
    leftmost = pieces[0]
    remainder = pieces[1:]

    wildcards = leftmost.count('*')
    if wildcards > max_wildcards:
        # Issue #17980: avoid denials of service by refusing more
        # than one wildcard per fragment.  A survery of established
        # policy among SSL implementations showed it to be a
        # reasonable choice.
        raise CertificateError(
            "too many wildcards in certificate DNS name: " + repr(dn))

    # speed up common case w/o wildcards
    if not wildcards:
        return dn.lower() == hostname.lower()

    # RFC 6125, section 6.4.3, subitem 1.
    # The client SHOULD NOT attempt to match a presented identifier in which
    # the wildcard character comprises a label other than the left-most label.
    if leftmost == '*':
        # When '*' is a fragment by itself, it matches a non-empty dotless
        # fragment.
        pats.append('[^.]+')
    elif leftmost.startswith('xn--') or hostname.startswith('xn--'):
        # RFC 6125, section 6.4.3, subitem 3.
        # The client SHOULD NOT attempt to match a presented identifier
        # where the wildcard character is embedded within an A-label or
        # U-label of an internationalized domain name.
        pats.append(re.escape(leftmost))
    else:
        # Otherwise, '*' matches any dotless string, e.g. www*
        pats.append(re.escape(leftmost).replace(r'\*', '[^.]*'))

    # add the remaining fragments, ignore any wildcards
    for frag in remainder:
        pats.append(re.escape(frag))

    pat = re.compile(r'\A' + r'\.'.join(pats) + r'\Z', re.IGNORECASE)
    return pat.match(hostname)


def match_hostname(cert, hostname):
    """Verify that *cert* (in decoded format as returned by
    SSLSocket.getpeercert()) matches the *hostname*.  RFC 2818 and RFC 6125
    rules are followed, but IP addresses are not accepted for *hostname*.

    CertificateError is raised on failure. On success, the function
    returns nothing.
    """
    if not cert:
        raise ValueError("empty or no certificate, match_hostname needs a "
                         "SSL socket or SSL context with either "
                         "CERT_OPTIONAL or CERT_REQUIRED")
    dnsnames = []
    san = cert.get('subjectAltName', ())
    for key, value in san:
        if key == 'DNS':
            if _dnsname_match(value, hostname):
                return
            dnsnames.append(value)
    if not dnsnames:
        # The subject is only checked when there is no dNSName entry
        # in subjectAltName
        for sub in cert.get('subject', ()):
            for key, value in sub:
                # XXX according to RFC 2818, the most specific Common Name
                # must be used.
                if key == 'commonName':
                    if _dnsname_match(value, hostname):
                        return
                    dnsnames.append(value)
    if len(dnsnames) > 1:
        raise CertificateError("hostname %r "
                               "doesn't match either of %s"
                               % (hostname, ', '.join(map(repr, dnsnames))))
    elif len(dnsnames) == 1:
        raise CertificateError("hostname %r "
                               "doesn't match %r"
                               % (hostname, dnsnames[0]))
    else:
        raise CertificateError("no appropriate commonName or "
                               "subjectAltName fields were found")


DefaultVerifyPaths = namedtuple("DefaultVerifyPaths",
                                "cafile capath openssl_cafile_env openssl_cafile openssl_capath_env "
                                "openssl_capath")


def get_default_verify_paths():
    """Return paths to default cafile and capath.
    """
    cafile, capath = None, None
    default_cert_dir_env = os.environ.get('SSL_CERT_DIR', None)
    default_cert_file_env = os.environ.get('SSL_CERT_FILE', None)

    java_cert_file = System.getProperty('javax.net.ssl.trustStore')

    if java_cert_file is not None and os.path.isfile(java_cert_file):
        cafile = java_cert_file
    else:
        if default_cert_dir_env is not None:
            capath = default_cert_dir_env if os.path.isdir(default_cert_dir_env) else None
        if default_cert_file_env is not None:
            cafile = default_cert_file_env if os.path.isfile(default_cert_file_env) else None

        if cafile is None:
            # http://docs.oracle.com/javase/6/docs/technotes/guides/security/jsse/JSSERefGuide.html
            java_home = System.getProperty('java.home')
            for _path in ('lib/security/jssecacerts', 'lib/security/cacerts'):
                java_cert_file = os.path.join(java_home, _path)
                if os.path.isfile(java_cert_file):
                    cafile = java_cert_file
                    capath = os.path.dirname(cafile)

    return DefaultVerifyPaths(cafile if os.path.isfile(cafile) else None,
                              capath if capath and os.path.isdir(capath) else None,
                              'SSL_CERT_FILE', default_cert_file_env,
                              'SSL_CERT_DIR', default_cert_dir_env)


class _ASN1Object(namedtuple("_ASN1Object", "nid shortname longname oid")):
    """ASN.1 object identifier lookup
    """
    __slots__ = ()

    def __new__(cls, oid):
        # TODO, just fake it for now
        if oid == '1.3.6.1.5.5.7.3.1':
            return super(_ASN1Object, cls).__new__(cls, 129, 'serverAuth', 'TLS Web Server Authentication', oid)
        elif oid == '1.3.6.1.5.5.7.3.2':
            return super(_ASN1Object, cls).__new__(cls, 130, 'clientAuth', 'clientAuth', oid)
        raise ValueError()


class Purpose(_ASN1Object):
    """SSLContext purpose flags with X509v3 Extended Key Usage objects
    """

Purpose.SERVER_AUTH = Purpose('1.3.6.1.5.5.7.3.1')
Purpose.CLIENT_AUTH = Purpose('1.3.6.1.5.5.7.3.2')


def create_default_context(purpose=Purpose.SERVER_AUTH, cafile=None,
                           capath=None, cadata=None):
    """Create a SSLContext object with default settings.

    NOTE: The protocol and settings may change anytime without prior
          deprecation. The values represent a fair balance between maximum
          compatibility and security.
    """
    if not isinstance(purpose, _ASN1Object):
        raise TypeError(purpose)

    context = SSLContext(PROTOCOL_SSLv23)

    # SSLv2 considered harmful.
    context.options |= OP_NO_SSLv2

    # SSLv3 has problematic security and is only required for really old
    # clients such as IE6 on Windows XP
    context.options |= OP_NO_SSLv3

    # disable compression to prevent CRIME attacks (OpenSSL 1.0+)
    # TODO not supported on Jython
    # context.options |= getattr(_ssl, "OP_NO_COMPRESSION", 0)

    if purpose == Purpose.SERVER_AUTH:
        # verify certs and host name in client mode
        context.verify_mode = CERT_REQUIRED
        context.check_hostname = True
    elif purpose == Purpose.CLIENT_AUTH:
        pass
        # TODO commeted out by darjus, none of the below is supported :(
        # # Prefer the server's ciphers by default so that we get stronger
        # # encryption
        # context.options |= getattr(_ssl, "OP_CIPHER_SERVER_PREFERENCE", 0)
        #
        # # Use single use keys in order to improve forward secrecy
        # context.options |= getattr(_ssl, "OP_SINGLE_DH_USE", 0)
        # context.options |= getattr(_ssl, "OP_SINGLE_ECDH_USE", 0)
        #
        # # disallow ciphers with known vulnerabilities
        # context.set_ciphers(_RESTRICTED_SERVER_CIPHERS)

    if cafile or capath or cadata:
        context.load_verify_locations(cafile, capath, cadata)
    elif context.verify_mode != CERT_NONE:
        # no explicit cafile, capath or cadata but the verify mode is
        # CERT_OPTIONAL or CERT_REQUIRED. Let's try to load default system
        # root CA certificates for the given purpose. This may fail silently.
        context.load_default_certs(purpose)
    return context


def _create_unverified_context(protocol=PROTOCOL_SSLv23, cert_reqs=None,
                               check_hostname=False, purpose=Purpose.SERVER_AUTH,
                               certfile=None, keyfile=None,
                               cafile=None, capath=None, cadata=None):
    """Create a SSLContext object for Python stdlib modules

    All Python stdlib modules shall use this function to create SSLContext
    objects in order to keep common settings in one place. The configuration
    is less restricted than create_default_context()'s to increase backward
    compatibility.
    """
    if not isinstance(purpose, _ASN1Object):
        raise TypeError(purpose)

    context = SSLContext(protocol)
    # SSLv2 considered harmful.
    context.options |= OP_NO_SSLv2
    # SSLv3 has problematic security and is only required for really old
    # clients such as IE6 on Windows XP
    context.options |= OP_NO_SSLv3

    if cert_reqs is not None:
        context.verify_mode = cert_reqs
    context.check_hostname = check_hostname

    if keyfile and not certfile:
        raise ValueError("certfile must be specified")
    if certfile or keyfile:
        context.load_cert_chain(certfile, keyfile)

    # load CA root certs
    if cafile or capath or cadata:
        context.load_verify_locations(cafile, capath, cadata)
    elif context.verify_mode != CERT_NONE:
        # no explicit cafile, capath or cadata but the verify mode is
        # CERT_OPTIONAL or CERT_REQUIRED. Let's try to load default system
        # root CA certificates for the given purpose. This may fail silently.
        context.load_default_certs(purpose)

    return context


# Used by http.client if no context is explicitly passed.
_create_default_https_context = create_default_context


# Backwards compatibility alias, even though it's not a public name.
_create_stdlib_context = _create_unverified_context


class SSLInitializer(ChannelInitializer):
    def __init__(self, ssl_handler):
        self.ssl_handler = ssl_handler

    def initChannel(self, ch):
        pipeline = ch.pipeline()
        pipeline.addFirst("ssl", self.ssl_handler)


class SSLSocket(object):

    def __init__(self, sock, keyfile=None, certfile=None, server_side=False, cert_reqs=CERT_NONE,
                 ssl_version=PROTOCOL_SSLv23, ca_certs=None,
                 do_handshake_on_connect=True, suppress_ragged_eofs=True, npn_protocols=None, ciphers=None,
                 server_hostname=None, _context=None):
        # TODO ^^ handle suppress_ragged_eofs
        self.sock = sock
        self.do_handshake_on_connect = do_handshake_on_connect
        self._sock = sock._sock  # the real underlying socket

        # FIXME in CPython, a check like so is performed - but this is
        # not quite correct, based on tests. We should revisit to see
        # if we can make this work as desired.

        # if do_handshake_on_connect and self._sock.timeout == 0:
        #     raise ValueError("do_handshake_on_connect should not be specified for non-blocking sockets")

        self._connected = False
        if _context:
            self._context = _context
        else:
            if server_side and not certfile:
                raise ValueError("certfile must be specified for server-side "
                                 "operations")
            if keyfile and not certfile:
                raise ValueError("certfile must be specified")
            if certfile and not keyfile:
                keyfile = certfile
            self._context = SSLContext(ssl_version)
            self._context.verify_mode = cert_reqs
            if ca_certs:
                self._context.load_verify_locations(ca_certs)
            if certfile:
                self._context.load_cert_chain(certfile, keyfile)
            if npn_protocols:
                self._context.set_npn_protocols(npn_protocols)
            if ciphers:
                self._context.set_ciphers(ciphers)
            self.keyfile = keyfile
            self.certfile = certfile
            self.cert_reqs = cert_reqs
            self.ssl_version = ssl_version
            self.ca_certs = ca_certs
            self.ciphers = ciphers

        if sock.getsockopt(SOL_SOCKET, SO_TYPE) != SOCK_STREAM:
            raise NotImplementedError("only stream sockets are supported")

        if server_side and server_hostname:
            raise ValueError("server_hostname can only be specified "
                             "in client mode")
        if self._context.check_hostname and not server_hostname:
            raise ValueError("check_hostname requires server_hostname")
        self.server_side = server_side
        self.server_hostname = server_hostname
        self.suppress_ragged_eofs = suppress_ragged_eofs

        self.ssl_handler = None
        # We use _sslobj here to support the CPython convention that
        # an object means we have handshaked. It is used by existing code
        # in the wild that looks at this ostensibly internal attribute.
        
        # FIXME CPython uses _sslobj to track the OpenSSL wrapper
        # object that's implemented in C, with the following
        # properties:
        #
        # 'cipher', 'compression', 'context', 'do_handshake',
        # 'peer_certificate', 'pending', 'read', 'shutdown',
        # 'tls_unique_cb', 'version', 'write'
        self._sslobj = self   # setting to self is not quite right

        self.engine = None

        if self.do_handshake_on_connect and self._sock.connected:
            log.debug("Handshaking socket on connect", extra={"sock": self._sock})
            if isinstance(self._sock, ChildSocket):
                # Need to handle child sockets differently depending
                # on whether the parent socket is wrapped or not.
                #
                # In either case, we cannot handshake here in this
                # thread - it must be done in the child pool and
                # before the child is activated.
                #
                # 1. If wrapped, this is going through SSLSocket.accept

                if isinstance(self._sock.parent_socket, SSLSocket):
                    # already wrapped, via `wrap_child` function a few lines below
                    log.debug(
                        "Child socket - will handshake in child loop type=%s parent=%s",
                        type(self._sock), self._sock.parent_socket,
                        extra={"sock": self._sock})
                    self._sock._make_active()

                # 2. If not, using code will be calling SSLContext.wrap_socket
                #    *after* accept from an unwrapped socket

                else:
                    log.debug("Child socket will wrap self with handshake", extra={"sock": self._sock})
                    setup_handshake_latch = CountDownLatch(1)

                    def setup_handshake():
                        handshake_future = self.do_handshake()
                        setup_handshake_latch.countDown()
                        return handshake_future

                    self._sock.ssl_wrap_self = setup_handshake
                    self._sock._make_active()
                    setup_handshake_latch.await()
                    log.debug("Child socket waiting on handshake=%s", self._handshake_future, extra={"sock": self._sock})
                    self._sock._handle_channel_future(self._handshake_future, "SSL handshake")
            else:
                self.do_handshake()

        if hasattr(self._sock, "accepted_children"):
            def wrap_child(child):
                log.debug(
                    "Wrapping child socket - about to handshake! parent=%s",
                    self._sock, extra={"sock": child})
                child._wrapper_socket = self.context.wrap_socket(
                    _socketobject(_sock=child),
                    do_handshake_on_connect=self.do_handshake_on_connect,
                    suppress_ragged_eofs=self.suppress_ragged_eofs,
                    server_side=True)
                if self.do_handshake_on_connect:
                    # this handshake will be done in the child pool - initChannel will block on it
                    child._wrapper_socket.do_handshake()
            self._sock.ssl_wrap_child_socket = wrap_child

    @property
    def context(self):
        return self._context

    @context.setter
    def context(self, context):
        self._context = context

    def setup_engine(self, addr):
        if self.engine is None:
            # http://stackoverflow.com/questions/13390964/java-ssl-fatal-error-80-unwrapping-net-record-after-adding-the-https-en
            self.engine = self._context._createSSLEngine(
                addr, self.server_hostname,
                cert_file=getattr(self, "certfile", None), key_file=getattr(self, "keyfile", None),
                server_side=self.server_side)
            self.engine.setUseClientMode(not self.server_side)

    def connect(self, addr):
        """Connects to remote ADDR, and then wraps the connection in
        an SSL channel."""
        if self.server_side:
            raise ValueError("can't connect in server-side mode")
        if self._connected:
            raise ValueError("attempt to connect already-connected SSLSocket!")

        log.debug("Connect SSL with handshaking %s", self.do_handshake_on_connect, extra={"sock": self._sock})

        self._sock.connect(addr)
        if self.do_handshake_on_connect:
            self.do_handshake()

    def connect_ex(self, addr):
        """Connects to remote ADDR, and then wraps the connection in
        an SSL channel."""
        if self.server_side:
            raise ValueError("can't connect in server-side mode")
        if self._connected:
            raise ValueError("attempt to connect already-connected SSLSocket!")

        log.debug("Connect SSL with handshaking %s", self.do_handshake_on_connect, extra={"sock": self._sock})

        rc = self._sock.connect_ex(addr)
        if rc == errno.EISCONN:
            self._connected = True
            if self.do_handshake_on_connect:
                self.do_handshake()
        return rc

    def accept(self):
        """Accepts a new connection from a remote client, and returns
        a tuple containing that new connection wrapped with a server-side
        SSL channel, and the address of the remote client."""
        child, addr = self._sock.accept()
        if self.do_handshake_on_connect:
            wrapped_child_socket = child._wrapper_socket
            del child._wrapper_socket
            return wrapped_child_socket, addr
        else:
            return self.context.wrap_socket(
                _socketobject(_sock=child),
                do_handshake_on_connect=self.do_handshake_on_connect,
                suppress_ragged_eofs=self.suppress_ragged_eofs,
                server_side=True)

    def unwrap(self):
        try:
            self._sock.channel.pipeline().remove("ssl")
        except NoSuchElementException:
            pass
        self.ssl_handler.close()
        return self._sock

    def do_handshake(self):
        log.debug("SSL handshaking", extra={"sock": self._sock})
        self.setup_engine(self.sock.getpeername())

        def handshake_step(result):
            log.debug("SSL handshaking completed %s", result, extra={"sock": self._sock})
            self._notify_selectors()

        if self.ssl_handler is None:
            self.ssl_handler = SslHandler(self.engine)
            self.ssl_handler.handshakeFuture().addListener(handshake_step)

            if hasattr(self._sock, "connected") and self._sock.connected:
                # The underlying socket is already connected, so some extra work to manage
                log.debug("Adding SSL handler to pipeline after connection", extra={"sock": self._sock})
                self._sock.channel.pipeline().addFirst("ssl", self.ssl_handler)
            else:
                log.debug("Not connected, adding SSL initializer...", extra={"sock": self._sock})
                self._sock.connect_handlers.append(SSLInitializer(self.ssl_handler))

        self._handshake_future = self.ssl_handler.handshakeFuture()
        if isinstance(self._sock, ChildSocket):
            pass
            # see
            # http://stackoverflow.com/questions/24628271/exception-in-netty-io-netty-util-concurrent-blockingoperationexception
            # - handshake in the child thread pool
        else:
            self._sock._handle_channel_future(self._handshake_future, "SSL handshake")

    def dup(self):
        raise NotImplemented("Can't dup() %s instances" %
                             self.__class__.__name__)

    @raises_java_exception
    def _ensure_handshake(self):
        log.debug("Ensure handshake", extra={"sock": self}) 
        self._sock._make_active()
        # nonblocking code should never wait here, but only attempt to
        # come to this point when notified via a selector
        if not hasattr(self, "_handshake_future"):
            self.do_handshake()
        # additional synchronization guard if this is a child socket
        self._handshake_future.sync()
        log.debug("Completed post connect", extra={"sock": self})

    # Various pass through methods to the wrapped socket

    def send(self, data):
        self._ensure_handshake()
        return self.sock.send(data)

    write = send

    def sendall(self, data):
        self._ensure_handshake()
        return self.sock.sendall(data)

    def recv(self, bufsize, flags=0):
        self._ensure_handshake()
        return self.sock.recv(bufsize, flags)

    def read(self, len=0, buffer=None):
        """Read up to LEN bytes and return them.
        Return zero-length string on EOF."""
        self._checkClosed()
        self._ensure_handshake()
        # FIXME? breaks test_smtpnet.py
        # if not self._sslobj:
        #     raise ValueError("Read on closed or unwrapped SSL socket.")
        try:
            if buffer is not None:
                v = self.recvfrom_into(buffer, len or 1024)
            else:
                v = self.recv(len or 1024)
            return v
        except SSLError as x:
            if x.args[0] == SSL_ERROR_EOF and self.suppress_ragged_eofs:
                if buffer is not None:
                    return 0
                else:
                    return b''
            else:
                raise

    def recvfrom(self, bufsize, flags=0):
        self._ensure_handshake()
        return self.sock.recvfrom(bufsize, flags)

    def recvfrom_into(self, buffer, nbytes=0, flags=0):
        self._ensure_handshake()
        return self.sock.recvfrom_into(buffer, nbytes, flags)

    def recv_into(self, buffer, nbytes=0, flags=0):
        self._ensure_handshake()
        return self.sock.recv_into(buffer, nbytes, flags)

    def sendto(self, string, arg1, arg2=None):
        # as observed on CPython, sendto when wrapped ignores the
        # destination address, thereby behaving just like send
        self._ensure_handshake()
        return self.sock.send(string)

    def close(self):
        self.sock.close()

    def setblocking(self, mode):
        self.sock.setblocking(mode)

    def settimeout(self, timeout):
        self.sock.settimeout(timeout)

    def gettimeout(self):
        return self.sock.gettimeout()

    def makefile(self, mode='r', bufsize=-1):
        return self.sock.makefile(mode, bufsize)

    def shutdown(self, how):
        self.sock.shutdown(how)
    # Need to work with the real underlying socket as well

    def pending(self):
        # undocumented function, used by some tests
        # see also http://bugs.python.org/issue21430
        return self._sock._pending()

    def _readable(self):
        return self._sock._readable()

    def _writable(self):
        return self._sock._writable()

    def _register_selector(self, selector):
        self._sock._register_selector(selector)

    def _unregister_selector(self, selector):
        return self._sock._unregister_selector(selector)

    def _notify_selectors(self):
        self._sock._notify_selectors()

    def _checkClosed(self, msg=None):
        # raise an exception here if you wish to check for spurious closes
        pass

    def _check_connected(self):
        if not self._connected:
            # getpeername() will raise ENOTCONN if the socket is really
            # not connected; note that we can be connected even without
            # _connected being set, e.g. if connect() first returned
            # EAGAIN.
            self.getpeername()

    def getpeername(self):
        return self.sock.getpeername()

    def selected_npn_protocol(self):
        self._checkClosed()
        # TODO Jython
        return None

    def selected_alpn_protocol(self):
        self._checkClosed()
        # TODO Jython

    def fileno(self):
        return self

    @raises_java_exception
    def getpeercert(self, binary_form=False):
        cert = self.engine.getSession().getPeerCertificates()[0]
        if binary_form:
            return cert.getEncoded()

        if self._context.verify_mode == CERT_NONE:
            return {}

        dn = cert.getSubjectX500Principal().getName()
        rdns = SSLContext._parse_dn(dn)
        alt_names = tuple()
        if cert.getSubjectAlternativeNames():
            alt_names = tuple(((_cert_name_types[type], str(name)) for (type, name) in cert.getSubjectAlternativeNames()))

        pycert = {
            "notAfter": str(_rfc2822_date_format.format(cert.getNotAfter())),
            "subject": rdns,
            "subjectAltName": alt_names,
        }
        return pycert

    @raises_java_exception
    def issuer(self):
        return self.getpeercert().getIssuerDN().toString()

    def cipher(self):
        session = self.engine.getSession()
        suite = str(session.cipherSuite)
        if "256" in suite:  # FIXME!!! this test usually works, but there must be a better approach
            strength = 256
        elif "128" in suite:
            strength = 128
        else:
            strength = None
        return suite, str(session.protocol), strength

    def get_channel_binding(self, cb_type="tls-unique"):
        """Get channel binding data for current connection.  Raise ValueError
        if the requested `cb_type` is not supported.  Return bytes of the data
        or None if the data is not available (e.g. before the handshake).
        """
        if cb_type not in CHANNEL_BINDING_TYPES:
            raise ValueError("Unsupported channel binding type")
        if cb_type != "tls-unique":
            raise NotImplementedError(
                "{0} channel binding type not implemented"
                    .format(cb_type))

        # TODO support this properly
        return None
        # if self._sslobj is None:
        #     return None
        # return self._sslobj.tls_unique_cb()

    def version(self):
        if self.ssl_handler:
            return str(self.engine.getSession().getProtocol())
        return None

# instantiates a SSLEngine, with the following things to keep in mind:

# FIXME not yet supported
# suppress_ragged_eofs - presumably this is an exception we can detect in Netty, the underlying SSLEngine certainly does
# ssl_version - use SSLEngine.setEnabledProtocols(java.lang.String[])
# ciphers - SSLEngine.setEnabledCipherSuites(String[] suites)

@raises_java_exception
def wrap_socket(sock, keyfile=None, certfile=None, server_side=False, cert_reqs=CERT_NONE,
                ssl_version=PROTOCOL_SSLv23, ca_certs=None, do_handshake_on_connect=True,
                suppress_ragged_eofs=True, ciphers=None):

    return SSLSocket(
        sock,
        keyfile=keyfile, certfile=certfile, cert_reqs=cert_reqs, ca_certs=ca_certs,
        server_side=server_side, ssl_version=ssl_version, ciphers=ciphers,
        do_handshake_on_connect=do_handshake_on_connect)


# some utility functions

def cert_time_to_seconds(cert_time):
    """Return the time in seconds since the Epoch, given the timestring
    representing the "notBefore" or "notAfter" date from a certificate
    in ``"%b %d %H:%M:%S %Y %Z"`` strptime format (C locale).

    "notBefore" or "notAfter" dates must use UTC (RFC 5280).

    Month is one of: Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec
    UTC should be specified as GMT (see ASN1_TIME_print())
    """
    from time import strptime
    from calendar import timegm

    months = (
        "Jan","Feb","Mar","Apr","May","Jun",
        "Jul","Aug","Sep","Oct","Nov","Dec"
    )
    time_format = ' %d %H:%M:%S %Y GMT' # NOTE: no month, fixed GMT
    try:
        month_number = months.index(cert_time[:3].title()) + 1
    except ValueError:
        raise ValueError('time data %r does not match '
                         'format "%%b%s"' % (cert_time, time_format))
    else:
        # found valid month
        tt = strptime(cert_time[3:], time_format)
        # return an integer, the previous mktime()-based implementation
        # returned a float (fractional seconds are always zero here).
        return timegm((tt[0], month_number) + tt[2:6])


PEM_HEADER = "-----BEGIN CERTIFICATE-----"
PEM_FOOTER = "-----END CERTIFICATE-----"


def DER_cert_to_PEM_cert(der_cert_bytes):
    """Takes a certificate in binary DER format and returns the
    PEM version of it as a string."""

    if hasattr(base64, 'standard_b64encode'):
        # preferred because older API gets line-length wrong
        f = base64.standard_b64encode(der_cert_bytes)
        return (PEM_HEADER + '\n' +
                textwrap.fill(f, 64) + '\n' +
                PEM_FOOTER + '\n')
    else:
        return (PEM_HEADER + '\n' +
                base64.encodestring(der_cert_bytes) +
                PEM_FOOTER + '\n')


def PEM_cert_to_DER_cert(pem_cert_string):
    """Takes a certificate in ASCII PEM format and returns the
    DER-encoded version of it as a byte sequence"""

    if not pem_cert_string.startswith(PEM_HEADER):
        raise ValueError("Invalid PEM encoding; must start with %s"
                         % PEM_HEADER)
    if not pem_cert_string.strip().endswith(PEM_FOOTER):
        raise ValueError("Invalid PEM encoding; must end with %s"
                         % PEM_FOOTER)
    d = pem_cert_string.strip()[len(PEM_HEADER):-len(PEM_FOOTER)]
    return base64.decodestring(d)


def get_server_certificate(addr, ssl_version=PROTOCOL_SSLv3, ca_certs=None):
    """Retrieve the certificate from the server at the specified address,
    and return it as a PEM-encoded string.
    If 'ca_certs' is specified, validate the server cert against it.
    If 'ssl_version' is specified, use it in the connection attempt."""

    host, port = addr
    if (ca_certs is not None):
        cert_reqs = CERT_REQUIRED
    else:
        cert_reqs = CERT_NONE

    s = wrap_socket(socket(), ssl_version=ssl_version,
                    cert_reqs=cert_reqs, ca_certs=ca_certs)
    s.connect(addr)
    dercert = s.getpeercert(True)
    s.close()
    return DER_cert_to_PEM_cert(dercert)


def get_protocol_name(protocol_code):
    return _PROTOCOL_NAMES.get(protocol_code, '<unknown>')


# a replacement for the old socket.ssl function

def sslwrap_simple(sock, keyfile=None, certfile=None):
    """A replacement for the old socket.ssl function.  Designed
    for compability with Python 2.5 and earlier.  Will disappear in
    Python 3.0."""

    ssl_sock = wrap_socket(sock, keyfile=keyfile, certfile=certfile, ssl_version=PROTOCOL_SSLv23)
    try:
        sock.getpeername()
    except socket_error:
        # no, no connection yet
        pass
    else:
        # yes, do the handshake
        ssl_sock.do_handshake()

    return ssl_sock


# Underlying Java does a good job of managing entropy, so these are just no-ops

def RAND_status():
    return True


def RAND_egd(path):
    if os.path.abspath(str(path)) != path:
        raise TypeError("Must be an absolute path, but ignoring it regardless")


def RAND_add(bytes, entropy):
    pass


class SSLContext(object):

    _jsse_keyType_names = ('RSA', 'DSA', 'DH_RSA', 'DH_DSA', 'EC', 'EC_EC', 'EC_RSA')

    def __init__(self, protocol):
        try:
            self._protocol_name = _PROTOCOL_NAMES[protocol]
        except KeyError:
            raise ValueError("invalid protocol version")

        if protocol == PROTOCOL_SSLv23:  # darjus: at least my Java does not let me use v2
            self._protocol_name = 'SSL'

        self.protocol = protocol
        self._check_hostname = False

        # defaults from _ssl.c
        self.options = OP_ALL | OP_NO_SSLv2 | OP_NO_SSLv3
        self._verify_flags = VERIFY_DEFAULT
        self._verify_mode = CERT_NONE
        self._ciphers = None

        self._trust_store = KeyStore.getInstance(KeyStore.getDefaultType())
        self._trust_store.load(None, None)

        self._key_store = KeyStore.getInstance(KeyStore.getDefaultType())
        self._key_store.load(None, None)

        self._key_managers = None

        self._server_name_callback = None

    def wrap_socket(self, sock, server_side=False,
                    do_handshake_on_connect=True,
                    suppress_ragged_eofs=True,
                    server_hostname=None):
        return SSLSocket(sock=sock, server_side=server_side,
                         do_handshake_on_connect=do_handshake_on_connect,
                         suppress_ragged_eofs=suppress_ragged_eofs,
                         server_hostname=server_hostname,
                         _context=self)

    def _createSSLEngine(self, addr, hostname=None, cert_file=None, key_file=None, server_side=False):
        tmf = InsecureTrustManagerFactory.INSTANCE
        if self.verify_mode != CERT_NONE:
            # XXX need to refactor so we don't have to get trust managers twice
            stmf = SimpleTrustManagerFactory.getInstance(SimpleTrustManagerFactory.getDefaultAlgorithm())
            stmf.init(self._trust_store)

            tmf = CompositeX509TrustManagerFactory(stmf.getTrustManagers())
            tmf.init(self._trust_store)

        kmf = self._key_managers
        if self._key_managers is None:
            kmf = _get_openssl_key_manager(cert_file=cert_file, key_file=key_file)

        context_builder = None

        if not server_side:
            context_builder = SslContextBuilder.forClient()

        if kmf:
            if server_side:
                context_builder = SslContextBuilder.forServer(kmf)
            else:
                context_builder = context_builder.keyManager(kmf)

        context_builder = context_builder.trustManager(tmf)
        context_builder = context_builder.sslProvider(SslProvider.JDK)
        context_builder = context_builder.clientAuth(_CERT_TO_CLIENT_AUTH[self.verify_mode])

        if self._ciphers is not None:
            context_builder = context_builder.ciphers(self._ciphers)

        if self._check_hostname:
            engine = context_builder.build().newEngine(ByteBufAllocator.DEFAULT, hostname, addr[1])
            if HAS_SNI:
                params = engine.getSSLParameters()
                params.setEndpointIdentificationAlgorithm('HTTPS')
                params.setServerNames([SNIHostName(hostname)])
                engine.setSSLParameters(params)
        else:
            engine = context_builder.build().newEngine(ByteBufAllocator.DEFAULT, addr[0], addr[1])

        return engine

    def cert_store_stats(self):
        return {'crl': 0, 'x509': self._key_store.size(), 'x509_ca': self._trust_store.size()}

    def load_cert_chain(self, certfile, keyfile=None, password=None):
        try:
            self._key_managers = _get_openssl_key_manager(certfile, keyfile, password, _key_store=self._key_store)
        except IllegalArgumentException as err:
            raise SSLError(SSL_ERROR_SSL, "PEM lib ({})".format(err))

    def set_ciphers(self, ciphers):
        # TODO conversion from OpenSSL to http://www.iana.org/assignments/tls-parameters/tls-parameters.xml
        # as Java knows no other
        #self._ciphers = ciphers
        pass

    def load_verify_locations(self, cafile=None, capath=None, cadata=None):
        if cafile is None and capath is None and cadata is None:
            raise TypeError("cafile, capath and cadata cannot be all omitted")

        cafiles = []
        if cafile is not None:
            cafiles.append(cafile)

        if capath is not None:
            for fname in os.listdir(capath):
                _, ext = os.path.splitext(fname)
                possible_cafile = os.path.join(capath, fname)
                if ext.lower() == 'pem':
                    cafiles.append(possible_cafile)
                elif fname == 'cacerts':  # java truststore
                    if os.path.isfile(possible_cafile):
                        cafiles.append(possible_cafile)
                elif os.path.isfile(possible_cafile):
                    try:
                        with open(possible_cafile) as f:
                            if PEM_HEADER in f.read():
                                cafiles.append(possible_cafile)
                    except IOError:
                        log.debug("Not including %s file as a possible cafile due to permissions error" % possible_cafile)
                        pass  # Probably permissions related...ignore

        certs = []
        private_key = None
        if cadata is not None:
            certs, private_key = _extract_cert_from_data(cadata)

        _certs, private_key = _extract_certs_for_paths(cafiles)
        certs.extend(_certs)
        for cert in certs:
            # FIXME not sure this is correct?
            if private_key is None:
                self._trust_store.setCertificateEntry(_str_hash_key_entry(cert), cert)
            else:
                self._key_store.setCertificateEntry(_str_hash_key_entry(cert), cert)

    def load_default_certs(self, purpose=Purpose.SERVER_AUTH):
        # TODO handle/support purpose
        if not isinstance(purpose, _ASN1Object):
            raise TypeError(purpose)

        self.set_default_verify_paths()

    def set_default_verify_paths(self):
        """
        Load a set of default "certification authority" (CA) certificates from a filesystem path defined when building
        the OpenSSL library. Unfortunately, there's no easy way to know whether this method succeeds: no error is
        returned if no certificates are to be found. When the OpenSSL library is provided as part of the operating
        system, though, it is likely to be configured properly.
        """
        default_verify_paths = get_default_verify_paths()

        self.load_verify_locations(cafile=default_verify_paths.cafile, capath=default_verify_paths.capath)

    def set_alpn_protocols(self, protocols):
        raise NotImplementedError()

    def set_npn_protocols(self, protocols):
        raise NotImplementedError()

    def set_servername_callback(self, server_name_callback):
        if not callable(server_name_callback) and server_name_callback is not None:
            raise TypeError("{!r} is not callable".format(server_name_callback))
        self._server_name_callback = server_name_callback


    def load_dh_params(self, dhfile):
        # TODO?
        pass

    def set_ecdh_curve(self, curve_name):
        params = _get_ecdh_parameter_spec(curve_name)

    def session_stats(self):
        # TODO
        return {
            'number': 0,
            'connect': 0,
            'connect_good': 0,
            'connect_renegotiate': 0,
            'accept': 0,
            'accept_good': 0,
            'accept_renegotiate': 0,
            'hits': 0,
            'misses': 0,
            'timeouts': 0,
            'cache_full': 0,
        }

    def get_ca_certs(self, binary_form=False):
        """get_ca_certs(binary_form=False) -> list of loaded certificate

        Returns a list of dicts with information of loaded CA certs. If the optional argument is True,
        returns a DER-encoded copy of the CA certificate.
        NOTE: Certificates in a capath directory aren't loaded unless they have been used at least once.
        """
        certs = []
        for alias in self._trust_store.aliases():
            if self._trust_store.isCertificateEntry(alias):
                cert = self._trust_store.getCertificate(alias)
                if binary_form:
                    certs.append(cert.getEncoded().tostring())
                else:
                    issuer_info = self._parse_dn(cert.issuerDN)
                    subject_info = self._parse_dn(cert.subjectDN)

                    cert_info = {'issuer': issuer_info, 'subject': subject_info}
                    for k in ('serialNumber', 'version'):
                        cert_info[k] = getattr(cert, k)

                    for k in ('notBefore', 'notAfter'):
                        cert_info[k] = str(_rfc2822_date_format.format(getattr(cert, k)))

                    certs.append(cert_info)

        return certs

    @property
    def check_hostname(self):
        return self._check_hostname

    @check_hostname.setter
    def check_hostname(self, val):
        if val and self.verify_mode == CERT_NONE:
            raise ValueError("check_hostname needs a SSL context with either "
                             "CERT_OPTIONAL or CERT_REQUIRED")
        self._check_hostname = val

    @property
    def verify_mode(self):
        return self._verify_mode

    @verify_mode.setter
    def verify_mode(self, val):
        if not isinstance(val, int):
            raise TypeError("verfy_mode must be one of the ssl.CERT_* modes")

        if val not in (CERT_NONE, CERT_OPTIONAL, CERT_REQUIRED):
            raise ValueError("verfy_mode must be one of the ssl.CERT_* modes")

        if self.check_hostname and val == CERT_NONE:
            raise ValueError("Cannot set verify_mode to CERT_NONE when "
                             "check_hostname is enabled.")
        self._verify_mode = val

    @property
    def verify_flags(self):
        return self._verify_flags

    @verify_flags.setter
    def verify_flags(self, val):
        if not isinstance(val, int):
            raise TypeError("verfy_flags must be one of the ssl.VERIFY_* flags")
        self._verify_flags = val

    @classmethod
    def _parse_dn(cls, dn):
        ln = LdapName(unicode(dn))
        # FIXME given this tuple of a single element tuple structure assumed here, is it possible this is
        # not actually the case, eg because of multi value attributes?
        return tuple((((_ldap_rdn_display_names.get(rdn.type), _str_or_unicode(rdn.value)),) for rdn in ln.getRdns()))
