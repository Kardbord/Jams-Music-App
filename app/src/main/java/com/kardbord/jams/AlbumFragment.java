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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment {

    private ArrayList<Audio> m_audioList;

    private Button m_backButton;

    private ListView m_listView;

    public AlbumFragment() {
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
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        m_listView = v.findViewById(R.id.albumList);

        m_backButton = v.findViewById(R.id.album_frag_back_button);
        setButtonProperties(View.INVISIBLE, false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getAlbums());

        m_listView.setAdapter(adapter);
        m_listView.setOnItemClickListener(albumClickListener);

        return v;
    }

    private void setButtonProperties(int visible, boolean clickable) {
        m_backButton.setVisibility(visible);
        m_backButton.setClickable(clickable);
    }

    private ArrayList<String> getAlbums() {
        Hashtable<String, String> hashedAlbums = new Hashtable<>();
        for (Audio a : m_audioList) {
            if (!a.albumUnknown()) hashedAlbums.put(a.getAlbum(), a.getAlbum());
        }
        ArrayList<String> albums = new ArrayList<>(hashedAlbums.values());
        Collections.sort(albums);
        return albums;
    }

    private ArrayList<String> getSongs(String album) {
        HashSet<String> hashedSongs = new HashSet<>();
        for (Audio a : m_audioList) {
            if (!a.titleUnknown() && Objects.equals(a.getAlbum(), album)) hashedSongs.add(a.getTitle());
        }
        ArrayList<String> songs = new ArrayList<>(hashedSongs);
        Collections.sort(songs);
        return songs;
    }

    private View.OnClickListener backToAlbumOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getAlbums());
            m_listView.setAdapter(adapter);
            setButtonProperties(View.INVISIBLE, false);
            m_listView.setOnItemClickListener(albumClickListener);
        }
    };

    private AdapterView.OnItemClickListener albumClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setButtonProperties(View.VISIBLE, true);
            String album = m_listView.getItemAtPosition(position).toString();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getSongs(album));
            m_listView.setAdapter(adapter);
            // TODO: set m_listView click listener to play the selected song
            m_backButton.setOnClickListener(backToAlbumOnClick);
        }
    };

}
