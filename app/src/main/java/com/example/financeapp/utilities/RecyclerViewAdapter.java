package com.example.financeapp.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financeapp.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    Context context;
    List<Transaction> listOfTransactions;

    public RecyclerViewAdapter(Context context, List<Transaction> listOfTransactions){
        this.context = context;
        this.listOfTransactions = listOfTransactions;
    }

    @NonNull
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.row_appearance, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerViewAdapter.MyViewHolder holder, int position) {
        holder.displaySource.setText(listOfTransactions.get(position).sourceOfSpendOrEarning);
        if(listOfTransactions.get(position).earnedOrSpent.equalsIgnoreCase("earned")){
            holder.displayEarnedOrSpent.setText(listOfTransactions.get(position).toString());
            //holder.displayEarnedOrSpent.setTextColor(0x008000);
        }else{
            holder.displayEarnedOrSpent.setText(listOfTransactions.get(position).toString());
            holder.displayEarnedOrSpent.setTextColor(Color.RED);
        }
        holder.displayEarnedOrSpent.setText(listOfTransactions.get(position).earnedOrSpent);
        holder.displayAmount.setText(listOfTransactions.get(position).amount);
    }

    @Override
    public int getItemCount() {
        return listOfTransactions.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView displayAmount, displaySource, displayEarnedOrSpent;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            displayAmount = itemView.findViewById(R.id.displayAmount);
            displayEarnedOrSpent = itemView.findViewById(R.id.displayEarnedOrSpent);
            displaySource = itemView.findViewById(R.id.displaySource);
        }
    }
}
