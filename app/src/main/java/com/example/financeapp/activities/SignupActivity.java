package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.financeapp.model.Client;
import com.example.financeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private TextView backToLogin;
    private Button signUp;
    private EditText signupName, signupEmail, signupPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        backToLogin = (TextView) findViewById(R.id.backToLogin);
        backToLogin.setOnClickListener(this);

        signUp = (Button) findViewById(R.id.signupButton);
        signUp.setOnClickListener(this);

        signupName = (EditText) findViewById(R.id.signupName);
        signupEmail = (EditText) findViewById(R.id.signupEmail);
        signupPassword = (EditText) findViewById(R.id.signupPassword);

    }


    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.backToLogin:
                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
                break;
            case R.id.signupButton:
                registerUser();
                break;
        }
    }

    private void registerUser() {
        final String name = signupName.getText().toString();
        final String email = signupEmail.getText().toString().trim();
        String password = signupPassword.getText().toString();

        if(name.isEmpty()){
            signupName.setError("Full Name Missing");
            signupName.requestFocus();
            return;
        }

        if(email.isEmpty()){
            signupEmail.setError("Email missing");
            signupEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            signupEmail.setError("Please provide correct email");
            signupEmail.requestFocus();
            return;
        }

        if(password.isEmpty() || password.length()<6){
            signupPassword.setError("Six or more characters required");
            signupPassword.requestFocus();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                            final Client client = new Client(uid, name, email, "0.0");
                            FirebaseDatabase.getInstance().getReference("Clients/" + uid)
                                    .setValue(client).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(SignupActivity.this, "Successfully Registered", Toast.LENGTH_SHORT).show();

                                        //Login the user to app
                                        Intent intent = new Intent(SignupActivity.this, InitSetupActivity.class);
//                                        Intent intent = new Intent(SignupActivity.this, HomePageActivity.class);
                                        SignupActivity.this.startActivity(intent);
                                        finish();

                                    }else{
                                        Toast.makeText(SignupActivity.this, "Failed To Register", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                            Toast.makeText(SignupActivity.this, "Failed To Register", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}