package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.financeapp.R;
import com.example.financeapp.utilities.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.UUID;

public class NewTransactionActivity extends HomePageActivity {
    private EditText amount, source, description;
    private Button submit;
    private FirebaseAuth mAuth;
    private RadioGroup radioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_new_transaction);
        //navigation
        DrawerLayout drawerLayout;
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener((NavigationView.OnNavigationItemSelectedListener) this);
        //
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

        storeTransactionToDatabase(clientId, earnedOrSpent, amountGiven, sourceGiven, descriptionGiven);

    }

    private void storeTransactionToDatabase(final String clientId, final String earnedOrSpent, final String amountGiven, String sourceGiven, String descriptionGiven) {
        Transaction transaction = new Transaction(clientId, earnedOrSpent, amountGiven, sourceGiven, descriptionGiven);
        try {
            String transactionId = UUID.randomUUID().toString();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("Transactions/" + transactionId);
            database.setValue(transaction).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        updateCurrentBalance(earnedOrSpent, clientId, amountGiven);
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

    private void updateCurrentBalance(final String earnedOrSpent, String clientId, final String amountGiven) {
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);

        try{
            final String[] currentValue = new String[1];
            ref.child("currentBalance").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(task.isSuccessful()){
                        currentValue[0] = String.valueOf(task.getResult().getValue());
                        Double newValue = 0.0;
                        if(earnedOrSpent.equalsIgnoreCase("spent")){
                            newValue = Double.parseDouble(currentValue[0]) - Double.parseDouble(amountGiven);
                        }else{
                            newValue = Double.parseDouble(currentValue[0]) + Double.parseDouble(amountGiven);
                        }

                        updateBalanceInDatabase(ref, newValue);
                        Log.d("Firebase", currentValue[0]);
                    }
                }
            });
        }catch(Exception e){
            Log.d("NewTransactionActivity", e.toString());
        }

    }

    private void updateBalanceInDatabase(DatabaseReference ref, final double newValue){
        ref.child("currentBalance").setValue(newValue).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Log.d("Firebase", "Added New Value "+ newValue + " ");
                    startActivity(new Intent(NewTransactionActivity.this, HomePageActivity.class));
                }
            }
        });
    }
}