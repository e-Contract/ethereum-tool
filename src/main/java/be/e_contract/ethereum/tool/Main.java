/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import com.google.gson.Gson;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

/**
 * Ethereum Tool main class.
 *
 * @author Frank Cornelis
 */
public class Main {

    public static void main(String[] args) throws Exception {
        Options options = new Options();

        Option helpOption = Option.builder("h").required(false).hasArg(false).longOpt("help").desc("show help").build();
        options.addOption(helpOption);

        Option createkeyOption = Option.builder("c").required(false).hasArg(true)
                .argName("keydirectory").longOpt("createkey").desc("create a new key").build();
        options.addOption(createkeyOption);

        Option verifykeyOption = Option.builder("v").required(false).hasArg(true)
                .argName("keyfile").longOpt("verifykey").desc("verify a key").build();
        options.addOption(verifykeyOption);

        Option nonceOption = Option.builder("n").required(false).hasArg(true).numberOfArgs(2)
                .argName("location> <address").longOpt("nonce").desc("retrieve the transaction nonce").build();
        options.addOption(nonceOption);

        Option gasPriceOption = Option.builder("p").required(false).hasArg(true).numberOfArgs(1)
                .argName("location").longOpt("gasprice").desc("retrieve the average gas price").build();
        options.addOption(gasPriceOption);

        Option generatePasswordOption = Option.builder("g").required(false).hasArg(false)
                .longOpt("generatepassword").desc("generate a strong password").build();
        options.addOption(generatePasswordOption);

        Option signOption = Option.builder("s").required(false).hasArg(true).numberOfArgs(3)
                .argName("template> <keyfile> <outfile").longOpt("sign").desc("sign a transaction").build();
        options.addOption(signOption);

        Option transmitOption = Option.builder("t").required(false).hasArg(true).numberOfArgs(2)
                .argName("location> <transactfile").longOpt("transmit").desc("transmit a transaction").build();
        options.addOption(transmitOption);

        Option transactionOption = Option.builder("i").required(false).hasArg(true).numberOfArgs(1)
                .argName("transactionfile").longOpt("inspect").desc("inspect a transaction file").build();
        options.addOption(transactionOption);

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
            char[] password = console.readPassword("Passphrase:");
            char[] password2 = console.readPassword("Repeat passphrase:");
            if (!Arrays.equals(password, password2)) {
                System.out.println("Passphrase mismatch");
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
            } else {
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_READ);
                permissions.add(PosixFilePermission.OWNER_WRITE);
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                Files.setPosixFilePermissions(destDir.toPath(), permissions);
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
            char[] password = console.readPassword("Passphrase: ");
            Credentials credentials;
            try {
                credentials = WalletUtils.loadCredentials(new String(password), keyFile);
            } catch (CipherException ex) {
                System.out.println("incorrect passphrase");
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
            System.out.println("gas price: " + gasPriceWei + " wei");
            BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
            System.out.println("gas price: " + gasPriceGwei + " Gwei");
            return;
        }

        if (line.hasOption("g")) {
            String password = RandomStringUtils.randomAlphanumeric(32);
            System.out.println(password);
            return;
        }

        if (line.hasOption("s")) {
            String template = line.getOptionValues("s")[0];
            String keyfile = line.getOptionValues("s")[1];
            String outfile = line.getOptionValues("s")[2];
            File outFile = new File(outfile);
            Console console = System.console();
            if (outFile.exists()) {
                System.out.println("existing output file: " + outfile);
                boolean confirmation = askConfirmation(console, "Overwrite output file? (y/n)");
                if (!confirmation) {
                    return;
                }
            }
            File templateFile = new File(template);
            if (!templateFile.exists()) {
                System.out.println("template file does not exist");
                return;
            }
            File keyFile = new File(keyfile);
            if (!keyFile.exists()) {
                System.out.println("non existing key file");
                return;
            }
            Gson gson = new Gson();
            TransactionTemplate transactionTemplate = gson.fromJson(new FileReader(templateFile), TransactionTemplate.class);
            System.out.println("to: " + transactionTemplate.to);
            System.out.println("value: " + transactionTemplate.value + " ether");
            System.out.println("gas price: " + transactionTemplate.gasPrice + " gwei");
            System.out.println("nonce: " + transactionTemplate.nonce);
            boolean confirmation = askConfirmation(console, "Sign transaction? (y/n)");
            if (!confirmation) {
                return;
            }

            char[] password = console.readPassword("Passphrase: ");
            Credentials credentials;
            try {
                credentials = WalletUtils.loadCredentials(new String(password), keyFile);
            } catch (CipherException ex) {
                System.out.println("incorrect passphrase");
                return;
            }
            System.out.println("From address: " + credentials.getAddress());
            confirmation = askConfirmation(console, "Confirm from address? (y/n)");
            if (!confirmation) {
                return;
            }
            BigInteger nonce = BigInteger.valueOf(transactionTemplate.nonce);
            BigDecimal gasPriceGwei = BigDecimal.valueOf(transactionTemplate.gasPrice);
            BigDecimal gasPriceWei = Convert.toWei(gasPriceGwei, Convert.Unit.GWEI);
            BigDecimal valueEther = BigDecimal.valueOf(transactionTemplate.value);
            BigDecimal valueWei = Convert.toWei(valueEther, Convert.Unit.ETHER);
            BigInteger gasLimit = BigInteger.valueOf(21000);
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPriceWei.toBigIntegerExact(),
                    gasLimit, transactionTemplate.to, valueWei.toBigIntegerExact());
            byte[] signedTransaction;
            if (null != transactionTemplate.chainId) {
                signedTransaction = TransactionEncoder.signMessage(rawTransaction, transactionTemplate.chainId, credentials);
            } else {
                signedTransaction = TransactionEncoder.signMessage(rawTransaction, credentials);
            }
            String transactionHash = Numeric.toHexString(HashUtil.sha3(signedTransaction));
            System.out.println("transaction hash: " + transactionHash);
            String hexValue = Numeric.toHexString(signedTransaction);
            FileUtils.writeStringToFile(outFile, hexValue, "UTF-8");
            return;
        }

