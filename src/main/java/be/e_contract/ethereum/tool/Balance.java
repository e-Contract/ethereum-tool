/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "balance", description = "retrieve the balance", separator = " ")
public class Balance implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private String location;

    @CommandLine.Option(names = {"-a", "--address"}, required = true, description = "the key address")
    private String address;

    @Override
    public Void call() throws Exception {
        Web3jService service;
        if (this.location.startsWith("http")) {
            service = new HttpService(this.location);
        } else {
            service = new UnixIpcService(this.location);
        }
        Web3j web3 = Web3j.build(service);
        BigInteger transactionCount = web3.ethGetTransactionCount(this.address, DefaultBlockParameterName.LATEST).send().getTransactionCount();
        System.out.println("transaction count: " + transactionCount);
        BigInteger balance = web3.ethGetBalance(this.address, DefaultBlockParameterName.LATEST).send().getBalance();
        BigDecimal balanceEther = Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
        System.out.println("balance: " + balanceEther + " ether");
        return null;
    }
}
