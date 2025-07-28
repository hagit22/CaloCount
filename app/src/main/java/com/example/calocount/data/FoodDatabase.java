package com.example.calocount.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {FoodEntry.class}, version = 2)
public abstract class FoodDatabase extends RoomDatabase {
    private static volatile FoodDatabase INSTANCE;

    public abstract FoodEntryDao foodEntryDao();

    // Singleton pattern for database instance
    public static FoodDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (FoodDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    FoodDatabase.class, "food_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

