package com.example.financeapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private TextView currentBalance;
    private Button newEntry;
    RecyclerView recyclerView;
    private List<Transaction> listOfTransactions;
//    private DrawerLayout drawer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_activity_home_page);

        //enter values
        recyclerView = findViewById(R.id.recyclerView);
        newEntry = (Button) findViewById(R.id.newEntryButton);
        currentBalance = (TextView) findViewById(R.id.currentBalance);
        mAuth = FirebaseAuth.getInstance();

        fetchBalanceFromFirebase();
        listOfTransactions = new ArrayList<>();
        fetchTransactionsFromFirebase();

        newEntry.setOnClickListener(this);
    }

    private void fetchTransactionsFromFirebase() {
        final String clientId = mAuth.getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Transactions/");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot s : snapshot.getChildren()) {
                    Transaction transaction = s.getValue(Transaction.class);
                    if (clientId.equals(transaction.clientUid)) {
                        listOfTransactions.add(transaction);
                        Log.d("HomePageActivity", transaction.toString());
                    }
                }
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(HomePageActivity.this, listOfTransactions);
                recyclerView.setAdapter(adapter);
                recyclerView.addItemDecoration(new DividerItemDecoration(HomePageActivity.this, LinearLayoutManager.VERTICAL));
                recyclerView.setLayoutManager(new LinearLayoutManager(HomePageActivity.this));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if(mAuth.getCurrentUser()!=null) {
//            fetchBalanceFromFirebase();
//        }
//    }

    private void fetchBalanceFromFirebase() {
        try {
            String clientId = mAuth.getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);
            ref.child("currentBalance").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().getValue() != null) {
                            currentBalance.setText(String.valueOf(task.getResult().getValue().toString()));
                            Log.d("TheCurrentBalance", task.getResult().getValue().toString());
                        }
                    } else {
                        Log.d("HomePageActivity", "Unsuccessful CurrentBalance update");
                    }
                }
            });
        } catch (Exception e) {
            Log.d("HomePageActivity", e.toString());
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
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
        switch (item.getItemId()) {
            case R.id.signoutButton:
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}