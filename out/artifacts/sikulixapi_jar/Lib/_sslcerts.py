import logging
import uuid
import re
from StringIO import StringIO
import types

from java.lang import RuntimeException, System
from java.io import BufferedInputStream, BufferedReader, FileReader, InputStreamReader, ByteArrayInputStream, IOException
from java.security import KeyStore, Security, InvalidAlgorithmParameterException
from java.security.cert import CertificateException, CertificateFactory
from java.security.interfaces import RSAPrivateCrtKey
from java.security.interfaces import RSAPublicKey
from javax.net.ssl import X509KeyManager, X509TrustManager, KeyManagerFactory, SSLContext

try:
    # jarjar-ed version
    from org.python.netty.handler.ssl.util import SimpleTrustManagerFactory

except ImportError:
    # dev version from extlibs
    from io.netty.handler.ssl.util import SimpleTrustManagerFactory

try:
    # dev version from extlibs OR if in classpath.
    #
    # Assumes BC's API is sufficiently stable, but this assumption
    # seems safe based on our experience using BC.
    #
    # This change in import ordering - compared to similar conditional
    # imports - is to workaround the problem in
    # http://bugs.jython.org/issue2469, due to the fact that jarjar-ed
    # jars - like other shading - lose their signatures. For most jars
    # this is not an issue, and we have been removing signature files
    # since 2.7.0. But in this specific case, removing signatures then
    # causes conflicts with Java's security provider model, because it
    # requires signing.
    from org.bouncycastle.asn1.pkcs import PrivateKeyInfo
    from org.bouncycastle.cert import X509CertificateHolder
    from org.bouncycastle.cert.jcajce import JcaX509CertificateConverter
    from org.bouncycastle.jce.provider import BouncyCastleProvider
    from org.bouncycastle.jce import ECNamedCurveTable
    from org.bouncycastle.jce.spec import ECNamedCurveSpec
    from org.bouncycastle.openssl import PEMKeyPair, PEMParser, PEMEncryptedKeyPair, PEMException, \
        EncryptionException
    from org.bouncycastle.openssl.jcajce import JcaPEMKeyConverter, JcePEMDecryptorProviderBuilder
except ImportError:
    # jarjar-ed version
    from org.python.bouncycastle.asn1.pkcs import PrivateKeyInfo
    from org.python.bouncycastle.cert import X509CertificateHolder
    from org.python.bouncycastle.cert.jcajce import JcaX509CertificateConverter
    from org.python.bouncycastle.jce.provider import BouncyCastleProvider
    from org.python.bouncycastle.jce import ECNamedCurveTable
    from org.python.bouncycastle.jce.spec import ECNamedCurveSpec
    from org.python.bouncycastle.openssl import PEMKeyPair, PEMParser, PEMEncryptedKeyPair, PEMException, \
        EncryptionException
    from org.python.bouncycastle.openssl.jcajce import JcaPEMKeyConverter, JcePEMDecryptorProviderBuilder

log = logging.getLogger("_socket")
Security.addProvider(BouncyCastleProvider())

RE_BEGIN_KEY_CERT = re.compile(r'^-----BEGIN.*(PRIVATE KEY|CERTIFICATE)-----$')


def _get_ca_certs_trust_manager(ca_certs=None):
    trust_store = KeyStore.getInstance(KeyStore.getDefaultType())
    trust_store.load(None, None)
    num_certs_installed = 0
    if ca_certs is not None:
        with open(ca_certs) as f:
            cf = CertificateFactory.getInstance("X.509")
            for cert in cf.generateCertificates(BufferedInputStream(f)):
                trust_store.setCertificateEntry(str(uuid.uuid4()), cert)
                num_certs_installed += 1
    tmf = SimpleTrustManagerFactory.getInstance(SimpleTrustManagerFactory.getDefaultAlgorithm())
    tmf.init(trust_store)
    log.debug("Installed %s certificates", num_certs_installed, extra={"sock": "*"})
    return tmf


def _stringio_as_reader(s):
    return BufferedReader(InputStreamReader(ByteArrayInputStream(bytearray(s.getvalue()))))


def _extract_readers(f):
    string_ios = []
    output = StringIO()
    key_cert_start_line_found = False
    for line in f:
        if RE_BEGIN_KEY_CERT.match(line):
            key_cert_start_line_found = True
            if output.getvalue():
                string_ios.append(output)

            output = StringIO()
        output.write(line)

    if output.getvalue():
        string_ios.append(output)

    if not key_cert_start_line_found:
        from _socket import SSLError, SSL_ERROR_SSL
        raise SSLError(SSL_ERROR_SSL, "PEM lib (no start line or not enough data)")

    return [_stringio_as_reader(sio) for sio in string_ios]


