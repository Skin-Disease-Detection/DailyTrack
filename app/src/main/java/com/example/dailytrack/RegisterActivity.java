package com.example.dailytrack;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();


        EditText edtUname = findViewById(R.id.editTextRegUsername);
        EditText edtMail = findViewById(R.id.editTextRegMail);
        EditText edtPass = findViewById(R.id.editTextRegPassword);
        EditText edtConPass = findViewById(R.id.editTextRegPassword2);
        Button btnReg = findViewById(R.id.Regbtn);
        TextView tvLg = findViewById(R.id.tvExistUser);

       // MyDataBase db = new MyDataBase(getApplicationContext(),"HealthCare",null,1);

        btnReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uname = edtUname.getText().toString();
                String email = edtMail.getText().toString();
                String pass = edtPass.getText().toString();
                String conPass = edtConPass.getText().toString();

                if(uname.length()==0||email.length()==0||pass.length()==0||conPass.length()==0){
                    Toast.makeText(RegisterActivity.this, "Please fill all the details ", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(pass.compareTo(conPass)==0){

                        if(isValidPassword(pass)){
                            mAuth.createUserWithEmailAndPassword(email, pass)
                                    .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Registered Successfuly", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                                            } else {
                                                // If sign in fails, display a message to the user.

                                                Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                            Toast.makeText(RegisterActivity.this, "Registered Successfuly", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "Password should contain at least eight characters, consists of only letters and digits,must contain at least two digits.", Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(RegisterActivity.this, "Password and confirm password did not match", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        tvLg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
            }
        });



    }

    public boolean isValidPassword(String password){
        if (password.length() < 8) {
            return false;
        } else {
            char c;
            int count = 1;
            for (int i = 0; i < password.length() - 1; i++) {
                c = password.charAt(i);
                if (!Character.isLetterOrDigit(c)) {
                    return false;
                } else if (Character.isDigit(c)) {
                    count++;
                    if (count < 2)   {
                        return false;
                    }
                }
            }
        }
        return true;
    }
}