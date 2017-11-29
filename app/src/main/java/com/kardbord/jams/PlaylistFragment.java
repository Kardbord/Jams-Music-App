package com.kardbord.jams;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlaylistFragment extends Fragment {

    private ArrayList<Audio> m_audioList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof MediaGetter)) throw new AssertionError();
        MediaGetter m_callback = (MediaGetter) context;
        m_audioList = m_callback.getAudioList();
    }


    public PlaylistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_playlist, container, false);
        ListView m_listView = v.findViewById(R.id.playlistList);

        return v;
    }

    // TODO: implement playlist somehow...

}
