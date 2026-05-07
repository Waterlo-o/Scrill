package com.example.scrill.util;

import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StatsManager {

    // --- СИНГЛТОН ---
    private static StatsManager instance;
    public static StatsManager getInstance() {
        if (instance == null) instance = new StatsManager();
        return instance;
    }
    private StatsManager() {}

    // --- ДАННЫЕ ---
    private long totalListeningSeconds = 0;
    private final Map<String, Long> dailyListeningStats = new HashMap<>();
    private final Map<String, Integer> playCountStats = new HashMap<>();
    private File profileFolder = null;

    // --- ИНИЦИАЛИЗАЦИЯ ПРОФИЛЯ ---
    public void setProfileFolder(File folder) {
        this.profileFolder = folder;
    }

    public void reset() {
        totalListeningSeconds = 0;
        dailyListeningStats.clear();
        playCountStats.clear();
    }

    // --- ГЕТТЕРЫ ---
    public long getTotalListeningSeconds() { return totalListeningSeconds; }
    public Map<String, Long> getDailyListeningStats() { return dailyListeningStats; }
    public Map<String, Integer> getPlayCountStats() { return playCountStats; }

    // --- СЧЁТЧИК ПРОСЛУШИВАНИЙ ---
    public void incrementPlayCount(String fileName) {
        playCountStats.put(fileName, playCountStats.getOrDefault(fileName, 0) + 1);
        savePlayCounts();
    }

    public int getPlayCount(String fileName) {
        return playCountStats.getOrDefault(fileName, 0);
    }

    // --- ТАЙМЕР ПРОСЛУШИВАНИЯ ---
    public void incrementListeningTime() {
        totalListeningSeconds++;
        String today = LocalDate.now().toString();
        dailyListeningStats.put(today, dailyListeningStats.getOrDefault(today, 0L) + 1);
    }

    // --- СТРИК ---
    public int calculateStreak() {
        int streak = 0;
        LocalDate date = LocalDate.now();
        while (dailyListeningStats.getOrDefault(date.toString(), 0L) > 0) {
            streak++;
            date = date.minusDays(1);
        }
        return streak;
    }

    // --- ФОРМАТИРОВАНИЕ ---
    public String formatListeningTime(long seconds) {
        int hours = (int) (seconds / 3600);
        int mins = (int) ((seconds % 3600) / 60);
        return hours > 0 ? String.format("%dh %02dm", hours, mins) : String.format("%dm", mins);
    }

    public long getWeeklySeconds() {
        long weekly = 0;
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            weekly += dailyListeningStats.getOrDefault(today.minusDays(i).toString(), 0L);
        }
        return weekly;
    }

    // --- ЗАГРУЗКА И СОХРАНЕНИЕ СТАТИСТИКИ ---
    public void loadListeningStats() {
        if (profileFolder == null) return;
        totalListeningSeconds = 0;
        dailyListeningStats.clear();

        File statsFile = new File(profileFolder, "stats.ini");
        if (!statsFile.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(statsFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    if (parts[0].equals("total")) {
                        totalListeningSeconds = Long.parseLong(parts[1]);
                    } else {
                        dailyListeningStats.put(parts[0], Long.parseLong(parts[1]));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[StatsManager] Ошибка чтения статистики: " + e.getMessage());
        }
    }

    public void saveListeningStats() {
        if (profileFolder == null) return;
        File statsFile = new File(profileFolder, "stats.ini");

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(statsFile))) {
            bw.write("total=" + totalListeningSeconds + "\n");
            for (Map.Entry<String, Long> entry : dailyListeningStats.entrySet()) {
                bw.write(entry.getKey() + "=" + entry.getValue() + "\n");
            }
        } catch (Exception e) {
            System.err.println("[StatsManager] Ошибка сохранения статистики: " + e.getMessage());
        }
    }

    // --- ЗАГРУЗКА И СОХРАНЕНИЕ СЧЁТЧИКА ---
    public void loadPlayCounts() {
        if (profileFolder == null) return;
        playCountStats.clear();

        File f = getPlayCountFile();
        if (!f.exists()) {
            //System.out.println("[StatsManager] Файл playcounts не найден: " + f.getAbsolutePath());
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    try {
                        playCountStats.put(parts[0], Integer.parseInt(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
            //System.out.println("[StatsManager] Загружено треков: " + playCountStats.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePlayCounts() {
        if (profileFolder == null) return;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getPlayCountFile()))) {
            for (Map.Entry<String, Integer> e : playCountStats.entrySet()) {
                bw.write(e.getKey() + "=" + e.getValue() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getPlayCountFile() {
        return new File(profileFolder, "playcounts.ini");
    }
}