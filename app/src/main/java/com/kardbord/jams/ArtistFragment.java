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
public class ArtistFragment extends Fragment {

    public interface mediaGetter {
        ArrayList<Audio> getAudioList();
    }

    private mediaGetter m_callback;

    private ArrayList<Audio> m_audioList;

    private ListView m_listView;

    public ArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof mediaGetter)) throw new AssertionError();
        m_callback = (mediaGetter) context;
        m_audioList = m_callback.getAudioList();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_artist, container, false);
        m_listView = v.findViewById(R.id.artistList);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, getArtists());
        m_listView.setAdapter(adapter);

        return v;
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
