package org.realitix.dfilesearch.filesearch.socket;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;

public class UDPClientExecutor {

    public static void main(String[] args) {
        BasicConfigurator.configure();
        UDPClient client = new UDPClient("127.0.0.1", 5001, "1234abcd");
        try {
            client.run("127.0.0.1", 55555, "127.0.0.1" , Integer.parseInt(args[0]), args[1]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
