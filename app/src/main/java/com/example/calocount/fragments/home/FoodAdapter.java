package com.example.calocount.fragments.home;

import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.calocount.Constants;
import com.example.calocount.data.FoodEntry;
import com.example.calocount.R;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.MyViewHolder> {

    private Context context;
    private List<FoodEntry> itemList;

    public FoodAdapter(Context context, List<FoodEntry> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewItem;
        TextView textViewDateTime;
        TextView textViewDescription;
        TextView textViewCalories;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageViewItem = itemView.findViewById(R.id.imageViewItem);
            textViewDateTime = itemView.findViewById(R.id.textViewDateTime);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewCalories = itemView.findViewById(R.id.textViewCalories);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food_entry, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        FoodEntry currentItem = itemList.get(position);
        holder.imageViewItem.setImageResource(currentItem.getImageResource());

        //holder.textViewDateTime.setText(currentItem.getDateTime().toString());
        //holder.textViewDateTime.setText(DateFormat.format("MMM dd, h:mm a", currentItem.getDateTime()));
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        String timeFormat = prefs.getString(Constants.KEY_TIME_FORMAT, "24-hour format");

        String formatString = "MMM dd, HH:mm";
        if (timeFormat.equals("12-hour format"))
            formatString = "MMM dd, h:mm a";
        holder.textViewDateTime.setText(DateFormat.format(formatString, currentItem.getDateTime()));

        holder.textViewDescription.setText(currentItem.getDescription());

        int calories = currentItem.getCalories();
        //Log.d("Adapter", "DEBUG - Item: " + currentItem.getDescription() + " calories: " + calories);
        String caloriesText = (calories == -1) ? "-" : "" + calories;
        //Log.d("Adapter", "DEBUG - Setting text: " + caloriesText);
        holder.textViewCalories.setText(caloriesText);

        // Implemented with Lambda notation, (instead of using an anonymous inner class - like below)
        holder.itemView.setOnClickListener(view -> {
            Toast.makeText(view.getContext(), "Clicked: " + currentItem.getDateTime(), Toast.LENGTH_SHORT).show();
            Log.d("Adapter", "Clicked: " + currentItem.getDateTime());
        });

        // Implemented with Lambda notation, (instead of using an anonymous inner class - like below)
        /*holder.textViewCalories.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            Log.d("Adapter", "Calories: " + currentItem.getDescription() + " - " + currentItem.getCalories());
            // some chosen logic...
            // If editing then:
                // notifyItemChanged(currentPosition);
        });*/


        // beginner option:
        // Set an OnClickListener on the itemView to handle clicks (== anonymous inner class)
        // (instead of the recommended way: using an interface for click events)
        /*
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click here, you have access to currentItem or position
                Toast.makeText(v.getContext(), "Clicked: " + currentItem.getName(), Toast.LENGTH_SHORT).show();
                Log.d("Adapter", "Clicked: " + currentItem.getName());
            }
        });

        holder.textViewCalories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle click here, you have access to currentItem or position
                int currentPosition = holder.getAdapterPosition();
                Log.d("Adapter", "Toggle Like: " + currentItem.getName());
                currentItem.setLike(!currentItem.isLiked());
                if (currentItem.isLiked())
                    Toast.makeText(v.getContext(), "I LOVE: " + currentItem.getName() + "s", Toast.LENGTH_SHORT).show();
                notifyItemChanged(currentPosition);
            }
        });
        */

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}