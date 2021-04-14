package com.example.financeapp.model;

public class Client {
    public String uid, name, email, currentBalance;

    public Client(){

    }

    public Client(String uid, String name, String email, String currentBalance){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.currentBalance = currentBalance;
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", currentBalance='" + currentBalance + '\'' +
                '}';
    }


    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
