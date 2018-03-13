/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.IOException;
import java.util.Properties;

public class Version {

    private final static String IMPLEMENTATION_VERSION;

    static {
        Properties properties = new Properties();
        try {
            properties.load(Version.class
                    .getResourceAsStream("/ethereum-tool-version.properties"));
        } catch (IOException e) {
            throw new RuntimeException("could not load ethereum-tool-version.properties");
        }
        IMPLEMENTATION_VERSION = properties.getProperty("implementation.version");
    }

    public static String getImplementationVersion() {
        return IMPLEMENTATION_VERSION;
    }
}
