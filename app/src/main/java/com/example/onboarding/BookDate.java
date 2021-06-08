package com.example.onboarding;

import java.util.Date;

public class BookDate {
    private String bookname;
    private String id;
    private Date issueDate;

    public BookDate() {
        //Empty constructor for Firebase
    }

    public BookDate(String bookname, Date issueDate) {
        this.id = String.valueOf(bookname.toLowerCase().hashCode());
        this.bookname = bookname;
        this.issueDate = issueDate;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Date issueDate) {
        this.issueDate = issueDate;
    }

    public long getFine(Date curr_date) {
        long diff = curr_date.getTime() - issueDate.getTime();
        long daysBetween = (diff / (1000 * 60 * 60 * 24));
        return (daysBetween + 1) * 10;
    }
}
