package com.example.dailytrack;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class RequestGatePass extends AppCompatActivity {
    private String gatePassId;
    String edtoutTime;
    String employeeId;
    String selectedDate;
    static String Name;
    EditText date;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
         employeeId = intent.getStringExtra("employeeId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        setContentView(R.layout.gatepass_request);
        EditText edtOutTime = findViewById(R.id.editTextTime);
        EditText reason = findViewById(R.id.editTextReason);
        Button submit = findViewById(R.id.buttonApply);
        Button cancel = findViewById(R.id.buttonCancle);
        date = findViewById(R.id.editTextdate);
        edtOutTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar c = Calendar.getInstance();

                // on below line we are getting our hour, minute.
                int hour = c.get(Calendar.HOUR_OF_DAY);
                int minute = c.get(Calendar.MINUTE);

                // on below line we are initializing our Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(RequestGatePass.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                edtOutTime.setText(hourOfDay + ":" + minute);
                                edtoutTime=hourOfDay + ":" + minute;
                            }

                        }, hour, minute, false);
                timePickerDialog.show();
            }
        });
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });
        // ...

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gatePassId = UUID.randomUUID().toString();

                CollectionReference gatePasses = db.collection("gatePasses");
                Map<String, Object> data1 = new HashMap<>();
                data1.put("status", "null");
                data1.put("employeeId", employeeId);
                data1.put("requestDate", selectedDate);
                data1.put("requestTime", edtoutTime);

                FirebaseFirestore.getInstance().collection("employees").document(employeeId)
                        .get().addOnCompleteListener(RequestGatePass.this, new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        RequestGatePass.Name = document.getString("name");
                                        data1.put("name", RequestGatePass.Name);

                                        // Add the rest of your logic that depends on the data here
                                        data1.put("reason", reason.getText().toString());
                                        Toast.makeText(RequestGatePass.this, "DONE", Toast.LENGTH_SHORT).show();

                                        gatePasses.document(gatePassId).set(data1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                Toast.makeText(RequestGatePass.this, "Request Sent ", Toast.LENGTH_SHORT).show();
                                                finish();
                                            }
                                        });
                                    }
                                }
                            }
                        });
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void showDatePickerDialog() {
        // Initialize a Calendar instance to get the current date
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create a DatePickerDialog and set the date picker listener
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // The date selected by the user
                        selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
                        // Update the text in the EditText with the selected date
                        date.setText(selectedDate);
                    }
                },
                year,
                month,
                dayOfMonth
        );

        // Show the date picker dialog
        datePickerDialog.show();
    }

}
