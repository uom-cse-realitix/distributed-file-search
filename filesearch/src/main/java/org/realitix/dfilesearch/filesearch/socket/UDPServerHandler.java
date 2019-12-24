package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import io.netty.channel.socket.DatagramPacket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(UDPServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Received from peer: " + message);
        channelHandlerContext.writeAndFlush(message);
    }
}
