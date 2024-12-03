package com.example.actividadbackground;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MuscService audioService;
    private boolean isBound = false;

    private TextView tvTitle, tvProgress;
    private SeekBar seekBar;
    private Button btnPlayPause, btnStop;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvTitle = findViewById(R.id.tv_title);
        tvProgress = findViewById(R.id.tv_progress);
        seekBar = findViewById(R.id.seekBar);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnStop = findViewById(R.id.btn_stop);

        btnPlayPause.setOnClickListener(view -> {
            if (audioService.isPlaying()) {
                audioService.pauseAudio();
                btnPlayPause.setText("Play");
            } else {
                audioService.playAudio(R.raw.pista1);
                btnPlayPause.setText("Pause");
                updateSeekBar();
            }
        });

        btnStop.setOnClickListener(view -> {
            audioService.stopAudio();
            btnPlayPause.setText("Play");
        });

        bindService(new Intent(this, MuscService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MuscService.AudioBinder binder = ( MuscService.AudioBinder) service;
            audioService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    private void updateSeekBar() {
        handler.postDelayed(() -> {
            if (audioService != null && audioService.isPlaying()) {
                int currentPosition = audioService.getCurrentPosition();
                int duration = audioService.getDuration();

                seekBar.setMax(duration);
                seekBar.setProgress(currentPosition);

                tvProgress.setText(getFormattedTime(currentPosition) + " / " + getFormattedTime(duration));
                updateSeekBar();
            }
        }, 1000);
    }


    private String getFormattedTime(int millis) {
        int minutes = millis / 1000 / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (audioService != null) {
            audioService.stopForegroundService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (audioService != null && audioService.isPlaying()) {
            Intent intent = new Intent(this, MuscService.class);
            intent.setAction("START");
            startService(intent); // Reactiva la notificación
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
    }
}

