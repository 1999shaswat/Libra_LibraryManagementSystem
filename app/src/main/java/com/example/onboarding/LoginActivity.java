package com.example.onboarding;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {
    EditText mUsername, mPass;
    TextView mSignup, mLogin;
    FirebaseAuth fAuth;
    ProgressBar mProgressBar;
    String userID;
    //    FirebaseFirestore fStore;
    Boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mProgressBar = findViewById(R.id.google_progress);

        mUsername = findViewById(R.id.username);
        mPass = findViewById(R.id.pass);
        mSignup = findViewById(R.id.signup);
        mLogin = findViewById(R.id.signin);
        fAuth = FirebaseAuth.getInstance();
//        fStore = FirebaseFirestore.getInstance();
        isAdmin = false;
//        if (fAuth.getCurrentUser() != null) {
//            userID = fAuth.getCurrentUser().getUid();
//            DocumentReference documentReference = fStore.collection("users").document(userID);
//            documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                @Override
//                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                    if (value != null) {
//                        String s = value.getString("name");
//                        Log.d("Tag", s + " name");
//                        if (s != null && s.equals("Admin"))
//                            isAdmin = true;
//                        Log.d("Tag", "admin set true");
//                        Log.d("Tag", String.valueOf(isAdmin) + " value");
//                        if (isAdmin)
//                            startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
//                        else
//                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//                        finish();
//                    }
//                }
//            });
//        }

        mSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), SignupActivity.class));
                finish();
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
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

                //authenticate the user
                fAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Toast.makeText(LoginActivity.this, "Sign in Successful", Toast.LENGTH_SHORT).show();

                            if (fAuth.getCurrentUser() != null) {
                                userID = fAuth.getCurrentUser().getUid();
                                if (userID.equals("G8LkH63ZVsTOBpy3DjL5fXZRa8c2"))
                                    startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                                else
                                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                finish();
//                                DocumentReference documentReference = fStore.collection("users").document(userID);
//                                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                                    @Override
//                                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                                        if (value != null) {
//                                            String s = value.getString("name");
//                                            Log.d("Tag", s + " name");
//                                            if (s != null && s.equals("Admin"))
//                                                isAdmin = true;
//                                            Log.d("Tag", "admin set true");
//                                            Log.d("Tag", String.valueOf(isAdmin) + " value");
//                                            if (isAdmin)
//                                                startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
//                                            else
//                                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//                                            finish();
//                                        }
//                                    }
//                                });
                            }

                        } else {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(LoginActivity.this, "Authentication failed." + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            mProgressBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }
}