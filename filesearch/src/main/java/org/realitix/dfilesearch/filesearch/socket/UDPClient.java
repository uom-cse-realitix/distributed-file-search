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
import org.realitix.dfilesearch.filesearch.messages.IMessage;
import org.realitix.dfilesearch.filesearch.messages.Register;

public class UDPClient {

    private String host;
    private int port;
    private String username;
    private final Logger logger = Logger.getLogger(UDPClient.class);
    private EventLoopGroup workerGroup;

    public UDPClient(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.workerGroup = new NioEventLoopGroup();
    }

    /**
     * Runs the client socket
     * @param bootstrapIp server host IP
     * @param bootstrapPort server port
     * @return channel connecting the client and the server
     * TODO: After the initial handshakes and housekeeping, the client should connect to other peers (for file sharing). Thus, "host" and "port" should resemble those of that peers. This can be facilitated by giving some sort of a map. Think about it. Or, we should close the connection with the BS and initiate another connection with the peers after the initial handshakes.
     */
    Channel run(String bootstrapIp, int bootstrapPort) {
        Channel channel = null;
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
            channel = b.bind(this.port).sync().channel();
            write(channel, (new Register("0036", "127.0.0.1", 5001, "1234abcd")), bootstrapIp, bootstrapPort);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } finally {
//            group.shutdownGracefully(); TODO: Shutdown gracefully. If you shutdown here, the socket will be closed. Shutdown upon errors or by the discretion of the server.
        }
        return channel;
    }

    /**
     * Write different messages
     * @param channel channel between the client and server
     * @param message transmitted message
     * @param bootstrapIp IP of bootstrap server
     * @param bootstrapPort IP of bootstrap server
     * @throws InterruptedException
     */
    private void write(Channel channel, IMessage message, String bootstrapIp, int bootstrapPort) throws InterruptedException {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8), SocketUtils.socketAddress(bootstrapIp, bootstrapPort))).sync();
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

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
