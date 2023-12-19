package com.example.dailytrack;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

 public class ApproveGatePass extends AppCompatActivity implements MyListAdapter.OnApprovalClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gate_pass_aproval);

        ListView listView = findViewById(R.id.list);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("gatePasses").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<GatePass2> gatePassList = new ArrayList<>();

                    for (QueryDocumentSnapshot document : task.getResult()) {
                        GatePass2 gatePass = GatePass2.fromDocumentSnapshot(document);
                        gatePassList.add(gatePass);
                    }

                    MyListAdapter adapter = new MyListAdapter(ApproveGatePass.this, gatePassList, ApproveGatePass.this);
                    listView.setAdapter(adapter);
                } else {
                    // Handle errors
                    Toast.makeText(ApproveGatePass.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

     @Override
     public void onApproveClick(String gatePassId) {
         // Handle approval logic here
         FirebaseFirestore db = FirebaseFirestore.getInstance();

         // Update the status field to "true" for the specified gate pass document
         db.collection("gatePasses").document(gatePassId)
                 .update("status", "true")
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         Toast.makeText(ApproveGatePass.this, "Gate Pass Approved", Toast.LENGTH_SHORT).show();
                         // Optionally, you can refresh the list or take other actions
                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Toast.makeText(ApproveGatePass.this, "Error updating status", Toast.LENGTH_SHORT).show();
                     }
                 });
     }


     @Override
    public void onRejectClick(String gatePassId) {
         FirebaseFirestore db = FirebaseFirestore.getInstance();

         // Update the status field to "true" for the specified gate pass document
         db.collection("gatePasses").document(gatePassId)
                 .update("status", "false")
                 .addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {
                         Toast.makeText(ApproveGatePass.this, "Gate Pass Approved", Toast.LENGTH_SHORT).show();
                         // Optionally, you can refresh the list or take other actions
                     }
                 })
                 .addOnFailureListener(new OnFailureListener() {
                     @Override
                     public void onFailure(@NonNull Exception e) {
                         Toast.makeText(ApproveGatePass.this, "Error updating status", Toast.LENGTH_SHORT).show();
                     }
                 });
        // Handle rejection logic here
        Toast.makeText(this, "Reject clicked for Gate Pass ID: " + gatePassId, Toast.LENGTH_SHORT).show();
    }
}


 class MyListAdapter extends ArrayAdapter<GatePass2> {

    private final Activity context;
    private final List<GatePass2> gatePassList;
    private final OnApprovalClickListener approvalClickListener;

    public MyListAdapter(Activity context, List<GatePass2> gatePassList, OnApprovalClickListener listener) {
        super(context, R.layout.gate_pass_approvallayout, gatePassList);
        this.context = context;
        this.gatePassList = gatePassList;
        this.approvalClickListener = listener;
    }

    public interface OnApprovalClickListener {
        void onApproveClick(String gatePassId);
        void onRejectClick(String gatePassId);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.gate_pass_approvallayout, null, true);
        }

        TextView nametv = convertView.findViewById(R.id.tv_name);
        TextView datetv = convertView.findViewById(R.id.tv_Date);
        TextView timetv = convertView.findViewById(R.id.tv_Time);
        TextView reasontv = convertView.findViewById(R.id.tv_Reason);
        Button approveButton = convertView.findViewById(R.id.buttonAccept);
        Button rejectButton = convertView.findViewById(R.id.buttonReject);

        GatePass2 gatePass = gatePassList.get(position);
        String gatePassId = gatePass.getGatePassId();

        nametv.setText(gatePass.getName());
        datetv.setText(gatePass.getRequestDate());
        timetv.setText(gatePass.getRequestTime());
        reasontv.setText(gatePass.getReason());

        // Set click listeners for the buttons
        approveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (approvalClickListener != null) {
                    approvalClickListener.onApproveClick(gatePassId);
                }
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (approvalClickListener != null) {
                    approvalClickListener.onRejectClick(gatePassId);
                }
            }
        });

        return convertView;
    }
}

 class GatePass2 {
    private String gatePassId;
    private String employeeId;
    private String name;
    private String reason;
    private String requestDate;
    private String requestTime;
    private String status;

    // Default constructor required for calls to DataSnapshot.getValue(GatePass.class)
    public GatePass2() {
    }

    public String getGatePassId() {
        return gatePassId;
    }

    public void setGatePassId(String gatePassId) {
        this.gatePassId = gatePassId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    public String getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Function to convert DocumentSnapshot to GatePass object
    public static GatePass2 fromDocumentSnapshot(QueryDocumentSnapshot documentSnapshot) {
        GatePass2 gatePass = new GatePass2();
        gatePass.setGatePassId(documentSnapshot.getId());
        gatePass.setEmployeeId(documentSnapshot.getString("employeeId"));
        gatePass.setReason(documentSnapshot.getString("reason"));
        gatePass.setRequestDate(documentSnapshot.getString("requestDate"));
        gatePass.setRequestTime(documentSnapshot.getString("requestTime"));
        gatePass.setName(documentSnapshot.getString("name"));
        gatePass.setStatus(documentSnapshot.getString("status"));

        return gatePass;
    }
}
