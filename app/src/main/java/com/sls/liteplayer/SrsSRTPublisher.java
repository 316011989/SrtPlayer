
package com.sls.liteplayer;

import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import com.sls.liteplayer.push.JniPush;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.FileNotFoundException;

//multicast
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * Srs implementation of an RTMP publisher
 *
 * @author francois, leoma
 */
public class SrsSRTPublisher extends SrsPublisher {

    //multicast
    private JniPush mSrt = new JniPush();
    private static final String TAG = "SrsSRTPublisher";
    private String mNetURL;
    private boolean mExit = false;
    private boolean mConnected = false;
    private static int mInitCount = -1;

    @Override
    public boolean open(String url) {
        mNetURL = url;
        init();
        super.open(url);
        mExit = false;
        if (!mConnected) {
            mConnected = mSrt.open(url);
        }
        return mConnected;
    }

    @Override
    public void close() {
        mExit = true;
        if (mSrt != null && mConnected) {
            mSrt.close();
            mConnected = false;
        }
        super.close();
        uninit();
        return;

    }

    @Override
    public int send(ByteBuffer data) {
        int ret = -1;
        if (mSrt != null) {
            byte[] dst = byteBuffer2Byte(data);
            ret = mSrt.send(dst);
            data.flip();
        }
        super.send(data);
        return ret;
    }


    @Override
    public int state() {
        if (mSrt != null) {
            return mSrt.state();
        }
        return -1;
    }

    private int init() {
        mInitCount++;
        if (mInitCount == 0) {
            int ret = mSrt.srtStartup();
            if (ret != 0) {
                Log.i(TAG, "srtStartup faild, ret" + ret);
                return -1;
            }
        }
        return mInitCount;
    }

    private int uninit() {
        if (mInitCount == -1) {
            return -1;
        }
        if (mInitCount > 0) {
            mInitCount--;
        }

        if (mInitCount == 0) {
            int ret = mSrt.srtCleanup();
            if (ret != 0) {
                Log.i(TAG, "srtStartup faild, ret" + ret);
                return -1;
            }
        }
        return mInitCount;
    }


}
