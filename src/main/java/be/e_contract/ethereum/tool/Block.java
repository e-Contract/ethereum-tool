/*
 * Ethereum Tool project.
 * Copyright (C) 2019 e-Contract.be BVBA.
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

import java.math.BigInteger;
import java.util.Date;
import java.util.concurrent.Callable;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import picocli.CommandLine;

@CommandLine.Command(name = "block", description = "inspect a block", separator = " ")
public class Block implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-h", "--hash"}, description = "the block hash")
    private String blockHash;

    @CommandLine.Option(names = {"-n", "--number"}, description = "the block number")
    private BigInteger blockNumber;

    @Override
    public Void call() throws Exception {
        if (null == this.blockHash && null == this.blockNumber) {
            Output.error("Provide block number or block hash");
            picocli.CommandLine.usage(this, System.out);
            return null;
        }
        if (null != this.blockNumber) {
            EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(this.blockNumber), true).send().getBlock();
            printBlock(block);
            return null;
        }
        EthBlock.Block block = this.web3.ethGetBlockByHash(this.blockHash, true).send().getBlock();
        printBlock(block);
        return null;
    }

    private void printBlock(EthBlock.Block block) throws Exception {
        if (null == block) {
            Output.error("Unknown block number: " + this.blockNumber);
            return;
        }
        System.out.println("block number: " + block.getNumber());
        System.out.println("block hash: " + block.getHash());
        BigInteger blockTimestamp = block.getTimestamp();
        Date blockTimestampDate = new Date(blockTimestamp.multiply(BigInteger.valueOf(1000)).longValue());
        System.out.println("block timestamp: " + blockTimestampDate);
        System.out.println("number of transactions: " + block.getTransactions().size());
        for (String uncle : block.getUncles()) {
            System.out.println("block uncle: " + uncle);
        }
        System.out.println("parent hash: " + block.getParentHash());
        System.out.println("SHA3Uncles: " + block.getSha3Uncles());
    }
}
