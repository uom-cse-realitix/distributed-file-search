package org.realitix.dfilesearch.filesearch.udp;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDPSocketStream{

    private String host;
    private int port;

    public UDPSocketStream(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void send() {

    }

    protected OutputStream socketOutputStream(final String host, final int port) throws SocketException {
        final DatagramSocket datagramSocket;
        datagramSocket = new DatagramSocket();
        return new OutputStream() {
            @Override
            public void write(int i) throws IOException {
                throw new UnsupportedOperationException("Datagram doesn't work at byte level");
            }

            @Override
            public void write(byte[] bytes, int i, int i1) throws IOException {
                datagramSocket.send(new DatagramPacket(bytes, i, i1, InetAddress.getByName(host), port));
            }

            @Override
            public void close() throws IOException {
                datagramSocket.close();
            }
        };
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

