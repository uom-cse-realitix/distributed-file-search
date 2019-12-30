package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

@ChannelHandler.Sharable
public class UDPJoinHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(UDPJoinHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        logger.info("JOIN CALLED!");
    }
}
