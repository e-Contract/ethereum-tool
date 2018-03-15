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
import java.util.concurrent.ConcurrentHashMap;
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

    @Override
    public Void call() throws Exception {
        Map<String, PendingTransaction> pendingTransactions = new ConcurrentHashMap<>();
        Map<BigInteger, Timing> gasPrices = new HashMap<>();
        this.web3.pendingTransactionObservable().subscribe(tx -> {
            System.out.println("incoming pending transactions...");
            pendingTransactions.put(tx.getHash(), new PendingTransaction(tx.getGasPrice()));
        });
        this.web3.blockObservable(true).subscribe(ethBlock -> {
            EthBlock.Block block = ethBlock.getBlock();
            for (EthBlock.TransactionResult<EthBlock.TransactionObject> transactionResult : block.getTransactions()) {
                EthBlock.TransactionObject transactionObject = transactionResult.get();
                Transaction transaction = transactionObject.get();
                String transactionHash = transaction.getHash();
                PendingTransaction pendingTransaction = pendingTransactions.remove(transactionHash);
                if (pendingTransaction != null) {
                    BigInteger gasPrice = pendingTransaction.gasPrice;
                    Timing timing = gasPrices.get(gasPrice);
                    if (null == timing) {
                        timing = new Timing(pendingTransaction.created);
                        gasPrices.put(gasPrice, timing);
                    } else {
                        timing.addTiming(pendingTransaction.created);
                    }
                }
            }

            List<Map.Entry<BigInteger, Timing>> gasPricesList = new ArrayList<>(gasPrices.entrySet());
            gasPricesList.sort((o1, o2) -> o1.getKey().compareTo(o2.getKey()));
            for (Map.Entry<BigInteger, Timing> gasPriceEntry : gasPricesList) {
                BigDecimal gasPriceGwei = Convert.fromWei(new BigDecimal(gasPriceEntry.getKey()), Convert.Unit.GWEI);
                System.out.println("gas price: " + gasPriceGwei + " gwei; average time: "
                        + gasPriceEntry.getValue().getAverageTime() / 1000 + " secs; tx count: " + gasPriceEntry.getValue().getCount());
            }
            System.out.println("-----------------------------------------------------------------");
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

        public void addTiming(DateTime created) {
            DateTime now = new DateTime();
            Interval interval = new Interval(created, now);
            Duration duration = interval.toDuration();
            this.totalTime.plus(duration);
            this.count++;
        }

        public long getAverageTime() {
            return this.totalTime.dividedBy(this.count).getMillis();
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
