package com.hehe.mymusiccollection;

import static androidx.core.content.ContextCompat.getString;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.List;
import com.squareup.picasso.Picasso;

public class Music_RecycleViewAdapter extends RecyclerView.Adapter<Music_RecycleViewAdapter.ViewHolder> {

    Context context;
    List<Music> musics;
    private ActionMode actionMode;
    private List<Music> selectedMusics = new ArrayList<>();
    private MusicDao musicDao;
    private AppDatabase db;
    public Music_RecycleViewAdapter(Context context, List<Music> musics){
        this.context = context;
        this.musics = musics;
    }

    @NonNull
    @Override
    public Music_RecycleViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.recycler_view_row, parent, false);
        return new Music_RecycleViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Music_RecycleViewAdapter.ViewHolder holder, int position) {

        Music music = musics.get(position);
        holder.listTxtAlbum.setText(musics.get(position).albumName);
        holder.listTxtArtist.setText(musics.get(position).artistName);
        if(music.medium == 1){
            holder.imgMedium.setImageResource(R.drawable.cd_icon);
        }else if(music.medium == 2){
            holder.imgMedium.setImageResource(R.drawable.cassette_icon);
        }else if(music.medium == 3){
            holder.imgMedium.setImageResource(R.drawable.vinyl_icon);
        }else if(music.medium == 4){
            holder.imgMedium.setImageResource(R.drawable.usb_icon);
        }
        if(!musics.get(position).coverUrl.isEmpty()){
            Picasso.with(context).load(musics.get(position).coverUrl).into(holder.imgCover);
        }else{
            holder.imgCover.setImageResource(R.drawable.ic_launcher_background);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (actionMode != null) {
                    toggleSelection(music);
                } else {
                    Intent intent = new Intent(context, AddMusicActivity.class);
                    intent.putExtra("mode", "edit");
                    intent.putExtra("musicId", music.uid);
                    context.startActivity(intent);
                }
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (actionMode == null) {
                    actionMode = ((AppCompatActivity) context).startActionMode(actionModeCallback);
                }
                toggleSelection(music);
                return true;
            }
        });
        holder.setSelected(selectedMusics.contains(music));
    }

    private void toggleSelection(Music music) {
        if (selectedMusics.contains(music)) {
            selectedMusics.remove(music);
        } else {
            selectedMusics.add(music);
        }
        notifyDataSetChanged();
        if (actionMode != null) {
            actionMode.setTitle(selectedMusics.size() + " " + getString(context.getApplicationContext(), R.string.selected));
        }
    }
    private ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.selection_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            db = AppDatabase.getDatabase(context);
            musicDao = db.musicDao();
            if (item.getItemId() == R.id.action_delete) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    musicDao.deleteMusics(selectedMusics);
                    ((AppCompatActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            musics.removeAll(selectedMusics);
                            notifyDataSetChanged();
                        }
                    });
                });
                Toast.makeText(context, getString(context.getApplicationContext(), R.string.deleted), Toast.LENGTH_SHORT).show();
                mode.finish();
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedMusics.clear();
            notifyDataSetChanged();
        }
    };

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {


        ImageView imgCover;
        ImageView imgMedium;
        TextView listTxtAlbum;
        TextView listTxtArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCover = itemView.findViewById(R.id.imgCover);
            imgMedium = itemView.findViewById(R.id.imgMedium);
            listTxtAlbum = itemView.findViewById(R.id.listTxtAlbum);
            listTxtArtist = itemView.findViewById(R.id.listTxtArtist);
        }
        public void setSelected(boolean isSelected) {
            itemView.setBackgroundColor(isSelected ? Color.LTGRAY : Color.TRANSPARENT);
        }
    }
}