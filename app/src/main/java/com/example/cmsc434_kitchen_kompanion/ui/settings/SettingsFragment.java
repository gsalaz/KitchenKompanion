package com.example.cmsc434_kitchen_kompanion.ui.settings;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cmsc434_kitchen_kompanion.R;
import com.example.cmsc434_kitchen_kompanion.databinding.FragmentSettingsBinding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class SettingsFragment extends Fragment{

    private FragmentSettingsBinding binding;
    private String userName = "";
    private int dropDownTypeText = -1;
    private int dropDownColorText = -1;
    private Button saveButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        saveButton = binding.getRoot().findViewById(R.id.settings_save);
        saveButton.setOnClickListener(view -> saveAction());

        loadUserData();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        String[] options1 = getResources().getStringArray(R.array.user_type_options);
        ArrayAdapter<String> aa1 = new ArrayAdapter<>(requireContext(), R.layout.dropdown_usertype_items, options1);
        binding.userTypeDropdown.setAdapter(aa1);

        String[] options2 = getResources().getStringArray(R.array.user_color_options);
        ArrayAdapter<String> aa2 = new ArrayAdapter<>(requireContext(), R.layout.dropdown_usertype_items, options2);
        binding.userColorDropdown.setAdapter(aa2);

        if(dropDownTypeText >= 0 && dropDownTypeText < options1.length)
            binding.userTypeDropdown.setText(options1[dropDownTypeText], false);

        if(dropDownColorText >= 0 && dropDownColorText < options2.length)
            binding.userColorDropdown.setText(options2[dropDownColorText], false);

        if(userName.length() != 0)
            binding.userName.setText(userName);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public void saveAction(){
        if(!checkInputData())
            return;

        userName = String.valueOf(binding.userName.getText()).trim();

        String[] options = getResources().getStringArray(R.array.user_type_options);
        String val = String.valueOf(binding.userTypeDropdown.getText());
        for(int i=0; i<options.length; i++){
            if(val.compareTo(options[i]) == 0) {
                dropDownTypeText = i;
                break;
            }
        }

        options = getResources().getStringArray(R.array.user_color_options);
        val = String.valueOf(binding.userColorDropdown.getText());
        for(int i=0; i<options.length; i++){
            if(val.compareTo(options[i]) == 0) {
                dropDownColorText = i;
                break;
            }
        }

        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/userData");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(userName);
            bw.write("\n");
            bw.write(Integer.toString(dropDownTypeText));
            bw.write("\n");
            bw.write(Integer.toString(dropDownColorText));
            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        saveButton.setText(R.string.saved);
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

    private boolean checkInputData(){
        boolean valid = true;

        if(!String.valueOf(binding.userName.getText()).trim().matches("^[\\w\\s]+$")){
            binding.userName.setError("Invalid input");
            valid = false;
        }

        if(String.valueOf(binding.userTypeDropdown.getText()).compareTo("Select") == 0){
            binding.userTypeDropdown.setError("Select one");
            valid = false;
        }

        if(String.valueOf(binding.userColorDropdown.getText()).compareTo("Select") == 0){
            binding.userColorDropdown.setError("Select one");
            valid = false;
        }

        return valid;
    }
}