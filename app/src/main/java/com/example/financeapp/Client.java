package com.example.financeapp;

import java.util.ArrayList;
import java.util.List;

public class Client {
    public String name, email;
    public List<Transaction> transactionList = new ArrayList<>();
    double currentBalance;

    public Client(){

    }

    public Client(String name, String email){
        this.name = name;
        this.email = email;
    }

    public void setTransactions(List<Transaction> transactions){
        this.transactionList = transactions;
    }

    public void addTransaction(Transaction transaction){
        if(transaction.spent){
            currentBalance = currentBalance - transaction.amount;
        }else if(transaction.earned){
            currentBalance = currentBalance + transaction.amount;
        }
        this.transactionList.add(transaction);
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public List<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
