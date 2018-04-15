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

import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import picocli.CommandLine;

public class AddressTypeConverter implements CommandLine.ITypeConverter<Address> {

    @Override
    public Address convert(String value) throws Exception {
        if (!WalletUtils.isValidAddress(value)) {
            Output.error("Invalid address: " + value);
            System.exit(1);
        }
        if (value.toLowerCase().equals(value)) {
            return new Address(value);
        }
        if (!Keys.toChecksumAddress(value).equals(value)) {
            Output.error("Invalid address checksum: " + value);
            System.exit(1);
        }
        return new Address(value);
    }
}
