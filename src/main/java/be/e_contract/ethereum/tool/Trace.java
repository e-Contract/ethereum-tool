/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2023 e-Contract.be BV.
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

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "trace", description = "Realtime trace transactions on an address", separator = " ")
public class Trace implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-a", "--address"}, required = true, description = "the key address")
    private Address address;

    private Disposable blockDisposable;

    @Override
    public Void call() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (null != this.blockDisposable) {
                this.blockDisposable.dispose();
            }
        }));
        System.out.println("Address: " + this.address.getAddress());
        BigInteger initialBlockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        BigInteger initialBalance = this.web3.ethGetBalance(this.address.getAddress(), DefaultBlockParameter.valueOf(initialBlockNumber)).send().getBalance();
        BigDecimal initialBalanceEther = Convert.fromWei(new BigDecimal(initialBalance), Convert.Unit.ETHER);
        Output.printlnBold("Block: " + initialBlockNumber + " balance: " + initialBalanceEther + " ether");
        EthBlock pendingEthBlock = this.web3.ethGetBlockByNumber(DefaultBlockParameterName.PENDING, true).send();
        EthBlock.Block pendingBlock = pendingEthBlock.getBlock();
        for (EthBlock.TransactionResult transactionResult : pendingBlock.getTransactions()) {
            EthBlock.TransactionObject transactionObject = (EthBlock.TransactionObject) transactionResult;
            Transaction transaction = transactionObject.get();
            if (this.address.getAddress().equals(transaction.getTo()) || this.address.getAddress().equals(transaction.getFrom())) {
                Output.println(10, "Pending transaction hash: " + transaction.getHash());
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
        this.blockDisposable = this.web3.blockFlowable(true).subscribe((EthBlock ethBlock) -> {
            EthBlock.Block block = ethBlock.getBlock();
            BigInteger blockNumber = block.getNumber();
            try {
                BigInteger balance = this.web3.ethGetBalance(this.address.getAddress(), DefaultBlockParameter.valueOf(blockNumber)).send().getBalance();
                // this can go wrong apparently
                BigDecimal balanceEther = Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
                Output.printlnBold("Block: " + block.getNumber() + " balance: " + balanceEther + " ether");
            } catch (IOException ex) {
                // silence here
            }
            for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : block.getTransactions()) {
                EthBlock.TransactionObject transactionObject = transactionResult.get();
                Transaction transaction = transactionObject.get();
                if (this.address.getAddress().equals(transaction.getTo()) || this.address.getAddress().equals(transaction.getFrom())) {
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
