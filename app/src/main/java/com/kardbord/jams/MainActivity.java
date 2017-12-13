package com.kardbord.jams;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
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
import android.view.MenuItem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements MediaInterface {

    private ArrayList<Audio> m_audioList;

    private MediaPlayerService m_player;
    private boolean m_serviceBound = false;

    private boolean m_playingAlbum = false;

    public static final String Broadcast_PLAY_NEW_AUDIO = "com.kardbord.jams.PlayNewAudio";

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

        artistFragment.setAudioList(m_audioList);
        albumFragment.setAudioList(m_audioList);
        songFragment.setAudioList(m_audioList);
        playlistFragment.setAudioList(m_audioList);

        // Manually set first fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.container, m_fragments.get(ARTIST_FRAG)).commit();
    }

    private ServiceConnection m_serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // We've bound to LocalService, case the IBinder and get LocalService instance
            MediaPlayerService.LocalBinder binder = (MediaPlayerService.LocalBinder) service;
            m_player = binder.getService();
            m_serviceBound = true;
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

    public void playAudio(int audioIndex) {
        if (!m_serviceBound) {
            // Store serializable audioList to shared preferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(m_audioList);
            storage.storeAudioIndex(audioIndex);

            Intent playerIntent = new Intent(this, MediaPlayerService.class);
            startService(playerIntent);
            bindService(playerIntent, m_serviceConnection, Context.BIND_AUTO_CREATE);
        } else {
            // Store the new audioIndex to SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            storage.storeAudio(m_audioList);
            storage.storeAudioIndex(audioIndex);

            // Service is active
            // Send a broadcast to the service -> PLAY_NEW_AUDIO
            Intent broadcastIntent = new Intent(Broadcast_PLAY_NEW_AUDIO);
            sendBroadcast(broadcastIntent);
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing
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
                Long album_id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));

                Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
                Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, album_id);

                m_audioList.add(new Audio(data, title, album, artist, albumArtUri.toString()));
            }
        }
        if (cursor != null) cursor.close();
    }

    private Hashtable<String, Integer> loadAlbum(String album) {
        ArrayList<Audio> albumTracks = new ArrayList<>();

        // Key is song title, value is position in m_audioList
        Hashtable<String, Integer> hashedAlbum = new Hashtable<>();

        int index = 0;
        for (Audio a : m_audioList) {
            if (Objects.equals(album, a.getAlbum()) && !a.containsUnknown()) {
                if (!hashedAlbum.containsKey(a.getTitle())) {
                    albumTracks.add(a);
                    hashedAlbum.put(a.getTitle(), index);
                    ++index;
                }
            }
        }
        m_audioList = albumTracks;
        return hashedAlbum;
    }

    @Override
    public ArrayList<Audio> getAudioList() {
        return m_audioList;
    }

    @Override
    public void playMedia(int audioIndex) {
        if (m_playingAlbum) {
            // load all songs instead of just an album
            loadAudio();
        }
        playAudio(audioIndex);
    }

    @Override
    public void playAlbum(String title, String album) {
        if (m_playingAlbum) {
            loadAudio();
        }

        m_playingAlbum = true;

        // Key is song title, value is position in m_audioList
        Hashtable<String, Integer> hashedAlbum = loadAlbum(album);

        playAudio(hashedAlbum.get(title));
    }

}
