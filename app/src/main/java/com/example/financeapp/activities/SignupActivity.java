package com.example.financeapp.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.financeapp.ViewModel.SignUpAndInViewModel;
import com.example.financeapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;


public class SignupActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;
    private TextView backToLogin;
    private Button signUp;
    private EditText signupName, signupEmail, signupPassword;
    private SignUpAndInViewModel signUpAndInViewModel;
    private ImageView profilePic;
    private Uri uriImage;
    private FirebaseStorage fireStore;
    private StorageReference reference;
    private String downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        signUpAndInViewModel = new ViewModelProvider(this).get(SignUpAndInViewModel.class);
        signUpAndInViewModel.getUserData().observe(this, new Observer<FirebaseUser>() {
            @Override
            public void onChanged(FirebaseUser firebaseUser) {
                if(firebaseUser!=null){
                    Log.d("TestingApp", "passed");
                    Toast.makeText(SignupActivity.this, "User Created Successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignupActivity.this, InitSetupActivity.class);
                    startActivity(intent);
                }else{
                    Log.d("TestingApp", "failed");
                    //Toast.makeText(SignupActivity.this, "User Creation Failed", Toast.LENGTH_SHORT).show();
                }
            }
        });


        fireStore = FirebaseStorage.getInstance();
        reference = fireStore.getReference();

        mAuth = FirebaseAuth.getInstance();
        backToLogin = (TextView) findViewById(R.id.backToLogin);
        backToLogin.setOnClickListener(this);

        signUp = (Button) findViewById(R.id.signupButton);
        signUp.setOnClickListener(this);
        
        profilePic = findViewById(R.id.profilePic);
        profilePic.setOnClickListener(this);

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
                
            case R.id.profilePic:
                selectPicture();
        }
    }

    private void selectPicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null){
            uriImage = data.getData();
            profilePic.setImageURI(uriImage);
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

        if(downloadUrl==null){
            downloadUrl = "";
        }

        Log.d("downloadNewUrl", downloadUrl + "second");
        signUpAndInViewModel.register(name, email, password, uriImage);
    }
}