package com.example.dailytrack;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailytrack.AttendanceRecord;
import com.example.dailytrack.MarkAttendanceActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class Track extends AppCompatActivity {

    private SearchView searchView;
    private ListView myListView;
    private CustomListAdapter listAdapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        searchView = findViewById(R.id.searchView);
        myListView = findViewById(R.id.myListView);
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);

        db = FirebaseFirestore.getInstance();

        // Initialize the CustomListAdapter for AttendanceRecord
        listAdapter = new CustomListAdapter(Track.this, new ArrayList<>());
        myListView.setAdapter(listAdapter);

        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                // Handle item click if needed
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                fetchAttendanceRecords(s);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                // You can perform actions while the text is changing if needed
                return false;
            }
        });
    }

    private void fetchAttendanceRecords(String desiredName) {
        CollectionReference usersCollection = db.collection("employees");

        // Query for documents with the specified name
        Query query = usersCollection.whereEqualTo("name", desiredName);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                List<AttendanceRecord> attendanceRecords = new ArrayList<>();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    String documentId = document.getId();
                    DocumentReference employeeDocRef = db.collection("attendance_records").document(MarkAttendanceActivity.getCurrentDate() + documentId);

                    // Get the employee document
                    employeeDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot employeeDocument = task.getResult();
                                if (employeeDocument.exists()) {
                                    // Parse attendance record data
                                    String checkInTime = employeeDocument.getString("CheckInTime");
                                    String checkOutTime = employeeDocument.getString("CheckOutTime");
                                    String attendaceTime = employeeDocument.getString("AttendaceTime");
                                    String location = employeeDocument.getString("location");
                                    String date = employeeDocument.getString("date");
                                    String status = employeeDocument.getString("status");
                                    String name = employeeDocument.getString("name");
                                    String workhour = employeeDocument.getString("workhour");
                                    String OutTime = employeeDocument.getString("OutTime");
                                    // Create AttendanceRecord object
                                    AttendanceRecord attendanceRecord = new AttendanceRecord(documentId, attendaceTime, checkInTime, checkOutTime, date, location, status, name,workhour,OutTime);

                                    // Add to the list
                                    attendanceRecords.add(attendanceRecord);
                                }
                            } else {
                                // Handle errors
                                Exception e = task.getException();
                                if (e != null) {
                                    e.printStackTrace();
                                }
                            }

                            // Update the adapter with the new data
                            listAdapter.setData(attendanceRecords);
                        }
                    });
                }
            }
        });
    }
}
class CustomListAdapter extends ArrayAdapter<AttendanceRecord> {

    private final Context context;
    private final List<AttendanceRecord> attendanceRecords;

    public CustomListAdapter(Context context, List<AttendanceRecord> attendanceRecords) {
        super(context, R.layout.list_item_attendance, attendanceRecords);
        this.context = context;
        this.attendanceRecords = attendanceRecords;
    }

    public void setData(List<AttendanceRecord> data) {
        attendanceRecords.clear();
        attendanceRecords.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.list_item_attendance, parent, false);
        }

        // Get the current attendance record
        AttendanceRecord attendanceRecord = getItem(position);

        if (attendanceRecord != null) {
            // Bind data to the views
            TextView nameTextView = convertView.findViewById(R.id.textViewName);
            TextView locationTextView = convertView.findViewById(R.id.textViewLocation);
            TextView timeTextView = convertView.findViewById(R.id.textViewTime);
            TextView dateTextView = convertView.findViewById(R.id.textViewDate);

            nameTextView.setText(attendanceRecord.getName());
            locationTextView.setText("Last Location: " + attendanceRecord.getLocation());
            timeTextView.setText("Last Location Time: " + attendanceRecord.getAttendaceTime());
            dateTextView.setText("Last Location Date: " + attendanceRecord.getDate());
        }

        return convertView;
    }
}

