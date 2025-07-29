package com.example.calocount.fragments.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.calocount.data.FoodEntry;
import com.example.calocount.data.FoodRepository;
import com.example.calocount.databinding.FragmentDashboardBinding;

import java.util.Calendar;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FoodRepository foodRepo;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        // Initialize repository
        foodRepo = new FoodRepository(getContext());

        // Load dashboard
        loadDashboard();

        return binding.getRoot();
    }

    private void loadDashboard() {
        foodRepo.getAll(foodEntries -> {
            requireActivity().runOnUiThread(() -> {
                calculateAndDisplayStats(foodEntries);
            });
        });
    }

    private void calculateAndDisplayStats(List<FoodEntry> allEntries) {
        long currentTime = System.currentTimeMillis();
        long oneDayMs = 24 * 60 * 60 * 1000L;

        // Get start of today
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfToday = calendar.getTimeInMillis();

        // Calculate boundaries
        long sevenDaysAgo = currentTime - (7 * oneDayMs);
        long thirtyDaysAgo = currentTime - (30 * oneDayMs);

        // Calculate statistics
        int todayCalories = 0;
        int last7DaysTotal = 0;
        int last30DaysTotal = 0;
        int allTimeTotal = 0;

        for (FoodEntry entry : allEntries) {
            // Skip entries without calculated calories
            if (entry.getCalories() <= 0) continue;

            long entryTime = entry.getDateTime();

            // Today's calories
            if (entryTime >= startOfToday) {
                todayCalories += entry.getCalories();
            }

            // Last 7 days
            if (entryTime >= sevenDaysAgo) {
                last7DaysTotal += entry.getCalories();
            }

            // Last 30 days
            if (entryTime >= thirtyDaysAgo) {
                last30DaysTotal += entry.getCalories();
            }

            // All time
            allTimeTotal += entry.getCalories();
        }

        // Count days with entries
        int last7DaysDays = Math.max(1, countDaysWithEntries(allEntries, sevenDaysAgo, currentTime));
        int last30DaysDays = Math.max(1, countDaysWithEntries(allEntries, thirtyDaysAgo, currentTime));
        int allTimeDays = Math.max(1, countDaysWithEntries(allEntries, 0, currentTime));

        // Calculate averages
        int avg7Days = last7DaysDays > 0 ? last7DaysTotal / last7DaysDays : 0;
        int avg30Days = last30DaysDays > 0 ? last30DaysTotal / last30DaysDays : 0;
        int avgAllTime = allTimeDays > 0 ? allTimeTotal / allTimeDays : 0;

        // Update UI using binding
        binding.textViewTodayCalories.setText(todayCalories + " cal");
        binding.textView7DayAverage.setText("Last 7 days: " + avg7Days + " cal/day");
        binding.textView30DayAverage.setText("Last 30 days: " + avg30Days + " cal/day");
        binding.textViewAllTimeAverage.setText("All time: " + avgAllTime + " cal/day");
    }

    private int countDaysWithEntries(List<FoodEntry> entries, long startTime, long endTime) {
        // Simple approximation - count unique calendar days
        long dayCount = (endTime - Math.max(startTime, getFirstEntryTime(entries))) / (24 * 60 * 60 * 1000L);
        return Math.max(1, (int) dayCount + 1);
    }

    private long getFirstEntryTime(List<FoodEntry> entries) {
        long firstTime = System.currentTimeMillis();
        for (FoodEntry entry : entries) {
            if (entry.getDateTime() < firstTime) {
                firstTime = entry.getDateTime();
            }
        }
        return firstTime;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}