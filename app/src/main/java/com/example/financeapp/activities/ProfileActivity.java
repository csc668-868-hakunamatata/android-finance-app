package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.financeapp.R;
import com.example.financeapp.utilities.BudgetAlert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
/*
    @author Ninh Le
 */
public class ProfileActivity extends AppCompatActivity {
    private TextView profileName;
    private EditText et_budgetLimit;
    private Button saveProfile;
    private FirebaseAuth mAuth;
    private RadioGroup profileRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        profileName = (TextView) findViewById(R.id.tv_profile_name);
        et_budgetLimit = (EditText) findViewById(R.id.et_profile_budget_limit);
        profileRadioGroup = (RadioGroup)findViewById(R.id.RG_profile);
        saveProfile = (Button) findViewById(R.id.btn_save);
        mAuth = FirebaseAuth.getInstance();
        try {
            String clientId = mAuth.getCurrentUser().getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);
            ref.child("name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        profileName.setText(String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));
                    }
                }
            });
            DatabaseReference baRef = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + clientId);
            baRef.child("budgetLimit").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>(){
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        et_budgetLimit.setHint(String.valueOf(Objects.requireNonNull(task.getResult()).getValue()));
                    }
                }
            });
        } catch (Exception e){
            Log.d("ProfileActivity", e.toString());
        }
    }

    public void saveProfile(View view) {
        String budgetLimitInput = et_budgetLimit.getText().toString();
        float budgetLimitNum = 0.0f;
        String clientId = mAuth.getCurrentUser().getUid();
        boolean optIn;
        if(budgetLimitInput.isEmpty()){
            budgetLimitInput = String.valueOf(et_budgetLimit.getHint());
        }
        try {
            budgetLimitNum = Float.parseFloat(budgetLimitInput);
        }catch(NumberFormatException e) {
            Log.d("ProfileActivity", e.toString());
            et_budgetLimit.requestFocus();
            return;
        }
        int selectedRadioButton = profileRadioGroup.getCheckedRadioButtonId();
        if(selectedRadioButton == R.id.rb_profile_on){
            Log.d("ProfileActivity", "Clicked On");
            optIn = true;
        }
        else{
            Log.d("ProfileActivity", "Clicked Off");
            optIn = false;
        }
        updateBudgetAlert(clientId, budgetLimitInput, optIn);
    }
    private void updateBudgetAlert(String clientId, String budgetLimit, boolean onOrOff) {
        BudgetAlert budgetAlert = new BudgetAlert(clientId, budgetLimit);
        budgetAlert.setAlertOn(onOrOff);
        try {
            DatabaseReference database = FirebaseDatabase.getInstance().getReference("BudgetAlert/" + clientId);
            database.setValue(budgetAlert).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ProfileActivity.this, "Successfully updated budget alert", Toast.LENGTH_SHORT).show();
                        Log.d("ProfileActivity", "Successfully added budget alert to user");
                    } else {
                        Log.d("ProfileActivity", "Failed to add budget alert to user");
                    }
                }
            });
        }catch(Exception e){
            Log.d("ProfileActivity", e.toString());
        }
    }

}