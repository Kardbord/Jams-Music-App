package com.kardbord.jams;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class ArtistFragment extends Fragment {

    // TODO: implement getAlbums(Artist), getSongs(Artist), and getSongs(Album)
    // TODO: when an artist is clicked, repopulate m_listView using getAlbums
    // TODO: when 'all songs' is clicked, repopulate m_listView using getSongs
    // TODO: when an album is clicked, repopulate m_listView using getSongs

    private ArrayList<Audio> m_audioList;

    private ListView m_listView;

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MediaGetter)) throw new AssertionError();
        MediaGetter m_callback = (MediaGetter) context;
        m_audioList = m_callback.getAudioList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        m_listView = v.findViewById(R.id.artistList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getArtists());
        m_listView.setAdapter(adapter);

        m_listView.setOnItemClickListener(artistClickListener);

        return v;
    }

    private AdapterView.OnItemClickListener artistClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String artist = m_listView.getItemAtPosition(position).toString();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getAlbums(artist));
            m_listView.setAdapter(adapter);
            m_listView.setOnItemClickListener(albumClickListener);
        }
    };

    private AdapterView.OnItemClickListener albumClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String album = m_listView.getItemAtPosition(position).toString();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getSongs(album));
            m_listView.setAdapter(adapter);
        }
    };

    private ArrayList<String> getSongs(String album) {
        HashSet<String> hashedSongs = new HashSet<>();
        for (Audio a : m_audioList) {
            if (!a.titleUnknown() && Objects.equals(a.getAlbum(), album)) hashedSongs.add(a.getTitle());
        }
        ArrayList<String> songs = new ArrayList<>(hashedSongs);
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
