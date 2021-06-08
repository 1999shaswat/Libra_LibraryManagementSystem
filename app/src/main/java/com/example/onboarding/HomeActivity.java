package com.example.onboarding;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import static android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    protected DrawerLayout drawerLayout;
    TextView userBooksIssued, userFineAmt;
    private FirebaseAuth fauth;
    private FirebaseFirestore fStore;
    private TextView mName, mEmail, mRequest;
    private String userID;
    private ProgressBar homeLoading, mIssueBookProgress, mReturnBookProgress, mViewIssuedProgress, mRequestBookProgress;
    private Dialog issueBook, returnBook, viewIssued, requestBook;
    private ImageView closeIssueBookPopup, closeReturnBookPopup, closeViewIssuedPopup, closeRequestBookPopup;
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DocumentReference documentReference;
    private CollectionReference library;
    private User current_user;
    private EditText requestBookInput;
    private EditText mSearchGoogleBookInput;
    private TextView mSearchGoogleTitleText;
    private TextView mSearchGoogleAuthorText;
    private TextView mSearchGoogleDescText, mAboutUsDesc;
    private ProgressBar googleBooksLoading;
    private TextView calcRes;
    private EditText calcBookQty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        fauth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        library = fStore.collection("library");

        issueBook = new Dialog(this);
        returnBook = new Dialog(this);
        viewIssued = new Dialog(this);
        requestBook = new Dialog(this);

        toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false); //hides app name in actionbar


        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.openNavDrawer, R.string.closeNavDrawer);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        //Set first item selected
        navigationView.setCheckedItem(R.id.dash);
        navigationView.getMenu().performIdentifierAction(R.id.dash, 0);


    }

    @Override
    protected void onStart() {
        super.onStart();
        //Change book issued and fine amount here
    }

    public void issueBooks(View view) {
        issueBook.setContentView(R.layout.issue_book_dialog);
        mIssueBookProgress = issueBook.findViewById(R.id.issuebook_progress);
        mIssueBookProgress.setVisibility(View.VISIBLE);
        closeIssueBookPopup = issueBook.findViewById(R.id.close_issue_books_popup);
        LinearLayout llViewBooks = issueBook.findViewById(R.id.llissuebooks);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        closeIssueBookPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                issueBook.dismiss();
            }
        });

        fStore.collection("library").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                mIssueBookProgress.setVisibility(View.GONE);
                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                    Book book = documentSnapshot.toObject(Book.class);
                    String data = book.getBookname();
                    String id = book.getHashID();
                    TextView textView = new TextView(getApplicationContext());
                    textView.setText(data + "\n");
//                    textView.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
                    textView.setTextSize(20);
                    textView.setLayoutParams(params);
                    textView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //Issue Books here
