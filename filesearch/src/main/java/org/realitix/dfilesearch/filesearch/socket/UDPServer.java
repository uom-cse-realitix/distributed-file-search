package org.realitix.dfilesearch.filesearch.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.log4j.Logger;

/**
 * Runs a UDP server at port specified in the constructor.
 */
public class UDPServer {
    private final Logger logger = Logger.getLogger(UDPServer.class);
    private String host;
    private int port;

    public UDPServer(UDPServerBuilder builder) {
        this.host = builder.host;
        this.port = builder.port;
    }

    public UDPServer run() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new UDPServerHandler());
        try {
            b.bind(host, port).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
        return this;
    }

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
