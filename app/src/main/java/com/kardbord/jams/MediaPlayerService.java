package com.kardbord.jams;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.util.Objects;

// This class is based off of a tutorial found at
// https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener
{

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_mediaPlayer != null) {
            stopMedia();
            m_mediaPlayer.release();
        }
        removeAudioFocus();
    }

    // System calls this method when an activity requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // an audio file is passed to the service through putExtra()
            //noinspection ConstantConditions
            m_mediaFile = intent.getExtras().getString("media");
        } catch (NullPointerException e) {
            stopSelf();
        }

        // Request audio focus
        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (m_mediaFile != null && !Objects.equals(m_mediaFile, "")) initMediaPlayer();

        return super.onStartCommand(intent, flags, startId);
    }

    private MediaPlayer m_mediaPlayer;

    private AudioManager m_audioManager;

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
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // resume playback
                if (m_mediaPlayer == null) initMediaPlayer();
                else if (!m_mediaPlayer.isPlaying()) m_mediaPlayer.start();
                m_mediaPlayer.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (m_mediaPlayer.isPlaying()) m_mediaPlayer.stop();
                m_mediaPlayer.release();
                m_mediaPlayer = null;
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // Lost focus for a short time, but need to stop playback
                // We won't release playback because playback is likely to resume
                if (m_mediaPlayer.isPlaying()) m_mediaPlayer.pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // Lost focus for a short time, but it's okay to keep playing at an attenuated level
                if (m_mediaPlayer.isPlaying()) m_mediaPlayer.setVolume(0.1f, 0.1f);
                break;
        }
    }

    private boolean requestAudioFocus() {
        m_audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert m_audioManager != null;
        int result = m_audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == m_audioManager.abandonAudioFocus(this);
    }


    public class LocalBinder extends Binder {
        public MediaPlayerService getService() { return MediaPlayerService.this; }
    }

}
