package com.example.calocount.fragments.home;

import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calocount.data.FoodEntry;
import com.example.calocount.databinding.FragmentHomeBinding;
import com.example.calocount.utils.CalorieEstimator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.calocount.data.FoodRepository;
import com.example.calocount.R;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    CalorieEstimator calorieEstimator;

    private RecyclerView recyclerView;
    private FoodAdapter foodAdapter;
    private List<FoodEntry> foodEntryList;
    FoodRepository foodRepo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);

        setUpRecyclerView();
        setUpFloatingActionButton();

        foodRepo = new FoodRepository(getContext());
        foodRepo.getAll(list -> updateFoodEntryList(list));
        calorieEstimator = new CalorieEstimator(getContext());

        return binding.getRoot();
    }

    private void setUpRecyclerView() {

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext())); // We use a linear layout manager for the recycler view

        // Here is where the magic happens, and we click everything together!!
        foodEntryList = new ArrayList<>(); // Initialize the list of food entries
        foodAdapter = new FoodAdapter(foodEntryList); // We create an adapter with the list of food entries
        recyclerView.setAdapter(foodAdapter); // We connect the adapter to the recycler view
        // Optional: Set item click listener if needed


        // use ItemTouchHelper for drag and drop or swipe actions if needed
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
        {
            @Override
            public boolean onMove(RecyclerView recyclerView,
                                  RecyclerView.ViewHolder originViewHolder,
                                  RecyclerView.ViewHolder targetViewHolder) {
                int fromPos = originViewHolder.getAdapterPosition();
                int toPos = targetViewHolder.getAdapterPosition();
                // Swap items and notify adapter
                Collections.swap(foodEntryList, fromPos, toPos);
                foodAdapter.notifyItemMoved(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                FoodEntry foodEntryItem = foodEntryList.get(position);
                if (direction == ItemTouchHelper.RIGHT) {   // Swipe right
                    shareFoodEntryItem(foodEntryItem);
                    foodAdapter.notifyItemChanged(position);
                }
                else if (direction == ItemTouchHelper.LEFT) {  // Swipe left
                    removeFoodEntry(position);
                    undoFoodEntryRemoval(foodEntryItem, position);
                }
            }

            @Override
            public void onChildDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                                    float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(canvas, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

                View itemView = viewHolder.itemView;
                Paint paint = new Paint();

                if (dX < 0) { // Swiping left
                    paint.setColor(Color.RED);
                    canvas.drawRect((float) itemView.getRight() + dX, (float) itemView.getTop(),
                            (float) itemView.getRight(), (float) itemView.getBottom(), paint);
                }
                else if (dX > 0) { // Swiping right
                    paint.setColor(Color.GREEN);
                    canvas.drawRect((float) itemView.getLeft(), (float) itemView.getTop(),
                            (float) itemView.getLeft() + dX, (float) itemView.getBottom(), paint);
                }
            }

            private void shareFoodEntryItem(FoodEntry foodEntryItem) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this meal: " + foodEntryItem.getDescription());
                startActivity(Intent.createChooser(shareIntent, "Share via"));
            }

            private void removeFoodEntry(int position) {
                foodEntryList.remove(position);
                foodAdapter.notifyItemRemoved(position);
            }

            private void undoFoodEntryRemoval(FoodEntry foodEntryItem, int position) {
                Snackbar snackbar = Snackbar.make(recyclerView, "Item removed", Snackbar.LENGTH_SHORT);
                snackbar.setAction("UNDO remove " + foodEntryItem.getDescription().toUpperCase(),
                        view -> addBackToFoodEntryList(foodEntryItem, position));
                snackbar.addCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar transientBottomBar, int event) {
                        if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                            // Delete permanently if not undone
                            foodRepo.delete(foodEntryItem, list -> updateFoodEntryList(list));
                        }
                    }
                });
                snackbar.show();
            }

            private void addBackToFoodEntryList(FoodEntry foodEntryItem, int position) {
                foodEntryList.add(position, foodEntryItem);
                foodAdapter.notifyItemInserted(position);
            }

        }; // End of anonymous inner class

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void setUpFloatingActionButton() {
        FloatingActionButton fab = binding.fabAddFoodEntry;

        fab.setOnClickListener( view -> {
            // inflate the custom dialog layout
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_food_entry, null);

            EditText descInput = dialogView.findViewById(R.id.edit_food_desc);
            ImageView foodImg = dialogView.findViewById(R.id.img_food);

            // set default image (later we will get from gallery/camera)
            foodImg.setImageResource(R.drawable.ic_food_default);
            foodImg.setTag(R.drawable.ic_food_default);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Add New Food Entry")
                    .setView(dialogView)
                    .setPositiveButton("Add", (dialog, which) -> {
                        String desc = descInput.getText().toString().trim();
                        submitFoodEntry(desc, (int) foodImg.getTag());
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .setCancelable(false);

            // Submit also using the keyboard's "Done" action / Enter key
            /*AlertDialog dialog = builder.create();
            descInput.setOnEditorActionListener((v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                            submitFoodEntry.run();  // Same method, no duplication
                            dialog.dismiss();
                            return true;
                }
                return false;
            });*/

            builder.show();
        });
    }

    private void submitFoodEntry(String description, int imageResource) {
        if (!description.isEmpty()) {
            long dateTime = System.currentTimeMillis();
            Log.d("HomeFragment", "Description: " + description);

            // Add immediately with -1 calories marking it as unknown
            FoodEntry newFoodEntry = new FoodEntry(imageResource, dateTime, description, -1);
            foodRepo.insert(newFoodEntry, (insertedItem, itemList) -> {
                updateFoodEntryList(itemList);
                newFoodEntry.setFoodEntryId(insertedItem.getFoodEntryId());
            });

            // Calculate calories in background
            estimateAndUpdateCalories(newFoodEntry);
        }
    }

    private void estimateAndUpdateCalories(FoodEntry foodEntry) {
        Log.d("HomeFragment", "Starting AI estimation for: " + foodEntry.getDescription());
        calorieEstimator.estimateCalories(foodEntry.getDescription(), new CalorieEstimator.CalorieListener() {
            @Override
            public void onSuccess(int calories) {
                long foodEntryId = foodEntry.getFoodEntryId();
                Log.d("HomeFragment", "AI onSuccess - ID: " + foodEntryId + " calories: " + calories);
                foodEntry.setCalories(calories);
                foodRepo.update(foodEntry, list -> {
                    Log.d("HomeFragment", "Database updated successfully");
                    updateFoodEntryList(list);
                });
            }

            @Override
            public void onError(String error) {
                Log.e("HomeFragment", "Calorie estimation failed: " + error);
                // Optional: Show user-friendly error message
                // Toast.makeText(getContext(), "Couldn't estimate calories", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Create a single callback method
    private void updateFoodEntryList(List<FoodEntry> list) {
        requireActivity().runOnUiThread(() -> {
            foodEntryList.clear();
            foodEntryList.addAll(list);
            updateEmptyState();
            foodAdapter.notifyDataSetChanged();
        });
    }

    private void updateEmptyState() {
        if (foodEntryList.isEmpty()) {
            binding.textViewEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        }
        else {
            binding.textViewEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}