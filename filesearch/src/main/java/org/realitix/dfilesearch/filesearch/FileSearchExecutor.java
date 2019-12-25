package org.realitix.dfilesearch.filesearch;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.netty.channel.Channel;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;
import org.realitix.dfilesearch.filesearch.socket.UDPClient;
import org.realitix.dfilesearch.filesearch.socket.UDPServer;
import org.realitix.dfilesearch.filesearch.util.NodeMap;
import org.realitix.dfilesearch.webservice.beans.FileResponse;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class FileSearchExecutor extends Application<FileExecutorConfiguration> {

    public static NodeMap neighbourMap = new NodeMap();
    private static final Logger logger = LogManager.getLogger(FileSearchExecutor.class);

    public static void main(String[] args) throws Exception {
        new FileSearchExecutor().run(args);
    }
    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void initialize(Bootstrap<FileExecutorConfiguration> bootstrap) { }

    @Override
    public void run(FileExecutorConfiguration fileExecutorConfiguration, Environment environment) {
        BasicConfigurator.configure();
        registerBackendClient(environment, fileExecutorConfiguration);
        startWebService(environment, fileExecutorConfiguration);
        startUdpServer(environment, fileExecutorConfiguration);
    }

    private void startWebService(Environment environment, FileExecutorConfiguration configuration) {
        logger.info("Enabling Web Service..");
        environment.jersey().register(new FileSharingResource());
    }

    private Channel startUdpServer(Environment environment, FileExecutorConfiguration configuration) {
        UDPServer server = UDPServer.UDPServerBuilder
                .newInstance()
                .setHost(configuration.getUdpServer().getHost())
                .setPort(configuration.getUdpServer().getPort())
                .build();
        final Channel channel = server.listen();
        return channel;
    }

    private UDPClient registerBackendClient(Environment environment, FileExecutorConfiguration configuration) {
        UDPClient client = UDPClient.UDPClientBuilder
                .newInstance()
                .setHost(configuration.getPorts().getHost())
                .setPort(configuration.getPorts().getPort())
                .setUsername(configuration.getPorts().getUsername())
                .build();
        client.messageBootstrapServer(
                configuration.getBootstrapServer().getHost(),
                configuration.getBootstrapServer().getPort());
        return client;
    }

    @Path("/file")
    @Produces(MediaType.APPLICATION_JSON)
    public static class FileSharingResource {

        private final Logger logger = LogManager.getLogger(this.getClass());

        @GET
        @Path("{fileName}")
        public Response getFile(@PathParam("fileName") String fileName) {
            return Response.status(200).entity(synthesizeFile(fileName)).build();
        }

        private FileResponse synthesizeFile(String fileName){
            logger.info("Synthesizing the file");
            String randomString = fileName + RandomStringUtils.randomAlphabetic(20).toUpperCase();
            int size = (int) ((Math.random() * ((10 - 2) + 1)) + 2);    // change this to a more random algorithm
            FileResponse fileResponse = new FileResponse();
            fileResponse.setFileSize(size);
            fileResponse.setHash(DigestUtils.sha1Hex(randomString));
            logger.info("File synthesizing completed.");
            return fileResponse;
        }
    }
}
