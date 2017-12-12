package com.kardbord.jams;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends Fragment {

    private ArrayList<Audio> m_audioList;

    private Hashtable<String, String> m_playlists;

    private ListView m_listView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MediaInterface)) throw new AssertionError();
        MediaInterface m_callback = (MediaInterface) context;
    }

    public void setAudioList(ArrayList<Audio> audioList) {
        m_audioList = audioList;
    }

    public PlaylistFragment() {
        // Required empty public constructor
    }

    private ArrayList<String> getPlaylists() {
        ArrayList<String> playlists = new ArrayList<>(m_playlists.values());
        Collections.sort(playlists);
        return playlists;
    }

    private void populateListView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getPlaylists());
        m_listView.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        // m_listView = v.findViewById(R.id.playlistList); // TODO: uncomment these lines
        // populateListView();
        m_playlists = new Hashtable<>();

        Button playlistButton = v.findViewById(R.id.addPlayListButton);
        playlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: prompt user for input - get name of playlist, select songs to be on list
                // TODO: add new playlist name to m_playlists
                // TODO: for (Audio a : m_audioList) if (a was selected) a.addToPlaylist(playlist_name)
                // TODO: populateListView()
            }
        });

        return v;
    }

}
