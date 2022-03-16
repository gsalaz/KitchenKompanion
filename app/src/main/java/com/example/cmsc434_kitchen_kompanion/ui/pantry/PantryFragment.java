package com.example.cmsc434_kitchen_kompanion.ui.pantry;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cmsc434_kitchen_kompanion.R;
import com.example.cmsc434_kitchen_kompanion.databinding.FragmentPantryBinding;

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

public class PantryFragment extends Fragment {

    private FragmentPantryBinding binding;
    private ArrayList<PantryFood> food;
    private ListView listView;
    private Button pantryButton, fridgeButton, freezerButton;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private String userName;
    private int dropDownColorText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentPantryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        loadUserData();

        pantryButton = binding.getRoot().findViewById(R.id.pantry_button);
        pantryButton.setOnClickListener(view -> filterList(0));

        fridgeButton = binding.getRoot().findViewById(R.id.fridge_button);
        fridgeButton.setOnClickListener(view -> filterList(1));

        freezerButton = binding.getRoot().findViewById(R.id.freezer_button);
        freezerButton.setOnClickListener(view -> filterList(2));

        Button addItemButton = root.findViewById(R.id.addButton_pantry);

        food = new ArrayList<>();
        readFoods();
        writeFoods();


        listView = root.findViewById(R.id.pantry_list);
        filterList(0);
        listView.setOnItemClickListener((adapterView, view, i, l) -> popup(view, (PantryFood) listView.getItemAtPosition(i), i));
        addItemButton.setOnClickListener(view -> {
            try {
                addItem(view);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

        return root;
    }

    private void filterList(int i) {
        ArrayList<PantryFood> filteredItems = new ArrayList<>();
        readFoods();

        for(PantryFood f: food){
            if(f.getLocation() == i)
                filteredItems.add(f);
        }

        PantryAdapter adapter = new PantryAdapter(getContext(), 0, filteredItems);
        listView.setAdapter(adapter);

        pantryButton.setTextColor(getResources().getColor(R.color.orange_500));
        pantryButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        fridgeButton.setTextColor(getResources().getColor(R.color.orange_500));
        fridgeButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        freezerButton.setTextColor(getResources().getColor(R.color.orange_500));
        freezerButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        if(i == 0){
            pantryButton.setTextColor(getResources().getColor(R.color.white));
            pantryButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
        else if(i == 1){
            fridgeButton.setTextColor(getResources().getColor(R.color.white));
            fridgeButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
        else if(i ==2){
            freezerButton.setTextColor(getResources().getColor(R.color.white));
            freezerButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
    }

    private void addItem(View v) throws ParseException {
        popup(v, null, -1);
    }

    private void popup(View view, PantryFood f, int i){
        Context ctx = getContext();

        assert ctx != null;
        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.pantry_popup, null);

        String[] options = getResources().getStringArray(R.array.location_options);
        ArrayAdapter<String> aa = new ArrayAdapter<>(requireContext(), R.layout.dropdown_usertype_items, options);
        AutoCompleteTextView drop = popupView.findViewById(R.id.foodLocationDropdown);
        drop.setAdapter(aa);


        EditText name = popupView.findViewById(R.id.popup_item_name_pantry);

        TextView minus = popupView.findViewById(R.id.minus);
        TextView plus = popupView.findViewById(R.id.plus);
        EditText quantity = popupView.findViewById(R.id.popup_quantity_pantry);

        EditText expDate = popupView.findViewById(R.id.popup_expiration_date_pantry);

        if(f != null) {
            drop.setText(options[f.getLocation()], false);
            name.setText(f.getName());
            quantity.setText(Integer.toString(f.getQuantity()));
            expDate.setText(dateFormat.format(f.getExpDate()));
        }

        Button saveButton = popupView.findViewById(R.id.save_button_pantry);
        Button cancelButton = popupView.findViewById(R.id.cancel_button_pantry);

        final PopupWindow popupWindow = new PopupWindow(popupView, 1000, 1100, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        minus.setOnClickListener(v -> {
            int q = Integer.parseInt(quantity.getText().toString());
            if(q > 1)
                quantity.setText(Integer.toString(q-1));
        });
        plus.setOnClickListener(v ->
                quantity.setText(Integer.toString(Integer.parseInt(quantity.getText().toString()) + 1)));

        saveButton.setOnClickListener((v) -> save(name, quantity, drop, expDate, popupWindow, i));

        cancelButton.setOnClickListener(v -> popupWindow.dismiss());
        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

    @Override
    public void onDestroyView() {
        writeFoods();
        super.onDestroyView();
        binding = null;
    }

    //add foods from foodlist raw file
    private void readFoods() {
        food = new ArrayList<>();
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

                    food.add(new PantryFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d, item[4], Integer.parseInt(item[5])));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            food = new ArrayList<>();
        }
    }

    private void writeFoods(){
        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/pantryFoodData");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(PantryFood f: food){
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


    //HELPERS
    private void addWOutDup(String n, int q, int l, Date d){
        for (int i = 0; i < food.size(); i++) {
            if(food.get(i).getName().trim().compareToIgnoreCase(n.trim()) == 0) {
                food.get(i).addToQuantity(q);
                return;
            }
        }

        food.add(new PantryFood(n, q, l, d, userName, dropDownColorText));
    }
    private void updateFood(String n, int q, int l, Date d, int i){
        food.get(i).updateName(n);
        food.get(i).updateQuantity(q);
        food.get(i).updateLocation(l);
        food.get(i).updateExpDate(d);
    }

    private void save(EditText name, EditText quantity, AutoCompleteTextView drop, EditText expDate, PopupWindow popupWindow, int i){
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
            String[] options = getResources().getStringArray(R.array.location_options);
            String val = String.valueOf(drop.getText());
            for(int j=0; j<options.length; j++) {
                if (val.compareTo(options[j]) == 0) {
                    itemLocation = j;
                    break;
                }
            }

            if (i < 0)
                addWOutDup(itemName, itemQuantity, itemLocation, itemDate);
            else
                updateFood(itemName, itemQuantity, itemLocation, itemDate, i);

            writeFoods();
            filterList(itemLocation);
            listView.invalidateViews();
            popupWindow.dismiss();
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