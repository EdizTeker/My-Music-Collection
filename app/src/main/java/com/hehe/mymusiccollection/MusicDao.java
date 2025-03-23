package com.hehe.mymusiccollection;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MusicDao {
    @Query("SELECT * FROM music")
    List<Music> getAll();

    @Query("SELECT * FROM music WHERE uid IN (:musicIds)")
    List<Music> loadAllByIds(int[] musicIds);

    @Query("SELECT * FROM music WHERE album_name LIKE :first AND " +
            "artist_name LIKE :last LIMIT 1")
    Music findByName(String first, String last);

    @Query("SELECT * FROM music WHERE album_name LIKE :searchText OR artist_name LIKE :searchText")
    List<Music> searchByAlbumOrArtist(String searchText);

    @Insert
    void insertAll(Music... musics);

    @Delete
    void delete(Music music);
    @Delete
    void deleteMusics(List<Music> musics);
    @Update
    void update(Music music);
}
