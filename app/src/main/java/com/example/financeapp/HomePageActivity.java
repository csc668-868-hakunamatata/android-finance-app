package com.example.financeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
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
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HomePageActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private TextView currentBalance;
    private Button newEntry;
    RecyclerView recyclerView;
    private List<Transaction> listOfTransactions;
    private static final int budgetNotificationID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        recyclerView = findViewById(R.id.recyclerView);
        newEntry = (Button) findViewById(R.id.newEntryButton);
        currentBalance = (TextView) findViewById(R.id.currentBalance);
        mAuth = FirebaseAuth.getInstance();

        fetchBalanceFromFirebase();
        // Notification
        createNotificationChannel();

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
                for(DataSnapshot s : snapshot.getChildren()){
                    Transaction transaction = s.getValue(Transaction.class);
                    if(clientId.equals(transaction.clientUid)) {
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
                        currentBalance.setText(String.valueOf(task.getResult().getValue().toString()));
                        if ((double) task.getResult().getValue() <= 0.0) {
                            createBudgetAlert("You have exceeded the Budget Limit!");
                        }
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
    private void createBudgetAlert(String messageBody) {
        Intent intent = new Intent(this, HomePageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,
                "BudgetAlert")
                .setSmallIcon(R.drawable.ic_alert)
                .setContentTitle("Budget Alert!")
                .setContentText(messageBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);
        // return builder;
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(budgetNotificationID, builder.build());
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Budget Alert";
            String description = "Budget Alert description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("BudgetAlert", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}