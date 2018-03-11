/*
 * Ethereum Tool project.
 *
 * Copyright 2018 e-Contract.be BVBA. All rights reserved.
 * e-Contract.be BVBA proprietary/confidential. Use is subject to license terms.
 */
package test.unit.be.e_contract.ethereum.tool;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.Test;
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
