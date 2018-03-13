/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
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
