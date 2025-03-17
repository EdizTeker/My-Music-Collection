package com.hehe.mymusiccollection;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class Music_RecycleViewAdapter extends RecyclerView.Adapter<Music_RecycleViewAdapter.ViewHolder> {

    Context context;
    List<Music> musics;
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

        holder.listTxtAlbum.setText(musics.get(position).albumName);
        holder.listTxtArtist.setText(musics.get(position).artistName);
        holder.imgCover.setImageResource(R.drawable.ic_launcher_background);

    }

    @Override
    public int getItemCount() {
        return musics.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgCover;
        TextView listTxtAlbum;
        TextView listTxtArtist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgCover = itemView.findViewById(R.id.imgCover);
            listTxtAlbum = itemView.findViewById(R.id.listTxtAlbum);
            listTxtArtist = itemView.findViewById(R.id.listTxtArtist);
        }
    }
}