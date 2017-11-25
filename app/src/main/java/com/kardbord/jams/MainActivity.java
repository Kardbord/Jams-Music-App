package com.kardbord.jams;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFrag = null;
            switch (item.getItemId()) {
                case R.id.navigation_artists:
                    selectedFrag = new ArtistFragment();
                    break;
                case R.id.navigation_albums:
                    selectedFrag = new AlbumFragment();
                    break;
                case R.id.navigation_songs:
                    break;
                case R.id.navigation_playlists:
                    break;
                default:
                    return false;
            }
            if (selectedFrag != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, selectedFrag).commit();
            }
            return true;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // Manually set first fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new ArtistFragment()).commit();
    }

}
