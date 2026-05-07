package com.example.scrill.controller;

import com.example.scrill.*;
import com.example.scrill.util.StatsManager;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.List;

public class ProfileController {

    private final HelloController controller;

    // --- ПОЛЯ ---
    public String currentProfileName = null;
    public File profileFolder = null;
    private File profileToDelete = null;
    private ContextMenu profileMenu;

    public ProfileController(HelloController controller) {
        this.controller = controller;
    }

    // ==========================================
    // ПРОФИЛЬ МЕНЮ
    // ==========================================

    public void onProfileBtnClick(javafx.scene.input.MouseEvent event) {
        if (profileMenu != null && profileMenu.isShowing()) {
            profileMenu.hide();
            return;
        }

        profileMenu = new ContextMenu();
        profileMenu.getStyleClass().add("context-menu");

        if (controller.rootPane.getStyleClass().contains("light-theme")) {
            profileMenu.getStyleClass().add("light-theme");
        }

        File rootDir = new File(controller.ROOT_MUSIC_DIR);
        if (!rootDir.exists()) rootDir.mkdirs();
        File[] profileDirs = rootDir.listFiles(File::isDirectory);

        if (profileDirs != null) {
            for (File dir : profileDirs) {
                String pName = dir.getName();

                HBox itemLayout = new HBox(15);
                itemLayout.setAlignment(Pos.CENTER_LEFT);
                itemLayout.setPrefWidth(180);

                HBox nameBox = new HBox(10);
                nameBox.setAlignment(Pos.CENTER_LEFT);
                nameBox.setCursor(javafx.scene.Cursor.HAND);

                FontIcon icon = new FontIcon(
                        pName.equals(currentProfileName) ? "mdi-account-check" : "mdi-account");
                icon.setIconSize(18);
                Label nameLabel = new Label(pName);

                if (pName.equals(currentProfileName)) {
                    nameLabel.getStyleClass().add("profile-label-active");
                    icon.getStyleClass().add("icon-light");
                } else {
                    nameLabel.getStyleClass().add("profile-label-inactive");
                    icon.getStyleClass().add("icon-gray");
                }

                nameBox.getChildren().addAll(icon, nameLabel);
                HBox.setHgrow(nameBox, Priority.ALWAYS);
                nameBox.setOnMouseClicked(e -> {
                    profileMenu.hide();
                    switchProfile(pName);
                });

                FontIcon deleteIcon = new FontIcon("mdi-close");
                deleteIcon.setIconSize(16);
                deleteIcon.setIconColor(javafx.scene.paint.Color.web("#706F8E"));

                StackPane deleteBtn = new StackPane(deleteIcon);
                deleteBtn.setPadding(new Insets(2, 5, 2, 5));
                deleteBtn.setCursor(javafx.scene.Cursor.HAND);
                deleteBtn.setOnMouseEntered(e ->
                        deleteIcon.setIconColor(javafx.scene.paint.Color.web("#FF5252")));
                deleteBtn.setOnMouseExited(e ->
                        deleteIcon.setIconColor(javafx.scene.paint.Color.web("#706F8E")));
                deleteBtn.setOnMouseClicked(e -> {
                    e.consume();
                    profileMenu.hide();
                    deleteProfile(dir);
                });

                itemLayout.getChildren().addAll(nameBox, deleteBtn);
                CustomMenuItem customMenuItem = new CustomMenuItem(itemLayout, false);
                profileMenu.getItems().add(customMenuItem);
            }
        }

        profileMenu.getItems().add(new SeparatorMenuItem());

        MenuItem createProfile = new MenuItem("Create Profile");
        FontIcon createIcon = new FontIcon("mdi-account-plus");
        createIcon.getStyleClass().add("icon-gray");
        createProfile.setGraphic(createIcon);
        createProfile.setOnAction(e -> openProfileModal());
        profileMenu.getItems().add(createProfile);

        profileMenu.show(controller.profileBtn, event.getScreenX() - 120, event.getScreenY() + 10);
    }

    // ==========================================
    // ПЕРЕКЛЮЧЕНИЕ ПРОФИЛЯ
    // ==========================================

    public void switchProfile(String profileName) {
        if (currentProfileName.equals(profileName)) return;

        StatsManager.getInstance().saveListeningStats();

        // Сброс состояния
        controller.globalPlayingTrack = null;
        StatsManager.getInstance().reset();
        controller.playQueue.clear();
        controller.isQueueMode = false;

        currentProfileName = profileName;
        controller.settingsController.saveAppConfig();

        StatsManager.getInstance().setProfileFolder(
                new File(controller.ROOT_MUSIC_DIR, profileName));
        StatsManager.getInstance().loadListeningStats();
        StatsManager.getInstance().loadPlayCounts();

        controller.libraryController.loadDurationCache();

        if (controller.profileNameLabel != null) {
            controller.profileNameLabel.setText(currentProfileName);
        }

        // Останавливаем плеер
        controller.playerController.stop();
        controller.playIcon.setIconLiteral("mdi-play-circle");
        if (controller.playerController.smoothProgressTimer != null)
            controller.playerController.smoothProgressTimer.stop();
        if (controller.playerController.coverPulse != null)
            controller.playerController.coverPulse.pause();

        // Сбрасываем нижнюю панель
        controller.trackTitleLabel.setText("No Track");
        controller.artistNameLabel.setText("-");
        controller.playerController.updateMarquee();
        controller.progressSlider.setValue(0);
        controller.playerController.updateSliderGradient();
        controller.currentTimeLabel.setText("0:00");
        controller.totalTimeLabel.setText("0:00");
        controller.coverImage.setImage(null);

        // Загружаем новый профиль
        controller.sidebarController.loadPlaylistsUI();

        controller.backHistory.clear();
        controller.forwardHistory.clear();

        controller.libraryController.loadMusicLibrary();
        controller.libraryController.calculateDurations();

        Platform.runLater(() -> {
            controller.navigateTo("Home", false);
            controller.updateListeningUI();
        });
    }

