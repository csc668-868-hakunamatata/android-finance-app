package com.example.financeapp.model;

import android.app.Application;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.financeapp.activities.HomePageActivity;
import com.example.financeapp.activities.SignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class SignupRepository {
    private Application application;
    private MutableLiveData<FirebaseUser> userData;
    private FirebaseAuth mAuth;

    public SignupRepository(Application application){
        this.application = application;

        userData = new MutableLiveData<>();
        mAuth = FirebaseAuth.getInstance();
    }

    public void register(final String name, final String email, String password){
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
                                        userData.postValue(mAuth.getCurrentUser());

                                    }else{
                                       Toast.makeText(application, "Failed To Register", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }else{
                           Toast.makeText(application, "Failed To Register", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }
}
