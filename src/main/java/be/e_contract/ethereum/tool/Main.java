/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Ethereum Tool main class.
 *
 * @author Frank Cornelis
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option help = new Option("h", "help", false, "print this message");
        options.addOption(help);

        Option createkey = new Option("c", "createkey", true, "create a new key");
        options.addOption(createkey);

        CommandLineParser parser = new DefaultParser();
        CommandLine line = parser.parse(options, args);

        if (line.hasOption("c")) {
            System.out.println("createkey TODO");
            return;
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ethereum-tool.sh", options);
    }
}
