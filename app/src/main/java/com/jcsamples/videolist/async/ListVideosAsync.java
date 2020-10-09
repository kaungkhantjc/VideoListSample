package com.jcsamples.videolist.async;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.RequiresApi;

import com.jcsamples.videolist.models.VideoModel;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class ListVideosAsync implements Callable<ListVideosAsync.ListVideosResult> {

    private ArrayList<VideoModel> videos = new ArrayList<>();
    private ListVideosResult result;
    private ContentResolver contentResolver;

    public static class ListVideosResult {
        public boolean success;
        public String error;
        public ArrayList<VideoModel> videos;
    }

    public ListVideosAsync(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
        result = new ListVideosResult();
    }

    @Override
    public ListVideosAsync.ListVideosResult call() {
        return doInBackground();
    }

    // This method is used to run on Main UI Thread
    @RequiresApi(api = 29)
    public ListVideosResult listVideosForAndroidQAndAbove() {
        String[] projection = new String[]{
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DISPLAY_NAME
        };

        String selection = MediaStore.Video.Media.DURATION + " >= ?";
        String[] selectionArgs = new String[] {String.valueOf(TimeUnit.MILLISECONDS.convert(2, TimeUnit.SECONDS))};
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection, selection, selectionArgs, sortOrder
        )) {

            int idColumn = Objects.requireNonNull(cursor).getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int nameColumn =
                    cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                Uri contentUri = ContentUris.withAppendedId(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
                videos.add(new VideoModel(contentUri, name, null));
            }

            result.success = true;
            result.videos = videos;
        }

        return result;
    }

    // This method is used to run in the background
    private ListVideosResult doInBackground() {
        try {
            File rootFolder = Environment.getExternalStorageDirectory();
            if (rootFolder.canRead()) {
                listVideosFrom(rootFolder);

                result.success = true;
                result.videos = videos;

            }
            result.error = "Cannot read External storage directory.";
        } catch (Exception e) {
            result.error = "Something went wrong.\n" + e.getMessage();
        }

        return result;
    }

    private void listVideosFrom(File rootFolder) {
        File[] files = rootFolder.listFiles();
        if (files == null)
            result.error = "cannot list movies from folder : " + rootFolder.getName();
        else {
            for (File file : files) {
                if (file.isDirectory()) {
                    listVideosFrom(file);
                } else {
                    addThisFileToArray(file);
                }
            }
        }
    }

    private void addThisFileToArray(File file) {
        String fileName = file.getName();
        if (isItVideo(fileName)) {
            VideoModel videoModel = new VideoModel(null, fileName, file.getAbsolutePath());
            videos.add(videoModel);
        }
    }

    private boolean isItVideo(String fileName) {
        String[] videoExtensions = {"mp4", "mov", "avi", "mkv"};
        for (String extension : videoExtensions) {
            if (fileName.toLowerCase().endsWith("." + extension)) return true;
        }
        return false;
    }
}