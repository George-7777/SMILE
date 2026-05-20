package com.example.smile.commands.commandsScripts.radio;


import android.content.Context;
import android.util.Log;

import androidx.annotation.OptIn;
import androidx.media3.common.MediaItem;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector;

public class RadioPlayer {
    private static final String TAG = "RadioPlayer";
    private static final String RADIO_URL = "https://rusradio.hostingradio.ru/rusradio128.mp3";

    private ExoPlayer player;
    private Context context;
    private RadioListener listener;
    private boolean isPlaying = false;

    public interface RadioListener {
        void onPlay();
        void onStop();
        void onError(String error);
        void onLoading(boolean isLoading);
    }

    public RadioPlayer(Context context) {
        this.context = context.getApplicationContext(); // Используем application context
        initPlayer();
    }

    public RadioPlayer(Context context, RadioListener listener) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        initPlayer();
    }

    @OptIn(markerClass = UnstableApi.class)
    private void initPlayer() {
        try {
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(context);
            trackSelector.setParameters(
                    trackSelector.getParameters().buildUpon()
                            .setMaxVideoSize(0, 0)
                            .build()
            );

            player = new ExoPlayer.Builder(context)
                    .setTrackSelector(trackSelector)
                    .build();

            MediaItem mediaItem = MediaItem.fromUri(RADIO_URL);
            player.setMediaItem(mediaItem);
            player.prepare();

            player.addListener(new androidx.media3.common.Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    switch (playbackState) {
                        case ExoPlayer.STATE_BUFFERING:
                            Log.d(TAG, "Буферизация...");
                            if (listener != null) listener.onLoading(true);
                            break;
                        case ExoPlayer.STATE_READY:
                            Log.d(TAG, "Готов к воспроизведению");
                            if (listener != null) listener.onLoading(false);
                            break;
                        case ExoPlayer.STATE_ENDED:
                            Log.d(TAG, "Воспроизведение завершено");
                            break;
                        case ExoPlayer.STATE_IDLE:
                            Log.d(TAG, "Простой");
                            break;
                    }
                }

                @Override
                public void onPlayerError(androidx.media3.common.PlaybackException error) {
                    Log.e(TAG, "Ошибка плеера: " + error.getMessage());
                    if (listener != null) {
                        listener.onError("Ошибка воспроизведения радио");
                    }
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Ошибка инициализации плеера", e);
            if (listener != null) {
                listener.onError("Не удалось инициализировать радио");
            }
        }
    }

    public void play() {
        if (player != null) {
            try {
                player.play();
                isPlaying = true;
                if (listener != null) listener.onPlay();
                Log.d(TAG, "Радио включено");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при воспроизведении", e);
                if (listener != null) listener.onError("Ошибка при воспроизведении");
            }
        }
    }

    public void stop() {
        if (player != null) {
            try {
                player.stop();
                isPlaying = false;
                if (listener != null) listener.onStop();
                Log.d(TAG, "Радио выключено");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при остановке", e);
            }
        }
    }

    public void pause() {
        if (player != null && isPlaying) {
            player.pause();
            isPlaying = false;
            Log.d(TAG, "Радио на паузе");
        }
    }

    public void release() {
        if (player != null) {
            try {
                player.stop();
                player.release();
                player = null;
                Log.d(TAG, "RadioPlayer освобожден");
            } catch (Exception e) {
                Log.e(TAG, "Ошибка при освобождении плеера", e);
            }
        }
    }

    public boolean isPlaying() {
        return isPlaying && player != null && player.isPlaying();
    }

    public boolean isInitialized() {
        return player != null;
    }

    public void setVolume(float volume) {
        if (player != null) {
            player.setVolume(volume);
        }
    }

    public void setListener(RadioListener listener) {
        this.listener = listener;
    }
}