//                            Toast.makeText(HomeActivity.this, data, Toast.LENGTH_SHORT).show();
                            //Reduce book count in library
                            //update user object
                            mIssueBookProgress.setVisibility(View.VISIBLE);
                            Boolean bookAddedToUser, removedFromLibrary;

                            current_user.issueBook(data);
                            documentReference.set(current_user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    book.decreaseCount();
                                    Log.d("Tag", "HomeActivity book issue id:" + book.getHashID());
                                    library.document(book.getHashID()).set(book).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Tag", "Book count updated");
                                            Toast.makeText(getApplicationContext(), "Book Issued", Toast.LENGTH_SHORT).show();
                                            mIssueBookProgress.setVisibility(View.GONE);
                                            updateInfo();
                                            issueBook.dismiss();
                                        }
                                    });
                                }
                            });
                        }
                    });
                    llViewBooks.addView(textView);
                }
            }
        });
        Window window = issueBook.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        issueBook.show();
    }

    public void returnBooks(View view) {
        //TODO: Make changes
        returnBook.setContentView(R.layout.return_book_dialog);
        mReturnBookProgress = returnBook.findViewById(R.id.returnbook_progress);
        mReturnBookProgress.setVisibility(View.VISIBLE);
        closeReturnBookPopup = returnBook.findViewById(R.id.close_return_books_popup);
        LinearLayout llViewBooks = returnBook.findViewById(R.id.llreturnbooks);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        closeReturnBookPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                returnBook.dismiss();
            }
        });

        mReturnBookProgress.setVisibility(View.GONE);
        for (BookDate bookDate : current_user.getBooksIssued()) {
            String data = bookDate.getBookname();
            String id = bookDate.getId();
            TextView textView = new TextView(getApplicationContext());
            textView.setText(data + "\n");
//            textView.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
            textView.setTextSize(20);
            textView.setLayoutParams(params);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mReturnBookProgress.setVisibility(View.VISIBLE);
                    current_user.returnBook(data);
                    documentReference.set(current_user).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            library.document(id).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    if (documentSnapshot.exists()) {
                                        Book book = documentSnapshot.toObject(Book.class);
                                        book.addCount();
                                        library.document(id).set(book).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getApplicationContext(), "Book Returned", Toast.LENGTH_SHORT).show();
                                                mReturnBookProgress.setVisibility(View.GONE);
                                                updateInfo();
                                                returnBook.dismiss();
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                }
            });
            llViewBooks.addView(textView);
        }

        Window window = returnBook.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        returnBook.show();
    }

    public void viewIssuedBooks(View view) {
        viewIssued.setContentView(R.layout.view_issued_dialog);
        mViewIssuedProgress = viewIssued.findViewById(R.id.viewbook_progress);
        mViewIssuedProgress.setVisibility(View.VISIBLE);
        closeViewIssuedPopup = viewIssued.findViewById(R.id.close_viewissued_books_popup);
        LinearLayout llViewBooks = viewIssued.findViewById(R.id.llviewissued_books);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        closeViewIssuedPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewIssued.dismiss();
            }
        });

        mViewIssuedProgress.setVisibility(View.GONE);
        for (BookDate bookDate : current_user.getBooksIssued()) {
            String data = bookDate.getBookname();
            TextView textView = new TextView(getApplicationContext());
            textView.setText(data + "\n");
//            textView.setTextColor(getResources().getColor(R.color.colorPrimary, getTheme()));
            textView.setTextSize(20);
            textView.setLayoutParams(params);
            llViewBooks.addView(textView);
        }

        Window window = viewIssued.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        viewIssued.show();
    }

    public void requestBook(View view) {
        requestBook.setContentView(R.layout.request_book_user);
        closeRequestBookPopup = requestBook.findViewById(R.id.close_requestbook_popup);
        mRequest = requestBook.findViewById(R.id.request_confirm);
        requestBookInput = requestBook.findViewById(R.id.request_bookname);
        mRequestBookProgress = requestBook.findViewById(R.id.request_progress);
        mRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRequestBookProgress.setVisibility(View.VISIBLE);
                String bookname, id;
                bookname = requestBookInput.getText().toString().trim();

                if (bookname.length() > 0) {
                    id = String.valueOf(bookname.hashCode());
                    Map<String, Object> book = new HashMap<>();
                    book.put("Title", bookname);
                    book.put("id", id);

                    fStore.collection("requested").document(id).set(book).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            requestBook.dismiss();
                            Toast.makeText(getApplicationContext(), "Request added", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Tag", "onFailure book add:" + e.toString());
                            Toast.makeText(getApplicationContext(), "Error adding books" + e.toString(), Toast.LENGTH_SHORT).show();
                            mRequestBookProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                } else {
                    requestBookInput.setError("Book name is required");
                    mRequestBookProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
        closeRequestBookPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestBook.dismiss();
            }
        });
        Window window = requestBook.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        requestBook.show();
    }

    public void initializeHome() {

//        View home=LayoutInflater.from(this).inflate()


        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = (LinearLayout) findViewById(R.id.content_frame);
        View home = inflater.inflate(R.layout.dashscreen, container, false);
        container.removeAllViewsInLayout();
        container.addView(home);

        homeLoading = findViewById(R.id.home_loading);
        homeLoading.setVisibility(View.VISIBLE);

        mName = findViewById(R.id.name);
        mEmail = findViewById(R.id.email);

        userFineAmt = findViewById(R.id.user_home_fine);
        userBooksIssued = findViewById(R.id.user_books_issued_count);


        //Fetch data from firebase

        userID = fauth.getCurrentUser().getUid();
        documentReference = fStore.collection("users").document(userID);
        documentReference.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (value != null) {
                    current_user = value.toObject(User.class);
                    homeLoading.setVisibility(View.INVISIBLE);
                    mName.setText(current_user.getName());
                    mEmail.setText(current_user.getEmail());
                    updateInfo();
                }
            }
        });
    }

    public void updateInfo() {
        userFineAmt.setText(String.valueOf(current_user.calculateFine()));
        userBooksIssued.setText(String.valueOf(current_user.getBooksIssued().size()));
    }

    public void searchBooks(View view) {
        String queryString = mSearchGoogleBookInput.getText().toString();


        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);

        if (inputManager != null) {
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connMgr != null) {
            networkInfo = connMgr.getActiveNetworkInfo();
        }

        if (networkInfo != null && networkInfo.isConnected()
                && queryString.length() != 0) {
            new FetchBooks(mSearchGoogleTitleText, mSearchGoogleAuthorText, googleBooksLoading, mSearchGoogleDescText).execute(queryString);
            googleBooksLoading.setVisibility(View.VISIBLE);

        } else {
            if (queryString.length() == 0) {
                mSearchGoogleAuthorText.setText("");
                mSearchGoogleTitleText.setText(R.string.no_search_term);
            } else {
                mSearchGoogleAuthorText.setText("");
                mSearchGoogleTitleText.setText(R.string.no_network);
            }
        }
    }

    public void initializeSearchBooks() {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = (LinearLayout) findViewById(R.id.content_frame);
        View home = inflater.inflate(R.layout.activity_search_books_google, container, false);
        container.removeAllViewsInLayout();
        container.addView(home);

        mSearchGoogleBookInput = (EditText) findViewById(R.id.bookInput);
        mSearchGoogleTitleText = (TextView) findViewById(R.id.titleText);
        mSearchGoogleAuthorText = (TextView) findViewById(R.id.authorText);
        mSearchGoogleDescText = findViewById(R.id.descText);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mSearchGoogleDescText.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
        googleBooksLoading = findViewById(R.id.searchGoogleBooksProgress);
        googleBooksLoading.setVisibility(View.INVISIBLE);
    }

    public void initializeAboutUs() {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = (LinearLayout) findViewById(R.id.content_frame);
        View home = inflater.inflate(R.layout.about_us, container, false);
        container.removeAllViewsInLayout();
        container.addView(home);


        mAboutUsDesc = findViewById(R.id.about_us_desc);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAboutUsDesc.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }

    }

    public void initializeCalcMembershipPrice() {
        LayoutInflater inflater = getLayoutInflater();
        LinearLayout container = (LinearLayout) findViewById(R.id.content_frame);
        View home = inflater.inflate(R.layout.calculate_membership_price_page, container, false);
        container.removeAllViewsInLayout();
        container.addView(home);


        calcBookQty = findViewById(R.id.calc_bookqty);
        calcRes = findViewById(R.id.calc_res);

    }

    public void calcMembershipPrice(View view) {
        String s = calcBookQty.getText().toString().trim();
        float res = 0;
        if (s.length() > 0) {
            int i = Integer.parseInt(s);
            res = i * 10;
            if (i >= 180) {
                res -= res * 0.25;
            } else if (i >= 120) {
                res -= res * 0.20;
            } else if (i >= 60) {
                res -= res * 0.15;
            }
            calcRes.setText("Membership Price: Rs " + String.valueOf(res) + "(including taxes)");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.dash:
                item.setChecked(true);

                initializeHome();

                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.search:
                item.setChecked(true);
                initializeSearchBooks();
                drawerLayout.closeDrawer(Gravity.LEFT);

                break;
            case R.id.calculate:
                item.setChecked(true);
                initializeCalcMembershipPrice();
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
            case R.id.about:
                item.setChecked(true);
                initializeAboutUs();
                drawerLayout.closeDrawer(Gravity.LEFT);
                break;
        }
        return true;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}