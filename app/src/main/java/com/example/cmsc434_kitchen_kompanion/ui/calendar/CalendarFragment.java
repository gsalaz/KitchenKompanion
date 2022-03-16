package com.example.cmsc434_kitchen_kompanion.ui.calendar;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cmsc434_kitchen_kompanion.R;
import com.example.cmsc434_kitchen_kompanion.databinding.FragmentCalendarBinding;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Scanner;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private ArrayList<NotificationFood> food;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private NotificationAdapter adapter;



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        food = new ArrayList<>();
        readFoods();
        readNotifications();
        sortByDate();

        ListView listView = root.findViewById(R.id.notification_list);

        adapter = new NotificationAdapter(root.getContext(), 0, food);
        listView.setAdapter(adapter);


        final TextView textView = binding.textCalendar;
        CalendarView calendarView;
        calendarView = root.findViewById(R.id.calendarView3);
        calendarView.setFirstDayOfWeek(1);
        ImageView trashButton = listView.findViewById(R.id.trashButton_pantry);

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> popup(view, (month+1) +"/"+dayOfMonth+"/"+year, year, month, dayOfMonth));

        return root;
    }

    private void readFoods() {
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

                    food.add(new NotificationFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            food = new ArrayList<>();
        }
    }
    private void readNotifications() {
        Context ctx = getContext();
        Scanner s;
        String line;
        try {
            assert ctx != null;
            s = new Scanner(new File(ctx.getFilesDir() + "/notificationData"));

            while(s.hasNextLine()){
                line = s.nextLine();

                if(line.compareTo("") != 0) {
                    String[] item = line.split("::");
                    Date d;

                    if(item[3].compareTo("None") == 0)
                        d = dateFormat.parse("01/01/2000");
                    else
                        d = dateFormat.parse(item[3]);

                    food.add(new NotificationFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }

    private void writeNotifications(){
        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/notificationData");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(NotificationFood f: food) {
                String d;
                if (f.getQuantity() == -1) {
                    if (f.getExpDate().compareTo(dateFormat.parse("01/01/2000")) == 0)
                        d = "None";
                    else
                        d = dateFormat.format(f.getExpDate());

                    bw.write(f.getName() + "::" + f.getQuantity() + "::" +
                            f.getLocation() + "::" + d + "::" + "\n");
                }
            }
            bw.close();
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    private void sortByDate(){
        Collections.sort(food, (food1, food2) -> food1.getExpDate().compareTo(food2.getExpDate()));    }


    private void popup(View view, String name, int year, int month, int day) {
        Context ctx = getContext();

        assert ctx != null;
        LayoutInflater inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.calendar_popup, null);

        Date selectedDate = new Date();
        try {
            selectedDate = new SimpleDateFormat("MM/dd/yyyy").parse(name);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        TextView title = popupView.findViewById(R.id.popup_date);
        title.setText(name);
        EditText stores = popupView.findViewById(R.id.popup_stores);
        EditText time = popupView.findViewById(R.id.popup_time);

        Button cancel = popupView.findViewById(R.id.cancel_button);
        Button ok = popupView.findViewById(R.id.ok_button);

        final PopupWindow popupWindow = new PopupWindow(popupView, 1000, 1100, true);
        Date finalSelectedDate = selectedDate;
        ok.setOnClickListener(v ->
                addEvent(new NotificationFood(stores.getText().toString(), -1, 1, finalSelectedDate), popupWindow, stores)
        );
        cancel.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }
    private void addEvent(NotificationFood e, PopupWindow popupWindow, EditText stores){
        if(!stores.getText().toString().trim().matches("^[\\w\\s]+$")){
            stores.setError("Invalid Input");
        }else {
            try {
                food.add(e);
            } catch (Exception error) {

            }
            sortByDate();
            writeNotifications();
            adapter.notifyDataSetChanged();
            popupWindow.dismiss();
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}