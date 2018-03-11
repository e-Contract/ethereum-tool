/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.ipc.UnixIpcService;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "gasprice", description = "retrieve the average gas price", separator = " ")
public class GasPrice implements Callable<Void> {

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
        BigDecimal gasPriceWei = BigDecimal.valueOf(web3.ethGasPrice().send().getGasPrice().longValueExact());
        System.out.println("gas price: " + gasPriceWei + " wei");
        BigDecimal gasPriceGwei = Convert.fromWei(gasPriceWei, Convert.Unit.GWEI);
        System.out.println("gas price: " + gasPriceGwei + " Gwei");
        return null;
    }
}
