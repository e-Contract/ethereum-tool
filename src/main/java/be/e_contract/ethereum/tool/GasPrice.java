/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2020 e-Contract.be BV.
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
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "gasprice", description = "retrieve the gas price", separator = " ")
public class GasPrice implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @Override
    public Void call() throws Exception {
        // calculates on latest blocks median gas price
        BigDecimal gasPriceWei = BigDecimal.valueOf(this.web3.ethGasPrice().send().getGasPrice().longValueExact());
        System.out.println("Gas price: " + gasPriceWei + " wei");
        BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
        System.out.println("Gas price: " + gasPriceGwei + " Gwei");

        BigInteger blockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
        System.out.println("Gas limit: " + block.getGasLimit() + " wei");

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

        BigDecimal gasUsed = BigDecimal.valueOf(21000);
        System.out.println("Gas used on regular transaction: " + gasUsed);
        BigDecimal gasPriceEther = Convert.fromWei(gasPriceWei, Convert.Unit.ETHER);
        BigDecimal costEther = gasUsed.multiply(gasPriceEther);
        BigDecimal costUsd = costEther.multiply(priceUsd);
        BigDecimal costEur = costEther.multiply(priceEur);
        System.out.println("Cost regular transaction: " + costUsd + " USD");
        System.out.println("Cost regular transaction: " + costEur + " EUR");
        Output.warning("This displayed gas price is the price reported by the node itself.");
        Output.warning("This is not necessarily the sharpest price possible on the network.");
        return null;
    }
}
