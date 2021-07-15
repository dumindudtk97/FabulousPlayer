package com.example.fabulousplayer.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fabulousplayer.R;
import com.example.fabulousplayer.VideoModel;
import com.example.fabulousplayer.activity.videFolder;
import com.example.fabulousplayer.VideoModel;
import com.example.fabulousplayer.activity.videFolder;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.ArrayList;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.MyViewHolder> {


    private ArrayList<String> folderName;
    private ArrayList<VideoModel> videoModels;
    private Context context;

    public FolderAdapter(ArrayList<String> folderName, ArrayList<VideoModel> videoModels, Context context) {
        this.folderName = folderName;
        this.videoModels = videoModels;
        this.context = context;
    }

    public static long getAllFileSize(File file) {

        long size = 0;  // Store total size of all files

        if (file.isDirectory()) {
            File[] files = file.listFiles();    // All files and subdirectories
            for (int i = 0; files != null && i < files.length; i++) {
                size += getSize(files[i]);  // Recursive call
            }
        } else {
            size += file.length();
        }

        return size;
    }

    public static long getSize(File file) {
        long size = 0;  // Store total size of all files

        if (file.isDirectory()) {
            File[] files = file.listFiles();    // All files and subdirectories
            for (int i = 0; files != null && i < files.length; i++) {
                size += getSize(files[i]);  // Recursive call
            }
        } else {
            size += file.length();
        }

        return size;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.folder_view,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderAdapter.MyViewHolder holder, int position) {

        int index = folderName.get(position).lastIndexOf("/");
        String onlyFolderName = folderName.get(position).substring(index+1);

        holder.name.setText(onlyFolderName);

        String videoCount = String.valueOf(countVideos(folderName.get(position)));
        holder.countVideos.setText(videoCount);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, videFolder.class);
                intent.putExtra("folderName",folderName.get(position));
                //Toast.makeText(context, folderName.get(position), Toast.LENGTH_SHORT).show();
                context.startActivity(intent);
            }
        });
        holder.menu.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.file_menu,null);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
            bottomSheetView.findViewById(R.id.menu_down).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.findViewById(R.id.menu_share).setVisibility(View.GONE);
            bottomSheetView.findViewById(R.id.menu_rename).setVisibility(View.GONE);
            bottomSheetView.findViewById(R.id.menu_delete).setOnClickListener(v4 ->{
                bottomSheetDialog.dismiss();
                String path = folderName.get(position);
                deleteFiles(path,position);
            });
            bottomSheetView.findViewById(R.id.menu_properties).setOnClickListener(v5 ->{
                bottomSheetDialog.dismiss();
                showProperties(onlyFolderName, videoCount ,position);
            });

        });
    }

    @Override
    public int getItemCount() {
        return folderName.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView name,countVideos;
        ImageView menu;
        public MyViewHolder(@NonNull  View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.folderName);
            countVideos = itemView.findViewById(R.id.videosCount);
            menu = itemView.findViewById(R.id.folder_menu);
        }
    }




    private void deleteFiles(String path, int position) {
        boolean isExists = isDirectoryExits(path);
        if (isExists) {
            boolean isDeleted = deleteDirectory(path);
            if (isDeleted) {
                Toast.makeText(context, "deleted",
                        Toast.LENGTH_SHORT).show();
                folderName.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, folderName.size());
                deleteFromMediaDatabase(path);
            } else {
                Toast.makeText(context, "fail to delete", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "file already deleted",
                    Toast.LENGTH_SHORT).show();
            folderName.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, folderName.size());
        }
    }

    public boolean isDirectoryExits(String path) {
        return new File(path).exists();
    }

    private boolean deleteDirectory(String path) {
        return deleteDirectoryImp(path);
    }

    private boolean deleteDirectoryImp(String path) {
        File directory = new File(path);
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectoryImp(files[i].getAbsolutePath());
                    deleteFromMediaDatabase(files[i].getAbsolutePath());
                } else {
                    files[i].delete();
                    deleteFromMediaDatabase(files[i].getAbsolutePath());
                }
            }
        }
        return directory.delete();
    }

    private void deleteFromMediaDatabase(String path){
        try{
            MediaScannerConnection.scanFile(context, new String[]{path},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            context.getContentResolver().delete(uri, null, null);
                        }
                    });
        }catch (Exception e){
            e.getStackTrace();
        }
    }

    private void showProperties(String onlyFolderName, String videoCount, int position) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.file_properties);

        String path = folderName.get(position);

        TextView title = dialog.findViewById(R.id.pro_title);
        TextView location = dialog.findViewById(R.id.pro_storage);
        TextView folderSize = dialog.findViewById(R.id.pro_size);
        TextView videoCountName = dialog.findViewById(R.id.file_properties_videoCount);
        TextView count = dialog.findViewById(R.id.pro_duration);
        LinearLayout lastLayout = dialog.findViewById(R.id.file_properties_last_layout);
        lastLayout.setVisibility(View.GONE);

        title.setText(onlyFolderName);
        location.setText(path);
        videoCountName.setText("Video count: ");
        long s = getAllFileSize(new File(path));
        String converted = videFolder.fileReadableSize(s);
        folderSize.setText(converted);
        count.setText(videoCount);
        dialog.show();
        dialog.setCancelable(true);
    }



    int countVideos(String folders){
        int count = 0;
        for(VideoModel videoModel : videoModels){
            if(videoModel.getPath().substring(0,videoModel.getPath().lastIndexOf("/")).endsWith(folders)){
                count++;
            }
        }
        return count;
    }
}
