package com.example.chitchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.hbb20.CountryCodePicker;

public class SignupActivity extends AppCompatActivity {
    CountryCodePicker countryCodePicker;
    EditText phoneInput;
    Button sendOtpBtn;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        countryCodePicker=findViewById(R.id.login_country_code);
        phoneInput=findViewById(R.id.login_mobile_number);
        sendOtpBtn=findViewById(R.id.send_otp);
        progressBar=findViewById(R.id.login_progressbar);
        progressBar.setVisibility(View.GONE);
        countryCodePicker.registerCarrierNumberEditText(phoneInput);
        sendOtpBtn.setOnClickListener((v)->{
            if(!countryCodePicker.isValidFullNumber()){
                phoneInput.setError("Phone number is not a valid number, please check it");
                return;
            }
            Intent intent=new Intent(SignupActivity.this,OtpActivity.class);
            intent.putExtra("phone",countryCodePicker.getFullNumberWithPlus());

            startActivity(intent);
        });
    }
}