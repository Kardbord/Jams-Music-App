package com.kardbord.jams;


import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

    private ArrayList<Audio> m_audioList;

    private ListView m_listView;


    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        m_listView = v.findViewById(R.id.albumList);

        loadAudio();

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

    private void loadAudio() {
        ContentResolver contentResolver = getActivity().getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cursor = contentResolver.query(uri, null, selection, null, sortOrder);

        if (cursor != null && cursor.getCount() > 0) {
            m_audioList = new ArrayList<>();
            while (cursor.moveToNext()) {
                String data = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                m_audioList.add(new Audio(data, title, album, artist));
            }
        }
        if (cursor != null) cursor.close();
    }

}
