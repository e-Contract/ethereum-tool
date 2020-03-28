/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2020 e-Contract.be BV.
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
                Output.error("Public destination not a directory");
                return;
            }
        } else if (!this.publicDirectory.mkdirs()) {
            Output.error("Could not create public destination directory");
            return;
        }
        File templateFile = new File(this.publicDirectory, "transaction-template-" + address + ".json");
        if (templateFile.exists()) {
            Console console = System.console();
            System.out.println("Existing template file: " + templateFile.getName());
            boolean confirmation = askConfirmation(console, "Overwrite output file? (y/n)");
            if (!confirmation) {
                return;
            }
        }
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.description = "human readable description of the transaction";
        transactionTemplate.from = address;
        transactionTemplate.to = "place destination address here";
        transactionTemplate.chainId = 1L; // Ethereum mainnet
        transactionTemplate.gasPrice = 2;
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String transactionTemplateJSon = gson.toJson(transactionTemplate);
        FileUtils.writeStringToFile(templateFile, transactionTemplateJSon, "UTF-8");
        System.out.println("Transaction template file: " + templateFile.getName());
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
