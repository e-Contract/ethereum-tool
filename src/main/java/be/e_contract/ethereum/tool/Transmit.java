/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.File;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import picocli.CommandLine;

@CommandLine.Command(name = "transmit", description = "transmit a transaction", separator = " ")
public class Transmit implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-f", "--file"}, required = true, description = "the transaction file")
    private File transactionFile;

    @Override
    public Void call() throws Exception {
        if (!this.transactionFile.exists()) {
            Output.error("transaction file not found");
            return null;
        }
        String transactionHex = FileUtils.readFileToString(this.transactionFile, "UTF-8");
        EthSendTransaction ethSendTransaction = this.web3.ethSendRawTransaction(transactionHex).send();
        if (ethSendTransaction.hasError()) {
            Response.Error error = ethSendTransaction.getError();
            String errorMessage = error.getMessage();
            Output.error("error: " + errorMessage);
            return null;
        }
        String transactionHash = ethSendTransaction.getTransactionHash();
        System.out.println("transaction hash: " + transactionHash);
        return null;
    }
}
