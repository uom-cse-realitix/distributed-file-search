package org.realitix.dfilesearch.filesearch.socket;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.util.RequestParser;
import org.realitix.dfilesearch.filesearch.util.RequestParserImpl;
import org.realitix.dfilesearch.filesearch.util.ResponseParser;
import org.realitix.dfilesearch.filesearch.util.ResponseParserImpl;

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Handler for Client.
 * This handler is not stateful.
 * Read the docs: https://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-19
 * The pipeline is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);
    private final ResponseParser<String> responseParser;
    private final RequestParser<String> requestParser;
    private Channel channel;

    public UDPClientHandler(Channel channel) {
        this.channel = channel;
        responseParser = new ResponseParserImpl();
        requestParser = new RequestParserImpl(channel);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel Active!");
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Response message: " + message);
        processResponse(message);
        processRequest(message, channelHandlerContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception { }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       logger.info("Connection inactive..");
    }

    private void processResponse(String string) {
        responseParser.parse(string);
    }

    /**
     * Executes WHEN A MESSAGE IS RECEIVED.
     * @param request join request
     * @param ctx handler context of the channel
     */
    private void processRequest(String request, ChannelHandlerContext ctx) {
        String command = request.split(" ")[1];
        String[] split = request.split(" ");
        switch (command) {
            case "JOIN":
                logger.info("JOIN MESSAGE RECEIVED.");
                ctx.channel().writeAndFlush(new DatagramPacket(
                        Unpooled.copiedBuffer(parseJoin(request), CharsetUtil.UTF_8),
                        SocketUtils.socketAddress(split[2], Integer.parseInt(split[3]))));
                break;
            case "LEAVE":
                logger.info("LEAVE MESSAGE RECEIVED.");
                break;
            case "SER":
                logger.info("SER MESSAGE RECEIVED.");
                break;
            default:
                logger.error("Undetermined Request Message");
        }
    }

    /**
     * Parses the join message and returns a response
     * SHOULD HAVE THE JOIN MESSAGE AS A PARAMETER, AND PARSE IT FOR CORRECTION.
     * @return response for JOIN
     */
    private String parseJoin(String request) {
        int actualLength = request.length();
        int length = Integer.parseInt(request.split(" ")[0]);
        String response;
        if (actualLength == length) response = "0014 JOINOK 0";
        else response = "0016 JOINOK 9999";
        return response;
    }

}
