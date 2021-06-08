package com.example.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;


public class SignupActivity extends AppCompatActivity {
    EditText mName, mUsername, mPass;
    TextView mSignup, mLogin;
    FirebaseAuth fAuth;
    ProgressBar mProgressBar;
    FirebaseFirestore fStore;
    String userID;
    Boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mProgressBar = findViewById(R.id.google_progress);
        mName = findViewById(R.id.name);
        mUsername = findViewById(R.id.username);
        mPass = findViewById(R.id.pass);
        mSignup = findViewById(R.id.signup);
        mLogin = findViewById(R.id.signin);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();


        if (fAuth.getCurrentUser() != null) {

            userID = fAuth.getCurrentUser().getUid();
            if (userID.equals("G8LkH63ZVsTOBpy3DjL5fXZRa8c2"))
                startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
            else
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
            finish();
        }

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
            }
        });

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                } catch (Exception e) {
                    // TODO: handle exception
                }

                String email = mUsername.getText().toString().trim();
                String pass = mPass.getText().toString().trim();
                String name = mName.getText().toString().trim();

                if (name.isEmpty()) {
                    mName.setError("Name is Required");
                    return;
                }
                if (email.isEmpty()) {
                    mUsername.setError("Email is Required");
                    return;
                }
                if (pass.isEmpty()) {
                    mPass.setError("Password is Required");
                    return;
                }
                if (pass.length() < 6) {
                    mPass.setError("Password must be at least six characters long");
                    return;
                }
                mProgressBar.setVisibility(View.VISIBLE);

                //register user

                fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign up success
//                            Map<String, Object> user = new HashMap<>();
//                            user.put("name", name);
//                            user.put("email", email);
                            userID = fAuth.getCurrentUser().getUid();
                            DocumentReference documentReference = fStore.collection("users").document(userID);
                            User user = new User(userID, name, email);
                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("Tag", "onSuccess: User created for " + userID);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Tag", "onFailure user create:" + e.toString());
                                }
                            });

                            Toast.makeText(SignupActivity.this, "Sign Up Successful", Toast.LENGTH_SHORT).show();
                            userID = fAuth.getCurrentUser().getUid();
                            if (userID.equals("G8LkH63ZVsTOBpy3DjL5fXZRa8c2"))
                                startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                            else
                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        } else {
                            // If sign in fails
                            Toast.makeText(SignupActivity.this, "Sign Up failed." + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });

            }
        });
    }
}