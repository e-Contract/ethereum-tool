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

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "trace", description = "Realtime trace transactions on an address", separator = " ")
public class Trace implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-a", "--address"}, required = true, description = "the key address")
    private String address;

    @Override
    public Void call() throws Exception {
        this.address = this.address.toLowerCase(); // fun fun fun.. capitals
        System.out.println("Address: " + this.address);
        this.web3.blockObservable(true).subscribe(ethBlock -> {
            EthBlock.Block block = ethBlock.getBlock();
            BigInteger blockNumber = block.getNumber();
            BigInteger balance;
            try {
                balance = this.web3.ethGetBalance(this.address, DefaultBlockParameter.valueOf(blockNumber)).send().getBalance();
                // this can go wrong apparently
                BigDecimal balanceEther = Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
                Output.printlnBold("Block: " + block.getNumber() + " balance: " + balanceEther + " ether");
            } catch (IOException ex) {
                balance = null;
            }
            for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : block.getTransactions()) {
                EthBlock.TransactionObject transactionObject = transactionResult.get();
                Transaction transaction = transactionObject.get();
                if (this.address.equals(transaction.getTo()) || this.address.equals(transaction.getFrom())) {
                    Output.println(10, "Transaction hash: " + transaction.getHash());
                    Output.println(20, "From: " + transaction.getFrom());
                    Output.println(20, "To: " + transaction.getTo());
                    if ("0x".equals(transaction.getInput())) {
                        BigDecimal valueEther = Convert.fromWei(new BigDecimal(transaction.getValue()), Convert.Unit.ETHER);
                        Output.println(20, "Value: " + valueEther + " ether");
                    } else {
                        Output.println(20, "Contract transaction: " + transaction.getInput());
                    }
                }
            }
        }, error -> {
            Output.error(error.getMessage());
            error.printStackTrace();
        });
        return null;
    }
}
