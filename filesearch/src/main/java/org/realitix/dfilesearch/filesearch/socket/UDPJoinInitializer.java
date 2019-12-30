package org.realitix.dfilesearch.filesearch.socket;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.DatagramChannel;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;

public class UDPJoinInitializer extends ChannelInitializer<DatagramChannel> {

    private FileExecutorConfiguration configuration;

    public UDPJoinInitializer(FileExecutorConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected void initChannel(DatagramChannel datagramChannel) throws Exception {
        datagramChannel.pipeline().addLast(new UDPJoinHandler());
    }
}
