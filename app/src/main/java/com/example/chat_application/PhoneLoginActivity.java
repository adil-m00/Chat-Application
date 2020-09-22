package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private Button sendVerificationButton,VerifyButton;
    private EditText inputPhoneNumber,inputVerifyCode;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mForcedToken;

    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        loadingBar = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        sendVerificationButton = findViewById(R.id.send_verification_code_button);
        VerifyButton = findViewById(R.id.verify_button);
        inputPhoneNumber = findViewById(R.id.phone_number_input);
        inputVerifyCode = findViewById(R.id.verification_code_input);


        sendVerificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String PhoneNmber = inputPhoneNumber.getText().toString();
                if(PhoneNmber.isEmpty())
                {
                    inputPhoneNumber.setError("Please Enter Phone number");
                    return;
                }
                else {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Pleaw Wait..");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            PhoneNmber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);
                }
            }
        });



        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {

                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please Enter correct phone number with country code", Toast.LENGTH_SHORT).show();

                inputPhoneNumber.setVisibility(View.VISIBLE);
                sendVerificationButton.setVisibility(View.VISIBLE);

                inputVerifyCode.setVisibility(View.INVISIBLE);
                VerifyButton.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                loadingBar.dismiss();
                mVerificationId = verificationId;
                mForcedToken = token;

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent", Toast.LENGTH_SHORT).show();

                inputPhoneNumber.setVisibility(View.INVISIBLE);
                sendVerificationButton.setVisibility(View.INVISIBLE);

                inputVerifyCode.setVisibility(View.VISIBLE);
                VerifyButton.setVisibility(View.VISIBLE);


            }
        };






        VerifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                sendVerificationButton.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerifyCode.getText().toString();
                if(verificationCode.isEmpty())
                {
                    loadingBar.dismiss();
                    Toast.makeText(PhoneLoginActivity.this, "Please enter verification code", Toast.LENGTH_SHORT).show();
                    sendToMainActivity();

                }
                else
                {
                    loadingBar.dismiss();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void sendToMainActivity() {
        Intent intent = new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                       if(task.isSuccessful())
                       {
                           Toast.makeText(PhoneLoginActivity.this, "Number has been verified", Toast.LENGTH_SHORT).show();
                           sendToMainActivity();
                       }
                       else
                       {
                           Toast.makeText(PhoneLoginActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                       }
                    }
                });
    }

}
