/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2023 e-Contract.be BV.
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

import com.google.gson.Gson;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import picocli.CommandLine;

@CommandLine.Command(name = "sign", description = "sign a transaction", separator = " ")
public class Sign implements Callable<Void> {

    @CommandLine.Option(names = {"-o", "--outfile"}, required = true, description = "the transaction output file")
    private File outFile;

    @CommandLine.Option(names = {"-t", "--template"}, required = true, description = "the transaction template file")
    private File templateFile;

    @CommandLine.Option(names = {"-f", "--keyfile"}, required = true, description = "the key file")
    private File keyFile;

    @Override
    public Void call() throws Exception {
        Console console = System.console();
        if (this.outFile.exists()) {
            System.out.println("Existing output file: " + this.outFile.getName());
            boolean confirmation = askConfirmation(console, "Overwrite output file? (y/n)");
            if (!confirmation) {
                return null;
            }
        }
        if (!this.templateFile.exists()) {
            Output.error("Template file does not exist");
            return null;
        }
        if (!this.keyFile.exists()) {
            Output.error("Non existing key file");
            return null;
        }
        Gson gson = new Gson();
        TransactionTemplate transactionTemplate = gson.fromJson(new FileReader(this.templateFile), TransactionTemplate.class);
        if (null != transactionTemplate.description) {
            System.out.println("Description: " + transactionTemplate.description);
        }
        System.out.println("From: " + transactionTemplate.from);
        System.out.println("To: " + transactionTemplate.to);
        System.out.println("Value: " + transactionTemplate.value + " ether");
        System.out.println("Gas price: " + transactionTemplate.gasPrice + " Gwei");
        System.out.println("Nonce: " + transactionTemplate.nonce);
        if (!WalletUtils.isValidAddress(transactionTemplate.to)) {
            Output.error("Invalid address: " + transactionTemplate.to);
            return null;
        }
        if (!transactionTemplate.to.toLowerCase().equals(transactionTemplate.to)) {
            if (!Keys.toChecksumAddress(transactionTemplate.to).equals(transactionTemplate.to)) {
                Output.error("Address checksum error: " + transactionTemplate.to);
                return null;
            }
        }
        boolean confirmation = askConfirmation(console, "Sign transaction? (y/n)");
        if (!confirmation) {
            return null;
        }
        char[] password = console.readPassword("Passphrase: ");
        Credentials credentials;
        try {
            credentials = WalletUtils.loadCredentials(new String(password), this.keyFile);
        } catch (CipherException ex) {
            Output.error("Incorrect passphrase");
            return null;
        }
        String address = credentials.getAddress();
        System.out.println("From address: " + credentials.getAddress());
        if (transactionTemplate.from != null) {
            String from = transactionTemplate.from.toLowerCase();
            if (!from.equals(address)) {
                Output.error("From address mismatch");
                return null;
            }
        }
        confirmation = askConfirmation(console, "Confirm from address? (y/n)");
        if (!confirmation) {
            return null;
        }
        BigInteger nonce = BigInteger.valueOf(transactionTemplate.nonce);
        BigDecimal gasPriceGwei = BigDecimal.valueOf(transactionTemplate.gasPrice);
        BigDecimal gasPriceWei = Convert.toWei(gasPriceGwei, Convert.Unit.GWEI);
        BigDecimal valueEther = BigDecimal.valueOf(transactionTemplate.value);
        BigDecimal valueWei = Convert.toWei(valueEther, Convert.Unit.ETHER);
        BigInteger gasLimit = BigInteger.valueOf(21000);
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPriceWei.toBigIntegerExact(),
                gasLimit, transactionTemplate.to, valueWei.toBigIntegerExact());
        byte[] signedTransaction;
        if (null != transactionTemplate.chainId) {
            System.out.println("Chain Id: " + transactionTemplate.chainId);
            signedTransaction = TransactionEncoder.signMessage(rawTransaction, transactionTemplate.chainId, credentials);
        } else {
            signedTransaction = TransactionEncoder.signMessage(rawTransaction, credentials);
        }
        String hexValue = Numeric.toHexString(signedTransaction);
        String transactionHash = Hash.sha3(hexValue);
        System.out.println("Transaction hash: " + transactionHash);

        FileUtils.writeStringToFile(this.outFile, hexValue, "UTF-8");
        return null;
    }

    private boolean askConfirmation(Console console, String message) {
        while (true) {
            String confirmation = console.readLine(message);
            if ("y".equals(confirmation)) {
                return true;
            }
            if ("n".equals(confirmation)) {
                return false;
            }
        }
    }
}
