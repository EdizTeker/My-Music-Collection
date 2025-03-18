package com.hehe.mymusiccollection;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Music {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "album_name")
    public String albumName;

    @ColumnInfo(name = "artist_name")
    public String artistName;
    @ColumnInfo(name = "coverUrl")
    public String coverUrl;

    public Music(String albumName, String artistName) {
        this.albumName = albumName;
        this.artistName = artistName;
    }

    @Override
    public String toString() {
        return uid + " " + albumName + " " + artistName + "\n";
    }
}