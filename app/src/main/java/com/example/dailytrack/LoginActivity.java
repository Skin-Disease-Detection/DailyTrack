package com.example.dailytrack;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser user;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Intent intent = getIntent();
        String b  = intent.getStringExtra("flag");
        if(b!=null && b.equals("false"))
        {

            showGpsDialog();
        }

        EditText edtUsername = findViewById(R.id.login_email);
        EditText edtpass = findViewById(R.id.login_password);
        Button btnLogin = findViewById(R.id.login_button);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uname = edtUsername.getText().toString();
                String pass = edtpass.getText().toString();
                Log.d(uname, "onClick: "+uname);
                if(uname.length()==0 || pass.length()==0){
                    Toast.makeText(LoginActivity.this, "Please fill all the details", Toast.LENGTH_SHORT).show();
                }
                else{
                        mAuth.signInWithEmailAndPassword(uname, pass)
                                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            user = mAuth.getCurrentUser();
                                            Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(LoginActivity.this, UserHomeActivity.class);
                                            intent.putExtra("employeeId", user.getUid());
                                            startActivity(intent);
                                            Log.d("tag","Login Sucesss");
                                            Log.d("tag","Login Sucesss");
                                            finish();

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            if(uname.equals("admin")) {
                                                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                                Log.d("tag","Login Sucesss");
                                                Log.d("tag","Login Sucesss");
                                                finish();
                                            }else {
                                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                }
            }
        });


    }
    private void showGpsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please turn on GPS to log in.")
                .setCancelable(false)
                .setPositiveButton("Turn On GPS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Open GPS settings
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

}