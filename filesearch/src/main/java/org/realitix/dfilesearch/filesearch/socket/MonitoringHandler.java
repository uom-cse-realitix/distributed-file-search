package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;

public class MonitoringHandler extends SimpleChannelInboundHandler<SocketChannel> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, SocketChannel socketChannel) throws Exception {

    }
}
