/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "version", description = "display version", separator = " ")
public class VersionCommand implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        System.out.println("Version: " + Version.getImplementationVersion());
        return null;
    }
}
