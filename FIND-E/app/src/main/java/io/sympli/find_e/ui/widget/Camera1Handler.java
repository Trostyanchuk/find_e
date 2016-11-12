package io.sympli.find_e.ui.widget;

import android.hardware.Camera;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.List;

import io.sympli.find_e.R;
import io.sympli.find_e.utils.CameraUtil;

public class Camera1Handler {

    public enum CAMERA_STATE {
        IDLE, OPENING, OPEN, CLOSING, ERROR
    }

    private int mResolutionWidth;
    private int mResolutionHeight;
    private int mCameraId;
    private Camera mCamera;
    private CameraPreview mPreview;
    private OpenCameraTask mOpenCameraTask;
    private CAMERA_STATE mCurrCameraState = CAMERA_STATE.IDLE;
    private CAMERA_STATE mRequestedCameraState;

    private CameraStateListener cameraStateListener;

    public Camera1Handler(int cameraId) {
        List<Camera.Size> sizes = CameraUtil.getSupportedVideoSizesForBackCamera();
        if (sizes != null && sizes.size() > 0) {
            mResolutionWidth = sizes.get(0).width;
            mResolutionHeight = sizes.get(0).height;
        }
        this.mCameraId = cameraId;
    }

    public void setup(View rootView, CameraStateListener listener) {
        cameraStateListener = listener;
        mPreview = new CameraPreview(rootView.getContext(), mResolutionWidth, mResolutionHeight);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        ((RelativeLayout) rootView.findViewById(R.id.camera_preview)).addView(mPreview, 0, params);
        mRequestedCameraState = null;
    }

    public void openCamera() {
        if (mCurrCameraState != CAMERA_STATE.IDLE) {
            mRequestedCameraState = CAMERA_STATE.OPEN;
            setCameraState(CAMERA_STATE.OPEN);
            return;
        }
        cancelCameraTask();
        if (mCamera == null) {
            mOpenCameraTask = new OpenCameraTask();
            mOpenCameraTask.execute();
        }
    }

    public void closeCamera() {
        if (mCurrCameraState != CAMERA_STATE.OPEN) {
            mRequestedCameraState = CAMERA_STATE.IDLE;
            return;
        }
        setCameraState(CAMERA_STATE.CLOSING);
        cancelCameraTask();
        new Thread(new CameraCloseRunnable()).start();
    }

    public void release() {

    }

    public boolean isCameraPrepared() {
        return mCamera != null && mCurrCameraState == CAMERA_STATE.OPEN;
    }

    public void makePicture(Camera.PictureCallback callback) {
        mCamera.takePicture(null, null, callback);
    }

    private void setCameraState(CAMERA_STATE newState) {
        if (mCurrCameraState == newState) {
            notifyListener();
            return;
        }
        mCurrCameraState = newState;
        if (mRequestedCameraState != null &&
                mRequestedCameraState != mCurrCameraState &&
                (mCurrCameraState != CAMERA_STATE.OPENING &&
                        mCurrCameraState != CAMERA_STATE.CLOSING)) {
            switch (mRequestedCameraState) {
                case OPEN:
                    openCamera();
                    break;
                case IDLE:
                    closeCamera();
                    break;
            }
            mRequestedCameraState = null;
            return;
        }
        notifyListener();
    }

    private void notifyListener() {
        if (cameraStateListener != null)
            cameraStateListener.onStateChanged(mCurrCameraState);
    }

    private void cancelCameraTask() {
        if (mOpenCameraTask != null) {
            mOpenCameraTask.cancel(true);
            mOpenCameraTask = null;
        }
    }

    private class OpenCameraTask extends BaseAsyncTask<Void, Void, Void> {

        @Override
        public void onPreExecute() {
            setCameraState(CAMERA_STATE.OPENING);
        }

        @Override
        public void onResult(Void aVoid) {
            setCameraState(CAMERA_STATE.OPEN);
        }

        @Override
        public void onException(Exception e) {
            setCameraState(CAMERA_STATE.ERROR);
            if (mCamera != null) {
                closeCamera();
            }
        }

        @Override
        public Void performInBackground(Void... taskParams) throws Exception {
            mCamera = Camera.open(mCameraId);
            Camera.Parameters params = mCamera.getParameters();
            mCamera.setPreviewCallback(mPreview);
            if (mResolutionWidth == 0 || mResolutionHeight == 0) {
                setCameraState(CAMERA_STATE.ERROR);
            } else {
                params.setPreviewSize(mResolutionWidth, mResolutionHeight);
                mCamera.setParameters(params);

                mPreview.refreshCamera(mCamera);
            }
            return null;
        }
    }

    private class CameraCloseRunnable implements Runnable {
        @Override
        public void run() {
            if (mCamera != null) {
                mCamera.setPreviewCallback(null);
                mPreview.setCamera(null);
                mCamera.release();
                mCamera = null;
            }
            setCameraState(CAMERA_STATE.IDLE);
        }
    }

    public interface CameraStateListener {

        void onStateChanged(CAMERA_STATE state);
    }
}
