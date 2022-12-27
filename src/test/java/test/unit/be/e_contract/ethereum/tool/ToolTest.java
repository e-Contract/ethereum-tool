/*
 * Ethereum Tool project.
 * Copyright (C) 2018-2022 e-Contract.be BV.
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
package test.unit.be.e_contract.ethereum.tool;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Convert;

public class ToolTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ToolTest.class);

    @Test
    public void testConvertions() throws Exception {
        double value = 0.05;
        LOGGER.debug("value: {}", value);
        BigDecimal valueEther = BigDecimal.valueOf(value);
        LOGGER.debug("value ether: {}", valueEther);
        BigDecimal valueWei = Convert.toWei(valueEther, Convert.Unit.ETHER);
        LOGGER.debug("value wei: {}", valueWei);
        BigInteger valueWeiBigInteger = valueWei.toBigIntegerExact();
        LOGGER.debug("value wei (big integer): {}", valueWeiBigInteger);

        int gasPrice = 3;
        BigDecimal gasPriceGwei = BigDecimal.valueOf(gasPrice);
        BigDecimal gasPriceWei = Convert.toWei(gasPriceGwei, Convert.Unit.GWEI);
        BigInteger gasPriceWeiBigInteger = gasPriceWei.toBigIntegerExact();
        LOGGER.debug("gas price wei (big integer): {}", gasPriceWeiBigInteger);
    }
}
