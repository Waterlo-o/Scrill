package com.example.scrill.ui;

import com.example.scrill.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.animation.*;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.util.List;

public class SidebarController {

    private final HelloController controller;

    public SidebarController(HelloController controller) {
        this.controller = controller;
    }

    // --- ЗАГРУЗКА ПЛЕЙЛИСТОВ В САЙДБАР ---
    public void loadPlaylistsUI() {
        controller.playlistsContainer.getChildren().clear();
        File profileFolder = controller.getCurrentMusicFolder();
        List<String> myPlaylists = PlaylistManager.getAllPlaylists(profileFolder);

        for (String pName : myPlaylists) {
            HBox pItem = new HBox(12);
            pItem.setAlignment(Pos.CENTER_LEFT);
            pItem.getStyleClass().add("sidebar-btn");
            pItem.setCursor(javafx.scene.Cursor.HAND);

            pItem.setOnMouseClicked(e -> {
                if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    controller.navigateTo("PLAYLIST:" + pName, true);

                    if (e.getClickCount() == 2 && !controller.trackListData.isEmpty()) {
                        Track firstTrack = controller.trackListData.get(0);
                        controller.playerController.currentTrackIndex = controller.musicFiles.indexOf(firstTrack.getFile());
                        controller.playerController.isPlaying = true;
                        controller.prepareTrack(firstTrack.getFile());
                        controller.updateUI(firstTrack);
                        controller.playIcon.setIconLiteral("mdi-pause-circle");
                        controller.playerController.smoothProgressTimer.start();
                        if (controller.playerController.coverPulse != null) controller.playerController.coverPulse.play();
                    }
                }
            });

            // Обложка
            Image coverImg = controller.dashboardBuilder.getPlaylistCoverWithFallback(profileFolder, pName);
            Node coverNode;
            int coverSize = 50;
            int coverRadius = 12;

            if (coverImg != null) {
                ImageView iv = new ImageView(coverImg);
                iv.setFitWidth(coverSize);
                iv.setFitHeight(coverSize);
                Rectangle clip = new Rectangle(coverSize, coverSize);
                clip.setArcWidth(coverRadius);
                clip.setArcHeight(coverRadius);
                iv.setClip(clip);
                controller.setupObjectFitCover(iv);
                coverNode = iv;
            } else {
                Rectangle rect = new Rectangle(coverSize, coverSize);
                rect.setArcWidth(coverRadius);
                rect.setArcHeight(coverRadius);
                rect.setFill(javafx.scene.paint.Color.web("#B388FF"));
                coverNode = rect;
            }

            // Текст
            VBox textBox = new VBox(2);
            textBox.setAlignment(Pos.CENTER_LEFT);
            Label nameLbl = new Label(pName);
            nameLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 14px;");

            int count = PlaylistManager.getTracksFromPlaylist(profileFolder, pName).size();
            Label countLbl = new Label(count + " tracks");
            countLbl.setStyle("-fx-text-fill: -text-dark-gray; -fx-font-size: 12px;");
            textBox.getChildren().addAll(nameLbl, countLbl);

            // Контекстное меню
            ContextMenu playlistMenu = new ContextMenu();
            playlistMenu.getStyleClass().add("context-menu");

            MenuItem editItem = new MenuItem("Edit Playlist");
            FontIcon editIcon = new FontIcon("mdi-pencil");
            editIcon.getStyleClass().add("icon-gray");
            editItem.setGraphic(editIcon);
            editItem.setOnAction(ev -> {
                controller.modalManager.editingPlaylistName = pName;
                controller.playlistModalTitle.setText("Edit Playlist");
                controller.savePlaylistBtn.setText("Save Changes");
                controller.playlistTabHeader.setVisible(false);
                controller.playlistTabHeader.setManaged(false);
                controller.modalManager.switchToPlaylistManual();

                controller.newPlaylistNameField.setText(pName);
                controller.newPlaylistNameField.getStyleClass().remove("input-error");

                Image cover = PlaylistManager.getPlaylistCover(profileFolder, pName);
                controller.newPlaylistCoverImage.setImage(cover);
                controller.modalManager.tempPlaylistCoverFile = null;

                controller.playlistModalOverlay.setVisible(true);
                FadeTransition ft = new FadeTransition(Duration.millis(300), controller.playlistModalOverlay);
                ft.setFromValue(0.0); ft.setToValue(1.0);
                ScaleTransition st = new ScaleTransition(Duration.millis(300), controller.playlistModalCard);
                st.setFromX(0.8); st.setFromY(0.8); st.setToX(1.0); st.setToY(1.0);
                ft.play(); st.play();
            });

            MenuItem deleteItem = new MenuItem("Delete Playlist");
            deleteItem.getStyleClass().add("delete-menu-item");
            deleteItem.setOnAction(ev -> {
                PlaylistManager.deletePlaylist(profileFolder, pName);
                if (("PLAYLIST:" + pName).equals(controller.getCurrentView())) {
                    controller.navigateTo("Home", true);
                }
                loadPlaylistsUI();
            });

            playlistMenu.getItems().addAll(editItem, new SeparatorMenuItem(), deleteItem);
            pItem.setOnContextMenuRequested(e ->
                    playlistMenu.show(pItem, e.getScreenX(), e.getScreenY()));

            pItem.getChildren().addAll(coverNode, textBox);
            controller.playlistsContainer.getChildren().add(pItem);

            toggleSidebarItem(pItem, controller.sidebar.getWidth() < 130);
        }

