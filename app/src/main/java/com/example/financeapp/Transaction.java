package com.example.financeapp;

import androidx.annotation.NonNull;

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

    @Override
    public String toString() {
        return "Transaction{" +
                "clientUid='" + clientUid + '\'' +
                ", earnedOrSpent='" + earnedOrSpent + '\'' +
                ", amount='" + amount + '\'' +
                ", sourceOfSpendOrEarning='" + sourceOfSpendOrEarning + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
