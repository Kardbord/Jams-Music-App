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
import java.util.Hashtable;


/**
 * A simple {@link Fragment} subclass.
 */
public class SongFragment extends Fragment {

    private ArrayList<Audio> m_audioList;

    private MediaInterface m_callback;

    private ListView m_listView;

    private ArrayList<String> m_songs = new ArrayList<>();

    // Value is position in m_audioList, key is title
    private Hashtable<String, Integer> m_hashedSongs = new Hashtable<>();

    public SongFragment() {
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
        View v = inflater.inflate(R.layout.fragment_song, container, false);
        m_listView = v.findViewById(R.id.songList);

        initSongData();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_selectable_list_item, m_songs);
        m_listView.setAdapter(adapter);

        m_listView.setOnItemClickListener(onSongClicked);

        return v;
    }

    private AdapterView.OnItemClickListener onSongClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String title = m_listView.getItemAtPosition(position).toString();
            m_callback.playMedia(m_hashedSongs.get(title));
        }
    };

    private void initSongData() {
        for (int i = 0; i < m_audioList.size(); ++i) {
            if (!m_audioList.get(i).titleUnknown() && !m_audioList.get(i).containsUnknown()) {
                if (!m_hashedSongs.containsKey(m_audioList.get(i).getTitle())) {
                    m_songs.add(m_audioList.get(i).getTitle());
                }
                m_hashedSongs.put(m_audioList.get(i).getTitle(), i);
            }
        }
        Collections.sort(m_songs);
    }
}
