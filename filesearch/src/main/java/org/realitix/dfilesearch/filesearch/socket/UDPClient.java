package org.realitix.dfilesearch.filesearch.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
import org.realitix.dfilesearch.filesearch.util.NodeMap;

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


    /**
     * Creates a channel abstraction on top of a socket.
     * Uses NioDatagramChannel (for UDP), therefore no notion of a connection. Channel is only bound to a port.
     * Channel acts as a vehicle which unreliably deliver DatagramPackets to their destinations.
     *
     * @param channelInitializer initializer of the channel.
     * @return created channel.
     * @throws InterruptedException interruptions
     */
    public Channel createChannel(ChannelInitializer<DatagramChannel> channelInitializer) throws InterruptedException {
        Bootstrap b = new Bootstrap();
        b.group(new NioEventLoopGroup())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .localAddress(host, port)
                .handler(channelInitializer);
        return b.bind(host, port).sync().channel();
    }

    /**
     * Runs the client socket
     * Registers the node with BS
     * Method connect() connects to a remote server and bind() binds the process to a local socket
     *
     * @param bootstrapIp   server host IP
     * @param bootstrapPort server port
     *                      host and port should be configured in the jar.
     */
    public ChannelFuture register(String bootstrapIp, int bootstrapPort) throws InterruptedException {
        Channel channel = createChannel(new UDPClientInitializer());
        ChannelFuture future = null;
        try {
            InetSocketAddress localAddress = (InetSocketAddress) channel.localAddress();
            future = write(channel,
                    new RegisterRequest(localAddress.getHostString(), localAddress.getPort(), username),
                    bootstrapIp,
                    bootstrapPort);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();         // Interrupt should not be ignored.
        }
        return future;
    }

    public void join(Channel channel, NodeMap nodeMap, String hostIp, int hostPort) throws InterruptedException {
        logger.info("----- JOINING NODEMAP OF SIZE" + nodeMap.getNodeMap().size() + " ------");
        for (java.util.Map.Entry<Integer, Node> integerNodeEntry : nodeMap.getNodeMap().entrySet()) {
            Node node = (Node) integerNodeEntry;
            write(channel, new JoinRequest(hostIp, hostPort), node.getIp(), node.getPort());
        }
        logger.info("----- END OF JOINING ------");
    }

    /**
     * Write different messages
     *
     * @param channel    channel between the client and server
     * @param message    transmitted message
     * @param remoteIp   IP of bootstrap server
     * @param remotePort IP of bootstrap server
     * @throws InterruptedException
     */
    public ChannelFuture write(Channel channel, CommonMessage message, String remoteIp, int remotePort)
            throws InterruptedException {
        return channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8),
                SocketUtils.socketAddress(remoteIp, remotePort))).sync().await();
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

    public FileExecutorConfiguration getConfiguration() {
        return configuration;
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

        private UDPClientBuilder() {
        }

        public UDPClientBuilder setHost(String host) {
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

