package com.sls.liteplayer;

import android.Manifest;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;

    private final String TAG = MainActivity.class.getSimpleName();

    private int mVideoWidth = 0;
    private int mVideoHeight = 0;

    private SLSSurfaceView mSurfaceView;

    //    private JNISrt mSrt = new JNISrt();
    //private String mPublishURL = "udp://224.5.5.5:5001";
//    private String mPublishURL = "srt://39.104.103.63:8080?streamid=uplive.sls.com/live/";
    private String mPublishURL = "srt://192.168.8.15:10080?streamid=#!::h=live/liteplayer,m=publish";
    //    private String mPlayURL = "srt://39.104.103.63:8080?streamid=live.sls.com/live/";
    private String mPlayURL = "srt://192.168.8.15:10080?streamid=#!::h=live/liteplayer,m=request";

    private SrsCameraView mCameraView;
    private SrsPublishManager mPublishManager = null;
    private SrsPlayManager mPlayManager = null;


    private static final int MY_PERMISSIONS_REQUEST = 1;
    private static final String[] MANDATORY_PERMISSIONS = {
            "android.permission.WRITE_EXTERNAL_STORAGE",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.hardware.camera",
            "android.hardware.camera.autofocus",
            "android.permission.ACCESS_NETWORK_STATE",
            "android.hardware.wifi",
            "android.permission.CAMERA",
            "android.permission.MODIFY_AUDIO_SETTINGS",
            "android.permission.RECORD_AUDIO",
            "android.permission.INTERNET",
            "android.permission.WAKE_LOCK"
    };


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    if (null == mPublishManager) {
                        mPublishManager = new SrsPublishManager();
                    }
                    mCameraView = (SrsCameraView) findViewById(R.id.camera_preview);
                    mPublishManager.setCameraView(mCameraView);
                    TextView localID = (TextView) findViewById(R.id.localID);
                    mPublishURL = localID.getText().toString();
                    mPublishManager.start(mPublishURL);
                    return true;
                case R.id.navigation_dashboard:

                    if (null == mPlayManager) {
                        mPlayManager = new SrsPlayManager();
                    }

                    mSurfaceView = (SLSSurfaceView) findViewById(R.id.video_surface);
                    mPlayManager.setSurfaceView(mSurfaceView);
                    TextView peerID = (TextView) findViewById(R.id.peerID);
                    mPlayURL = peerID.getText().toString();
                    mPlayManager.start(mPlayURL);

                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    if (mPublishManager != null)
                        mPublishManager.switchCameraFace();
                    return true;
            }
            return false;
        }
    };

    //length用户要求产生字符串的长度
    public static String getRandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int ret = 0;
        int sock = 0;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        //register
        //registerActivityLifecycleCallbacks(activityLifecycleCallbacks);

        // response screen rotation event
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        //check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST", "Granted");
            //init(barcodeScannerView, getIntent(), null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 1);//1 can be another integer
        }

        //check the file permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST", "Granted");
            //init(barcodeScannerView, getIntent(), null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);//1 can be another integer
        }
        //check audio record
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST", "Granted");
            //init(barcodeScannerView, getIntent(), null);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);//1 can be another integer
        }

//        mStreamName = getRandomString(5);
//        mPlayURL += mStreamName;
//        mPublishURL += mStreamName;

        //init url
        TextView peerID = (TextView) findViewById(R.id.peerID);
        peerID.setText(mPlayURL);
        TextView localID = (TextView) findViewById(R.id.localID);
        localID.setText(mPublishURL);

        //start timer
        startLogTimer();

        //audio aec
        AudioManager audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : MANDATORY_PERMISSIONS) {
                if (checkCallingOrSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("SDK", permission + " request ");
                    ActivityCompat.requestPermissions(this, new String[]{permission}, MY_PERMISSIONS_REQUEST);
                } else {
                    Log.d("SDK", permission + " success ");
                }
            }
        }


        //m_audio = new SLSAudioAec();
        //m_audio.StartRecorderAndPlayer();


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("TAG", "onStart");
        resetMedec();
    }

    @Override
    protected void onDestroy() {
// TODO Auto-generated method stub
        super.onDestroy();
        Log.d("TAG", "onDestroy");
        if (mPublishManager != null) {
            mPublishManager.stop();
        }
        if (mPlayManager != null) {
            mPlayManager.stop();
        }
    }

    @Override
    protected void onPause() {
// TODO Auto-generated method stub
        super.onPause();
        Log.d("TAG", "onPause");
    }

    @Override
    protected void onRestart() {
// TODO Auto-generated method stub
        super.onRestart();
        Log.d("TAG", "onRestart");
    }

    @Override
    protected void onResume() {
// TODO Auto-generated method stub
        super.onResume();
        Log.d("TAG", "onResume");
    }

    void startLogTimer() {
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                refreshLogInfo();
            }
        };
        timer.schedule(task, 0, 1000);
    }

    void refreshLogInfo() {
        TextView logInfo = (TextView) findViewById(R.id.debugInfoView);
        String log = String.format("publisher:\n");
        String logEncoded = String.format("  encoded duration: %d(ms)\n", mPublishManager == null ? 0 : mPublishManager.getEncodedDuration());
        log += logEncoded;
        log += "player:\n";
        String info = String.format("  video size: %s\n", mPlayManager != null ? mPlayManager.getVideoSize() : "");
        log += info;
        info = String.format("  net delay: %d(ms)\n", mPlayManager != null ? mPlayManager.getNetDelay() : 0);
        log += info;
        info = String.format("  decoded duration: %d(ms)\n", mPlayManager == null ? 0 : mPlayManager.getDecodedDuration());
        log += info;
        logInfo.setText(log);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.i(TAG, "onConfigurationChanged: newConfig.orientation=" + newConfig.orientation);

        resetMedec();
    }

    public void resetMedec() {
        if (mPlayManager != null) {
            mPlayManager.reset();
        }
        if (mPublishManager != null) {
            mPublishManager.reset();
        }
    }

}
