/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.Console;
import java.io.File;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.web3j.crypto.WalletUtils;

/**
 * Ethereum Tool main class.
 *
 * @author Frank Cornelis
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option help = Option.builder("h").required(false).hasArg(false).longOpt("help").desc("show help").build();
        options.addOption(help);

        Option createkey = Option.builder("c").required(false).hasArg(true)
                .argName("keydirectory").longOpt("createkey").desc("create a new key").build();
        options.addOption(createkey);

        CommandLineParser parser = new DefaultParser();
        CommandLine line;
        try {
            line = parser.parse(options, args);
        } catch (ParseException e) {
            printHelp(options);
            return;
        }

        if (line.hasOption("c")) {
            Console console = System.console();
            char[] password = console.readPassword("Password: ");
            char[] password2 = console.readPassword("Again: ");
            if (!Arrays.equals(password, password2)) {
                System.out.println("Password mismatch");
                return;
            }
            String destinationDirectory = line.getOptionValue("c");
            File destDir = new File(destinationDirectory);
            if (destDir.exists()) {
                if (!destDir.isDirectory()) {
                    System.out.println("destination not a directory");
                    return;
                }
            } else if (!destDir.mkdirs()) {
                System.out.println("could not create destination directory");
                return;
            }
            WalletUtils.generateNewWalletFile(new String(password), destDir, true);
            return;
        }

        printHelp(options);
    }

    private static void printHelp(Options options) throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ethereum-tool.sh", options);
    }
}
