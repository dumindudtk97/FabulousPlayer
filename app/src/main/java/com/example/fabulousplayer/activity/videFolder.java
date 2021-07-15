package com.example.fabulousplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentUris;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.widget.CheckBox;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fabulousplayer.R;
import com.example.fabulousplayer.VideoModel;
import com.example.fabulousplayer.adapter.VideosAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import static android.text.TextUtils.concat;

public class videFolder extends AppCompatActivity
        implements SearchView.OnQueryTextListener, View.OnLongClickListener {

    private static final String MY_SORT_PREF = "sortOrder";
    private RecyclerView recyclerView;
    private String name;
    private ArrayList<VideoModel> videoModelArrayList = new ArrayList<>();
    private VideosAdapter videosAdapter;
    Toolbar toolbar;
    public boolean is_selectable = false;
    TextView countText;
    ArrayList<VideoModel> selectionArrayList =new ArrayList<>();
    ArrayList<Uri> uris = new ArrayList<>();
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vide_folder);

        name =  getIntent().getStringExtra("folderName");
        recyclerView = findViewById(R.id.video_recyclerview);
        countText = findViewById(R.id.counter_textView);
        toolbar = findViewById(R.id.toolbar);


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()
                .setHomeAsUpIndicator(getResources().getDrawable(R.drawable.go_back));
        int index = name.lastIndexOf("/");
        String onlyFolderName = name.substring(index + 1);
        countText.setText(onlyFolderName);
        //Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
        loadVideos();

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        ImageView ivClose = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        ivClose.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white),
                android.graphics.PorterDuff.Mode.SRC_IN);
        searchView.setQueryHint("Search file name");
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String input = newText.toLowerCase();
        ArrayList<VideoModel> searchList = new ArrayList<>();
        for (VideoModel model : videoModelArrayList) {
            if (model.getTitle().toLowerCase().contains(input)) {
                searchList.add(model);
            }
        }
        videosAdapter.updateSearchList(searchList);
        return false;
    }

    private void loadVideos() {
        videoModelArrayList = getAllVideosFromFolder(this,name);

        if (name!=null && videoModelArrayList.size()>0){

            //Toast.makeText(this, name, Toast.LENGTH_SHORT).show();
            videosAdapter = new VideosAdapter(videoModelArrayList,this);
            recyclerView.setHasFixedSize(true);
            recyclerView.setItemViewCacheSize(20);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
            recyclerView.setNestedScrollingEnabled(false);

            recyclerView.setAdapter(videosAdapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this,RecyclerView.VERTICAL,false));
        }else{
            Toast.makeText(this, "no videos found", Toast.LENGTH_SHORT).show();
        }
    }

    private ArrayList<VideoModel> getAllVideosFromFolder(Context context, String name) {

        SharedPreferences preferences = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE);

        String sort = preferences.getString("sorting", "sortByDate");
        String order = null;
        switch (sort) {

            case "sortByDate":
                order = MediaStore.Video.Media.DATE_ADDED + " ASC";
                break;

            case "sortByName":
                order = MediaStore.MediaColumns.DISPLAY_NAME + " ASC";
                break;

            case "sortBySize":
                order = MediaStore.MediaColumns.SIZE + " DESC";
                break;

        }

        ArrayList<VideoModel> list = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        //String orderBy = MediaStore.Video.Media.DATE_ADDED + " DESC";

        String [] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.HEIGHT,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Video.Media.RESOLUTION
        };
        String selection = MediaStore.Video.Media.DATA + " like?";
        String[] selectionArgs = new String[]{"%"+name+"%"};

        Cursor cursor = context.getContentResolver().query(uri,projection,selection,selectionArgs,order);

        if(cursor!=null){
            while (cursor.moveToNext()){
                String id = cursor.getString(0);
                String path = cursor.getString(1);
                String title = cursor.getString(2);
                int size = cursor.getInt(3);
                String resolution = cursor.getString(4);
                resolution = resolution + "p";
                int duration = cursor.getInt(5);
                String disName = cursor.getString(6);
                String bucket_display_name = cursor.getString(7);
                String width_height = cursor.getString(8);

                // convert 1024 in 1MB
                String human_can_read = null;
                if(size<1024){
                    human_can_read = String.format(context.getString(R.string.size_in_b), (double) size );
                } else if(size < Math.pow(1024,2)){
                    human_can_read = String.format(context.getString(R.string.size_in_kb),(double) (size/1024));
                } else if(size < Math.pow(1024,3)){
                    human_can_read = String.format(context.getString(R.string.size_in_mb), (double) (size/Math.pow(1024,2)) );
                }else{
                    human_can_read = String.format(context.getString(R.string.size_in_gb), (double) (size/Math.pow(1024,3)) );
                }

                // convert duration
                String duration_Formatted;
                int sec = (duration /1000) %60 ;
                int min = (duration /(1000*60)) %60 ;
                int hrs = (duration /(1000*60*60));

                if (hrs == 0){
                    duration_Formatted = String.valueOf(min).concat(":".concat(String.format(Locale.UK,"%02d",sec)));
                }else{
                    duration_Formatted = String.valueOf(hrs).
                            concat(":".concat(String.format(Locale.UK,"%02d",min).
                                    concat(":".concat(String.format(Locale.UK,"%02d",sec)))));
                }

                VideoModel files = new VideoModel(id,path,title,human_can_read,resolution,duration_Formatted,disName,width_height);

                if (name.endsWith(bucket_display_name)){
                    list.add(files);
                }
                //Toast.makeText(context, human_can_read, Toast.LENGTH_SHORT).show();

            }cursor.close();
        }
        return list;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        SharedPreferences.Editor editor = getSharedPreferences(MY_SORT_PREF, MODE_PRIVATE).edit();

        switch (item.getItemId()) {

            case R.id.sort_by_date:
                editor.putString("sorting", "sortByDate");
                editor.apply();
                this.recreate();
                break;

            case R.id.sort_by_name:
                editor.putString("sorting", "sortByName");
                editor.apply();
                this.recreate();
                break;

            case R.id.sort_by_size:
                editor.putString("sorting", "sortBySize");
                editor.apply();
                this.recreate();
                break;
            case R.id.refresh:
                refresh();
                break;
            case android.R.id.home:
                if (is_selectable){
                    clearSelectedToolBar();
                    videosAdapter.notifyDataSetChanged();
                }else {
                    onBackPressed();
                }
                break;

            case R.id.share_selected:
                boolean isSuccess = selectedFile(selectionArrayList, false);
                if (isSuccess) {
                    clearSelectedToolBar();
                    refresh();
                    Toast.makeText(this, "Loading",
                            Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.delete_selected:
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isDeleted = selectedFile(selectionArrayList, true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isDeleted) {
                                    clearSelectedToolBar();
                                    refresh();
                                    Toast.makeText(videFolder.this, "Delete Success",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(videFolder.this, "Delete Fail",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
                thread.start();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean selectedFile(ArrayList<VideoModel> list, boolean canIDelete) {
        for (int i = 0; i < list.size(); i++) {
            String id = list.get(i).getId();
            String path = list.get(i).getPath();
            uris.add(Uri.parse(path));
            if (canIDelete) {
                Uri contentUris = ContentUris
                        .withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                                , Long.parseLong(id));
                File file = new File(path);
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    getApplicationContext().getContentResolver().delete(contentUris,
                            null, null);
                }
            }
        }

        if (!canIDelete) {
            MediaScannerConnection.scanFile(getApplicationContext(), new String[]{String.valueOf(uris)},
                    null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                            intent.setType("video/*");
                            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            startActivity(Intent.createChooser(intent, "share"));
                        }
                    });
        }

        return true;
    }

    public static String fileReadableSize(long size){
        String s = "";
        long kiloByte = 1024;
        long megabyte = kiloByte * kiloByte;
        long gigaByte = megabyte * megabyte;
        long terabyte = gigaByte* gigaByte;


        double kb = (double) size / kiloByte;
        double mb = kb / kiloByte;
        double gb = mb / kiloByte;
        double tb = gb / kiloByte;

        if (size < kiloByte){
            s = size + "bytes";
        } else if (size >= kiloByte && size < megabyte){
            s = String.format("%.2f",kb) + "KB";
        } else if (size >= megabyte && size < gigaByte){
            s = String.format("%.2f",mb) + "mb";
        } else if (size >= gigaByte && size < terabyte){
            s = String.format("%.2f",gb) + "gb";
        } else if (size >= terabyte){
            s = String.format("%.2f",tb) + "tb";
        }
        return s;
    }



    private void refresh(){
        if(name!= null && videoModelArrayList.size()>0){
            videoModelArrayList.clear();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    loadVideos();
                    videosAdapter.notifyDataSetChanged();
                    Toast.makeText(videFolder.this, "Refreshed", Toast.LENGTH_SHORT).show();
                }
            },1000);
        }else {
            Toast.makeText(this, "Folder is empty.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.item_selectd_menu);
        is_selectable = true;
        videosAdapter.notifyDataSetChanged();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()
                .setHomeAsUpIndicator(getResources().getDrawable(R.drawable.go_back));
        return true;
    }

    public void prepareSelection(View v, int position) {

        if ( ((CheckBox) v).isChecked() ){
            selectionArrayList.add(videoModelArrayList.get(position));
            count = count + 1;
            updateCount(count);
        } else {
            selectionArrayList.remove(videoModelArrayList.get(position));
            count = count - 1;
            updateCount(count);
        }
    }

    private void updateCount(int counts) {
        if(counts == 0){
            countText.setText("0 item selected");
        }else {
            countText.setText(counts + " items selected");
        }
    }

    private void clearSelectedToolBar(){
        is_selectable = false;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.main_toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()
                .setHomeAsUpIndicator(getResources().getDrawable(R.drawable.go_back));
        int index = name.lastIndexOf("/");
        String onlyFolderName = name.substring(index + 1);
        countText.setText(onlyFolderName);
        count = 0;
        selectionArrayList.clear();
    }

    @Override
    public void onBackPressed() {
        if (is_selectable){
            clearSelectedToolBar();
            videosAdapter.notifyDataSetChanged();
        }else {
            super.onBackPressed();
        }
    }
}