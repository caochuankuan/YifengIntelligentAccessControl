package com.ai.face.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.yifeng.face.R;

public class FaceListAdapter extends RecyclerView.Adapter<FaceListAdapter.ViewHolder> {

    private List<File> faceImageList;
    private Context context;

    public FaceListAdapter(List<File> faceImageList) {
        this.faceImageList = faceImageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_face_list, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        File faceImageFile = faceImageList.get(position);
        Glide.with(context)
                .load(faceImageFile)
                .into(holder.imageViewFace);

        // 设置时间
        long lastModified = faceImageFile.lastModified();
        String formattedTime = getFormattedTime(lastModified);
        holder.textViewTime.setText(formattedTime);
    }

    // 辅助方法：将时间戳格式化为字符串
    private String getFormattedTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date(timestamp);
        return sdf.format(date);
    }


    @Override
    public int getItemCount() {
        return faceImageList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFace;
        TextView textViewTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFace = itemView.findViewById(R.id.imageViewFace);
            textViewTime = itemView.findViewById(R.id.textViewTime);
        }
    }

    public void setFaceImageList(List<File> faceImageList) {
        this.faceImageList = faceImageList;
    }

}

