package com.ai.face.ui;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.yifeng.face.R;

public class DisplayFacesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFaces;
    private FaceListAdapter faceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_faces);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerViewFaces = findViewById(R.id.recyclerViewFaces);
        faceListAdapter = new FaceListAdapter(getSavedFaceImages());
        recyclerViewFaces.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFaces.setAdapter(faceListAdapter);

        Button button = findViewById(R.id.btnDeleteAll);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DisplayFacesActivity.this, "已全部删除", Toast.LENGTH_SHORT).show();
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (storageDir != null) {
                    File[] files = storageDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            // 删除文件
                            file.delete();
                        }
                    }
                }
                // 刷新列表
                faceListAdapter.setFaceImageList(getSavedFaceImages());
                faceListAdapter.notifyDataSetChanged();
            }
        });



    }

    private List<File> getSavedFaceImages() {
        List<File> faceImageList = new ArrayList<>();

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null) {
            File[] files = storageDir.listFiles();
            if (files != null) {
                // 将文件数组转换为列表
                List<File> fileList = Arrays.asList(files);

                // 按时间戳降序排序文件列表（最新的排在前面）
                Collections.sort(fileList, (file1, file2) -> Long.compare(file2.lastModified(), file1.lastModified()));

                // 筛选出图片文件（你可能需要根据实际情况修改此检查条件）
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    faceImageList.addAll(fileList.stream().filter(file -> isImageFile(file)).collect(Collectors.toList()));
                }
            }
        }

        return faceImageList;
    }

    // 检查文件是否为图片文件（你可能需要根据实际情况修改此检查条件）
    private boolean isImageFile(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".png");
    }

}
