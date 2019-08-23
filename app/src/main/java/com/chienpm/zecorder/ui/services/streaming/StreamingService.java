package com.chienpm.zecorder.ui.services.streaming;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.widget.Toast;

import com.chienpm.zecorder.R;
import com.chienpm.zecorder.controllers.encoder.RenderUtil.CustomDecorator;
import com.chienpm.zecorder.controllers.streaming.StreamProfile;
import com.chienpm.zecorder.ui.utils.MyUtils;
import com.takusemba.rtmppublisher.Publisher;
import com.takusemba.rtmppublisher.PublisherListener;

import java.util.ArrayList;

public class StreamingService extends Service implements PublisherListener {
    private static final boolean DEBUG = false;	// TODO set false on release
    private final IBinder mIBinder = new StreamingBinder();

    private static final String TAG = "chienpm";
    private MediaProjectionManager mMediaProjectionManager;
    private MediaProjection mMediaProjection;
    private Intent mScreenCaptureIntent;
    private int mScreenCaptureResultCode;
    private int mScreenWidth, mScreenHeight, mScreenDensity;
    private Publisher mPublisher;
    private static final Object sSync = new Object();

    private StreamProfile mStreamProfile;

    private String url = "rtmp://live.restream.io/live/re_1565015_e4b94414a7288cfef172";//"rtmp://10.199.220.239/live/key";

    //Implement Publisher listener
    @Override
    public void onStarted() {
        Log.i(TAG, "onStarted");
        MyUtils.toast(getApplicationContext(), "Streaming Started", Toast.LENGTH_SHORT);
    }

    @Override
    public void onStopped() {
        Log.i(TAG, "onStopped");
        MyUtils.toast(getApplicationContext(), "Streaming Stopped", Toast.LENGTH_SHORT);
    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "onDisconnected");
        MyUtils.toast(getApplicationContext(), "Streaming Disconnected", Toast.LENGTH_SHORT);
    }

    @Override
    public void onFailedToConnect() {
        Log.i(TAG, "onFailedToConnect");
        MyUtils.toast(getApplicationContext(), "Streaming Connection Failed", Toast.LENGTH_SHORT);
    }

    public class StreamingBinder extends Binder{
        public StreamingService getService(){
            return StreamingService.this;
        }
    }

    public StreamingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(
                Context.MEDIA_PROJECTION_SERVICE);


    }

    private void getScreenSize() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        mScreenDensity = metrics.densityDpi;
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        if (width > height) {
            final float scale_x = width / 1920f;
            final float scale_y = height / 1080f;
            final float scale = Math.max(scale_x,  scale_y);
            width = (int)(width / scale);
            height = (int)(height / scale);
        } else {
            final float scale_x = width / 1080f;
            final float scale_y = height / 1920f;
            final float scale = Math.max(scale_x,  scale_y);
            width = (int)(width / scale);
            height = (int)(height / scale);
        }
        //just support landscape
        if(width>height)
        {
            mScreenWidth = width;
            mScreenHeight = height;
        }
        else {
            mScreenWidth = height;
            mScreenHeight = width;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "RecordingService: onBind()");
        mScreenCaptureIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
        mScreenCaptureResultCode = mScreenCaptureIntent.getIntExtra(MyUtils.SCREEN_CAPTURE_INTENT_RESULT_CODE, MyUtils.RESULT_CODE_FAILED);

        mStreamProfile = (StreamProfile) intent.getSerializableExtra(MyUtils.STREAM_PROFILE);

        getScreenSize();
        mMediaProjection = mMediaProjectionManager.getMediaProjection(mScreenCaptureResultCode, mScreenCaptureIntent);
        DisplayManager dm = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display defaultDisplay;
        if (dm != null) {
            defaultDisplay = dm.getDisplay(Display.DEFAULT_DISPLAY);
        } else {
            throw new IllegalStateException("Cannot display manager?!?");
        }
        if (defaultDisplay == null) {
            throw new RuntimeException("No display found.");
        }

        Log.d(TAG, "onBindStream: "+ mScreenCaptureIntent);
        Log.d(TAG, "onBindStream: "+ mStreamProfile.toString());
        return mIBinder;
    }

    public void startStreaming() {
        synchronized (sSync) {
            if(mPublisher==null) {
                if (DEBUG) Log.v(TAG, "startStreaming:");
                try {

                    mPublisher = new Publisher.Builder()
                            .setUrl(url)
                            .setSize(mScreenWidth, mScreenHeight)
                            .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
                            .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
                            .setDensity(mScreenDensity)
                            .setMediaProjection(mMediaProjection)
                            .setListener(this)
                            .build();
                    mPublisher.startPublishing();


                 } catch (final Exception e) {
                    Log.e(TAG, "startStreaming error:", e);
                }
            }else{
                mPublisher.startPublishing();
            }
        }

    }

    private ArrayList<CustomDecorator> createDecorators() {
        ArrayList<CustomDecorator> list = new ArrayList<>();

        //watermask
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.wartermark);

        list.add(new CustomDecorator(bitmap, new Size(240, 240), new Point(0, 0)));

        return list;
    }

    public void stopStreaming() {
        mPublisher.stopPublishing();
    }

}
