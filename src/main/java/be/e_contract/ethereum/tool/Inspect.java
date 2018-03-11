/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.HashUtil;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import picocli.CommandLine;

@CommandLine.Command(name = "inspect", description = "inspect a transaction", separator = " ")
public class Inspect implements Callable<Void> {

    @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "the transaction file")
    private File transactionFile;

    @Override
    public Void call() throws Exception {
        if (!this.transactionFile.exists()) {
            System.out.println("transaction file not found");
            return null;
        }
        String hexData = FileUtils.readFileToString(this.transactionFile, "UTF-8");
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
        return null;
    }
}
