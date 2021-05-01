package com.example.financeapp.model;

public class Client {
    public String uid, name, email, currentBalance, imageUri;

    public Client(){

    }

    public Client(String uid, String name, String email, String currentBalance, String imageUri){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.currentBalance = currentBalance;
        this.imageUri = imageUri;
    }

    @Override
    public String toString() {
        return "Client{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", currentBalance='" + currentBalance + '\'' +
                "image='" + imageUri + '\''+
                '}';
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
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
