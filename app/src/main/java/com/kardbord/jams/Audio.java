package com.kardbord.jams;

import android.provider.MediaStore;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Objects;


public class Audio implements Serializable {

    private Hashtable<String, String> m_playlists;

    private String m_data;
    private String m_title;
    private String m_album;
    private String m_artist;

    public Audio(String data, String title, String album, String artist) {
        this.m_data = data;
        this.m_title = title;
        this.m_album = album;
        this.m_artist = artist;
        m_playlists = new Hashtable<>();
    }

    public void addToPlaylist(String playlist) {
        m_playlists.put(playlist, playlist);
    }

    public void removeFromPlayList(String playlist) {
        m_playlists.remove(playlist);
    }

    public Hashtable<String, String> getPlaylists() { return m_playlists; }

    public boolean containsUnknown() {
        return (Objects.equals(m_artist, MediaStore.UNKNOWN_STRING) ||
                Objects.equals(m_data, MediaStore.UNKNOWN_STRING) ||
                Objects.equals(m_title, MediaStore.UNKNOWN_STRING) ||
                Objects.equals(m_album, MediaStore.UNKNOWN_STRING));
    }

    public boolean artistUnknown() {
        return (Objects.equals(m_artist, MediaStore.UNKNOWN_STRING));
    }

    public boolean albumUnknown() {
        return (Objects.equals(m_album, MediaStore.UNKNOWN_STRING));
    }

    public boolean titleUnknown() {
        return (Objects.equals(m_title, MediaStore.UNKNOWN_STRING));
    }

    public boolean dataUnknown() {
        return (Objects.equals(m_data, MediaStore.UNKNOWN_STRING));
    }

    public String getData() { return m_data; }

    public void setData(String data) { m_data = data; }

    public String getTitle() { return m_title; }

    public void setTitle(String title) { m_title = title; }

    public String getAlbum() { return m_album; }

    public void setAlbum(String album) { m_album = album; }

    public String getArtist() { return m_artist; }

    public void setArtist(String artist) { m_artist = artist; }
}
