package com.example.financeapp;

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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class NewTransactionActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText amount, source, description;
    private Button submit;
    private FirebaseAuth mAuth;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_transaction);

        submit = (Button) findViewById(R.id.submitButton);
        amount = (EditText) findViewById(R.id.amountText);
        source = (EditText) findViewById(R.id.sourceText);
        description = (EditText) findViewById(R.id.descriptionText);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mAuth = FirebaseAuth.getInstance();
        submit.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.submitButton:
                submitTransaction();
                break;
        }
    }

    private void submitTransaction() {
        String amountGiven = amount.getText().toString();
        String sourceGiven = source.getText().toString();
        String descriptionGiven = description.getText().toString();
        String clientId = mAuth.getCurrentUser().getUid();
        String earnedOrSpent = "";

        if(amountGiven.isEmpty()){
            amount.setError("Please provide the amount");
            amount.requestFocus();
            return;
        }

        if(sourceGiven.isEmpty()){
            source.setError("Please provide the source");
            source.requestFocus();
            return;
        }

        if(descriptionGiven.isEmpty()){
            descriptionGiven = "";
        }

        int selectedRadioButton = radioGroup.getCheckedRadioButtonId();

        if(selectedRadioButton == R.id.radioSpending){
            Log.d("NewTransactionActivity", "Clicked on Spending");
            earnedOrSpent = "Spent";
        }
        else{
            Log.d("NewTransactionActivity", "Clicked on Earning");
            earnedOrSpent = "Earned";
        }

        storeToDatabase(clientId, earnedOrSpent, amountGiven, sourceGiven, descriptionGiven);
    }

    private void storeToDatabase(String clientId, String earnedOrSpent, String amountGiven, String sourceGiven, String descriptionGiven) {
        Transaction transaction = new Transaction(clientId, earnedOrSpent, amountGiven, sourceGiven, descriptionGiven);
        try {
            String transactionId = UUID.randomUUID().toString();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Transactions/" + transactionId);
            database.setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("NewTransactionActivity", "Successfully added Transaction to Database");
                    } else {
                        Log.d("NewTransactionActivity", "Failed to add Transaction to Database");
                    }
                }
            });
        }catch(Exception e){
            Log.d("NewTransactionActivity", e.toString());
        }
    }
}