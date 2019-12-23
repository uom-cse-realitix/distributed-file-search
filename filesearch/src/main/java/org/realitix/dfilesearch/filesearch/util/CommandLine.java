package org.realitix.dfilesearch.filesearch.util;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

public class CommandLine extends Command {

    public CommandLine(String name, String description) {
        super(name, description);
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("-h", "--host")
                .dest("host")
                .type(String.class)
                .required(false)
                .help("Host IP of the client");
        subparser.addArgument("-p", "--port")
                .dest("port")
                .type(int.class)
                .required(true)
                .help("Port of the client");
        subparser.addArgument("-u", "--user")
                .type(String.class)
                .required(true)
                .help("Username of the client");
    }

    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {

    }
}
