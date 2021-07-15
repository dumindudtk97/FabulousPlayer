package com.example.fabulousplayer;

public class VideoModel {
    String id;
    String path;
    String title;
    String size;
    String resolution;
    String duration;
    String displayName;
    String wh;

    public VideoModel(String id, String path, String title, String size, String resolution, String duration, String displayName, String wh) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.size = size;
        this.resolution = resolution;
        this.duration = duration;
        this.displayName = displayName;
        this.wh = wh;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolutin) {
        this.resolution = resolutin;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getWh() {
        return wh;
    }

    public void setWh(String wh) {
        this.wh = wh;
    }
}
