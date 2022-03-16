package com.example.cmsc434_kitchen_kompanion.ui.recipes;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.cmsc434_kitchen_kompanion.R;
import com.example.cmsc434_kitchen_kompanion.databinding.FragmentRecipesBinding;
import com.example.cmsc434_kitchen_kompanion.ui.list.ListFood;
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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class RecipesFragment extends Fragment {

    private FragmentRecipesBinding binding;
    private ArrayList<Recipe> recipes;
    private ArrayList<PantryFood> pantry;
    private ArrayList<ListFood> list;
    private ListView listView;
    private Button allButton, favButton, veganButton, vegetarianButton;
    DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentRecipesBinding.inflate(inflater, container, false);

        allButton = binding.getRoot().findViewById(R.id.all_button);
        allButton.setOnClickListener(view -> filterList("All"));

        favButton = binding.getRoot().findViewById(R.id.favorite_button);
        favButton.setOnClickListener(view -> filterList("Favorite"));

        veganButton = binding.getRoot().findViewById(R.id.vegan_button);
        veganButton.setOnClickListener(view -> filterList("Vegan"));

        vegetarianButton = binding.getRoot().findViewById(R.id.vegetarian_button);
        vegetarianButton.setOnClickListener(view -> filterList("Vegetarian"));

        recipes = new ArrayList<>();
        pantry = new ArrayList<>();
        list = new ArrayList<>();
        readListFoods();
        readPantryFoods();

        addRecipe(R.raw.recipe1, R.drawable.french_toast);
        addRecipe(R.raw.recipe2, R.drawable.chicken_piccata);
        addRecipe(R.raw.recipe3, R.drawable.tex_mex);
        addRecipe(R.raw.recipe4, R.drawable.garlic_chicken_breasts);
        addRecipe(R.raw.recipe5, R.drawable.barbecue_beef);
        addRecipe(R.raw.recipe6, R.drawable.chocolate_molten_lava_cake);
        addRecipe(R.raw.recipe7, R.drawable.german_pancakes);
        addRecipe(R.raw.recipe8, R.drawable.peanut_butter_and_jelly);
        addRecipe(R.raw.recipe9, R.drawable.black_bean_and_corn);
        addRecipe(R.raw.recipe10, R.drawable.black_bean_burger);
        addRecipe(R.raw.recipe11, R.drawable.german_potato_salad);
        addRecipe(R.raw.recipe12, R.drawable.blender_salsa);
        addRecipe(R.raw.recipe13, R.drawable.pork_chops_and_rice);
        addRecipe(R.raw.recipe14, R.drawable.chipotle_burrito_bowl);

        loadFavs();

        Collections.sort(recipes, (r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));

        setUpList(binding.getRoot());
        setupOnClickListener();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveFavs();
        binding = null;
    }

    private void saveFavs(){
        Context ctx = getContext();
        try {
            assert ctx != null;
            File file = new File(ctx.getFilesDir() + "/userFavs");
            if (!file.exists())
                file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for(Recipe r: recipes){
                if(r.isFavorite()) {
                    bw.write(r.getName());
                    bw.write("\n");
                }
            }

            bw.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFavs(){
        Context ctx = getContext();
        Scanner scanner;
        try {
            assert ctx != null;
            scanner = new Scanner(new File(ctx.getFilesDir() + "/userFavs"));

            String s;

            while(scanner.hasNextLine()) {
                s = scanner.nextLine();
                for(Recipe r: recipes){
                    if(s.compareTo(r.getName()) == 0) {
                        r.favorite();
                        break;
                    }
                }
            }

            scanner.close();
        } catch (FileNotFoundException e) {

        }
    }

    private void popup(View view, Recipe r){
        Context ctx = getContext();

        assert ctx != null;
        LayoutInflater inflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.recipe_popup, null);

        TextView title = popupView.findViewById(R.id.popup_title);
        title.setText(r.getName());
        TextView ingredients = popupView.findViewById(R.id.popup_ingredients);
        ingredients.setText(r.getIngredients());
        TextView instructions = popupView.findViewById(R.id.popup_instructions);
        instructions.setText(r.getInstructions());

        Button addList = popupView.findViewById(R.id.addToListButton);
        if(r.getStatus() == 2)
            addList.setText("You can make this!");
        else
            addList.setOnClickListener(view1 -> addToList(r, addList));

        ImageView img = popupView.findViewById(R.id.recipe_image);
        img.setImageResource(r.getImgId());

        ImageView fav = popupView.findViewById(R.id.img_fav_filled);
        ImageView fav_outline = popupView.findViewById(R.id.img_fav_border);
        fav.setOnClickListener(view13 -> updateFavorite(r, fav, fav_outline));
        fav_outline.setOnClickListener(view14 -> updateFavorite(r, fav, fav_outline));


        if(r.isFavorite()){
            fav.setVisibility(View.VISIBLE);
            fav_outline.setVisibility(View.INVISIBLE);
        }
        else{
            fav.setVisibility(View.INVISIBLE);
            fav_outline.setVisibility(View.VISIBLE);
        }

        final PopupWindow popupWindow = new PopupWindow(popupView, 1000, 1100, true);

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);

        popupView.setOnTouchListener((v, event) -> {
            popupWindow.dismiss();
            return true;
        });
    }

    private void addRecipe(int id, int img_id){
        Scanner s = new Scanner(this.getResources().openRawResource(id));
        Recipe r = new Recipe(s, img_id, pantry);
        recipes.add(r);
    }

    private void filterList(String status){
        boolean vegan = status.compareTo("Vegan") == 0;
        boolean vegetarian = status.compareTo("Vegetarian") == 0;
        boolean all = status.compareTo("All") == 0;
        boolean fav = status.compareTo("Favorite") == 0;

        ArrayList<Recipe> filteredRecipes = new ArrayList<>();

        for(Recipe r: recipes){
            if(all || vegan && r.isVegan() || vegetarian && r.isVegetarian() || fav && r.isFavorite())
                filteredRecipes.add(r);
        }

        RecipeAdapter adapter = new RecipeAdapter(getContext(), 0, filteredRecipes);
        listView.setAdapter(adapter);

        allButton.setTextColor(getResources().getColor(R.color.orange_500));
        allButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        favButton.setTextColor(getResources().getColor(R.color.orange_500));
        favButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        vegetarianButton.setTextColor(getResources().getColor(R.color.orange_500));
        vegetarianButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        veganButton.setTextColor(getResources().getColor(R.color.orange_500));
        veganButton.setBackgroundTintList(getResources().getColorStateList(R.color.white));

        if(all){
            allButton.setTextColor(getResources().getColor(R.color.white));
            allButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
        else if(fav){
            favButton.setTextColor(getResources().getColor(R.color.white));
            favButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
        else if(vegan){
            veganButton.setTextColor(getResources().getColor(R.color.white));
            veganButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
        else if(vegetarian){
            vegetarianButton.setTextColor(getResources().getColor(R.color.white));
            vegetarianButton.setBackgroundTintList(getResources().getColorStateList(R.color.orange_500));
        }
    }

    private void setUpList(View v){
        listView = v.findViewById(R.id.recipeList);
        filterList("All");
    }

    private void setupOnClickListener(){
        listView.setOnItemClickListener((adapterView, view, i, l) -> popup(view, (Recipe) (listView.getItemAtPosition(i))));
    }

    private void addToList(Recipe r, Button addList){
        readPantryFoods();
        HashMap<String, Integer> needed = r.missingItems(pantry);

        for(String s: needed.keySet()){
            int amt = needed.get(s);
            addWOutDup(s, amt);
        }

        writeListFoods();
        addList.setText("Added!");
    }

    private void updateFavorite(Recipe r, ImageView fav, ImageView fav_outline){
        r.favorite();

        if(r.isFavorite()){
            fav.setVisibility(View.VISIBLE);
            fav_outline.setVisibility(View.INVISIBLE);
        }
        else{
            fav.setVisibility(View.INVISIBLE);
            fav_outline.setVisibility(View.VISIBLE);
        }

        filterList("Favorite");
    }

    private void readPantryFoods() {
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

    private void readListFoods() {
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
                    list.add(new ListFood(item[0], Integer.parseInt(item[1])));
                }
            }

            s.close();
        } catch (FileNotFoundException e) {
            list = new ArrayList<>();
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

    private void addWOutDup(String n, int q){
        for (ListFood l: list) {
            if(l.getName().trim().compareToIgnoreCase(n.trim()) == 0) {
                l.addToQuantity(q);
                break;
            }
        }
        list.add(new ListFood(n, q));
    }
}