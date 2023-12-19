package com.example.dailytrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

// ... (Your existing imports)

public class Attendance_summary extends AppCompatActivity {
    String employeeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        employeeId = intent.getStringExtra("employeeId");
        setContentView(R.layout.activity_attendance_summary);

        // Example data (replace with your actual data)
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference attendanceRecordsCollection = db.collection("attendance_records");

        // Fetch data for a specific employeeId
        Query query = attendanceRecordsCollection.whereEqualTo("employeeId", employeeId);
        List<String[]> data = new ArrayList<>();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    AttendanceRecord attendanceRecord = document.toObject(AttendanceRecord.class);
                    // Use the attendanceRecord object
                    String checkInDateTime = attendanceRecord.getDate();
                    String AttendaceTime = attendanceRecord.getAttendaceTime();
                    String Outtime = attendanceRecord.getOutTime();
                    String WorkHour = attendanceRecord.getWorkhour();
                    data.add(new String[]{checkInDateTime, AttendaceTime,Outtime,WorkHour});
                }

                // Populate the ListView after the Firestore query completes
                ListView listView = findViewById(R.id.listView);
                populateListView(listView, data);
            } else {
                // Handle errors
            }
        });
    }

    private void populateListView(ListView listView, List<String[]> data) {
        CustomListAdapter2 adapter = new CustomListAdapter2(this, data);
        listView.setAdapter(adapter);
    }
}
class CustomListAdapter2 extends BaseAdapter {
    private List<String[]> data;
    private Context context;

    public CustomListAdapter2(Context context, List<String[]> data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_layout, parent, false);
        }

        String[] rowData = data.get(position);

        TextView textViewDate = convertView.findViewById(R.id.textViewDate);
        TextView textViewTime = convertView.findViewById(R.id.textViewTime);
        TextView textViewStatus = convertView.findViewById(R.id.textViewStatus);
        TextView workHour = convertView.findViewById(R.id.workhoue);

        textViewDate.setText(rowData[0]);
        textViewTime.setText(rowData[1]);
        textViewStatus.setText(rowData[2]);
        workHour.setText(rowData[3]);

        return convertView;
    }
}
