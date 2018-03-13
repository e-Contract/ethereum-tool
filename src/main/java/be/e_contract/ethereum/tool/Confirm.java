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
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.ethereum.crypto.HashUtil;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import picocli.CommandLine;

@CommandLine.Command(name = "confirm", description = "confirm a transaction", separator = " ")
public class Confirm implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private String location;

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
            _transactionHash = this.transactionHash;
        }
        System.out.println("transaction hash: " + _transactionHash);
        Web3jService service;
        if (this.location.startsWith("http")) {
            service = new HttpService(this.location);
        } else {
            service = new UnixIpcService(this.location);
        }
        Web3j web3 = Web3j.build(service);
        EthGetTransactionReceipt getTransactionReceipt = web3.ethGetTransactionReceipt(_transactionHash).send();
        Optional<TransactionReceipt> transactionReceiptOptional = getTransactionReceipt.getTransactionReceipt();
        if (!transactionReceiptOptional.isPresent()) {
            System.out.println("transaction receipt not available");
            EthBlock pendingEthBlock = web3.ethGetBlockByNumber(DefaultBlockParameterName.PENDING, true).send();
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
                System.out.println("transaction is pending");
                System.out.println("number of pending transactions: " + pendingBlock.getTransactions().size());
            } else {
                System.out.println("transaction is not pending");
            }
            return null;
        }
        TransactionReceipt transactionReceipt = transactionReceiptOptional.get();
        System.out.println("From: " + transactionReceipt.getFrom());
        System.out.println("To: " + transactionReceipt.getTo());
        BigInteger transactionBlockNumber = transactionReceipt.getBlockNumber();
        System.out.println("transaction block number: " + transactionBlockNumber);
        BigDecimal gasUsed = new BigDecimal(transactionReceipt.getGasUsed());
        System.out.println("gas used: " + gasUsed + " wei");
        EthBlock ethBlock = web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(transactionBlockNumber), false).send();
        EthBlock.Block block = ethBlock.getBlock();
        BigInteger timestamp = block.getTimestamp();
        Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
        System.out.println("transaction block timestamp: " + timestampDate);
        BigInteger latestBlockNumber = web3.ethBlockNumber().send().getBlockNumber();
        System.out.println("latest block number: " + latestBlockNumber);
        // add one, since the transaction block also serves as confirmation
        BigInteger blocksOnTop = latestBlockNumber.subtract(transactionBlockNumber).add(BigInteger.ONE);
        System.out.println("number of confirming blocks: " + blocksOnTop);
        EthTransaction ethTransaction = web3.ethGetTransactionByHash(_transactionHash).send();
        Transaction transaction = ethTransaction.getTransaction().get();
        BigInteger nonce = transaction.getNonce();
        System.out.println("nonce: " + nonce);
        BigDecimal valueWei = new BigDecimal(transaction.getValue());
        BigDecimal valueEther = Convert.fromWei(valueWei, Convert.Unit.ETHER);
        System.out.println("value: " + valueEther + " ether");
        BigDecimal gasPriceWei = new BigDecimal(transaction.getGasPrice());
        BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
        System.out.println("gas price: " + gasPriceGwei + " gwei");
        BigDecimal transactionCostWei = gasUsed.multiply(gasPriceWei);
        BigDecimal transactionCostEther = Convert.fromWei(transactionCostWei, Convert.Unit.ETHER);
        System.out.println("transaction cost: " + transactionCostEther + " ether");
        return null;
    }
}
