/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
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
