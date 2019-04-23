package com.example.mhzhaog.camera2test;

import android.Manifest;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.example.mhzhaog.camera2test.camera.CameraNew;
import com.example.mhzhaog.camera2test.camera.CameraOld;
import com.example.mhzhaog.camera2test.camera.CameraSupport;
import com.example.mhzhaog.camera2test.listener.CameraEventListener;
import com.example.mhzhaog.camera2test.listener.OnCameraCallbackListener;

import pub.devrel.easypermissions.EasyPermissions;

import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity implements CameraEventListener {

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
                    buttonCamera.setVisibility(View.GONE);
                    initCamera();

                }
            }
        });

	}

    public void initCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mCameraSupport = new CameraNew(this, mTextureView);
        } else {
            mCameraSupport = new CameraOld(this, mTextureView);
        }
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
}
