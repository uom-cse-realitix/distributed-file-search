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
 * Read the docs: https://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-19
 * The pipeline is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);
    private final ResponseParser<String> responseParser;
    private Channel channel;

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
        switch (command) {
            case "JOIN":
                logger.info("JOIN MESSAGE RECEIVED.");
                parseJoin(request, ctx);
                break;
            case "LEAVE":
                logger.info("LEAVE MESSAGE RECEIVED.");
                break;
            case "SER":
                logger.info("SER MESSAGE RECEIVED.");
                break;
            default:
                logger.error("Message passed to response handler.");
        }
    }

    /**
     * Parses the join message and returns a response
     * SHOULD HAVE THE JOIN MESSAGE AS A PARAMETER, AND PARSE IT FOR CORRECTION.
     */
    private void parseJoin(String request, ChannelHandlerContext ctx) {
        String response = "0016 JOINOK 9999";        // ref counted string.
        int actualLength = request.length();
        String[] split = request.split(" ");
        int length = Integer.parseInt(request.split(" ")[0]);
        if (actualLength == length){
            response = "0014 JOINOK 0";
            String ip = split[2];
            int port = Integer.parseInt(split[3]);
            Node joiningNode = new Node(ip, port);
            FileSearchExecutor.joinMap.add(joiningNode);
            logger.info("Node: " + joiningNode + " added to the joinMap.");
        }
        ctx.channel().writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(response, CharsetUtil.UTF_8),
                SocketUtils.socketAddress(split[2], Integer.parseInt(split[3]))));
    }

}
