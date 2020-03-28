/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2019 e-Contract.be BVBA.
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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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

    @CommandLine.Option(names = {"-n", "--number"}, description = "the block number")
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

        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://api.coinmarketcap.com/v1/ticker/ethereum/?convert=EUR").build();
        Response response = httpClient.newCall(request).execute();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        CoinmarketcapTickerResponse[] coinmarketcapTickerResponse
                = objectMapper.readValue(response.body().byteStream(), CoinmarketcapTickerResponse[].class);
        BigDecimal priceUsd = BigDecimal.valueOf(coinmarketcapTickerResponse[0].priceUsd);
        BigDecimal priceEur = BigDecimal.valueOf(coinmarketcapTickerResponse[0].priceEur);
        System.out.println("Ether price: " + priceUsd + " USD");
        System.out.println("Ether price: " + priceEur + " EUR");

        BigDecimal balanceUsd = balanceEther.multiply(priceUsd);
        BigDecimal balanceEur = balanceEther.multiply(priceEur);
        System.out.println("Balance: " + balanceUsd + " USD");
        System.out.println("Balance: " + balanceEur + " EUR");

        return null;
    }
}
