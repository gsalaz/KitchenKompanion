package com.example.cmsc434_kitchen_kompanion.ui.calendar;

import java.util.Date;

public class NotificationFood {
    private String name;
    private int location;
    private int quantity;
    private Date expDate;

    public NotificationFood(String n, int q, int l, Date ed){
        expDate = ed;
        quantity = q;
        name = n;
        location = l;
    }

    public String getName(){ return name; }
    public Date getExpDate(){return expDate;}
    public int getQuantity(){return quantity;}
    public int getLocation() { return location; }

}
