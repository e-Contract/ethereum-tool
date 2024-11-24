/*
 * Ethereum Tool project.
 * Copyright (C) 2019-2024 e-Contract.be BV.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "block", description = "inspect a block", separator = " ")
public class Block implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-h", "--hash"}, description = "the optional block hash")
    private String blockHash;

    @CommandLine.Option(names = {"-n", "--number"}, description = "the optional block number")
    private BigInteger blockNumber;

    @CommandLine.Option(names = {"-t", "--transactions"}, description = "show the transactions")
    private boolean[] displayTransactions;

    @CommandLine.Option(names = {"-r", "--regular-transactions"}, description = "show the regular transactions")
    private boolean[] displayRegularTransactions;

    @Override
    public Void call() throws Exception {
        if (null == this.blockHash && null == this.blockNumber) {
            this.blockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        }
        if (null != this.blockNumber) {
            EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(this.blockNumber), true).send().getBlock();
            printBlock(block);
            return null;
        }
        EthBlock.Block block = this.web3.ethGetBlockByHash(this.blockHash, true).send().getBlock();
        printBlock(block);
        return null;
    }

    private void printBlock(EthBlock.Block block) throws Exception {
        if (null == block) {
            if (null != this.blockNumber) {
                Output.error("Unknown block number: " + this.blockNumber);
            } else {
                Output.error("Unknown block hash: " + this.blockHash);
            }
            return;
        }
        System.out.println("Block number: " + block.getNumber());
        System.out.println("Block hash: " + block.getHash());
        BigInteger blockTimestamp = block.getTimestamp();
        Date blockTimestampDate = new Date(blockTimestamp.multiply(BigInteger.valueOf(1000)).longValue());
        System.out.println("Block timestamp: " + blockTimestampDate);
        System.out.println("Number of transactions: " + block.getTransactions().size());
        double percentageGasUsed = (double) block.getGasUsed().longValueExact() / block.getGasLimit().longValueExact() * 100;
        System.out.println("Gas limit: " + block.getGasLimit() + " gas units");
        System.out.println("Gas used: " + block.getGasUsed() + " gas units (" + percentageGasUsed + " %)");
        BigDecimal baseFeePerGas = new BigDecimal(block.getBaseFeePerGas());
        BigDecimal baseFeePerGasGwei = Convert.fromWei(baseFeePerGas, Convert.Unit.GWEI);
        System.out.println("Base Fee: " + baseFeePerGasGwei + " Gwei");
        if (this.displayTransactions != null) {
            System.out.println("Transactions:");
            for (EthBlock.TransactionResult transactionResult : block.getTransactions()) {
                EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) transactionResult.get();
                Output.println(10, "Transaction hash: " + transaction.getHash());
                Output.println(20, "From: " + transaction.getFrom());
                Output.println(20, "To: " + transaction.getTo());
                if ("0x".equals(transaction.getInput())) {
                    BigDecimal valueEther = Convert.fromWei(new BigDecimal(transaction.getValue()), Convert.Unit.ETHER);
                    Output.println(20, "Value: " + valueEther + " ether");
                } else {
                    Output.println(20, "Contract transaction: " + transaction.getInput());
                }
                BigInteger gasUsed = transaction.getGas();
                Output.println(20, "Gas used: " + gasUsed + " gas units");
                BigDecimal gasPrice = Convert.fromWei(new BigDecimal(transaction.getGasPrice()), Convert.Unit.GWEI);
                Output.println(20, "Gas price: " + gasPrice + " Gwei");
                BigInteger maxFeePerGas = transaction.getMaxFeePerGas();
                if (null != maxFeePerGas) {
                    // eip-1559
                    Output.println(20, "Max fee per gas: " + Convert.fromWei(new BigDecimal(maxFeePerGas), Convert.Unit.GWEI) + " Gwei");
                    if (null != transaction.getMaxPriorityFeePerGasRaw()) {
                        BigInteger maxPriorityFeePerGas = transaction.getMaxPriorityFeePerGas();
                        Output.println(20, "Max priority fee per gas: " + Convert.fromWei(new BigDecimal(maxPriorityFeePerGas), Convert.Unit.GWEI) + " Gwei");
                    }
                } else {
                    Output.println(20, "Legacy transaction.");
                }
            }
        }
        if (this.displayRegularTransactions != null) {
            System.out.println("Regular transactions:");
            for (EthBlock.TransactionResult transactionResult : block.getTransactions()) {
                EthBlock.TransactionObject transaction = (EthBlock.TransactionObject) transactionResult.get();
                if (!"0x".equals(transaction.getInput())) {
                    continue;
                }
                Output.println(10, "Transaction hash: " + transaction.getHash());
                Output.println(20, "From: " + transaction.getFrom());
                Output.println(20, "To: " + transaction.getTo());
                BigDecimal valueEther = Convert.fromWei(new BigDecimal(transaction.getValue()), Convert.Unit.ETHER);
                Output.println(20, "Value: " + valueEther + " ether");
                BigInteger gasUsed = transaction.getGas();
                Output.println(20, "Gas used: " + gasUsed + " gas units");
                BigDecimal gasPrice = Convert.fromWei(new BigDecimal(transaction.getGasPrice()), Convert.Unit.GWEI);
                Output.println(20, "Gas price: " + gasPrice + " Gwei");
                BigInteger maxFeePerGas = transaction.getMaxFeePerGas();
                if (null != maxFeePerGas) {
                    // eip-1559
                    Output.println(20, "Max fee per gas: " + Convert.fromWei(new BigDecimal(maxFeePerGas), Convert.Unit.GWEI) + " Gwei");
                    if (null != transaction.getMaxPriorityFeePerGasRaw()) {
                        BigInteger maxPriorityFeePerGas = transaction.getMaxPriorityFeePerGas();
                        Output.println(20, "Max priority fee per gas: " + Convert.fromWei(new BigDecimal(maxPriorityFeePerGas), Convert.Unit.GWEI) + " Gwei");
                    }
                } else {
                    Output.println(20, "Legacy transaction.");
                }
            }
        }
        for (String uncle : block.getUncles()) {
            System.out.println("Block uncle: " + uncle);
        }
        System.out.println("Parent hash: " + block.getParentHash());
        System.out.println("SHA3Uncles: " + block.getSha3Uncles());
    }
}
