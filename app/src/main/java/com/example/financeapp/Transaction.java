package com.example.financeapp;

public class Transaction {
    boolean spent;
    boolean earned;
    double amount;
    String sourceOfSpendOrEarning;
    String description;

    public Transaction(){
        //Empty Constructor
    }

    public Transaction(boolean spent, boolean earned, double amount, String sourceOfSpendOrEarning, String description){
        this.spent = spent;
        this.earned = earned;
        this.sourceOfSpendOrEarning = sourceOfSpendOrEarning;
        this.amount = amount;
        this.description = description;
    }
}
