/*
 * Ethereum Tool project.
 * Copyright (C) 2022 e-Contract.be BV.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EthereumRates {

    private Double eurRate;
    private Double dollarRate;

    private void init() throws IOException {
        if (null != this.eurRate) {
            return;
        }
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url("https://api.coinbase.com/v2/exchange-rates?currency=ETH").build();
        Response response = httpClient.newCall(request).execute();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response.body().byteStream());
        JsonNode dataNode = rootNode.get("data");
        JsonNode ratesNode = dataNode.get("rates");
        this.eurRate = ratesNode.get("EUR").asDouble();
        this.dollarRate = ratesNode.get("USD").asDouble();
    }

    public double getEuro(double ether) {
        try {
            init();
        } catch (IOException ex) {
            return 0;
        }
        return this.eurRate * ether;
    }

    public double getDollar(double ether) {
        try {
            init();
        } catch (IOException ex) {
            return 0;
        }
        return this.dollarRate * ether;
    }

    public BigDecimal getEuro(BigDecimal ether) {
        try {
            init();
        } catch (IOException ex) {
            return BigDecimal.ZERO;
        }
        return ether.multiply(BigDecimal.valueOf(this.eurRate));
    }

    public BigDecimal getDollar(BigDecimal ether) {
        try {
            init();
        } catch (IOException ex) {
            return BigDecimal.ZERO;
        }
        return ether.multiply(BigDecimal.valueOf(this.dollarRate));
    }
}
