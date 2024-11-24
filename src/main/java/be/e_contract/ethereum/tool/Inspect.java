/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2024 e-Contract.be BV.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version
 * 3.0 as published by the Free Software Foundation.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, see 
 * http://www.gnu.org/licenses/.
 */
package be.e_contract.ethereum.tool;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "inspect", description = "inspect a transaction", separator = " ")
public class Inspect implements Callable<Void> {

    @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "the transaction file")
    private File transactionFile;

    @Override
    public Void call() throws Exception {
        if (!this.transactionFile.exists()) {
            Output.error("Transaction file not found");
            return null;
        }
        String hexData = FileUtils.readFileToString(this.transactionFile, "UTF-8");
        String transactionHash = Hash.sha3(hexData);
        System.out.println("Transaction hash: " + transactionHash);
        RawTransaction rawTransaction = TransactionDecoder.decode(hexData);
        if (!(rawTransaction instanceof SignedRawTransaction)) {
            Output.error("Transaction is not signed.");
            return null;
        }
        SignedRawTransaction transaction = (SignedRawTransaction) rawTransaction;
        String from = transaction.getFrom();
        String to = transaction.getTo();
        System.out.println("From: " + from);
        System.out.println("To: " + to);
        String checksumTo = Keys.toChecksumAddress(to);
        System.out.println("To (checksum): " + checksumTo);
        Long chainId = transaction.getChainId();
        if (null != chainId) {
            System.out.println("Chain id: " + chainId);
        }
        BigInteger nonce = transaction.getNonce();
        System.out.println("Nonce: " + nonce);
        BigDecimal valueWei = new BigDecimal(transaction.getValue());
        BigDecimal valueEther = Convert.fromWei(valueWei, Convert.Unit.ETHER);
        System.out.println("Value: " + valueEther + " ether");
        BigDecimal gasLimitWei = new BigDecimal(transaction.getGasLimit());
        System.out.println("Gas limit: " + gasLimitWei + " gas units");
        BigDecimal gasPriceWei = new BigDecimal(transaction.getGasPrice());
        BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
        System.out.println("Gas price: " + gasPriceGwei + " Gwei");
        return null;
    }
}
