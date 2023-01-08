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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "node", description = "display node information", separator = " ")
public class Node implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @Override
    public Void call() throws Exception {
        String clientVersion = this.web3.web3ClientVersion().send().getWeb3ClientVersion();
        if (null != clientVersion) {
            System.out.println("Client version: " + clientVersion);
        }
        boolean mining = this.web3.ethMining().send().isMining();
        System.out.println("Mining: " + mining);
        if (mining) {
            String coinbase = this.web3.ethCoinbase().send().getAddress();
            System.out.println("Coinbase address: " + coinbase);
            BigInteger hashRate = this.web3.ethHashrate().send().getHashrate();
            System.out.println("Hash rate: " + hashRate);
        }
        boolean syncing = this.web3.ethSyncing().send().isSyncing();
        System.out.println("Syncing: " + syncing);
        String version = this.web3.netVersion().send().getNetVersion();
        System.out.println("Network version: " + version);
        BigInteger peerCount = this.web3.netPeerCount().send().getQuantity();
        System.out.println("Peer count: " + peerCount);
        BigInteger chainId = this.web3.ethChainId().send().getChainId();
        if (null != chainId) {
            System.out.println("Chain id: " + chainId);
        }
        BigInteger blockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        System.out.println("Latest block: " + blockNumber);
        EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true).send().getBlock();
        double percentageGasUsed = (double) block.getGasUsed().longValueExact() / block.getGasLimit().longValueExact();
        System.out.println("Gas used: " + block.getGasUsed() + " wei");
        System.out.println("Gas limit: " + block.getGasLimit() + " wei (" + percentageGasUsed + " %)");
        BigDecimal baseFeePerGas = new BigDecimal(block.getBaseFeePerGas());
        BigDecimal baseFeePerGasGwei = Convert.fromWei(baseFeePerGas, Convert.Unit.GWEI);
        System.out.println("Base Fee: " + baseFeePerGasGwei + " Gwei");
        return null;
    }
}
