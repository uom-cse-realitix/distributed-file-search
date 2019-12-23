package org.realitix.dfilesearch.filesearch.configuration;

/**
 * Configures the port information
 */
public class Client {

    private int port;
    private String host;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
