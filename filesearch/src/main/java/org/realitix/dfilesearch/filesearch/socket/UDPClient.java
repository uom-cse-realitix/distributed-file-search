package org.realitix.dfilesearch.filesearch.socket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
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
import org.realitix.dfilesearch.filesearch.beans.messages.CommonMessage;
import org.realitix.dfilesearch.filesearch.beans.messages.RegisterRequest;
import org.realitix.dfilesearch.filesearch.configuration.Client;

import java.io.IOException;
import java.io.InputStream;

public class UDPClient {

    private String host;
    private int port;
    private String username;
    private final Logger logger = Logger.getLogger(UDPClient.class);
    private static ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private EventLoopGroup workerGroup;
    private Client config;

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
    Channel run(String bootstrapIp, int bootstrapPort, String host, int port, String username) throws IOException {
//        this.config = readFromResources("config.yaml");
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
            channel = b.bind(port).sync().channel();
//            write(channel, (new RegisterRequest("0036", config.getHost(), config.getPort(), config.getUsername())), bootstrapIp, bootstrapPort);
            write(channel, (new RegisterRequest("0036", host, port, username)), bootstrapIp, bootstrapPort);
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
    private void write(Channel channel, CommonMessage message, String bootstrapIp, int bootstrapPort) throws InterruptedException {
        channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message.toString(), CharsetUtil.UTF_8), SocketUtils.socketAddress(bootstrapIp, bootstrapPort))).sync();
    }

    private Client readFromResources(String fileName) throws IOException {
        InputStream stream = this.getClass().getClassLoader().getResourceAsStream(fileName);
        Client client = mapper.readValue(stream, Client.class);
        return client;
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
