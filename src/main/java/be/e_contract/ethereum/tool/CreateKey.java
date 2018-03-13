/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import picocli.CommandLine;

@CommandLine.Command(name = "createkey", description = "create a new Ethereum key", separator = " ")
public class CreateKey implements Callable<Void> {

    @CommandLine.Option(names = {"-d", "--keydirectory"}, required = true, description = "the key directory")
    private File keyDirectory;

    @CommandLine.Option(names = {"-t", "--templatedirectory"}, description = "the optional public transaction template directory")
    private File publicDirectory;

    @Override
    public Void call() throws Exception {
        Console console = System.console();
        char[] password = console.readPassword("Passphrase:");
        char[] password2 = console.readPassword("Repeat passphrase:");
        if (!Arrays.equals(password, password2)) {
            Output.error("Passphrase mismatch");
            return null;
        }
        if (this.keyDirectory.exists()) {
            if (!this.keyDirectory.isDirectory()) {
                Output.error("destination not a directory");
                return null;
            }
        } else if (!this.keyDirectory.mkdirs()) {
            Output.error("could not create destination directory");
            return null;
        } else {
            Set<PosixFilePermission> permissions = new HashSet<>();
            permissions.add(PosixFilePermission.OWNER_READ);
            permissions.add(PosixFilePermission.OWNER_WRITE);
            permissions.add(PosixFilePermission.OWNER_EXECUTE);
            Files.setPosixFilePermissions(this.keyDirectory.toPath(), permissions);
        }
        String keyfile = WalletUtils.generateNewWalletFile(new String(password), this.keyDirectory, true);
        System.out.println("key file: " + keyfile);
        File keyFile = new File(this.keyDirectory, keyfile);
        Credentials credentials = WalletUtils.loadCredentials(new String(password), keyFile);
        String address = credentials.getAddress();
        System.out.println("address: " + address);
        if (null != this.publicDirectory) {
            TransactionTemplateGenerator transactionTemplateGenerator = new TransactionTemplateGenerator(this.publicDirectory);
            transactionTemplateGenerator.generateTemplate(address);
        }
        return null;
    }
}
