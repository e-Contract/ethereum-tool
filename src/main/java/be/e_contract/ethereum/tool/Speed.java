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

import io.reactivex.disposables.Disposable;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.utils.Convert;
import picocli.CommandLine;

@CommandLine.Command(name = "speed", description = "Realtime analysis of network speed", separator = " ")
public class Speed implements Callable<Void> {

    @CommandLine.Option(names = {"-l", "--location"}, required = true, description = "the location of the client node")
    private Web3j web3;

    private Disposable pendingTransactionDisposable;

    private Disposable blockDisposable;

    @Override
    public Void call() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (null != this.pendingTransactionDisposable) {
                this.pendingTransactionDisposable.dispose();
            }
            if (null != this.blockDisposable) {
                this.blockDisposable.dispose();
            }
        }));
        System.out.println("Waiting for first block...");
        final Map<String, PendingTransaction> pendingTransactions = new ConcurrentHashMap<>();
        final Map<BigInteger, Timing> gasPrices = new HashMap<>();
        this.pendingTransactionDisposable = this.web3.pendingTransactionFlowable().subscribe((Transaction tx) -> {
            // we don't know the transaction type (regular or contract) here yet, so we add everything here
            pendingTransactions.put(tx.getHash(), new PendingTransaction(tx.getGasPrice()));
        }, error -> {
            Output.error(error.getMessage());
            error.printStackTrace();
        });
        this.blockDisposable = this.web3.blockFlowable(false).subscribe((EthBlock ethBlock) -> {
            EthBlock.Block block = ethBlock.getBlock();
            BigInteger timestamp = block.getTimestamp();
            Date timestampDate = new Date(timestamp.multiply(BigInteger.valueOf(1000)).longValue());
            DateTime timestampDateTime = new DateTime(timestampDate);
            int countProcessed = 0;
            for (EthBlock.TransactionResult<String> transactionResult : block.getTransactions()) {
                String transactionHash = transactionResult.get();
                PendingTransaction pendingTransaction = pendingTransactions.remove(transactionHash);
                if (null == pendingTransaction) {
                    // transaction was not known as a pending one before
                    continue;
                }
                countProcessed++;
                BigInteger gasPrice = pendingTransaction.gasPrice;
                Timing timing = gasPrices.get(gasPrice);
                if (null == timing) {
                    timing = new Timing(pendingTransaction.created);
                    gasPrices.put(gasPrice, timing);
                } else {
                    // we should not be using "now" here, but the block timestamp
                    timing.addTiming(pendingTransaction.created, timestampDateTime);
                }
            }

            BigInteger nodeGasPrice;
            try {
                nodeGasPrice = this.web3.ethGasPrice().send().getGasPrice();
            } catch (IOException ex) {
                Output.error("Error: " + ex.getMessage());
                return;
            }
            AnsiConsole.out().print(Ansi.ansi().reset().eraseScreen().cursor(0, 0));
            BigDecimal baseFeePerGas = new BigDecimal(block.getBaseFeePerGas().longValueExact());
            BigDecimal baseFeePerGasGwei = Convert.fromWei(baseFeePerGas, Convert.Unit.GWEI);
            System.out.println("Block: " + block.getNumber() + " - " + block.getTransactions().size() + " transactions - base fee " + baseFeePerGasGwei + " gwei");
            System.out.println("Processed transactions: " + countProcessed);
            System.out.println("Total pending transactions: " + pendingTransactions.size());
            int count = 40;
            System.out.print("Gas price (Gwei)");
            AnsiConsole.out().print(Ansi.ansi().cursorToColumn(20));
            System.out.print("Average time (sec)");
            AnsiConsole.out().print(Ansi.ansi().cursorToColumn(40));
            System.out.println("Tx count");
            List<Map.Entry<BigInteger, Timing>> gasPricesList = new ArrayList<>(gasPrices.entrySet());
            // sort on gas price
            gasPricesList.sort((o1, o2) -> o1.getKey().compareTo(o2.getKey()));
            for (Map.Entry<BigInteger, Timing> gasPriceEntry : gasPricesList) {
                if (count-- == 0) {
                    //only show top of the list
                    break;
                }
                switch (nodeGasPrice.compareTo(gasPriceEntry.getKey())) {
                    case -1:
                        AnsiConsole.out().print(Ansi.ansi().fgBrightGreen());
                        break;
                    case 0:
                        AnsiConsole.out().print(Ansi.ansi().fgBrightYellow());
                        break;
                    case 1:
                        AnsiConsole.out().print(Ansi.ansi().fgBrightRed());
                        break;
                }
                BigDecimal gasPriceGwei = Convert.fromWei(new BigDecimal(gasPriceEntry.getKey()), Convert.Unit.GWEI);
                System.out.print(gasPriceGwei);
                AnsiConsole.out().print(Ansi.ansi().cursorToColumn(20));
                System.out.print(gasPriceEntry.getValue().getAverageTime());
                AnsiConsole.out().print(Ansi.ansi().cursorToColumn(40));
                System.out.println(gasPriceEntry.getValue().getCount());
            }
            // TODO: we should implement a "sliding window" here
        }, error -> {
            Output.error(error.getMessage());
            error.printStackTrace();
        });
        return null;
    }

    private static final class Timing {

        private Duration totalTime;
        private int count;

        public Timing(DateTime created) {
            DateTime now = new DateTime();
            Interval interval = new Interval(created, now);
            Duration duration = interval.toDuration();
            this.totalTime = duration;
            this.count++;
        }

        public synchronized void addTiming(DateTime created, DateTime blockTimestamp) {
            // seems like blockTimestamp can be before created in the beginning...
            // so we still have to use now here
            DateTime now = new DateTime();
            Interval interval = new Interval(created, now);
            Duration duration = interval.toDuration();
            this.totalTime = this.totalTime.plus(duration);
            this.count++;
        }

        public long getAverageTime() {
            return this.totalTime.dividedBy(this.count).getStandardSeconds();
        }

        public int getCount() {
            return this.count;
        }
    }

    private static final class PendingTransaction {

        private final BigInteger gasPrice;
        private final DateTime created;

        public PendingTransaction(BigInteger gasPrice) {
            this.created = new DateTime();
            this.gasPrice = gasPrice;
        }
    }
}
