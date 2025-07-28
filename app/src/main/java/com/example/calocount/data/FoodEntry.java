package com.example.calocount.data;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity // This annotation indicates that this class is an entity in the Room database
public class FoodEntry {
    @PrimaryKey(autoGenerate = true)
    // Added as primary key, Room will use this for database operations
    private long foodEntryId;

    @ColumnInfo(name = "image") // Optional: Column name in the database
    private int imageResource;
    private long dateTime;
    private String description;
    private int calories;

    public FoodEntry(int imageResource, long dateTime, String description) {
        this(imageResource, dateTime, description, -1);
    }

    @Ignore
    public FoodEntry(int imageResource, long dateTime, String description, int calories) {
        this.imageResource = imageResource;
        this.dateTime = dateTime;
        this.description = description;
        this.calories = calories;
    }

    public long getFoodEntryId() {
        return foodEntryId;
    }

    public void setFoodEntryId(long id) {
        this.foodEntryId = id;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCalories() {
        return calories;
    }

    public void setCalories(int calories) {
        this.calories = calories;
    }
}
