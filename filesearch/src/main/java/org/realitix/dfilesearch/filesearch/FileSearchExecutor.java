package org.realitix.dfilesearch.filesearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.netty.channel.Channel;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.beans.Node;
import org.realitix.dfilesearch.filesearch.beans.UserInterfaceBean;
import org.realitix.dfilesearch.filesearch.beans.messages.JoinRequest;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;
import org.realitix.dfilesearch.filesearch.socket.UDPClient;
import org.realitix.dfilesearch.filesearch.util.NodeMap;
import org.realitix.dfilesearch.webservice.beans.FileResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class FileSearchExecutor extends Application<FileExecutorConfiguration> {

    private static UDPClient udpClient;
    private static Channel udpChannel;
    private FileExecutorConfiguration configuration;
    public static final NodeMap neighbourMap = new NodeMap();
    public static final List<Node> joinMap = new ArrayList<>();
    private static final Logger logger = LogManager.getLogger(FileSearchExecutor.class);

    public static void main(String[] args) throws Exception {
        new FileSearchExecutor().run(args);
    }

    @Override
    public void initialize(Bootstrap<FileExecutorConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/" , "index.html"));
        bootstrap.addBundle(new ViewBundle<FileExecutorConfiguration>());
    }

    @Override
    public void run(FileExecutorConfiguration fileExecutorConfiguration, Environment environment) {
        BasicConfigurator.configure();
        configuration = fileExecutorConfiguration;
        registerBackendClient(fileExecutorConfiguration);
        startWebService(environment);
    }

    private void startWebService(Environment environment) {
        logger.info("Enabling Web Service..");
        environment.jersey().setUrlPattern("/api/*");
        environment.jersey().register(new FileSharingResource());
    }

    private static UDPClient registerBackendClient(FileExecutorConfiguration configuration) {
        final UDPClient client = UDPClient.UDPClientBuilder.newInstance()
                .setHost(configuration.getClient().getHost())
                .setPort(configuration.getClient().getPort())
                .setUsername(configuration.getClient().getUsername())
                .build(configuration);
        try {
            udpChannel = client.register(configuration.getBootstrapServer().getHost(),
                    configuration.getBootstrapServer().getPort()).sync().await().channel();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
        udpClient = client;
        return client;
    }

    public static UDPClient getUdpClient() {
        return udpClient;
    }

    public FileExecutorConfiguration getConfiguration() {
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

        private FileResponse synthesizeFile(String fileName){
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

        private void processCommands(String command) throws InterruptedException {
            String cmd = command.split(" ")[1];
            switch (cmd) {
                case "JOIN":
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
                    }
                    break;
                case "REG":
                    FileExecutorConfiguration fseConfig = FileSearchExecutor.this.configuration;
                    FileSearchExecutor
                            .getUdpClient()
                            .register(fseConfig.getBootstrapServer().getHost(), fseConfig.getBootstrapServer().getPort());
                    break;
                default:
                    logger.error("Unknown command from the UI.");
            }
        }
    }
}
