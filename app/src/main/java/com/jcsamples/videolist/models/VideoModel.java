package com.jcsamples.videolist.models;

import android.net.Uri;

public class VideoModel {
    private final Uri uri;
    private final String name, path;

    public VideoModel(Uri uri, String name, String path) {
        this.uri = uri;
        this.name = name;
        this.path = path;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}