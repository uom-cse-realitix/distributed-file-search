package org.realitix.dfilesearch.filesearch.socket;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.FileSearchExecutor;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.util.ResponseParser;
import org.realitix.dfilesearch.filesearch.util.ResponseParserImpl;

/**
 * Handler for Client.
 * This handler is not stateful.
 * The order of events is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);
    private final ResponseParser<String> responseParser;
    private Channel channel;

    private enum REQUEST_TYPE {JOIN, LEAVE, SER}

    public UDPClientHandler(Channel channel) {
        this.channel = channel;
        responseParser = new ResponseParserImpl();
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
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Connection inactive..");
    }

    private void processResponse(String string) {
        responseParser.parse(string);
    }

    /**
     * Executes WHEN A MESSAGE IS RECEIVED.
     *
     * @param request join request
     * @param ctx     handler context of the channel
     */
    private void processRequest(String request, ChannelHandlerContext ctx) {
        String command = request.split(" ")[1];
        switch (command) {
            case "JOIN":
                logger.info("JOIN MESSAGE RECEIVED.");
                parseLeaveAndJoin(request, ctx, REQUEST_TYPE.JOIN);
                break;
            case "LEAVE":
                logger.info("LEAVE MESSAGE RECEIVED.");
                parseLeaveAndJoin(request, ctx, REQUEST_TYPE.LEAVE);
                break;
            case "SER":
                logger.info("SER MESSAGE RECEIVED.");
                parseSer(request, ctx, REQUEST_TYPE.SER);
                break;
        }
    }

    private void parseLeaveAndJoin(String request, ChannelHandlerContext ctx, REQUEST_TYPE type) {
        String response = null;
        int actualLength = request.length();
        String[] split = request.split(" ");
        int length = Integer.parseInt(split[0]);
        final String ip = split[2];
        final int port = Integer.parseInt(split[3]);
        if (type.equals(REQUEST_TYPE.JOIN)) {
            response = "0014 JOINOK 0";
            if (length == actualLength) {
                Node node = new Node(ip, port);
                FileSearchExecutor.joinMap.add(node);
                logger.info("Node: " + node + " added to the joinMap.");
            } else response = "0016 JOINOK 9999";
        } else if (type.equals(REQUEST_TYPE.LEAVE)) {
            response = "0017 LEAVEOK 9999";
            if (length == actualLength) {
                FileSearchExecutor.joinMap.removeIf(node -> (node.getIp().equals(ip)) && (node.getPort() == port));
                logger.info("Node removed");
            } else response = "0017 LEAVEOK 9999";
        }
        assert response != null;
        ctx.channel().writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(response, CharsetUtil.UTF_8),
                SocketUtils.socketAddress(split[2], Integer.parseInt(split[3]))));
    }

    /**
     * Parses the SER request for a file
     * If the file is found, this host should respond with its socket credentials.
     * @param request request string
     * @param ctx channel context
     * @param type type of the request
     */
    private void parseSer(String request, ChannelHandlerContext ctx, REQUEST_TYPE type) {
        // parse SER request

    }
}
