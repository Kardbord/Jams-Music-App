package com.kardbord.jams;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment {

    private final int HEADING_CHAR_LIMIT = 32;

    private Button m_backButton;

    private ArrayList<Audio> m_audioList;

    private TextView m_heading;

    private String m_currentAlbum;

    private ListView m_listView;

    private String m_mostRecentArtist;

    private MediaInterface m_callback;

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MediaInterface)) throw new AssertionError();
        m_callback = (MediaInterface) context;
    }

    public void setAudioList(ArrayList<Audio> audioList) {
        m_audioList = audioList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        m_listView = v.findViewById(R.id.artistList);

        m_heading = v.findViewById(R.id.artist_frag_heading);

        m_backButton = v.findViewById(R.id.artist_frag_back_button);
        setButtonProperties(View.INVISIBLE, false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getArtists());
        m_listView.setAdapter(adapter);

        m_listView.setOnItemClickListener(onArtistClicked);

        return v;
    }

    private void setButtonProperties(int visibiltity, boolean clickable) {
        m_backButton.setClickable(clickable);
        m_backButton.setVisibility(visibiltity);
    }

    private AdapterView.OnItemClickListener onArtistClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String artist = m_listView.getItemAtPosition(position).toString();
            m_mostRecentArtist = artist;
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getAlbums(artist));
            m_listView.setAdapter(adapter);
            m_listView.setOnItemClickListener(onAlbumClicked);
            if (!textIsTooLong(artist)) {
                m_heading.setText(artist);
            } else m_heading.setText(R.string.albums);
            setButtonProperties(View.VISIBLE, true);
            m_backButton.setOnClickListener(backToArtistsOnClick);
        }
    };

    private AdapterView.OnItemClickListener onAlbumClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            m_currentAlbum = m_listView.getItemAtPosition(position).toString();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getSongs(m_currentAlbum));
            m_listView.setAdapter(adapter);
            if (!textIsTooLong(m_currentAlbum)) {
                m_heading.setText(m_currentAlbum);
            } else m_heading.setText(R.string.songs);
            m_listView.setOnItemClickListener(onSongClicked);
            m_backButton.setOnClickListener(backToAlbumsOnClick);
        }
    };

    private View.OnClickListener backToArtistsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            setButtonProperties(View.INVISIBLE, false);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getArtists());
            m_listView.setAdapter(adapter);
            m_heading.setText(R.string.artists);
            m_listView.setOnItemClickListener(onArtistClicked);
        }
    };

    private View.OnClickListener backToAlbumsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getAlbums(m_mostRecentArtist));
            m_listView.setAdapter(adapter);
            if (!textIsTooLong(m_mostRecentArtist)) {
                m_heading.setText(m_mostRecentArtist);
            } else m_heading.setText(R.string.albums);
            m_listView.setOnItemClickListener(onAlbumClicked);
            m_backButton.setOnClickListener(backToArtistsOnClick);
        }
    };

    AdapterView.OnItemClickListener onSongClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String title = m_listView.getItemAtPosition(position).toString();
            m_callback.playAlbum(title, m_currentAlbum);
        }
    };

    private ArrayList<String> getSongs(String album) {
        Hashtable<String, Integer> hashedSongs = new Hashtable<>();
        ArrayList<String> songs = new ArrayList<>();
        for (int i = 0; i < m_audioList.size(); ++i) {
            Audio song = m_audioList.get(i);
            if (!song.titleUnknown() && Objects.equals(song.getAlbum(), album) && !song.containsUnknown()) {
                if (!hashedSongs.containsKey(song.getTitle())) {
                    songs.add(song.getTitle());
                }
                hashedSongs.put(song.getTitle(), i);
            }
        }
        Collections.sort(songs);
        return songs;
    }

    private ArrayList<String> getAlbums(String artist) {
        HashSet<String> hashedAlbums = new HashSet<>();
        for (Audio a: m_audioList) {
            if (!a.albumUnknown() && Objects.equals(a.getArtist(), artist)) hashedAlbums.add(a.getAlbum());
        }
        ArrayList<String> artistAlbums = new ArrayList<>(hashedAlbums);
        Collections.sort(artistAlbums);
        return artistAlbums;
    }

    // Checks to see if heading text is too long to fit on a line with the back button
    private boolean textIsTooLong(String text) {
        return (text.length() > HEADING_CHAR_LIMIT);
    }


    private ArrayList<String> getArtists() {
        Hashtable<String, String> hashedArtists = new Hashtable<>();
        for (Audio a : m_audioList) {
            if (!a.artistUnknown()) hashedArtists.put(a.getArtist(), a.getArtist());
        }
        ArrayList<String> artists = new ArrayList<>(hashedArtists.values());
        Collections.sort(artists);
        return artists;
    }

}
