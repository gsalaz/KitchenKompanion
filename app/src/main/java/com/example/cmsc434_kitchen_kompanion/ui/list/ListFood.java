package com.example.cmsc434_kitchen_kompanion.ui.list;

public class ListFood {
    private String name;
    private int quantity;

    public ListFood(String n, int q){
        name = n;
        quantity = q;
    }

    public void updateQuantity(int q){ quantity = q; }
    public void addToQuantity(int i) {quantity += i;}
    public void updateName(String n){ name = n; }

    public String getName(){ return name; }
    public int getQuantity() {return quantity; }

}