package com.example.fabulousplayer.adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.fabulousplayer.R;
import com.example.fabulousplayer.VideoModel;
import com.example.fabulousplayer.activity.VideoPlayer;
import com.example.fabulousplayer.activity.videFolder;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.MyHolder> {

    public static ArrayList<VideoModel> videoFolder = new ArrayList<>();
    private Context context;
    com.example.fabulousplayer.activity.videFolder videFolderActivity;

    public VideosAdapter(ArrayList<VideoModel> videoFolder, Context context) {
        this.videoFolder = videoFolder;
        this.context = context;
        videFolderActivity = (videFolder) context;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.files_view,parent,false);
        return new MyHolder(view,videFolderActivity);
    }

    @Override
    public void onBindViewHolder(@NonNull VideosAdapter.MyHolder holder, int position) {

        Glide.with(context).load(videoFolder.get(position).getPath()).into(holder.thumbnail);
        holder.title.setText(videoFolder.get(position).getTitle());
        holder.duration.setText(videoFolder.get(position).getDuration());
        holder.size.setText(videoFolder.get(position).getSize());
        holder.resolution.setText(videoFolder.get(position).getResolution());
        holder.menu.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(context,R.style.BottomSheetDialogTheme);
            View bottomSheetView = LayoutInflater.from(context).inflate(R.layout.file_menu,null);
            bottomSheetDialog.setContentView(bottomSheetView);
            bottomSheetDialog.show();
            bottomSheetView.findViewById(R.id.menu_down).setOnClickListener(v1 -> {
                bottomSheetDialog.dismiss();
            });
            bottomSheetView.findViewById(R.id.menu_share).setOnClickListener(v2 -> {
                bottomSheetDialog.dismiss();
                shareFile(position);
            });
            bottomSheetView.findViewById(R.id.menu_rename).setOnClickListener(v3 ->{
                bottomSheetDialog.dismiss();
                renameFiles(position,v);
            });
            bottomSheetView.findViewById(R.id.menu_delete).setOnClickListener(v4 ->{
                bottomSheetDialog.dismiss();
                deleteFiles(position,v);
            });
            bottomSheetView.findViewById(R.id.menu_properties).setOnClickListener(v5 ->{
                bottomSheetDialog.dismiss();
                showProperties(position);
            });

        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, VideoPlayer.class);
                intent.putExtra("p", position);
                context.startActivity(intent);
            }
        });
        if (videFolderActivity.is_selectable){
            holder.checkBox.setVisibility(View.VISIBLE);
            holder.menu.setVisibility(View.GONE);
            holder.checkBox.setChecked(false);
        }else{
            holder.checkBox.setVisibility(View.GONE);
            holder.menu.setVisibility(View.VISIBLE);
        }
    }

    private void shareFile(int p){
        Uri uri = Uri.parse(videoFolder.get(p).getPath());
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("video/*");
        intent.putExtra(Intent.EXTRA_STREAM,uri);
        context.startActivity(Intent.createChooser(intent,"share"));
        Toast.makeText(context, "loading...", Toast.LENGTH_SHORT).show();
    }

    private void deleteFiles(int p,View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("delete")
                .setMessage(videoFolder.get(p).getTitle())
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // don't do anything
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Uri contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                        Long.parseLong(videoFolder.get(p).getId()));
                File file = new File(videoFolder.get(p).getPath());
                boolean deleted = file.delete();
                if(deleted){
                    context.getApplicationContext().getContentResolver().delete(contentUri,null,null);
                    videoFolder.remove(p);
                    notifyItemRemoved(p);
                    notifyItemRangeChanged(p,videoFolder.size());
                    Snackbar.make(view,"File Deleted",Snackbar.LENGTH_SHORT).show();;
                }else{
                    Snackbar.make(view,"File Delete Failed",Snackbar.LENGTH_SHORT).show();;
                }
            }
        }).show();
    }

    private void renameFiles(int p, View view){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.rename_layout);
        final EditText editText = dialog.findViewById(R.id.rename_edit_text);
        Button cancel = dialog.findViewById(R.id.cancel_rename_button);
        Button rename_button = dialog.findViewById(R.id.rename_button);
        final  File renameFile = new File(videoFolder.get(p).getPath());
        String nameText = renameFile.getName();
        nameText = nameText.substring(0,nameText.lastIndexOf("."));
        editText.setText(nameText);
        editText.clearFocus();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        rename_button.setOnClickListener(v1-> {
            String onlyPath = renameFile.getParentFile().getAbsolutePath();
            String ext = renameFile.getAbsolutePath();
            ext = ext.substring(ext.lastIndexOf("."));
            String newPath = onlyPath + "/" + editText.getText() + ext;
            File newFile = new File(newPath);
            boolean rename = renameFile.renameTo(newFile);
            if (rename){
                context.getApplicationContext().getContentResolver().
                        delete(MediaStore.Files.getContentUri("external"),
                                MediaStore.MediaColumns.DATA + "=?",
                                new String[]{renameFile.getAbsolutePath()});
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                intent.setData(Uri.fromFile(newFile));
                context.getApplicationContext().sendBroadcast(intent);
                Snackbar.make(view,"File Renamed",Snackbar.LENGTH_SHORT).show();;
            } else {
                Snackbar.make(view,"Rename Failed",Snackbar.LENGTH_SHORT).show();;
            }
            dialog.dismiss();
        });
        dialog.show();
    }

    private void showProperties(int p){
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.file_properties);

        String name = videoFolder.get(p).getTitle();
        String path = videoFolder.get(p).getPath();
        String size = videoFolder.get(p).getSize();
        String resolution = videoFolder.get(p).getResolution();
        String duration = videoFolder.get(p).getDuration();

        TextView tit = dialog.findViewById(R.id.pro_title);
        TextView sr = dialog.findViewById(R.id.pro_storage);
        TextView siz = dialog.findViewById(R.id.pro_size);
        TextView dur = dialog.findViewById(R.id.pro_duration);
        TextView res = dialog.findViewById(R.id.pro_resolution);

        tit.setText(name);
        sr.setText(path);
        siz.setText(size);
        dur.setText(duration);
        res.setText(resolution);

        dialog.show();
    }

    @Override
    public int getItemCount() {
        return videoFolder.size();
    }

    public void updateSearchList(ArrayList<VideoModel> searchList) {
        videoFolder = new ArrayList<>();
        videoFolder.addAll(searchList);
        notifyDataSetChanged();
    }

    public class MyHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView thumbnail,menu;
        TextView title,size,duration,resolution;
        CheckBox checkBox;
        videFolder videFolderActivity;
        public MyHolder(@NonNull View itemView,videFolder videFolderActivity) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            title = itemView.findViewById(R.id.video_title);
            size = itemView.findViewById(R.id.video_size);
            duration = itemView.findViewById(R.id.video_duration);
            resolution = itemView.findViewById(R.id.video_quality);
            menu = itemView.findViewById(R.id.video_menu);
            checkBox = itemView.findViewById(R.id.video_folder_checkbox);
            this.videFolderActivity = videFolderActivity;

            itemView.setOnLongClickListener(videFolderActivity);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            videFolderActivity.prepareSelection(v,getAdapterPosition());
        }
    }
}
