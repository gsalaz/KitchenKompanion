package com.example.cmsc434_kitchen_kompanion.ui.recipes;

import android.util.Pair;

import com.example.cmsc434_kitchen_kompanion.ui.pantry.PantryFood;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Recipe {

    private final HashMap<String, Pair<Double, String>> ingredients;
    private String instructions;
    private String name;
    private boolean vegan, vegetarian, favorite;
    private int status, img_id;
    private ArrayList<PantryFood> pantry;

    private static final Pair<Double, String> empty = new Pair<>(0.0, "");

    public Recipe(Scanner s, int i, ArrayList<PantryFood> p){
        this();

        img_id = i;
        pantry = p;

        if(s.hasNextLine())
            updateName(s.nextLine());

        if(s.hasNextLine())
            s.nextLine();

        if(s.hasNextLine()){
            String v = s.nextLine();

            if(v.compareTo("None") == 0){
                updateVegan(false);
                updateVegetarian(false);
            }
            else if(v.compareTo("Vegetarian") == 0){
                updateVegan(false);
                updateVegetarian(true);
            }
            else if(v.compareTo("Vegan") == 0){
                updateVegan(true);
                updateVegetarian(true);
            }
        }

        if(s.hasNextLine())
            s.nextLine();

        if(s.hasNextLine()){
            String line = s.nextLine();

            while(line.compareTo("") != 0){
                String[] items = line.split("::");

                if(items[0].compareTo("None") == 0){
                    addIngredient(items[2]);
                }
                else{
                    double amt = Double.parseDouble(items[0]);

                    if(items[1].compareTo("None") == 0){
                        addIngredient(items[2], amt);
                    }
                    else{
                        addIngredient(items[2], amt, items[1]);
                    }
                }

                line = s.nextLine();
            }
        }

        if(s.hasNextLine())
            updateInstructions(s.nextLine());

        s.close();

        updateStatus();
    }

    public Recipe(){
        ingredients = new HashMap<>();
        instructions = "";
        name = "";
        vegan = false;
        vegetarian = false;
        favorite = false;
    }

    public void updateInstructions(String s){
        instructions = s;
    }

    public void addIngredient(String s, double amount, String unit){
        ingredients.put(s, new Pair(amount, unit));
    }

    public void addIngredient(String s, double amount){
        ingredients.put(s, new Pair(amount, ""));
    }

    public void addIngredient(String s){
        ingredients.put(s, empty);
    }

    public void updateName(String s){
        name = s;
    }

    public void updateVegan(boolean b){
        vegan = b;
    }

    public void updateVegetarian(boolean b){
        vegetarian = b;
    }

    public String getInstructions(){
        return instructions;
    }

    public String getName(){
        return name;
    }

    public String getIngredients(){
        StringBuilder retVal = new StringBuilder();

        for(String s: ingredients.keySet()){
            String unit = ingredients.get(s).second;
            double amt = ingredients.get(s).first;

            if(amt == 0.0)
                retVal.append(s);
            else if(unit.compareTo("") == 0){
                if(isWholeNumber(amt))
                    retVal.append((int) amt).append(" ").append(s);
                else
                    retVal.append(amt).append(" ").append(s);
            }
            else {
                if (isWholeNumber(amt))
                    retVal.append((int) amt).append(" ").append(unit).append(" ").append(s);
                else
                    retVal.append(amt).append(" ").append(unit).append(" ").append(s);
            }
            retVal.append("\n");
        }

        return retVal.substring(0, retVal.length()-1);
    }

    private boolean isWholeNumber(double d){
        return d % 1 == 0;
    }

    public boolean isVegan() {
        return vegan;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public int getStatus(){
        return status;
    }

    private void updateStatus(){
        //Check pantry for ingredients
        //0 - <50% ingredients
        //1 50%<99% ingredients
        //2 100% ingredients
        double count = 0.0;

        for(String s: ingredients.keySet()){
            PantryFood f = containsIngredient(s);
            if(f != null) {
                String unit = ingredients.get(s).second;
                double amt = ingredients.get(s).first;

                if(unit.compareTo("None") == 0)
                    count += (double)f.getQuantity() / amt;
                else if(f.getQuantity() >= 1)
                    count += 1.0;
            }
        }

        count /= ingredients.size();

        if(count < 0.5)
            status = 0;
        else if(count < 1.0)
            status = 1;
        else
            status = 2;
    }

    public void favorite(){
        favorite = !favorite;
    }

    public boolean isFavorite(){
        return favorite;
    }

    public int getImgId(){
        return img_id;
    }

    public HashMap<String, Pair<Double, String>> getIngredientList(){
        return ingredients;
    }

    private PantryFood containsIngredient(String ingredient_name){
        for (PantryFood f : pantry) {
            if(f.getName().toLowerCase().compareTo(ingredient_name.toLowerCase()) == 0){
                return f;
            }
        }
        return null;
    }

    public HashMap<String, Integer> missingItems(ArrayList<PantryFood> p){
        HashMap<String, Integer> items = new HashMap<>();
        pantry = p;

        for(String s: ingredients.keySet()){
            PantryFood f = containsIngredient(s);
            String unit = ingredients.get(s).second;
            double amt = ingredients.get(s).first;

            if(f != null) {
                int needed = 0;

                if(unit.compareTo("None") == 0)
                    needed = (int) (Math.round(amt + 0.99) - f.getQuantity());
                else if(f.getQuantity() < 1)
                    needed = 1;

                if(needed > 0)
                    items.put(s, needed);
            }
            else{
                if(unit.compareTo("None") == 0)
                    items.put(s, (int) (Math.round(amt + 0.99)));
                else
                    items.put(s, 1);
            }
        }

        return items;
    }

}
