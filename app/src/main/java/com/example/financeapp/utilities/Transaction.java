package com.example.financeapp.utilities;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Transaction {
    public String clientUid;
    public String earnedOrSpent;
    public String amount;
    public String sourceOfSpendOrEarning;
    public String description;
    public String date;

    //public List<Transaction> transactionList;

    public Transaction(){
        //Empty Constructor
    }

    public Transaction(String clientUid, String earnedOrSpent, String amount, String sourceOfSpendOrEarning, String description, String date){
        this.clientUid = clientUid;
        this.earnedOrSpent = earnedOrSpent;
        this.sourceOfSpendOrEarning = sourceOfSpendOrEarning;
        this.amount = amount;
        this.description = description;
        this.date = date;
    //this.transactionList = new ArrayList<>();

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

    //    public List<Transaction> getTransactionList() {
//        return transactionList;
//    }
}
