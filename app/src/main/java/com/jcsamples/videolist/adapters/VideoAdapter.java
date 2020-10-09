package com.jcsamples.videolist.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.jcsamples.videolist.R;
import com.jcsamples.videolist.models.VideoModel;

import java.util.ArrayList;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.PlaceHolder> {

    private Context context;
    private ArrayList<VideoModel> videos;
    private OnItemClickedListener onItemClickedListener;

    public VideoAdapter(Context context, ArrayList<VideoModel> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NonNull
    @Override
    public PlaceHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaceHolder(LayoutInflater.from(context).inflate(R.layout.item_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PlaceHolder holder, int position) {
        VideoModel model = videos.get(position);
        holder.textView.setText(model.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickedListener != null) {
                    VideoModel videoModel = videos.get(holder.getAdapterPosition());
                    onItemClickedListener.onItemClicked(videoModel.getUri(), videoModel.getPath());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public static class PlaceHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        public PlaceHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.item_video_textView);
        }
    }

    public interface OnItemClickedListener {
        void onItemClicked(Uri uri, String path);
    }

    public void setOnItemClickedListener(OnItemClickedListener onItemClickedListener) {
        this.onItemClickedListener = onItemClickedListener;
    }
}
