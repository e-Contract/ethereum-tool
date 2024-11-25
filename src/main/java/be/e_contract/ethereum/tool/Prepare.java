/*
 * Ethereum Tool project.
 * Copyright (C) 2024 e-Contract.be BV.
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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.Callable;
import org.apache.commons.io.FileUtils;
import org.web3j.crypto.Keys;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "prepare", description = "prepare a transaction", separator = " ")
public class Prepare implements Callable<Void> {

    @CommandLine.Option(names = {"-o", "--outfile"}, required = true, description = "the transaction template output file")
    private File outFile;

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-f", "--from"}, required = true, description = "the from address")
    private Address from;

    @CommandLine.Option(names = {"-t", "--to"}, required = true, description = "the to address")
    private Address to;

    @CommandLine.Option(names = {"-v", "--value"}, description = "the value to transfer in ether")
    private Double value;

    @CommandLine.Option(names = {"-e", "--empty"}, description = "take a value to empty the from address")
    private boolean[] empty;

    @Override
    public Void call() throws Exception {
        if (this.value == null && this.empty == null) {
            Output.error("Provide either --value or --empty");
            return null;
        }
        Console console = System.console();
        if (this.outFile.exists()) {
            System.out.println("Existing output file: " + this.outFile.getName());
            boolean confirmation = askConfirmation(console, "Overwrite output file? (y/n)");
            if (!confirmation) {
                return null;
            }
        }

        BigInteger chainId = this.web3.ethChainId().send().getChainId();

        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.description = "human readable description of the transaction";
        transactionTemplate.from = Keys.toChecksumAddress(this.from.getAddress());
        transactionTemplate.to = Keys.toChecksumAddress(this.to.getAddress());
        transactionTemplate.chainId = chainId.longValueExact();

        BigInteger gasPrice = this.web3.ethGasPrice().send().getGasPrice();
        BigInteger maxFeePerGas = gasPrice.multiply(BigInteger.valueOf(2));
        BigDecimal maxFeePerGasWei = new BigDecimal(maxFeePerGas);
        BigDecimal maxFeePerGasGwei = Convert.fromWei(maxFeePerGasWei, Convert.Unit.GWEI);
        transactionTemplate.maxFeePerGas = maxFeePerGasGwei.doubleValue();

        BigInteger maxPriorityFeePerGas = this.web3.ethMaxPriorityFeePerGas().send().getMaxPriorityFeePerGas();
        BigDecimal maxPriorityFeePerGasWei = new BigDecimal(maxPriorityFeePerGas);
        BigDecimal maxPriorityFeePerGasGwei = Convert.fromWei(maxPriorityFeePerGasWei, Convert.Unit.GWEI);
        transactionTemplate.maxPriorityFeePerGas = maxPriorityFeePerGasGwei.doubleValue();

        if (null != this.value) {
            transactionTemplate.value = this.value;
        } else {
            BigInteger balance = this.web3.ethGetBalance(this.from.getAddress(), DefaultBlockParameterName.LATEST).send().getBalance();
            BigInteger maxValue = balance.subtract(maxFeePerGas.multiply(BigInteger.valueOf(21000)));
            if (maxValue.compareTo(BigInteger.ZERO) < 0) {
                Output.error("Account already empty.");
                return null;
            }
            BigDecimal maxValueWei = new BigDecimal(maxValue);
            BigDecimal maxValueEther = Convert.fromWei(maxValueWei, Convert.Unit.ETHER);
            transactionTemplate.value = maxValueEther.doubleValue();
        }

        BigInteger transactionCount = this.web3.ethGetTransactionCount(this.from.getAddress(), DefaultBlockParameterName.LATEST).send().getTransactionCount();
        transactionTemplate.nonce = transactionCount.longValueExact();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String transactionTemplateJSon = gson.toJson(transactionTemplate);
        FileUtils.writeStringToFile(this.outFile, transactionTemplateJSon, "UTF-8");
        System.out.println("Transaction template file: " + this.outFile.getName());
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
