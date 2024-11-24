/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2024 e-Contract.be BV.
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
        System.out.println("Gas price per unit: " + gasPriceWei + " wei");
        BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
        System.out.println("Gas price per unit: " + gasPriceGwei + " Gwei");

        BigInteger blockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
        BigDecimal baseFeePerGasWei = BigDecimal.valueOf(block.getBaseFeePerGas().longValueExact());
        System.out.println("Base fee per gas: " + baseFeePerGasWei + " wei");
        BigDecimal baseFeePerGasGwei = Convert.fromWei(baseFeePerGasWei, Convert.Unit.GWEI);
        System.out.println("Base fee per gas: " + baseFeePerGasGwei + " Gwei");
        BigDecimal priorityFeeWei = gasPriceWei.subtract(baseFeePerGasWei);
        BigDecimal priorityFeeGwei = Convert.fromWei(priorityFeeWei, Convert.Unit.GWEI);
        System.out.println("Priority fee (tip): " + priorityFeeGwei + " Gwei");
        System.out.println("Gas limit: " + block.getGasLimit() + " gas units");

        BigDecimal gasUsed = BigDecimal.valueOf(21000);
        System.out.println("Gas used on regular transaction: " + gasUsed + " units");
        BigDecimal gasPriceEther = Convert.fromWei(gasPriceWei, Convert.Unit.ETHER);
        BigDecimal costEther = gasUsed.multiply(gasPriceEther);
        System.out.println("Cost regular transaction: " + costEther + " ETH");

        EthereumRates ethereumRates = new EthereumRates();
        BigDecimal costUsd = ethereumRates.getDollar(costEther);
        BigDecimal costEur = ethereumRates.getEuro(costEther);
        System.out.println("Cost regular transaction: " + costUsd + " USD");
        System.out.println("Cost regular transaction: " + costEur + " EUR");

        Output.warning("This displayed gas price is the price reported by the node itself.");
        Output.warning("This is not necessarily the sharpest price possible on the network.");
        return null;
    }
}
