package org.realitix.dfilesearch.filesearch.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.beans.messages.CommonMessage;
import org.realitix.dfilesearch.filesearch.beans.messages.JoinRequest;
import org.realitix.dfilesearch.filesearch.beans.messages.RegisterRequest;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;

import java.net.InetSocketAddress;

public class UDPClient {

    private String host;
    private int port;
    private String username;
    private FileExecutorConfiguration configuration;
    private final Logger logger = Logger.getLogger(UDPClient.class);

    private UDPClient(UDPClientBuilder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.configuration = builder.configuration;
    }

    public Channel createChannel(String remoteIp, int remotePort, ChannelInitializer<DatagramChannel> channelInitializer) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(host, port)
                .remoteAddress(remoteIp, remotePort)
                .handler(channelInitializer);
        return b.connect().sync().await().channel();
    }

    public Channel createChannel(String localIp, int localPort, String remoteIp, int remotePort, ChannelInitializer<DatagramChannel> channelInitializer) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(localIp, localPort)
                .remoteAddress(remoteIp, remotePort)
                .handler(channelInitializer);
        return b.connect().sync().await().channel();
    }

    /**
     * Runs the client socket
     * Registers the node with BS
     * Method connect() connects to a remote server and bind() binds the process to a local socket
     * @param bootstrapIp server host IP
     * @param bootstrapPort server port
     * host and port should be configured in the jar.
     */
    public ChannelFuture register(String bootstrapIp, int bootstrapPort) throws InterruptedException {
        Channel channel = createChannel(bootstrapIp, bootstrapPort, new UDPClientInitializer());
        ChannelFuture future = null;
        try {
            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
            future = write(channel, (new RegisterRequest(localAddress.getHostString(), localAddress.getPort(), username)), bootstrapIp, bootstrapPort);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();         // Interrupt should not be ignored.
        }
        return future;
    }

    /**
     * Sends the JOIN request to the neighbors
     * @param neighbour1 first neighbour
     * @param neighbour2 second neighbour
     * @throws InterruptedException
     */
    public void join(Node neighbour1, Node neighbour2) throws InterruptedException {
        neighbour1.setChannel(createChannel(configuration.getClientsToNeighbours().get(1).getHost(), configuration.getClientsToNeighbours().get(1).getPort(), neighbour1.getIp(), neighbour1.getPort(),  new UDPJoinInitializer(configuration)));
        logger.info("First node: " + neighbour1.getIp() + ":" + neighbour1.getPort());
        write(neighbour1.getChannel(), new JoinRequest(host, port), neighbour1.getIp(), neighbour1.getPort());
        if (neighbour2 != null) {
            neighbour2.setChannel(createChannel(configuration.getClientsToNeighbours().get(2).getHost(), configuration.getClientsToNeighbours().get(2).getPort(), neighbour2.getIp(), neighbour2.getPort(),  new UDPJoinInitializer(configuration)));
            write(neighbour2.getChannel(), new JoinRequest(host, port), neighbour2.getIp(), neighbour2.getPort());
        }
    }

    /**
     * Write different messages
     * @param channel channel between the client and server
     * @param message transmitted message
     * @param remoteIp IP of bootstrap server
     * @param remotePort IP of bootstrap server
     * @throws InterruptedException
     */
    private ChannelFuture write(Channel channel, CommonMessage message, String remoteIp, int remotePort)
            throws InterruptedException {
       return channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8),
               SocketUtils.socketAddress(remoteIp, remotePort))).sync().await();
    }

    public FileExecutorConfiguration getConfiguration() {
        return configuration;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Builder class for the client
     */
    public static class UDPClientBuilder {
        private String host;
        private int port;
        private String username;
        private FileExecutorConfiguration configuration;

        public static UDPClientBuilder newInstance() {
            return new UDPClientBuilder();
        }

        private UDPClientBuilder() {}

        public UDPClientBuilder setHost (String host) {
            this.host = host;
            return this;
        }

        public UDPClientBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public UDPClientBuilder setUsername(String username) {
            this.username = username;
            return this;
        }

        public UDPClient build(FileExecutorConfiguration configuration) {
            this.configuration = configuration;
            return new UDPClient(this);
        }
    }


}