        if (line.hasOption("i")) {
            String transactionfile = line.getOptionValues("i")[0];
            File transactionFile = new File(transactionfile);
            if (!transactionFile.exists()) {
                System.out.println("transaction file not found");
                return;
            }
            String hexData = FileUtils.readFileToString(transactionFile, "UTF-8");
            byte[] rawData = Numeric.hexStringToByteArray(hexData);
            String transactionHash = Numeric.toHexString(HashUtil.sha3(rawData));
            System.out.println("transaction hash: " + transactionHash);
            Transaction transaction = new Transaction(rawData);
            transaction.verify();
            String from = Numeric.toHexString(transaction.getSender());
            String to = Numeric.toHexString(transaction.getReceiveAddress());
            System.out.println("from: " + from);
            System.out.println("to: " + to);
            Integer chainId = transaction.getChainId();
            if (null != chainId) {
                System.out.println("chain id: " + chainId);
            }
            BigInteger nonce = new BigInteger(transaction.getNonce());
            System.out.println("nonce: " + nonce);
            BigDecimal valueWei = new BigDecimal(new BigInteger(1, transaction.getValue()));
            BigDecimal valueEther = Convert.fromWei(valueWei, Convert.Unit.ETHER);
            System.out.println("value: " + valueEther + " ether");
            BigDecimal gasLimitWei = new BigDecimal(new BigInteger(1, transaction.getGasLimit()));
            System.out.println("gas limit: " + gasLimitWei + " wei");
            BigDecimal gasPriceWei = new BigDecimal(new BigInteger(1, transaction.getGasPrice()));
            BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
            System.out.println("gas price: " + gasPriceGwei + " gwei");
            return;
        }

        if (line.hasOption("t")) {
            String location = line.getOptionValues("t")[0];
            String transactionfile = line.getOptionValues("t")[1];
            File transactionFile = new File(transactionfile);
            if (!transactionFile.exists()) {
                System.out.println("transaction file not found");
                return;
            }
            String transactionHex = FileUtils.readFileToString(transactionFile, "UTF-8");
            Web3jService service;
            if (location.startsWith("http")) {
                service = new HttpService(location);
            } else {
                service = new UnixIpcService(location);
            }
            Web3j web3 = Web3j.build(service);
            EthSendTransaction ethSendTransaction = web3.ethSendRawTransaction(transactionHex).send();
            String transactionHash = ethSendTransaction.getTransactionHash();
            System.out.println("transaction hash: " + transactionHash);
            return;
        }

        printHelp(options);
    }

    private static void printHelp(Options options) throws Exception {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ethereum-tool.sh", options);
    }

    private static boolean askConfirmation(Console console, String message) {
        while (true) {
            String confirmation = console.readLine(message);
            if ("y".equals(confirmation)) {
                return true;
            }
            if ("n".equals(confirmation)) {
                return false;
            }
        }
    }
}
