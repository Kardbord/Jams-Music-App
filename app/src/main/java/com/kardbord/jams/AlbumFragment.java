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
public class AlbumFragment extends Fragment {

    private ArrayList<Audio> m_audioList;

    private Button m_backButton;

    private TextView m_heading;

    private String m_currentAlbum;

    private final int HEADING_CHAR_LIMIT = 32;

    private MediaInterface m_callback;

    private ListView m_listView;

    private ArrayList<String> m_albums = new ArrayList<>();

    private HashSet<String> m_hashedAlbums = new HashSet<>();

    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MediaInterface)) throw new AssertionError();
        m_callback = (MediaInterface) context;
        m_audioList = m_callback.getAudioList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        m_listView = v.findViewById(R.id.albumList);

        m_heading = v.findViewById(R.id.album_frag_heading);

        initSongData();

        m_backButton = v.findViewById(R.id.album_frag_back_button);
        setButtonProperties(View.INVISIBLE, false);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, m_albums);

        m_listView.setAdapter(adapter);
        m_listView.setOnItemClickListener(onAlbumClicked);

        return v;
    }

    private void setButtonProperties(int visible, boolean clickable) {
        m_backButton.setVisibility(visible);
        m_backButton.setClickable(clickable);
    }

    private void initSongData() {
        getAlbums();
    }

    private void getAlbums() {
        for (Audio a : m_audioList) {
            if (!a.albumUnknown()) m_hashedAlbums.add(a.getAlbum());
        }
        m_albums = new ArrayList<>(m_hashedAlbums);
        Collections.sort(m_albums);
    }

    private ArrayList<String> getSongs(String album) {
        Hashtable<String, Integer> hashedSongs = new Hashtable<>();
        ArrayList<String> songs = new ArrayList<>();
        for (int i = 0; i < m_audioList.size(); ++i) {
            if (!m_audioList.get(i).titleUnknown() && Objects.equals(m_audioList.get(i).getAlbum(), album)
                    && !m_audioList.get(i).containsUnknown()){
                if (!hashedSongs.containsKey(m_audioList.get(i).getTitle())) {
                    songs.add(m_audioList.get(i).getTitle());
                }
                hashedSongs.put(m_audioList.get(i).getTitle(), i);
            }
        }
        Collections.sort(songs);
        return songs;
    }

    private View.OnClickListener backToAlbumsOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, m_albums);
            m_listView.setAdapter(adapter);
            m_heading.setText(R.string.albums);
            setButtonProperties(View.INVISIBLE, false);
            m_listView.setOnItemClickListener(onAlbumClicked);
        }
    };

    private AdapterView.OnItemClickListener onAlbumClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            setButtonProperties(View.VISIBLE, true);
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

    AdapterView.OnItemClickListener onSongClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String title = m_listView.getItemAtPosition(position).toString();
            m_callback.playAlbum(title, m_currentAlbum);
        }
    };

    private boolean textIsTooLong(String text) {
        return (text.length() > HEADING_CHAR_LIMIT);
    }
}
