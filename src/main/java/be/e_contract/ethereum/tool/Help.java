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

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import picocli.CommandLine;

@CommandLine.Command(name = "help", description = "display help", separator = " ", helpCommand = true)
public class Help implements Callable<Void>, CommandLine.IHelpCommandInitializable {

    @CommandLine.ParentCommand
    private Main main;

    @CommandLine.Parameters
    private List<String> parameters;

    private PrintStream out;

    private CommandLine parent;

    private CommandLine.Help.Ansi ansi;

    @Override
    public Void call() throws Exception {
        if (null == this.parameters) {
            picocli.CommandLine.usage(this.main, this.out);
        } else {
            String command = this.parameters.get(0);
            Map<String, CommandLine> subcommands = this.parent.getSubcommands();
            CommandLine subCommand = subcommands.get(command);
            if (null == subCommand) {
                Output.error("Unknown command: " + command);
            } else {
                subCommand.usage(this.out, this.ansi);
            }
        }
        return null;
    }

    @Override
    public void init(CommandLine helpCommandLine, CommandLine.Help.Ansi ansi, PrintStream out, PrintStream err) {
        this.out = out;
        this.parent = helpCommandLine.getParent();
        this.ansi = ansi;
    }
}
