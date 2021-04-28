package com.example.financeapp.ViewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.financeapp.model.SignupRepository;
import com.google.firebase.auth.FirebaseUser;

public class SignupViewModel extends AndroidViewModel {
    private SignupRepository signupRepository;
    private MutableLiveData<FirebaseUser> userData;
    public SignupViewModel(@NonNull Application application) {
        super(application);

        signupRepository = new SignupRepository(application);
        userData = signupRepository.getUserData();
    }

    public void register(String name, String email, String password){
        signupRepository.register(name, email, password);
    }

    public MutableLiveData<FirebaseUser> getUserData() {
        return userData;
    }
}
