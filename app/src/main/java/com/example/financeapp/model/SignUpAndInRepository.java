package com.example.financeapp.model;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.financeapp.activities.HomePageActivity;
import com.example.financeapp.activities.LoginActivity;
import com.example.financeapp.activities.SignupActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class SignUpAndInRepository {
    private Application application;
    private MutableLiveData<FirebaseUser> userData;
    private FirebaseAuth mAuth;

    public SignUpAndInRepository(Application application){
        this.application = application;

        userData = new MutableLiveData<>();
        mAuth = FirebaseAuth.getInstance();
    }

    public void register(final String name, final String email, String password,  final Uri image){
       // Log.d("receiverUrl", downloadUrl+" printed");
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            uploadImage(image, name, email);
                        }else{
                           Toast.makeText(application, "Failed To Register", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void login(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    userData.postValue(mAuth.getCurrentUser());
                }else{
                    //Toast.makeText(application, "Failed Login", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }

    private void uploadImage(Uri uriImage, final String name, final String email) {
        String uuid = UUID.randomUUID().toString();
        final StorageReference ref = FirebaseStorage.getInstance().getReference("pictures/" + uuid);
        if(uriImage!=null) {
            ref.putFile(uriImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    saveToDatabase(uri.toString(), name, email);
                                }
                            });
                        }
                    });

        }else{
            saveToDatabase("", name, email);
        }

    }

    private void saveToDatabase(String imageUrl, String name, String email){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Client client = new Client(uid, name, email, "0.0", imageUrl);
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
    }
}
