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
public class AlbumFragment extends Fragment {

    private MediaGetter m_callback;

    private ArrayList<Audio> m_audioList;

    private ListView m_listView;


    public AlbumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MediaGetter)) throw new AssertionError();
        m_callback = (MediaGetter) context;
        m_audioList = m_callback.getAudioList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        m_listView = v.findViewById(R.id.albumList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getAlbums());

        m_listView.setAdapter(adapter);

        return v;
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

}
