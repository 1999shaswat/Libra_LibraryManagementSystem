package com.example.onboarding;


import java.util.ArrayList;
import java.util.Date;

public class User {
    private String uid;
    private String name;
    private String email;
    private int fine;
    private ArrayList<BookDate> booksIssued;

    public User() {
        //Firebase
    }

    public User(String uid, String name, String email) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.booksIssued = new ArrayList<BookDate>();
    }

    public void setFine(int fine) {
        this.fine = calculateFine();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ArrayList<BookDate> getBooksIssued() {
        return booksIssued;
    }

    public void setBooksIssued(ArrayList<BookDate> booksIssued) {
        this.booksIssued = booksIssued;
    }

    public void issueBook(String bookName) {
        this.booksIssued.add(new BookDate(bookName, new Date()));
    }

    public void returnBook(String bookName) {
        String b_id = String.valueOf(bookName.toLowerCase().hashCode());
        BookDate del;
        for (BookDate b : this.booksIssued) {
            if (b.getId().equals(b_id)) {
                this.booksIssued.remove(b);
                break;
            }
        }
    }

    public int calculateFine() {
        Date currDate = new Date();
        int fine = 0;
        for (BookDate b : this.booksIssued) {
            fine += b.getFine(currDate);
        }
        return fine;
    }


}
