/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class Output {

    static {
        AnsiConsole.systemInstall();
    }

    public static void error(String message) {
        AnsiConsole.out.print(Ansi.ansi().reset().fg(Ansi.Color.RED));
        System.out.println(message);
        AnsiConsole.out.print(Ansi.ansi().reset());
    }
}
