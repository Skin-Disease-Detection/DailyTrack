package com.example.dailytrack;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
public class UserHomeActivity extends AppCompatActivity {
    String employeeId,img_url;
    LocationTask locationTask;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Timer locationUpdateTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);
        Intent intent = getIntent();
        employeeId = intent.getStringExtra("employeeId");
        TextView t1=findViewById(R.id.tv_name);
        startLocationUpdateTimer();

        final LatLng[] latLng = {null};
        final Long[] radius = {null};
        FirebaseFirestore.getInstance().collection("employees").document(employeeId)
                .get().addOnCompleteListener(UserHomeActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                RequestGatePass.Name = document.getString("name");
                                img_url=document.getString("imgPath");
                                t1.setText( RequestGatePass.Name);
                            }
                        }
                    }
                });
        DocumentReference employeeDocRef = db.collection("attendance_records").document(MarkAttendanceActivity.getCurrentDate()+employeeId);
        // Get the employee document
        employeeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot employeeDocument = task.getResult();
                    if (employeeDocument.exists()) {
                        retrieveImageUrlFromFirestore();
                        Double latitude =  employeeDocument.getDouble("latitude");
                        Double longitude =  employeeDocument.getDouble("longitude");
                        if (latitude != null && longitude != null) {
                            // Create a LatLng object
                            latLng[0] = new LatLng(latitude, longitude);
                            radius[0] = employeeDocument.getLong("GEOFENCE_RADIUS");
                            GeofenceHelper geofenceHelper = new GeofenceHelper(UserHomeActivity.this); // Initialize GeofenceHelper
                            GeofencingClient  geofencingClient = LocationServices.getGeofencingClient(UserHomeActivity.this);

                            MapsActivity a = new MapsActivity();

                            addGeofence(UserHomeActivity.this,latLng[0], radius[0],geofenceHelper,geofencingClient);
                            Toast.makeText(UserHomeActivity.this,""+latitude,Toast.LENGTH_SHORT).show();


                        }
                        Toast.makeText(UserHomeActivity.this,""+latitude,Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView markAttend = findViewById(R.id.cardRules);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView attendSummary = findViewById(R.id.cardAttendSum);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView leaveAply = findViewById(R.id.cardAddUser);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) CardView leaveStus = findViewById(R.id.cardAttendStatus);

        ImageView logout = findViewById(R.id.imgLogOut);

        if (isLocationEnabled(this)) {


            markAttend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent intent = new Intent(UserHomeActivity.this, MarkAttendanceActivity.class);
                    intent.putExtra("employeeId",employeeId);
                    startActivity(intent);
                }
            });
            leaveStus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UserHomeActivity.this,GatePassStatus.class);
                    intent.putExtra("employeeId",employeeId);
                    startActivity(intent);
                }
            });
            attendSummary.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UserHomeActivity.this, Attendance_summary.class);
                    intent.putExtra("employeeId",employeeId);
                    startActivity(intent);
                   // finish();
                }
            });

            leaveAply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(UserHomeActivity.this, RequestGatePass.class);
                    intent.putExtra("employeeId",employeeId);
                    startActivity(intent);
                   // finish();
                }
            });

            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(UserHomeActivity.this,LoginActivity.class));
                    finish();
                }
            });
        }
        else {
            Toast.makeText(this, "GPS is OFF \n Please Turn on GPS And Then Login", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(UserHomeActivity.this, LoginActivity.class);
            i.putExtra("flag","false");
            startActivity(i);
            finish();
        }
    }
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(context, "GPS is OFF", Toast.LENGTH_SHORT).show();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }
    private void addGeofence(Context context, LatLng latLng, Long radius, GeofenceHelper geofenceHelper, GeofencingClient geofencingClient) {
        String geofenceId = "YOUR_GEOFENCE_ID"; // Replace with a unique geofence ID
        Geofence geofence = geofenceHelper.getGeofence(geofenceId, latLng, radius.floatValue(), Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d("TAG", "onSuccess: Geofence Added...");
                            Toast.makeText(UserHomeActivity.this, "onSuccess: Geofence Added...", Toast.LENGTH_SHORT).show();
                            GeofenceBroadcastReceiver.track = -1;
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String errorMessage = geofenceHelper.getErrorString(e);
                            Log.d("TAG", "onFailure " + errorMessage);
                        }
                    });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.out_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch(item.getItemId()){
            case R.id.logout:
                startActivity(new Intent(UserHomeActivity.this,LoginActivity.class));
                finish();
                Toast.makeText(this, "Log out...", Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);

    }
    private void startLocationUpdateTimer() {
        locationUpdateTimer = new Timer();
        locationUpdateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateLocationInFirebase();
            }
        }, 0, 1000 * 10); // Update every 60 seconds (adjust the interval as needed)
    }

    private void updateLocationInFirebase() {
         locationTask = new LocationTask(this, new LocationTask.LocationListener() {
            @Override
            public void onLocationReceived(Location location) {
                if (location != null) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();

                    // Update the location in Firebase using your AttendanceRecord model
                    storeLocationInFirebase(latitude, longitude);
                } else {
                    // Handle location retrieval failure
                    Log.e("LocationUpdate", "Failed to get location");
                }
            }

            @Override
            public void onLocationFailed() {
                // Handle location retrieval failure
                Log.e("LocationUpdate", "Failed to get location");
            }
        });

        // Execute the AsyncTask
        locationTask.execute();
    }

    private void storeLocationInFirebase(double latitude, double longitude) {
        // Get the current date
        String currentDate = MarkAttendanceActivity.getCurrentDate();
        // Create an instance of AttendanceRecord with the location details
        Map<String, Object> locationData = new HashMap<>();
        locationData.put("location", MarkAttendanceActivity.getAddressFromLatLng(this,latitude, longitude));

        // Store the AttendanceRecord in Firebase
        CollectionReference attendanceCollection = db.collection("attendance_records");
        attendanceCollection.document(currentDate + employeeId).set(locationData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("LocationUpdate", "Location stored in Firebase"+latitude+longitude);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure
                        Log.e("LocationUpdate", "Failed to store location in Firebase", e);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel the location update timer when the activity is destroyed
        if (locationUpdateTimer != null) {
            locationUpdateTimer.cancel();
            locationUpdateTimer.purge();
            if (locationTask != null) {
                locationTask.cancelTask();
            }

        }
    }
    private void retrieveImageUrlFromFirestore() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            // Retrieve image URL from Firestore
            db.collection("user_images")
                    .document(employeeId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                ImageView i = findViewById(R.id.profileImg);
                                String imageUrl = documentSnapshot.getString("imageUrl");
                                 Picasso.get().load(imageUrl).into(i);
                            } else {
                                Toast.makeText(UserHomeActivity.this, "No image URL found in Firestore", Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(UserHomeActivity.this, "Error retrieving image URL from Firestore", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }


}