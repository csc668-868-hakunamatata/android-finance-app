package com.example.financeapp;

public class Transaction {
    String clientUid;
    boolean spent;
    boolean earned;
    String amount;
    String sourceOfSpendOrEarning;
    String description;

    public Transaction(){
        //Empty Constructor
    }

    public Transaction(String clientUid, boolean spent, boolean earned, String amount, String sourceOfSpendOrEarning, String description){
        this.clientUid = clientUid;
        this.spent = spent;
        this.earned = earned;
        this.sourceOfSpendOrEarning = sourceOfSpendOrEarning;
        this.amount = amount;
        this.description = description;
    }
}
