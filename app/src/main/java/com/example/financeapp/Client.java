package com.example.financeapp;

import java.util.ArrayList;
import java.util.List;

public class Client {
    public String uid, name, email, currentBalance;
    //public List<Transaction> transactionList;

    public Client(){

    }

    public Client(String uid, String name, String email, String currentBalance){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.currentBalance = currentBalance;
        //this.transactionList = new ArrayList<>();
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", currentBalance='" + currentBalance + '\'' +
                '}';
    }
//    public void setTransactions(List<Transaction> transactions){
//        this.transactionList = transactions;
//    }
//
//    public void addTransaction(Transaction transaction){
//        if(transaction.spent){
//            double newAmount = Double.parseDouble(currentBalance) - Double.parseDouble(transaction.amount);
//            currentBalance = String.valueOf(newAmount);
//        }else if(transaction.earned){
//            double newAmount = Double.parseDouble(currentBalance) - Double.parseDouble(transaction.amount);
//            currentBalance = String.valueOf(newAmount);
//        }
//        this.transactionList.add(transaction);
//    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

//    public List<Transaction> getTransactionList() {
//        return transactionList;
//    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
