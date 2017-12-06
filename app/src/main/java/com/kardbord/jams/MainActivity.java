package com.kardbord.jams;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Hashtable;

public class MainActivity extends AppCompatActivity implements MediaGetter {

    private ArrayList<Audio> m_audioList;

    private MediaPlayerService m_player;
    boolean m_serviceBound = false;

    private Hashtable<String, Fragment> m_fragments;
    private final String ARTIST_FRAG = "ARTIST";
    private final String ALBUM_FRAG = "ALBUM";
    private final String SONG_FRAG = "SONG";
    private final String PLAYLIST_FRAG = "PLAYLIST";

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

        playAudio(m_audioList.get(0).getData());
    }

    private ServiceConnection m_serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, case the IBinder and et LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            m_player = binder.getService();
            m_serviceBound = true;

            Toast.makeText(MainActivity.this, "Service Bound", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            m_serviceBound = false;
        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("ServiceState", m_serviceBound);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m_serviceBound) {
            unbindService(m_serviceConnection);
            // service is active
            m_player.stopSelf();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        m_serviceBound = savedInstanceState.getBoolean("ServiceState");
    }

    private void playAudio(String media) {
        // Check if service is active
        if (!m_serviceBound) {
            Log.i("Media Player", "PlayAudio");
            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            playerIntent.putExtra("media", media);
            startService(playerIntent);
            bindService(playerIntent, m_serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // Service is active, send media with Broadcast Receiver
        }
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
