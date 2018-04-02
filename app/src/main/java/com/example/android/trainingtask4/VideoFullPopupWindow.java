package com.example.android.trainingtask4;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.IOException;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

/**
 * Created by TQ on 22-Mar-18.
 */

public class VideoFullPopupWindow extends PopupWindow implements TextureView.SurfaceTextureListener {

    View view;
    Context mContext;
    TextureView textureView;
    MediaPlayer mp;
    View loadingBG;
    ProgressBar loadingVid;
    boolean videoLoaded;


    public VideoFullPopupWindow(Context context, int layout, View v, String videoUrl) {
        super(((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popup_video_full, null), ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(5.0f);
        }
        this.mContext = context;
        this.view = getContentView();
        ImageButton closeButton = this.view.findViewById(R.id.vid_close);
        setOutsideTouchable(true);

        setFocusable(true);
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                dismiss();
            }
        });

        videoLoaded = false;
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!videoLoaded) {
                    Toast.makeText(mContext, "ERROR: FAILED TO LOAD VIDEO", Toast.LENGTH_SHORT).show();
                    dismiss();
                }
            }
        }, 4500);

        loadingVid = this.view.findViewById(R.id.loading_vid);
        loadingBG = this.view.findViewById(R.id.black_loading_video_background);
        loadingVid.setVisibility(View.VISIBLE);
        loadingVid.setIndeterminate(true);
        loadingBG.setVisibility(View.VISIBLE);
        textureView = this.view.findViewById(R.id.video);
        textureView.setSurfaceTextureListener(this);

        mp = new MediaPlayer();
        try {
            mp.setDataSource(videoUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        showAtLocation(v, Gravity.CENTER, 0, 0);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        Surface surface = new Surface(surfaceTexture);

        mp.setSurface(surface);
        mp.prepareAsync();
        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                videoLoaded = true;
                loadingVid.setVisibility(View.GONE);
                loadingBG.setVisibility(View.GONE);
                mp.start();
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        dismiss();
                    }
                });
            }
        });
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void dismiss() {
        if (mp != null) {
            mp.stop();
            mp.release();
        }
        super.dismiss();
    }
}
