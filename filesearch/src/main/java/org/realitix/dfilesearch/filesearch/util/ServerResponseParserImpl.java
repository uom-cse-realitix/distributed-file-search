package org.realitix.dfilesearch.filesearch.util;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ServerResponseParserImpl implements ResponseParser<String> {

    private static final Logger logger = LogManager.getLogger(ServerResponseParserImpl.class);

    @Override
    public void parse(String s) {
        switch (s) {
            case "JOIN":
                parseJoin(s);
        }
    }

    private void parseJoin(String joinRequest){

    }



}
