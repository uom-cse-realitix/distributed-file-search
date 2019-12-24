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

    private NodeConfiguration ports;

    private NodeConfiguration bootstrapServer;

    private UDPServerConfiguration udpServer;

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    public NodeConfiguration getPorts() {
        return ports;
    }

    public void setPorts(NodeConfiguration ports) {
        this.ports = ports;
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
}
