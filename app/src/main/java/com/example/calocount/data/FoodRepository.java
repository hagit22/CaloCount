package com.example.calocount.data;

import android.content.Context;
import android.util.Log;

import java.util.List;

public class FoodRepository {
    private final FoodEntryDao foodEntryDao;

    public FoodRepository(Context context) {
        this.foodEntryDao = FoodDatabase.getDatabase(context).foodEntryDao();
    }

    public void getAll(FoodCallback<List<FoodEntry>> callback) {
        new Thread(() -> {
            List<FoodEntry> list = foodEntryDao.getAll();
            for (FoodEntry entry : list) {
                Log.d("FoodRepository", "Item: " + entry.getDescription() + " calories: " + entry.getCalories());
            }
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
            for (FoodEntry entry : list) {
                Log.d("FoodRepository", "Item: " + entry.getDescription() + " calories: " + entry.getCalories());
            }
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
}
