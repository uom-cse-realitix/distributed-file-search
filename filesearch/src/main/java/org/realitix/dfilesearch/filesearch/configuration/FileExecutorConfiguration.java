package org.realitix.dfilesearch.filesearch.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for FileSearchExecutor
 * Strictly adhere to the guidelines given in docs: https://www.dropwizard.io/en/stable/getting-started.html
 */
public class FileExecutorConfiguration extends Configuration {

    @NotEmpty
    private String name;

    private NodeConfiguration client;

    private NodeConfiguration bootstrapServer;

    private UDPServerConfiguration udpServer;

    private List<NodeConfiguration> clientsToNeighbours;

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

    public UDPServerConfiguration getUdpServer() {
        return udpServer;
    }

    public void setUdpServer(UDPServerConfiguration udpServer) {
        this.udpServer = udpServer;
    }

    public List<NodeConfiguration> getClientsToNeighbours() {
        return clientsToNeighbours;
    }

    public void setClientsToNeighbours(List<NodeConfiguration> clientsToNeighbours) {
        this.clientsToNeighbours = clientsToNeighbours;
    }
}
