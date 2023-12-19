package com.example.dailytrack;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// AttendanceDetailsActivity.java
public class AttendanceDetailsActivity extends AppCompatActivity {

    private DatePicker datePicker;
    private ListView listView;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_details);

        datePicker = findViewById(R.id.datePicker);
        listView = findViewById(R.id.listView);
        db = FirebaseFirestore.getInstance();

        // Set a listener for date changes
        datePicker.init(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                (view, year, monthOfYear, dayOfMonth) -> updateListView(year, monthOfYear + 1, dayOfMonth));

        // Initial data load with the current date
        updateListView(datePicker.getYear(), datePicker.getMonth() + 1, datePicker.getDayOfMonth());
    }

    private void updateListView(int year, int month, int day) {
        String selectedDate = year + "-" + (month < 10 ? "0" : "") + month + "-" + (day < 10 ? "0" : "") + day;

        // Query your attendance_records collection for details of employees for the selected date
        db.collection("attendance_records")
                .whereEqualTo("date", selectedDate)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<AttendanceRecord> employeeDetailsList = new ArrayList<>();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            AttendanceRecord employeeDetails = document.toObject(AttendanceRecord.class);
                            employeeDetailsList.add(employeeDetails);
                        }

                        // Update the ListView with the details
                        EmployeeDetailsAdapter adapter = new EmployeeDetailsAdapter(this, R.layout.employee_details_item, employeeDetailsList);
                        listView.setAdapter(adapter);
                    } else {
                        Log.d("TAG", "Error getting documents: ", task.getException());
                    }
                });
    }
}

// EmployeeDetailsAdapter.java
 class EmployeeDetailsAdapter extends ArrayAdapter<AttendanceRecord> {

    private Context context;
    private int resource;

    public EmployeeDetailsAdapter(Context context, int resource, List<AttendanceRecord> employeeDetailsList) {
        super(context, resource, employeeDetailsList);
        this.context = context;
        this.resource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        AttendanceRecord employeeDetails = getItem(position);

        if (employeeDetails != null) {
            TextView employeeNameTextView = convertView.findViewById(R.id.employeeNameTextView);
            TextView attendanceStatusTextView = convertView.findViewById(R.id.attendanceStatusTextView);

            // Set data to views
            employeeNameTextView.setText(employeeDetails.getName());
            attendanceStatusTextView.setText("Status: " + employeeDetails.getStatus());

            // Add other views and data as needed
        }

        return convertView;
    }
}
