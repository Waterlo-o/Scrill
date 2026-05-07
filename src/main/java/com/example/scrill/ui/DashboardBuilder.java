package com.example.scrill.ui;

import com.example.scrill.*;
import com.example.scrill.util.StatsManager;
import com.example.scrill.util.ThemeManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import javafx.scene.control.OverrunStyle;
import javafx.beans.binding.Bindings;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class DashboardBuilder {

    private final HelloController controller;

    public DashboardBuilder(HelloController controller) {
        this.controller = controller;
    }

    // --- ГЛАВНЫЙ МЕТОД ДАШБОРДА ---
    public void updateHomeDashboard() {
        if (controller.greetingLabel != null) {
            controller.greetingLabel.setText("YOUR LISTENING");
            controller.greetingLabel.setStyle(
                    "-fx-text-fill: -text-white; -fx-font-weight: bold; " +
                            "-fx-font-size: 18px; -fx-letter-spacing: 3px;");
        }

        StatsManager stats = StatsManager.getInstance();

        controller.homeTotalTracks.setText(String.valueOf(controller.getTrackListData().size()));
        File profileFolder = controller.getCurrentMusicFolder();
        List<String> myPlaylists = PlaylistManager.getAllPlaylists(profileFolder);
        controller.homeTotalPlaylists.setText(String.valueOf(myPlaylists.size()));

        int totalSeconds = 0;
        for (Track track : controller.getTrackListData()) {
            String dur = track.getDuration();
            if (dur != null && !dur.equals("0:00") && !dur.equals("N/A") && !dur.equals("Error")) {
                String[] parts = dur.split(":");
                try {
                    if (parts.length == 2)
                        totalSeconds += (Integer.parseInt(parts[0]) * 60) + Integer.parseInt(parts[1]);
                    else if (parts.length == 3)
                        totalSeconds += (Integer.parseInt(parts[0]) * 3600)
                                + (Integer.parseInt(parts[1]) * 60) + Integer.parseInt(parts[2]);
                } catch (NumberFormatException ignored) {}
            }
        }
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        controller.homeTotalDuration.setText(
                hours > 0 ? String.format("%dh %02dm", hours, minutes) : String.format("%dm", minutes));

        // Плейлисты
        if (controller.homePlaylistsContainer != null) {
            controller.homePlaylistsContainer.getChildren().clear();
            if (myPlaylists.isEmpty()) {
                HBox empty = createEmptyState("mdi-playlist-plus", "No playlists yet", "Create your first playlist");
                empty.setOnMouseClicked(e -> controller.onAddPlaylistClick());
                controller.homePlaylistsContainer.getChildren().add(empty);
            } else {
                int pLimit = Math.min(5, myPlaylists.size());
                for (int i = 0; i < pLimit; i++) {
                    controller.homePlaylistsContainer.getChildren().add(
                            createDashboardPlaylistCard(myPlaylists.get(i), profileFolder));
                }
                controller.homePlaylistsContainer.getChildren().add(createAddPlaylistCard());
            }
        }

        // Jump Back In
        controller.recentlyAddedContainer.getChildren().clear();
        List<Track> sortedByDate = new ArrayList<>(controller.getTrackListData());
        sortedByDate.sort((t1, t2) -> Long.compare(t2.getFile().lastModified(), t1.getFile().lastModified()));

        int limit = Math.min(10, sortedByDate.size());
        if (limit == 0) {
            controller.recentlyAddedContainer.getChildren().add(
                    createEmptyState("mdi-music-note", "No tracks yet", "Add your first song to get started"));
        } else {
            for (int i = 0; i < limit; i++) {
                controller.recentlyAddedContainer.getChildren().add(
                        createRecentTrackCardHorizontal(sortedByDate.get(i)));
            }
        }

        // Heavy Rotation
        controller.getCurrentHeavyRotation().clear();
        controller.getCurrentHeavyRotation().addAll(controller.getTrackListData());
        Collections.shuffle(controller.getCurrentHeavyRotation());

        updateHeavyRotationUI(controller.getIsDashboardExpanded() ? 6 : 4);

        // Виджеты
        if (controller.weeklyActivityContainer != null) {
            controller.weeklyActivityContainer.getChildren().clear();
            controller.weeklyActivityContainer.getChildren().add(createWeeklyActivityWidget());
        }
        if (controller.dailyGoalContainer != null) {
            controller.dailyGoalContainer.getChildren().clear();
            controller.dailyGoalContainer.getChildren().add(createDailyGoalWidget());
        }
        if (controller.streakContainer != null) {
            controller.streakContainer.getChildren().clear();
            controller.streakContainer.getChildren().add(createListeningStreakWidget());
        }
        if (controller.paletteContainer != null) {
            controller.paletteContainer.getChildren().clear();
            Track ref = controller.getGlobalPlayingTrack() != null ? controller.getGlobalPlayingTrack() :
                    (!controller.getTrackListData().isEmpty() ? controller.getTrackListData().get(0) : null);
            if (ref != null) controller.paletteContainer.getChildren().add(createPaletteExplorerWidget(ref));
        }
        if (controller.mostPlayedContainer != null) {
            controller.mostPlayedContainer.getChildren().clear();
            controller.mostPlayedContainer.getChildren().add(createMostPlayedWidget());
        }

        updateTopArtistsUI(controller.getIsDashboardExpanded() ? 6 : 4);
        updateNowPlayingWidget();
    }

    // --- NOW PLAYING ВИДЖЕТ ---
    public void updateNowPlayingWidget() {
        if (controller.nowPlayingContent == null || controller.nowPlayingWidget == null) return;
        controller.nowPlayingContent.getChildren().clear();

        controller.nowPlayingWidget.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(controller.nowPlayingWidget, Priority.ALWAYS);

        if (controller.getGlobalPlayingTrack() == null) {
            controller.nowPlayingWidget.setOnMouseEntered(null);
            controller.nowPlayingWidget.setOnMouseExited(null);
            controller.nowPlayingWidget.setScaleX(1.0);
            controller.nowPlayingWidget.setScaleY(1.0);
            controller.nowPlayingWidget.setStyle(
                    "-fx-background-color: -tertiary-bg;" +
                            "-fx-background-radius: 20;" +
                            "-fx-border-color: -border-color;" +
                            "-fx-border-radius: 20;");

            FontIcon musicIcon = new FontIcon("mdi-music-note");
            musicIcon.setIconSize(32);
            musicIcon.getStyleClass().add("icon-dark");

            Label nowLabel = new Label("NOW PLAYING");
            nowLabel.setStyle("-fx-text-fill: -text-dark-gray; -fx-font-size: 10px; -fx-font-weight: bold;");
            Label nothingLabel = new Label("Nothing playing");
            nothingLabel.setStyle("-fx-text-fill: -text-dark-gray; -fx-font-size: 13px;");

            controller.nowPlayingContent.setAlignment(Pos.CENTER);
            controller.nowPlayingContent.getChildren().addAll(nowLabel, musicIcon, nothingLabel);
            return;
        }

        Image coverImg = controller.getGlobalPlayingTrack().getCover();

        if (coverImg != null && coverImg.getProgress() < 1.0) {
            coverImg.progressProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal.doubleValue() >= 1.0) {
                    javafx.application.Platform.runLater(this::updateNowPlayingWidget);
                }
            });
        }

        Color dominant;
        if (coverImg == null || coverImg.getProgress() < 1.0 || coverImg.getWidth() <= 0) {
            dominant = Color.web("#2D2D3D");
        } else {
            dominant = extractDominantColor(coverImg);
        }

        String hex = String.format("#%02X%02X%02X",
                (int)(dominant.getRed() * 255),
                (int)(dominant.getGreen() * 255),
                (int)(dominant.getBlue() * 255));

        Color darker = dominant.darker().darker();
        String hexDark = String.format("#%02X%02X%02X",
                (int)(darker.getRed() * 255),
                (int)(darker.getGreen() * 255),
                (int)(darker.getBlue() * 255));

        Color bg = dominant.deriveColor(0, 0.3, 0.15, 1.0);
        String hexBg = String.format("#%02X%02X%02X",
                (int)(bg.getRed() * 255),
                (int)(bg.getGreen() * 255),
                (int)(bg.getBlue() * 255));

        String baseStyle = String.format(
                "-fx-background-color: linear-gradient(to right, %s, %s);" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: transparent;" +
                        "-fx-border-radius: 20;", hexDark, hexBg);

        String normalShadow = "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 15, 0, 0, 5);";
        String glowShadow = String.format(
                "-fx-effect: dropshadow(three-pass-box, %s, 35, 0.4, 0, 0);", hex);

        controller.nowPlayingWidget.setStyle(baseStyle + normalShadow);

        controller.nowPlayingWidget.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), controller.nowPlayingWidget);
            st.setToX(1.02); st.setToY(1.02);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
            controller.nowPlayingWidget.setStyle(baseStyle + glowShadow);
            controller.nowPlayingWidget.setViewOrder(-1.0);
        });

        controller.nowPlayingWidget.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), controller.nowPlayingWidget);
            st.setToX(1.0); st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
            controller.nowPlayingWidget.setStyle(baseStyle + normalShadow);
            controller.nowPlayingWidget.setViewOrder(0.0);
        });

        controller.nowPlayingContent.setAlignment(Pos.CENTER_LEFT);

        HBox mainLayout = new HBox(20);
        mainLayout.setAlignment(Pos.CENTER_LEFT);
        mainLayout.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(mainLayout, Priority.ALWAYS);

        // Обложка
        StackPane coverContainer = new StackPane();
        coverContainer.setMinSize(90, 90);
        coverContainer.setPrefSize(90, 90);

        ImageView nowCover = new ImageView();
        nowCover.setFitWidth(90); nowCover.setFitHeight(90);
        nowCover.setSmooth(true);
        Rectangle coverClip = new Rectangle(90, 90);
        coverClip.setArcWidth(14); coverClip.setArcHeight(14);
        nowCover.setClip(coverClip);
        controller.setupObjectFitCover(nowCover);

        if (coverImg != null) {
            nowCover.setImage(coverImg);
        } else {
            var res = controller.getClass().getResource("default_cover.png");
            if (res != null) nowCover.setImage(new Image(res.toExternalForm(), 90, 90, true, true));
        }
        nowCover.setEffect(new javafx.scene.effect.DropShadow(20, 0, 4, Color.web(hex + "99")));

        HBox eq = createAnimatedEqualizer();
        eq.setAlignment(Pos.BOTTOM_CENTER);
        eq.setStyle("-fx-padding: 0 0 0 0;");
        for (Node rect : eq.getChildren()) rect.setStyle("-fx-fill: white;");

        if (controller.isPlaying()) {
            eq.setVisible(true);
            Timeline eqAnim = (Timeline) eq.getProperties().get("animTimeline");
            if (eqAnim != null) eqAnim.play();
        }

        coverContainer.getChildren().addAll(nowCover, eq);

        // Правая часть
        VBox rightPart = new VBox(10);
        rightPart.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(rightPart, Priority.ALWAYS);
        rightPart.setMaxWidth(Double.MAX_VALUE);

        Label nowLabel = new Label("NOW PLAYING");
        nowLabel.setStyle(String.format(
                "-fx-text-fill: %s; -fx-font-size: 10px; -fx-font-weight: bold;" +
                        "-fx-background-color: %s; -fx-background-radius: 4; -fx-padding: 2 6;",
                hex, hex + "33"));

        Label titleLbl = new Label(controller.getGlobalPlayingTrack().getTitle());
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        titleLbl.setMaxWidth(Double.MAX_VALUE);
        titleLbl.setTextOverrun(OverrunStyle.ELLIPSIS);

        Label artistLbl = new Label(controller.getGlobalPlayingTrack().getArtist());
        artistLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.75); -fx-font-size: 13px;");

        // Прогресс
        VBox progressArea = new VBox(5);
        progressArea.setMaxWidth(Double.MAX_VALUE);

        StackPane progressBg = new StackPane();
        progressBg.setMaxWidth(Double.MAX_VALUE);
        progressBg.setPrefHeight(3);
        progressBg.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 2;");
        progressBg.setAlignment(Pos.CENTER_LEFT);

        Region progressFill = new Region();
        progressFill.setPrefHeight(3);
        progressFill.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 2;" +
                        "-fx-effect: dropshadow(gaussian, %s, 20, 0.3, 0, 0);", hex, hex));

        progressFill.prefWidthProperty().bind(
                progressBg.widthProperty()
                        .multiply(controller.progressSlider.valueProperty())
                        .divide(Bindings.max(1.0, controller.progressSlider.maxProperty())));

        progressBg.getChildren().add(progressFill);
        progressArea.getChildren().add(progressBg);

        // Кнопки
        HBox controls = new HBox(15);
        controls.setAlignment(Pos.CENTER_LEFT);

        FontIcon prevBtn = new FontIcon("mdi-skip-previous");
        prevBtn.setIconSize(26);
        prevBtn.setIconColor(Color.web("rgba(255,255,255,0.8)"));
        prevBtn.setCursor(javafx.scene.Cursor.HAND);
        prevBtn.setOnMouseClicked(e -> {
            controller.onPreviousClick();
            PauseTransition delay = new PauseTransition(Duration.millis(150));
            delay.setOnFinished(ev -> updateNowPlayingWidget());
            delay.play();
        });

        FontIcon playBtn = new FontIcon(controller.isPlaying() ? "mdi-pause-circle" : "mdi-play-circle");
        playBtn.setIconSize(38);
        playBtn.setIconColor(Color.WHITE);
        playBtn.setCursor(javafx.scene.Cursor.HAND);
        playBtn.setEffect(new javafx.scene.effect.DropShadow(12, Color.web(hex + "AA")));
        playBtn.setOnMouseClicked(e -> {
            controller.onPlayClick();
            updateNowPlayingWidget();
        });

        FontIcon nextBtn = new FontIcon("mdi-skip-next");
        nextBtn.setIconSize(26);
        nextBtn.setIconColor(Color.web("rgba(255,255,255,0.8)"));
        nextBtn.setCursor(javafx.scene.Cursor.HAND);
        nextBtn.setOnMouseClicked(e -> {
            controller.onNextClick();
            PauseTransition delay = new PauseTransition(Duration.millis(150));
            delay.setOnFinished(ev -> updateNowPlayingWidget());
            delay.play();
        });

        for (FontIcon icon : new FontIcon[]{prevBtn, nextBtn}) {
            icon.setOnMouseEntered(e -> icon.setIconColor(Color.WHITE));
            icon.setOnMouseExited(e -> icon.setIconColor(Color.web("rgba(255,255,255,0.8)")));
        }

        controls.getChildren().addAll(prevBtn, playBtn, nextBtn);
        rightPart.getChildren().addAll(nowLabel, titleLbl, artistLbl, progressArea, controls);
        mainLayout.getChildren().addAll(coverContainer, rightPart);
        controller.nowPlayingContent.getChildren().add(mainLayout);
    }

    // --- АРТИСТЫ ---
    public void updateTopArtistsUI(int limit) {
        if (controller.topArtistsContainer == null) return;
        controller.topArtistsContainer.getChildren().clear();

        Map<String, Integer> artistCounts = new HashMap<>();
        for (Track t : controller.getTrackListData()) {
            String artist = t.getArtist();
            if (artist == null || artist.trim().isEmpty() || artist.equals("Unknown Artist")) continue;
            artistCounts.put(artist, artistCounts.getOrDefault(artist, 0) + 1);
        }

        if (artistCounts.isEmpty()) {
            controller.topArtistsContainer.getChildren().add(createEmptyState(
                    "mdi-headphones", "No artists yet", "Artists appear after adding tracks"));
            return;
        }

        List<Map.Entry<String, Integer>> sorted = new ArrayList<>(artistCounts.entrySet());
        sorted.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        int actual = Math.min(limit, sorted.size());
        for (int i = 0; i < actual; i++) {
            controller.topArtistsContainer.getChildren().add(
                    createArtistCard(sorted.get(i).getKey(), sorted.get(i).getValue()));
        }
    }

    public void updateHeavyRotationUI(int limit) {
        if (controller.heavyRotationContainer == null) return;
        controller.heavyRotationContainer.getChildren().clear();

        if (controller.getCurrentHeavyRotation().isEmpty()) {
            controller.heavyRotationContainer.getChildren().add(createEmptyState(
                    "mdi-rotate-3d", "Heavy Rotation is empty", "Start listening to fill this section"));
            return;
        }

        int actual = Math.min(limit, controller.getCurrentHeavyRotation().size());
        for (int i = 0; i < actual; i++) {
            controller.heavyRotationContainer.getChildren().add(
                    createRecentTrackCard(controller.getCurrentHeavyRotation().get(i)));
        }
    }

    public void refreshHomeCardsState() {
        if (controller.recentlyAddedContainer != null) {
            for (Node node : controller.recentlyAddedContainer.getChildren()) {
                if (node.hasProperties() && node.getProperties().containsKey("updateState")) {
                    ((Runnable) node.getProperties().get("updateState")).run();
                }
            }
        }
        if (controller.heavyRotationContainer != null) {
            for (Node node : controller.heavyRotationContainer.getChildren()) {
                if (node.hasProperties() && node.getProperties().containsKey("updateState")) {
                    ((Runnable) node.getProperties().get("updateState")).run();
                }
            }
        }
    }

    // --- КАРТОЧКИ ---
    private VBox createDashboardPlaylistCard(String pName, File profileFolder) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(15));
        card.setPrefWidth(140); card.setPrefHeight(170);
        card.getStyleClass().addAll("stat-card", "playlist-card");

        Image coverImg = getPlaylistCoverWithFallback(profileFolder, pName);
        Node coverNode;
        if (coverImg != null) {
            ImageView iv = new ImageView(coverImg);
            iv.setFitWidth(100); iv.setFitHeight(100);
            Rectangle clip = new Rectangle(100, 100);
            clip.setArcWidth(15); clip.setArcHeight(15);
            iv.setClip(clip);
            controller.setupObjectFitCover(iv);
            coverNode = iv;
        } else {
            Rectangle rect = new Rectangle(100, 100);
            rect.setArcWidth(15); rect.setArcHeight(15);
            rect.setFill(Color.web("#B388FF"));
            coverNode = rect;
        }

        HBox coverWrapper = new HBox(coverNode);
        coverWrapper.setAlignment(Pos.CENTER);

        VBox textLayout = new VBox(2);
        textLayout.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label(pName);
        nameLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 14px;");
        nameLbl.setMaxWidth(110);

        int count = PlaylistManager.getTracksFromPlaylist(profileFolder, pName).size();
        Label countLbl = new Label(count + " tracks");
        countLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        textLayout.getChildren().addAll(nameLbl, countLbl);

        card.getChildren().addAll(coverWrapper, textLayout);
        setupSmoothCardHover(card);
        card.setOnMouseClicked(e -> controller.navigateTo("PLAYLIST:" + pName, true));
        return card;
    }

    private VBox createAddPlaylistCard() {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new javafx.geometry.Insets(15));
        card.setPrefWidth(140); card.setPrefHeight(170);
        card.getStyleClass().addAll("stat-card", "playlist-card", "add-playlist-card");

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(100, 100); iconBox.setMinSize(100, 100); iconBox.setMaxSize(100, 100);
        iconBox.getStyleClass().add("add-playlist-icon-box");

        FontIcon plusIcon = new FontIcon("mdi-plus");
        plusIcon.setIconSize(40);
        plusIcon.getStyleClass().add("icon-gray");
        iconBox.getChildren().add(plusIcon);

        HBox iconWrapper = new HBox(iconBox);
        iconWrapper.setAlignment(Pos.CENTER);

        VBox textLayout = new VBox(2);
        textLayout.setAlignment(Pos.CENTER_LEFT);
        Label nameLbl = new Label("New Playlist");
        nameLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label descLbl = new Label("Create custom");
        descLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        textLayout.getChildren().addAll(nameLbl, descLbl);

        card.getChildren().addAll(iconWrapper, textLayout);
        setupSmoothCardHover(card);
        card.setOnMouseClicked(e -> controller.onAddPlaylistClick());
        return card;
    }

    private HBox createRecentTrackCard(Track track) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("recent-row");

        ImageView widgetCover = new ImageView();
        widgetCover.setFitWidth(50); widgetCover.setFitHeight(50);
        widgetCover.setSmooth(true);
        Rectangle clip = new Rectangle(50, 50);
        clip.setArcWidth(10); clip.setArcHeight(10);
        widgetCover.setClip(clip);
        controller.setupObjectFitCover(widgetCover);

        if (track.getCover() != null) widgetCover.setImage(track.getCover());
        else {
            var res = controller.getClass().getResource("default_cover.png");
            if (res != null) widgetCover.setImage(new Image(res.toExternalForm(), 50, 50, true, true));
        }

        VBox textContainer = new VBox(2);
        textContainer.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(track.getTitle());
        title.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 14px;");
        Label artist = new Label(track.getArtist());
        artist.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        textContainer.getChildren().addAll(title, artist);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        StackPane actionContainer = new StackPane();
        actionContainer.setPrefSize(40, 40);

        FontIcon playIndicator = new FontIcon("mdi-play-circle");
        playIndicator.setIconSize(36);
        playIndicator.getStyleClass().add("icon-accent");

        HBox eqBox = createAnimatedEqualizer();

        actionContainer.getChildren().addAll(playIndicator, eqBox);

        Runnable updateState = () -> {
            boolean isThis = controller.getGlobalPlayingTrack() != null
                    && controller.getGlobalPlayingTrack().equals(track);
            Timeline eqAnim = (Timeline) eqBox.getProperties().get("animTimeline");
            if (isThis && controller.isPlaying()) {
                playIndicator.setVisible(false);
                eqBox.setVisible(true);
                if (eqAnim != null) eqAnim.play();
            } else if (isThis) {
                playIndicator.setVisible(true);
                eqBox.setVisible(false);
                if (eqAnim != null) eqAnim.pause();
            } else {
                playIndicator.setVisible(true);
                eqBox.setVisible(false);
                playIndicator.setOpacity(row.isHover() ? 1.0 : 0.0);
                if (eqAnim != null) eqAnim.stop();
            }
        };

        row.getProperties().put("updateState", updateState);
        row.setOnMouseEntered(e -> {
            if (!(controller.getGlobalPlayingTrack() != null
                    && controller.getGlobalPlayingTrack().equals(track) && controller.isPlaying()))
                playIndicator.setOpacity(1.0);
        });
        row.setOnMouseExited(e -> {
            if (!(controller.getGlobalPlayingTrack() != null
                    && controller.getGlobalPlayingTrack().equals(track) && controller.isPlaying()))
                playIndicator.setOpacity(0.0);
        });

        updateState.run();

        row.setOnMouseClicked(e -> {
            if (controller.getGlobalPlayingTrack() != null && controller.getGlobalPlayingTrack().equals(track))
                controller.onPlayClick();
            else
                controller.startPlayback(track);
        });

        row.getChildren().addAll(widgetCover, textContainer, spacer, actionContainer);
        return row;
    }

    public VBox createRecentTrackCardHorizontal(Track track) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(150); card.setMaxWidth(150);
        card.getStyleClass().add("stat-card");
        card.setStyle("-fx-padding: 12; -fx-background-radius: 16; -fx-cursor: hand;");

        StackPane coverStack = new StackPane();
        coverStack.setPrefSize(126, 126);

        ImageView cover = new ImageView();
        cover.setFitWidth(126); cover.setFitHeight(126);
        cover.setSmooth(true);
        Rectangle clip = new Rectangle(126, 126);
        clip.setArcWidth(12); clip.setArcHeight(12);
        cover.setClip(clip);
        controller.setupObjectFitCover(cover);

        if (track.getCover() != null) cover.setImage(track.getCover());
        else {
            var res = controller.getClass().getResource("default_cover.png");
            if (res != null) cover.setImage(new Image(res.toExternalForm(), 126, 126, true, true));
        }

        FontIcon playIcon2 = new FontIcon("mdi-play-circle");
        playIcon2.setIconSize(40);
        playIcon2.getStyleClass().add("icon-light");
        playIcon2.setOpacity(0);

        HBox eq = createAnimatedEqualizer();
        eq.setAlignment(Pos.BOTTOM_CENTER);
        coverStack.getChildren().addAll(cover, playIcon2, eq);

        Runnable updateState = () -> {
            boolean isThis = controller.getGlobalPlayingTrack() != null
                    && controller.getGlobalPlayingTrack().equals(track);
            Timeline eqAnim = (Timeline) eq.getProperties().get("animTimeline");
            if (isThis && controller.isPlaying()) {
                playIcon2.setOpacity(0);
                eq.setVisible(true);
                if (eqAnim != null) eqAnim.play();
            } else if (isThis) {
                playIcon2.setOpacity(card.isHover() ? 1.0 : 0);
                eq.setVisible(false);
                if (eqAnim != null) eqAnim.pause();
            } else {
                playIcon2.setOpacity(card.isHover() ? 1.0 : 0);
                eq.setVisible(false);
                if (eqAnim != null) eqAnim.stop();
            }
        };

        card.getProperties().put("updateState", updateState);
        card.setOnMouseEntered(e -> {
            playIcon2.setOpacity(1.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.03); st.setToY(1.03); st.play();
        });
        card.setOnMouseExited(e -> {
            updateState.run();
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
        card.setOnMouseClicked(e -> {
            if (controller.getGlobalPlayingTrack() != null && controller.getGlobalPlayingTrack().equals(track))
                controller.onPlayClick();
            else
                controller.startPlayback(track);
        });

        Label titleLbl = new Label(track.getTitle());
        titleLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 13px;");
        titleLbl.setMaxWidth(126);
        titleLbl.setTextOverrun(OverrunStyle.ELLIPSIS);

        Label artistLbl = new Label(track.getArtist());
        artistLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 11px;");
        artistLbl.setMaxWidth(126);
        artistLbl.setTextOverrun(OverrunStyle.ELLIPSIS);

        card.getChildren().addAll(coverStack, titleLbl, artistLbl);
        updateState.run();
        return card;
    }

    private VBox createArtistCard(String artistName, int trackCount) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(130); card.setPrefHeight(130);
        card.getStyleClass().add("artist-card");

        FontIcon icon = new FontIcon("mdi-headphones");
        icon.setIconSize(40);
        icon.getStyleClass().add("icon-accent");

        Label name = new Label(artistName);
        name.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 13px;");
        name.setMaxWidth(100);
        name.setAlignment(Pos.CENTER);

        Label countLbl = new Label(trackCount + " tracks");
        countLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 11px;");

        card.getChildren().addAll(icon, name, countLbl);
        card.setOnMouseClicked(e -> {
            controller.navigateTo("Library", true);
            controller.searchField.setText(artistName);
        });
        return card;
    }

    // --- ВИДЖЕТЫ ---
    private VBox createWeeklyActivityWidget() {
        VBox widget = new VBox();
        widget.getStyleClass().add("stat-card");
        widget.setStyle("-fx-padding: 15 20; -fx-background-radius: 12;");

        HBox content = new HBox(20);
        content.setAlignment(Pos.CENTER_LEFT);

        VBox textBox = new VBox(3);
        textBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label("Weekly Pulse");
        titleLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label subtitleLbl = new Label("Your 7-day streak");
        subtitleLbl.setStyle("-fx-text-fill: -accent-purple; -fx-font-size: 12px; -fx-font-weight: bold;");
        textBox.getChildren().addAll(titleLbl, subtitleLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox barsBox = new HBox(8);
        barsBox.setAlignment(Pos.BOTTOM_CENTER);
        barsBox.setPrefHeight(35);

        Map<String, Long> dailyStats = StatsManager.getInstance().getDailyListeningStats();
        LocalDate today = LocalDate.now();
        long maxSeconds = 1;
        long[] weekData = new long[7];
        for (int i = 6; i >= 0; i--) {
            long sec = dailyStats.getOrDefault(today.minusDays(i).toString(), 0L);
            weekData[6 - i] = sec;
            if (sec > maxSeconds) maxSeconds = sec;
        }

        for (int i = 0; i < 7; i++) {
            Region bar = new Region();
            double height = (double) weekData[i] / maxSeconds * 30.0;
            if (height < 4) height = 4;
            bar.setPrefSize(10, height); bar.setMinSize(10, height); bar.setMaxSize(10, height);
            bar.setStyle(i == 6
                    ? "-fx-background-color: -accent-purple; -fx-background-radius: 4;"
                    : "-fx-background-color: -hover-bg; -fx-background-radius: 4;");
            barsBox.getChildren().add(bar);
        }

        content.getChildren().addAll(textBox, spacer, barsBox);
        widget.getChildren().add(content);
        setupSmoothCardHover(widget);
        return widget;
    }

    private VBox createDailyGoalWidget() {
        VBox widget = new VBox();
        widget.getStyleClass().add("stat-card");
        widget.setStyle("-fx-padding: 15 20; -fx-background-radius: 12;");

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40); iconBox.setMinSize(40, 40); iconBox.setMaxSize(40, 40);
        iconBox.setStyle("-fx-background-color: -accent-dim; -fx-background-radius: 10;");
        FontIcon targetIcon = new FontIcon("mdi-bullseye");
        targetIcon.setIconSize(24);
        targetIcon.getStyleClass().add("icon-accent");
        iconBox.getChildren().add(targetIcon);

        Map<String, Long> dailyStats = StatsManager.getInstance().getDailyListeningStats();
        long todaySeconds = dailyStats.getOrDefault(LocalDate.now().toString(), 0L);
        long goalSeconds = 7200;
        double progress = Math.min(1.0, (double) todaySeconds / goalSeconds);
        int todayMins = (int) (todaySeconds / 60);

        VBox textBox = new VBox(2);
        Label titleLbl = new Label("Daily Goal");
        titleLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label subLbl = new Label(todayMins + "m / 120m");
        subLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        textBox.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double totalBarWidth = 100.0;
        double fillWidth = totalBarWidth * progress;

        StackPane barBg = new StackPane();
        barBg.setPrefSize(totalBarWidth, 8); barBg.setMaxSize(totalBarWidth, 8);
        barBg.setStyle("-fx-background-color: -hover-bg; -fx-background-radius: 4;");
        barBg.setAlignment(Pos.CENTER_LEFT);

        Region barFill = new Region();
        barFill.setPrefHeight(8);
        barFill.setMinWidth(fillWidth); barFill.setMaxWidth(fillWidth);

        if (progress >= 1.0) {
            barFill.setStyle("-fx-background-color: -accent-purple; -fx-background-radius: 4;" +
                    "-fx-effect: dropshadow(three-pass-box, -accent-purple, 10, 0.4, 0, 0);");
            subLbl.setText("Goal reached!");
            subLbl.setStyle("-fx-text-fill: -accent-purple; -fx-font-size: 12px; -fx-font-weight: bold;");
        } else {
            barFill.setStyle("-fx-background-color: -accent-purple; -fx-background-radius: 4;");
        }

        barBg.getChildren().add(barFill);
        content.getChildren().addAll(iconBox, textBox, spacer, barBg);
        widget.getChildren().add(content);
        setupSmoothCardHover(widget);
        return widget;
    }

    private VBox createListeningStreakWidget() {
        VBox widget = new VBox();
        widget.getStyleClass().add("stat-card");
        widget.setStyle("-fx-padding: 15 20; -fx-background-radius: 12;");

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40);
        iconBox.setStyle("-fx-background-color: rgba(255, 145, 0, 0.15); -fx-background-radius: 10;");

        ImageView flameView = new ImageView();
        try {
            flameView.setImage(new Image(controller.getClass().getResourceAsStream("Icons/flame.png")));
        } catch (Exception e) {
            System.err.println("Не удалось загрузить flame.png");
        }
        flameView.setFitWidth(32); flameView.setFitHeight(32); flameView.setSmooth(true);

        javafx.scene.effect.ColorInput colorInput = new javafx.scene.effect.ColorInput(
                0, 0, 26, 26, Color.web("#ff9100"));
        javafx.scene.effect.Blend tint = new javafx.scene.effect.Blend(
                javafx.scene.effect.BlendMode.SRC_ATOP);
        tint.setTopInput(colorInput);
        flameView.setEffect(tint);
        iconBox.getChildren().add(flameView);

        int streak = StatsManager.getInstance().calculateStreak();
        VBox text = new VBox(2);
        Label streakLbl = new Label(streak + " Day Streak");
        streakLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label subLbl = new Label(streak > 0 ? "You're on fire!" : "Start your streak today!");
        subLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        text.getChildren().addAll(streakLbl, subLbl);

        content.getChildren().addAll(iconBox, text);
        widget.getChildren().add(content);
        setupSmoothCardHover(widget);
        return widget;
    }

    private VBox createPaletteExplorerWidget(Track track) {
        VBox widget = new VBox();
        widget.getStyleClass().add("stat-card");
        widget.setStyle("-fx-padding: 15 20; -fx-background-radius: 12;");

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(40, 40); iconBox.setMinSize(40, 40); iconBox.setMaxSize(40, 40);
        iconBox.setStyle("-fx-background-color: -accent-dim; -fx-background-radius: 10;");
        FontIcon magicIcon = new FontIcon("mdi-star");
        magicIcon.setIconSize(24);
        magicIcon.getStyleClass().add("icon-accent");
        iconBox.getChildren().add(magicIcon);

        VBox text = new VBox(2);
        Label titleLbl = new Label("Surprise Me");
        titleLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label subLbl = new Label("Play a random track");
        subLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        text.getChildren().addAll(titleLbl, subLbl);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        FontIcon playIndicator = new FontIcon("mdi-play-circle");
        playIndicator.setIconSize(36);
        playIndicator.getStyleClass().add("icon-accent");
        playIndicator.setOpacity(0.0);

        widget.setOnMouseEntered(e -> {
            playIndicator.setOpacity(1.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), widget);
            st.setToX(1.02); st.setToY(1.02); st.play();
        });
        widget.setOnMouseExited(e -> {
            playIndicator.setOpacity(0.0);
            ScaleTransition st = new ScaleTransition(Duration.millis(200), widget);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });

        content.getChildren().addAll(iconBox, text, spacer, playIndicator);
        widget.getChildren().add(content);

        widget.setOnMouseClicked(e -> {
            if (controller.getTrackListData().isEmpty()) return;
            RotateTransition rt = new RotateTransition(Duration.millis(400), iconBox);
            rt.setByAngle(360);
            rt.setInterpolator(Interpolator.EASE_BOTH);
            rt.play();
            int randomIdx = new Random().nextInt(controller.getTrackListData().size());
            controller.startPlayback(controller.getTrackListData().get(randomIdx));
        });

        return widget;
    }

    public VBox createMostPlayedWidget() {
        VBox widget = new VBox();
        widget.getStyleClass().add("stat-card");
        widget.setStyle("-fx-padding: 15 20; -fx-background-radius: 12;");
        setupSmoothCardHover(widget);

        Track topTrack = null;
        int topCount = 0;
        for (Track t : controller.getTrackListData()) {
            int count = StatsManager.getInstance().getPlayCount(t.getFile().getName());
            if (count > topCount) { topCount = count; topTrack = t; }
        }

        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);

        if (topTrack == null || topCount == 0) {
            StackPane iconBox = new StackPane();
            iconBox.setPrefSize(40, 40); iconBox.setMinSize(40, 40); iconBox.setMaxSize(40, 40);
            iconBox.setStyle("-fx-background-color: -accent-dim; -fx-background-radius: 10;");
            FontIcon icon = new FontIcon("mdi-trophy");
            icon.setIconSize(24);
            icon.getStyleClass().add("icon-accent");
            iconBox.getChildren().add(icon);

            VBox text = new VBox(2);
            Label title = new Label("Most Played");
            title.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 15px;");
            Label sub = new Label("Play some tracks first");
            sub.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
            text.getChildren().addAll(title, sub);

            content.getChildren().addAll(iconBox, text);
            widget.getChildren().add(content);
            return widget;
        }

        ImageView cover = new ImageView();
        cover.setFitWidth(40); cover.setFitHeight(40); cover.setSmooth(true);
        Rectangle clip = new Rectangle(40, 40);
        clip.setArcWidth(10); clip.setArcHeight(10);
        cover.setClip(clip);
        controller.setupObjectFitCover(cover);

        if (topTrack.getCover() != null) cover.setImage(topTrack.getCover());
        else {
            var res = controller.getClass().getResource("default_cover.png");
            if (res != null) cover.setImage(new Image(res.toExternalForm(), 40, 40, true, true));
        }
        cover.setEffect(new javafx.scene.effect.DropShadow(8, Color.web("#00000066")));

        VBox text = new VBox(2);
        HBox.setHgrow(text, Priority.SOMETIMES);
        Label titleTop = new Label("Most Played");
        titleTop.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label trackName = new Label(topTrack.getTitle());
        trackName.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 12px;");
        trackName.setMaxWidth(160);
        trackName.setTextOverrun(OverrunStyle.ELLIPSIS);
        text.getChildren().addAll(titleTop, trackName);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countBadge = new Label(topCount + "x");
        countBadge.setStyle(
                "-fx-background-color: -accent-dim; -fx-text-fill: -accent-purple;" +
                        "-fx-font-weight: bold; -fx-font-size: 13px;" +
                        "-fx-background-radius: 8; -fx-padding: 4 10;");

        countBadge.setMinWidth(Region.USE_PREF_SIZE);
        countBadge.setTextOverrun(OverrunStyle.CLIP);

        content.getChildren().addAll(cover, text, spacer, countBadge);
        widget.getChildren().add(content);

        final Track finalTrack = topTrack;
        widget.setOnMouseClicked(e -> controller.startPlayback(finalTrack));
        return widget;
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ---
    public HBox createEmptyState(String iconLiteral, String title, String subtitle) {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setStyle(
                "-fx-background-color: -tertiary-bg; -fx-background-radius: 12;" +
                        "-fx-padding: 20 25; -fx-opacity: 0.6;");
        container.setMaxWidth(Double.MAX_VALUE);

        StackPane iconBox = new StackPane();
        iconBox.setPrefSize(44, 44); iconBox.setMinSize(44, 44);
        iconBox.setStyle("-fx-background-color: -hover-bg; -fx-background-radius: 22;");
        FontIcon icon = new FontIcon(iconLiteral);
        icon.setIconSize(22);
        icon.getStyleClass().add("icon-dark");
        iconBox.getChildren().add(icon);

        VBox textBox = new VBox(3);
        textBox.setAlignment(Pos.CENTER_LEFT);
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: -text-gray; -fx-font-size: 14px; -fx-font-weight: bold;");
        Label subtitleLbl = new Label(subtitle);
        subtitleLbl.setStyle("-fx-text-fill: -text-dark-gray; -fx-font-size: 12px;");
        textBox.getChildren().addAll(titleLbl, subtitleLbl);

        container.getChildren().addAll(iconBox, textBox);
        return container;
    }

    public HBox createAnimatedEqualizer() {
        HBox eqBox = new HBox(4);
        eqBox.setAlignment(Pos.CENTER);
        eqBox.setPrefHeight(36);
        eqBox.setVisible(false);

        javafx.scene.shape.Rectangle r1 = new javafx.scene.shape.Rectangle(4, 12);
        javafx.scene.shape.Rectangle r2 = new javafx.scene.shape.Rectangle(4, 20);
        javafx.scene.shape.Rectangle r3 = new javafx.scene.shape.Rectangle(4, 10);
        javafx.scene.shape.Rectangle r4 = new javafx.scene.shape.Rectangle(4, 16);

        for (javafx.scene.shape.Rectangle r : new javafx.scene.shape.Rectangle[]{r1, r2, r3, r4}) {
            r.setArcWidth(4); r.setArcHeight(4);
            r.setStyle("-fx-fill: -accent-purple;");
            eqBox.getChildren().add(r);
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(r1.scaleYProperty(), 0.3),
                        new KeyValue(r2.scaleYProperty(), 1.0),
                        new KeyValue(r3.scaleYProperty(), 0.4),
                        new KeyValue(r4.scaleYProperty(), 0.8)),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(r1.scaleYProperty(), 1.0),
                        new KeyValue(r2.scaleYProperty(), 0.4),
                        new KeyValue(r3.scaleYProperty(), 1.0),
                        new KeyValue(r4.scaleYProperty(), 0.3)),
                new KeyFrame(Duration.millis(600),
                        new KeyValue(r1.scaleYProperty(), 0.5),
                        new KeyValue(r2.scaleYProperty(), 0.8),
                        new KeyValue(r3.scaleYProperty(), 0.5),
                        new KeyValue(r4.scaleYProperty(), 1.0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.setAutoReverse(true);
        eqBox.getProperties().put("animTimeline", timeline);
        return eqBox;
    }

    public Image getPlaylistCoverWithFallback(File profileFolder, String playlistName) {
        Image customCover = PlaylistManager.getPlaylistCover(profileFolder, playlistName);
        if (customCover != null) return customCover;

        List<String> tracks = PlaylistManager.getTracksFromPlaylist(profileFolder, playlistName);
        if (!tracks.isEmpty()) {
            String firstTrackFilename = tracks.get(0);
            for (Track t : controller.getTrackListData()) {
                if (t.getFile().getName().equals(firstTrackFilename) && t.getCover() != null)
                    return t.getCover();
            }
            String baseName = firstTrackFilename.replace(".mp3", "");
            File trackFile = new File(profileFolder, firstTrackFilename);
            Image trackCover = controller.libraryController.loadSmallCover(trackFile, baseName);
            if (trackCover != null) return trackCover;
        }
        return null;
    }

    public Color extractDominantColor(Image image) {
        if (image == null) return Color.web("#B388FF");

        int sampleSize = 20;
        double width = image.getWidth();
        double height = image.getHeight();
        if (width <= 0 || height <= 0) return Color.web("#B388FF");

        javafx.scene.image.PixelReader reader = image.getPixelReader();
        if (reader == null) return Color.web("#B388FF");

        double totalR = 0, totalG = 0, totalB = 0, count = 0;
        double fallbackR = 0, fallbackG = 0, fallbackB = 0, fallbackCount = 0;

        double stepX = width / sampleSize;
        double stepY = height / sampleSize;

        for (int y = 0; y < sampleSize; y++) {
            for (int x = 0; x < sampleSize; x++) {
                int px = (int) Math.min(x * stepX, width - 1);
                int py = (int) Math.min(y * stepY, height - 1);
                Color c = reader.getColor(px, py);

                fallbackR += c.getRed(); fallbackG += c.getGreen();
                fallbackB += c.getBlue(); fallbackCount++;

                if (c.getBrightness() < 0.15 || c.getBrightness() > 0.95) continue;
                if (c.getSaturation() < 0.2) continue;

                double weight = c.getSaturation() * c.getBrightness();
                totalR += c.getRed() * weight;
                totalG += c.getGreen() * weight;
                totalB += c.getBlue() * weight;
                count += weight;
            }
        }

        Color avg;
        if (count == 0 && fallbackCount > 0) {
            avg = Color.color(Math.min(fallbackR / fallbackCount, 1.0),
                    Math.min(fallbackG / fallbackCount, 1.0),
                    Math.min(fallbackB / fallbackCount, 1.0));
            return Color.hsb(avg.getHue(), avg.getSaturation(), Math.min(avg.getBrightness(), 0.45));
        } else if (count == 0) {
            return Color.web("#B388FF");
        }

        avg = Color.color(Math.min(totalR / count, 1.0),
                Math.min(totalG / count, 1.0),
                Math.min(totalB / count, 1.0));

        return Color.hsb(avg.getHue(),
                Math.min(avg.getSaturation() * 1.4, 1.0),
                Math.min(avg.getBrightness() * 1.1, 1.0));
    }

    private void setupSmoothCardHover(Node card) {
        card.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.02); st.setToY(1.02);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
        card.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setToX(1.0); st.setToY(1.0);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.play();
        });
    }

    private String getSmartGreeting(String profileName) {
        int hour = java.time.LocalTime.now().getHour();
        if (hour >= 5 && hour < 12) return "Good morning, " + profileName;
        if (hour >= 12 && hour < 17) return "Good afternoon, " + profileName;
        if (hour >= 17 && hour < 22) return "Good evening, " + profileName;
        return "Still awake, " + profileName + "?";
    }
}