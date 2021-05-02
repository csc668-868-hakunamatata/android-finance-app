package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.financeapp.R;
import com.example.financeapp.model.Client;
import com.example.financeapp.utilities.BudgetAlert;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;
/*
    @author Ninh Le
 */
public class ProfileActivity extends HomePageActivity {
    private TextView profileName;
    private EditText et_budgetLimit;
    private Button saveProfile;
    private FirebaseAuth mAuth;
    private RadioGroup profileRadioGroup;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_profile);

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

        //enter profile
        profileName = (TextView) findViewById(R.id.tv_profile_name);
        et_budgetLimit = (EditText) findViewById(R.id.et_profile_budget_limit);
        profileRadioGroup = (RadioGroup)findViewById(R.id.RG_profile);
        saveProfile = (Button) findViewById(R.id.btn_save);
        profilePic = findViewById(R.id.profileDp);

        mAuth = FirebaseAuth.getInstance();
        try {
            final String clientId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clients/" + clientId);



                ref.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Log.d("ErrorProfile", snapshot.getValue().toString());
                        try {
                            Client client = snapshot.getValue(Client.class);
                            if (client != null) {
                                profileName.setText(client.getName());
                                if (client.getImageUri() != null && !client.getImageUri().equals("")) {
                                    Picasso.get().load(client.getImageUri()).into(profilePic);
                                }
                            }
                        }catch(Exception error){
                            Log.d("ErrorProfile", error.toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

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