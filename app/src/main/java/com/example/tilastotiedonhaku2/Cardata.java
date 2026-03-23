package com.example.tilastotiedonhaku2;

public class Cardata {
    private String type;
    private int amount;

    public Cardata(String type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }
}