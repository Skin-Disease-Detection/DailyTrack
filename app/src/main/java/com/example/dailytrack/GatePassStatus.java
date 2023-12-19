package com.example.dailytrack;

import static android.graphics.Color.RED;
import static android.graphics.Color.YELLOW;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class GatePassStatus extends AppCompatActivity {


    private static final String TAG = "GatePassStatus";
    private FirebaseFirestore db;
    String employeeId2;
    private ListView listView;
    private GatePassAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatepass_status);

        Intent intent = getIntent();
        employeeId2= intent.getStringExtra("employeeId");


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Assuming you have a ListView in your layout with id 'listView'
        listView = findViewById(R.id.listView);

        // Create the custom adapter
        adapter = new GatePassAdapter(this, new ArrayList<>());

        // Set the adapter to the ListView
        listView.setAdapter(adapter);

        // Fetch and display data
        fetchData();
    }

    private void fetchData() {
        String employeeId = employeeId2; // Replace with the desired employeeId

        db.collection("gatePasses")
                .whereEqualTo("employeeId", employeeId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            List<GatePass2> gatePassList = new ArrayList<>();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Convert Firestore document to GatePass object
                                GatePass2 gatePass = document.toObject(GatePass2.class);
                                gatePassList.add(gatePass);
                            }

                            // Update the adapter with the fetched data
                            adapter.addAll(gatePassList);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}

class GatePassAdapter extends ArrayAdapter<GatePass2> {

    public GatePassAdapter(Context context, List<GatePass2> gatePassList) {
        super(context, 0, gatePassList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        GatePass2 gatePassItem = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_gate_pass, parent, false);
        }

        // Lookup view for data population
        TextView tvDate = convertView.findViewById(R.id.tv_Date);
        TextView tvTime = convertView.findViewById(R.id.tv_Time);
        TextView tvReason = convertView.findViewById(R.id.tv_Reason);
        Button btnAccept = convertView.findViewById(R.id.buttonAccept);

        // Populate the data into the template view using the data object
        if (gatePassItem != null) {
            tvDate.setText(gatePassItem.getRequestDate());
            tvTime.setText(gatePassItem.getRequestTime());
            tvReason.setText(gatePassItem.getReason());

            // Set a click listener for the button
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Handle button click
                    getStatusAndSetText(gatePassItem, btnAccept);
                }
            });
        }

        // Return the completed view to render on screen
        return convertView;
    }

    private void getStatusAndSetText(GatePass2 gatePass, Button btnAccept) {
        // Your logic to get the status and set the text on the button
        String status = gatePass.getStatus();

        if (!status.equals("null") ) {
            if (status.equals("true")) {
                // Status is true, set text as Approved
                btnAccept.setText("Approved");
            } else {
                // Status is false, set text as Rejected
                btnAccept.setText("Rejected");
            }
        } else {

            btnAccept.setText("Pending");
        }

    }
}