def _get_openssl_key_manager(cert_file=None, key_file=None, password=None, _key_store=None):
    certs, private_key = [], None

    if _key_store is None:
        _key_store = KeyStore.getInstance(KeyStore.getDefaultType())
        _key_store.load(None, None)

    if key_file is not None:
        certs, private_key = _extract_certs_for_paths([key_file], password)
        if private_key is None:
            from _socket import SSLError, SSL_ERROR_SSL
            raise SSLError(SSL_ERROR_SSL, "PEM lib (No private key loaded)")

    if cert_file is not None:
        _certs, _private_key = _extract_certs_for_paths([cert_file], password)
        private_key = _private_key if _private_key else private_key
        certs.extend(_certs)

        if not private_key:
            from _socket import SSLError, SSL_ERROR_SSL
            raise SSLError(SSL_ERROR_SSL, "PEM lib (No private key loaded)")

        keys_match, validateable_keys_found = False, False
        for cert in certs:
            # TODO works for RSA only for now
            if isinstance(cert.publicKey, RSAPublicKey) and isinstance(private_key, RSAPrivateCrtKey):
                validateable_keys_found = True

            if validateable_keys_found:
                if cert.publicKey.getModulus() == private_key.getModulus() \
                        and cert.publicKey.getPublicExponent() == private_key.getPublicExponent():
                    keys_match = True
                    break

        if key_file is not None and validateable_keys_found and not keys_match:
            from _socket import SSLError, SSL_ERROR_SSL
            raise SSLError(SSL_ERROR_SSL, "key values mismatch")

        _key_store.setKeyEntry(_str_hash_key_entry(private_key, *certs), private_key, [], certs)

    kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    kmf.init(_key_store, [])
    return kmf


def _str_hash_key_entry(*args):
    """Very naiive"""
    _hash = 0
    for arg in args:
        if arg:
            _hash += hash(arg.toString().encode('utf8'))

    return str(_hash)


def _parse_password(password):
    is_password_func = False
    if isinstance(password, (types.FunctionType, types.MethodType)) or hasattr(password, '__call__'):
        password = password()
        is_password_func = True
    if password is None:
        password = []
    elif isinstance(password, (str, bytearray)):
        password = str(password)
        if len(password) >= 102400:  # simulate openssl PEM_BUFSIZE limit error
            raise ValueError("PEM lib (password cannot be longer than 102400 -1)")
    else:
        if is_password_func:
            raise TypeError("PEM lib (must return a string")
        else:
            raise TypeError("PEM lib (password should be a string)")
    return password


def _extract_certs_from_keystore_file(f, password):
    keystore = KeyStore.getInstance(KeyStore.getDefaultType())
    if password is None:
        password = System.getProperty('javax.net.ssl.trustStorePassword')
        if password is None:  # default java keystore password is changeit
            password = 'changeit'
    elif not isinstance(password, str):
        password = []

    keystore.load(BufferedInputStream(f), password)
    certs = []

    alias_iter = keystore.aliases()
    while alias_iter.hasMoreElements():
        alias = alias_iter.nextElement()
        certs.append(keystore.getCertificate(alias))

    return certs


def _extract_certs_for_paths(paths, password=None):
    # Go from Bouncy Castle API to Java's; a bit heavyweight for the Python dev ;)
    key_converter = JcaPEMKeyConverter().setProvider("BC")
    cert_converter = JcaX509CertificateConverter().setProvider("BC")
    certs = []
    private_key = None
    for path in paths:
        err = None
        with open(path) as f:
            # try to load the file as keystore file first
            try:
                _certs = _extract_certs_from_keystore_file(f, password)
                certs.extend(_certs)
            except IOException as err:
                pass  # reported as 'Invalid keystore format'
        if err is not None:  # try loading pem version instead
            with open(path) as f:
                _certs, _private_key = _extract_cert_from_data(f, password, key_converter, cert_converter)
                private_key = _private_key if _private_key else private_key
                certs.extend(_certs)
    return certs, private_key


def _extract_cert_from_data(f, password=None, key_converter=None, cert_converter=None):
    certs = []
    private_key = None

    if isinstance(f, unicode):
        f = StringIO(str(f))
    elif isinstance(f, str):
        f = StringIO(f)

    if not hasattr(f, 'seek'):
        raise TypeError("PEM lib (data must be a file like object, string or bytes")

    if _is_cert_pem(f):
        certs, private_key = _read_pem_cert_from_data(f, password, key_converter, cert_converter)
    else:
        cf = CertificateFactory.getInstance("X.509")
        certs = list(cf.generateCertificates(ByteArrayInputStream(f.read())))

    return certs, private_key


