package org.realitix.dfilesearch.filesearch.util;

import io.netty.channel.Channel;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.FileSearchExecutor;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.beans.messages.JoinRequest;
import org.realitix.dfilesearch.filesearch.socket.UDPClient;

import java.util.HashMap;

public class RequestParserImpl implements RequestParser<String> {

    private Channel channel;
    private static final Logger logger = LogManager.getLogger(RequestParserImpl.class);

    public RequestParserImpl(Channel channel) {
        this.channel = channel;
    }

    @Override
    public String parse(String s) {
        String command = s.split(" ")[1];
        StringBuilder builder = new StringBuilder();
        if (command.equals("JOIN")) {       // check for other commands as well
            switch (command) {
                case "JOIN":
                    logger.info("JOIN message received");
                    try {
                        join(channel);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage());
                        Thread.currentThread().interrupt();
                    }
                    break;
                default:
                    builder.append("ERROR");
            }
        }
        return builder.toString();
    }

    private void join(Channel channel) throws InterruptedException {
        HashMap<Integer, Node> nodeMap = FileSearchExecutor.neighbourMap.getNodeMap();
        UDPClient udpClient = FileSearchExecutor.getUdpClient();
        int size = nodeMap.size();
        String preAmble = "NodeMap size is: ";
        switch (size) {
            case 0:
                logger.info(preAmble + size + ". Therefore, not calling JOIN.");
                break;
            case 1:
                logger.info(preAmble + size + ". Therefore, calling JOIN.");
                udpClient.write(channel, new JoinRequest(udpClient.getHost(), udpClient.getPort()), nodeMap.get(1).getIp(), nodeMap.get(1).getPort());
                break;
            case 2:
                logger.info(preAmble + size + ". Therefore, calling JOIN.");
                udpClient.write(channel, new JoinRequest(udpClient.getHost(), udpClient.getPort()), nodeMap.get(1).getIp(), nodeMap.get(1).getPort());
                udpClient.write(channel, new JoinRequest(udpClient.getHost(), udpClient.getPort()), nodeMap.get(2).getIp(), nodeMap.get(2).getPort());
                break;
            default:
                logger.error("Undefined JOIN");
        }
    }

}
