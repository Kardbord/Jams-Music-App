package com.kardbord.jams;

import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import java.io.Serializable;
import java.util.Objects;


public class Audio implements Serializable, Parcelable {

    private String m_data;
    private String m_title;
    private String m_album;
    private String m_artist;

    public Audio(String data, String title, String album, String artist) {
        this.m_data = data;
        this.m_title = title;
        this.m_album = album;
        this.m_artist = artist;
    }

    protected Audio(Parcel in) {
        m_data = in.readString();
        m_title = in.readString();
        m_album = in.readString();
        m_artist = in.readString();
    }

    public static final Creator<Audio> CREATOR = new Creator<Audio>() {
        @Override
        public Audio createFromParcel(Parcel in) {
            return new Audio(in);
        }

        @Override
        public Audio[] newArray(int size) {
            return new Audio[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(m_data);
        parcel.writeString(m_title);
        parcel.writeString(m_album);
        parcel.writeString(m_artist);
    }
}
