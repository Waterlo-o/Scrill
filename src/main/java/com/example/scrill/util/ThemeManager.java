package com.example.scrill.util;

import javafx.animation.FillTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ThemeManager {

    private static ThemeManager instance;
    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }
    private ThemeManager() {}

    // --- ПОЛЯ ---
    private String currentBaseTheme = "Default";
    private boolean isDarkMode = true;
    private boolean tempDarkModeState = true;
    private Rectangle toggleBg;
    private Circle toggleThumb;

    // --- ГЕТТЕРЫ/СЕТТЕРЫ ---
    public String getCurrentBaseTheme() { return currentBaseTheme; }
    public void setCurrentBaseTheme(String theme) { this.currentBaseTheme = theme; }
    public boolean isDarkMode() { return isDarkMode; }
    public void setDarkMode(boolean dark) { this.isDarkMode = dark; }
    public boolean getTempDarkModeState() { return tempDarkModeState; }
    public void setTempDarkModeState(boolean state) { this.tempDarkModeState = state; }
    public void setToggleControls(Rectangle bg, Circle thumb) {
        this.toggleBg = bg;
        this.toggleThumb = thumb;
    }

    // --- ЦВЕТА СЛАЙДЕРОВ ---
    public String[] getSliderColors() {
        String active, inactive;

        switch (currentBaseTheme) {
            case "Red":
                active = isDarkMode ? "#ff4a4a" : "#e53935";
                inactive = isDarkMode ? "#522626" : "#ffc2c2";
                break;
            case "Green":
                active = isDarkMode ? "#2ed15d" : "#43a047";
                inactive = isDarkMode ? "#2a5231" : "#c2ffcd";
                break;
            case "Cream":
                active = isDarkMode ? "#e0a96d" : "#c28851";
                inactive = isDarkMode ? "#523c26" : "#dfcdae";
                break;
            case "Pink":
                active = isDarkMode ? "#ff4d94" : "#ff4d94";
                inactive = isDarkMode ? "#4d2635" : "#ffc2d8";
                break;
            case "Nordic":
                active = isDarkMode ? "#88c0d0" : "#5e81ac";
                inactive = isDarkMode ? "#4c566a" : "#d8dee9";
                break;
            case "Penguin":
                active = isDarkMode ? "#ffffff" : "#000000";
                inactive = isDarkMode ? "#333333" : "#cccccc";
                break;
            default: // Default (Purple)
                active = isDarkMode ? "#B388FF" : "#7C4DFF";
                inactive = isDarkMode ? "#393A5A" : "#D1D1D6";
                break;
        }
        return new String[]{active, inactive};
    }

    // --- АКЦЕНТНЫЙ ЦВЕТ ТЕМЫ ---
    public String getAccentHex() {
        String[] colors = getSliderColors();
        return colors[0];
    }

    // --- АНИМАЦИЯ ТОГГЛА ---
    public void animateToggle(boolean isDark) {
        if (toggleBg == null || toggleThumb == null) return;

        String activeHex = getAccentHex();
        String inactiveHex = isDark ? "#393A5A" : "#D1D1D6";

        TranslateTransition tt = new TranslateTransition(Duration.millis(250), toggleThumb);
        tt.setToX(isDark ? 10 : -10);
        tt.setInterpolator(javafx.animation.Interpolator.EASE_BOTH);

        FillTransition ft = new FillTransition(Duration.millis(250), toggleBg);
        ft.setToValue(isDark ? Color.web(activeHex) : Color.web(inactiveHex));

        tt.play();
        ft.play();
    }

    // --- ЯРКОСТЬ ИКОНОК ОКНА ---
    public void updateImageBrightness(ImageView iv, double brightness) {
        if (iv == null) return;
        if (iv.getEffect() instanceof javafx.scene.effect.ColorAdjust) {
            ((javafx.scene.effect.ColorAdjust) iv.getEffect()).setBrightness(brightness);
        } else {
            javafx.scene.effect.ColorAdjust ca = new javafx.scene.effect.ColorAdjust();
            ca.setBrightness(brightness);
            iv.setEffect(ca);
        }
    }
}