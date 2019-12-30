package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;

public class UDPServerInitializer extends ChannelInitializer<DatagramChannel> {
    @Override
    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
        datagramChannel.pipeline().addLast(new UDPServerHandler());
    }
}
