package com.example.onboarding;

public class Book {
    //hashID,bookname,qty
    private String hashID;
    private String bookname;
    private int qty;

    public Book() {

    }

    public Book(String bookname, int qty) {
        this.hashID = String.valueOf(bookname.toLowerCase().hashCode());
        this.bookname = bookname;
        this.qty = qty;
    }

    public void addCount() {
        qty += 1;
    }

    public void decreaseCount() {
        qty -= 1;
    }


    public String getHashID() {
        return hashID;
    }

    public void setHashID(String hashID) {
        this.hashID = hashID;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }


}
