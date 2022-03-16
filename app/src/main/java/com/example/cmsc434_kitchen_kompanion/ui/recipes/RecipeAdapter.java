package com.example.cmsc434_kitchen_kompanion.ui.recipes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.cmsc434_kitchen_kompanion.R;

import java.util.List;

public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public RecipeAdapter(Context c, int resource, List<Recipe> recipeList){
        super(c, resource, recipeList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Recipe recipe = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.recipe_cell, parent, false);
        }

        TextView tv = convertView.findViewById(R.id.textViewRecipeItem);

        tv.setText(recipe.getName());

        ImageView green = convertView.findViewById(R.id.recipe_green_check);
        ImageView yellow = convertView.findViewById(R.id.recipe_yellow_bar);
        ImageView red = convertView.findViewById(R.id.recipe_red_x);
        ImageView fav = convertView.findViewById(R.id.recipe_favorite);

        green.setVisibility(View.INVISIBLE);
        yellow.setVisibility(View.INVISIBLE);
        red.setVisibility(View.INVISIBLE);
        fav.setVisibility(View.INVISIBLE);

        int val = recipe.getStatus();

        if(val == 0)
            red.setVisibility(View.VISIBLE);
        else if(val == 1)
            yellow.setVisibility(View.VISIBLE);
        else if(val == 2)
            green.setVisibility(View.VISIBLE);

        if(recipe.isFavorite())
            fav.setVisibility(View.VISIBLE);

        return convertView;
    }
}