        updateSidebarStyles(controller.getCurrentView());
    }

    // --- АДАПТИВНЫЙ САЙДБАР ---
    public void setupAdaptiveSidebar() {
        controller.sidebar.widthProperty().addListener((obs, oldVal, newVal) -> {
            boolean isCompact = newVal.doubleValue() < 130;

            if (isCompact) {
                controller.sidebar.setPadding(new Insets(20.0, 15.0, 20.0, 15.0));
            } else {
                controller.sidebar.setPadding(new Insets(20.0, 15.0, 20.0, 27.0));
            }

            controller.playlistsHeaderLabel.setVisible(!isCompact);
            controller.playlistsHeaderLabel.setManaged(!isCompact);

            if (controller.addPlaylistBtn.getParent() instanceof HBox headerBox) {
                if (headerBox.getChildren().size() > 1
                        && headerBox.getChildren().get(1) instanceof Region spacer) {
                    spacer.setVisible(!isCompact);
                    spacer.setManaged(!isCompact);
                }
                headerBox.setAlignment(isCompact ? Pos.CENTER : Pos.CENTER_LEFT);
            }
            controller.addPlaylistBtn.setPickOnBounds(false);

            if (controller.navHomeBtn != null) toggleSidebarItem(controller.navHomeBtn, isCompact);
            if (controller.navLibraryBtn != null) toggleSidebarItem(controller.navLibraryBtn, isCompact);

            for (Node node : controller.playlistsContainer.getChildren()) {
                if (node instanceof HBox item) toggleSidebarItem(item, isCompact);
            }
        });
    }

    // --- ПОДСВЕТКА АКТИВНОГО ПУНКТА ---
    public void updateSidebarStyles(String activeView) {
        setSidebarItemActive(controller.navHomeBtn, false);
        setSidebarItemActive(controller.navLibraryBtn, false);

        if (activeView.equals("Home")) {
            setSidebarItemActive(controller.navHomeBtn, true);
        } else if (activeView.equals("Library")) {
            setSidebarItemActive(controller.navLibraryBtn, true);
        }

        String activePlaylistName = activeView.startsWith("PLAYLIST:")
                ? activeView.split(":")[1] : null;

        if (controller.playlistsContainer != null) {
            for (Node node : controller.playlistsContainer.getChildren()) {
                if (node instanceof HBox pItem && pItem.getChildren().size() > 1) {
                    VBox textBox = (VBox) pItem.getChildren().get(1);
                    Label nameLbl = (Label) textBox.getChildren().get(0);
                    String pName = nameLbl.getText();

                    if (pName.equals(activePlaylistName)) {
                        pItem.setStyle("-fx-background-color: -accent-dim; -fx-background-radius: 10;");
                        nameLbl.setStyle("-fx-text-fill: -accent-purple; -fx-font-weight: bold; -fx-font-size: 13px;");
                    } else {
                        pItem.setStyle("-fx-background-color: transparent;");
                        nameLbl.setStyle("-fx-text-fill: -text-white; -fx-font-weight: bold; -fx-font-size: 13px;");
                    }
                }
            }
        }
    }

    // --- ВСПОМОГАТЕЛЬНЫЕ ---
    public void toggleSidebarItem(HBox item, boolean isCompact) {
        if (item.getChildren().size() > 1) {
            Node textNode = item.getChildren().get(1);
            textNode.setVisible(!isCompact);
            textNode.setManaged(!isCompact);
            item.setAlignment(isCompact ? Pos.CENTER : Pos.CENTER_LEFT);
        }
    }

    private void setSidebarItemActive(HBox item, boolean isActive) {
        if (item == null || item.getChildren().size() < 2) return;

        FontIcon icon = (FontIcon) item.getChildren().get(0);
        Label label = (Label) item.getChildren().get(1);

        if (isActive) {
            icon.getStyleClass().removeAll("icon-gray", "icon-dark", "icon-light");
            if (!icon.getStyleClass().contains("icon-accent"))
                icon.getStyleClass().add("icon-accent");
            label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-white;");
        } else {
            icon.getStyleClass().removeAll("icon-accent", "icon-dark", "icon-light");
            if (!icon.getStyleClass().contains("icon-gray"))
                icon.getStyleClass().add("icon-gray");
            label.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: -text-gray;");
        }
    }
}