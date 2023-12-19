package com.example.dailytrack;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class AddUserActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;


    String UID ;
    String imgid;
    EditText EmpName;
    static int flag=0;
    StorageReference storageReference;
    ImageView s;
    FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference("images");

      Button  uploadButton = findViewById(R.id.uploadImageButton);
      s=findViewById(R.id.face_preview);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        db = FirebaseFirestore.getInstance();
        EmpName = findViewById(R.id.editTextName);
        EditText EmpMail = findViewById(R.id.editTextEmail);
        EditText EmpPhone = findViewById(R.id.editTextPhone);
        EditText Emppass  = findViewById(R.id.pass);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) EditText EmpDesign = findViewById(R.id.editTextDesign);

        Button btnAdd = findViewById(R.id.buttonAddEmployee);
        Button btnCancle = findViewById(R.id.buttonCancleEmployee);
       // Button b = findViewById(R.id.button_add_face);


        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name;
                String phone;
                String  email = EmpMail.getText().toString();
               String pass = Emppass.getText().toString();
               name = EmpName.getText().toString();
               phone =EmpPhone.getText().toString();

                mAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    UID = mAuth.getCurrentUser().getUid();
                                    CollectionReference employees = db.collection("employees");
                                    Map<String, Object> data1 = new HashMap<>();
                                    data1.put("name", name);
                                    data1.put("phone", phone);
                                    data1.put("email", email);
                                    data1.put("pass", pass);
                                    data1.put("imgPath",imgid);


                                        employees.document(UID).set(data1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                saveImageUrlToFirestore(imgid,UID);
                                              //  Intent i = new Intent(AddUserActivity.this, FaceMainActivity.class);
                                               // i.putExtra("employeeId",UID);
                                             //   startActivity(i );
                                                AlertDialog.Builder builder = new AlertDialog.Builder(AddUserActivity.this);
                                                builder.setTitle("success")
                                                        .setMessage("User Added Successfuly")
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                finish();
                                                            }
                                                        })
                                                        .show();
                                            }

                                        });
                                    }else{
                                        Toast.makeText(AddUserActivity.this ,"Please Add Face ",Toast.LENGTH_SHORT).show();
                                    }
                            }
                        });
            }
        });


        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    private void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            uploadImage(imageUri);
            s.setImageURI(imageUri);
        }
    }

    private void uploadImage(Uri imageUri) {
        if (imageUri != null) {
            // Generate a unique name for the image
            String imageName = System.currentTimeMillis() + "_" +EmpName.getText().toString();

            final StorageReference imageRef = storageReference.child(imageName);
            imageRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Image upload success
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUri) {
                                    // Save download URL to Firestore
                                    imgid=downloadUri.toString();

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Handle errors during upload
                            Toast.makeText(AddUserActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void saveImageUrlToFirestore(String imageUrl,String s) {
            // Save image URL to Firestore
            Map<String, Object> data = new HashMap<>();
            data.put("imageUrl", imageUrl);

            db.collection("user_images")
                    .document(s)
                    .set(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(AddUserActivity.this, "Image URL saved to Firestore", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddUserActivity.this, "Error saving image URL to Firestore", Toast.LENGTH_SHORT).show();
                        }
                    });
    }
    }