package com.kardbord.jams;


import java.util.ArrayList;

public interface MediaInterface {
    ArrayList<Audio> getAudioList();
    void playMedia(int audioIndex);
    void playAlbum(String title, String album);
    // void playPlaylist(String songTitle, String playlist);
    // void playArtist(String title, String artist);
}
