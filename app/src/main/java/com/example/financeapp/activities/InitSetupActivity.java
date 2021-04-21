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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class InitSetupActivity extends AppCompatActivity {
    private EditText budgetLimit, ET_budgetAlert;
    private Button submit, cancel;
    private FirebaseAuth mAuth;
    private RadioGroup initRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_setup);
        budgetLimit = (EditText) findViewById(R.id.ET_budgetLimit);
        ET_budgetAlert = (EditText) findViewById(R.id.ET_budgetAlert);
        submit = (Button) findViewById(R.id.btn_confirm);
        cancel = (Button) findViewById(R.id.btn_cancel);
        initRadioGroup = (RadioGroup)findViewById(R.id.RG_initSetUp);
        mAuth = FirebaseAuth.getInstance();
    }
    public void cancelBudgetLimit(View view){

    }
    public void submitBudgetLimit(View view) {
        String budgetLimitInput = budgetLimit.getText().toString();
        float budgetLimitNum = 0.0f;
        String budgetAlertInput = ET_budgetAlert.getText().toString();
        float budgetAlertNum = 0.0f;
        String clientId = mAuth.getCurrentUser().getUid();
        String frequency = "";
        if(budgetLimitInput.isEmpty()){
            budgetLimit.setError("Please provide the amount");
            budgetLimit.requestFocus();
            return;
        }
        if(budgetAlertInput.isEmpty()) {
            ET_budgetAlert.setError("Please proved the amount");
            ET_budgetAlert.requestFocus();
            return;
        }
        try {
            budgetLimitNum = Float.parseFloat(budgetLimitInput);
            budgetAlertNum = Float.parseFloat(budgetAlertInput);
        }catch(NumberFormatException e) {
            Log.d("InitSetupActivity", e.toString());
        }
        int selectedRadioButton = initRadioGroup.getCheckedRadioButtonId();
        if(selectedRadioButton == R.id.RB_weekly){
            Log.d("InitSetupActivity", "Clicked on Weekly");
            frequency = "Weekly";
        }
        else{
            Log.d("InitSetupActivity", "Clicked on Monthly");
            frequency = "Monthly";
        }
        storeBudgetAlert(clientId, frequency, budgetLimitNum, budgetAlertNum);
    }

    private void storeBudgetAlert(String clientId, String frequency, float budgetLimit, final float budgetAlertPercent) {
        BudgetAlert budgetAlert = new BudgetAlert(clientId, frequency, budgetLimit, budgetAlertPercent);
        try {
            String budgetAlertId = UUID.randomUUID().toString();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + budgetAlertId);
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
}