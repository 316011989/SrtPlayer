package com.sls.liteplayer.push;

import android.util.Log;

public class JniPush {
    private final String TAG = JniPush.class.getSimpleName();

    public JniPush() {
        System.loadLibrary("JNISrt");
        //System.loadLibrary("srt");
    }

    private long mSRT = 0;

    public boolean open(String url) {
        Log.i(TAG, "JNISRT: open" + url);
        mSRT = srtOpen(url);
        if (mSRT > 0)
            return true;
        return false;
    }

    public boolean close() {
        Log.i(TAG, "JNISRT: close");
        if (mSRT <= 0)
            return false;
        int ret = srtClose(mSRT);
        mSRT = 0;
        return ret == 0;
    }

    public int send(byte[] data) {
        if (mSRT <= 0)
            return 0;
        return srtSend(mSRT, data);
    }

    public int state() {
        if (mSRT <= 0)
            return -1;
        return srtGetSockState(mSRT);
    }

    public native int srtStartup();

    public native int srtCleanup();


    public native long srtOpen(String url);

    public native int srtClose(long srt);

    public native int srtSend(long srt, byte[] data);


    public native int srtGetSockState(long srt);

    //tools
    public static native int yv12RotationAnti(byte[] src, byte[] dst, int w, int h, int angle);

}
