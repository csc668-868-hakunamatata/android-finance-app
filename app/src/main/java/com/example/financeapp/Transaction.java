package com.example.financeapp;

public class Transaction {
    public String clientUid;
    public String earnedOrSpent;
    public String amount;
    public String sourceOfSpendOrEarning;
    public String description;

    public Transaction(){
        //Empty Constructor
    }

    public Transaction(String clientUid, String earnedOrSpent, String amount, String sourceOfSpendOrEarning, String description){
        this.clientUid = clientUid;
        this.earnedOrSpent = earnedOrSpent;
        this.sourceOfSpendOrEarning = sourceOfSpendOrEarning;
        this.amount = amount;
        this.description = description;
    }
}
