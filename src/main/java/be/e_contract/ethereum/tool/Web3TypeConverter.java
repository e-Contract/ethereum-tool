/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
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
        return web3;
    }
}
