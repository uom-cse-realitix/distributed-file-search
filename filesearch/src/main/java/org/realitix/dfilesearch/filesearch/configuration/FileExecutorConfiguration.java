package org.realitix.dfilesearch.filesearch.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;

/**
 * Configuration for FileSearchExecutor
 * Strictly adhere to the guidelines given in docs: https://www.dropwizard.io/en/stable/getting-started.html
 */
public class FileExecutorConfiguration extends Configuration {

    @NotEmpty
    private String name;

    private NodeConfiguration client;

    private NodeConfiguration bootstrapServer;

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    public NodeConfiguration getClient() {
        return client;
    }

    public void setClient(NodeConfiguration client) {
        this.client = client;
    }

    public NodeConfiguration getBootstrapServer() {
        return bootstrapServer;
    }

    public void setBootstrapServer(NodeConfiguration bootstrapServer) {
        this.bootstrapServer = bootstrapServer;
    }

}
