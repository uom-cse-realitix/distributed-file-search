package org.realitix.dfilesearch.filesearch.beans.messages;

public class RegisterResponse implements CommonResponse {

    private String length;
    private int numberOfNodes;
    private String ip_1;
    private String ip_2;
    private int port1;
    private int port2;

    public RegisterResponse(String length, int numberOfNodes, String ip_1, String ip_2, int port1, int port2) {
        this.length = length;
        this.numberOfNodes = numberOfNodes;
        this.ip_1 = ip_1;
        this.ip_2 = ip_2;
        this.port1 = port1;
        this.port2 = port2;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public int getNumberOfNodes() {
        return numberOfNodes;
    }

    public void setNumberOfNodes(int numberOfNodes) {
        this.numberOfNodes = numberOfNodes;
    }

    public String getIp_1() {
        return ip_1;
    }

    public void setIp_1(String ip_1) {
        this.ip_1 = ip_1;
    }

    public String getIp_2() {
        return ip_2;
    }

    public void setIp_2(String ip_2) {
        this.ip_2 = ip_2;
    }

    public int getPort1() {
        return port1;
    }

    public void setPort1(int port1) {
        this.port1 = port1;
    }

    public int getPort2() {
        return port2;
    }

    public void setPort2(int port2) {
        this.port2 = port2;
    }
}