    // ==========================================
    // МОДАЛЬНОЕ ОКНО ПРОФИЛЯ
    // ==========================================

    public void openProfileModal() {
        controller.modalManager.toggleGlassmorphism(true);
        if (controller.profileModalOverlay.isVisible()) return;

        controller.profileModalOverlay.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), controller.profileModalOverlay);
        ft.setFromValue(0.0); ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), controller.profileModalCard);
        st.setFromX(0.8); st.setFromY(0.8); st.setToX(1.0); st.setToY(1.0);
        ft.play(); st.play();
    }

    public void closeProfileModal() {
        controller.modalManager.toggleGlassmorphism(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), controller.profileModalOverlay);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> controller.profileModalOverlay.setVisible(false));
        ft.play();
    }

    public void saveNewProfile() {
        String name = controller.newProfileNameField.getText().trim()
                .replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isEmpty()) {
            controller.newProfileNameField.getStyleClass().add("input-error");
            return;
        }

        File newProfileDir = new File(controller.ROOT_MUSIC_DIR, name);
        if (!newProfileDir.exists()) newProfileDir.mkdirs();

        controller.newProfileNameField.clear();
        closeProfileModal();
        switchProfile(name);
    }

    // ==========================================
    // УДАЛЕНИЕ ПРОФИЛЯ
    // ==========================================

    public void deleteProfile(File dir) {
        this.profileToDelete = dir;
        controller.deleteConfirmTitle.setText("Delete '" + dir.getName() + "'?");

        controller.modalManager.toggleGlassmorphism(true);
        controller.deleteConfirmOverlay.setVisible(true);

        FadeTransition ft = new FadeTransition(Duration.millis(300), controller.deleteConfirmOverlay);
        ft.setFromValue(0.0); ft.setToValue(1.0);
        ScaleTransition st = new ScaleTransition(Duration.millis(300), controller.deleteConfirmCard);
        st.setFromX(0.8); st.setFromY(0.8); st.setToX(1.0); st.setToY(1.0);
        ft.play(); st.play();
    }

    public void closeDeleteConfirmModal() {
        controller.modalManager.toggleGlassmorphism(false);
        FadeTransition ft = new FadeTransition(Duration.millis(200), controller.deleteConfirmOverlay);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            controller.deleteConfirmOverlay.setVisible(false);
            profileToDelete = null;
        });
        ft.play();
    }

    public void confirmDeleteProfile() {
        if (profileToDelete == null) return;

        String pName = profileToDelete.getName();
        deleteDirectoryRecursively(profileToDelete);

        FadeTransition ft = new FadeTransition(Duration.millis(200), controller.deleteConfirmOverlay);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            controller.deleteConfirmOverlay.setVisible(false);
            profileToDelete = null;

            if (pName.equals(currentProfileName)) {
                File rootDir = new File(controller.ROOT_MUSIC_DIR);
                File[] remaining = rootDir.listFiles(File::isDirectory);

                if (remaining != null && remaining.length > 0) {
                    String nextProfile = remaining[0].getName();

                    controller.playerController.stop();
                    controller.playIcon.setIconLiteral("mdi-play-circle");

                    controller.trackTitleLabel.setText("No Track");
                    controller.artistNameLabel.setText("-");
                    controller.playerController.updateMarquee();
                    controller.progressSlider.setValue(0);
                    controller.playerController.updateSliderGradient();
                    controller.currentTimeLabel.setText("0:00");
                    controller.totalTimeLabel.setText("0:00");
                    controller.coverImage.setImage(null);

                    currentProfileName = nextProfile;
                    profileFolder = new File(controller.ROOT_MUSIC_DIR, nextProfile);
                    controller.settingsController.saveAppConfig();

                    if (controller.profileNameLabel != null)
                        controller.profileNameLabel.setText(currentProfileName);

                    StatsManager.getInstance().setProfileFolder(profileFolder);
                    StatsManager.getInstance().loadListeningStats();
                    StatsManager.getInstance().loadPlayCounts();

                    controller.libraryController.loadDurationCache();
                    controller.sidebarController.loadPlaylistsUI();
                    controller.libraryController.loadMusicLibrary();
                    controller.libraryController.calculateDurations();

                    controller.backHistory.clear();
                    controller.forwardHistory.clear();
                    controller.modalManager.toggleGlassmorphism(false);
                    controller.navigateTo("Home", false);

                } else {
                    currentProfileName = null;
                    profileFolder = null;
                    if (controller.profileNameLabel != null)
                        controller.profileNameLabel.setText("");
                    controller.settingsController.saveAppConfig();
                    Platform.runLater(controller::startApplicationSequence);
                }

            } else {
                controller.modalManager.toggleGlassmorphism(false);
                controller.sidebarController.loadPlaylistsUI();
            }
        });
        ft.play();
    }

    // ==========================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================

    public File getCurrentMusicFolder() {
        String name = (currentProfileName != null) ? currentProfileName : "temp_guest";
        File folder = new File(controller.ROOT_MUSIC_DIR, name);
        if (currentProfileName != null && !folder.exists()) folder.mkdirs();
        return folder;
    }

    private void deleteDirectoryRecursively(File file) {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) deleteDirectoryRecursively(entry);
            }
        }
        file.delete();
    }
}