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
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import picocli.CommandLine;

@CommandLine.Command(name = "node", description = "display node information", separator = " ")
public class Node implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private String location;

    @Override
    public Void call() throws Exception {
        Web3jService service;
        if (this.location.startsWith("http")) {
            service = new HttpService(this.location);
        } else {
            service = new UnixIpcService(this.location);
        }
        Web3j web3 = Web3j.build(service);
        boolean mining = web3.ethMining().send().isMining();
        System.out.println("Mining: " + mining);
        if (mining) {
            String coinbase = web3.ethCoinbase().send().getAddress();
            System.out.println("Coinbase address: " + coinbase);
            BigInteger hashRate = web3.ethHashrate().send().getHashrate();
            System.out.println("Hash rate: " + hashRate);
        }
        String protocolVersion = web3.ethProtocolVersion().send().getProtocolVersion();
        System.out.println("protocol version: " + protocolVersion);
        boolean syncing = web3.ethSyncing().send().isSyncing();
        if (syncing) {
            Output.error("Syncing: " + syncing);
        } else {
            System.out.println("Syncing: " + syncing);
        }
        String version = web3.netVersion().send().getNetVersion();
        System.out.println("Version: " + version);
        BigInteger peerCount = web3.netPeerCount().send().getQuantity();
        System.out.println("Peer count: " + peerCount);
        return null;
    }
}
