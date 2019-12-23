package org.realitix.dfilesearch.filesearch.socket;

import org.apache.log4j.BasicConfigurator;
import org.junit.Test;

class UDPClientTest {

    @Test
    public void run() {
        BasicConfigurator.configure();
        UDPClient clientOne = new UDPClient("127.0.0.1", 5001 , "1234abcd");
        clientOne.messageBootstrapServer("127.0.0.1", 55555);
        UDPClient clientTwo = new UDPClient("127.0.0.1", 5002 , "1234abcf");
        clientTwo.messageBootstrapServer("127.0.0.1", 55555);
        UDPClient clientThree = new UDPClient("127.0.0.1", 5003 , "1234abcg");
        clientThree.messageBootstrapServer("127.0.0.1", 55555);
    }

}