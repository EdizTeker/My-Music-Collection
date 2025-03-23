package com.hehe.mymusiccollection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    SwipeRefreshLayout sw;
    private FloatingActionButton btnMove;
    private ImageButton btnTheme;
    private AppDatabase db;
    private MusicDao musicDao;
    private SearchView searchView;
    private TextView textView;
    RecyclerView recyclerView;
    private boolean isGrid;
    Music_RecycleViewAdapter adapter;
    Music_RecycleViewGridAdapter gridAdapter;
    Context context = this;

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
        sw = findViewById(R.id.swiperefresh);
        btnMove = findViewById(R.id.btnMove);
        btnTheme = findViewById(R.id.btnTheme);
        db = AppDatabase.getDatabase(this);
        musicDao = db.musicDao();
        List<Music> musics = musicDao.getAll();
        gridAdapter = new Music_RecycleViewGridAdapter(this, musics);
        adapter = new Music_RecycleViewAdapter(this, musics);
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGrid = sharedPreferences.getBoolean("isGrid", false); // Varsayılan olarak liste görünümü

        // Layout'u ayarlama
        if (isGrid) {
            recyclerView.setAdapter(gridAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        } else {
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
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

        sw.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                List<Music> musics = musicDao.getAll();
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                isGrid = sharedPreferences.getBoolean("isGrid", false); // Varsayılan olarak liste görünümü

                // Layout'u ayarlama
                if (isGrid) {
                    gridAdapter = new Music_RecycleViewGridAdapter(context, musics);
                    recyclerView.setAdapter(gridAdapter);
                    recyclerView.setLayoutManager(new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false));
                } else {
                    adapter = new Music_RecycleViewAdapter(context, musics);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(context));
                }
                searchView.clearFocus();
                sw.setRefreshing(false);
            }
        });

    }

    private void filterList(String text) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGrid = sharedPreferences.getBoolean("isGrid", false);
        List<Music> musics = musicDao.getAll();
        List<Music> filteredList = musicDao.searchByAlbumOrArtist(text);
        if (isGrid) {
            if(text.isEmpty()){
                gridAdapter = new Music_RecycleViewGridAdapter(this, musics);
                recyclerView.setAdapter(gridAdapter);

            }else {
                gridAdapter = new Music_RecycleViewGridAdapter(this, filteredList);
                recyclerView.setAdapter(gridAdapter);
            }
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        } else {
            if(text.isEmpty()){
                adapter = new Music_RecycleViewAdapter(this, musics);
                recyclerView.setAdapter(adapter);

            }else {
                adapter = new Music_RecycleViewAdapter(this, filteredList);
                recyclerView.setAdapter(adapter);
            }
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }


    }

    public void moveToNewActivity(View view){
            Intent intent = new Intent(this, AddMusicActivity.class);
        intent.putExtra("mode", "add");
            startActivity(intent);
    }

    @Override
    public void onResume(){
        super.onResume();
        List<Music> musics = musicDao.getAll();
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGrid = sharedPreferences.getBoolean("isGrid", false); // Varsayılan olarak liste görünümü

        // Layout'u ayarlama
        if (isGrid) {
            gridAdapter = new Music_RecycleViewGridAdapter(this, musics);
            recyclerView.setAdapter(gridAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        } else {
            adapter = new Music_RecycleViewAdapter(this, musics);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
        searchView.clearFocus();

    }



    public void changeTheme(View view){
        if (isGrid) {
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            isGrid = false;
            btnTheme.setImageResource(R.drawable.grid_icon);
        } else {
            recyclerView.setAdapter(gridAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
            isGrid = true;
            btnTheme.setImageResource(R.drawable.list_icon);

        }
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isGrid", isGrid);
        editor.apply();
    }



}