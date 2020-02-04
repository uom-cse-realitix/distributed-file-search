package org.realitix.dfilesearch.filesearch.socket;

import io.netty.buffer.Unpooled;
import io.netty.channel.*;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.FileSearchExecutor;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.util.RequestHasher;
import org.realitix.dfilesearch.filesearch.util.ResponseParser;
import org.realitix.dfilesearch.filesearch.util.ResponseParserImpl;

import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Handler for Client.
 * This handler is not stateful.
 * The order of events is channelRegistered -> channelActive -> channelInactive -> channelUnregistered
 */
public class UDPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private static Logger logger = LogManager.getLogger(UDPClientHandler.class);
    private final ResponseParser<String> responseParser;
    private Channel channel;

    private enum REQUEST_TYPE {JOIN, LEAVE, SER}

    public UDPClientHandler(Channel channel) {
        this.channel = channel;
        responseParser = new ResponseParserImpl();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("Channel Active!");
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) {
        String message = datagramPacket.content().toString(CharsetUtil.UTF_8);
        logger.info("Response message: " + message);
        processResponse(message);
        processRequest(message, channelHandlerContext);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage());
        ctx.channel().close();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("Connection inactive..");
    }

    private void processResponse(String string) {
        responseParser.parse(string);
    }

    /**
     * Executes WHEN A MESSAGE IS RECEIVED.
     *
     * @param request join request
     * @param ctx     handler context of the channel
     */
    private void processRequest(String request, ChannelHandlerContext ctx) {
        String command = request.split(" ")[1];
        switch (command) {
            case "JOIN":
                logger.info("JOIN MESSAGE RECEIVED.");
                parseLeaveAndJoin(request, ctx, REQUEST_TYPE.JOIN);
                break;
            case "LEAVE":
                logger.info("LEAVE MESSAGE RECEIVED.");
                parseLeaveAndJoin(request, ctx, REQUEST_TYPE.LEAVE);
                break;
            case "SER":
                logger.info("SER MESSAGE RECEIVED.");
                parseSer(request, ctx, REQUEST_TYPE.SER);
                break;
        }
    }

    /**
     * When a JOIN message arrives, processes it, puts the corresponding node into a joinMap, and sends the response
     * @param request sent by the node
     * @param ctx context
     * @param type type of request
     */
    private void parseLeaveAndJoin(String request, ChannelHandlerContext ctx, REQUEST_TYPE type) {
        String response = null;
        int actualLength = request.length();
        String[] split = request.split(" ");
        int length = Integer.parseInt(split[0]);
        final String ip = split[2];
        final int port = Integer.parseInt(split[3]);
        if (type.equals(REQUEST_TYPE.JOIN)) {
            response = "0014 JOINOK 0";
            if (length == actualLength) {
                Node node = new Node(ip, port);
                FileSearchExecutor.getJoinMap().add(node);
                logger.info("Node: " + node + " added to the joinMap.");
            } else response = "0016 JOINOK 9999";
        } else if (type.equals(REQUEST_TYPE.LEAVE)) {
            response = "0017 LEAVEOK 9999";
            if (length == actualLength) {
                FileSearchExecutor.getJoinMap().removeIf(node -> (node.getIp().equals(ip)) && (node.getPort() == port));
                logger.info("Node removed");
            } else response = "0017 LEAVEOK 9999";
        }
        assert response != null;
        write(ctx, response, split[2], split[3]);
    }

    /**
     * Write to the socket
     * @param ctx channel handler context
     * @param message message to be written from the socket
     * @param host destination IP
     * @param port destination port
     */
    private void write(ChannelHandlerContext ctx, String message, String host, String port) {
        ctx.channel().writeAndFlush(
                new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                        SocketUtils.socketAddress(host, Integer.parseInt(port))));
    }

    private void write(ChannelHandlerContext ctx, String message, String host, int port) {
        ctx.channel().writeAndFlush(
                new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                        SocketUtils.socketAddress(host, port)));
    }


    /**
     * Parses the SER request for a file'
     * e.g. length SER IP port file_name hops
     * If the file is found, this host should respond with its socket credentials.
     * Use JoinMap instead of NeighborMap, to query the responded nodes. Nodes responded by the bootstrap server (captured in NeighborMap) might not be available.
     * split[2]: host IP, split[3]: host port, split[4]: file name
     * When the file is found, the node having the file responds with its IP and file names it has stored.
     * @param request request string
     * @param ctx channel context
     * @param type type of the request
     */
    private void parseSer(final String request, final ChannelHandlerContext ctx, REQUEST_TYPE type) {
        // parse SER request
        List<Node> nodeMap = FileSearchExecutor.getJoinMap();
        List<String> fileList = FileSearchExecutor.getFileList();
        List<String> hashedRequests = FileSearchExecutor.getHashedRequests();
        String[] split = request.split(" ");
        int hops;
        if (split.length > 4) hops = Integer.parseInt(split[5]);
        else hops = FileSearchExecutor.getConfiguration().getHops();
        try {
            // checks whether the file is in its file system
            String hashedRequest = RequestHasher.hash(request);
            // checks whether the node has served the same request before
            if (!hashedRequests.contains(hashedRequest)) {
                hashedRequests.add(hashedRequest);
                // might have to store the response in order to get use of these hashed requests. Temporarily, we'll ignore.
            }
        } catch (NoSuchAlgorithmException e) {
            logger.error(e.getMessage());
        }
        final Pattern pattern = Pattern.compile(String.join("\\b",
                split[4], "\\b"));
        List<String> matchedFiles = fileList
                .stream()
                .filter(file -> pattern.matcher(file).matches())
                .collect(Collectors.toList());
        int fileCount = matchedFiles.size();    // no_of_files
        if (fileCount != 0) {
            UDPClient client = FileSearchExecutor.getUdpClient();
            write(
                    ctx,
                    synthesizeSerResponse(
                            client.getHost(),
                            client.getPort(),
                            fileCount,
                            hops,
                            matchedFiles
                    ),     // XXXX SEROK no_of_files IP port hops filename1 filename2
                    split[2],
                    split[3]
            ); // send the response
        }
         if (hops > 0) nodeMap.forEach(node -> write(ctx, propagateRequest(request), node.getIp(), node.getPort()));
    }

    /**
     * Synthesizes a response for the request. Only in case a file is found in this particular node.
     * @param hostIp IP of this node
     * @param port port of this node
     * @param noOfFiles number of files found w.r.t the regular pattern
     * @param hops number of hops indicated in the request
     * @param fileNames names of the matched files
     * @return the response to be sent to the asking node
     */
    private String synthesizeSerResponse(String hostIp, int port, int noOfFiles, int hops, List<String> fileNames) {
        String basicString = "SEROK" +
                " " +
                noOfFiles +
                " " +
                hostIp +
                " " +
                port +
                " " +
                hops +
                " " +
                StringUtils.join(fileNames, " ");
        int length = basicString.length() + 5;
        return length + " " + basicString;
    }

    /**
     * Decrements the hops and returns the new request
     * e.g. if the hops in the request is 5 (i.e. 5 more hops to be propagated), 5 - 1 = 4 hops are there after this node.
     * Should be called if the number of hops in the request is larger than 0.
     * @param request request which arrived at the node
     * @return new request to be propogated
     */
    private String propagateRequest(String request) {
        String[] split = request.split(" ");
        int hops = Integer.parseInt(split[5]);
        split[5] = Integer.toString(--hops);
        return Arrays.toString(split);
    }
}
