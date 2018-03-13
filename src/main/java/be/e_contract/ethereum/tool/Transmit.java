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
