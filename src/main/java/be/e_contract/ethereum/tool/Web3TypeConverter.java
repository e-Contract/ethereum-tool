/*
 * Ethereum Tool project.
 * Copyright (C) 2018 e-Contract.be BVBA.
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

import java.math.BigInteger;
import java.util.Date;
import org.joda.time.DateTime;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Async;
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
                // https://github.com/web3j/web3j/pull/245
                Output.warning("web3j IPC is not really stable");
                service = new UnixIpcService(location);
            }
            // poll every hald second
            web3 = Web3j.build(service, 500, Async.defaultExecutorService());
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
