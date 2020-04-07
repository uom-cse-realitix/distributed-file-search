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

    private int hops;

    private MonitoringConfiguration monitoringConf;

    @JsonProperty(required = false)
    private int webPort;

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

    public int getHops() {
        return hops;
    }

    public void setHops(int hops) {
        this.hops = hops;
    }

    public int getWebPort() {
        return webPort;
    }

    public void setWebPort(int webPort) {
        this.webPort = webPort;
    }

    public MonitoringConfiguration getMonitoringConf() {
        return monitoringConf;
    }

    public void setMonitoringConf(MonitoringConfiguration monitoringConf) {
        this.monitoringConf = monitoringConf;
    }
}
