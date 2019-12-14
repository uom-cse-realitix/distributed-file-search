package org.realitix.dfilesearch.filesearch;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.realitix.dfilesearch.filesearch.configuration.FileExecutorConfiguration;
import org.realitix.dfilesearch.filesearch.util.NodeMap;
import org.realitix.dfilesearch.webservice.WebServiceExecutor;

public class FileSearchExecutor extends Application<FileExecutorConfiguration> {

    public static NodeMap nodeMap = new NodeMap();

    public static void main(String[] args) throws Exception {
        new FileSearchExecutor().run(args);
    }
    @Override
    public String getName() {
        return super.getName();
    }

    @Override
    public void initialize(Bootstrap<FileExecutorConfiguration> bootstrap) {
        super.initialize(bootstrap);

    }

    @Override
    public void run(FileExecutorConfiguration fileExecutorConfiguration, Environment environment)
            throws Exception {
        WebServiceExecutor.runServer();
    }

}
