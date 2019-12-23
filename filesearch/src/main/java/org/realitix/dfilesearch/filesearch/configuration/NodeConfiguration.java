package org.realitix.dfilesearch.filesearch.configuration;

import javax.annotation.Nullable;

/**
 * Configures the port information
 */
public class NodeConfiguration {

    private int port;
    private String host;
    @Nullable
    private String username;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    public void setUsername(@Nullable String username) {
        this.username = username;
    }
}
