package com.example.scrill.controller;

import com.example.scrill.HelloController;

import java.util.Stack;

public class NavigationController {

    private final HelloController controller;

    // --- СОСТОЯНИЕ НАВИГАЦИИ ---
    public Stack<String> backHistory = new Stack<>();
    public Stack<String> forwardHistory = new Stack<>();
    public boolean isNavigating = false;

    public NavigationController(HelloController controller) {
        this.controller = controller;
    }

    // ==========================================
    // ПЕРЕКЛЮЧЕНИЕ ЭКРАНОВ
    // ==========================================

    public void navigateTo(String viewName, boolean saveToHistory) {
        if (!isNavigating && saveToHistory) {
            if (!controller.currentView.equals(viewName)) backHistory.push(controller.currentView);
            forwardHistory.clear();
        }
        controller.currentView = viewName;

        if (viewName.equalsIgnoreCase("Home")) {
            boolean wasInPlaylist = (controller.activePlaylist != null);
            controller.activePlaylist = null;
            controller.mainTitleLabel.setText("Welcome, " + controller.getCurrentProfileName());
            controller.mainSubtitleLabel.setText("What are we listening to today?");
            controller.libraryStatsLabel.setVisible(false);
            controller.mainHeaderCover.setVisible(false);
            controller.mainHeaderCover.setManaged(false);
            controller.searchContainer.setVisible(false);
            controller.searchContainer.setManaged(false);
            controller.addSongFAB.setVisible(false);
            controller.addSongFAB.setManaged(false);
            controller.trackTable.setVisible(false);
            controller.homeDashboardScroll.setVisible(true);

            if (wasInPlaylist) {
                controller.libraryController.loadMusicLibrary();
            }

            controller.dashboardBuilder.updateHomeDashboard();
        }
        else if (viewName.equals("Library")) {
            boolean wasInPlaylist = (controller.activePlaylist != null);
            controller.activePlaylist = null;
            controller.mainTitleLabel.setText("All My Music");
            controller.mainSubtitleLabel.setText("Manage and play your collection");
            controller.libraryStatsLabel.setVisible(true);
            controller.mainHeaderCover.setVisible(false);
            controller.mainHeaderCover.setManaged(false);
            controller.searchContainer.setVisible(true);
            controller.searchContainer.setManaged(true);
            controller.addSongFAB.setVisible(true);
            controller.addSongFAB.setManaged(true);
            controller.homeDashboardScroll.setVisible(false);
            controller.trackTable.setVisible(true);
            controller.libraryController.loadMusicLibrary();

            if (wasInPlaylist) {
                controller.libraryController.loadMusicLibrary();
            }
        }
        else if (viewName.startsWith("PLAYLIST:")) {
            String newPlaylist = viewName.split(":")[1];
            controller.activePlaylist = newPlaylist;
            controller.mainSubtitleLabel.setText("Your custom playlist");
            controller.libraryStatsLabel.setVisible(true);
            controller.searchContainer.setVisible(true);
            controller.searchContainer.setManaged(true);
            controller.addSongFAB.setVisible(true);
            controller.addSongFAB.setManaged(true);
            controller.homeDashboardScroll.setVisible(false);
            controller.trackTable.setVisible(true);
            controller.libraryController.loadMusicLibrary();
        }

        updateNavButtons();
        controller.sidebarController.updateSidebarStyles(viewName);
    }

    // ==========================================
    // НАЗАД / ВПЕРЁД
    // ==========================================

    public void onBackClick() {
        if (!backHistory.isEmpty()) {
            isNavigating = true;
            forwardHistory.push(controller.currentView);
            String previousView = backHistory.pop();
            navigateTo(previousView, false);
            isNavigating = false;
        }
    }

    public void onForwardClick() {
        if (!forwardHistory.isEmpty()) {
            isNavigating = true;
            backHistory.push(controller.currentView);
            String nextView = forwardHistory.pop();
            navigateTo(nextView, false);
            isNavigating = false;
        }
    }

    // ==========================================
    // НАВИГАЦИЯ ПО КНОПКАМ САЙДБАРА
    // ==========================================

    public void onHomeClick() {
        navigateTo("Home", true);
    }

    public void onLibraryClick() {
        navigateTo("Library", true);
    }

    // ==========================================
    // ОБНОВЛЕНИЕ КНОПОК НАВИГАЦИИ
    // ==========================================

    public void updateNavButtons() {
        if (controller.backIcon != null) {
            controller.backIcon.setDisable(backHistory.isEmpty());
        }
        if (controller.forwardIcon != null) {
            controller.forwardIcon.setDisable(forwardHistory.isEmpty());
        }
    }
}
