package com.example.cmsc434_kitchen_kompanion.ui.list;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.cmsc434_kitchen_kompanion.R;
import com.example.cmsc434_kitchen_kompanion.ui.pantry.PantryFood;

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

public class ListAdapter extends ArrayAdapter<ListFood> {
    private ArrayList<ListFood> list;
    private ArrayList<PantryFood> pantry;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private ViewGroup par;
    private String userName;
    private int dropDownColorText;

    public ListAdapter(Context c, int resource, ArrayList<ListFood> foodList){
        super(c, resource, foodList);
        list = foodList;
        pantry = new ArrayList<>();
        readPantryFoods();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        par = parent;
        ListFood item = getItem(position);
        loadUserData();

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.item_text_list);
        TextView quantity = convertView.findViewById(R.id.item_quantity_list);
        ImageView intoButton = convertView.findViewById(R.id.intoButton_list);
        ImageView trashButton = convertView.findViewById(R.id.trashButton_list);

        intoButton.setOnClickListener(view -> popup(view, item));
        trashButton.setOnClickListener(view -> delete(item));

        if(item.getName().length() < 17)
            tv.setText(item.getName());
        else
            tv.setText(item.getName().substring(0, 14) + "...");

        quantity.setText(Integer.toString(item.getQuantity()));

        return convertView;
    }

    private void delete(ListFood item){
        list.remove(item);
        writeListFoods();
        par.invalidate();
        this.notifyDataSetChanged();
    }

    private void popup(View view, ListFood f){
        Context ctx = getContext();

        assert ctx != null;
        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.move_popup, null);

        String[] options = ctx.getResources().getStringArray(R.array.location_options);
        ArrayAdapter<String> aa = new ArrayAdapter<>(ctx, R.layout.dropdown_usertype_items, options);
        AutoCompleteTextView drop = popupView.findViewById(R.id.foodLocationDropdown_move);
        drop.setAdapter(aa);


        EditText name = popupView.findViewById(R.id.popup_item_name_move);

        EditText quantity = popupView.findViewById(R.id.popup_quantity_move);

        EditText expDate = popupView.findViewById(R.id.popup_expiration_date_move);

        if(f != null) {
            name.setText(f.getName());
            quantity.setText(Integer.toString(f.getQuantity()));
        }

        Button saveButton = popupView.findViewById(R.id.move_button_move);
        Button cancelButton = popupView.findViewById(R.id.cancel_button_move);

        final PopupWindow popupWindow = new PopupWindow(popupView, 1000, 1100, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        saveButton.setOnClickListener((v) -> moveToPantry(f, name, quantity, drop, expDate, popupWindow));

        cancelButton.setOnClickListener(v -> popupWindow.dismiss());
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });



    }

    private void moveToPantry(ListFood item, EditText name, EditText quantity, AutoCompleteTextView drop, EditText expDate, PopupWindow popupWindow) {
        Date itemDate = null;
        boolean valid = true;

        try {
            itemDate = dateFormat.parse(expDate.getText().toString());
        } catch (ParseException e) {
            valid = false;
            expDate.setError("Invalid input");
        }
        if(!name.getText().toString().trim().matches("^[\\w\\s]+$")){
            name.setError("Invalid input");
            valid = false;
        }
        if(quantity.getText().toString().compareTo("") == 0 || Integer.parseInt(quantity.getText().toString()) <= 0){
            quantity.setError("Invalid input");
            valid = false;
        }
        if(String.valueOf(drop.getText()).compareTo("Select") == 0){
            drop.setError("Select one");
            valid = false;
        }

        if(valid) {
            String itemName = name.getText().toString();
            int itemQuantity = Integer.parseInt(quantity.getText().toString());
            int itemLocation = -1;
            String[] options = getContext().getResources().getStringArray(R.array.location_options);
            String val = String.valueOf(drop.getText());
            for(int j=0; j<options.length; j++) {
                if (val.compareTo(options[j]) == 0) {
                    itemLocation = j;
                    break;
                }
            }

            list.remove(item);
            pantry.add(new PantryFood(itemName, itemQuantity, itemLocation, itemDate, userName, dropDownColorText));
            writeListFoods();
            writePantryFoods();
            par.invalidate();
            this.notifyDataSetChanged();
            popupWindow.dismiss();
        }
    }

    private void readPantryFoods() {
        pantry = new ArrayList<>();

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

                    pantry.add(new PantryFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d, item[4], Integer.parseInt(item[5])));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            pantry = new ArrayList<>();
        }
    }

    private void writeListFoods(){
        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/listFoodData");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(int i = 0; i < list.size(); i++){
                bw.write(list.get(i).getName() + "::" + list.get(i).getQuantity() + "\n");
            }

            bw.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
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

            for(PantryFood f: pantry){
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
        int dropDownTypeText;
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
}

