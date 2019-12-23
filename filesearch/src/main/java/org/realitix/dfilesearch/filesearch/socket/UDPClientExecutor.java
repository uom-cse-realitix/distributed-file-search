package org.realitix.dfilesearch.filesearch.socket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

public class UDPClientExecutor {

    private static final Logger logger = LogManager.getLogger(UDPClientExecutor.class);

    public static void main(String[] args) {
        BasicConfigurator.configure();
        UDPClient client = new UDPClient(args[0], Integer.parseInt(args[1]) , args[2]);
        client.messageBootstrapServer("127.0.0.1", 55555);
    }

}
