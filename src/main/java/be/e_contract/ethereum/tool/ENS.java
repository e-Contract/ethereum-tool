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

import java.util.concurrent.Callable;
import org.web3j.ens.EnsResolver;
import org.web3j.protocol.Web3j;
import picocli.CommandLine;

@CommandLine.Command(name = "ens", description = "Ethereum Name Service resolver", separator = " ")
public class ENS implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-n", "--name"}, description = "the ENS name")
    private String name;

    @CommandLine.Option(names = {"-a", "--address"}, description = "the ethereum address")
    private Address address;

    @Override
    public Void call() throws Exception {
        if (this.name == null && this.address == null) {
            Output.error("Provide name or address");
            picocli.CommandLine.usage(this, System.out);
            return null;
        }
        EnsResolver ensResolver = new EnsResolver(this.web3);
        if (null != this.address) {
            String name = ensResolver.reverseResolve(this.address.getAddress());
            System.out.println("Name: " + name);
            return null;
        }

        if (null != this.name) {
            String address = ensResolver.resolve(this.name);
            System.out.println("Address: " + address);
            return null;
        }
        return null;
    }
}
