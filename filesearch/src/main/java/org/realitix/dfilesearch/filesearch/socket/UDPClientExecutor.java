package org.realitix.dfilesearch.filesearch.socket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class UDPClientExecutor {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        UDPClient client = new UDPClient("127.0.0.1", 5001, "1234abcd");
        client.run("127.0.0.1", 55555);
    }

}
