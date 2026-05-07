package com.example.scrill;

import javafx.scene.image.Image;

public class Playlist {
    private String name;
    private int trackCount;
    private Image cover;

    public Playlist(String name, int trackCount, Image cover) {
        this.name = name;
        this.trackCount = trackCount;
        this.cover = cover;
    }

    public String getName() { return name; }
    public int getTrackCount() { return trackCount; }
    public Image getCover() { return cover; }
}