package com.example.calocount.data;

import android.content.Context;
import android.util.Log;

import java.util.List;

import com.example.calocount.R;

public class FoodRepository {
    private final FoodEntryDao foodEntryDao;

    public FoodRepository(Context context) {
        this.foodEntryDao = FoodDatabase.getDatabase(context).foodEntryDao();
    }

    public void getAll(FoodCallback<List<FoodEntry>> callback) {
        new Thread(() -> {
            List<FoodEntry> list = foodEntryDao.getAll();
            /*for (FoodEntry entry : list) {
                Log.d("FoodRepository", "Item: " + entry.getDescription() + " calories: " + entry.getCalories());
            }*/
            if (list == null || list.isEmpty()) {
//                dao.insert(new FoodEntry(R.drawable.apple, "Apple", "Rich in fiber and vitamin C."));
//                dao.insert(new FoodEntry(R.drawable.banana, "Banana", "Great source of potassium."));
//                dao.insert(new FoodEntry(R.drawable.orange, "Orange", "Loaded with vitamin C."));
//                dao.insert(new FoodEntry(R.drawable.strawberry, "Strawberry", "Full of antioxidants."));
//                dao.insert(new FoodEntry(R.drawable.watermelon, "Watermelon", "Very refreshing and hydrating."));
//
//                list = dao.getAll(); // Fetch again after inserting initial items
            }
            callback.onResult(list);
        }).start();
    }

    public void insert(FoodEntry item, FoodInsertCallback<FoodEntry, List<FoodEntry>> callback) {
        new Thread(() -> {
            long newId = foodEntryDao.insert(item);
            item.setFoodEntryId(newId);
            List<FoodEntry> itemList = foodEntryDao.getAll();
            callback.onResult(item, itemList);
        }).start();
    }

    public void update(FoodEntry item, FoodCallback<List<FoodEntry>> callback) {
        new Thread(() -> {
            Log.d("FoodRepository", "About to update item with calories: " + item.getCalories());
            foodEntryDao.update(item);
            Log.d("FoodRepository", "DAO update completed");
            List<FoodEntry> list = foodEntryDao.getAll();
            /*for (FoodEntry entry : list) {
                Log.d("FoodRepository", "Item: " + entry.getDescription() + " calories: " + entry.getCalories());
            }*/
            callback.onResult(list);
        }).start();
    }

    public void delete(FoodEntry item, FoodCallback<List<FoodEntry>> callback) {
        new Thread(() -> {
            foodEntryDao.delete(item);
            List<FoodEntry> list = foodEntryDao.getAll();
            callback.onResult(list);
        }).start();
    }

    public interface FoodCallback<T> {
        void onResult(T result);
    }
    public interface FoodInsertCallback<T, U> {
        void onResult(T item, U itemList);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////
    // TEST DATA
    // Call either of the following methods once, to test Statistics results in dashboard fragment!

    public void addTestData() {
        new Thread(() -> {
            // Add entries from different days
            long now = System.currentTimeMillis();
            long oneDay = 24 * 60 * 60 * 1000L;

            // Today
            foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, now, "Breakfast sandwich", 450));
            foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, now - 3600000, "Apple", 80));

            // Yesterday
            foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, now - oneDay, "Chicken salad", 350));
            foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, now - oneDay - 3600000, "Pizza slice", 300));

            // Few days ago
            foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, now - (3 * oneDay), "Pasta", 400));
            foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, now - (5 * oneDay), "Burger", 600));
        }).start();
    }

    public void addTestDataAcrossDays() {
        new Thread(() -> {
            long now = System.currentTimeMillis();
            long oneDay = 24 * 60 * 60 * 1000L;

            // Create entries for last 35 days to test all ranges
            for (int daysAgo = 0; daysAgo < 35; daysAgo++) {
                long dayTime = now - (daysAgo * oneDay);

                // Add 2-3 meals per day with varying calories
                foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, dayTime, "Breakfast day " + daysAgo, 300 + (daysAgo * 10)));
                foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, dayTime - 3600000, "Lunch day " + daysAgo, 400 + (daysAgo * 5)));
                foodEntryDao.insert(new FoodEntry(R.drawable.ic_food_default, dayTime - 7200000, "Dinner day " + daysAgo, 500 + (daysAgo * 8)));
            }

            Log.d("Repository", "Added test data for 35 days");
        }).start();
    }

}
