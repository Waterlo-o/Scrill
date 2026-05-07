package com.example.scrill.controller;

import com.example.scrill.*;
import com.example.scrill.util.ThemeManager;
import javafx.animation.*;
import javafx.util.Duration;

import java.io.*;

public class SettingsController {

    private final HelloController controller;

    // --- ПОЛЯ ---
    public boolean enableAnimations = true;
    public boolean autoPlayNext = true;
    public String defaultStartScreen = "HOME";
    public int trackDisplaySize = 1;
    public String lastPlayedFileName = null;

    public SettingsController(HelloController controller) {
        this.controller = controller;
    }

    // ==========================================
    // МОДАЛЬНОЕ ОКНО НАСТРОЕК
    // ==========================================

    public void openSettingsModal() {
        controller.modalManager.toggleGlassmorphism(true);

        controller.animationsToggle.setSelected(enableAnimations);
        controller.autoPlayToggle.setSelected(autoPlayNext);
        controller.startScreenCombo.setValue(defaultStartScreen);
        controller.themeCombo.setValue(ThemeManager.getInstance().getCurrentBaseTheme());

        ThemeManager.getInstance().setTempDarkModeState(
                ThemeManager.getInstance().isDarkMode());
        ThemeManager.getInstance().animateToggle(
                ThemeManager.getInstance().isDarkMode());

        controller.trackSizeCombo.setValue(
                trackDisplaySize == 0 ? "Compact" :
                        trackDisplaySize == 2 ? "Large" : "Default");

        controller.settingsModalOverlay.setVisible(true);
        controller.settingsModalOverlay.setOpacity(0.0);

        FadeTransition ft = new FadeTransition(
                Duration.millis(300), controller.settingsModalOverlay);
        ft.setToValue(1.0);

        controller.settingsModalCard.setScaleX(0.8);
        controller.settingsModalCard.setScaleY(0.8);
        ScaleTransition st = new ScaleTransition(
                Duration.millis(300), controller.settingsModalCard);
        st.setToX(1.0); st.setToY(1.0);
        st.setInterpolator(Interpolator.EASE_OUT);

        ft.play(); st.play();
    }

    public void saveSettings() {
        enableAnimations = controller.animationsToggle.isSelected();
        autoPlayNext = controller.autoPlayToggle.isSelected();
        defaultStartScreen = controller.startScreenCombo.getValue();

        String selectedBase = controller.themeCombo.getValue();
        boolean selectedDark = ThemeManager.getInstance().getTempDarkModeState();

        String sizeVal = controller.trackSizeCombo.getValue();
        int newSize = sizeVal.equals("Compact") ? 0 : sizeVal.equals("Large") ? 2 : 1;

        if (newSize != trackDisplaySize) {
            trackDisplaySize = newSize;
            controller.libraryController.applyTrackDisplaySize();
            controller.trackTable.refresh();
        }

        if (!selectedBase.equals(ThemeManager.getInstance().getCurrentBaseTheme())
                || selectedDark != ThemeManager.getInstance().isDarkMode()) {
            ThemeManager.getInstance().setCurrentBaseTheme(selectedBase);
            ThemeManager.getInstance().setDarkMode(selectedDark);
            controller.applyTheme(selectedBase, selectedDark);
        }

        saveAppConfig();
        controller.libraryController.applyTrackDisplaySize();
        controller.trackTable.refresh();

        if (!enableAnimations && controller.playerController.coverPulse != null) {
            controller.playerController.coverPulse.pause();
            controller.coverImage.setScaleX(1.0);
            controller.coverImage.setScaleY(1.0);
        } else if (enableAnimations && controller.playerController.isPlaying
                && controller.playerController.coverPulse != null) {
            controller.playerController.coverPulse.play();
        }

        closeSettingsModal();
    }

    public void closeSettingsModal() {
        controller.modalManager.toggleGlassmorphism(false);

        FadeTransition ft = new FadeTransition(
                Duration.millis(200), controller.settingsModalOverlay);
        ft.setToValue(0.0);

        ScaleTransition st = new ScaleTransition(
                Duration.millis(200), controller.settingsModalCard);
        st.setToX(0.8); st.setToY(0.8);

        ft.setOnFinished(e -> controller.settingsModalOverlay.setVisible(false));

        ft.play(); st.play();
    }

    // ==========================================
    // КОНФИГ ФАЙЛ
    // ==========================================

    public void loadAppConfig() {
        File file = getSettingsFile();
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("animations="))
                    enableAnimations = Boolean.parseBoolean(line.split("=")[1]);
                if (line.startsWith("autoplay="))
                    autoPlayNext = Boolean.parseBoolean(line.split("=")[1]);
                if (line.startsWith("startscreen="))
                    defaultStartScreen = line.split("=")[1];
                if (line.startsWith("tracksize="))
                    trackDisplaySize = Integer.parseInt(line.split("=")[1]);
                if (line.startsWith("basetheme="))
                    ThemeManager.getInstance().setCurrentBaseTheme(line.split("=")[1]);
                if (line.startsWith("darkmode="))
                    ThemeManager.getInstance().setDarkMode(
                            Boolean.parseBoolean(line.split("=")[1]));
                if (line.startsWith("lasttrack=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length > 1) lastPlayedFileName = parts[1];
                }
                if (line.startsWith("currentprofile=")) {
                    String[] parts = line.split("=", 2);
                    if (parts.length > 1) {
                        String savedName = parts[1];
                        if (new File(controller.ROOT_MUSIC_DIR, savedName).exists()) {
                            controller.profileController.currentProfileName = savedName;
                        }
                    }
                }
            }
            // В конце метода добавь:
            //System.out.println("[Config] Загружен lasttrack: " + lastPlayedFileName);
            //System.out.println("[Config] Файл настроек: " + getSettingsFile().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveAppConfig() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(getSettingsFile()))) {
            bw.write("animations=" + enableAnimations + "\n");
            bw.write("autoplay=" + autoPlayNext + "\n");
            bw.write("startscreen=" + defaultStartScreen + "\n");
            bw.write("tracksize=" + trackDisplaySize + "\n");
            bw.write("basetheme=" + ThemeManager.getInstance().getCurrentBaseTheme() + "\n");
            bw.write("darkmode=" + ThemeManager.getInstance().isDarkMode() + "\n");

            if (controller.profileController.currentProfileName != null) {
                bw.write("currentprofile="
                        + controller.profileController.currentProfileName + "\n");
            }
            if (controller.globalPlayingTrack != null) {
                bw.write("lasttrack="
                        + controller.globalPlayingTrack.getFile().getName() + "\n");
                //System.out.println("[Config] Сохраняем lasttrack: " +
                        //controller.globalPlayingTrack.getFile().getName());
            }//else {
                //System.out.println("[Config] globalPlayingTrack = NULL, lasttrack не сохранён");
            //}

            //System.out.println("[Config] Файл настроек: " + getSettingsFile().getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getSettingsFile() {
        File dir = new File(System.getProperty("user.home")
                + "/AppData/Roaming/Scrill");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "scrill_settings.ini");
    }
}