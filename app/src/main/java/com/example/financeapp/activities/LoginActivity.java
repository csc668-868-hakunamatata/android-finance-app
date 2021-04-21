package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.financeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private EditText userEmail, userPassword;
    private TextView forgotPassword;
    private Button loginButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        forgotPassword = (TextView) findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser()!=null){
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            // Name, email address, and profile photo Url
            assert user != null;
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            // Check if user's email is verified
            boolean emailVerified = user.isEmailVerified();

//            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
//            startActivity(intent);
        }

        TextView noAccount = (TextView) findViewById(R.id.noAccount);
        noAccount.setOnClickListener(this);

        loginButton = (Button) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        userEmail = (EditText) findViewById(R.id.userEmail);
        userPassword = (EditText) findViewById(R.id.userPassword);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.noAccount:
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
                break;
                
            case R.id.loginButton:
                initiateLogin();
                break;

            case R.id.forgotPassword:
                Intent newIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(newIntent);
                break;
        }
    }

    private void initiateLogin() {
        String email = userEmail.getText().toString().trim();
        String password = userPassword.getText().toString();

        if(email.isEmpty()){
            userEmail.setError("Missing Email");
            userEmail.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            userEmail.setError("Enter valid email");
            userEmail.requestFocus();
            return;
        }

        if(password.isEmpty() || password.length()<=5){
            userPassword.setError("Enter 6 or more characters");
            userPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
                    LoginActivity.this.startActivity(intent);
                }else{
                    Toast.makeText(LoginActivity.this, "InValid Credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}