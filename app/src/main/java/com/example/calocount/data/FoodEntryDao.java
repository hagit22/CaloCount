package com.example.calocount.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FoodEntryDao {
    @Query("SELECT * FROM FoodEntry ORDER BY dateTime DESC")
    List<FoodEntry> getAll();

    @Insert
    long insert(FoodEntry item);

    @Delete
    void delete(FoodEntry item);

    @Update
    void update(FoodEntry item);
}
