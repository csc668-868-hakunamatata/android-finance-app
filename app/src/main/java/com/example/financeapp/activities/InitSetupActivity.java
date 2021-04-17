package com.example.financeapp.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.financeapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class InitSetupActivity extends AppCompatActivity {
    private EditText budgetLimit;
    private Button submit, cancel;
    private FirebaseAuth mAuth;
    private RadioGroup initRadioGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init_setup);
        budgetLimit = (EditText) findViewById(R.id.ET_budgetLimit);
        submit = (Button) findViewById(R.id.btn_confirm);
        cancel = (Button) findViewById(R.id.btn_cancel);
        initRadioGroup = (RadioGroup)findViewById(R.id.RG_initSetUp);
        mAuth = FirebaseAuth.getInstance();
    }
    public void cancelBudgetLimit(View view){

    }
    public void submitBudgetLimit(View view) {

    }
}