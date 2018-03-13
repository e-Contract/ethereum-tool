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
import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "display help", separator = " ")
public class Help implements Callable<Void> {

    @CommandLine.ParentCommand
    private Main main;

    @CommandLine.Parameters
    private String parameter;

    @Override
    public Void call() throws Exception {
        picocli.CommandLine.usage(this.main, System.out);
        return null;
    }
}
