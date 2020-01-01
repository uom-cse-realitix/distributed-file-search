package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.FileSearchExecutor;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.beans.messages.JoinRequest;
import org.realitix.dfilesearch.filesearch.util.ResponseParser;
import org.realitix.dfilesearch.filesearch.util.ResponseParserImpl;

import java.util.HashMap;

/**
 * Handler for Client.
 * This handler is not stateful.
 * Read the docs: https://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-19
 * The pipeline is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);
    private static final ResponseParser<String> responseParser = new ResponseParserImpl();
    private Channel channel;

    public UDPClientHandler(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel Active!");
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Response message: " + message);
        processResponse(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        join(channel);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       logger.info("Connection inactive..");
    }

    private void processResponse(String string) {
        responseParser.parse(string);
    }

    private void join(Channel channel) throws InterruptedException {
        HashMap<Integer, Node> nodeMap = FileSearchExecutor.neighbourMap.getNodeMap();
        UDPClient udpClient = FileSearchExecutor.getUdpClient();
        int size = nodeMap.size();
        String preAmble = "NodeMap size is: ";
        switch (size) {
            case 0:
                logger.info(preAmble + size + ". Therefore, not calling JOIN.");
                break;
            case 1:
                logger.info(preAmble + size + ". Therefore, calling JOIN.");
                udpClient.write(channel, new JoinRequest(udpClient.getHost(), udpClient.getPort()), nodeMap.get(1).getIp(), nodeMap.get(1).getPort());
                break;
            case 2:
                logger.info(preAmble + size + ". Therefore, calling JOIN.");
                udpClient.write(channel, new JoinRequest(udpClient.getHost(), udpClient.getPort()), nodeMap.get(2).getIp(), nodeMap.get(2).getPort());
                break;
            default:
                logger.error("Undefined JOIN");
        }

    }

}
