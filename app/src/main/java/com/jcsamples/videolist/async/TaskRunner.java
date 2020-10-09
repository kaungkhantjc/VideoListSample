package com.jcsamples.videolist.async;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class TaskRunner {
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final Handler handler = new Handler(Looper.getMainLooper());

    public interface Callback<R> {
        void onTaskCompleted(R result);
        void onTaskFailure(String error);
    }

    public <R> void executeAsync(final Callable<R> callable, final Callback<R> callback) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final R result;
                try {
                    result = callable.call();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onTaskCompleted(result);
                        }
                    });

                } catch (Exception e) {
                    callback.onTaskFailure(e.getMessage());
                }
            }
        });
    }

}