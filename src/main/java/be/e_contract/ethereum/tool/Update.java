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
import java.util.Arrays;
import java.util.concurrent.Callable;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "update", description = "update the password on a key file", separator = " ")
public class Update implements Callable<Void> {
    
    @CommandLine.Option(names = {"-f", "--keyfile"}, required = true, description = "the key file")
    private File keyFile;
    
    @Override
    public Void call() throws Exception {
        if (!this.keyFile.exists()) {
            Output.error("non existing key file");
            return null;
        }
        Console console = System.console();
        char[] currentPassword = console.readPassword("Passphrase: ");
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(new String(currentPassword), this.keyFile);
        } catch (CipherException ex) {
            Output.error("incorrect passphrase");
            return null;
        }
        
        char[] password = console.readPassword("New passphrase:");
        char[] password2 = console.readPassword("Repeat new passphrase:");
        if (!Arrays.equals(password, password2)) {
            Output.error("Passphrase mismatch");
            return null;
        }
        
        String newKeyfile = WalletUtils.generateWalletFile(new String(password), credentials.getEcKeyPair(), this.keyFile.getParentFile(), true);
        System.out.println("new key file: " + newKeyfile);
        
        Output.warning("Old key file: " + this.keyFile.getAbsolutePath());
        Output.warning("Old key file you have to remove yourself.");
        
        return null;
    }
}
