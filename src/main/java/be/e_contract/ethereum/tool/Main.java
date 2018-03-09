/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.Console;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.RandomStringUtils;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Convert;

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

        Option verifykey = Option.builder("v").required(false).hasArg(true)
                .argName("keyfile").longOpt("verifykey").desc("verify a key").build();
        options.addOption(verifykey);

        Option nonce = Option.builder("n").required(false).hasArg(true).numberOfArgs(2)
                .argName("location> <address").longOpt("nonce").desc("retrieve the transaction nonce").build();
        options.addOption(nonce);

        Option gas = Option.builder("p").required(false).hasArg(true).numberOfArgs(1)
                .argName("location").longOpt("gasprice").desc("retrieve the average gas price").build();
        options.addOption(gas);

        Option generatePassword = Option.builder("g").required(false).hasArg(false)
                .longOpt("generatepassword").desc("generate a strong password").build();
        options.addOption(generatePassword);

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
            String keyfile = WalletUtils.generateNewWalletFile(new String(password), destDir, true);
            System.out.println("key file: " + keyfile);
            return;
        }

        if (line.hasOption("v")) {
            File keyFile = new File(line.getOptionValue("v"));
            if (!keyFile.exists()) {
                System.out.println("non existing key file");
                return;
            }
            Console console = System.console();
            char[] password = console.readPassword("Password: ");
            Credentials credentials;
            try {
                credentials = WalletUtils.loadCredentials(new String(password), keyFile);
            } catch (CipherException ex) {
                System.out.println("incorrect password");
                return;
            }
            System.out.println("address: " + credentials.getAddress());
            return;
        }

        if (line.hasOption("n")) {
            String location = line.getOptionValues("n")[0];
            String address = line.getOptionValues("n")[1];
            Web3jService service;
            if (location.startsWith("http")) {
                service = new HttpService(location);
            } else {
                service = new UnixIpcService(location);
            }
            Web3j web3 = Web3j.build(service);
            BigInteger transactionCount = web3.ethGetTransactionCount(address, DefaultBlockParameterName.LATEST).send().getTransactionCount();
            System.out.println("transaction count: " + transactionCount);
            return;
        }

        if (line.hasOption("p")) {
            String location = line.getOptionValue("p");
            Web3jService service;
            if (location.startsWith("http")) {
                service = new HttpService(location);
            } else {
                service = new UnixIpcService(location);
            }
            Web3j web3 = Web3j.build(service);
            BigDecimal gasPriceWei = BigDecimal.valueOf(web3.ethGasPrice().send().getGasPrice().longValueExact());
            System.out.println("gas price: " + gasPriceWei + "wei");
            BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
            System.out.println("gas price: " + gasPriceGwei + " Gwei");
            return;
        }

        if (line.hasOption("g")) {
            String password = RandomStringUtils.randomAlphanumeric(32);
            System.out.println(password);
            return;
        }

        printHelp(options);
    }

    private static void printHelp(Options options) throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ethereum-tool.sh", options);
    }
}
