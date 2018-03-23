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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "miners", description = "Show information on miners", separator = " ")
public class Miners implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    @CommandLine.Option(names = {"-n", "--blocks"}, required = true, description = "number of blocks from latest to scan")
    private int n;

    @Override
    public Void call() throws Exception {
        BigInteger blockNumber = this.web3.ethBlockNumber().send().getBlockNumber();
        System.out.println("scanning from block " + blockNumber + " down to block " + blockNumber.subtract(BigInteger.valueOf(this.n)) + " ...");
        Map<String, Miner> miners = new HashMap<>();
        int count = this.n;
        while (count > 0) {
            EthBlock.Block block = this.web3.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), false).send().getBlock();
            String minerAddress = block.getMiner();
            Miner miner = miners.get(minerAddress);
            if (null == miner) {
                miner = new Miner(minerAddress);
                miners.put(minerAddress, miner);
            } else {
                miner.addBlock();
            }
            count--;
            blockNumber = blockNumber.subtract(BigInteger.ONE);
        }
        List<Miner> minerList = new ArrayList<>(miners.values());
        minerList.sort((o1, o2) -> Integer.compare(o2.getBlocks(), o1.getBlocks()));
        System.out.print("number of blocks");
        AnsiConsole.out.print(Ansi.ansi().cursorToColumn(20));
        System.out.print("% of blocks");
        AnsiConsole.out.print(Ansi.ansi().cursorToColumn(40));
        System.out.print("miner");
        AnsiConsole.out.print(Ansi.ansi().cursorToColumn(90));
        System.out.println("balance (ether)");
        for (Miner miner : minerList) {
            System.out.print(miner.blocks);
            AnsiConsole.out.print(Ansi.ansi().cursorToColumn(20));
            System.out.print((double) miner.blocks / this.n * 100);
            AnsiConsole.out.print(Ansi.ansi().cursorToColumn(40));
            System.out.print(miner.address);
            AnsiConsole.out.print(Ansi.ansi().cursorToColumn(90));
            BigDecimal balance = new BigDecimal(this.web3.ethGetBalance(miner.address, DefaultBlockParameterName.LATEST).send().getBalance());
            BigDecimal balanceEther = Convert.fromWei(balance, Convert.Unit.ETHER);
            System.out.println(balanceEther);
        }
        return null;
    }

    private static class Miner {

        private final String address;
        private int blocks;

        public Miner(String address) {
            this.address = address;
            this.blocks = 1;
        }

        public void addBlock() {
            this.blocks++;
        }

        public int getBlocks() {
            return this.blocks;
        }

        public String getAddress() {
            return this.address;
        }
    }
}
