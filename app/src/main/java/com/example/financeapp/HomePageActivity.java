package com.example.financeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private TextView currentBalance;
    private Button newEntry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        newEntry = (Button) findViewById(R.id.newEntryButton);
        currentBalance = (TextView) findViewById(R.id.currentBalance);
        mAuth = FirebaseAuth.getInstance();
        newEntry.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mAuth.getCurrentUser()!=null) {
            fetchBalanceFromFirebase();
        }
    }

    private void fetchBalanceFromFirebase() {
        try {
            String clientId = mAuth.getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);
            ref.child("currentBalance").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        currentBalance.setText(String.valueOf(task.getResult().getValue().toString()));
                        Log.d("TheCurrentBalance", task.getResult().getValue().toString());
                    } else {
                        Log.d("HomePageActivity", "Unsuccessful CurrentBalance update");
                    }
                }
            });
        }catch(Exception e){
            Log.d("HomePageActivity", e.toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.newEntryButton:
                startActivity(new Intent(HomePageActivity.this, NewTransactionActivity.class));
                break;
        }
    }

    private void signOut() {
        mAuth.signOut();
        Intent intent = new Intent(HomePageActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.signoutButton:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}