package com.hehe.mymusiccollection;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class AddMusicActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_add_music);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtAlbum = findViewById(R.id.txtAlbum);
        txtArtist = findViewById(R.id.txtArtist);
        btnAdd = findViewById(R.id.btnAdd);
        db = AppDatabase.getDatabase(this);
        musicDao = db.musicDao();
    }

//    public void updateText(View view){
//        setMusics();
//        List<Music> musics = getMusics();
//        labelAlbum.setText(musics.toString());
//    }
//    public List<Music> getMusics() {
//
//        return musicDao.getAll();
//    }
    public void setMusics(View view) {
        Music newMusic = new Music(txtAlbum.getText().toString(), txtArtist.getText().toString());
        musicDao.insertAll(newMusic);
    }

}