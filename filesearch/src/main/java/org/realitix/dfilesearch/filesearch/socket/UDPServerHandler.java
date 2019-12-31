package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import io.netty.channel.socket.DatagramPacket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.util.ResponseParser;
import org.realitix.dfilesearch.filesearch.util.ServerResponseParserImpl;

/**
 * Normally, a server extends ChannelInboundHandlerAdapter
 * In UDPServerHandler you still have to process the message,
 * and a write() operation, which is asynchronous, may not complete until after
 * channelRead() return. For this reason UDPServerHandler
 * flushes the message in channelReadComplete.
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(UDPServerHandler.class);
    private static final ResponseParser<String> responseParser = new ServerResponseParserImpl();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel Active!");
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        logger.info("Channel read!");
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Message received from peer: " + message);
        processMessage(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    public void processMessage(String message) {
        responseParser.parse(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
    }
}
