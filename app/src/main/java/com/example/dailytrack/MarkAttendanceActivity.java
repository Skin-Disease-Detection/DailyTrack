package com.example.dailytrack;

import static android.service.controls.ControlsProviderService.TAG;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.face.facerecognition.FaceMainActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class MarkAttendanceActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

    Bundle b;
    double latitude;
    double longitude;
    Button btnMark ;

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    TextView okay_text;
    static String bb = "--",cc="--";
    String employeeId,name,imgPath;
    String checkInTime, checkOutTime,workhour="--", date, AttendaceTime = "--";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        this.b=savedInstanceState;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mark_attendance);
        btnMark = findViewById(R.id.btnMark);
        Intent intent = getIntent();
        employeeId = intent.getStringExtra("employeeId");
        IN();
        DocumentReference employeeDocRef = db.collection("attendance_records").document(MarkAttendanceActivity.this.getCurrentDate() + employeeId);
        // Get the employee document
        employeeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot employeeDocument = task.getResult();
                    if (employeeDocument.exists()) {
                        checkInTime = employeeDocument.getString("CheckInTime");
                        checkOutTime = employeeDocument.getString("CheckOutTime");
                        AttendaceTime = employeeDocument.getString("AttendaceTime");
                        date = employeeDocument.getString("date");
                        cc =employeeDocument.getString("workhour");
                        bb = AttendaceTime;
                        TextView text = findViewById(R.id.Date);
                        TextView text2 = findViewById(R.id.Time1);
                        TextView text3 = findViewById(R.id.Time2);
                        text.setText("DATE :" + date);
                        text2.setText("Check In Time : "+checkInTime);
                        text3.setText("Check Out Time : " + checkOutTime );
                        Log.d(TAG, "Document data: "+workhour );

                    }
                }
            }
        });
        try {
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                return;
            }
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                // Use the location
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                // Do something with the latitude and longitude
                            }
                        }
                    });

            Dialog dialog = new Dialog(MarkAttendanceActivity.this);

            btnMark.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("ResourceType")
                @Override
                public void onClick(View view) {
                    String checkInDateTimeString = date + " " + checkInTime;
                    String checkOutDateTimeString = date + " " + checkOutTime;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

                    try {
                        Date checkInDateTime = dateFormat.parse(checkInDateTimeString);
                        Date checkOutDateTime = dateFormat.parse(checkOutDateTimeString);
                        Date currentDateTime = new Date(); // Current date and time
                        if (bb.equals("--")) {
                            if (currentDateTime.after(checkInDateTime) && currentDateTime.before(checkOutDateTime)) {
                                if (isLocationEnabled(MarkAttendanceActivity.this))
                                {
                                    fetchEmployeeData(employeeId);
                                    loadImageAsBitmap(imgPath,name);
                                } else {
                                    Toast.makeText(MarkAttendanceActivity.this, "GPS is OFF", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                // Current time is outside the CheckInTime to CheckOutTime range
                                Toast.makeText(MarkAttendanceActivity.this, "Employee is not checked in at the moment.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            {
                                if (currentDateTime.after(checkInDateTime) && currentDateTime.before(checkOutDateTime)) {
                                    if (isLocationEnabled(MarkAttendanceActivity.this)) {

                                        if (GeofenceBroadcastReceiver.track != -1) {
                                            fetchEmployeeData(employeeId);
                                            F(imgPath);
                                        } else {
                                            dialog.setContentView(R.layout.dialog_close_layout);
                                            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                            dialog.setCancelable(true);
                                            dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                                            okay_text = dialog.findViewById(R.id.okay_text);

                                            okay_text.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    dialog.dismiss();
                                                    // Toast.makeText(MarkAttendanceActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            dialog.show();
                                        }
                                    } else {
                                        Toast.makeText(MarkAttendanceActivity.this, "GPS is OFF", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    // Current time is outside the CheckInTime to CheckOutTime range
                                    Toast.makeText(MarkAttendanceActivity.this, "Employee is not checked in at the moment.", Toast.LENGTH_SHORT).show();
                                }
                            }


                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }//onclick
            });//end of onclick
        } catch (Exception e) {
            Toast.makeText(this, "On the GPS", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "onCreate: Error=" + e);
            System.out.println("last " + e);
        }
    }

    // this below code is to check Gps is on or off
    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();

                Toast.makeText(context, "GPS is OFF", Toast.LENGTH_SHORT).show();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    public static String getAddressFromLatLng(Context context, double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String addressString = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Example: "123 Main Street, City, Country"
                addressString = address.getAddressLine(0) + ", " + address.getLocality() + ", " + address.getCountryName();
            }
        } catch (IOException e) {
            Log.e("LocationHelper", "Error getting address from location", e);
        }

        return addressString;
    }

    public static String getCurrentDate() {
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        // Get the current date
        Date currentDate = new Date();
        // Format the date and return as a string
        return dateFormat.format(currentDate);
    }

    public String getCurrentDateTime() {
        // Create a SimpleDateFormat object with the desired format
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

        // Get the current date and time using Calendar
        Calendar calendar = Calendar.getInstance();
        String currentDateTime = dateFormat.format(calendar.getTime());

        // Return the formatted date and time as a string
        return currentDateTime;
    }

    public String calculateTimeDuration(String checkInTimeStr, String checkOutTimeStr) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            // Parse check-in and check-out times
            Date checkInTime = format.parse(checkInTimeStr);
            Date checkOutTime = format.parse(checkOutTimeStr);

            // Calculate duration
            long durationMilliseconds = checkOutTime.getTime() - checkInTime.getTime();

            // Convert milliseconds to hours, minutes, and seconds
            long hours = durationMilliseconds / (60 * 60 * 1000);
            long minutes = (durationMilliseconds % (60 * 60 * 1000)) / (60 * 1000);
            long seconds = (durationMilliseconds % (60 * 1000)) / 1000;

            return String.format("%d:%d:%d",hours, minutes, seconds);

        } catch (ParseException e) {
            e.printStackTrace();
            return "Error parsing dates";
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        DocumentReference employeeDocRef = db.collection("attendance_records").document(MarkAttendanceActivity.this.getCurrentDate() + employeeId);
        // Get the employee document
        employeeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot employeeDocument = task.getResult();
                    if (employeeDocument.exists()) {
                        checkInTime = employeeDocument.getString("CheckInTime");
                        checkOutTime = employeeDocument.getString("CheckOutTime");
                        AttendaceTime = employeeDocument.getString("AttendaceTime");
                        date = employeeDocument.getString("date");
                        cc =employeeDocument.getString("workhour");
                        bb = AttendaceTime;
                        TextView text = findViewById(R.id.Date);
                        TextView text2 = findViewById(R.id.Time1);
                        TextView text3 = findViewById(R.id.Time2);
                        text.setText("DATE :" + date);
                        text2.setText("Check In Time : ");
                        text3.setText("Check Out Time : " + checkOutTime + AttendaceTime );
                        Log.d(TAG, "Document data: "+workhour );

                    }
                }
            }
        });

         if(!cc.equals("--"))
        {
            btnMark.setText("NO SHEDULE FOR TODAY \n Thank YOU");
            btnMark.setEnabled(false);
        }else {
                btnMark.setText("Mark Check-OUT Attendance");
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }
    private void fetchEmployeeData(String documentId) {
        db.collection("employees")
                .document(documentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            // Retrieve name and image path
                            name = documentSnapshot.getString("name");
                            imgPath = documentSnapshot.getString("imgPath");

                            // Display the name
                            // Optionally, you can convert the image path to Bitmap

                        } else {
                            Toast.makeText(MarkAttendanceActivity.this, "Document does not exist in Firestore", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MarkAttendanceActivity.this, "Error retrieving data from Firestore", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadImageAsBitmap(String imgPath,String name)
    {
        Intent intent = new Intent(MarkAttendanceActivity.this, FaceMainActivity.class);
        intent.putExtra("imag", imgPath);
        intent.putExtra("name",name);
        intent.putExtra("employeeId",employeeId);
        startActivityForResult(intent, 1410);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Dialog dialog = new Dialog(MarkAttendanceActivity.this);

        // Check if the result is from the expected activity and if it's successful
        if (requestCode == 1410 && resultCode == RESULT_OK) {
            {

                if (GeofenceBroadcastReceiver.track != -1) {
                    String attendance_id = UUID.randomUUID().toString();
                    CollectionReference attendance = db.collection("attendance_records");
                    Map<String, Object> data1 = new HashMap<>();
                    data1.put("employeeId", employeeId);
                    data1.put("AttendaceTime", getCurrentDateTime());
                    data1.put("status", "true");
                    data1.put("location", getAddressFromLatLng(MarkAttendanceActivity.this, latitude, longitude));
                    attendance.document(getCurrentDate() + employeeId).set(data1, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {

                        }
                    });

                    dialog.setContentView(R.layout.dialog_layout);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
                    okay_text = dialog.findViewById(R.id.okay_text);

                    okay_text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    dialog.show();
                } else {
                    dialog.setContentView(R.layout.dialog_close_layout);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.animation;

                    okay_text = dialog.findViewById(R.id.okay_text);

                    okay_text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            // Toast.makeText(MarkAttendanceActivity.this, "okay clicked", Toast.LENGTH_SHORT).show();
                        }
                    });
                    dialog.show();
                }
                }
        }
        if (requestCode == 1510 && resultCode == RESULT_OK) {
            {
                {
                    String attendance_id = UUID.randomUUID().toString();
                    CollectionReference attendance = db.collection("attendance_records");
                    Map<String, Object> data1 = new HashMap<>();
                    data1.put("employeeId", employeeId);
                    data1.put("OutTime", getCurrentDateTime());
                    data1.put("workhour", calculateTimeDuration(getCurrentDateTime(), AttendaceTime));
                    data1.put("outlocation", getAddressFromLatLng(MarkAttendanceActivity.this, latitude, longitude));
                    attendance.document(getCurrentDate() + employeeId).set(data1, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            fetchEmployeeData(employeeId);
                            Toast.makeText(MarkAttendanceActivity.this, "Check Out Attendace is marked", Toast.LENGTH_SHORT).show();
                        }
                    });

                    dialog.setContentView(R.layout.dialog_layout);
                    dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    dialog.setCancelable(true);
                    dialog.getWindow().getAttributes().windowAnimations = R.style.animation;
                    okay_text = dialog.findViewById(R.id.okay_text);

                    okay_text.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                            finish();
                        }
                    });

                    dialog.show();
                }
            }
        }

    }

    public void F(String imgPath)
    {

        Intent intent = new Intent(MarkAttendanceActivity.this, FaceMainActivity.class);
        intent.putExtra("imag", imgPath);
        intent.putExtra("name",name);
        intent.putExtra("employeeId",employeeId);
        startActivityForResult(intent, 1510);

    }
