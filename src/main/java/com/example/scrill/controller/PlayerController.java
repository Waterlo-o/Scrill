package com.example.scrill.controller;

import com.example.scrill.*;
import com.example.scrill.util.StatsManager;
import com.example.scrill.util.ThemeManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerController {

    private final HelloController controller;

    // --- ПОЛЯ ---
    public MediaPlayer mediaPlayer;
    public boolean isPlaying = false;
    public boolean isShuffleMode = false;
    public boolean isRepeatMode = false;
    public int currentTrackIndex = 0;
    public List<File> shuffledFiles = new ArrayList<>();
    public AnimationTimer smoothProgressTimer;
    public ScaleTransition coverPulse;
    private Timeline marqueeTimeline;
    private double previousVolume = 0.5;
    public boolean isSliderDragging = false;
    private Node volumeTrackNode;
    private javafx.animation.PauseTransition marqueeDebouncer =
            new javafx.animation.PauseTransition(Duration.millis(200));

    public PlayerController(HelloController controller) {
        this.controller = controller;
    }

    // ==========================================
    // ИНИЦИАЛИЗАЦИЯ
    // ==========================================

    public void initialize() {
        setupAnimationTimer();
        setupCoverAnimation();
        marqueeDebouncer.setOnFinished(e -> startMarqueeAnimation());
    }

    private void setupAnimationTimer() {
        smoothProgressTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (mediaPlayer != null && isPlaying && !isSliderDragging) {
                    double current = mediaPlayer.getCurrentTime().toSeconds();
                    controller.progressSlider.setValue(current);
                    controller.currentTimeLabel.setText(
                            formatTime(mediaPlayer.getCurrentTime()));
                }
            }
        };
    }

    private void setupCoverAnimation() {
        coverPulse = new ScaleTransition(Duration.seconds(1.5), controller.coverImage);
        coverPulse.setFromX(1.0); coverPulse.setFromY(1.0);
        coverPulse.setToX(1.04); coverPulse.setToY(1.04);
        coverPulse.setCycleCount(Animation.INDEFINITE);
        coverPulse.setAutoReverse(true);
    }

    // ==========================================
    // ПОДГОТОВКА И ВОСПРОИЗВЕДЕНИЕ
    // ==========================================

    public void prepareTrack(File file) {
        controller.progressSlider.setValue(0);
        updateSliderGradient();

        if (marqueeTimeline != null) {
            marqueeTimeline.stop();
            marqueeTimeline = null;
        }
        controller.trackTitleLabel.setTranslateX(0);

        Track currentTrack = controller.libraryController.getTrackByFile(file);
        controller.globalPlayingTrack = currentTrack;

        int realTableIndex = controller.libraryController.trackListData.indexOf(currentTrack);
        if (realTableIndex >= 0) {
            controller.trackTable.getSelectionModel().select(realTableIndex);
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        updateUI(currentTrack);

        Platform.runLater(() ->
                controller.dashboardBuilder.updateNowPlayingWidget());


        controller.coverImage.setScaleX(1.0);
        controller.coverImage.setScaleY(1.0);

        try {
            mediaPlayer = new MediaPlayer(new Media(file.toURI().toString()));
            mediaPlayer.setVolume(Math.pow(controller.volumeSlider.getValue(), 2));

            mediaPlayer.setOnReady(() -> {
                controller.progressSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
                controller.totalTimeLabel.setText(formatTime(mediaPlayer.getTotalDuration()));
                controller.progressSlider.setValue(0);
                updateSliderGradient();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                // Считаем прослушивание
                StatsManager.getInstance().incrementPlayCount(file.getName());
                Platform.runLater(() -> {
                    if (controller.mostPlayedContainer != null) {
                        controller.mostPlayedContainer.getChildren().clear();
                        controller.mostPlayedContainer.getChildren().add(
                                controller.dashboardBuilder.createMostPlayedWidget());
                    }
                });

                if (isRepeatMode) {
                    mediaPlayer.seek(Duration.ZERO);
                    mediaPlayer.play();
                } else if (controller.settingsController.autoPlayNext) {
                    onNextClick();
                } else {
                    isPlaying = false;
                    controller.playIcon.setIconLiteral("mdi-play-circle");
                    if (coverPulse != null) coverPulse.pause();
                    if (smoothProgressTimer != null) smoothProgressTimer.stop();
                }
            });

            if (isPlaying) mediaPlayer.play();

        } catch (Exception e) {
            e.printStackTrace();
        }

        updateMarquee();

        FadeTransition flash = new FadeTransition(Duration.millis(300), controller.coverImage);
        flash.setFromValue(0.4); flash.setToValue(1.0);
        flash.play();

        controller.dashboardBuilder.refreshHomeCardsState();
    }

    public void startPlayback(Track track) {
        currentTrackIndex = controller.libraryController.musicFiles.indexOf(track.getFile());
        isPlaying = true;
        prepareTrack(track.getFile());
        smoothProgressTimer.start();
        if (coverPulse != null) coverPulse.play();
        if (controller.playIcon != null) controller.playIcon.setIconLiteral("mdi-pause-circle");
        controller.dashboardBuilder.refreshHomeCardsState();
    }

    // ==========================================
    // УПРАВЛЕНИЕ ВОСПРОИЗВЕДЕНИЕМ
    // ==========================================

    public void onPlayClick() {
        if (mediaPlayer == null) return;
        if (!isPlaying) {
            mediaPlayer.play();
            smoothProgressTimer.start();
            if (coverPulse != null) coverPulse.play();
            controller.playIcon.setIconLiteral("mdi-pause-circle");
            isPlaying = true;
        } else {
            mediaPlayer.pause();
            smoothProgressTimer.stop();
            if (coverPulse != null) coverPulse.pause();
            controller.playIcon.setIconLiteral("mdi-play-circle");
            isPlaying = false;
        }
        controller.dashboardBuilder.refreshHomeCardsState();
        if ("Home".equals(controller.getCurrentView())) {
            controller.dashboardBuilder.updateNowPlayingWidget();
        }

        Platform.runLater(() ->
                controller.dashboardBuilder.updateNowPlayingWidget());
    }

    public void onNextClick() {
        if (!controller.playQueue.isEmpty()) {
            Track next = controller.playQueue.remove(0);
            controller.updateQueueIndicator();
            controller.isQueueMode = true;

            int idx = controller.libraryController.musicFiles.indexOf(next.getFile());
            if (idx >= 0) currentTrackIndex = idx;

            isPlaying = true;
            prepareTrack(next.getFile());
            return;
        }

        controller.isQueueMode = false;
        if (controller.libraryController.musicFiles.isEmpty()) return;

        List<File> activeList = isShuffleMode
                ? shuffledFiles : controller.libraryController.musicFiles;
        currentTrackIndex++;
        if (currentTrackIndex >= activeList.size()) {
            currentTrackIndex = 0;
            if (isShuffleMode) Collections.shuffle(shuffledFiles);
        }
        prepareTrack(activeList.get(currentTrackIndex));
    }

    public void onPreviousClick() {
        if (mediaPlayer == null || controller.libraryController.musicFiles.isEmpty()) return;
        if (mediaPlayer.getCurrentTime().toSeconds() > 3.0) {
            mediaPlayer.seek(Duration.ZERO);
            return;
        }
        List<File> activeList = isShuffleMode
                ? shuffledFiles : controller.libraryController.musicFiles;
        currentTrackIndex--;
        if (currentTrackIndex < 0) currentTrackIndex = activeList.size() - 1;
        prepareTrack(activeList.get(currentTrackIndex));
    }

    public void onShuffleClick() {
        isShuffleMode = !isShuffleMode;
        if (isShuffleMode) {
            controller.shuffleIcon.getStyleClass().remove("icon-gray");
            if (!controller.shuffleIcon.getStyleClass().contains("icon-accent"))
                controller.shuffleIcon.getStyleClass().add("icon-accent");
            applyGlow(controller.shuffleIcon, true);

            shuffledFiles = new ArrayList<>(controller.libraryController.musicFiles);
            Collections.shuffle(shuffledFiles);
            if (mediaPlayer != null) {
                File currentFile = controller.libraryController.musicFiles.get(currentTrackIndex);
                currentTrackIndex = shuffledFiles.indexOf(currentFile);
            }
        } else {
            controller.shuffleIcon.getStyleClass().remove("icon-accent");
            if (!controller.shuffleIcon.getStyleClass().contains("icon-gray"))
                controller.shuffleIcon.getStyleClass().add("icon-gray");
            applyGlow(controller.shuffleIcon, false);

            if (mediaPlayer != null && !shuffledFiles.isEmpty()) {
                File currentFile = shuffledFiles.get(currentTrackIndex);
                currentTrackIndex = controller.libraryController.musicFiles.indexOf(currentFile);
            }
        }
    }

    public void onRepeatClick() {
        isRepeatMode = !isRepeatMode;
        if (isRepeatMode) {
            controller.repeatIcon.getStyleClass().remove("icon-gray");
            if (!controller.repeatIcon.getStyleClass().contains("icon-accent"))
                controller.repeatIcon.getStyleClass().add("icon-accent");
            applyGlow(controller.repeatIcon, true);
        } else {
            controller.repeatIcon.getStyleClass().remove("icon-accent");
            if (!controller.repeatIcon.getStyleClass().contains("icon-gray"))
                controller.repeatIcon.getStyleClass().add("icon-gray");
            applyGlow(controller.repeatIcon, false);
        }
    }

    public void onVolumeClick() {
        if (controller.volumeSlider.getValue() > 0) {
            previousVolume = controller.volumeSlider.getValue();
            controller.volumeSlider.setValue(0);
        } else {
            controller.volumeSlider.setValue(previousVolume > 0 ? previousVolume : 0.2);
        }
    }

    public void onSliderPressed() {
        isSliderDragging = true;
    }

    public void onSliderReleased() {
        if (mediaPlayer != null) {
            mediaPlayer.seek(Duration.seconds(controller.progressSlider.getValue()));
        }
        isSliderDragging = false;
    }

    public void onSliderClicked(javafx.scene.input.MouseEvent event) {
        if (mediaPlayer == null) return;
        double mouseX = event.getX();
        double width = controller.progressSlider.getWidth();
        double percentage = Math.max(0, Math.min(1, mouseX / width));
        double newValue = percentage * controller.progressSlider.getMax();
        controller.progressSlider.setValue(newValue);
        mediaPlayer.seek(Duration.seconds(newValue));
        updateSliderGradient();
    }

    public void seekForward() {
        if (mediaPlayer != null) {
            double current = mediaPlayer.getCurrentTime().toSeconds();
            double total = mediaPlayer.getTotalDuration().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.min(current + 5, total)));
        }
    }

    public void seekBackward() {
        if (mediaPlayer != null) {
            double current = mediaPlayer.getCurrentTime().toSeconds();
            mediaPlayer.seek(Duration.seconds(Math.max(current - 5, 0)));
        }
    }

    // ==========================================
    // UI ОБНОВЛЕНИЕ
    // ==========================================

    public void updateUI(Track track) {
        if (track == null) return;
        controller.trackTitleLabel.setText(track.getTitle());
        controller.artistNameLabel.setText(track.getArtist());

        if (track.getCover() != null) {
            controller.coverImage.setImage(track.getCover());
        } else {
            var res = controller.getClass().getResource("default_cover.png");
            if (res != null) controller.coverImage.setImage(
                    new javafx.scene.image.Image(res.toExternalForm(), 250, 250, true, true));
        }
        updateMarquee();
    }

    public void updateMarquee() {
        if (marqueeTimeline != null) {
            marqueeTimeline.stop();
            marqueeTimeline = null;
        }
        controller.trackTitleLabel.setTranslateX(0);
        marqueeDebouncer.playFromStart();
    }

    private void startMarqueeAnimation() {
        if (controller.trackTitleLabel.getText() == null
                || controller.trackTitleLabel.getText().isEmpty()) return;

        controller.trackTitleLabel.applyCss();
        controller.trackTitleLabel.getParent().layout();

        double textWidth = controller.trackTitleLabel.prefWidth(-1);
        double containerWidth = controller.titleViewport.getWidth();

        if (textWidth <= containerWidth + 2) {
            controller.trackTitleLabel.setTranslateX(0);
            return;
        }

        double scrollDistance = textWidth - containerWidth + 40;
        double duration = scrollDistance / 35.0;

        marqueeTimeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(controller.trackTitleLabel.translateXProperty(), 0)),
                new KeyFrame(Duration.seconds(2),
                        new KeyValue(controller.trackTitleLabel.translateXProperty(), 0)),
                new KeyFrame(Duration.seconds(2 + duration),
                        new KeyValue(controller.trackTitleLabel.translateXProperty(),
                                -scrollDistance, Interpolator.LINEAR)),
                new KeyFrame(Duration.seconds(2 + duration + 2),
                        new KeyValue(controller.trackTitleLabel.translateXProperty(),
                                -scrollDistance)),
                new KeyFrame(Duration.seconds(2 + duration + 2 + duration),
                        new KeyValue(controller.trackTitleLabel.translateXProperty(),
                                0, Interpolator.LINEAR))
        );
        marqueeTimeline.setCycleCount(Animation.INDEFINITE);
        marqueeTimeline.play();
    }

    // ==========================================
    // ГРАДИЕНТЫ СЛАЙДЕРОВ
    // ==========================================

    public void updateSliderGradient() {
        if (controller.progressSlider == null) return;
        Node track = controller.progressSlider.lookup(".track");
        if (track == null) return;

        double p = controller.progressSlider.getMax() > 0
                ? controller.progressSlider.getValue() / controller.progressSlider.getMax() : 0;

        String[] colors = ThemeManager.getInstance().getSliderColors();
        String activeColor = colors[0];
        String inactiveColor = colors[1];

        double trackWidth = track.getLayoutBounds().getWidth();
        if (trackWidth <= 0) trackWidth = controller.progressSlider.getWidth();
        double rightInset = Math.max(0, trackWidth * (1.0 - p));

        track.setStyle(String.format(
                "-fx-background-color: %s, %s; " +
                        "-fx-background-insets: 0, 0 %.1f 0 0; " +
                        "-fx-background-radius: 6, 6;",
                inactiveColor, activeColor, rightInset));
    }

    public void updateVolumeGradient() {
        if (controller.volumeSlider == null) return;
        if (volumeTrackNode == null) volumeTrackNode = controller.volumeSlider.lookup(".track");
        if (volumeTrackNode == null) return;

        double p = controller.volumeSlider.getValue();
        String[] colors = ThemeManager.getInstance().getSliderColors();
        String activeColor = colors[0];
        String inactiveColor = colors[1];

        double trackWidth = volumeTrackNode.getLayoutBounds().getWidth();
        if (trackWidth <= 0) trackWidth = controller.volumeSlider.getWidth();
        double rightInset = Math.max(0, trackWidth * (1.0 - p));

        volumeTrackNode.setStyle(String.format(
                "-fx-background-color: %s, %s; " +
                        "-fx-background-insets: 0, 0 %.1f 0 0; " +
                        "-fx-background-radius: 6, 6;",
                inactiveColor, activeColor, rightInset));
    }

    // ==========================================
    // ЭФФЕКТЫ КНОПОК
    // ==========================================

    public void setupMediaButtonEffects(Button btn) {
        btn.setCache(true);
        btn.setCacheHint(CacheHint.SCALE);

        btn.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(250), btn);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.setToX(1.18); st.setToY(1.18); st.play();
            if (btn.getGraphic() instanceof FontIcon icon)
                icon.setIconColor(javafx.scene.paint.Color.WHITE);
        });
        btn.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btn);
            st.setInterpolator(Interpolator.EASE_IN);
            st.setToX(1.0); st.setToY(1.0); st.play();
            if (btn.getGraphic() instanceof FontIcon icon)
                icon.setIconColor(javafx.scene.paint.Color.web("#E9E9E9"));
        });
        btn.setOnMousePressed(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(80), btn);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.setToX(0.9); st.setToY(0.9); st.play();
        });
        btn.setOnMouseReleased(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(120), btn);
            st.setInterpolator(Interpolator.EASE_OUT);
            st.setToX(1.18); st.setToY(1.18); st.play();
        });
    }

    private void applyGlow(FontIcon icon, boolean active) {
        if (icon == null) return;
        Node button = icon.getParent();
        if (button != null) {
            if (active) {
                button.setStyle(
                        "-fx-effect: dropshadow(three-pass-box, -accent-purple, 15, 0.4, 0, 0);");
            } else {
                button.setStyle("-fx-background-color: transparent; -fx-effect: null;");
            }
        }
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================

    public String formatTime(Duration d) {
        if (d == null) return "0:00";
        int m = (int) d.toMinutes();
        int s = (int) d.toSeconds() % 60;
        return String.format("%d:%02d", m, s);
    }

    public void stop() {
        if (smoothProgressTimer != null) smoothProgressTimer.stop();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        isPlaying = false;
    }
}