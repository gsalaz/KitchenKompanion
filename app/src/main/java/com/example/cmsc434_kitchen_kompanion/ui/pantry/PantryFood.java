package com.example.cmsc434_kitchen_kompanion.ui.pantry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PantryFood {
    private String name, owner;
    private int quantity;
    private int location; // 0-pantry 1-fridge 2-freezer
    private int color_array_index;
    private Date expDate;
    //TODO: Make invincible if the food has date 0/0/00 so expDate doesnt appear
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yy");
    private boolean invincible;

    public PantryFood(String n, int q, int l, Date ed, String s, int i){
        name = n;
        quantity = q;
        location = l;
        expDate = ed;
        owner = s;
        color_array_index = i;
    }

    public void updateQuantity(int q){ quantity = q <= 0 ? quantity : q; }
    public void addToQuantity(int q) {quantity += q; }
    public void updateLocation(int l){ location = l; }
    public void updateName(String n){ name = n; }
    public void updateExpDate(Date d){ expDate = d;}
    public void updateColor(int c){ color_array_index = c;}

    public String getName(){ return name; }
    public int getQuantity() { return quantity; }
    public int getLocation() { return location; }
    public Date getExpDate() { return expDate; }
    public String getOwner() { return owner; }
    public int getColor() {return color_array_index; }
}
