package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.net.DatagramPacket;

public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        // Parse the join request first, and respond accordingly.
    }
}
