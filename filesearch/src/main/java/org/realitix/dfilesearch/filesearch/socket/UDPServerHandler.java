package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;

import io.netty.channel.socket.DatagramPacket;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * Normally, a server extends ChannelInboundHandlerAdapter
 * In UDPServerHandler you still have to process the message,
 * and a write() operation, which is asynchronous, may not complete until after
 * channelRead() return. For this reason UDPServerHandler
 * flushes the message in channelReadComplete.
 */
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static final Logger logger = LogManager.getLogger(UDPServerHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Message received from peer: " + message);
        channelHandlerContext.write(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
    }
}
