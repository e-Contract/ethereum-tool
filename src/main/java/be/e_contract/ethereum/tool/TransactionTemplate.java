/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package be.e_contract.ethereum.tool;

public class TransactionTemplate {

    public String to;

    // unit: ether
    public double value;

    // unit: gwei
    public int gasPrice;

    public long nonce;

    // optional
    public Byte chainId;
}
