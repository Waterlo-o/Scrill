package com.example.scrill;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;
import java.io.File;

public class Track {
    private int id;
    private String title;
    private String artist;
    private String dateAdded;
    private final StringProperty duration = new SimpleStringProperty("--:--"); // Property для автообновления
    private Image cover;
    private File file;

    public Track(int id, String title, String artist, String dateAdded, String duration, Image cover, File file) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.dateAdded = dateAdded;
        setDuration(duration);
        this.cover = cover;
        this.file = file;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getDateAdded() { return dateAdded; }

    // Методы для работы с длительностью через Property
    public String getDuration() { return duration.get(); }
    public void setDuration(String value) { this.duration.set(value); }
    public StringProperty durationProperty() { return duration; }

    public Image getCover() { return cover; }
    public File getFile() { return file; }
}