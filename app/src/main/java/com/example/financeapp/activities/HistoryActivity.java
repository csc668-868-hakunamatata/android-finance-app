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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * authored by Inez Wibowo
 */

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
        mAuth = FirebaseAuth.getInstance();

        listOfTransactions = new ArrayList<>();
        fetchTransactionsFromFirebase();
    }


    /**
     * Loads the data from transactions into the pie charts
     *
     * @param  currentTransaction  List of transactions completed
     *
     */
    private void loadPieChartData(List<Transaction> currentTransaction) {
        expensePieChart = findViewById(R.id.chartDeposit);
        depositPieChart = findViewById(R.id.chartExpense);

        ArrayList<PieEntry> tempDepositList = new ArrayList<>();
        ArrayList<PieEntry> tempExpenseList = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();
        double totalDeposit = 0.0, totalExpense = 0.0;

        Map<String, Double> spendingMap = new HashMap<>();
        Map<String, Double> earnedMap = new HashMap<>();

        for (int i = 0; i < currentTransaction.size(); i++) {
            if (currentTransaction.get(i).earnedOrSpent.toLowerCase().equals("spent")) {
                String s = currentTransaction.get(i).amount;
                String sourceSpent = currentTransaction.get(i).sourceOfSpendOrEarning;
                //parse value to make sure there aren't any duplicates spent
                double tempDouble = Double.parseDouble(s);
                if (spendingMap.containsKey(sourceSpent)) {
                    double prev = spendingMap.get(sourceSpent);
                    spendingMap.put(sourceSpent, prev + tempDouble);
                } else {
                    spendingMap.put(sourceSpent, tempDouble);
                }
                totalExpense += tempDouble;
            } else if (currentTransaction.get(i).earnedOrSpent.toLowerCase().equals("earned")) {
                String s = currentTransaction.get(i).amount;
                String sourceEarned = currentTransaction.get(i).sourceOfSpendOrEarning;
                //parse value to make sure there aren't any duplicates earned
                double tempDouble = Double.parseDouble(s);
                if (earnedMap.containsKey(sourceEarned)) {
                    double prev = earnedMap.get(sourceEarned);
                    earnedMap.put(sourceEarned, prev + tempDouble);
                } else {
                    earnedMap.put(sourceEarned, tempDouble);
                }
                totalDeposit += tempDouble;
            }
        }
        setupPieChart(expensePieChart, "Spending by category");
        setupPieChart(depositPieChart, "Deposit by category");

        for (String i : spendingMap.keySet()) {
            double d = spendingMap.get(i);
            float tempFloat = (float) d / ((float) totalExpense);
            tempExpenseList.add(new PieEntry(tempFloat, i)); //need to also add the label
        }
        for (String i : earnedMap.keySet()) {
            double d = earnedMap.get(i);
            float tempFloat = (float) d / ((float) totalDeposit);
            tempDepositList.add(new PieEntry(tempFloat, i)); //need to also add the label
        }

        for (int color : ColorTemplate.MATERIAL_COLORS) {
            colors.add(color);
        }
        for (int color : ColorTemplate.VORDIPLOM_COLORS) {
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
        depositData.setValueTextColor(Color.BLACK);

        expensePieChart.setData(expenseData);
        expensePieChart.invalidate();
        depositPieChart.setData(depositData);
        depositPieChart.invalidate();
    }

    /**
     * Initializes the type and label of the pie chart
     *
     * @param  pieChartType  type of pie chart being used
     * @param  label label of the pie chart
     */
    private void setupPieChart(PieChart pieChartType, String label) {
        pieChartType.setDrawHoleEnabled(true);
        pieChartType.setUsePercentValues(true);
        //set labels for expense
        pieChartType.setEntryLabelTextSize(12);
        pieChartType.setEntryLabelColor(Color.BLACK);
        pieChartType.setCenterText(label);
        pieChartType.setCenterTextSize(12);
        pieChartType.getDescription().setEnabled(false);
        //set legend for expense
        Legend pieChartTypeLegend = pieChartType.getLegend();
        pieChartTypeLegend.setEnabled(false);
    }

    /**
     * Function makes a call to Firebase and gets the list of transactions
     * which is passed to loadPieChartData()
     */
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

    /**
     * Sets the list of transactions to the current transactions
     *
     * @param  currentTransaction  a list of transactions
     */
    public void setListOfTransactions(List<Transaction> currentTransaction) {
        this.listOfTransactions = currentTransaction;
    }

    public List<Transaction> getListOfTransactions() {
        return this.listOfTransactions;
    }

}