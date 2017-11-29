package com.kardbord.jams;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements MediaGetter {

    private ArrayList<Audio> m_audioList;

    Hashtable<String, Fragment> m_fragments;
    public final String ARTIST_FRAG = "ARTIST";
    public final String ALBUM_FRAG = "ALBUM";
    public final String SONG_FRAG = "SONG";
    public final String PLAYLIST_FRAG = "PLAYLIST";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFrag;
            switch (item.getItemId()) {
                case R.id.navigation_artists:
                    selectedFrag = m_fragments.get(ARTIST_FRAG);
                    break;
                case R.id.navigation_albums:
                    selectedFrag = m_fragments.get(ALBUM_FRAG);
                    break;
                case R.id.navigation_songs:
                    selectedFrag = m_fragments.get(SONG_FRAG);
                    break;
                case R.id.navigation_playlists:
                    selectedFrag = m_fragments.get(PLAYLIST_FRAG);
                    break;
                default:
                    return false;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFrag).commit();
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadAudio();

        m_fragments = new Hashtable<>();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Create m_fragments to be used
        ArtistFragment artistFragment = new ArtistFragment();

        AlbumFragment albumFragment = new AlbumFragment();

        SongFragment songFragment = new SongFragment();

        PlaylistFragment playlistFragment = new PlaylistFragment();

        m_fragments.put(ARTIST_FRAG, artistFragment);
        m_fragments.put(ALBUM_FRAG, albumFragment);
        m_fragments.put(SONG_FRAG, songFragment);
        m_fragments.put(PLAYLIST_FRAG, playlistFragment);

        // Manually set first fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.container, m_fragments.get(ARTIST_FRAG)).commit();
    }

    private void loadAudio() {
        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            m_audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                m_audioList.add(new Audio(data, title, album, artist));
            }
        }
        if (cursor != null) cursor.close();
    }

    @Override
    public ArrayList<Audio> getAudioList() {
        return m_audioList;
    }
}
