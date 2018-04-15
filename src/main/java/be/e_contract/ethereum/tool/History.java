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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "history", description = "history of an address", separator = " ")
public class History implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-a", "--address"}, required = true, description = "the key address")
    private Address address;

    @CommandLine.Option(names = {"-n", "--blocks"}, required = true, description = "number of blocks from latest to scan")
    private int n;

    @Override
    public Void call() throws Exception {
        BigInteger blockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        System.out.println("Scanning from block " + blockNumber + " down to block " + blockNumber.subtract(BigInteger.valueOf(this.n)) + " ...");
        // cannot use transaction count here as we would then miss incoming transactions
        // also cannot use "historical" balance has we might receive "missing trie node" errors
        while (this.n > 0) {
            EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
            for (EthBlock.TransactionResult transactionResult : block.getTransactions()) {
                EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
                Transaction transaction = transactionObject.get();
                if (this.address.getAddress().equals(transaction.getFrom())
                        || this.address.getAddress().equals(transaction.getTo())) {
                    Output.printlnBold("Transaction hash: " + transaction.getHash());
                    Output.println(10, "From: " + transaction.getFrom());
                    Output.println(10, "To: " + transaction.getTo());
                    Output.println(10, "Value: " + Convert.fromWei(new BigDecimal(transaction.getValue()), Convert.Unit.ETHER) + " ether");
                    Output.println(10, "Block number: " + blockNumber);
                    BigInteger timestamp = block.getTimestamp();
                    Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
                    Output.println(10, "Transaction block timestamp: " + timestampDate);
                }
            }
            this.n--;
            blockNumber = blockNumber.subtract(BigInteger.ONE);
            if (blockNumber.signum() == -1) {
                return null;
            }
        }
        return null;
    }
}
