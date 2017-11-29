package com.kardbord.jams;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.Hashtable;

public class MainActivity extends AppCompatActivity {

    Hashtable<String, Fragment> m_fragments;
    public final String ARTIST_FRAG = "ARTIST";
    public final String ALBUM_FRAG = "ALBUM";
    public final String SONG_FRAG = "SONG";
    public final String PLAYLIST_FRAG = "PLAYLIST";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            // TODO: keep an array of Fragments and swap them out
            // TODO: this will allow for keeping the audio database here rather than generating it for every fragment
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

        m_fragments = new Hashtable<>();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Create m_fragments to be used
        m_fragments.put(ARTIST_FRAG, new ArtistFragment());
        m_fragments.put(ALBUM_FRAG, new AlbumFragment());
        m_fragments.put(SONG_FRAG, new SongFragment());
        m_fragments.put(PLAYLIST_FRAG, new PlaylistFragment());

        // Manually set first fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.container, m_fragments.get(ARTIST_FRAG)).commit();
    }

}
