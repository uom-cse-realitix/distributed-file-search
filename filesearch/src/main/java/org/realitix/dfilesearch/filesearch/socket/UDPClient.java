package org.realitix.dfilesearch.filesearch.socket;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.apache.log4j.Logger;

public class UDPClient {

    private final Logger logger = Logger.getLogger(UDPClient.class);
    private EventLoopGroup group = new NioEventLoopGroup();

    /**
     * Runs the client socket
     * @param host server host IP
     * @param port server port
     * @return channel connecting the client and the server
     * TODO: After the initial handshakes and housekeeping, the client should connect to other peers (for file sharing). Thus, "host" and "port" should resemble those of that peers. This can be facilitated by giving some sort of a map. Think about it. Or, we should close the connection with the BS and initiate another connection with the peers after the initial handshakes.
     */
    public Channel run(String host, int port) {
        Channel channel = null;
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioDatagramChannel.class)
                .option(ChannelOption.SO_BROADCAST, true)
                .handler(new ChannelInitializer<DatagramChannel>() {
                    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
                        datagramChannel.pipeline().addLast(new UDPClientHandler());
                    }
                });
        try {
        channel = b.connect(host, port).sync().channel();   // connects to the server.
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
            group.shutdownGracefully();
        }
        return channel;
    }
}
