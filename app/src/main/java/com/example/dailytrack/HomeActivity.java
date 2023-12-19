package com.example.dailytrack;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);


        SharedPreferences sharedPreferences = getSharedPreferences("shred_prefs",MODE_PRIVATE);

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView cardRules = findViewById(R.id.cardRules);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView cardMap = findViewById(R.id.cardmap);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView cardAddUser = findViewById(R.id.cardAddUser);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"})CardView cardAtndStatus = findViewById(R.id.cardAttendStatus);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView CardMsg = findViewById(R.id.cardMsg);
        CardView CardTrack = findViewById(R.id.cardTrack);

        ImageView logout = findViewById(R.id.imgLogOut);

        cardAtndStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,ApproveGatePass.class));

            }
        });
        CardMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,AttendanceDetailsActivity.class));

            }
        });
        cardRules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,RulesActivity.class));
               // finish();
            }
        });
        CardTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.clear();
                editor.apply();

                startActivity(new Intent(HomeActivity.this,Track.class));

            }
        });

         cardMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HomeActivity.this,MapsActivity.class));
            }
        });

         cardAddUser.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(HomeActivity.this,AddUserActivity.class));
                 //finish();
             }
         });

         logout.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                 finish();
             }
         });
    }



   /* @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.out_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.logout:
                startActivity(new Intent(HomeActivity.this,LoginActivity.class));
                finish();
                Toast.makeText(this, "Log out...", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
*/
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseMessaging.getInstance().subscribeToTopic("admin_topic")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "Subscribed to admin_topic");
                        } else {
                            Log.e("TAG", "Subscription to admin_topic failed", task.getException());
                        }
                    }
                });

    }
}