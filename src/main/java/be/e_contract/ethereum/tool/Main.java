/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2019 e-Contract.be BVBA.
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
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.web3j.protocol.Web3j;

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
            Pending.class,
            History.class,
            Update.class,
            ENS.class,
            Speed.class,
            Miners.class,
            Trace.class
        },
        versionProvider = VersionProvider.class,
        footer = "Copyright (C) 2018 Frank Cornelis\nDonations: " + Version.DONATION,
        separator = " "
)
public class Main implements Callable<Void> {

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                AnsiConsole.out.print(Ansi.ansi().reset());
                System.out.println();
            }

        });
        picocli.CommandLine commandLine = new picocli.CommandLine(new Main());
        commandLine.registerConverter(Web3j.class, new Web3TypeConverter());
        commandLine.registerConverter(Address.class, new AddressTypeConverter());
        commandLine.parseWithHandler(new picocli.CommandLine.RunLast(), args);
    }

    @Override
    public Void call() throws Exception {
        picocli.CommandLine.usage(this, System.out);
        return null;
    }
}