public void IN()
{
    {
        super.onRestart();

        DocumentReference employeeDocRef = db.collection("attendance_records").document(MarkAttendanceActivity.this.getCurrentDate() + employeeId);
        // Get the employee document
        employeeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot employeeDocument = task.getResult();
                    if (employeeDocument.exists()) {
                        checkInTime = employeeDocument.getString("CheckInTime");
                        checkOutTime = employeeDocument.getString("CheckOutTime");
                        AttendaceTime = employeeDocument.getString("AttendaceTime");
                        date = employeeDocument.getString("date");
                        cc =employeeDocument.getString("workhour");
                        bb = AttendaceTime;
                        TextView text = findViewById(R.id.Date);
                        TextView text2 = findViewById(R.id.Time1);
                        TextView text3 = findViewById(R.id.Time2);
                        text.setText("DATE :" + date);
                        text2.setText("Check In Time : ");
                        text3.setText("Check Out Time : " + checkOutTime + AttendaceTime );
                        Log.d(TAG, "Document data: "+workhour );

                    }
                }
            }
        });

        if(!cc.equals("--"))
        {
            btnMark.setText("NO SHEDULE FOR TODAY \n Thank YOU");
            btnMark.setEnabled(false);
        }else {
            btnMark.setText("Mark Check-OUT Attendance");
        }
    }
}
}