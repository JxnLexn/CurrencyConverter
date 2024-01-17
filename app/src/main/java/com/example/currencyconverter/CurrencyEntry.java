package com.example.currencyconverter;

public class CurrencyEntry {
    private final String currencyName;
    private final double exchangeRate;

    public CurrencyEntry(String currencyName, double exchangeRate) {
        this.currencyName = currencyName;
        this.exchangeRate = exchangeRate;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }
}

