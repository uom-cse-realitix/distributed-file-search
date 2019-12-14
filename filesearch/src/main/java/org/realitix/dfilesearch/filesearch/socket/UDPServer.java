package org.realitix.dfilesearch.filesearch.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.log4j.Logger;

public class UDPServer {
    private static final int PORT = 9000;
    private final Logger logger = Logger.getLogger(UDPServer.class);

    /**
     * Runs a UDP server at port 9000.
     * TODO: Get the port number to an external config file.
     */
    public UDPServer run() {
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new UDPServerHandler());
        try {
            b.bind(PORT).sync().channel().closeFuture().await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }

        return this;
    }
}
