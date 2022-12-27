/*
 * Ethereum Tool project.
 * Copyright (C) 2022 e-Contract.be BV.
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
import picocli.CommandLine;

@CommandLine.Command(name = "price", description = "get the current ethereum exchange rates", separator = " ")
public class Price implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        EthereumRates ethereumRates = new EthereumRates();
        double eurRate = ethereumRates.getEuro(1);
        double dollarRate = ethereumRates.getDollar(1);
        System.out.println("1 ETH = " + eurRate + " EUR");
        System.out.println("1 ETH = " + dollarRate + " USD");
        return null;
    }
}
