package com.kardbord.jams;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

// This class is based off of a tutorial found at
// https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener
{

    private MediaPlayer m_mediaPlayer;

    // path to the audio file
    private String m_mediaFile;

    private int resumePosition;

    private IBinder iBinder = new LocalBinder();

    private void initMediaPlayer() {
        m_mediaPlayer = new MediaPlayer();
        // Set up MediaPlayer event listeners
        m_mediaPlayer.setOnCompletionListener(this);
        m_mediaPlayer.setOnErrorListener(this);
        m_mediaPlayer.setOnPreparedListener(this);
        m_mediaPlayer.setOnBufferingUpdateListener(this);
        m_mediaPlayer.setOnSeekCompleteListener(this);
        m_mediaPlayer.setOnInfoListener(this);
        // Reset so the MediaPlayer is not pointing to another data source
        m_mediaPlayer.reset();

        m_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            m_mediaPlayer.setDataSource(m_mediaFile);
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
    }

    private void playMedia() {
        if (!m_mediaPlayer.isPlaying()) {
            m_mediaPlayer.start();
        }
    }

    private void stopMedia() {
        if (m_mediaPlayer == null) return;
        if (m_mediaPlayer.isPlaying()) {
            m_mediaPlayer.stop();
        }
    }

    private void pauseMedia() {
        if (m_mediaPlayer.isPlaying()) {
            m_mediaPlayer.pause();
            resumePosition = m_mediaPlayer.getCurrentPosition();
        }
    }

    private void resumeMedia() {
        if (!m_mediaPlayer.isPlaying()) {
            m_mediaPlayer.seekTo(resumePosition);
            m_mediaPlayer.start();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return iBinder;
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // Invoked indicating buffering status of a media resource being streamed over the network
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        // Invoked when playback of a media source has completed
        stopMedia();
        // Stop the service
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // Invoked when there has been an error during an asynchronous operation
        switch (what) {
            case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
                Log.d("MediaPlayer Error", "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK " + extra);
                break;

            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.d("MediaPlayer Error", "MEDIA ERROR SERVER DIED " + extra);
                break;

            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.d("MediaPlayer Error", "MEDIA ERROR UNKOWN " + extra);
                break;
        }
        return false;
    }

    @Override
    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        // Invoked to communicate some info
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // Invoked when the media source is ready for playback
        playMedia();
    }

    @Override
    public void onSeekComplete(MediaPlayer mp) {
        // Invoked indicating the completion of a seek operation
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        // Invoked when the audio focus of the system is updated
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() { return MediaPlayerService.this; }
    }

}
