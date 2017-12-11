package com.kardbord.jams;


import java.util.ArrayList;

public interface MediaInterface {
    ArrayList<Audio> getAudioList();
    void playMedia(int audioIndex);
}
