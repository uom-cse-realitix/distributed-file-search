package org.realitix.dfilesearch.filesearch.beans.messages;

public class RegisterRequest implements CommonMessage {

    private String length;
    private final String key = "REG";
    private String ip;
    private int port;
    private String username;

    public RegisterRequest(String ip, int port, String username) {
        this.ip = ip;
        this.port = port;
        this.username = username;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 4 initial digits to represent length
     * 4 spaces
     * 3 for REG
     * @return length of the message
     */
    private int calculateLength() {
        return 4 + 4 + 3 + ip.length() + String.valueOf(port).length() + username.length();
    }

    @Override
    public String toString() {
        return "00" + calculateLength() + " " + key + " " + ip + " " + port + " " + username;
    }
}
