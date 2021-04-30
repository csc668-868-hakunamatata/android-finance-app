package com.example.financeapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.financeapp.model.SignUpAndInRepository;
import com.google.firebase.auth.FirebaseUser;

public class SignUpAndInViewModel extends AndroidViewModel {
    private SignUpAndInRepository signUpAndInRepository;
    private MutableLiveData<FirebaseUser> userData;
    public SignUpAndInViewModel(@NonNull Application application) {
        super(application);

        signUpAndInRepository = new SignUpAndInRepository(application);
        userData = signUpAndInRepository.getUserData();
    }

    public void register(String name, String email, String password){
        signUpAndInRepository.register(name, email, password);
    }

    public void login(String email, String password){
        signUpAndInRepository.login(email, password);
    }

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }
}
