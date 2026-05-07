package com.example.scrill;

import javafx.scene.image.Image;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.scrill.controller.LibraryController.copyAndCropToSquare;

public class PlaylistManager {

    private static File getPlaylistsFolder(File profileFolder) {
        File pFolder = new File(profileFolder, "playlists");
        if (!pFolder.exists()) pFolder.mkdirs();
        return pFolder;
    }

    public static List<String> getAllPlaylists(File profileFolder) {
        List<String> lists = new ArrayList<>();
        File pFolder = getPlaylistsFolder(profileFolder);
        File[] files = pFolder.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File f : files) lists.add(f.getName().replace(".txt", ""));
        }
        return lists;
    }

    public static boolean createPlaylist(File profileFolder, String name, File coverFile) {
        File folder = getPlaylistsFolder(profileFolder);
        File textFile = new File(folder, name + ".txt");
        try {
            boolean created = textFile.createNewFile();
            // Если выбрали картинку — копируем её туда же с тем же именем
            if (created && coverFile != null) {
                String ext = coverFile.getName().substring(coverFile.getName().lastIndexOf("."));
                File destCover = new File(folder, name + ext);
                copyAndCropToSquare(coverFile, destCover);
            }
            return created;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Достает картинку плейлиста, если она есть
    public static Image getPlaylistCover(File profileFolder, String playlistName) {
        File pFolder = getPlaylistsFolder(profileFolder);
        String[] exts = {".jpg", ".png", ".jpeg", ".PNG", ".JPG"};
        for (String ext : exts) {
            File img = new File(pFolder, playlistName + ext);
            if (img.exists()) {
                return new Image(img.toURI().toString(), 100, 100, true, true);
            }
        }
        return null; // Если картинки нет, вернем null
    }

    public static void addTrackToPlaylist(File profileFolder, String playlistName, String trackFileName) {
        // 1. ПРОВЕРКА НА ДУБЛИКАТЫ: не добавляем трек, если он уже есть в плейлисте
        List<String> existingTracks = getTracksFromPlaylist(profileFolder, playlistName);
        if (existingTracks.contains(trackFileName.trim())) {
            return;
        }

        File file = new File(getPlaylistsFolder(profileFolder), playlistName + ".txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(trackFileName);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getTracksFromPlaylist(File profileFolder, String playlistName) {
        File file = new File(getPlaylistsFolder(profileFolder), playlistName + ".txt");
        List<String> tracks = new ArrayList<>();
        if (!file.exists()) return tracks;
        try {
            tracks = Files.readAllLines(file.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tracks;
    }

    // --- НОВЫЕ МЕТОДЫ ДЛЯ УДАЛЕНИЯ ---

    // Удалить сам плейлист и его обложку с диска
    public static void deletePlaylist(File profileFolder, String playlistName) {
        File pFolder = getPlaylistsFolder(profileFolder);

        // Удаляем текстовый файл со списком
        File textFile = new File(pFolder, playlistName + ".txt");
        if (textFile.exists()) textFile.delete();

        // Ищем и удаляем картинку (если она была)
        String[] exts = {".jpg", ".png", ".jpeg", ".PNG", ".JPG"};
        for (String ext : exts) {
            File img = new File(pFolder, playlistName + ext);
            if (img.exists()) img.delete();
        }
    }

    // Удалить конкретный трек из плейлиста
    public static void removeTrackFromPlaylist(File profileFolder, String playlistName, String trackFileName) {
        File file = new File(getPlaylistsFolder(profileFolder), playlistName + ".txt");
        if (!file.exists()) return;

        try {
            // Читаем все строки и сразу очищаем их от лишних пробелов/переносов
            List<String> lines = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);

            // Фильтруем список: оставляем только те треки, которые НЕ совпадают с удаляемым
            List<String> updatedLines = lines.stream()
                    .map(String::trim)
                    .filter(line -> !line.equals(trackFileName.trim()))
                    .collect(Collectors.toList());

            // Перезаписываем файл полностью
            Files.write(file.toPath(), updatedLines, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            //System.out.println("Трек " + trackFileName + " удален из плейлиста " + playlistName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean renamePlaylist(File profileFolder, String oldName, String newName, File newCoverFile) {
        File folder = getPlaylistsFolder(profileFolder); // Вот она, правильная папка!

        File oldTxt = new File(folder, oldName + ".txt");
        File newTxt = new File(folder, newName + ".txt");
        boolean success = false;

        // 1. Переименовываем текстовый файл (если он существует)
        if (oldTxt.exists()) {
            success = oldTxt.renameTo(newTxt);
        }

        if (success) {
            String[] exts = {".jpg", ".png", ".jpeg", ".PNG", ".JPG"};

            if (newCoverFile == null) {
                // 2. Если новую картинку НЕ выбрали, просто переименовываем старую
                for (String ext : exts) {
                    File oldImg = new File(folder, oldName + ext);
                    File newImg = new File(folder, newName + ext);
                    if (oldImg.exists()) oldImg.renameTo(newImg);
                }
            } else {
                // 3. Если выбрали НОВУЮ картинку - удаляем старые и копируем новую
                for (String ext : exts) {
                    File oldImg = new File(folder, oldName + ext);
                    if (oldImg.exists()) oldImg.delete();
                }

                String ext = newCoverFile.getName().substring(newCoverFile.getName().lastIndexOf("."));
                File destCover = new File(folder, newName + ext);
                copyAndCropToSquare(newCoverFile, destCover);
            }
        }
        return success;
    }

    public static void updateTrackNameInAllPlaylists(File folder, String oldName, String newName) {
        File pFolder = getPlaylistsFolder(folder);
        File[] playlistFiles = pFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));

        if (playlistFiles == null) return;

        for (File playlist : playlistFiles) {
            try {
                List<String> lines = new ArrayList<>(java.nio.file.Files.readAllLines(playlist.toPath()));
                boolean wasModified = false;

                if (newName == null) {
                    // Удаляем строку с этим треком
                    wasModified = lines.removeIf(line -> line.trim().equals(oldName.trim()));
                } else {
                    // Переименовываем
                    for (int i = 0; i < lines.size(); i++) {
                        if (lines.get(i).trim().equals(oldName.trim())) {
                            lines.set(i, newName);
                            wasModified = true;
                        }
                    }
                }

                if (wasModified) {
                    java.nio.file.Files.write(playlist.toPath(), lines);
                    //System.out.println("[PlaylistManager] Обновлен плейлист: " + playlist.getName());
                }

            } catch (java.io.IOException e) {
                System.err.println("[PlaylistManager] Ошибка при обновлении плейлиста " + playlist.getName());
                e.printStackTrace();
            }
        }
    }
}