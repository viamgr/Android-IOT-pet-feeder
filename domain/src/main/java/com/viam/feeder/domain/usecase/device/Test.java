package com.viam.feeder.domain.usecase.device;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Test {
    /**
     * Check if host is reachable.
     *
     * @param host    The host to check for availability. Can either be a machine name, such as "google.com",
     *                or a textual representation of its IP address, such as "8.8.8.8".
     * @param port    The port number.
     * @param timeout The timeout in milliseconds.
     * @return True if the host is reachable. False otherwise.
     */
    public static boolean isHostAvailable(final String host, final int port, final int timeout) {
        try (final Socket socket = new Socket()) {
            final InetAddress inetAddress = InetAddress.getByName(host);
            final InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, port);

            socket.connect(inetSocketAddress, timeout);
            return true;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
