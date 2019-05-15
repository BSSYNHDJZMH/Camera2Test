package com.example.mhzhaog.camera2test;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.example.mhzhaog.camera2test.camera.CameraNew;
import com.example.mhzhaog.camera2test.camera.CameraOld;
import com.example.mhzhaog.camera2test.camera.CameraSupport;
import com.example.mhzhaog.camera2test.listener.CameraControllerListener;
import com.example.mhzhaog.camera2test.listener.CameraEventListener;
import com.example.mhzhaog.camera2test.listener.OnCameraCallbackListener;
import com.example.mhzhaog.camera2test.model.FileItem;
import com.example.mhzhaog.camera2test.model.VideoItem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import pub.devrel.easypermissions.EasyPermissions;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements CameraEventListener, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private final int RC_CAMERA = 0x0002;
	private Button buttonCamera;

    private CameraSupport mCameraSupport;
    private int mCameraId = -1;
    private boolean mIsBackCamera = true;
    private boolean mIsFullScreen = false;
    private Context mContext;

    private OnCameraCallbackListener mCameraListener;

    private FrameLayout mCameraFl;
    private TextureView mTextureView;
    private ImageButton mCaptureBtn;
    private ImageButton mCloseBtn;
    private ImageButton mSwitchCameraBtn;
    private ImageButton mFullScreenBtn;
    private ImageButton mRecordVideoBtn;

    private int mWidth;
    private int mHeight;
    private int mSoftKeyboardHeight;
    public static int sMenuHeight = 831;
    // To judge if it is record video mode
    private boolean mIsRecordVideoMode = false;

    // To judge if it is recording video now
    private boolean mIsRecordingVideo = false;

    // To judge if is finish recording video
    private boolean mFinishRecordingVideo = false;

    // Video file to be saved at
    private String mVideoFilePath;
    private MediaPlayer mMediaPlayer = new MediaPlayer();

    public void setmCameraControllerListener(CameraControllerListener mCameraControllerListener) {
        this.mCameraControllerListener = mCameraControllerListener;
    }

    private CameraControllerListener mCameraControllerListener;

    @Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		buttonCamera = findViewById(R.id.buttonCamera);

        mCameraFl = (FrameLayout) findViewById(R.id.aurora_fl_camera_container);
        mTextureView = (TextureView) findViewById(R.id.aurora_txtv_camera_texture);
        mCloseBtn = (ImageButton) findViewById(R.id.aurora_ib_camera_close);
        mFullScreenBtn = (ImageButton) findViewById(R.id.aurora_ib_camera_full_screen);
        mRecordVideoBtn = (ImageButton) findViewById(R.id.aurora_ib_camera_record_video);
        mCaptureBtn = (ImageButton) findViewById(R.id.aurora_ib_camera_capture);
        mSwitchCameraBtn = (ImageButton) findViewById(R.id.aurora_ib_camera_switch);

        mCloseBtn.setOnClickListener(this);
        mFullScreenBtn.setOnClickListener(this);
        mRecordVideoBtn.setOnClickListener(this);
        mCaptureBtn.setOnClickListener(this);
        mSwitchCameraBtn.setOnClickListener(this);

        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });

        DisplayMetrics dm = getResources().getDisplayMetrics();
        mWidth = dm.widthPixels;
        mHeight = dm.heightPixels;

        buttonCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] perms = new String[] {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO };

                if (!EasyPermissions.hasPermissions(MainActivity.this, perms))
                {
                    EasyPermissions.requestPermissions(MainActivity.this, "app需要相机权限",
                            RC_CAMERA, perms);
                }
                else
                {

//			File rootDir = getFilesDir();
//			String fileDir = rootDir.getAbsolutePath() + "/photo";
//			chatInputView.setCameraCaptureFile(fileDir,
//					new SimpleDateFormat("yyyy-MM-dd-hhmmss",
//							Locale.getDefault()).format(new Date()));
                    mCameraFl.setVisibility(VISIBLE);
                    buttonCamera.setVisibility(GONE);
                    initCamera();

                }
            }
        });

	}

    public void initCamera() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            mCameraSupport = new CameraNew(this, mTextureView);
//        } else {
            mCameraSupport = new CameraOld(this, mTextureView);
