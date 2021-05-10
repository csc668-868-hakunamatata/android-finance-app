package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.financeapp.R;
import com.example.financeapp.utilities.BudgetAlert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.slider.Slider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

/*
    @author Ninh Le
 */

public class InitSetupActivity extends AppCompatActivity {
    private EditText budgetLimit, initBalance;
    private Button submit, cancel;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_setup);
        budgetLimit = (EditText) findViewById(R.id.ET_budgetLimit);
        initBalance = (EditText) findViewById(R.id.ET_balance);
        submit = (Button) findViewById(R.id.btn_confirm);
        cancel = (Button) findViewById(R.id.btn_cancel);
        mAuth = FirebaseAuth.getInstance();
    }
    public void cancelBudgetLimit(View view){
        String clientId = mAuth.getCurrentUser().getUid();
        storeBudgetAlert(clientId, "0.0", false);
    }

    public void submitBudgetLimit(View view) {
        String budgetLimitInput = budgetLimit.getText().toString();
        double decimalBudget = Double.parseDouble(budgetLimitInput);
        budgetLimitInput = String.format("%.2f", decimalBudget);

        String initBalanceInput = initBalance.getText().toString();
        double decimalBalance = Double.parseDouble(initBalanceInput);
        initBalanceInput = String.format("%.2f", decimalBalance);

        float initBalanceNum = 0.0f;
        float budgetLimitNum = 0.0f;
        String clientId = mAuth.getCurrentUser().getUid();
        if(budgetLimitInput.isEmpty()){
            budgetLimit.setError("Please provide the amount");
            budgetLimit.requestFocus();
            return;
        }
        if(budgetLimitInput.isEmpty()){
            initBalance.setError("Please provide the amount");
            initBalance.requestFocus();
            return;
        }
        try {
            budgetLimitNum = Float.parseFloat(budgetLimitInput);
            initBalanceNum = Float.parseFloat(initBalanceInput);
        }catch(NumberFormatException e) {
            Log.d("InitSetupActivity", e.toString());
            budgetLimit.requestFocus();
            return;
        }
        updateCurrentBalance(clientId, initBalanceInput);
        storeBudgetAlert(clientId, budgetLimitInput, true);
    }

    private void storeBudgetAlert(String clientId, String budgetLimit, boolean onOrOff) {
        BudgetAlert budgetAlert = new BudgetAlert(clientId, budgetLimit);
        budgetAlert.setAlertOn(onOrOff);
        try {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + clientId);
            database.setValue(budgetAlert).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(InitSetupActivity.this, "Successfully added budget alert", Toast.LENGTH_SHORT).show();
                        Log.d("InitSetupActivity", "Successfully added budget alert to user");
                        Intent intent = new Intent(InitSetupActivity.this, HomePageActivity.class);
                        InitSetupActivity.this.startActivity(intent);
                        finish();
                    } else {
                        Log.d("InitSetupActivity", "Failed to add budget alert to user");
                    }
                }
            });
        }catch(Exception e){
            Log.d("initSetupActivity", e.toString());
        }
    }

    private void updateCurrentBalance(String clientId, final String amountGiven) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);

        try{
            //final String[] currentValue = new String[1];
            ref.child("currentBalance").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
//                        currentValue[0] = String.valueOf(task.getResult().getValue());
//                        Double newValue = 0.0;
//                        newValue = Double.parseDouble(currentValue[0]) + Double.parseDouble(amountGiven);
                        updateBalanceInDatabase(ref, amountGiven);
                        //Log.d("Firebase", currentValue[0]);
                    }
                }
            });
        }catch(Exception e){
            Log.d("NewTransactionActivity", e.toString());
        }

    }

    private void updateBalanceInDatabase(DatabaseReference ref, final String newValue){
        //String updatedValue = String.valueOf(newValue);
        ref.child("currentBalance").setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("Firebase", "Added New Value "+ newValue + " ");
                }
            }
        });
    }
}