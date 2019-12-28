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

public class UDPClient {

    private String host;
    private int port;
    private String username;
    private final Logger logger = Logger.getLogger(UDPClient.class);
    private EventLoopGroup workerGroup;

    private UDPClient(UDPClientBuilder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.username = builder.username;
        this.workerGroup = new NioEventLoopGroup();
    }

    /**
     * Runs the client socket
     * Registers the node with BS
     * @param bootstrapIp server host IP
     * @param bootstrapPort server port
     * host and port should be configured in the jar.
     */
    public void register(String bootstrapIp, int bootstrapPort) {
        Channel channel;
        Bootstrap b = new Bootstrap();
        b.group(getWorkerGroup())
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                        datagramChannel.pipeline().addLast(new UDPClientHandler());
                    }
                });
        try {
            channel = b.bind(this.host, this.port).sync().await().channel();
            write(channel, (new RegisterRequest(host, port, username)), bootstrapIp, bootstrapPort);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    /**
     * Sends the JOIN request to the neighbors
     * @param neighbour1 first neighbour
     * @param neighbour2 second neighbour
     * @param channel client channel open for communication with other components.
     * @throws InterruptedException
     */
    private void join(Node neighbour1, Node neighbour2, Channel channel) throws InterruptedException {
        write(
                channel,
                new JoinRequest(neighbour1.getIp(),
                        neighbour1.getPort()),
                neighbour1.getIp(),
                neighbour1.getPort()
        );
        write(
                channel,
                new JoinRequest(neighbour2.getIp(),
                        neighbour2.getPort()),
                neighbour2.getIp(),
                neighbour2.getPort()
        );
    }

    /**
     * Write different messages
     * @param channel channel between the client and server
     * @param message transmitted message
     * @param bootstrapIp IP of bootstrap server
     * @param bootstrapPort IP of bootstrap server
     * @throws InterruptedException
     */
    private void write(Channel channel, CommonMessage message, String bootstrapIp, int bootstrapPort) throws InterruptedException {
       channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8),
               SocketUtils.socketAddress(bootstrapIp, bootstrapPort))).sync().await();
    }

    private EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    /**
     * Builder class for the client
     */
    public static class UDPClientBuilder {
        private String host;
        private int port;
        private String username;

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

        public UDPClient build() {
            return new UDPClient(this);
        }
    }


}

