package se.vidstige.jadb;

import java.io.IOException;
import java.net.InetSocketAddress;

class HostConnectToRemoteTcpDevice extends HostConnectionCommand {
    HostConnectToRemoteTcpDevice(Transport transport) {
        super(transport, new ResponseValidatorImp());
    }

    //Visible for testing
    HostConnectToRemoteTcpDevice(Transport transport, ResponseValidator responseValidator) {
        super(transport, responseValidator);
    }

    InetSocketAddress connect(InetSocketAddress inetSocketAddress)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        return executeHostCommand("connect", inetSocketAddress);
    }

    static final class ResponseValidatorImp extends ResponseValidatorBase {
        private static final String SUCCESSFULLY_CONNECTED = "connected to";
        private static final String ALREADY_CONNECTED = "already connected to";

        ResponseValidatorImp() {
            super(SUCCESSFULLY_CONNECTED, ALREADY_CONNECTED);
        }
    }
}
