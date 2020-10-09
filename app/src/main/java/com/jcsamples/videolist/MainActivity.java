package com.jcsamples.videolist;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.jcsamples.videolist.adapters.VideoAdapter;
import com.jcsamples.videolist.async.ListVideosAsync;
import com.jcsamples.videolist.async.TaskRunner;
import com.jcsamples.videolist.models.VideoModel;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private static final int REQ_CODE_READ_EXTERNAL_PERMISSION = 123;
    private String READ_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.READ_EXTERNAL_STORAGE;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<VideoModel> videos = new ArrayList<>();
    private VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initApp();
    }

    private void initApp() {
        swipeRefreshLayout = findViewById(R.id.activityMainSwipe1);
        RecyclerView recyclerView = findViewById(R.id.activityMainRecyclerView1);

        adapter = new VideoAdapter(this, videos);
        recyclerView.setAdapter(adapter);
        setRecyclerOnItemClickListener();
        setSwipeOnRefreshListener();

        if (isReadExternalStoragePermissionGranted()) readVideos();
        else requestReadExternalStoragePermission();
    }

    private void setSwipeOnRefreshListener() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                readVideos();
            }
        });
    }

    private void setRecyclerOnItemClickListener() {
        adapter.setOnItemClickedListener(new VideoAdapter.OnItemClickedListener() {
            @Override
            public void onItemClicked(Uri uri, String path) {
                openVideo(uri, path);
            }
        });
    }

    private boolean isReadExternalStoragePermissionGranted() {
        return ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED;
    }

    private void startRefreshing() {
        swipeRefreshLayout.setRefreshing(true);
    }

    private void stopRefreshing() {
        swipeRefreshLayout.setRefreshing(false);
    }

    private boolean isAndroidQAndAbove() {
        return Build.VERSION.SDK_INT >= 29;
    }

    private void readVideos() {
        startRefreshing();

        ListVideosAsync async = new ListVideosAsync(getContentResolver());

        if (isAndroidQAndAbove()) handleResult(async.listVideosForAndroidQAndAbove());
        else
            new TaskRunner().executeAsync(async, new TaskRunner.Callback<ListVideosAsync.ListVideosResult>() {
                @Override
                public void onTaskCompleted(ListVideosAsync.ListVideosResult result) {
                    handleResult(result);
                }

                @Override
                public void onTaskFailure(String error) {
                    stopRefreshing();
                    showErrorDialog(error);
                }
            });
    }

    private void handleResult(ListVideosAsync.ListVideosResult result) {
        stopRefreshing();

        if (result.success) {
            addVideosToRecycler(result.videos);
        } else showErrorDialog(result.error);
    }

    private void addVideosToRecycler(ArrayList<VideoModel> videos) {
        this.videos.clear();
        this.videos.addAll(videos);
        adapter.notifyDataSetChanged();
    }

    private void showErrorDialog(String errorMessage) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(errorMessage)
                .setPositiveButton("OK", null)
                .show();
    }

    private void openVideo(Uri uri, String path) {
        // path is null when android version is Q or above
        if (path != null)
            uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", new File(path));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

    private void requestReadExternalStoragePermission() {
        ActivityCompat.requestPermissions(this, new String[]{READ_EXTERNAL_STORAGE_PERMISSION}, REQ_CODE_READ_EXTERNAL_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQ_CODE_READ_EXTERNAL_PERMISSION) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readVideos();
            } else {
                showPermissionDeniedInfoDialog();
            }
        }
    }

    private void showPermissionDeniedInfoDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Need Read Storage Permission")
                .create();

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE_PERMISSION)) {
            dialog.setMessage("App cannot read videos without READ EXTERNAL STORAGE PERMISSION. Please restart the app and Allow this permission.");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Restart App", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface p1, int p2) {
                    recreate();
                }
            });

        } else {
            dialog.setMessage("App cannot read videos without READ EXTERNAL STORAGE PERMISSION. You need to allow this permission manually in Settings because you choose \"Don't Ask Again\" option.");
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, "Open Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface p1, int p2) {
                    openSettings();
                }
            });
        }
        dialog.show();
    }

    private void openSettings() {
        Intent settingIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
        settingIntent.addCategory(Intent.CATEGORY_DEFAULT);
        settingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(settingIntent);
        finish();
    }

}
