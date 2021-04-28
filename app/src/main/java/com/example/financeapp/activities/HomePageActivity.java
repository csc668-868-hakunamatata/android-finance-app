package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.example.financeapp.R;
import com.example.financeapp.utilities.RecyclerViewAdapter;
import com.example.financeapp.utilities.Transaction;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class HomePageActivity extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private FirebaseAuth mAuth;
    private TextView currentBalance;
    private Button newEntry;
    RecyclerView recyclerView;
    private List<Transaction> listOfTransactions;
    private static final int budgetNotificationID = 1;
    private DrawerLayout drawerLayout;
    private double budgetLimit = 0.0;
    private boolean oneTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_home_page);

        //navigation
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //enter values
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
        getBudgetLimit();
        getOneTime();
        try {
            String clientId = mAuth.getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);
            ref.child("currentBalance").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (task.isSuccessful()) {
                        currentBalance.setText(String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));

                        if (task.getResult() != null && task.getResult().getValue() != null){
                            String stringToConvert = String.valueOf(task.getResult().getValue());
                            Double convertedLongBalance = Double.parseDouble(stringToConvert);
                               if (convertedLongBalance <= budgetLimit && !oneTime) {
                                   createBudgetAlert("You have exceeded the Budget Limit!");
                                   setBudgetAlertOneTime(true);
                               }
                               else if (convertedLongBalance > budgetLimit && oneTime) {
                                   setBudgetAlertOneTime(false);
                               }
                        }
                        Log.d("TheCurrentBalance", Objects.requireNonNull(task.getResult().getValue()).toString());
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
    public void onBackPressed() {
//        To close navigation drawer and not leave the activity immediate
//        when the back button is clicked
        if (drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
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

    private void getBudgetLimit() {
        String clientId = mAuth.getCurrentUser().getUid();
        DatabaseReference baRef = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + clientId);
        baRef.child("budgetLimit").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    String budgetLimitStr = String.valueOf(Objects.requireNonNull(task.getResult()).getValue());
                    budgetLimit = Double.parseDouble(budgetLimitStr);
                    Log.d("firebase", budgetLimitStr);
                }
            }
        });
    }
    // oneTime is true when budget alert is fired off once already while being below the budget limit
    private void setBudgetAlertOneTime(boolean oneTime) {
        try {
            String clientId = mAuth.getCurrentUser().getUid();
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + clientId);
            database.child("oneTime").setValue(oneTime).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("HomePageActivity", "Successfully changed oneTime");
                    } else {
                        Log.d("HomePageActivity", "Failed to change oneTime");
                    }
                }
            });
        }catch(Exception e){
            Log.d("HomePageActivity", e.toString());
        }
    }
    private void getOneTime() {
        String clientId = mAuth.getCurrentUser().getUid();
        DatabaseReference baRef = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + clientId);
        baRef.child("oneTime").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    oneTime = (Boolean) Objects.requireNonNull(task.getResult()).getValue();
                    Log.d("firebase oneTime", String.valueOf(oneTime));
                }
            }
        });
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        switch (id) {
            case R.id.nav_financehome:
                intent = new Intent(HomePageActivity.this, HomePageActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_entry:
                intent = new Intent(HomePageActivity.this, NewTransactionActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_history:
                intent = new Intent( HomePageActivity.this, HistoryActivity.class);
                startActivity(intent);
                break;
            case R.id.nav_profile:
                intent = new Intent(HomePageActivity.this, ProfileActivity.class);
                startActivity(intent);
                break;

        }

        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}