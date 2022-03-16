package com.example.cmsc434_kitchen_kompanion.ui.calendar;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class NotificationAdapter extends ArrayAdapter<NotificationFood>{
    private ArrayList<NotificationFood> list, tempList;
    private ViewGroup par;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");


    public NotificationAdapter(Context c, int resource, ArrayList<NotificationFood> foodList){
        super(c, resource, foodList);
        list = foodList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NotificationFood item = getItem(position);
        par = parent;
        readFoods();
        readNotifications();


        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.calendar_item, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.item_text_calendar);

        ImageView green = convertView.findViewById(R.id.cal_green_check);
        ImageView yellow = convertView.findViewById(R.id.cal_yellow_bar);
        ImageView red = convertView.findViewById(R.id.cal_red_x);
        ImageView trashCan = convertView.findViewById(R.id.trashButton_pantry);

        green.setVisibility(View.INVISIBLE);
        yellow.setVisibility(View.INVISIBLE);
        red.setVisibility(View.INVISIBLE);
        trashCan.setVisibility(View.INVISIBLE);


        Date currentTime = Calendar.getInstance().getTime();
        long diff = item.getExpDate().getTime() - currentTime.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if(item.getQuantity() == -1){
            trashCan.setVisibility(View.VISIBLE);
        } else {
            if (days < 0) {
                red.setVisibility(View.VISIBLE);
            } else if (days <= 7) {
                yellow.setVisibility((View.VISIBLE));
            } else {
                green.setVisibility(View.VISIBLE);
            }
        }

        trashCan.setOnClickListener(v -> delete(list.get(position)));
        String prettyDate = item.getExpDate().toString();
        prettyDate = prettyDate.substring(0, prettyDate.length() - 18);
        tv.setText(prettyDate + " - " + item.getName());

        return convertView;
    }
    private void delete(NotificationFood item){
        list.remove(item);
        for(NotificationFood f: tempList){
            if(item.getName().compareTo(f.getName()) == 0){
                tempList.remove(f);
                break;
            }
        }
        writeNotifications();
        par.invalidate();
        this.notifyDataSetChanged();
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

            for(NotificationFood f: list) {
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

    private void readFoods() {
        tempList = new ArrayList<>();

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

                    tempList.add(new NotificationFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
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

                    tempList.add(new NotificationFood(item[0], Integer.parseInt(item[1]),
                            Integer.parseInt(item[2]), d));
                }
            }

            s.close();
        } catch (FileNotFoundException | ParseException e) {
            e.printStackTrace();
        }
    }
}
