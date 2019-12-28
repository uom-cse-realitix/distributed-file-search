package org.realitix.dfilesearch.filesearch.beans.messages;

public class JoinRequest implements CommonMessage {

    private String length;
    private final String key = "JOIN";
    private String ip;
    private int port;

    public JoinRequest(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getKey() {
        return key;
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

    /**
     * 4 initial digits to represent length
     * 3 spaces
     * 4 for JOIN
     * @return length of the message
     */
    private int calculateLength() {
        return 4 + 3 + 4 + ip.length() + String.valueOf(port).length();
    }

    @Override
    public String toString() {
        return "00" + calculateLength() + " " + key + " " + ip + " " + port;
    }
}
