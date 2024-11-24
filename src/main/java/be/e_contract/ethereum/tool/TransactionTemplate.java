/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2024 e-Contract.be BV.
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

public class TransactionTemplate {

    // optional
    public String description;

    // optional
    public String from;

    public String to;

    // unit: ether
    public double value;

    // unit: gwei
    public Double gasPrice;

    // unit: gwei
    public Double maxFeePerGas;

    // unit: gwei
    public Double maxPriorityFeePerGas;

    public long nonce;

    // optional
    public Long chainId;
}
