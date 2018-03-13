/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.math.BigInteger;
import java.util.Date;
import org.joda.time.DateTime;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import picocli.CommandLine;

public class Web3TypeConverter implements CommandLine.ITypeConverter<Web3j> {

    @Override
    public Web3j convert(String location) throws Exception {
        Web3j web3;
        try {
            Web3jService service;
            if (location.startsWith("http")) {
                service = new HttpService(location);
            } else {
                service = new UnixIpcService(location);
            }
            web3 = Web3j.build(service);
        } catch (Exception e) {
            Output.error("Could not connect to node: " + location);
            Output.error("Error: " + e.getMessage());
            System.exit(1);
            throw new RuntimeException(e);
        }
        if (web3.ethSyncing().send().isSyncing()) {
            Output.warning("Node is still syncing.");
            Output.warning("Results will be inaccurate!");
        }
        BigInteger peerCount = web3.netPeerCount().send().getQuantity();
        if (BigInteger.ZERO.equals(peerCount)) {
            Output.warning("Node has no peers.");
            Output.warning("Node probably just started.");
            Output.warning("Results will be inaccurate!");
        }
        EthBlock.Block block = web3.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock();
        BigInteger timestamp = block.getTimestamp();
        Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
        DateTime timestampDateTime = new DateTime(timestampDate);
        DateTime now = new DateTime();
        if (timestampDateTime.plusMinutes(1).isBefore(now)) {
            Output.warning("latest block is more than 1 minute old.");
            Output.warning("Node might be out-of-sync.");
            Output.warning("Results might be inaccurate.");
        }
        return web3;
    }
}
