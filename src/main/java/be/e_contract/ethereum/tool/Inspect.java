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
import org.web3j.crypto.transaction.type.Transaction1559;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "inspect", description = "inspect a transaction", separator = " ")
public class Inspect implements Callable<Void> {

    @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "the transaction file")
    private File transactionFile;

    @CommandLine.Option(names = {"-l", "--location"}, description = "the optional location of the client node for additional sanity checks")
    private Web3j web3;

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
        if (null != this.web3) {
            BigInteger transactionCount = this.web3.ethGetTransactionCount(from, DefaultBlockParameterName.LATEST).send().getTransactionCount();
            if (!transactionCount.equals(nonce)) {
                Output.error("Nonce " + nonce + " incorrect. Should be " + transactionCount);
            }
        }
        BigDecimal valueWei = new BigDecimal(transaction.getValue());
        BigDecimal valueEther = Convert.fromWei(valueWei, Convert.Unit.ETHER);
        System.out.println("Value: " + valueEther + " ether");
        BigDecimal gasLimitWei = new BigDecimal(transaction.getGasLimit());
        System.out.println("Gas limit: " + gasLimitWei + " gas units");
        TransactionType transactionType = transaction.getType();
        if (transactionType == TransactionType.EIP1559) {
            Transaction1559 transaction1559 = (Transaction1559) transaction.getTransaction();
            System.out.println("Chain id: " + transaction1559.getChainId());
            BigDecimal maxFeePerGasWei = new BigDecimal(transaction1559.getMaxFeePerGas());
            BigDecimal maxFeePerGasGwei = Convert.fromWei(maxFeePerGasWei, Convert.Unit.GWEI);
            System.out.println("Maximum fee per gas: " + maxFeePerGasGwei + " Gwei");
            BigDecimal maxPriorityFeePerGasWei = new BigDecimal(transaction1559.getMaxPriorityFeePerGas());
            BigDecimal maxPriorityFeePerGasGwei = Convert.fromWei(maxPriorityFeePerGasWei, Convert.Unit.GWEI);
            System.out.println("Maximum priority fee per gas: " + maxPriorityFeePerGasGwei + " Gwei");
            if (null != this.web3) {
                BigInteger nodeGasPrice = this.web3.ethGasPrice().send().getGasPrice();
                if (nodeGasPrice.compareTo(transaction1559.getMaxFeePerGas()) > 0) {
                    Output.error("Current gas price above maximum fee per gas.");
                    BigDecimal nodeGasPriceWei = new BigDecimal(nodeGasPrice);
                    BigDecimal nodeGasPriceGwei = Convert.fromWei(nodeGasPriceWei, Convert.Unit.GWEI);
                    Output.error("Current gas price: " + nodeGasPriceGwei + " Gwei.");
                }
                BigInteger nodeMaxPriorityFeePerGas = this.web3.ethMaxPriorityFeePerGas().send().getMaxPriorityFeePerGas();
                BigDecimal nodeMaxPriorityFeePerGasWei = new BigDecimal(nodeMaxPriorityFeePerGas);
                BigDecimal nodeMaxPriorityFeePerGasGwei = Convert.fromWei(nodeMaxPriorityFeePerGasWei, Convert.Unit.GWEI);
                System.out.println("Current maximum priority fee per gas: " + nodeMaxPriorityFeePerGasGwei + " Gwei");
                BigInteger balance = this.web3.ethGetBalance(from, DefaultBlockParameterName.LATEST).send().getBalance();
                BigInteger maxTotalCost = transaction.getValue().add(transaction.getGasLimit().multiply(transaction1559.getMaxFeePerGas()));
                if (balance.compareTo(maxTotalCost) < 0) {
                    Output.error("Balance might be too low.");
                    BigDecimal balanceWei = new BigDecimal(balance);
                    BigDecimal balanceEther = Convert.fromWei(balanceWei, Convert.Unit.ETHER);
                    Output.error("Balance: " + balanceEther + " ETH");
                    BigDecimal maxTotalCostWei = new BigDecimal(maxTotalCost);
                    BigDecimal maxTotalCostEther = Convert.fromWei(maxTotalCostWei, Convert.Unit.ETHER);
                    Output.error("Maximum total cost: " + maxTotalCostEther + " ETH");
                }
            }
        } else {
            BigDecimal gasPriceWei = new BigDecimal(transaction.getGasPrice());
            BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
            System.out.println("Gas price: " + gasPriceGwei + " Gwei");
            System.out.println("This is a legacy transaction.");
        }
        return null;
    }
}
