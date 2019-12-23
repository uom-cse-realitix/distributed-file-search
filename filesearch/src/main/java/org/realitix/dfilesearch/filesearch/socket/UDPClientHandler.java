package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.Arrays;

/**
 * Handler for Client.
 * Read the docs: https://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-19
 * The pipeline is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("Channel Active!");
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Response message: " + message);
        processResponse(message);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
       logger.info("Connection inactive..");
    }

    private void processResponse(String string) {
        String[] splitResponse = string.split(" ");
        if (splitResponse[1].equals("REGOK")) {
            logger.info("ACK for REG received.");
        } else {
            logger.error("Undetermined response from the server.");
        }
    }
}
