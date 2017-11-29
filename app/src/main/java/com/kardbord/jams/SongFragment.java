package com.kardbord.jams;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {

    private ArrayList<Audio> m_audioList;

    public SongFragment() {
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
        View v = inflater.inflate(R.layout.fragment_song, container, false);
        ListView m_listView = v.findViewById(R.id.songList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getSongs());
        m_listView.setAdapter(adapter);

        return v;
    }

    private ArrayList<String> getSongs() {
        Hashtable<String, String> hashedSongs = new Hashtable<>();
        for (Audio a : m_audioList) {
            if (!a.titleUnknown()) hashedSongs.put(a.getTitle(), a.getTitle());
        }
        ArrayList<String> songs = new ArrayList<>(hashedSongs.values());
        Collections.sort(songs);
        return songs;
    }

}