def _read_pem_cert_from_data(f, password, key_converter, cert_converter):
    certs = []
    private_key = None

    if key_converter is None:
        key_converter = JcaPEMKeyConverter().setProvider("BC")
    if cert_converter is None:
        cert_converter = JcaX509CertificateConverter().setProvider("BC")
    for br in _extract_readers(f):
        while True:
            try:
                obj = PEMParser(br).readObject()
            except PEMException as err:
                from _socket import SSLError, SSL_ERROR_SSL
                raise SSLError(SSL_ERROR_SSL, "PEM lib ({})".format(err))

            if obj is None:
                break

            if isinstance(obj, PEMKeyPair):
                private_key = key_converter.getKeyPair(obj).getPrivate()
            elif isinstance(obj, PrivateKeyInfo):
                private_key = key_converter.getPrivateKey(obj)
            elif isinstance(obj, X509CertificateHolder):
                certs.append(cert_converter.getCertificate(obj))
            elif isinstance(obj, PEMEncryptedKeyPair):
                provider = JcePEMDecryptorProviderBuilder().build(_parse_password(password))
                try:
                    key_pair = key_converter.getKeyPair(obj.decryptKeyPair(provider))
                except EncryptionException as err:
                    from _socket import SSLError, SSL_ERROR_SSL
                    raise SSLError(SSL_ERROR_SSL, "PEM lib ({})".format(err))

                private_key = key_pair.getPrivate()
            else:
                raise NotImplementedError("Jython does not implement PEM object {!r}".format(obj))
    return certs, private_key


def _is_cert_pem(f):
    try:
        # check if the data is string specifically to support DER certs
        f.read().decode('ascii')
        return True
    except UnicodeDecodeError:
        return False
    finally:
        f.seek(0)

    assert "Should not reach here"


def _get_ecdh_parameter_spec(curve_name):
    if not isinstance(curve_name, str):
        raise TypeError("curve_name must be string/bytes")

    spec_param = ECNamedCurveTable.getParameterSpec(curve_name)
    if spec_param is None:
        raise ValueError("unknown elliptic curve name {}".format(curve_name))

    return ECNamedCurveSpec(spec_param.getName(), spec_param.getCurve(), spec_param.getG(), spec_param.getN(),
                            spec_param.getH(), spec_param.getSeed())


# CompositeX509KeyManager and CompositeX509TrustManager allow for mixing together Java built-in managers
# with new managers to support Python ssl.
#
# See http://tersesystems.com/2014/01/13/fixing-the-most-dangerous-code-in-the-world/
# for a good description of this composite approach.
#
# Ported to Python from http://codyaray.com/2013/04/java-ssl-with-multiple-keystores
# which was inspired by http://stackoverflow.com/questions/1793979/registering-multiple-keystores-in-jvm

class CompositeX509KeyManager(X509KeyManager):
                                                   
    def __init__(self, key_managers):
        self.key_managers = key_managers

    def chooseClientAlias(self, key_type, issuers, socket):
        for key_manager in self.key_managers:
            alias = key_manager.chooseClientAlias(key_type, issuers, socket)
            if alias:
                return alias;
        return None

    def chooseServerAlias(self, key_type, issuers, socket):
        for key_manager in self.key_managers:
            alias = key_manager.chooseServerAlias(key_type, issuers, socket)
            if alias:
                return alias;
        return None
    
    def getPrivateKey(self, alias):
        for key_manager in self.key_managers:
            private_key = key_manager.getPrivateKey(alias)
            if private_key:
                return private_key
        return None

    def getCertificateChain(self, alias):
        for key_manager in self.key_managers:
            chain = key_manager.getCertificateChain(alias)
            if chain:
                return chain
        return None

    def getClientAliases(self, key_type, issuers):
        aliases = []
        for key_manager in self.key_managers:
            aliases.extend(key_manager.getClientAliases(key_type, issuers))
        if not aliases:
            return None
        else:
            return aliases

    def getServerAliases(self, key_type, issuers):
        aliases = []
        for key_manager in self.key_managers:
            aliases.extend(key_manager.getServerAliases(key_type, issuers))
        if not aliases:
            return None
        else:
            return aliases


class CompositeX509TrustManager(X509TrustManager):

    def __init__(self, trust_managers):
        self.trust_managers = trust_managers

    def checkClientTrusted(self, chain, auth_type):
        for trust_manager in self.trust_managers:
            try:
                trust_manager.checkClientTrusted(chain, auth_type)
                return
            except CertificateException:
                continue
            except RuntimeException as err:
                # special-case to raise proper CertificateException ;)
                if isinstance(err.getCause(), InvalidAlgorithmParameterException):
                    if err.getCause().getMessage() == u'the trustAnchors parameter must be non-empty':
                        continue
                raise

        raise CertificateException("certificate verify failed")

    def checkServerTrusted(self, chain, auth_type):
        for trust_manager in self.trust_managers:
            try:
                trust_manager.checkServerTrusted(chain, auth_type)
                return
            except CertificateException:
                continue
            except RuntimeException as err:
                # special-case to raise proper CertificateException ;)
                if isinstance(err.getCause(), InvalidAlgorithmParameterException):
                    if err.getCause().getMessage() == u'the trustAnchors parameter must be non-empty':
                        continue
                raise

        raise CertificateException("certificate verify failed")

    def getAcceptedIssuers(self):
        certs = []
        for trust_manager in self.trust_managers:
            certs.extend(trust_manager.getAcceptedIssuers())
        return certs


class CompositeX509TrustManagerFactory(SimpleTrustManagerFactory):

    def __init__(self, trust_managers):
        self._trust_manager = CompositeX509TrustManager(trust_managers)

    def engineInit(self, arg):
        pass

    def engineGetTrustManagers(self):
        return [self._trust_manager]
