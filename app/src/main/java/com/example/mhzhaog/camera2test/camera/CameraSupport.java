package com.example.mhzhaog.camera2test.camera;


import com.example.mhzhaog.camera2test.listener.CameraEventListener;
import com.example.mhzhaog.camera2test.listener.OnCameraCallbackListener;

public interface CameraSupport {
    CameraSupport open(int cameraId, int width, int height, boolean isFacingBack, float cameraQuality);
    void release();
    void takePicture();
    void setCameraCallbackListener(OnCameraCallbackListener listener);
    void setCameraEventListener(CameraEventListener listener);
    void startRecordingVideo();
    void cancelRecordingVideo();
    String finishRecordingVideo();
}
