package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Handler for Client.
 * Read the docs: https://netty.io/wiki/new-and-noteworthy-in-4.0.html#wiki-h4-19
 * The pipeline is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel Active!");
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Response message: " + message);
        processResponse(message);
        channelHandlerContext.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       logger.info("Connection inactive..");
    }

    private void processResponse(String string) {
        String[] splitResponse = string.split(" ");
        switch (splitResponse[1]) {
            case "REGOK":
                logger.info("ACK for REG received.");
                break;
            case "UNROK":
                logger.info("Response FOR UNROK received.");
                break;
            default:
                logger.error("Undetermined response from the server.");
        }
    }

}
