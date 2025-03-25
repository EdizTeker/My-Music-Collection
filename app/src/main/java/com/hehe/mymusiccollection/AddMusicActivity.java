package com.hehe.mymusiccollection;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class AddMusicActivity extends AppCompatActivity {

    EditText txtAlbum, txtArtist;
    RadioGroup radioMedium;
    private ImageView imgCover;
    Button btnAdd;
    private AppDatabase db;
    private MusicDao musicDao;
    int musicId;
    String mode;
    Music music;
    Context context;

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
        radioMedium = findViewById(R.id.radioMedium);
        txtArtist = findViewById(R.id.txtArtist);
        imgCover = findViewById(R.id.imgCover);
        btnAdd = findViewById(R.id.btnAdd);
        db = AppDatabase.getDatabase(this);
        musicDao = db.musicDao();
        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");
        if (mode.equals("edit")) {
            musicId = intent.getIntExtra("musicId", -1);
            if (musicId != -1) {
                AppDatabase.databaseWriteExecutor.execute(() -> {
                    music = musicDao.loadAllByIds(new int[]{musicId}).get(0);
                    runOnUiThread(() -> {
                        txtAlbum.setText(music.albumName);
                        txtArtist.setText(music.artistName);
                        radioMedium.check(music.medium);
                        if(!music.coverUrl.isEmpty()) {
                            Picasso.with(context).load(music.coverUrl).into(imgCover);
                        }
                    });
                });
            }
            btnAdd.setText(getString(R.string.update));
        } else {
            btnAdd.setText(getString(R.string.add));
        }

    }

    public void setMusics(View view) {
        String album = txtAlbum.getText().toString();
        String artist = txtArtist.getText().toString();
        int selectedRadioButtonId = radioMedium.getCheckedRadioButtonId();

        if (album.isEmpty() || artist.isEmpty() || selectedRadioButtonId == -1){
            txtAlbum.setText("");
            txtArtist.setText("");
            String errorMessage = getString(R.string.error_fill);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }else {

            if (mode.equals("add")) {

                getAlbumCover(album, artist, new CoverUrlCallback() {
                    @Override
                    public void onCoverUrlReceived(String coverUrl) {
                        Music newMusic = new Music(txtAlbum.getText().toString(), txtArtist.getText().toString(), coverUrl, selectedRadioButtonId);
                        musicDao.insertAll(newMusic);
                        if (coverUrl.isEmpty()) {
                            String errorNotFound = getString(R.string.error_cover_not_found);
                            Toast.makeText(getBaseContext(), errorNotFound, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getBaseContext(), getString(R.string.succes_add), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else {
                getAlbumCover(album, artist, new CoverUrlCallback() {
                    @Override
                    public void onCoverUrlReceived(String coverUrl) {

                        music.albumName = album;
                        music.artistName = artist;
                        music.coverUrl = coverUrl;
                        music.medium = selectedRadioButtonId;
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            musicDao.update(music);
                        });
                        if (coverUrl.isEmpty()) {
                            Toast.makeText(getBaseContext(), getString(R.string.error_cover_not_found), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getBaseContext(), getString(R.string.succes_update), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            finish();
        }

    }

    public interface CoverUrlCallback {
        void onCoverUrlReceived(String coverUrl);
    }
    public void getAlbumCover(String albumName, String artistName, final CoverUrlCallback callback) {
// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.discogs.com/database/search?release_title=" + albumName + "&artist=" + artistName + "&key=tiCpKrZiZoWgVzeCyuLX&secret=QsStdCKQcKCjveyhBdKDTtuKaiNxfWXC";

// Request a string response from the provided URL.
        JsonObjectRequest searchRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject searchResponse) {
                        try {
                            JSONArray results = searchResponse.getJSONArray("results");
                            String coverUrl;
                            if (results.length() == 0) {
                                coverUrl = "";
                            } else {
                                // İlk sonucu alıyoruz (en iyi eşleşme varsayımı)
                                JSONObject firstResult = results.getJSONObject(0);
                                int albumId = firstResult.getInt("id");
                                coverUrl = firstResult.getString("cover_image");
                            }

                            callback.onCoverUrlReceived(coverUrl);

                        } catch (JSONException e) {
                            //textView.setText("Albüm bilgileri ayrıştırılamadı: " + e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

        };

// Add the request to the RequestQueue.
        queue.add(searchRequest);


    }

}