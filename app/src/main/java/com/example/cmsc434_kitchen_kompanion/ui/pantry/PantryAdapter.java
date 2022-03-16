package com.example.cmsc434_kitchen_kompanion.ui.pantry;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmsc434_kitchen_kompanion.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class PantryAdapter extends ArrayAdapter<PantryFood> {
    private ArrayList<PantryFood> pantry, altList;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private ViewGroup par;
    private String userName;
    private int dropDownColorText, dropDownTypeText;

    public PantryAdapter(Context c, int resource, ArrayList<PantryFood> foodList){
        super(c, resource, foodList);
        pantry = foodList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        par = parent;
        PantryFood item = getItem(position);
        loadUserData();
        readPantryFoods();

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.pantry_item, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.item_text_pantry);
        TextView quantity = convertView.findViewById(R.id.item_quantity_pantry);
        ImageView trashButton = convertView.findViewById(R.id.trashButton_pantry);
        ImageView owner = convertView.findViewById(R.id.owner);

        trashButton.setOnClickListener(v -> delete(pantry.get(position)));

        if(tv.getText().toString().length() < 17)
            tv.setText(item.getName());
        else
            tv.setText(item.getName().substring(0, 14) + "...");

        quantity.setText(Integer.toString(item.getQuantity()));

        int color;

        if(dropDownTypeText != 0)
            owner.setVisibility(View.INVISIBLE);
        else {
            switch (item.getColor()){
                case 0:
                    color = R.drawable.ic_baseline_person_24_blue;
                    break;
                case 1:
                    color = R.drawable.ic_baseline_person_24_green;
                    break;
                case 2:
                    color = R.drawable.ic_baseline_person_24_pink;
                    break;
                case 3:
                    color = R.drawable.ic_baseline_person_24_purple;
                    break;
                case 4:
                    color = R.drawable.ic_baseline_person_24_red;
                    break;
                case 5:
                    color = R.drawable.ic_baseline_person_24_yellow;
                    break;
                default:
                    color = R.drawable.ic_baseline_person_24;
                    break;
            }
            owner.setImageResource(color);
        }

        return convertView;
    }

    private void delete(PantryFood item){
        pantry.remove(item);
        for(PantryFood f: altList){
            if(item.getName().compareTo(f.getName()) == 0){
                altList.remove(f);
                break;
            }
        }
        writePantryFoods();
        par.invalidate();
        this.notifyDataSetChanged();
    }

    private void writePantryFoods(){
        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/pantryFoodData");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(PantryFood f: altList){
                String d;

                if(f.getExpDate().compareTo(dateFormat.parse("01/01/2000")) == 0)
                    d = "None";
                else
                    d = dateFormat.format(f.getExpDate());

                if(f.getOwner().compareTo(userName) == 0)
                    f.updateColor(dropDownColorText);

                bw.write(f.getName() + "::" + f.getQuantity() + "::" +
                        f.getLocation() + "::" + d + "::" + f.getOwner() + "::" + f.getColor() + "\n");
            }
            bw.close();
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void loadUserData() {
        Context ctx = getContext();
        Scanner scanner;
        try {
            assert ctx != null;
            scanner = new Scanner(new File(ctx.getFilesDir() + "/userData"));

            if(scanner.hasNextLine())
                userName = scanner.nextLine();
            if(scanner.hasNextLine())
                dropDownTypeText = Integer.parseInt(scanner.nextLine());
            if(scanner.hasNextLine())
                dropDownColorText = Integer.parseInt(scanner.nextLine());

            scanner.close();
        } catch (FileNotFoundException e) {
            userName = "";
            dropDownTypeText = -1;
            dropDownColorText = -1;
        }
    }

    private void readPantryFoods() {
        altList = new ArrayList<>();

        Context ctx = getContext();
        Scanner s;
        String line;
        try {
            assert ctx != null;
            s = new Scanner(new File(ctx.getFilesDir() + "/pantryFoodData"));

            while(s.hasNextLine()){
                line = s.nextLine();

                if(line.compareTo("") != 0) {
                    String[] item = line.split("::");
                    Date d;

                    if(item[3].compareTo("None") == 0)
                        d = dateFormat.parse("01/01/2000");
                    else
                        d = dateFormat.parse(item[3]);

                    altList.add(new PantryFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d, item[4], Integer.parseInt(item[5])));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            altList = new ArrayList<>();
        }
    }
}

