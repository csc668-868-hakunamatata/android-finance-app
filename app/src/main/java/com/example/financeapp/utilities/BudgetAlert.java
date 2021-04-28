package com.example.financeapp.utilities;
/*
@Author Ninh Le
 */

public class BudgetAlert {
    private String clientId;
    private float budgetLimit;
    private boolean alertOn = false;
    // oneTime tracks push notification so that it only trigger once when below budget limit
    private boolean oneTime = false;

    public BudgetAlert(){}

    public BudgetAlert(String clientId, float budgetLimit){
        this.clientId = clientId;
        this.budgetLimit = budgetLimit;
        this.alertOn = true;
    }

    @Override
    public String toString(){
        return "BudgetLimit{" +
                "budgetLimit='" + budgetLimit + '\'' +
                '}';
    }
    public void setAlertOn(boolean onOrOff) {
        this.alertOn = onOrOff;
    }
    public void setOneTime(boolean onOrOff) { this.oneTime = onOrOff; }
    public boolean getAlertOn() { return this.alertOn; }
    public boolean getOneTime() { return this.oneTime; }
}
