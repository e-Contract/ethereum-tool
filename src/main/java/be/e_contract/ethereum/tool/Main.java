/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.util.concurrent.Callable;

/**
 * Ethereum Tool main class.
 *
 * @author Frank Cornelis
 */
@picocli.CommandLine.Command(name = "ethereum-tool",
        description = "Tool to manage offline template-based transaction signing.",
        subcommands = {
            CreateKey.class,
            VerifyKey.class,
            Nonce.class,
            GasPrice.class,
            Password.class,
            Sign.class,
            Inspect.class,
            Transmit.class,
            Help.class,
            Confirm.class,
            Balance.class,
            Node.class,
            VersionCommand.class,
            Pending.class
        },
        versionProvider = VersionProvider.class,
        footer = "Copyright (C) 2018 e-Contract.be BVBA",
        separator = " "
)
public class Main implements Callable<Void> {

    public static void main(String[] args) throws Exception {
        picocli.CommandLine commandLine = new picocli.CommandLine(new Main());
        commandLine.parseWithHandler(new picocli.CommandLine.RunLast(), System.out, args);
    }

    @Override
    public Void call() throws Exception {
        picocli.CommandLine.usage(this, System.out);
        return null;
    }
}
