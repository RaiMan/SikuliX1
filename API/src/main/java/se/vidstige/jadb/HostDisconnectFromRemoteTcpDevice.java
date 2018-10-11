package se.vidstige.jadb;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HostDisconnectFromRemoteTcpDevice extends HostConnectionCommand {
    HostDisconnectFromRemoteTcpDevice(Transport transport) {
        super(transport, new ResponseValidatorImp());

    }

    //Visible for testing
    HostDisconnectFromRemoteTcpDevice(Transport transport, ResponseValidator responseValidator) {
        super(transport, responseValidator);
    }

    InetSocketAddress disconnect(InetSocketAddress inetSocketAddress)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        return executeHostCommand("disconnect", inetSocketAddress);
    }

    static final class ResponseValidatorImp extends ResponseValidatorBase {
        private static final String SUCCESSFULLY_DISCONNECTED = "disconnected";
        private static final String ALREADY_DISCONNECTED = "error: no such device";

        ResponseValidatorImp() {
            super(SUCCESSFULLY_DISCONNECTED, ALREADY_DISCONNECTED);
        }
    }
}
