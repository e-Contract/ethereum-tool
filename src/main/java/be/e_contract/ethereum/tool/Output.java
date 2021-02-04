/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2021 e-Contract.be BV.
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

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

public class Output {

    static {
        AnsiConsole.systemInstall();
    }

    public static void error(String message) {
        AnsiConsole.out().print(Ansi.ansi().reset().fg(Ansi.Color.RED));
        System.out.println(message);
        AnsiConsole.out().print(Ansi.ansi().reset());
    }

    public static void warning(String message) {
        AnsiConsole.out().print(Ansi.ansi().reset().fg(Ansi.Color.YELLOW));
        System.out.println(message);
        AnsiConsole.out().print(Ansi.ansi().reset());
    }

    public static void println(int indent, String message) {
        AnsiConsole.out().print(Ansi.ansi().reset().cursorRight(indent));
        System.out.println(message);
        AnsiConsole.out().print(Ansi.ansi().reset());
    }

    public static void printlnBold(String message) {
        AnsiConsole.out().print(Ansi.ansi().reset().bold());
        System.out.println(message);
        AnsiConsole.out().print(Ansi.ansi().reset());
    }
}
