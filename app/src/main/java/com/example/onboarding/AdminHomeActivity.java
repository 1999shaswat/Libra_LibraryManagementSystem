package com.example.onboarding;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminHomeActivity extends AppCompatActivity {
    Dialog addBookDialog, viewBookDialog, viewRequestedBookDialog;
    TextView addBooks, mConfirm, viewBooks, admin_booksAvailable, admin_requestsCount;
    ImageView closeNewBookPopup, closeViewBookPopup, closeViewRequestedBookPopup;
    EditText mbookname, mbookqty;
    FirebaseFirestore db;
    ProgressBar mAddBookProgress, mViewBookProgress, mViewRequestedBookProgress, madminHomeLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        madminHomeLoading = findViewById(R.id.adminhome_loading);
        addBooks = findViewById(R.id.add_books);
        viewBooks = findViewById(R.id.view_books);
        addBookDialog = new Dialog(this);
        viewBookDialog = new Dialog(this);
        viewRequestedBookDialog = new Dialog(this);
        db = FirebaseFirestore.getInstance();
        admin_booksAvailable = findViewById(R.id.admin_booksavailable);
        admin_requestsCount = findViewById(R.id.admin_requestscount);
        adminUpdateInfo(true);
    }

    public void adminUpdateInfo(boolean update_Request_Count) {
        madminHomeLoading.setVisibility(View.VISIBLE);

        db.collection("library").addSnapshotListener(AdminHomeActivity.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("TAG", "Listen failed.", e);
                    return;
                }
                int booksAvailable = 0;
                for (QueryDocumentSnapshot documentSnapshot : value) {
                    Book book = documentSnapshot.toObject(Book.class);
                    booksAvailable += book.getQty();
                }
                if (!update_Request_Count)
                    madminHomeLoading.setVisibility(View.GONE);
                admin_booksAvailable.setText(String.valueOf(booksAvailable));
            }
        });


        if (update_Request_Count) {
            db.collection("requested").addSnapshotListener(AdminHomeActivity.this, new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w("TAG", "Listen failed.", e);
                        return;
                    }
                    int requestsCount = 0;
                    for (QueryDocumentSnapshot documentSnapshot : value) {
                        requestsCount += 1;
                    }
                    madminHomeLoading.setVisibility(View.GONE);
                    admin_requestsCount.setText(String.valueOf(requestsCount));

                }
            });
        }
    }

    public void mViewBooks(View view) {
        viewBookDialog.setContentView(R.layout.admin_view_books_popup);
        mViewBookProgress = viewBookDialog.findViewById(R.id.viewbook_progress);
        mViewBookProgress.setVisibility(View.VISIBLE);
        closeViewBookPopup = viewBookDialog.findViewById(R.id.close_view_books_popup);
        LinearLayout llViewBooks = viewBookDialog.findViewById(R.id.llviewbooks);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        closeViewBookPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewBookDialog.dismiss();
            }
        });

        db.collection("library").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mViewBookProgress.setVisibility(View.GONE);
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Book book = documentSnapshot.toObject(Book.class);
                    String data = book.getBookname() + "\t\t\t" + book.getQty();
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText(data);
                    textView.setTextSize(20);
                    textView.setLayoutParams(params);
                    llViewBooks.addView(textView);
                }
            }
        });
        Window window = viewBookDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        viewBookDialog.show();
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        finish();
    }

    public void showNewBookPopup(View view) {
        addBookDialog.setContentView(R.layout.admin_new_books_popup);
        closeNewBookPopup = addBookDialog.findViewById(R.id.close_new_books_popup);
        mConfirm = addBookDialog.findViewById(R.id.confirm);
        mbookname = addBookDialog.findViewById(R.id.bookname);
        mbookqty = addBookDialog.findViewById(R.id.quantity);
        mAddBookProgress = addBookDialog.findViewById(R.id.upload_progress);
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAddBookProgress.setVisibility(View.VISIBLE);
                String bookname, tmpbookqty;
                int bookqty;
                bookname = mbookname.getText().toString().trim();
                tmpbookqty = mbookqty.getText().toString().trim();
                if (bookname.length() > 0 && tmpbookqty.length() > 0) {
                    bookqty = Integer.parseInt(tmpbookqty);
                    //Create book class and upload to db
                    Book book = new Book(bookname, bookqty);
                    db.collection("library").document(book.getHashID()).set(book).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            adminUpdateInfo(false);
                            addBookDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Book added", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Tag", "onFailure book add:" + e.toString());
                            Toast.makeText(getApplicationContext(), "Error adding books" + e.toString(), Toast.LENGTH_SHORT).show();
                            mAddBookProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                } else if (bookname.length() > 0) {
                    mbookqty.setError("Length is Required");
                    mAddBookProgress.setVisibility(View.GONE);
                } else {
                    mbookname.setError("Book Name is Required");
                    mAddBookProgress.setVisibility(View.GONE);
                }
            }
        });
        closeNewBookPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addBookDialog.dismiss();
            }
        });
        Window window = addBookDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        addBookDialog.show();
    }

    public void viewRequestedBooks(View view) {
        viewRequestedBookDialog.setContentView(R.layout.view_requested_books);
        mViewRequestedBookProgress = viewRequestedBookDialog.findViewById(R.id.viewrequestedbookprogress);
        mViewRequestedBookProgress.setVisibility(View.VISIBLE);
        closeViewRequestedBookPopup = viewRequestedBookDialog.findViewById(R.id.close_viewrequested_books_popup);
        LinearLayout llViewRequestedBooks = viewRequestedBookDialog.findViewById(R.id.llviewrequested_books);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        closeViewRequestedBookPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewRequestedBookDialog.dismiss();
            }
        });

        db.collection("requested").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mViewRequestedBookProgress.setVisibility(View.GONE);
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    String data = documentSnapshot.getString("Title");
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText(data);
                    textView.setTextSize(20);
                    textView.setLayoutParams(params);
                    llViewRequestedBooks.addView(textView);
                }
            }
        });
        Window window = viewRequestedBookDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        viewRequestedBookDialog.show();
    }
}