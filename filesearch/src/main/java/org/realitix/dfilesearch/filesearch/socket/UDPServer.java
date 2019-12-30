package org.realitix.dfilesearch.filesearch.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Runs a UDP server at port specified in the constructor.
 */
public class UDPServer {
    private final Logger logger = LogManager.getLogger(UDPServer.class);
    private String host;
    private int port;

    private UDPServer(UDPServerBuilder builder) {
        this.host = builder.host;
        this.port = builder.port;
    }

    /**
     * Note: https://stackoverflow.com/questions/41505852/netty-closefuture-sync-channel-blocks-rest-api
     * ServerBootstrap allows many client to connect via its channel. Therefore TCP has a dedicated ServerSocketChannel.
     * Bootstrap is used to create channels for single connections. Because UDP has one channel for all clients it makes sense that only the Bootstrap is required. All clients bind to the same channel.
     * @return the udp server
     */
    public Channel listen() {
        Channel channel = null;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new UDPServerInitializer());
        try {
            channel = b.bind(host, port).sync().channel(); // .sync().channel().closeFuture.await()
            logger.info("WS UDP server listening to port: " + port);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
        return channel;
    }

    /**
     * Builder class for the server
     */
    public static class UDPServerBuilder {
        private String host;
        private int port;

        private UDPServerBuilder() {}

        public static UDPServerBuilder newInstance() {
            return new UDPServerBuilder();
        }

        public UDPServerBuilder setHost(String host) {
            this.host = host;
            return this;
        }

        public UDPServerBuilder setPort(int port) {
            this.port = port;
            return this;
        }

        public UDPServer build() {
            return new UDPServer(this);
        }
    }
}
