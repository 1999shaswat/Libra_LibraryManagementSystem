package com.example.onboarding;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    FirebaseAuth fAuth;
    //    FirebaseFirestore fStore;
    Boolean isAdmin;
    String userID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = MainActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary)); //status bar or the time bar at the top

        fAuth = FirebaseAuth.getInstance();
//        fStore = FirebaseFirestore.getInstance();
//        isAdmin = false;


        SharedPreferences pref = getApplicationContext().getSharedPreferences("AppPref", MODE_PRIVATE);
        if (pref.getBoolean("Onboarding", true)) {

            //Thread sleep
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //Thread sleep Ends
            startActivity(new Intent(getApplicationContext(), OnboardingScr.class));
        } else {
            if (fAuth.getCurrentUser() != null) {
                userID = fAuth.getCurrentUser().getUid();
                if (userID.equals("G8LkH63ZVsTOBpy3DjL5fXZRa8c2"))
                    startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
                else
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));

//                DocumentReference documentReference = fStore.collection("users").document(userID);
//                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
//                    @Override
//                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//                        if (value != null) {
//                            String s = value.getString("name");
//                            Log.d("Tag", s + " name");
//                            if (s != null && s.equals("Admin"))
//                                isAdmin = true;
//                            Log.d("Tag", "admin set true");
//                            Log.d("Tag", String.valueOf(isAdmin) + " value");
//                            if (isAdmin)
//                                startActivity(new Intent(getApplicationContext(), AdminHomeActivity.class));
//                            else
//                                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
//                            finish();
//                        }
//                    }
//                });
            } else {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            finish();
        }

        finish();

    }
}