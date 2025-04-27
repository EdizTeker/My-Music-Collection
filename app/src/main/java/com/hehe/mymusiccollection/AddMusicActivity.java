package com.hehe.mymusiccollection;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
    int cassette;
    int vinyl;
    int cd;
    int digital;
    boolean isCoverArtUploaded = false;
    private Uri selectedImageUri;
    private String glbCoverUrl;
    private ActivityResultLauncher<String> pickImageLauncher;
    private static final int PERMISSION_REQUEST_CODE = 123;

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
        initComponents();

        isCoverArtUploaded = false;

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
                        if(music.medium == 1) { radioMedium.check(cd); }
                        else if(music.medium == 2) { radioMedium.check(cassette); }
                        else if(music.medium == 3) { radioMedium.check(vinyl); }
                        else if (music.medium == 4) { radioMedium.check(digital); }
                        if(!music.coverUrl.isEmpty()) {
                            Picasso.with(context).load(music.coverUrl).into(imgCover);
                            //
                        }else{
                            imgCover.setImageResource(R.drawable.music_note_icon);
                        }
                    });
                });
            }
            btnAdd.setText(getString(R.string.update));
        } else {
            btnAdd.setText(getString(R.string.add));
        }

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        isCoverArtUploaded = true;
                        selectedImageUri = uri;
                        Log.d("AddMusicActivity", "Choosen URI: " + uri);
                        takePersistableUriPermission(uri);
                        imgCover.setImageURI(uri);
                    }
                }
        );

    }

    public void setMusics(View view) {
        String album = txtAlbum.getText().toString();
        String artist = txtArtist.getText().toString();
        int mediumId = getMediumid();

        if (album.isEmpty() || artist.isEmpty() || radioMedium.getCheckedRadioButtonId() == -1){
            txtAlbum.setText("");
            txtArtist.setText("");
            String errorMessage = getString(R.string.error_fill);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }else {

            if (mode.equals("add")) {
                String coverUrl = "";
                if(!isCoverArtUploaded){
                    getAlbumCover(album, artist, mediumId, new CoverUrlCallback() {
                        @Override
                        public void onCoverUrlReceived(String coverUrl) {
                            Music newMusic = new Music(txtAlbum.getText().toString(), txtArtist.getText().toString(), coverUrl, mediumId);
                            musicDao.insertAll(newMusic);
                            if (coverUrl.isEmpty()) {
                                String errorNotFound = getString(R.string.error_cover_not_found);
                                Toast.makeText(getBaseContext(), errorNotFound, Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getBaseContext(), getString(R.string.succes_add), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else if (selectedImageUri != null){
                    coverUrl = selectedImageUri.toString();
                    Music newMusic = new Music(txtAlbum.getText().toString(), txtArtist.getText().toString(), coverUrl, mediumId);
                    musicDao.insertAll(newMusic);

                }
            }
            else {
                if(!isCoverArtUploaded){
                    getAlbumCover(album, artist, mediumId, new CoverUrlCallback() {
                        @Override
                        public void onCoverUrlReceived(String coverUrl) {

                            music.albumName = album;
                            music.artistName = artist;
                            music.coverUrl = coverUrl;
                            music.medium = mediumId;
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
                }else{
                    music.albumName = album;
                    music.artistName = artist;
                    music.medium = mediumId;
                    music.coverUrl = selectedImageUri.toString();
                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        musicDao.update(music);

                    });
                }
            }
            finish();
        }

    }
    public int getMediumid(){
        int mediumId = radioMedium.getCheckedRadioButtonId();
        if(mediumId == cd){return 1;}
        else if(mediumId == cassette){return 2;}
        else if(mediumId == vinyl){return 3;}
        else if(mediumId == digital){return 4;}
        else{return -1;}
    }

    public interface CoverUrlCallback {
        void onCoverUrlReceived(String coverUrl);
    }
    public void getAlbumCover(String albumName, String artistName, int mediumId, final CoverUrlCallback callback) {
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

    public void openImage(View view){
        if(mode.equals("edit")){
            if(!music.coverUrl.isEmpty()){
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(music.coverUrl));
                startActivity(intent);
            }
        }}



    public void launchPicker(View view) {
        pickImageLauncher.launch("image/*");
    }

    private void takePersistableUriPermission(Uri uri) {
        try {
            getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
            );
        } catch (SecurityException e) {
            Log.e("AddMusicActivity", "Failed to take persistable URI permission: " + e.getMessage());

        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
//            isCoverArtUploaded = true;
//            selectedImageUri = data.getData();
//            imgCover.setImageURI(selectedImageUri);
//        }
//    }

    private void initComponents(){
        txtAlbum = findViewById(R.id.txtAlbum);
        radioMedium = findViewById(R.id.radioMedium);
        txtArtist = findViewById(R.id.txtArtist);
        imgCover = findViewById(R.id.imgCover);
        btnAdd = findViewById(R.id.btnAdd);
        cassette = R.id.radioCassette;
        vinyl = R.id.radioVinyl;
        cd = R.id.radioCD;
        digital = R.id.radioDigital;
    }
}