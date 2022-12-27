/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2022 e-Contract.be BV.
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
import java.util.concurrent.Callable;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "balance", description = "retrieve the balance", separator = " ")
public class Balance implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-a", "--address"}, required = true, description = "the key address")
    private Address address;

    @CommandLine.Option(names = {"-n", "--number"}, description = "the optional block number")
    private BigInteger blockNumber;

    @Override
    public Void call() throws Exception {
        System.out.println("Address: " + this.address.getAddress());
        String checksumAddress = Keys.toChecksumAddress(this.address.getAddress());
        System.out.println("Address (checksum): " + checksumAddress);
        BigInteger balanceBlockNumber;
        if (null != this.blockNumber) {
            balanceBlockNumber = this.blockNumber;
        } else {
            balanceBlockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        }
        System.out.println("Block number: " + balanceBlockNumber);
        EthGetTransactionCount ethGetTransactionCount = this.web3.ethGetTransactionCount(this.address.getAddress(), DefaultBlockParameter.valueOf(balanceBlockNumber)).send();
        if (ethGetTransactionCount.hasError()) {
            Output.error("Could not retrieve transaction count for block " + balanceBlockNumber);
            Output.error(ethGetTransactionCount.getError().getMessage());
            return null;
        }
        BigInteger transactionCount = ethGetTransactionCount.getTransactionCount();
        System.out.println("Transaction count: " + transactionCount);
        BigInteger balance = this.web3.ethGetBalance(this.address.getAddress(), DefaultBlockParameter.valueOf(balanceBlockNumber)).send().getBalance();
        BigDecimal balanceEther = Convert.fromWei(new BigDecimal(balance), Convert.Unit.ETHER);
        System.out.println("Balance: " + balanceEther + " ether");

        return null;
    }
}
