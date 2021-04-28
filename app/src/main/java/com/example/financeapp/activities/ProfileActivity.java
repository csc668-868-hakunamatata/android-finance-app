package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity {
    private TextView profileName;
    private EditText et_budgetLimit, et_budgetAlert;
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
            // hardcoded
            et_budgetLimit.setHint("200.00");
            et_budgetAlert.setHint("70.0");
        } catch (Exception e){
            Log.d("ProfileActivity", e.toString());
        }
    }

    public void saveProfile(View view) {
        Toast.makeText(ProfileActivity.this, "Successfully Saved Settings", Toast.LENGTH_SHORT).show();
        // @TODO update firebase
    }
}