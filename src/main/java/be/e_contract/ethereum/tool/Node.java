/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import picocli.CommandLine;

@CommandLine.Command(name = "node", description = "display node information", separator = " ")
public class Node implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @Override
    public Void call() throws Exception {
        boolean mining = this.web3.ethMining().send().isMining();
        System.out.println("Mining: " + mining);
        if (mining) {
            String coinbase = this.web3.ethCoinbase().send().getAddress();
            System.out.println("Coinbase address: " + coinbase);
            BigInteger hashRate = this.web3.ethHashrate().send().getHashrate();
            System.out.println("Hash rate: " + hashRate);
        }
        String protocolVersion = this.web3.ethProtocolVersion().send().getProtocolVersion();
        System.out.println("protocol version: " + protocolVersion);
        boolean syncing = this.web3.ethSyncing().send().isSyncing();
        System.out.println("Syncing: " + syncing);
        String version = this.web3.netVersion().send().getNetVersion();
        System.out.println("Version: " + version);
        BigInteger peerCount = this.web3.netPeerCount().send().getQuantity();
        System.out.println("Peer count: " + peerCount);
        return null;
    }
}
