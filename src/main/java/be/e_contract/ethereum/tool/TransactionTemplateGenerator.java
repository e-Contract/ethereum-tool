/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

public class TransactionTemplateGenerator {

    private final File publicDirectory;

    public TransactionTemplateGenerator(File publicDirectory) {
        this.publicDirectory = publicDirectory;
    }

    public void generateTemplate(String address) throws IOException {
        if (null == this.publicDirectory) {
            return;
        }
        if (this.publicDirectory.exists()) {
            if (!this.publicDirectory.isDirectory()) {
                Output.error("public destination not a directory");
                return;
            }
        } else if (!this.publicDirectory.mkdirs()) {
            Output.error("could not create public destination directory");
            return;
        }
        File templateFile = new File(this.publicDirectory, "transaction-template-" + address + ".json");
        if (templateFile.exists()) {
            Console console = System.console();
            System.out.println("existing template file: " + templateFile.getName());
            boolean confirmation = askConfirmation(console, "Overwrite output file? (y/n)");
            if (!confirmation) {
                return;
            }
        }
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.description = "human readable description of the transaction";
        transactionTemplate.from = address;
        transactionTemplate.to = "place destination address here";
        transactionTemplate.chainId = 1; // Ethereum mainnet
        transactionTemplate.gasPrice = 3;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String transactionTemplateJSon = gson.toJson(transactionTemplate);
        FileUtils.writeStringToFile(templateFile, transactionTemplateJSon, "UTF-8");
        System.out.println("transaction template file: " + templateFile.getName());
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
