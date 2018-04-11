/*
 * Ethereum Tool project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.ethereum.crypto.HashUtil;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import picocli.CommandLine;

@CommandLine.Command(name = "confirm", description = "confirm a transaction", separator = " ")
public class Confirm implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-f", "--file"}, description = "the transaction file")
    private File transactionFile;

    @CommandLine.Option(names = {"-h", "--hash"}, description = "the transaction hash")
    private String transactionHash;

    @Override
    public Void call() throws Exception {
        if (null == this.transactionFile && null == this.transactionHash) {
            Output.error("provide transaction file or transaction hash");
            picocli.CommandLine.usage(this, System.out);
            return null;
        }
        String _transactionHash;
        if (null != transactionFile) {
            if (!this.transactionFile.exists()) {
                Output.error("transaction file not found");
                return null;
            }
            String transactionHex = FileUtils.readFileToString(this.transactionFile, "UTF-8");
            byte[] rawData = Numeric.hexStringToByteArray(transactionHex);
            _transactionHash = Numeric.toHexString(HashUtil.sha3(rawData));
        } else {
            _transactionHash = this.transactionHash.toLowerCase();
        }
        System.out.println("Transaction hash: " + _transactionHash);
        EthGetTransactionReceipt getTransactionReceipt = this.web3.ethGetTransactionReceipt(_transactionHash).send();
        Optional<TransactionReceipt> transactionReceiptOptional = getTransactionReceipt.getTransactionReceipt();
        if (!transactionReceiptOptional.isPresent()) {
            System.out.println("Transaction receipt not available");
            EthBlock pendingEthBlock = this.web3.ethGetBlockByNumber(DefaultBlockParameterName.PENDING, true).send();
            EthBlock.Block pendingBlock = pendingEthBlock.getBlock();
            boolean pendingTransaction = false;
            for (EthBlock.TransactionResult transactionResult : pendingBlock.getTransactions()) {
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                if (_transactionHash.equals(transaction.getHash())) {
                    pendingTransaction = true;
                    break;
                }
            }
            if (pendingTransaction) {
                System.out.println("Transaction is pending");
                System.out.println("Number of pending transactions: " + pendingBlock.getTransactions().size());
            } else {
                Output.warning("Transaction is not pending");
            }
            return null;
        }
        TransactionReceipt transactionReceipt = transactionReceiptOptional.get();
        if (!"0x1".equals(transactionReceipt.getStatus())) {
            Output.error("Transaction has failed with status: " + transactionReceipt.getStatus());
            return null;
        }
        System.out.println("From: " + transactionReceipt.getFrom());
        System.out.println("To: " + transactionReceipt.getTo());
        BigInteger transactionBlockNumber = transactionReceipt.getBlockNumber();
        System.out.println("Transaction block number: " + transactionBlockNumber);
        BigDecimal gasUsed = new BigDecimal(transactionReceipt.getGasUsed());
        System.out.println("Gas used: " + gasUsed + " wei");
        EthBlock ethBlock = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(transactionBlockNumber), false).send();
        EthBlock.Block block = ethBlock.getBlock();
        BigInteger timestamp = block.getTimestamp();
        Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
        System.out.println("Transaction block timestamp: " + timestampDate);
        BigInteger latestBlockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        System.out.println("Latest block number: " + latestBlockNumber);
        // add one, since the transaction block also serves as confirmation
        BigInteger blocksOnTop = latestBlockNumber.subtract(transactionBlockNumber).add(BigInteger.ONE);
        System.out.println("Number of confirming blocks: " + blocksOnTop);
        EthTransaction ethTransaction = this.web3.ethGetTransactionByHash(_transactionHash).send();
        Transaction transaction = ethTransaction.getTransaction().get();
        BigInteger nonce = transaction.getNonce();
        System.out.println("Nonce: " + nonce);
        BigDecimal valueWei = new BigDecimal(transaction.getValue());
        BigDecimal valueEther = Convert.fromWei(valueWei, Convert.Unit.ETHER);
        System.out.println("Value: " + valueEther + " ether");
        BigDecimal gasPriceWei = new BigDecimal(transaction.getGasPrice());
        BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
        System.out.println("Gas price: " + gasPriceGwei + " gwei");
        BigDecimal transactionCostWei = gasUsed.multiply(gasPriceWei);
        BigDecimal transactionCostEther = Convert.fromWei(transactionCostWei, Convert.Unit.ETHER);
        System.out.println("Transaction cost: " + transactionCostEther + " ether");
        BigInteger fromBalance = this.web3.ethGetBalance(transactionReceipt.getFrom(), DefaultBlockParameterName.LATEST).send().getBalance();
        BigDecimal fromBalanceEther = Convert.fromWei(new BigDecimal(fromBalance), Convert.Unit.ETHER);
        System.out.println("Balance from address: " + fromBalanceEther + " ether");
        BigInteger toBalance = this.web3.ethGetBalance(transactionReceipt.getTo(), DefaultBlockParameterName.LATEST).send().getBalance();
        BigDecimal toBalanceEther = Convert.fromWei(new BigDecimal(toBalance), Convert.Unit.ETHER);
        System.out.println("Balance to address: " + toBalanceEther + " ether");
        return null;
    }
}
