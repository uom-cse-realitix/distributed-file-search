package org.realitix.dfilesearch.filesearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.beans.UserInterfaceBean;
import org.realitix.dfilesearch.filesearch.beans.messages.CommonMessage;
import org.realitix.dfilesearch.filesearch.beans.messages.JoinRequest;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;
import org.realitix.dfilesearch.filesearch.socket.UDPClient;
import org.realitix.dfilesearch.filesearch.util.NodeMap;
import org.realitix.dfilesearch.webservice.beans.FileResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.Array;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FileSearchExecutor extends Application<FileExecutorConfiguration> {

    private static UDPClient udpClient;
    private static Channel udpChannel;
    private static FileExecutorConfiguration configuration;
    public static final NodeMap neighbourMap = new NodeMap();
    private static final List<Node> joinMap = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(FileSearchExecutor.class);
    private static List<String> fileList;
    private static List<String> hashedRequests = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new FileSearchExecutor().run(args);
    }

    @Override
    public void initialize(Bootstrap<FileExecutorConfiguration> bootstrap) {
        fileList = initializeFileList();
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        bootstrap.addBundle(new ViewBundle<FileExecutorConfiguration>());
    }

    @Override
    public void run(FileExecutorConfiguration fileExecutorConfiguration, Environment environment) {
        BasicConfigurator.configure();
        configuration = fileExecutorConfiguration;
        registerBackendClient(fileExecutorConfiguration);
        startWebService(environment);
        fileList.forEach(logger::info);
    }

    private void startWebService(Environment environment) {
        logger.info("Enabling Web Service..");
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new FileSharingResource());
    }

    private static void registerBackendClient(FileExecutorConfiguration configuration) {
        final UDPClient client = UDPClient.UDPClientBuilder.newInstance()
                .setHost(configuration.getClient().getHost())
                .setPort(configuration.getClient().getPort())
                .setUsername(configuration.getClient().getUsername())
                .build(configuration);
        try {
            udpChannel = client.register(configuration.getBootstrapServer().getHost(),
                    configuration.getBootstrapServer().getPort()).sync().await().channel();
//            client.join(udpChannel, neighbourMap, client.getHost(), client.getPort());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        udpClient = client;
    }

    private List<String> initializeFileList() {
        List<String> list = Arrays.asList(
                "Shadows",
                "Sapiens",
                "Deus",
                "Tamed",
                "Cosmos",
                "OutgrowingGod",
                "Habit",
                "CosmicConnection",
                "ThinkingFastAndSlow",
                "MansSearchForMeaning",
                "DragonsOfEden",
                "SurelyYoureJoking",
                "RedLimit",
                "FutureOfHumanity",
                "EinstinesCosmos",
                "ThirdWave",
                "SpinningMagnet",
                "BookOfUniverses",
                "12RulesForLife",
                "ConstantsOfNature",
                "PhysicsOfFuture",
                "TwoSidesOfTheMoon",
                "Money",
                "OutgrowingGod",
                "OnGovernment",
                "OnOriginsOfSpecies"
        );
        Collections.shuffle(list);
        return new ArrayList<>(new HashSet<>(list.subList(0, 5)));
    }

    public static void setConfiguration(FileExecutorConfiguration configuration) {
        FileSearchExecutor.configuration = configuration;
    }



    public static List<String> getHashedRequests() {
        return hashedRequests;
    }

    public static List<String> getFileList() {
        return fileList;
    }

    public static List<Node> getJoinMap() {
        return joinMap;
    }

    public static UDPClient getUdpClient() {
        return udpClient;
    }

    public static FileExecutorConfiguration getConfiguration() {
        return configuration;
    }

    /**************
     * Web Service
     *************
     */
    @Path("/file")
    @Produces(MediaType.APPLICATION_JSON)
    public class FileSharingResource {

        private final Logger logger = LogManager.getLogger(this.getClass());

        @GET
        @Path("{fileName}")
        public Response getFile(@PathParam("fileName") String fileName) {
            return Response.status(200).entity(synthesizeFile(fileName)).build();
        }

        @POST
        @Path("/command")
        @Consumes(MediaType.APPLICATION_JSON)
        public Response sendCommand(UserInterfaceBean command) {
            logger.info("Command: " + command);
            try {
                processCommands(command.toString());
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
            return Response.status(201).build();
        }

        @GET
        @Path("/join")
        public Response sendJoin() {
            join();
            return Response.status(201).build();
        }

            @GET
        @Path("/map")
        public Response getNodeMap() {
            Response r = null;
            try {
                r = Response.status(200)
                        .entity((new ObjectMapper())
                                .writeValueAsString(FileSearchExecutor.neighbourMap.getNodeMap()))
                        .build();
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
            return r;
        }

        @GET
        @Path("/fileList")
        public Response getFileList() {
            Response r = null;
            try {
                r = Response.status(200)
                        .entity((new ObjectMapper()).writeValueAsString(FileSearchExecutor.fileList))
                        .build();
            } catch (JsonProcessingException e) {
                logger.error(e.getMessage());
            }
            return r;
        }

        private void join() {
            try {
                HashMap<Integer, Node> nodeMap = FileSearchExecutor.neighbourMap.getNodeMap();
                logger.info("NodeMap: " +
                        Arrays.toString(FileSearchExecutor.neighbourMap.getNodeMap().entrySet().toArray()));
                if (nodeMap.size() != 0) {
                    for (int i = 1; i < nodeMap.size() + 1; i++) {
                        Node n1 = FileSearchExecutor.neighbourMap.getNodeMap().get(i);
                        logger.info("Sending JOIN to node: " + n1.getPort());
                        FileSearchExecutor.getUdpClient().write(
                                FileSearchExecutor.udpChannel,
                                new JoinRequest(
                                        FileSearchExecutor.getUdpClient().getHost(),
                                        FileSearchExecutor.getUdpClient().getPort()
                                ),
                                FileSearchExecutor.neighbourMap.getNodeMap().get(i).getIp(),
                                FileSearchExecutor.neighbourMap.getNodeMap().get(i).getPort()
                        );
                    }
                } else {
                    logger.info("First Node Joining.");
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        private FileResponse synthesizeFile(String fileName) {
            logger.info("Synthesizing the file");
            String randomString = fileName + RandomStringUtils.randomAlphabetic(20).toUpperCase();
            int size = (int) ((Math.random() * ((10 - 2) + 1)) + 2);    // change this to a more random algorithm
            FileResponse fileResponse = FileResponse.FileResponseBuilder
                    .newInstance()
                    .setFileSize(size)
                    .setHash(DigestUtils.sha1Hex(randomString))
                    .build();
            logger.info("File synthesizing completed.");
            return fileResponse;
        }

        public ChannelFuture write(Channel channel, String message, String remoteIp, int remotePort)
                throws InterruptedException {
            logger.info("Sending write to: " + remoteIp + ":" + remotePort);
            return channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                    SocketUtils.socketAddress(remoteIp, remotePort))).sync().await();
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
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < split.length; ++i) {
                builder.append(" ");
                builder.append(split[i]);
            }
            return builder.toString().substring(1);
        }

        /**
         * Sends JOIN, and REG commands.
         * Initiates SER command
         * Cognitive complexity of this method is high. Refactor it according to definitions (https://www.sonarsource.com/docs/CognitiveComplexity.pdf)
         * @param command the executing command
         * @throws InterruptedException thread interruptions
         */
        private void processCommands(String command) throws InterruptedException {
            String cmd = command.split(" ")[1];
            switch (cmd) {
                // JOIN message is sent to each node in neighbor map.
                case "JOIN":
                    join();
                    break;
                case "REG":
                    FileExecutorConfiguration fseConfig = FileSearchExecutor.configuration;
                    FileSearchExecutor
                            .getUdpClient()
                            .register(fseConfig.getBootstrapServer().getHost(), fseConfig.getBootstrapServer().getPort());
                    break;
                case "SER":
                    logger.info("SER command identified");
                    String[] split = command.split(" ");
                    int hops = 0;
                    if (split.length > 4) hops = Integer.parseInt(split[5]);
                    final Pattern pattern = Pattern.compile(String.join("\\b",
                            split[4], "\\b"));
                        List<String> matchedFiles = fileList
                                .stream()
                                .filter(file -> pattern.matcher(file).matches())
                                .collect(Collectors.toList());
                        int fileCount = matchedFiles.size();
                        if (fileCount != 0) {
                            logger.info("File matched!");
                            write(udpChannel, synthesizeSerResponse(
                                    udpClient.getHost(),
                                    udpClient.getPort(),    // this port needs to be the web port
                                    fileCount,
                                    0,
                                    matchedFiles),
                                    split[2],
                                    Integer.parseInt(split[3]));
                        }
                        final String propagateRequest = propagateRequest(command);
                        logger.info("Request propogate: " + propagateRequest);
                        if (hops > 0) {
                            joinMap.forEach(
                                    node -> {
                                        try {
                                            write(udpChannel, propagateRequest, node.getIp(), node.getPort());
                                        } catch (InterruptedException e) {
                                            logger.error(e.getMessage());
                                            Thread.currentThread().interrupt();
                                        }
                                    }
                            );
                            FileSearchExecutor.neighbourMap.getNodeMap()
                                    .forEach((id, node) -> {
                                        try {
                                            write(udpChannel, propagateRequest, node.getIp(), node.getPort());
                                        } catch (InterruptedException e) {
                                            logger.error(e.getMessage());
                                            Thread.currentThread().interrupt();
                                        }
                                    });
                        }
                    break;
                default:
                    logger.error("Unknown command from the UI.");
            }
        }
    }
}
