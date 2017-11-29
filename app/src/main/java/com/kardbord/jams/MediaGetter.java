package com.kardbord.jams;


import java.util.ArrayList;
import java.util.Hashtable;

public interface MediaGetter {
    ArrayList<Audio> getAudioList();
    Hashtable<String, String> getPlaylists();
}
