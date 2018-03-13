/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import picocli.CommandLine;

@CommandLine.Command(name = "pending", description = "information on pending block", separator = " ")
public class Pending implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @Override
    public Void call() throws Exception {
        EthBlock pendingEthBlock = this.web3.ethGetBlockByNumber(DefaultBlockParameterName.PENDING, true).send();
        EthBlock.Block pendingBlock = pendingEthBlock.getBlock();
        int pendingTransactionCount = pendingBlock.getTransactions().size();
        System.out.println("number of pending transactions: " + pendingTransactionCount);
        return null;
    }
}
