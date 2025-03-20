package com.hehe.mymusiccollection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private FloatingActionButton btnMove;
    private AppDatabase db;
    private MusicDao musicDao;
    private SearchView searchView;
    private TextView textView;
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.mRecyclerView);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        btnMove = findViewById(R.id.btnMove);
        db = AppDatabase.getDatabase(this);
        musicDao = db.musicDao();
        List<Music> musics = musicDao.getAll();
        Music_RecycleViewAdapter adapter = new Music_RecycleViewAdapter(this, musics);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView = findViewById(R.id.search);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

    }

    private void filterList(String text) {
        if(text.isEmpty()){
            List<Music> musics = musicDao.getAll();
            Music_RecycleViewAdapter adapter = new Music_RecycleViewAdapter(this, musics);
            recyclerView.setAdapter(adapter);

        }else {
        List<Music> filteredList = musicDao.searchByAlbumOrArtist(text);
        Music_RecycleViewAdapter adapter = new Music_RecycleViewAdapter(this, filteredList);
        recyclerView.setAdapter(adapter);
        if(filteredList.isEmpty()){
            String noData = getString(R.string.no_data);
            Toast.makeText(this, noData, Toast.LENGTH_SHORT).show();
        }
        }

    }


    public void moveToNewActivity(View view){
            Intent intent = new Intent(this, AddMusicActivity.class);
            startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        List<Music> musics = musicDao.getAll();
        Music_RecycleViewAdapter adapter = new Music_RecycleViewAdapter(this, musics);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchView.clearFocus();

    }






}