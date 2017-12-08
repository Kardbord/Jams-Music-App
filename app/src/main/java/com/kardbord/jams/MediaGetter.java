package com.kardbord.jams;


import java.util.ArrayList;

public interface MediaGetter {
    ArrayList<Audio> getAudioList();
    void playMedia(int audioIndex);
}
