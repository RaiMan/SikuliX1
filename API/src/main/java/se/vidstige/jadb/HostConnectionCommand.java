package se.vidstige.jadb;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HostConnectionCommand {
    private final Transport transport;
    private final ResponseValidator responseValidator;

    HostConnectionCommand(Transport transport, ResponseValidator responseValidator) {
        this.transport = transport;
        this.responseValidator = responseValidator;
    }

    InetSocketAddress executeHostCommand(String command, InetSocketAddress inetSocketAddress)
            throws IOException, JadbException, ConnectionToRemoteDeviceException {
        transport.send(String.format("host:%s:%s:%d", command, inetSocketAddress.getHostString(), inetSocketAddress.getPort()));
        verifyTransportLevel();
        verifyProtocolLevel();

        return inetSocketAddress;
    }

    private void verifyTransportLevel() throws IOException, JadbException {
        transport.verifyResponse();
    }

    private void verifyProtocolLevel() throws IOException, ConnectionToRemoteDeviceException {
        String status = transport.readString();
        responseValidator.validate(status);
    }

    //@VisibleForTesting
    interface ResponseValidator {
        void validate(String response) throws ConnectionToRemoteDeviceException;
    }

    static class ResponseValidatorBase implements ResponseValidator {
        private final String successMessage;
        private final String errorMessage;

        ResponseValidatorBase(String successMessage, String errorMessage) {
            this.successMessage = successMessage;
            this.errorMessage = errorMessage;
        }

        public void validate(String response) throws ConnectionToRemoteDeviceException {
            if (!checkIfConnectedSuccessfully(response) && !checkIfAlreadyConnected(response)) {
                throw new ConnectionToRemoteDeviceException(extractError(response));
            }
        }

        private boolean checkIfConnectedSuccessfully(String response) {
            return response.startsWith(successMessage);
        }

        private boolean checkIfAlreadyConnected(String response) {
            return response.startsWith(errorMessage);
        }

        private String extractError(String response) {
            int lastColon = response.lastIndexOf(':');
            if (lastColon != -1) {
                return response.substring(lastColon);
            } else {
                return response;
            }
        }
    }
}
