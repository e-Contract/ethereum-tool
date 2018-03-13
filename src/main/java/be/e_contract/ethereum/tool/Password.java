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
import org.apache.commons.lang3.RandomStringUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "password", description = "generate a strong password", separator = " ")
public class Password implements Callable<Void> {

    @Override
    public Void call() throws Exception {
        String password = "";
        for (int idx = 0; idx < 32 / 4; idx++) {
            if (!password.isEmpty()) {
                // make it readable
                password += "-";
            }
            password += RandomStringUtils.randomAlphanumeric(4);
        }
        System.out.println(password);
        return null;
    }
}
