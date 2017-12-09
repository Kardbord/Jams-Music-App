package com.kardbord.jams;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSessionManager;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

// This class is based off of a tutorial found at
// https://www.sitepoint.com/a-step-by-step-guide-to-building-an-android-audio-player-app/
public class MediaPlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
        MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
        AudioManager.OnAudioFocusChangeListener {

    public class LocalBinder extends Binder {
        public MediaPlayerService getService() {
            return MediaPlayerService.this;
        }
    }

    private MediaPlayer m_mediaPlayer;

    private AudioManager m_audioManager;

    private int resumePosition;

    public static final String ACTION_PLAY = "com.kardbord.jams.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.kardbord.jams.ACTION_PAUSE";
    public static final String ACTION_PREVIOUS = "com.kardbord.jams.ACTION_PREVIOUS";
    public static final String ACTION_NEXT = "com.kardbord.jams.ACTION_NEXT";
    public static final String ACTION_STOP = "com.kardbord.jams.ACTION_STOP";

    // MediaSession
    private MediaSessionManager mediaSessionManager;
    private MediaSessionCompat mediaSession;
    private MediaControllerCompat.TransportControls transportControls;

    // AudioPlayer notification ID
    private static final int NOTIFICATION_ID = 101;

    private ArrayList<Audio> audioList;
    private int audioIndex = -1;
    private Audio activeAudio;

    private IBinder iBinder = new LocalBinder();

    private boolean ongoingCall = false;
    private PhoneStateListener phoneStateListener;
    private TelephonyManager telephonyManager;

    public enum PlaybackStatus {
        PLAYING,
        PAUSED
    }

    private BroadcastReceiver becomingNoisyReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pauseMedia();
            buildNotification(PlaybackStatus.PAUSED);
        }
    };

    private BroadcastReceiver playNewAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            audioIndex = new StorageUtil(getApplicationContext()).loadAudioIndex();
            if (audioIndex != -1 && audioIndex < audioList.size()) {
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }

            // A PLAY_NEW_AUDIO action received
            stopMedia();
            m_mediaPlayer.reset();
            initMediaPlayer();
            updateMetaData();
            buildNotification(PlaybackStatus.PLAYING);
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (m_mediaPlayer != null) {
            stopMedia();
            m_mediaPlayer.release();
        }
        removeAudioFocus();
        // Disable the PhoneStateListener
        if (phoneStateListener != null) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        removeNotification();

        // unregister BroadcastReceivers
        unregisterReceiver(becomingNoisyReciever);
        unregisterReceiver(playNewAudio);

        // clear cached playlist
        new StorageUtil(getApplicationContext()).clearCachedAudioPlaylist();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mediaSession.release();
        removeNotification();
        return super.onUnbind(intent);
    }

    // System calls this method when an activity requests the service be started
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            // Load data from SharedPreferences
            StorageUtil storage = new StorageUtil(getApplicationContext());
            audioList = storage.loadAudio();
            audioIndex = storage.loadAudioIndex();

            if (audioIndex != -1 && audioIndex < audioList.size()) {
                // index is in a valid range
                activeAudio = audioList.get(audioIndex);
            } else {
                stopSelf();
            }
        } catch (NullPointerException e) {
            stopSelf();
        }

        // Request audio focus
        if (!requestAudioFocus()) {
            stopSelf();
        }

        if (mediaSessionManager == null) {
            try {
                initMediaSession();
                initMediaPlayer();
            }
            catch (RemoteException e) {
                e.printStackTrace();
                stopSelf();
            }
            buildNotification(PlaybackStatus.PLAYING);
        }
        handleincomingActions(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Perform one-time setup procedures

        // Manage incoming phone calls during playback
        // Pause MediaPlayer on incoming call, resume on hangup
        callStateListener();
        // ACTION_AUDIO_BECOMING_NOISY -- change in audio outputs -- BroadcastReceiver
        registerBecomingNoisyReceiver();
        // Listen for new Audio to play -- BroadcastReceiver
        register_playNewAudio();
    }

    private void register_playNewAudio() {
        IntentFilter filter = new IntentFilter(MainActivity.Broadcast_PLAY_NEW_AUDIO);
        registerReceiver(playNewAudio, filter);
    }

    private void callStateListener() {
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state) {
                    // if at least one call exists or the phone is ringing, pause the MediaPlayer
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                    case TelephonyManager.CALL_STATE_RINGING:
                        if (m_mediaPlayer != null) {
                            pauseMedia();
                            ongoingCall = true;
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // Phone idle, start playing
                        if (m_mediaPlayer != null) {
                            if (ongoingCall) {
                                ongoingCall = false;
                                resumeMedia();
                            }
                        }
                        break;
                }
            }
        };

        // Register the listener with the telephony manager
        // Listen for changes to the device call state
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    private void initMediaSession() throws RemoteException {
        if (mediaSessionManager != null) return;

        mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);

        mediaSession = new MediaSessionCompat(getApplicationContext(), "AudioPlayer");
        transportControls = mediaSession.getController().getTransportControls();
        mediaSession.setActive(true);
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        updateMetaData();

        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                resumeMedia();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
                pauseMedia();
                buildNotification(PlaybackStatus.PAUSED);
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                skipToNext();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                skipToPrevious();
                updateMetaData();
                buildNotification(PlaybackStatus.PLAYING);
            }

            @Override
            public void onStop() {
                super.onStop();
                removeNotification();
                stopSelf();
            }

            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
            }
        });
    }

    private void updateMetaData() {
        Bitmap albumArt = BitmapFactory.decodeResource(getResources(), R.drawable.albumart); // TODO: replace with medias album art
        mediaSession.setMetadata(new MediaMetadataCompat.Builder()
                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, albumArt)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, activeAudio.getArtist())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, activeAudio.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, activeAudio.getTitle())
                .build());
    }

    private void skipToNext() {
        if (audioIndex == audioList.size() - 1) {
            // if last in playlist
            audioIndex = 0;
            activeAudio = audioList.get(audioIndex);
        } else {
            // get next in playlist
            activeAudio = audioList.get(++audioIndex);
        }

        // update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        m_mediaPlayer.reset();
        initMediaPlayer();
    }

    private void skipToPrevious() {
        if (audioIndex == 0) {
            //if first in playlist
            //set index to the last of audioList
            audioIndex = audioList.size() - 1;
            activeAudio = audioList.get(audioIndex);
        } else {
            //get previous in playlist
            activeAudio = audioList.get(--audioIndex);
        }

        //Update stored index
        new StorageUtil(getApplicationContext()).storeAudioIndex(audioIndex);

        stopMedia();
        //reset mediaPlayer
        m_mediaPlayer.reset();
        initMediaPlayer();
    }

    private void registerBecomingNoisyReceiver() {
        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(becomingNoisyReciever, intentFilter);
    }

    private void buildNotification(PlaybackStatus playbackStatus) {
        int notificationAction = android.R.drawable.ic_media_pause;
        PendingIntent play_pauseAction = null;

        // Build a new notification according to the current state of the MediaPlayer
        if (playbackStatus == PlaybackStatus.PLAYING) {
            notificationAction = android.R.drawable.ic_media_pause;
            play_pauseAction = playbackAction(1);
        } else if (playbackStatus == PlaybackStatus.PAUSED) {
            notificationAction = android.R.drawable.ic_media_play;
            play_pauseAction = playbackAction(0);
        }

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.albumart); // TODO: replace with album art

        android.support.v7.app.NotificationCompat.Builder notificationBuilder  = (android.support.v7.app.NotificationCompat.Builder) new android.support.v7.app.NotificationCompat.Builder(this)
                .setShowWhen(false)
                .setStyle(new android.support.v7.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.getSessionToken()).setShowActionsInCompactView(0, 1, 2))
                .setColor(getResources().getColor(R.color.primaryColor))
                .setLargeIcon(largeIcon)
                .setSmallIcon(android.R.drawable.stat_sys_headset)
                .setContentText(activeAudio.getArtist())
                .setContentTitle(activeAudio.getTitle())
                .setContentInfo(activeAudio.getAlbum())
                .addAction(android.R.drawable.ic_media_previous, "previous", playbackAction(3))
                .addAction(notificationAction, "pause", play_pauseAction)
                .addAction(android.R.drawable.ic_media_next, "next", playbackAction(2));

        assert getSystemService(Context.NOTIFICATION_SERVICE) != null;
        //noinspection ConstantConditions
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notificationBuilder.build());
    }

    private void removeNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private PendingIntent playbackAction(int actionNumber) {
        Intent playbackAction = new Intent(this, MediaPlayerService.class);
        switch (actionNumber) {
            case 0:
                // Play
                playbackAction.setAction(ACTION_PLAY);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 1:
                // Pause
                playbackAction.setAction(ACTION_PAUSE);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 2:
                // Next track
                playbackAction.setAction(ACTION_NEXT);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            case 3:
                // Previous track
                playbackAction.setAction(ACTION_PREVIOUS);
                return PendingIntent.getService(this, actionNumber, playbackAction, 0);
            default:
                break;
        }
        return null;
    }

    private void handleincomingActions(Intent playbackAction) {
        if (playbackAction == null || playbackAction.getAction() == null) return;

        String actionString = playbackAction.getAction();
        if (actionString.equalsIgnoreCase(ACTION_PLAY)) {
            transportControls.play();
        } else if (actionString.equalsIgnoreCase(ACTION_PAUSE)) {
            transportControls.pause();
        } else if (actionString.equalsIgnoreCase(ACTION_NEXT)) {
            transportControls.skipToNext();
        } else if (actionString.equalsIgnoreCase(ACTION_PREVIOUS)) {
            transportControls.skipToPrevious();
        } else if (actionString.equalsIgnoreCase(ACTION_STOP)) {
            transportControls.stop();
        }
    }


    private void initMediaPlayer() {
        if (m_mediaPlayer == null) {
            m_mediaPlayer = new MediaPlayer();
        }
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
            m_mediaPlayer.setDataSource(activeAudio.getData());
        } catch (IOException e) {
            e.printStackTrace();
            stopSelf();
        }
        m_mediaPlayer.prepareAsync();
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
        //// Invoked when playback of a media source has completed
        //stopMedia();
        //removeNotification();
        //// Stop the service
        //stopSelf();

        // Play next song
        // TODO: make this play the next song based on artist rather than just the database of music; maybe using an interface?
        skipToNext();
        buildNotification(PlaybackStatus.PLAYING);
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

    @SuppressWarnings("UnusedReturnValue")
    private boolean removeAudioFocus() {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED == m_audioManager.abandonAudioFocus(this);
    }

}
