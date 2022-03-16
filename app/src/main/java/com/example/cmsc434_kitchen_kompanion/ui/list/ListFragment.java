package com.example.cmsc434_kitchen_kompanion.ui.list;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cmsc434_kitchen_kompanion.R;
import com.example.cmsc434_kitchen_kompanion.databinding.FragmentListBinding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ListFragment extends Fragment {

    private FragmentListBinding binding;
    private ArrayList<ListFood> food;
    private ListView listView;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentListBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        food = new ArrayList<>();
        readFoods();

        Button myListButton = binding.getRoot().findViewById(R.id.list1_button);
        myListButton.setTextColor(getResources().getColor(R.color.white));
        myListButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));

        Button person1Button = binding.getRoot().findViewById(R.id.list2_button);
        person1Button.setOnClickListener(view ->
                Toast.makeText(getActivity(), "Work in Progress", Toast.LENGTH_LONG).show());

        Button person2Button = binding.getRoot().findViewById(R.id.list3_button);
        person2Button.setOnClickListener(view ->
                Toast.makeText(getActivity(), "Work in Progress", Toast.LENGTH_LONG).show());

        listView = root.findViewById(R.id.shopping_list);
        Button addItemButton = root.findViewById(R.id.addButton_list);

        ListAdapter adapter = new ListAdapter(root.getContext(), 0, food);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((adapterView, view, i, l) -> popup(view, food.get(i), i));

        addItemButton.setOnClickListener(this::addFood);

        return root;
    }

    private void addFood(View v){
        popup(v, null, -1);
    }

    private void readFoods() {
        food = new ArrayList<>();
        Context ctx = getContext();
        Scanner s;
        String line;
        try {
            assert ctx != null;
            s = new Scanner(new File(getContext().getFilesDir() + "/listFoodData"));

            while(s.hasNextLine()){
                line = s.nextLine();

                if(line.compareTo("") != 0) {
                    String[] item = line.split("::");
                    food.add(new ListFood(item[0], Integer.parseInt(item[1])));
                }
            }

            s.close();
        } catch (FileNotFoundException e) {
            food = new ArrayList<>();
        }
    }
    private void writeFoods(){
        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/listFoodData");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(int i = 0; i < food.size(); i++){
                bw.write(food.get(i).getName() + "::" + food.get(i).getQuantity() + "\n");
            }

            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void popup(View view, ListFood f, int i){
        Context ctx = getContext();

        assert ctx != null;
        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.list_popup, null);

        TextView name = popupView.findViewById(R.id.popup_item_name_list);

        TextView minus = popupView.findViewById(R.id.minus);
        TextView plus = popupView.findViewById(R.id.plus);
        EditText quantity = popupView.findViewById(R.id.popup_quantity_list);

        if(f != null){
            name.setText(f.getName());
            quantity.setText(Integer.toString(f.getQuantity()));
        }

        Button saveButton = popupView.findViewById(R.id.save_button_list);
        Button cancelButton = popupView.findViewById(R.id.cancel_button_list);

        final PopupWindow popupWindow = new PopupWindow(popupView, 1000, 800, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        minus.setOnClickListener(v -> {
            int q = Integer.parseInt(quantity.getText().toString());
            if(q > 1)
                quantity.setText(Integer.toString(q-1));
        });
        plus.setOnClickListener(v ->
                quantity.setText(Integer.toString(Integer.parseInt(quantity.getText().toString()) + 1)));

        saveButton.setOnClickListener((v) -> save(name, quantity, i, popupWindow));

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

    //Add item with out duplicating the item. If is dup then add the quantity.
    private void addWOutDup(String n, int q){
        for (int i = 0; i < food.size(); i++) {
            if(food.get(i).getName().trim().compareToIgnoreCase(n.trim()) == 0) {
                food.get(i).addToQuantity(q);
                return;
            }
        }
        food.add(new ListFood(n, q));
    }
    private void updateFood(String n, int q, int i){

        food.get(i).updateName(n);
        food.get(i).updateQuantity(q);
    }

    private void save(TextView name, EditText quantity, int i, PopupWindow popupWindow){
        boolean valid = true;

        if(!name.getText().toString().trim().matches("^[\\w\\s]+$")){
            name.setError("Invalid input");
            valid = false;
        }
        if(quantity.getText().toString().compareTo("") == 0 || Integer.parseInt(quantity.getText().toString()) <= 0){
            quantity.setError("Invalid input");
            valid = false;
        }

        if(valid) {
            String itemName = name.getText().toString();
            int itemQuantity = Integer.parseInt(quantity.getText().toString());

            if (i < 0)
                addWOutDup(itemName, itemQuantity);
            else {
                updateFood(itemName, itemQuantity, i);
            }

            listView.invalidateViews();
            popupWindow.dismiss();
        }
    }

}