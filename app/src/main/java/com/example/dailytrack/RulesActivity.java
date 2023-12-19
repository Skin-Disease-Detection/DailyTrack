package com.example.dailytrack;

import static android.service.controls.ControlsProviderService.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.BlockingDeque;

public class RulesActivity extends AppCompatActivity {
    String edtintIme, edtoutTime;
    Calendar c = Calendar.getInstance();
    EditText Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        EditText edtInTIme = findViewById(R.id.editTextInTime);
        EditText edtOutTime = findViewById(R.id.editTextOutTime);
        Date = findViewById(R.id.editTextInDate);
        Button buttonSet = findViewById(R.id.buttonSet);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) Button buttonCancle = findViewById(R.id.buttonCancle);

        edtInTIme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // on below line we are getting our hour, minute.
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // on below line we are initializing our Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(RulesActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                edtInTIme.setText(hourOfDay + ":" + minute);
                                edtintIme = (hourOfDay + ":" + minute);
                            }

                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });

        edtOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();

                // on below line we are getting our hour, minute.
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // on below line we are initializing our Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(RulesActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                edtOutTime.setText(hourOfDay + ":" + minute);
                                edtoutTime = hourOfDay + ":" + minute;
                            }

                        }, hour, minute, false);
                // at last we are calling show to
                // display our time picker dialog.
                timePickerDialog.show();
            }
        });


        Date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();

            }
        });
        buttonSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAttendanceRecord();

               // startActivity(new Intent(RulesActivity.this,HomeActivity.class));
                finish();
            }
        });

        buttonCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              //  startActivity(new Intent(RulesActivity.this, HomeActivity.class));
                finish();
            }
        });

    }

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                dateSetListener,
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void updateDateInView() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", new Locale("en", "IN"));
        Date.setText(sdf.format(c.getTime()));
    }

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            // Update calendar with the selected date
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);

            // Update the TextView with the selected date
            updateDateInView();
        }
    };

    private void fetchEmployeeNameAndAddRecord(String employeeId, Map<String, Object> data) {
        FirebaseFirestore.getInstance().collection("employees").document(employeeId)
                .get().addOnCompleteListener(RulesActivity.this, new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                RequestGatePass.Name = document.getString("name");
                                data.put("name", RequestGatePass.Name);

                                // Add the data to the attendance_records collection
                                FirebaseFirestore.getInstance().collection("attendance_records")
                                        .document(data.get("date") + employeeId)
                                        .set(data)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(RulesActivity.this, "Rules are set...", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                });
    }

    private void addAttendanceRecord() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String date = Date.getText().toString();
        CollectionReference employees = db.collection("attendance_records");
        Map<String, Object> data1 = new HashMap<>();

        data1.put("date", date);
        data1.put("CheckInTime", edtintIme);
        data1.put("CheckOutTime", edtoutTime);
        data1.put("status", "false");
        data1.put("AttendaceTime", "--");
        data1.put("CheckInTime", edtintIme);
        data1.put("workhour", "--");



        db.collection("employees").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Assuming 'employeeId' is the field containing the employee ID
                        String employeeId = document.getId();
                        if (employeeId != null) {
                            data1.put("employeeId", employeeId);
                            fetchEmployeeNameAndAddRecord(employeeId, data1);
                        }
                    }
                }
            }
        });
    }
}
