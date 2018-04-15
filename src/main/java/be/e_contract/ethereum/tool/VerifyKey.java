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

import java.io.Console;
import java.io.File;
import java.util.concurrent.Callable;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "verifykey", description = "verify an Ethereum key", separator = " ")
public class VerifyKey implements Callable<Void> {

    @CommandLine.Option(names = {"-f", "--keyfile"}, required = true, description = "the key file")
    private File keyFile;

    @CommandLine.Option(names = {"-t", "--templatedirectory"}, description = "the optional public transaction template directory")
    private File publicDirectory;

    @Override
    public Void call() throws Exception {
        if (!this.keyFile.exists()) {
            Output.error("Non existing key file");
            return null;
        }
        Console console = System.console();
        char[] password = console.readPassword("Passphrase: ");
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(new String(password), this.keyFile);
        } catch (CipherException ex) {
            Output.error("Incorrect passphrase");
            return null;
        }
        String address = credentials.getAddress();
        System.out.println("Address: " + address);
        String checksumAddress = Keys.toChecksumAddress(address);
        System.out.println("Address (checksum): " + checksumAddress);
        if (null != this.publicDirectory) {
            TransactionTemplateGenerator transactionTemplateGenerator = new TransactionTemplateGenerator(this.publicDirectory);
            transactionTemplateGenerator.generateTemplate(address);
        }
        return null;
    }
}