//        }
        ViewGroup.LayoutParams params = mTextureView.getLayoutParams();
        params.height = mSoftKeyboardHeight == 0 ? sMenuHeight : mSoftKeyboardHeight;
        mTextureView.setLayoutParams(params);
        Log.e(TAG, "TextureView height: " + mTextureView.getHeight());
        mCameraSupport.setCameraCallbackListener(mCameraListener);
        mCameraSupport.setCameraEventListener(this);
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i;
                mIsBackCamera = true;
                break;
            }
        }
        if (mTextureView.isAvailable()) {
            mCameraSupport.open(mCameraId, mWidth, sMenuHeight, mIsBackCamera, 0.5f);
        } else {
            mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
                    Log.d("ChatInputView", "Opening camera");
                    if (mCameraSupport == null) {
                        initCamera();
                    } else {
                        mCameraSupport.open(mCameraId, width, height, mIsBackCamera, 0.5f);
                    }

                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int width, int height) {
                    Log.d("ChatInputView", "Texture size changed, Opening camera");
                    if (mTextureView.getVisibility() == VISIBLE && mCameraSupport != null) {
                        mCameraSupport.open(mCameraId, width, height, mIsBackCamera, 0.5f);
                    }
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
                    if (null != mCameraSupport) {
                        mCameraSupport.release();
                    }
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

                }
            });
        }
    }

    @Override
    public void onFinishTakePicture() {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.aurora_ib_camera_full_screen) {
            // full screen/recover screen button in texture view
            if (!mIsFullScreen) {
                if (mCameraControllerListener != null) {
                    mCameraControllerListener.onFullScreenClick();
                }
                fullScreen();
            } else {
                if (mCameraControllerListener != null) {
                    mCameraControllerListener.onRecoverScreenClick();
                }
                recoverScreen();
            }

        }else if (view.getId() == R.id.aurora_ib_camera_record_video) {
            // click record video button
            // if it is not record video mode
            if (mCameraControllerListener != null) {
                mCameraControllerListener.onSwitchCameraModeClick(!mIsRecordVideoMode);
            }
            if (!mIsRecordVideoMode) {
                mIsRecordVideoMode = true;
                mCaptureBtn.setBackgroundResource(R.drawable.aurora_preview_record_video_start);
                mRecordVideoBtn.setBackgroundResource(R.drawable.aurora_preview_camera);
                fullScreen();
                mCloseBtn.setVisibility(VISIBLE);
            } else {
                mIsRecordVideoMode = false;
                mRecordVideoBtn.setBackgroundResource(R.drawable.aurora_preview_record_video);
                mCaptureBtn.setBackgroundResource(R.drawable.aurora_menuitem_send_pres);
                mFullScreenBtn.setBackgroundResource(R.drawable.aurora_preview_recover_screen);
                mFullScreenBtn.setVisibility(VISIBLE);
                mCloseBtn.setVisibility(GONE);
            }

        }else if (view.getId() == R.id.aurora_ib_camera_capture) {
            // click capture button in preview camera view
            // is record video mode
            if (mIsRecordVideoMode) {
                if (!mIsRecordingVideo) { // start recording
                    mCameraSupport.startRecordingVideo();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mCaptureBtn.setBackgroundResource(R.drawable.aurora_preview_record_video_stop);
                            mRecordVideoBtn.setVisibility(GONE);
                            mSwitchCameraBtn.setVisibility(GONE);
                            mCloseBtn.setVisibility(VISIBLE);
                        }
                    }, 200);
                    mIsRecordingVideo = true;

                } else { // finish recording
                    mVideoFilePath = mCameraSupport.finishRecordingVideo();
                    mIsRecordingVideo = false;
                    mIsRecordVideoMode = false;
                    mFinishRecordingVideo = true;
                    mCaptureBtn.setBackgroundResource(R.drawable.aurora_menuitem_send_pres);
                    mRecordVideoBtn.setVisibility(GONE);
                    mSwitchCameraBtn.setBackgroundResource(R.drawable.aurora_preview_delete_video);
                    mSwitchCameraBtn.setVisibility(VISIBLE);
                    if (mVideoFilePath != null) {
                        playVideo();
                    }
                }
                // if finished recording video, send it
            } else if (mFinishRecordingVideo) {
                if ( mVideoFilePath != null) {
                    File file = new File(mVideoFilePath);
                    VideoItem video = new VideoItem(mVideoFilePath, file.getName(), file.length() + "",
                            System.currentTimeMillis() + "", mMediaPlayer.getDuration() / 1000);
                    List<FileItem> list = new ArrayList<>();
                    list.add(video);
//                    mListener.onSendFiles(list);
                    mVideoFilePath = null;
                }
                mFinishRecordingVideo = false;
                mMediaPlayer.stop();
                mMediaPlayer.release();
                recoverScreen();
//                dismissMenuLayout();
                // take picture and send it
            } else {
                mCameraSupport.takePicture();
            }
        } else if (view.getId() == R.id.aurora_ib_camera_close) {
            try {
                if (mCameraControllerListener != null) {
                    mCameraControllerListener.onCloseCameraClick();
                }
                mMediaPlayer.stop();
                mMediaPlayer.release();
                if (mCameraSupport != null) {
                    mCameraSupport.cancelRecordingVideo();
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            recoverScreen();
//            dismissMenuLayout();
            mIsRecordVideoMode = false;
            mIsRecordingVideo = false;
            if (mFinishRecordingVideo) {
                mFinishRecordingVideo = false;
            }
        }else if (view.getId() == R.id.aurora_ib_camera_switch) {
            if (mFinishRecordingVideo) {
                mCameraSupport.cancelRecordingVideo();
                mSwitchCameraBtn.setBackgroundResource(R.drawable.aurora_preview_switch_camera);
                mRecordVideoBtn.setBackgroundResource(R.drawable.aurora_preview_camera);
                showRecordVideoBtn();

                mVideoFilePath = null;
                mFinishRecordingVideo = false;
                mIsRecordVideoMode = true;
                mCaptureBtn.setBackgroundResource(R.drawable.aurora_preview_record_video_start);
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mCameraSupport.open(mCameraId, mWidth, mHeight, mIsBackCamera, 0.5f);
            } else {
                for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
                    Camera.CameraInfo info = new Camera.CameraInfo();
                    Camera.getCameraInfo(i, info);
                    if (mIsBackCamera) {
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            mCameraId = i;
                            mIsBackCamera = false;
                            mCameraSupport.release();
                            mCameraSupport.open(mCameraId, mTextureView.getWidth(), mTextureView.getHeight(),
                                    mIsBackCamera, 0.5f);
                            break;
                        }
                    } else {
                        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            mCameraId = i;
                            mIsBackCamera = true;
                            mCameraSupport.release();
                            mCameraSupport.open(mCameraId, mTextureView.getWidth(), mTextureView.getHeight(),
                                    mIsBackCamera, 0.5f);
                            break;
                        }
                    }
                }
            }
        }

    }

    private void playVideo() {
            try {
                mCameraSupport.release();
                mMediaPlayer = new MediaPlayer();
                mMediaPlayer.setDataSource(mVideoFilePath);
                Surface surface = new Surface(mTextureView.getSurfaceTexture());
                mMediaPlayer.setSurface(surface);
                surface.release();
                mMediaPlayer.setLooping(true);
                mMediaPlayer.prepareAsync();
                mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mediaPlayer) {
                        mediaPlayer.start();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    /**
     * Full screen mode
     */
    private void fullScreen() {
        // hide top status bar
//        Activity activity = (Activity) getContext();
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        getWindow().setAttributes(attrs);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        mFullScreenBtn.setBackgroundResource(R.drawable.aurora_preview_recover_screen);
        mFullScreenBtn.setVisibility(VISIBLE);
        int height = mHeight;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = getWindowManager().getDefaultDisplay();
            DisplayMetrics dm = getResources().getDisplayMetrics();
            display.getRealMetrics(dm);
            height = dm.heightPixels;
        }
        ViewGroup.MarginLayoutParams marginParams1 = new ViewGroup.MarginLayoutParams(mCaptureBtn.getLayoutParams());
        marginParams1.setMargins(marginParams1.leftMargin, marginParams1.topMargin, marginParams1.rightMargin,
                dp2px(40));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(marginParams1);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER;
        mCaptureBtn.setLayoutParams(params);

        ViewGroup.MarginLayoutParams marginParams2 = new ViewGroup.MarginLayoutParams(mRecordVideoBtn.getLayoutParams());
        marginParams2.setMargins(dp2px(20), marginParams2.topMargin, marginParams2.rightMargin, dp2px(48));
        FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(marginParams2);
        params2.gravity = Gravity.BOTTOM | Gravity.START;
        mRecordVideoBtn.setLayoutParams(params2);

        ViewGroup.MarginLayoutParams marginParams3 = new ViewGroup.MarginLayoutParams(mSwitchCameraBtn.getLayoutParams());
        marginParams3.setMargins(marginParams3.leftMargin, marginParams3.topMargin, dp2px(20), dp2px(48));
        FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(marginParams3);
        params3.gravity = Gravity.BOTTOM | Gravity.END;
        mSwitchCameraBtn.setLayoutParams(params3);

//        mMenuContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, height));
//        mMenuContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mTextureView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        mIsFullScreen = true;
    }

    public int dp2px(float value) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    /**
     * Recover screen
     */
    private void recoverScreen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WindowManager.LayoutParams attrs = getWindow().getAttributes();
                attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getWindow().setAttributes(attrs);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                mIsFullScreen = false;
                mIsRecordingVideo = false;
                mIsRecordVideoMode = false;
                mCloseBtn.setVisibility(GONE);
                mFullScreenBtn.setBackgroundResource(R.drawable.aurora_preview_full_screen);
                mFullScreenBtn.setVisibility(VISIBLE);
//                mChatInputContainer.setVisibility(VISIBLE);
//                mMenuItemContainer.setVisibility(isShowBottomMenu()?VISIBLE:GONE);
                int height = sMenuHeight;
                if (mSoftKeyboardHeight != 0) {
                    height = mSoftKeyboardHeight;
                }
//                setMenuContainerHeight(height);
                ViewGroup.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        height);
                mTextureView.setLayoutParams(params);
                mRecordVideoBtn.setBackgroundResource(R.drawable.aurora_preview_record_video);
                showRecordVideoBtn();
                mSwitchCameraBtn.setBackgroundResource(R.drawable.aurora_preview_switch_camera);
                mSwitchCameraBtn.setVisibility(VISIBLE);
                mCaptureBtn.setBackgroundResource(R.drawable.aurora_menuitem_send_pres);

                ViewGroup.MarginLayoutParams marginParams1 = new ViewGroup.MarginLayoutParams(mCaptureBtn.getLayoutParams());
                marginParams1.setMargins(marginParams1.leftMargin, marginParams1.topMargin, marginParams1.rightMargin,
                        dp2px(12));
                FrameLayout.LayoutParams params1 = new FrameLayout.LayoutParams(marginParams1);
                params1.gravity = Gravity.BOTTOM | Gravity.CENTER;
                mCaptureBtn.setLayoutParams(params1);

                ViewGroup.MarginLayoutParams marginParams2 = new ViewGroup.MarginLayoutParams(mRecordVideoBtn.getLayoutParams());
                marginParams2.setMargins(dp2px(20), marginParams2.topMargin, marginParams2.rightMargin, dp2px(20));
                FrameLayout.LayoutParams params2 = new FrameLayout.LayoutParams(marginParams2);
                params2.gravity = Gravity.BOTTOM | Gravity.START;
                mRecordVideoBtn.setLayoutParams(params2);

                ViewGroup.MarginLayoutParams marginParams3 = new ViewGroup.MarginLayoutParams(mSwitchCameraBtn.getLayoutParams());
                marginParams3.setMargins(marginParams3.leftMargin, marginParams3.topMargin, dp2px(20), dp2px(20));
                FrameLayout.LayoutParams params3 = new FrameLayout.LayoutParams(marginParams3);
                params3.gravity = Gravity.BOTTOM | Gravity.END;
                mSwitchCameraBtn.setLayoutParams(params3);
            }
        });
    }

    public void showRecordVideoBtn(){
        if(mRecordVideoBtn.getTag()!=null && mRecordVideoBtn.getTag() instanceof String && ((String)mRecordVideoBtn.getTag()).equals("GONE")){
            mRecordVideoBtn.setVisibility(GONE);
        }else {
            mRecordVideoBtn.setVisibility(VISIBLE);
        }

    }
}
