package com.hehe.mymusiccollection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    EditText txtAlbum;
    EditText txtArtist;
    Button btnAdd;
    TextView labelAlbum;
    private AppDatabase db;
    private MusicDao musicDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        RecyclerView recyclerView = findViewById(R.id.mRecyclerView);
        txtAlbum = findViewById(R.id.txtAlbum);
        txtArtist = findViewById(R.id.txtArtist);
        btnAdd = findViewById(R.id.btnAdd);
        labelAlbum = findViewById(R.id.lblAlbum);
        db = AppDatabase.getDatabase(this);
        musicDao = db.musicDao();
        List<Music> musics = musicDao.getAll();
        Music_RecycleViewAdapter adapter = new Music_RecycleViewAdapter(this, musics);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


    }









    public void updateText(View view){
        setMusics();
        List<Music> musics = getMusics();
        labelAlbum.setText(musics.toString());
    }



    public List<Music> getMusics() {

        return musicDao.getAll();
    }
    public void setMusics() {
        Music newMusic = new Music(txtAlbum.getText().toString(), txtArtist.getText().toString());
        musicDao.insertAll(newMusic);
    }


}