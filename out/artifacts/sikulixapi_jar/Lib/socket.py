# dispatches to _socket for actual implementation

from _socket import (
    socket, SocketType, error, herror, gaierror, timeout, has_ipv6,

    create_connection,

    getdefaulttimeout,
    setdefaulttimeout,
    
    getfqdn,
    gethostbyaddr,
    gethostbyname,
    gethostbyname_ex,
    gethostname,
    getprotobyname,
    getservbyname,
    getservbyport,

    AF_UNSPEC,
    AF_INET,
    AF_INET6,

    AI_PASSIVE,
    AI_CANONNAME,
    AI_NUMERICHOST,
    AI_V4MAPPED,
    AI_ALL,
    AI_ADDRCONFIG,
    AI_NUMERICSERV,

    EAI_NONAME,
    EAI_SERVICE,
    EAI_ADDRFAMILY,
    
    NI_NUMERICHOST,
    NI_NUMERICSERV,
    NI_NOFQDN,
    NI_NAMEREQD,
    NI_DGRAM,
    NI_MAXSERV,
    NI_IDN,
    NI_IDN_ALLOW_UNASSIGNED,
    NI_IDN_USE_STD3_ASCII_RULES,
    NI_MAXHOST,

    SHUT_RD,
    SHUT_WR,
    SHUT_RDWR,

    SOCK_DGRAM,
    SOCK_STREAM,
    SOCK_RAW,
    SOCK_RDM,
    SOCK_SEQPACKET,
    
    SOL_SOCKET,
    SOL_TCP,
    # not supported, but here for apparent completeness
    IPPROTO_AH,
    IPPROTO_DSTOPTS,
    IPPROTO_ESP,
    IPPROTO_FRAGMENT,
    IPPROTO_GGP,
    IPPROTO_HOPOPTS,
    IPPROTO_ICMP,
    IPPROTO_ICMPV6,
    IPPROTO_IDP,
    IPPROTO_IGMP,
    IPPROTO_IP, # supported
    # not supported
    IPPROTO_IPV4,
    IPPROTO_IPV6,
    IPPROTO_MAX,
    IPPROTO_ND,
    IPPROTO_NONE,
    IPPROTO_PUP,
    IPPROTO_RAW,
    IPPROTO_ROUTING,
    IPPROTO_TCP, # supported
    IPPROTO_UDP, # supported

    # supported
    SO_BROADCAST,
    SO_KEEPALIVE,
    SO_LINGER,
    SO_RCVBUF,
    SO_REUSEADDR,
    SO_SNDBUF,
    SO_TIMEOUT,
    TCP_NODELAY,

    # pseudo options
    SO_ACCEPTCONN,
    SO_ERROR,
    SO_TYPE,

    # unsupported, will return errno.ENOPROTOOPT if actually used
    SO_OOBINLINE,
    SO_DEBUG,
    SO_DONTROUTE,
    SO_RCVLOWAT,
    SO_RCVTIMEO,
    SO_REUSEPORT,
    SO_SNDLOWAT,
    SO_SNDTIMEO,
    SO_USELOOPBACK,
    
    INADDR_ANY,
    INADDR_BROADCAST,
    IN6ADDR_ANY_INIT,

    _GLOBAL_DEFAULT_TIMEOUT,

    is_ipv4_address, is_ipv6_address, is_ip_address,
    getaddrinfo,
    getnameinfo,
    htons,
    htonl,
    ntohs,
    ntohl,
    inet_aton,
    inet_ntoa,
    inet_pton,
    inet_ntop,

    _fileobject,
    _get_jsockaddr
)


def supports(feature):
    # FIXME this seems to be Jython internals specific, and for
    # testing only; consider removing since it really no longer
    # matters
 
    if feature == "idna":
        return True
    raise KeyError("Unknown feature", feature)



