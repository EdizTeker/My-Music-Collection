package com.hehe.mymusiccollection;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


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

    public void setMusics(View view) {
        String album = txtAlbum.getText().toString();
        String artist = txtArtist.getText().toString();

        if (!album.isEmpty() && !artist.isEmpty()) {

            getAlbumCover(album, artist, new CoverUrlCallback() {
                @Override
                public void onCoverUrlReceived(String coverUrl){
                    Music newMusic = new Music(txtAlbum.getText().toString(), txtArtist.getText().toString(), coverUrl);
                    musicDao.insertAll(newMusic);
                    String successMessage = getString(R.string.succes);
                    Toast.makeText(getBaseContext(), successMessage, Toast.LENGTH_SHORT).show();
                    finish();
                }
            });

            }
        else {
            txtAlbum.setText("");
            txtArtist.setText("");
            String errorMessage = getString(R.string.error_fill);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
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

                            if (results.length() == 0) {
                                String errorNotFound = getString(R.string.error_cover_not_found);
                                Toast.makeText(getBaseContext(), errorNotFound, Toast.LENGTH_SHORT).show();
                                return;
                            }

                            // İlk sonucu alıyoruz (en iyi eşleşme varsayımı)
                            JSONObject firstResult = results.getJSONObject(0);
                            int albumId = firstResult.getInt("id");
                            String coverUrl = firstResult.getString("cover_image");
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