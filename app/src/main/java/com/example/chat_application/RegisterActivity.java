package com.example.chat_application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {

    private Button signUpButton;
    private EditText emailText,passwordText;
    private TextView alreadyhaveAccount;

    private FirebaseAuth firebaseAuth;

    private ProgressDialog progressDialog;

    private DatabaseReference RootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        progressDialog = new ProgressDialog(this);

        RootRef = FirebaseDatabase.getInstance().getReference();


        signUpButton = findViewById(R.id.register_create);
        emailText = findViewById(R.id.login_email);
        passwordText = findViewById(R.id.login_password);
        alreadyhaveAccount = findViewById(R.id.already_have_account);


        firebaseAuth = FirebaseAuth.getInstance();

        alreadyhaveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = emailText.getText().toString();
                String Password = passwordText.getText().toString();
                if(Email.isEmpty())
                {
                    emailText.setError("Please Enter Email");
                    return;
                }
                if(Password.isEmpty() || Password.length()<6)
                {
                    passwordText.setError("Pleae Enter Password. minimum length should be greater than 5");
                    return;
                }
                else
                {
                    progressDialog.setTitle("Please Wait");
                    progressDialog.setMessage("Until account has been created");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    firebaseAuth.createUserWithEmailAndPassword(Email,Password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {


                                String userId = firebaseAuth.getCurrentUser().getUid().toString();
                                String tokens  = FirebaseInstanceId.getInstance().getToken();

                                RootRef.child("Users").child(userId).setValue("");
                                RootRef.child("Users").child(userId).child("device_token").setValue(tokens);
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(RegisterActivity.this, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
