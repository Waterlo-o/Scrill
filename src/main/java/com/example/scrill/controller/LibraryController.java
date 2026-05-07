package com.example.scrill.controller;

import com.example.scrill.*;
import com.example.scrill.util.StatsManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class LibraryController {

    private final HelloController controller;

    // --- ПОЛЯ ---
    public List<File> musicFiles = new ArrayList<>();
    public javafx.collections.ObservableList<Track> trackListData =
            javafx.collections.FXCollections.observableArrayList();
    public Map<String, String> durationCache = new HashMap<>();
    private boolean isDurationCalculating = false;

    public LibraryController(HelloController controller) {
        this.controller = controller;
    }

    // ==========================================
    // ЗАГРУЗКА БИБЛИОТЕКИ
    // ==========================================

    public void loadMusicLibrary() {
        StatsManager.getInstance().loadListeningStats();
        File folder = controller.getCurrentMusicFolder();
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".mp3"));

        // Заголовок и обложка
        if (controller.activePlaylist != null) {
            controller.mainTitleLabel.setText(controller.activePlaylist);
            Image pCover = controller.dashboardBuilder
                    .getPlaylistCoverWithFallback(folder, controller.activePlaylist);
            if (pCover != null) {
                controller.mainHeaderCover.setImage(pCover);
                controller.mainHeaderCover.setVisible(true);
                controller.mainHeaderCover.setManaged(true);
            } else {
                controller.mainHeaderCover.setVisible(false);
                controller.mainHeaderCover.setManaged(false);
            }
        } else {
            controller.mainTitleLabel.setText("All My Music");
            controller.mainHeaderCover.setVisible(false);
            controller.mainHeaderCover.setManaged(false);
        }

        // Фильтрация по плейлисту
        List<String> allowedFiles = null;
        if (controller.activePlaylist != null) {
            allowedFiles = PlaylistManager.getTracksFromPlaylist(folder, controller.activePlaylist);
        }

        if (files != null) {
            Arrays.sort(files, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));

            musicFiles.clear();
            trackListData.clear();
            int index = 1;

            for (File file : files) {
                if (allowedFiles != null) {
                    String physicalName = file.getName()
                            .replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
                    boolean found = false;
                    for (String allowedName : allowedFiles) {
                        if (allowedName == null) continue;
                        String cleanAllowed = allowedName
                                .replaceAll("[^\\p{L}\\p{N}]", "").toLowerCase();
                        if (cleanAllowed.equals(physicalName)) { found = true; break; }
                    }
                    if (!found) continue;
                }

                musicFiles.add(file);
                String fileName = file.getName().replace(".mp3", "");
                String title = fileName;
                String artist = "Unknown Artist";

                if (fileName.contains("~~")) {
                    String[] parts = fileName.split("~~", 2);
                    title = parts[0].trim();
                    artist = parts[1].trim();
                }

                String date = new java.text.SimpleDateFormat("dd.MM.yyyy")
                        .format(file.lastModified());
                String cachedDuration = durationCache.getOrDefault(file.getName(), "0:00");
                Image cover = loadSmallCover(file, fileName);

                trackListData.add(new Track(index++, title, artist, date, cachedDuration, cover, file));
            }
        }

        // Восстанавливаем выделение играющего трека
        if (controller.playerController.mediaPlayer != null) {
            String playingFilePath = controller.playerController.mediaPlayer.getMedia().getSource();
            for (int i = 0; i < trackListData.size(); i++) {
                if (trackListData.get(i).getFile().toURI().toString().equals(playingFilePath)) {
                    final int idx = i;
                    Platform.runLater(() -> controller.trackTable.getSelectionModel().select(idx));
                    break;
                }
            }
        }

        Platform.runLater(this::updateLibraryStats);
    }

    // ==========================================
    // ОБЛОЖКИ
    // ==========================================

    public Image loadSmallCover(File file, String fileNameNoExt) {
        File parentDir = file.getParentFile();
        for (String ext : new String[]{".jpg", ".png", ".jpeg", ".JPG", ".PNG"}) {
            File imgFile = new File(parentDir, fileNameNoExt + ext);
            if (imgFile.exists()) {
                return new Image(imgFile.toURI().toString(), 400, 400, true, true, true);
            }
        }
        var res = controller.getClass().getResource("default_cover.png");
        return (res != null) ? new Image(res.toExternalForm(), 400, 400, true, true) : null;
    }

    public File extractCoverFromMp3(File mp3File, File destFolder, String baseName) {
        try {
            org.jaudiotagger.audio.AudioFile audioFile =
                    org.jaudiotagger.audio.AudioFileIO.read(mp3File);
            org.jaudiotagger.tag.Tag tag = audioFile.getTag();
            if (tag == null) return null;

            List<org.jaudiotagger.tag.images.Artwork> artworkList = tag.getArtworkList();
            if (artworkList == null || artworkList.isEmpty()) return null;

            org.jaudiotagger.tag.images.Artwork artwork = artworkList.get(0);
            byte[] imageData = artwork.getBinaryData();
            if (imageData == null || imageData.length == 0) return null;

            String mimeType = artwork.getMimeType();
            String ext = (mimeType != null && mimeType.contains("png")) ? ".png" : ".jpg";

            File coverFile = new File(destFolder, baseName + ext);
            try (FileOutputStream fos = new FileOutputStream(coverFile)) {
                fos.write(imageData);
            }
            //System.out.println("[Cover] Извлечена из метаданных: " + coverFile.getName());
            return coverFile;

        } catch (Exception e) {
            //System.out.println("[Cover] Метаданных нет или ошибка: " + e.getMessage());
            return null;
        }
    }

    // ==========================================
    // УДАЛЕНИЕ ТРЕКА
    // ==========================================

    public void deleteTrack(Track track) {
        if (track == null) return;

        if (controller.playerController.mediaPlayer != null
                && controller.playerController.mediaPlayer.getMedia() != null) {
            if (track.getFile().toURI().toString()
                    .equals(controller.playerController.mediaPlayer.getMedia().getSource())) {
                controller.playerController.mediaPlayer.stop();
                controller.playerController.mediaPlayer.dispose();
                controller.playerController.mediaPlayer = null;
                controller.playerController.isPlaying = false;
                controller.playIcon.setIconLiteral("mdi-play-circle");

                controller.trackTitleLabel.setText("No Track");
                controller.artistNameLabel.setText("-");
                controller.coverImage.setImage(null);
                controller.progressSlider.setValue(0);
                controller.updateSliderGradient();
                controller.currentTimeLabel.setText("0:00");
                controller.totalTimeLabel.setText("0:00");
            }
        }

        System.gc();

        try {
            File audioFile = track.getFile();
            if (audioFile.exists()) {
                boolean deleted = audioFile.delete();
            }

            String baseName = audioFile.getName().replace(".mp3", "");
            File parentDir = audioFile.getParentFile();
            for (String ext : new String[]{".jpg", ".jpeg", ".png", ".JPG", ".PNG", ".webp"}) {
                File img = new File(parentDir, baseName + ext);
                if (img.exists()) img.delete();
            }

            PlaylistManager.updateTrackNameInAllPlaylists(
                    controller.getCurrentMusicFolder(), track.getFile().getName(), null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        musicFiles.remove(track.getFile());
        trackListData.remove(track);

        for (int i = 0; i < trackListData.size(); i++) {
            trackListData.get(i).setId(i + 1);
        }

        controller.trackTable.refresh();
        updateLibraryStats();
        controller.trackTable.getSelectionModel().clearSelection();

        if ("Home".equals(controller.getCurrentView())) {
            controller.dashboardBuilder.updateHomeDashboard();
        }

        Platform.runLater(() -> controller.sidebarController.loadPlaylistsUI());
    }

    // ==========================================
    // DRAG & DROP
    // ==========================================

    public void handleDroppedFiles(List<File> files) {
        File musicFolder = controller.getCurrentMusicFolder();
        controller.loadingOverlay.setVisible(true);
        controller.loadingOverlay.setOpacity(0.85);
        Label loadingLabel = (Label) controller.loadingOverlay.lookup(".label");

        new Thread(() -> {
            int total = files.size();
            int[] current = {0};

            for (File file : files) {
                current[0]++;
                final int cur = current[0];
                Platform.runLater(() -> {
                    if (loadingLabel != null)
                        loadingLabel.setText("Adding " + cur + " / " + total + ": " + file.getName());
                });

                try {
                    String fileName = file.getName().replace(".mp3", "");
                    String title = fileName;
                    String artist = "Unknown Artist";

                    try {
                        org.jaudiotagger.audio.AudioFile af =
                                org.jaudiotagger.audio.AudioFileIO.read(file);
                        org.jaudiotagger.tag.Tag tag = af.getTag();
                        if (tag != null) {
                            String tagTitle = tag.getFirst(org.jaudiotagger.tag.FieldKey.TITLE);
                            String tagArtist = tag.getFirst(org.jaudiotagger.tag.FieldKey.ARTIST);
                            if (tagTitle != null && !tagTitle.isEmpty()) title = tagTitle;
                            if (tagArtist != null && !tagArtist.isEmpty()) artist = tagArtist;
                        }
                    } catch (Exception ignored) {
                        if (fileName.contains(" - ")) {
                            String[] parts = fileName.split(" - ", 2);
                            artist = parts[0].trim();
                            title = parts[1].trim();
                        } else if (fileName.contains("~~")) {
                            String[] parts = fileName.split("~~", 2);
                            title = parts[0].trim();
                            artist = parts[1].trim();
                        }
                    }

                    String newBaseName = title + " ~~ " + artist;
                    String newFileName = newBaseName + ".mp3";
                    File dest = new File(musicFolder, newFileName);

                    if (!file.getParentFile().getCanonicalPath()
                            .equals(musicFolder.getCanonicalPath())) {
                        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }

                    boolean coverFound = false;
                    String baseOld = file.getName().replace(".mp3", "");
                    for (String ext : new String[]{".jpg", ".jpeg", ".png"}) {
                        File coverFile = new File(file.getParentFile(), baseOld + ext);
                        if (coverFile.exists()) {
                            Files.copy(coverFile.toPath(),
                                    new File(musicFolder, newBaseName + ext).toPath(),
                                    StandardCopyOption.REPLACE_EXISTING);
                            coverFound = true;
                            break;
                        }
                    }

                    if (!coverFound) {
                        extractCoverFromMp3(file, musicFolder, newBaseName);
                    }

                } catch (Exception e) {
                    System.err.println("Ошибка: " + file.getName() + " — " + e.getMessage());
                }
            }

            Platform.runLater(() -> {
                Timeline fadeOut = new Timeline(new KeyFrame(Duration.millis(400),
                        new KeyValue(controller.loadingOverlay.opacityProperty(), 0.0)));
                fadeOut.setOnFinished(e -> controller.loadingOverlay.setVisible(false));
                fadeOut.play();

                loadMusicLibrary();
                calculateDurations();
                controller.modalManager.showToast(
                        files.size() + " track" + (files.size() > 1 ? "s" : "") + " added!");
            });
        }).start();
    }

    // ==========================================
    // КОНВЕРТАЦИЯ WEBP
    // ==========================================

    public void convertWebpThumbnailsToJpg() {
        File musicFolder = controller.getCurrentMusicFolder();
        if (!musicFolder.exists()) return;

        File[] webpFiles = musicFolder.listFiles(
                (dir, name) -> name.toLowerCase().endsWith(".webp"));
        if (webpFiles == null || webpFiles.length == 0) return;

        for (File webpFile : webpFiles) {
            String jpgPath = webpFile.getAbsolutePath()
                    .substring(0, webpFile.getAbsolutePath().lastIndexOf(".")) + ".jpg";
            try {
                ProcessBuilder builder = new ProcessBuilder(
                        HelloController.getAppDir() + "/tools/ffmpeg.exe", "-i", webpFile.getAbsolutePath(), "-y", jpgPath);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    while (br.readLine() != null) {}
                }
                int exitCode = process.waitFor();
                if (exitCode == 0) webpFile.delete();
            } catch (Exception e) {
                System.err.println("Ошибка ffmpeg для " + webpFile.getName());
            }
        }
    }

    // ==========================================
    // ДЛИТЕЛЬНОСТИ
    // ==========================================

    public void calculateDurations() {
        Platform.runLater(this::updateLibraryStats);

        if (isDurationCalculating) {
            //System.out.println("[Duration] Уже считается, пропускаем");
            return;
        }

        List<Track> tracksToProcess = new ArrayList<>();
        for (Track track : trackListData) {
            String dur = track.getDuration();
            if (dur == null || dur.equals("0:00") || dur.equals("Error") || dur.isEmpty()) {
                tracksToProcess.add(track);
            }
        }

        //System.out.println("[Duration] Нужно посчитать: " + tracksToProcess.size());

        if (!tracksToProcess.isEmpty()) {
            isDurationCalculating = true;
            processDurationQueue(tracksToProcess, 0);
        }
    }

    private void processDurationQueue(List<Track> queue, int index) {
        if (index >= queue.size()) {
            Platform.runLater(() -> {
                saveDurationCache();
                updateLibraryStats();
                isDurationCalculating = false;
                //System.out.println("[Duration] Готово! Треков: " + queue.size());
            });
            return;
        }

        Track track = queue.get(index);
        try {
            Media media = new Media(track.getFile().toURI().toString());
            MediaPlayer tempPlayer = new MediaPlayer(media);

            tempPlayer.setOnReady(() -> {
                double secs = media.getDuration().toSeconds();
                String dur = String.format("%d:%02d", (int)(secs / 60), (int)(secs % 60));

                track.setDuration(dur);
                durationCache.put(track.getFile().getName(), dur);

                Platform.runLater(() -> {
                    controller.trackTable.refresh();
                    updateLibraryStats();
                });

                tempPlayer.dispose();

                Timeline delay = new Timeline(new KeyFrame(Duration.millis(30),
                        e -> processDurationQueue(queue, index + 1)));
                delay.play();
            });

            tempPlayer.setOnError(() -> {
                track.setDuration("N/A");
                durationCache.put(track.getFile().getName(), "N/A");
                tempPlayer.dispose();
                Platform.runLater(() -> processDurationQueue(queue, index + 1));
            });

            Timeline timeout = new Timeline(new KeyFrame(Duration.seconds(5), e -> {
                if (track.getDuration() == null || track.getDuration().equals("0:00")) {
                    track.setDuration("N/A");
                    tempPlayer.dispose();
                    Platform.runLater(() -> processDurationQueue(queue, index + 1));
                }
            }));
            timeout.play();

        } catch (Exception e) {
            track.setDuration("Error");
            Platform.runLater(() -> processDurationQueue(queue, index + 1));
        }
    }

    // ==========================================
    // КЭШ ДЛИТЕЛЬНОСТЕЙ
    // ==========================================

    public void loadDurationCache() {
        durationCache.clear();
        File cacheFile = getDurationCacheFile();
        if (!cacheFile.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(cacheFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) durationCache.put(parts[0], parts[1]);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void saveDurationCache() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getDurationCacheFile()))) {
            for (Map.Entry<String, String> e : durationCache.entrySet()) {
                bw.write(e.getKey() + "=" + e.getValue() + "\n");
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private File getDurationCacheFile() {
        return new File(controller.getCurrentMusicFolder(), "durations.cache");
    }

    // ==========================================
    // СТАТИСТИКА БИБЛИОТЕКИ
    // ==========================================

    public void updateLibraryStats() {
        int trackCount = trackListData.size();
        int totalSeconds = 0;

        for (Track track : trackListData) {
            String dur = track.getDuration();
            if (dur != null && !dur.equals("0:00")
                    && !dur.equals("N/A") && !dur.equals("Error")) {
                String[] parts = dur.split(":");
                try {
                    if (parts.length == 2) {
                        totalSeconds += (Integer.parseInt(parts[0]) * 60)
                                + Integer.parseInt(parts[1]);
                    } else if (parts.length == 3) {
                        totalSeconds += (Integer.parseInt(parts[0]) * 3600)
                                + (Integer.parseInt(parts[1]) * 60)
                                + Integer.parseInt(parts[2]);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        String timeString = hours > 0
                ? String.format("%dh %02dm", hours, minutes)
                : String.format("%d:%02d", minutes, seconds);

        String finalText = String.format("🎵 %d Tracks   •   ⏱ %s", trackCount, timeString);
        Platform.runLater(() -> controller.libraryStatsLabel.setText(finalText));
    }

    // ==========================================
    // РАЗМЕР ЯЧЕЕК ТАБЛИЦЫ
    // ==========================================

    public void applyTrackDisplaySize() {
        int cellHeight, coverSize, titleSize, artistSize;

        switch (controller.settingsController.trackDisplaySize) {
            case 0 -> { cellHeight = 50; coverSize = 35; titleSize = 12; artistSize = 10; }
            case 2 -> { cellHeight = 90; coverSize = 55; titleSize = 17; artistSize = 14; }
            default -> { cellHeight = 72; coverSize = 48; titleSize = 15; artistSize = 13; }
        }

        controller.trackTable.setStyle(String.format(
                "-fx-background-color: -main-bg; -fx-cell-size: %dpx;", cellHeight));

        final int fCH = cellHeight, fCS = coverSize, fTS = titleSize, fAS = artistSize;
        controller.coverCol.setCellFactory(p -> createSizedCell(Pos.CENTER, "cover", fCH, fCS, fTS, fAS));
        controller.infoCol.setCellFactory(p -> createSizedCell(Pos.CENTER_LEFT, "info", fCH, fCS, fTS, fAS));
        controller.idCol.setCellFactory(p -> createSizedCell(Pos.CENTER, "id", fCH, fCS, fTS, fAS));
        controller.dateCol.setCellFactory(p -> createSizedCell(Pos.CENTER, "date", fCH, fCS, fTS, fAS));
        controller.durationCol.setCellFactory(p -> createSizedCell(Pos.CENTER, "duration", fCH, fCS, fTS, fAS));
    }

    private <T> TableCell<Track, T> createSizedCell(Pos alignment, String colType,
                                                    int cellHeight, int coverSize,
                                                    int titleSize, int artistSize) {
        return new TableCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    HBox cellRoot = new HBox();
                    cellRoot.setAlignment(Pos.CENTER_LEFT);
                    cellRoot.setPrefHeight(cellHeight);

                    StackPane contentPane = new StackPane();
                    contentPane.setAlignment(alignment);
                    HBox.setHgrow(contentPane, Priority.ALWAYS);

                    switch (colType) {
                        case "id" -> contentPane.setPadding(new Insets(0, 5, 0, 5));
                        case "cover" -> contentPane.setPadding(new Insets(0, 10, 0, 0));
                        case "info" -> contentPane.setPadding(new Insets(0, 15, 0, 15));
                    }

                    if (colType.equals("cover") && item instanceof Track t) {
                        ImageView iv = new ImageView();
                        iv.setFitHeight(coverSize); iv.setFitWidth(coverSize);
                        iv.setSmooth(true);
                        Rectangle clip = new Rectangle(coverSize, coverSize);
                        clip.setArcWidth(coverSize * 0.25);
                        clip.setArcHeight(coverSize * 0.25);
                        iv.setClip(clip);
                        controller.setupObjectFitCover(iv);
                        if (t.getCover() != null) iv.setImage(t.getCover());
                        contentPane.getChildren().add(iv);

                    } else if (colType.equals("info") && item instanceof Track t) {
                        VBox v = new VBox(2);
                        v.setAlignment(Pos.CENTER_LEFT);
                        Label title = new Label(t.getTitle());
                        title.setStyle(String.format(
                                "-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: %dpx;",
                                titleSize));
                        Label artist = new Label(t.getArtist());
                        artist.setStyle(String.format(
                                "-fx-text-fill: -text-gray; -fx-font-size: %dpx;", artistSize));
                        v.getChildren().addAll(title, artist);
                        contentPane.getChildren().add(v);

                    } else {
                        Label lbl = new Label(item.toString());
                        lbl.setStyle(String.format(
                                "-fx-text-fill: -text-dark-gray; -fx-font-size: %dpx;", artistSize));
                        contentPane.getChildren().add(lbl);
                    }

                    cellRoot.getChildren().add(contentPane);
                    setGraphic(cellRoot);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                }
            }
        };
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================

    public Track getTrackByFile(File file) {
        for (Track track : trackListData) {
            if (track.getFile().equals(file)) return track;
        }
        return null;
    }

    public static boolean copyAndCropToSquare(File sourceFile, File destFile) {
        try {
            java.awt.image.BufferedImage originalImage = javax.imageio.ImageIO.read(sourceFile);
            if (originalImage == null) return false;

            int width = originalImage.getWidth();
            int height = originalImage.getHeight();

            if (width == height) {
                Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                return true;
            }

            int size = Math.min(width, height);
            int x = (width - size) / 2;
            int y = (height - size) / 2;

            java.awt.image.BufferedImage cropped = originalImage.getSubimage(x, y, size, size);
            String ext = destFile.getName()
                    .substring(destFile.getName().lastIndexOf(".") + 1).toLowerCase();

            if (ext.equals("jpg") || ext.equals("jpeg")) {
                java.awt.image.BufferedImage rgb =
                        new java.awt.image.BufferedImage(size, size,
                                java.awt.image.BufferedImage.TYPE_INT_RGB);
                java.awt.Graphics2D g = rgb.createGraphics();
                g.drawImage(cropped, 0, 0, null);
                g.dispose();
                javax.imageio.ImageIO.write(rgb, ext, destFile);
            } else {
                javax.imageio.ImageIO.write(cropped, ext, destFile);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}