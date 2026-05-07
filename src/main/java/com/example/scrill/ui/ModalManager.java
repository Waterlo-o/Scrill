package com.example.scrill.ui;

import com.example.scrill.*;
import com.example.scrill.util.SpotifyHelper;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

public class ModalManager {

    private final HelloController controller;

    // --- ПОЛЯ ---
    private File tempAudioFile = null;
    private File tempCoverFile = null;
    public File tempPlaylistCoverFile = null;
    private File tempEditCoverFile = null;
    private Track trackToEdit = null;
    public String editingPlaylistName = null;

    private static final String ACTIVE_TAB_STYLE =
            "-fx-background-color: -tertiary-bg; -fx-text-fill: white; -fx-font-weight: bold;" +
                    "-fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;";
    private static final String INACTIVE_TAB_STYLE =
            "-fx-background-color: transparent; -fx-text-fill: #ADA9BA;" +
                    "-fx-background-radius: 20; -fx-padding: 8 20; -fx-cursor: hand;";

    public ModalManager(HelloController controller) {
        this.controller = controller;
    }

    // ==========================================
    // ADD TRACK MODAL
    // ==========================================

    public void openAddTrackModal() {
        toggleGlassmorphism(true);
        controller.modalOverlay.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(300), controller.modalOverlay);
        ft.setFromValue(0.0); ft.setToValue(1.0);

        ScaleTransition st = new ScaleTransition(Duration.millis(300), controller.modalCard);
        st.setFromX(0.8); st.setFromY(0.8);
        st.setToX(1.0); st.setToY(1.0);

