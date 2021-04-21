package com.example.financeapp.utilities;
/*
@Author Ninh Le
 */

public class BudgetAlert {
    private String clientUid;
    private String frequency;
    private float budgetLimit;
    private float budgetAlertPercent;

    public BudgetAlert(){}

    public BudgetAlert(String clientUid, String frequency, float budgetLimit, float budgetAlertPercent){
        this.clientUid = clientUid;
        this.frequency = frequency;
        this.budgetLimit = budgetLimit;
        this.budgetAlertPercent = budgetAlertPercent;
    }

    @Override
    public String toString(){
        return "Transaction{" +
                "clientUid='" + clientUid + '\'' +
                ", frequency='" + frequency + '\'' +
                ", budgetLimit='" + budgetLimit + '\'' +
                ", budgetAlertPercent'" + budgetAlertPercent + '\'' +
                '}';
    }
}
