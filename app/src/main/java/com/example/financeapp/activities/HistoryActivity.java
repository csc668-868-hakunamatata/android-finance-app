package com.example.financeapp.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financeapp.R;
import com.example.financeapp.utilities.RecyclerViewAdapter;
import com.example.financeapp.utilities.Transaction;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;


public class HistoryActivity extends HomePageActivity {
    private FirebaseAuth mAuth;
    private TextView currentBalance;
    private Button newEntry;
    RecyclerView recyclerView;


    private List<Transaction> listOfTransactions;
    private static final int budgetNotificationID = 1;
    private PieChart expensePieChart, depositPieChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_history);

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
        navigationView.setNavigationItemSelectedListener(this);


        //enter values
        recyclerView = findViewById(R.id.recyclerView);
        newEntry = (Button) findViewById(R.id.newEntryButton);
        currentBalance = (TextView) findViewById(R.id.currentBalance);
        mAuth = FirebaseAuth.getInstance();

        listOfTransactions = new ArrayList<>();
        fetchTransactionsFromFirebase();

        newEntry.setOnClickListener(this);

        //chart

//        pieChart = findViewById(R.id.chart);
//        System.out.println(getIntent());

    }



    private void loadPieChartData(List<Transaction> currentTransaction) {
        expensePieChart = findViewById(R.id.chart); //TODO need to get deposit chart
        ArrayList<PieEntry> tempDepositList = new ArrayList<>();
        ArrayList<PieEntry> tempExpenseList = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        double totalDeposit = 0.0, totalExpense = 0.0;
        for (int i =0; i< currentTransaction.size(); i++){
            if (currentTransaction.get(i).earnedOrSpent.toLowerCase().equals("spent")) {
                String s = currentTransaction.get(i).amount;
                double tempDouble = Double.parseDouble(s);
                totalExpense += tempDouble;
            } else if (currentTransaction.get(i).earnedOrSpent.toLowerCase().equals("earned")) {
                String s = currentTransaction.get(i).amount;
                double tempDouble = Double.parseDouble(s);
                totalDeposit += tempDouble;
            }
        }
        setupPieChart();

        for (int i =0; i< currentTransaction.size(); i++){
            if (currentTransaction.get(i).earnedOrSpent.toLowerCase().equals("spent")) {
                String s = currentTransaction.get(i).amount;
                float tempFloat = Float.parseFloat(s)/ ((float) totalExpense) ;
                tempExpenseList.add(new PieEntry(tempFloat, currentTransaction.get(i).sourceOfSpendOrEarning)); //need to also add the label
            } else if (currentTransaction.get(i).earnedOrSpent.toLowerCase().equals("earned")) {
                String s = currentTransaction.get(i).amount;
                float tempFloat = Float.parseFloat(s)/ ((float) totalDeposit) ;
                tempDepositList.add(new PieEntry(tempFloat, currentTransaction.get(i).sourceOfSpendOrEarning)); //need to also add the label
            }
        }

        for (int color: ColorTemplate.MATERIAL_COLORS){
            colors.add(color);
        }
        for (int color: ColorTemplate.VORDIPLOM_COLORS){
            colors.add(color);
        }

        PieDataSet expenseDataSet = new PieDataSet(tempExpenseList, "Expense Category");
        expenseDataSet.setColors(colors);
        PieDataSet depositDataSet = new PieDataSet(tempDepositList, "Deposit Category");
        depositDataSet.setColors(colors);

        PieData expenseData = new PieData(expenseDataSet);
        expenseData.setDrawValues(true);
        expenseData.setValueFormatter(new PercentFormatter(expensePieChart));
        expenseData.setValueTextSize(12f);
        expenseData.setValueTextColor(Color.BLACK);

        PieData depositData = new PieData(depositDataSet);
        depositData.setDrawValues(true);
        depositData.setValueFormatter(new PercentFormatter(depositPieChart));
        depositData.setValueTextSize(12f);
        depositData.setValueTextColor(Color.GREEN);

        expensePieChart.setData(expenseData);
        expensePieChart.invalidate();
        depositPieChart.setData(depositData);
        depositPieChart.invalidate();
    }

    private void setupPieChart(){
        expensePieChart.setDrawHoleEnabled(true);
        expensePieChart.setUsePercentValues(true);
        //set labels
        expensePieChart.setEntryLabelTextSize(12);
        expensePieChart.setEntryLabelColor(Color.BLACK);
        expensePieChart.setCenterText("Spending by Category");
        expensePieChart.setCenterTextSize(24);
        expensePieChart.getDescription().setEnabled(false);

        //set legend
        Legend legend = expensePieChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setEnabled(true);
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
                        setListOfTransactions(listOfTransactions);
                        Log.d("HistoryActivity", transaction.toString());
                    }
                }
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(HistoryActivity.this, listOfTransactions);
                recyclerView.setAdapter(adapter);
                recyclerView.addItemDecoration(new DividerItemDecoration(HistoryActivity.this, LinearLayoutManager.VERTICAL));
                recyclerView.setLayoutManager(new LinearLayoutManager(HistoryActivity.this));
                loadPieChartData(listOfTransactions);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }


        });
    }

    public void setListOfTransactions(List<Transaction> currentTransaction) {
        this.listOfTransactions = currentTransaction;
    }

    public List<Transaction> getListOfTransactions() {
        return this.listOfTransactions;
    }
}