        ft.play(); st.play();
    }

    public void closeAddTrackModal() {
        toggleGlassmorphism(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), controller.modalOverlay);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> controller.modalOverlay.setVisible(false));
        ft.play();
    }

    public void onChooseCoverClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Cover Image");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(controller.modalOverlay.getScene().getWindow());
        if (file != null) {
            tempCoverFile = file;
            controller.newTrackCoverImage.setImage(new Image(file.toURI().toString()));
            controller.coverUploadArea.getStyleClass().remove("input-error");
            controller.manualAddErrorLabel.setVisible(false);
        }
    }

    public void onChooseAudioClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select MP3 File");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Audio Files", "*.mp3"));
        File file = fileChooser.showOpenDialog(controller.modalOverlay.getScene().getWindow());
        if (file != null) {
            tempAudioFile = file;
            controller.selectedAudioLabel.setText(file.getName());
            controller.selectedAudioLabel.setStyle("-fx-text-fill: #706F8E; -fx-font-size: 12px;");
            controller.manualAddErrorLabel.setVisible(false);

            if (controller.newTrackTitleField.getText().isEmpty()) {
                String name = file.getName().replace(".mp3", "");
                if (name.contains("-")) {
                    String[] parts = name.split("-", 2);
                    controller.newTrackTitleField.setText(parts[0].trim());
                    controller.newTrackArtistField.setText(parts[1].trim());
                } else {
                    controller.newTrackTitleField.setText(name);
                }
            }
        }
    }

    public void onSaveNewTrackClick() {
        boolean hasError = false;

        controller.newTrackTitleField.getStyleClass().remove("input-error");
        controller.newTrackArtistField.getStyleClass().remove("input-error");
        controller.coverUploadArea.getStyleClass().remove("input-error");
        controller.selectedAudioLabel.setStyle("-fx-text-fill: #706F8E; -fx-font-size: 12px;");
        controller.manualAddErrorLabel.setVisible(false);

        if (controller.newTrackTitleField.getText().trim().isEmpty()) {
            controller.newTrackTitleField.getStyleClass().add("input-error");
            hasError = true;
        }
        if (controller.newTrackArtistField.getText().trim().isEmpty()) {
            controller.newTrackArtistField.getStyleClass().add("input-error");
            hasError = true;
        }
        if (tempAudioFile == null) {
            controller.selectedAudioLabel.setStyle(
                    "-fx-text-fill: #FF5252; -fx-font-size: 12px; -fx-font-weight: bold;");
            hasError = true;
        }
        if (tempCoverFile == null) {
            controller.coverUploadArea.getStyleClass().add("input-error");
            hasError = true;
        }

        if (hasError) {
            controller.manualAddErrorLabel.setVisible(true);
            return;
        }

        String title = controller.newTrackTitleField.getText().trim();
        String artist = controller.newTrackArtistField.getText().trim();
        String baseFileName = title + " ~~ " + artist;
        File musicFolder = controller.getCurrentMusicFolder();
        if (!musicFolder.exists()) musicFolder.mkdir();

        File newAudioFile = new File(musicFolder, baseFileName + ".mp3");

        try {
            Files.copy(tempAudioFile.toPath(), newAudioFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            String ext = tempCoverFile.getName()
                    .substring(tempCoverFile.getName().lastIndexOf("."));
            File newCoverFile = new File(musicFolder, baseFileName + ext);
            Files.copy(tempCoverFile.toPath(), newCoverFile.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);

            controller.newTrackTitleField.clear();
            controller.newTrackArtistField.clear();
            controller.newTrackCoverImage.setImage(null);
            controller.selectedAudioLabel.setText("No file selected");
            tempAudioFile = null;
            tempCoverFile = null;

            closeAddTrackModal();
            controller.libraryController.loadMusicLibrary();
            controller.libraryController.calculateDurations();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void switchToAddManual() {
        controller.manualAddForm.setVisible(true);
        controller.linkAddForm.setVisible(false);
        controller.tabManualBtn.setStyle(ACTIVE_TAB_STYLE);
        controller.tabLinkBtn.setStyle(INACTIVE_TAB_STYLE);
    }

    public void switchToAddLink() {
        controller.manualAddForm.setVisible(false);
        controller.linkAddForm.setVisible(true);
        controller.tabManualBtn.setStyle(INACTIVE_TAB_STYLE);
        controller.tabLinkBtn.setStyle(ACTIVE_TAB_STYLE);

        controller.newTrackLinkField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.trim().isEmpty()) {
                controller.downloadLinkBtn.setText("Download & Add");
            } else if (newVal.trim().startsWith("http")) {
                controller.downloadLinkBtn.setText("⬇  Download from URL");
            } else {
                controller.downloadLinkBtn.setText("🔍  Search & Download");
            }
        });
    }

    public void onDownloadFromLinkClick() {
        String url = controller.newTrackLinkField.getText().trim();
        controller.newTrackLinkField.getStyleClass().remove("input-error");

        if (url.isEmpty()) {
            controller.newTrackLinkField.getStyleClass().add("input-error");
            return;
        }

        boolean isSearch = !url.startsWith("http");
        controller.downloadLinkBtn.setDisable(true);
        controller.downloadLinkBtn.setText(isSearch ? "Searching..." : "Downloading...");
        controller.newTrackLinkField.setDisable(true);

        new Thread(() -> {
            try {
                File musicFolder = controller.getCurrentMusicFolder();
                String profilePath = controller.ROOT_MUSIC_DIR + "/" + controller.profileController.currentProfileName;
                String downloadTarget = url;
                String audioSuffix = (controller.ignoreClipToggle != null
                        && controller.ignoreClipToggle.isSelected()) ? " official audio" : "";

                if (url.toLowerCase().contains("spotify")) {
                    Platform.runLater(() -> controller.downloadLinkBtn.setText("Fetching title..."));
                    String parsedTitle = SpotifyHelper.getTitleFromSpotify(url);
                    if (parsedTitle != null && !parsedTitle.isEmpty()) {
                        downloadTarget = "ytsearch1:" + parsedTitle + audioSuffix;
                    } else {
                        throw new Exception("Could not fetch Spotify track info.");
                    }
                } else if (isSearch) {
                    downloadTarget = "ytsearch1:" + url + audioSuffix;
                }

                Platform.runLater(() -> controller.downloadLinkBtn.setText("Downloading..."));

                ProcessBuilder builder = new ProcessBuilder(
                        "tools/yt-dlp.exe",
                        "--ffmpeg-location", "tools/ffmpeg.exe",
                        "-x", "--audio-format", "mp3",
                        "--windows-filenames",
                        "--write-thumbnail", "--convert-thumbnails", "jpg",
                        "--embed-thumbnail", "--add-metadata",
                        "--match-filter", "!is_live",
                        "-o", profilePath + "/%(title)s ~~ %(uploader)s.%(ext)s",
                        downloadTarget
                );
                builder.environment().put("PYTHONIOENCODING", "utf-8");
                builder.redirectErrorStream(true);
                Process process = builder.start();

                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        //System.out.println("[yt-dlp]: " + line);
                    }
                }

                int exitCode = process.waitFor();
                final boolean isSearchFinal = isSearch;

                Platform.runLater(() -> {
                    controller.downloadLinkBtn.setDisable(false);
                    controller.downloadLinkBtn.setText(
                            isSearchFinal ? "🔍  Search & Download" : "⬇  Download from URL");
                    controller.newTrackLinkField.setDisable(false);

                    if (exitCode == 0) {
                        controller.libraryController.convertWebpThumbnailsToJpg();
                        controller.newTrackLinkField.clear();
                        closeAddTrackModal();
                        controller.libraryController.loadMusicLibrary();
                        controller.libraryController.calculateDurations();
                    } else {
                        controller.downloadLinkBtn.setText("Error! Try Again");
                        controller.newTrackLinkField.getStyleClass().add("input-error");
                    }
                });

            } catch (Exception e) {
                System.err.println("Download error: " + e.getMessage());
                Platform.runLater(() -> {
                    controller.downloadLinkBtn.setDisable(false);
                    controller.downloadLinkBtn.setText("Error! Try Again");
                    controller.newTrackLinkField.setDisable(false);
                });
            }
        }).start();
    }

    // ==========================================
    // EDIT TRACK MODAL
    // ==========================================

    public void openEditTrackModal(Track track) {
        toggleGlassmorphism(true);
        if (track == null) return;
        this.trackToEdit = track;
        this.tempEditCoverFile = null;

        controller.editTrackTitleField.setText(track.getTitle());
        controller.editTrackArtistField.setText(track.getArtist());
        controller.editTrackCoverImage.setImage(track.getCover());

        controller.editTrackModalOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), controller.editTrackModalOverlay);
        ft.setFromValue(0.0); ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), controller.editTrackModalCard);
        st.setFromX(0.8); st.setFromY(0.8); st.setToX(1.0); st.setToY(1.0);
        ft.play(); st.play();
    }

    public void closeEditTrackModal() {
        toggleGlassmorphism(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), controller.editTrackModalOverlay);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> controller.editTrackModalOverlay.setVisible(false));
        ft.play();
    }

    public void onSaveEditedTrackClick() {
        if (trackToEdit == null) return;

        String newTitle = controller.editTrackTitleField.getText().trim();
        String newArtist = controller.editTrackArtistField.getText().trim();
        File oldAudioFile = trackToEdit.getFile();
        String oldFileName = oldAudioFile.getName();
        String newFileName = newTitle + " ~~ " + newArtist + ".mp3";

        if (controller.playerController.mediaPlayer != null && oldAudioFile.toURI().toString()
                .equals(controller.playerController.mediaPlayer.getMedia().getSource())) {
            controller.playerController.mediaPlayer.stop();
            controller.playerController.mediaPlayer.dispose();
            controller.playerController.mediaPlayer = null;
            controller.playerController.isPlaying = false;
            controller.playIcon.setIconLiteral("mdi-play-circle");
        }

        System.gc();

        try {
            File musicFolder = controller.getCurrentMusicFolder();
            File newAudioFile = new File(musicFolder, newFileName);

            if (!oldFileName.equals(newFileName)) {
                Files.move(oldAudioFile.toPath(), newAudioFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            }

            String baseNameOld = oldFileName.replace(".mp3", "");
            String baseNameNew = newFileName.replace(".mp3", "");

            if (tempEditCoverFile != null) {
                for (String ext : new String[]{".jpg", ".png", ".jpeg"}) {
                    new File(musicFolder, baseNameOld + ext).delete();
                }
                String ext = tempEditCoverFile.getName()
                        .substring(tempEditCoverFile.getName().lastIndexOf("."));
                Files.copy(tempEditCoverFile.toPath(),
                        new File(musicFolder, baseNameNew + ext).toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
            } else if (!oldFileName.equals(newFileName)) {
                for (String ext : new String[]{".jpg", ".png", ".jpeg"}) {
                    File oldImg = new File(musicFolder, baseNameOld + ext);
                    if (oldImg.exists()) {
                        oldImg.renameTo(new File(musicFolder, baseNameNew + ext));
                    }
                }
            }

            if (!oldFileName.equals(newFileName)) {
                PlaylistManager.updateTrackNameInAllPlaylists(musicFolder, oldFileName, newFileName);
            }

            closeEditTrackModal();
            controller.libraryController.loadMusicLibrary();
            if (controller.getCurrentView().equals("Home")) {
                controller.dashboardBuilder.updateHomeDashboard();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onChooseEditCoverClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        File file = fileChooser.showOpenDialog(controller.editTrackModalOverlay.getScene().getWindow());
        if (file != null) {
            tempEditCoverFile = file;
            controller.editTrackCoverImage.setImage(new Image(file.toURI().toString()));
        }
    }

    // ==========================================
    // PLAYLIST MODAL
    // ==========================================

    public void onAddPlaylistClick() {
        toggleGlassmorphism(true);
        if (controller.playlistModalOverlay.isVisible()) return;

        editingPlaylistName = null;
        controller.playlistModalTitle.setText("New Playlist");
        controller.savePlaylistBtn.setText("Create Playlist");
        controller.playlistTabHeader.setVisible(true);
        controller.playlistTabHeader.setManaged(true);
        switchToPlaylistManual();

        controller.newPlaylistNameField.clear();
        controller.newPlaylistCoverImage.setImage(null);
        tempPlaylistCoverFile = null;

        controller.playlistModalOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), controller.playlistModalOverlay);
        ft.setFromValue(0.0); ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), controller.playlistModalCard);
        st.setFromX(0.8); st.setFromY(0.8); st.setToX(1.0); st.setToY(1.0);
        ft.play(); st.play();
    }

    public void closePlaylistModal() {
        toggleGlassmorphism(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), controller.playlistModalOverlay);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> controller.playlistModalOverlay.setVisible(false));
        ft.play();
    }

    public void onChoosePlaylistCoverClick() {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Select Playlist Cover");
        fileChooser.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(controller.playlistModalOverlay.getScene().getWindow());
        if (file != null) {
            tempPlaylistCoverFile = file;
            controller.newPlaylistCoverImage.setImage(new Image(file.toURI().toString()));
            controller.newPlaylistNameField.getStyleClass().remove("input-error");
        }
    }

    public void saveNewPlaylist() {
        String name = controller.newPlaylistNameField.getText().trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isEmpty()) {
            controller.newPlaylistNameField.getStyleClass().add("input-error");
            return;
        }

        File profileFolder = controller.getCurrentMusicFolder();

        if (editingPlaylistName != null) {
            boolean success = PlaylistManager.renamePlaylist(
                    profileFolder, editingPlaylistName, name, tempPlaylistCoverFile);
            if (success) {
                if (controller.getCurrentView().equals("PLAYLIST:" + editingPlaylistName)) {
                    controller.setCurrentView("PLAYLIST:" + name);
                    controller.activePlaylist = name;
                }
            }
        } else {
            PlaylistManager.createPlaylist(profileFolder, name, tempPlaylistCoverFile);
        }

        closePlaylistModal();

        Platform.runLater(() -> {
            controller.sidebarController.loadPlaylistsUI();

            if (controller.getCurrentView().equals("PLAYLIST:" + name)) {
                controller.mainTitleLabel.setText(name);
                Image cover = controller.dashboardBuilder.getPlaylistCoverWithFallback(profileFolder, name);
                if (cover != null) {
                    controller.mainHeaderCover.setImage(cover);
                    controller.mainHeaderCover.setVisible(true);
                    controller.mainHeaderCover.setManaged(true);
                } else {
                    controller.mainHeaderCover.setVisible(false);
                    controller.mainHeaderCover.setManaged(false);
                }
                controller.libraryController.loadMusicLibrary();
            }

            if (controller.getCurrentView().equals("Home")) {
                controller.dashboardBuilder.updateHomeDashboard();
            }
        });
    }

    public void switchToPlaylistManual() {
        controller.playlistManualForm.setVisible(true);
        controller.playlistLinkForm.setVisible(false);
        controller.tabPlaylistManualBtn.setStyle(ACTIVE_TAB_STYLE);
        controller.tabPlaylistLinkBtn.setStyle(INACTIVE_TAB_STYLE);
    }

    public void switchToPlaylistLink() {
        controller.playlistManualForm.setVisible(false);
        controller.playlistLinkForm.setVisible(true);
        controller.tabPlaylistManualBtn.setStyle(INACTIVE_TAB_STYLE);
        controller.tabPlaylistLinkBtn.setStyle(ACTIVE_TAB_STYLE);
    }

    public void onImportPlaylistClick() {
        final String urlToImport = controller.importPlaylistLinkField.getText().trim();
        if (urlToImport.isEmpty()) return;

        controller.importPlaylistBtn.setDisable(true);
        controller.importProgressLabel.setText("Распознавание ссылки...");

        boolean isYouTube = urlToImport.toLowerCase().contains("youtube.com")
                || urlToImport.toLowerCase().contains("youtu.be");


        if (!isYouTube) {
            Platform.runLater(() -> {
                controller.importProgressLabel.setText(
                        "Spotify import is not available yet.");
                controller.importPlaylistBtn.setDisable(false);
            });
            return;
        }

        new Thread(() -> {
            try {
                File musicFolder = controller.getCurrentMusicFolder();
                importYouTubePlaylist(urlToImport, musicFolder);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    controller.importProgressLabel.setText("Ошибка: " + e.getMessage());
                    controller.importPlaylistBtn.setDisable(false);
                });
            }
        }).start();
    }

    private void importYouTubePlaylist(String url, File musicFolder) throws Exception {
        Platform.runLater(() -> controller.importProgressLabel.setText("Получение данных YouTube..."));

        ProcessBuilder titlePb = new ProcessBuilder(
                "tools/yt-dlp.exe", "--print", "playlist_title", "--playlist-end", "1", url);
        Process titleP = titlePb.start();
        java.io.BufferedReader titleReader = new java.io.BufferedReader(
                new java.io.InputStreamReader(titleP.getInputStream(), "UTF-8"));
        String playlistName = titleReader.readLine();
        titleP.waitFor();

        if (playlistName == null || playlistName.isEmpty() || playlistName.equals("NA")) {
            playlistName = "YouTube Playlist";
        }
        playlistName = playlistName.replaceAll("[\\\\/:*?\"<>|]", "_");
        final String finalPName = playlistName;

        Platform.runLater(() -> {
            PlaylistManager.createPlaylist(musicFolder, finalPName, null);
            controller.importProgressLabel.setText("Скачивание: " + finalPName);
        });

        ProcessBuilder pb = new ProcessBuilder(
                HelloController.getAppDir() + "/tools/yt-dlp.exe",
                "--ffmpeg-location", HelloController.getAppDir() + "/tools/ffmpeg.exe",
                "-x", "--audio-format", "mp3",
                "--yes-playlist",
                "--add-metadata", "--write-thumbnail",
                "--convert-thumbnails", "jpg", "--embed-thumbnail",
                "-o", musicFolder.getAbsolutePath() + "/%(title)s ~~ %(uploader)s.%(ext)s",
                url
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();

        try (java.io.BufferedReader r = new java.io.BufferedReader(
                new java.io.InputStreamReader(p.getInputStream(), "UTF-8"))) {
            String line;
            while ((line = r.readLine()) != null) {
                //System.out.println("[yt-dlp YouTube]: " + line);
                String parsedName = extractFilenameRobust(line);
                if (parsedName != null) {
                    PlaylistManager.addTrackToPlaylist(musicFolder, finalPName, parsedName);
                }
            }
        }
        p.waitFor();

        Platform.runLater(() -> finishImport(finalPName, musicFolder));
    }

    private void importSpotifyPlaylist(String url, File musicFolder) throws Exception {
        String cleanUrl = url.split("\\?")[0];
        String playlistId = "";
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("playlist/([a-zA-Z0-9]+)").matcher(cleanUrl);
        if (m.find()) playlistId = m.group(1);
        else throw new Exception("ID Spotify плейлиста не найден!");

        String accessToken = SpotifyHelper.getClientToken();

        Platform.runLater(() -> controller.importProgressLabel.setText("Загрузка данных обложки..."));
        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();

        String infoUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "?fields=name,images";
        java.net.http.HttpRequest infoReq = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(infoUrl))
                .header("Authorization", "Bearer " + accessToken)
                .GET().build();

        java.net.http.HttpResponse<String> infoRes = client.send(
                infoReq, java.net.http.HttpResponse.BodyHandlers.ofString());
        org.json.JSONObject infoJson = new org.json.JSONObject(infoRes.body());

        String playlistName = infoJson.optString("name", "Spotify Import")
                .replaceAll("[\\\\/:*?\"<>|]", "_");

        File downloadedCover = null;
        if (infoJson.has("images") && !infoJson.getJSONArray("images").isEmpty()) {
            String imageUrl = infoJson.getJSONArray("images").getJSONObject(0).getString("url");
            try {
                downloadedCover = File.createTempFile("spotify_cover", ".jpg");
                try (java.io.InputStream in = new java.net.URL(imageUrl).openStream()) {
                    Files.copy(in, downloadedCover.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (Exception e) {
                System.err.println("Не удалось скачать обложку: " + e.getMessage());
            }
        }

        final File finalCover = downloadedCover;
        final String finalPName = playlistName;
        Platform.runLater(() -> PlaylistManager.createPlaylist(musicFolder, finalPName, finalCover));

        // Получаем треки
        java.util.List<String> trackQueries = new java.util.ArrayList<>();
        String tracksUrl = "https://api.spotify.com/v1/playlists/" + playlistId + "/items?limit=100";

        while (tracksUrl != null && !tracksUrl.equals("null")) {
            java.net.http.HttpRequest tracksReq = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(tracksUrl))
                    .header("Authorization", "Bearer " + accessToken)
                    .GET().build();

            java.net.http.HttpResponse<String> tracksRes = client.send(
                    tracksReq, java.net.http.HttpResponse.BodyHandlers.ofString());

            //System.out.println("[Spotify] Статус треков: " + tracksRes.statusCode());

            if (tracksRes.statusCode() != 200) {
                //System.out.println("[Spotify] Ошибка: " + tracksRes.body());
                break;
            }

            org.json.JSONObject responseJson = new org.json.JSONObject(tracksRes.body());

            //System.out.println("[Spotify] Ключи ответа: " + responseJson.keySet());

            if (!responseJson.has("items")) {
                //System.out.println("[Spotify] Нет поля items!");
                break;
            }

            org.json.JSONArray items = responseJson.getJSONArray("items");
            //System.out.println("[Spotify] Количество items: " + items.length());

            for (int i = 0; i < items.length(); i++) {
                try {
                    org.json.JSONObject arrayElement = items.getJSONObject(i);

                    // Spotify может вернуть null track (удалённые треки)
                    if (arrayElement.isNull("track")) {
                        //System.out.println("[Spotify] item " + i + " — track is null, пропускаем");
                        continue;
                    }

                    org.json.JSONObject trackObj = arrayElement.getJSONObject("track");

                    if (trackObj.isNull("name")) {
                        //System.out.println("[Spotify] item " + i + " — name is null, пропускаем");
                        continue;
                    }

                    // Пропускаем локальные треки
                    if (trackObj.optBoolean("is_local", false)) {
                        //System.out.println("[Spotify] item " + i + " — локальный трек, пропускаем");
                        continue;
                    }

                    String tName = trackObj.optString("name", "").trim();
                    if (tName.isEmpty()) continue;

                    String artist = "";
                    if (trackObj.has("artists") && !trackObj.getJSONArray("artists").isEmpty()) {
                        artist = trackObj.getJSONArray("artists")
                                .getJSONObject(0).optString("name", "").trim();
                    }

                    String query = artist.isEmpty() ? tName : artist + " - " + tName;
                    if (!trackQueries.contains(query)) {
                        trackQueries.add(query);
                        //System.out.println("[Spotify] Добавлен трек: " + query);
                    }

                } catch (Exception ex) {
                    //System.out.println("[Spotify] Ошибка парсинга item " + i + ": " + ex.getMessage());
                }
            }

            // Следующая страница
            if (responseJson.has("next") && !responseJson.isNull("next")) {
                tracksUrl = responseJson.getString("next");
                //System.out.println("[Spotify] Следующая страница: " + tracksUrl);
            } else {
                tracksUrl = null;
                //System.out.println("[Spotify] Последняя страница, выходим");
            }
        }

        //System.out.println("[Spotify Import] Найдено треков для скачивания: " + trackQueries.size());
        for (String q : trackQueries) {
            //System.out.println("  - " + q);
        }


        //System.out.println("[Spotify Import] Найдено треков для скачивания: " + trackQueries.size());
        for (String q : trackQueries) {
            //System.out.println("  - " + q);
        }

        // Скачиваем треки
        int count = 0;
        for (String query : trackQueries) {
            count++;
            final int current = count;
            final int total = trackQueries.size();

            // ДОБАВЬ:
            //System.out.println("[Spotify Import] Скачиваем (" + current + "/" + total + "): " + query);

            Platform.runLater(() ->
                    controller.importProgressLabel.setText(
                            "Импорт (" + current + "/" + total + "): " + query));

            ProcessBuilder pb = new ProcessBuilder(
                    HelloController.getAppDir() + "/tools/yt-dlp.exe",
                    "--no-colors",
                    "--ffmpeg-location", HelloController.getAppDir() + "/tools/ffmpeg.exe",
                    "-x", "--audio-format", "mp3", "--no-playlist",
                    "--add-metadata", "--write-thumbnail",
                    "--convert-thumbnails", "jpg", "--embed-thumbnail",
                    "-o", musicFolder.getAbsolutePath() + "/%(title)s ~~ %(uploader)s.%(ext)s",
                    "--print", "after_move:filename",
                    "ytsearch1:" + query + " Topic"
            );
            pb.redirectErrorStream(true);
            Process p = pb.start();
            String finalFileName = null;

            try (java.io.BufferedReader r = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = r.readLine()) != null) {
                    // ДОБАВЬ:
                    //System.out.println("[yt-dlp]: " + line);
                    if (line.toLowerCase().endsWith(".mp3") && new File(line.trim()).exists()) {
                        finalFileName = new File(line.trim()).getName();
                        //System.out.println("[Spotify Import] Найден файл: " + finalFileName);
                    }
                }
            }
            int exitCode = p.waitFor();
            //System.out.println("[Spotify Import] Exit code: " + exitCode);


            if (finalFileName == null) {
                String[] parts = query.split(" - ");
                String artistSearch = parts[0].toLowerCase().trim();
                String titleSearch = parts.length > 1 ? parts[1].toLowerCase().trim() : "";
                File[] files = musicFolder.listFiles((dir, name) -> {
                    String lower = name.toLowerCase();
                    return lower.contains(artistSearch) && lower.contains(titleSearch)
                            && lower.endsWith(".mp3");
                });
                if (files != null && files.length > 0) {
                    java.util.Arrays.sort(files,
                            (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
                    finalFileName = files[0].getName();
                }
            }

            if (finalFileName != null) {
                PlaylistManager.addTrackToPlaylist(musicFolder, finalPName, finalFileName);
            }
        }

        Platform.runLater(() -> finishImport(finalPName, musicFolder));
    }

    private void finishImport(String playlistName, File musicFolder) {
        controller.importProgressLabel.setText("Готово!");
        controller.importPlaylistBtn.setDisable(false);
        controller.sidebarController.loadPlaylistsUI();

        if (controller.activePlaylist != null && controller.activePlaylist.equals(playlistName)) {
            controller.libraryController.loadMusicLibrary();
        } else {
            controller.navigateTo("PLAYLIST:" + playlistName, true);
        }

        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> closePlaylistModal());
        pause.play();
    }

    private String extractFilenameRobust(String logLine) {
        String cleanLine = logLine.replaceAll("\u001B\\[[;\\d]*m", "");
        if (cleanLine.contains(" ~~ ") && cleanLine.toLowerCase().contains(".mp3")) {
            try {
                int endIdx = cleanLine.toLowerCase().indexOf(".mp3") + 4;
                String path = cleanLine.substring(0, endIdx);
                int slashIdx = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
                if (slashIdx != -1) path = path.substring(slashIdx + 1);
                if (path.startsWith("\"") || path.startsWith("'")) path = path.substring(1);
                if (path.contains("Destination: ")) path = path.split("Destination: ")[1];
                if (path.contains("into ")) path = path.split("into ")[1];
                return path.trim();
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // ==========================================
    // GLASSMORPHISM
    // ==========================================

    public void toggleGlassmorphism(boolean apply) {
        javafx.scene.Node mainAppNode = controller.rootPane.getChildren().get(0);

        if (apply) {
            if (mainAppNode.getEffect() instanceof javafx.scene.effect.GaussianBlur) return;

            mainAppNode.setCache(true);
            mainAppNode.setCacheHint(javafx.scene.CacheHint.SPEED);

            javafx.scene.effect.GaussianBlur blur = new javafx.scene.effect.GaussianBlur(0);
            mainAppNode.setEffect(blur);

            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(blur.radiusProperty(), 25, Interpolator.EASE_OUT)));
            timeline.play();
        } else {
            if (mainAppNode.getEffect() instanceof javafx.scene.effect.GaussianBlur blur) {
                Timeline timeline = new Timeline(
                        new KeyFrame(Duration.millis(200),
                                new KeyValue(blur.radiusProperty(), 0, Interpolator.EASE_IN)));
                timeline.setOnFinished(e -> {
                    mainAppNode.setEffect(null);
                    mainAppNode.setCache(false);
                });
                timeline.play();
            }
        }
    }

    // ==========================================
    // TOAST
    // ==========================================

    public void showToast(String message) {
        javafx.scene.control.Label toast = new javafx.scene.control.Label(message);
        toast.setStyle(
                "-fx-background-color: -accent-purple; -fx-text-fill: -fab-color;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 10 20;" +
                        "-fx-background-radius: 20;" +
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 5);");
        toast.setMouseTransparent(true);

        javafx.geometry.Insets margin = new javafx.geometry.Insets(0, 0, 120, 0);
        javafx.scene.layout.StackPane.setAlignment(toast, javafx.geometry.Pos.BOTTOM_CENTER);
        javafx.scene.layout.StackPane.setMargin(toast, margin);

        controller.rootPane.getChildren().add(toast);

        toast.setOpacity(0);
        Timeline show = new Timeline(
                new KeyFrame(Duration.millis(200), new KeyValue(toast.opacityProperty(), 1.0)));

        PauseTransition wait = new PauseTransition(Duration.seconds(2.5));
        Timeline hide = new Timeline(
                new KeyFrame(Duration.millis(300), new KeyValue(toast.opacityProperty(), 0.0)));
        hide.setOnFinished(e -> controller.rootPane.getChildren().remove(toast));

        show.setOnFinished(e -> wait.play());
        wait.setOnFinished(e -> hide.play());
        show.play();
    }
}