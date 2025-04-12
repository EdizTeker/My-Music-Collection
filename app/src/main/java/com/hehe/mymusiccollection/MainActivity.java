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
import android.widget.RadioGroup;
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

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    SwipeRefreshLayout swipeLayout;
    private ImageButton btnTheme;
    private AppDatabase db;
    private MusicDao musicDao;
    private SearchView searchView;
    RecyclerView recyclerView;
    private boolean isGrid;
    Music_RecycleViewAdapter adapter;
    Music_RecycleViewGridAdapter gridAdapter;
    Context context = this;
    int lastSelectedRadioId = R.id.radioAllFilter;
    List<Music> currentFilteredList;

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
        initComponents();

        searchView.clearFocus();

        db = AppDatabase.getDatabase(this);
        musicDao = db.musicDao();
        currentFilteredList = musicDao.getAll();


        updateRecyclerView(currentFilteredList);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                resetAll();
                searchView.clearFocus();
                swipeLayout.setRefreshing(false);
            }
        });

    }

    private void searchList(String text) {
        if(text.isEmpty()){
            List<Music> musics = musicDao.getAll();
            updateRecyclerView(musics);
        }else {
            currentFilteredList = musicDao.searchByAlbumOrArtist(text);
            updateRecyclerView(currentFilteredList);
        }

    }

    public void changeTheme(View view){
        if (isGrid) {
            if(currentFilteredList!=null){ adapter = new Music_RecycleViewAdapter(this, currentFilteredList);} else { adapter = new Music_RecycleViewAdapter(this, musicDao.getAll());}
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            isGrid = false;
            btnTheme.setImageResource(R.drawable.grid_icon);
        } else {
            if(currentFilteredList!=null){ gridAdapter = new Music_RecycleViewGridAdapter(this, currentFilteredList);} else { gridAdapter = new Music_RecycleViewGridAdapter(this, musicDao.getAll());}
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

    public void filterButton(View view){
        searchView.clearFocus();
        searchView.setQuery("", false);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View viewBttm = getLayoutInflater().inflate(R.layout.bottom_sheet_layout, null);
        bottomSheetDialog.setContentView(viewBttm);
        bottomSheetDialog.show();
        RadioGroup radioGroup = viewBttm.findViewById(R.id.radioFilter);
        if (lastSelectedRadioId != -1) {
            radioGroup.check(lastSelectedRadioId);
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.radioCDFilter ){
                    currentFilteredList = musicDao.getAllByMedium("1");

                }
                else if(checkedId == R.id.radioCassetteFilter ){
                    currentFilteredList = musicDao.getAllByMedium("2");

                }
                else if(checkedId == R.id.radioVinylFilter ) {
                    currentFilteredList = musicDao.getAllByMedium("3");

                }
                else if(checkedId == R.id.radioDigitalFilter ) {
                    currentFilteredList = musicDao.getAllByMedium("4");

                }
                else if(checkedId == R.id.radioAllFilter){
                    currentFilteredList = musicDao.getAll();

                }
                if(currentFilteredList != null){updateRecyclerView(currentFilteredList);}
                lastSelectedRadioId = checkedId;
            }
        });
    }

    private void updateRecyclerView(List<Music> filteredList) {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        isGrid = sharedPreferences.getBoolean("isGrid", false);
        if (isGrid) {
            gridAdapter = new Music_RecycleViewGridAdapter(this, filteredList);
            recyclerView.setAdapter(gridAdapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false));
        }
        else {
            adapter = new Music_RecycleViewAdapter(this, filteredList);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        resetAll();
        searchView.clearFocus();

    }

    private void initComponents(){
        lastSelectedRadioId = R.id.radioAllFilter;
        recyclerView = findViewById(R.id.mRecyclerView);
        swipeLayout = findViewById(R.id.swiperefresh);
        btnTheme = findViewById(R.id.btnTheme);
        searchView = findViewById(R.id.search);
    }
    public void moveToNewActivity(View view){
        Intent intent = new Intent(this, AddMusicActivity.class);
        intent.putExtra("mode", "add");
        startActivity(intent);
    }
    public void resetAll(){
        searchView.clearFocus();
        searchView.setQuery("", false);
        lastSelectedRadioId = R.id.radioAllFilter;
        currentFilteredList = musicDao.getAll();
        updateRecyclerView(currentFilteredList);
    }

}