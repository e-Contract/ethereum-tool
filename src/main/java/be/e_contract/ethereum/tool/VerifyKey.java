/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.Console;
import java.io.File;
import java.util.concurrent.Callable;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "verifykey", description = "verify an Ethereum key", separator = " ")
public class VerifyKey implements Callable<Void> {

    @CommandLine.Option(names = {"-f", "--keyfile"}, required = true, description = "the key file")
    private File keyFile;

    @Override
    public Void call() throws Exception {
        if (!this.keyFile.exists()) {
            System.out.println("non existing key file");
            return null;
        }
        Console console = System.console();
        char[] password = console.readPassword("Passphrase: ");
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(new String(password), this.keyFile);
        } catch (CipherException ex) {
            System.out.println("incorrect passphrase");
            return null;
        }
        System.out.println("address: " + credentials.getAddress());
        return null;
    }
}
