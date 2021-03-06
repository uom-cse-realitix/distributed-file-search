package org.realitix.dfilesearch.filesearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.SimpleServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.beans.UserInterfaceBean;
import org.realitix.dfilesearch.filesearch.beans.messages.JoinRequest;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;
import org.realitix.dfilesearch.filesearch.socket.MonitoringInitializer;
import org.realitix.dfilesearch.filesearch.socket.UDPClient;
import org.realitix.dfilesearch.filesearch.util.NodeMap;
import org.realitix.dfilesearch.webservice.beans.FileResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        bootstrap.addBundle(new AssetsBundle("/assets/", "/", "index.html"));
        bootstrap.addBundle(new ViewBundle<FileExecutorConfiguration>());
    }

    @Override
    public void run(FileExecutorConfiguration fileExecutorConfiguration, Environment environment) {
        configuration = fileExecutorConfiguration;
        fileList = initializeFileList(fileExecutorConfiguration);
        registerBackendClient(fileExecutorConfiguration);
        startWebService(environment);
        fileList.forEach(logger::info);
        setWebPort(fileExecutorConfiguration, environment);
        try {
            Channel monitoringClient = registerMonitoringClient(fileExecutorConfiguration);
            initiateHeartBeat(monitoringClient);
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sets web port of the configuration. This is sent to the nodes in SEROK.
     *
     * @param fileExecutorConfiguration configuration bean
     * @param environment               dropwizard environment bean
     */
    private void setWebPort(FileExecutorConfiguration fileExecutorConfiguration, Environment environment) {
        environment.lifecycle().addServerLifecycleListener(server -> {
            Stream<ConnectorFactory> connectors = configuration.getServerFactory() instanceof DefaultServerFactory
                    ? ((DefaultServerFactory) fileExecutorConfiguration.getServerFactory()).getApplicationConnectors()
                    .stream() : Stream.of((SimpleServerFactory) fileExecutorConfiguration.getServerFactory())
                    .map(SimpleServerFactory::getConnector);
            int port = connectors
                    .filter(connector -> connector.getClass().isAssignableFrom(HttpConnectorFactory.class))
                    .map(connector -> (HttpConnectorFactory) connector)
                    .mapToInt(HttpConnectorFactory::getPort)
                    .findFirst()
                    .orElseThrow(IllegalStateException::new);
            configuration.setWebPort(port);
            logger.info(String.format("Web Port set to: %d", port));
        });
    }

    private void startWebService(Environment environment) {
        logger.info("Enabling Web Service..");
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new FileSharingResource());
    }

    /**
     * TODO: Check why the websocket client doesn't connect to nodejs monitoring server
     * @param configuration configuration for dropwizard application
     * @return socket channel
     * @throws InterruptedException interrupt
     */
    private Channel registerMonitoringClient(FileExecutorConfiguration configuration) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        io.netty.bootstrap.Bootstrap b = new io.netty.bootstrap.Bootstrap();
        b.group(workerGroup);
        b.channel(NioSocketChannel.class);
        b.option(ChannelOption.SO_KEEPALIVE, true);
        b.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new MonitoringInitializer());
            }
        });
        ChannelFuture f = b.connect(configuration.getMonitoringConf().getHost(),
                configuration.getMonitoringConf().getPort()).sync();
        return f.channel();
    }

    private void initiateHeartBeat(Channel heartBeatChannel) {
        heartBeatChannel.eventLoop().scheduleAtFixedRate((Runnable) () ->
                heartBeatChannel.writeAndFlush("PING"), 60, 60,  TimeUnit.MILLISECONDS);
    }

    private static void registerBackendClient(FileExecutorConfiguration configuration) {
        final UDPClient client = UDPClient
                .UDPClientBuilder
                .newInstance()
                .setHost(configuration.getClient().getHost())
                .setPort(configuration.getClient().getPort())
                .setUsername(configuration.getClient().getUsername())
                .build(configuration);
        try {
            udpChannel = client
                    .register(configuration.getBootstrapServer().getHost(), configuration.getBootstrapServer().getPort())
                    .sync()
                    .await()
                    .channel();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        udpClient = client;
    }

    private List<String> initializeFileList(FileExecutorConfiguration configuration) {
        List<String> list = configuration.getClient().getFiles();
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
    public static class FileSharingResource {

        private final Logger logger = LogManager.getLogger(this.getClass());

        @GET
        @Path("{fileName}")
        public Response getFile(@PathParam("fileName") String fileName) {
            return Response.status(200).entity(synthesizeFile(fileName)).build();
        }


        @GET
        @Path("{fileName}/download")
        public Response download(@PathParam("fileName") String fileName) {
            try {
                return Response.ok(sendFile(fileName), MediaType.APPLICATION_OCTET_STREAM)
                        .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                        .build();
            } catch (IOException e) {
                Thread.currentThread().interrupt();
            }
            return Response.accepted().build();
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
                ArrayList<Node> nodes = new ArrayList<>(FileSearchExecutor.neighbourMap.getNodeMap().values());
                nodes.addAll(joinMap);
                r = Response.status(200)
                        .entity((new ObjectMapper())
                                .writeValueAsString(nodes))
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

        private File sendFile(String fileName) throws IOException {
            logger.info("Synthesizing the file");
            String randomString = fileName + RandomStringUtils.randomAlphabetic(20).toUpperCase();
            File file = new File(fileName);
            if (file.createNewFile()) {
                FileWriter writer = new FileWriter(file, true);
                writer.write(randomString);
                writer.close();
                return file;
            } else return null;
        }

        public ChannelFuture write(Channel channel, String message, String remoteIp, int remotePort)
                throws InterruptedException {
            logger.info("Sending write to: " + remoteIp + ":" + remotePort);
            return channel.writeAndFlush(new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                    SocketUtils.socketAddress(remoteIp, remotePort))).sync().await();
        }

        /**
         * Synthesizes a response for the request. Only in case a file is found in this particular node.
         *
         * @param hostIp    IP of this node
         * @param port      port of this node
         * @param noOfFiles number of files found w.r.t the regular pattern
         * @param hops      number of hops indicated in the request
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
            return "00" + length + " " + basicString;
        }

        /**
         * Decrements the hops and returns the new request
         * e.g. if the hops in the request is 5 (i.e. 5 more hops to be propagated), 5 - 1 = 4 hops are there after this node.
         * Should be called if the number of hops in the request is larger than 0.
         *
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
         *
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
                                udpClient.getConfiguration().getWebPort(),    // this port needs to be the web port
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
