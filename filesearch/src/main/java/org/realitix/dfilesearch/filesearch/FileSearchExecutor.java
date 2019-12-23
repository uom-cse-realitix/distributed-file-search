package org.realitix.dfilesearch.filesearch;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;
import org.realitix.dfilesearch.filesearch.resources.FileSharingResource;
import org.realitix.dfilesearch.filesearch.socket.UDPClient;
import org.realitix.dfilesearch.filesearch.util.NodeMap;

public class FileSearchExecutor extends Application<FileExecutorConfiguration> {

    public static NodeMap nodeMap = new NodeMap();
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
        environment.jersey().register(new FileSharingResource());
        UDPClient client = new UDPClient(fileExecutorConfiguration.getPorts().getHost() , fileExecutorConfiguration.getPorts().getPort() , fileExecutorConfiguration.getPorts().getUsername());
        client.messageBootstrapServer(fileExecutorConfiguration.getBootstrapServer().getHost(), fileExecutorConfiguration.getBootstrapServer().getPort());
    }
}
