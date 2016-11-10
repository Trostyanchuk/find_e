package io.sympli.find_e.ui.widget;

import android.content.Context;
import android.hardware.Camera;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.IOException;

import io.sympli.find_e.utils.UIUtil;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback,
        Camera.PreviewCallback {
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private OnPreviewUpdateListener previewUpdateListener;
    private int mPreviewWidth, mPreviewHeight;
    private boolean isPreviewRunning;

    public CameraPreview(Context context) {
        this(context, 0, 0);
    }

    public CameraPreview(Context context, int previewWidth, int previewHeight) {
        super(context);
        mHolder = getHolder();
        mHolder.addCallback(this);
        mPreviewWidth = 720;
        mPreviewHeight = 1280;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (previewUpdateListener != null)
            previewUpdateListener.onUpdatePreview(data);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (mCamera != null) {
                mCamera.setPreviewDisplay(holder);
                mCamera.startPreview();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refreshCamera(Camera camera) {
        isPreviewRunning = true;
        if (mHolder.getSurface() == null) {
            return;
        }
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }
        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        setCamera(camera);
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCamera(Camera camera) {
        //method to set a camera instance
        mCamera = camera;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        // mCamera.release();
    }

    public CameraPreview setPreviewUpdateListener(OnPreviewUpdateListener previewUpdateListener) {
        this.previewUpdateListener = previewUpdateListener;
        return this;
    }

    public interface OnPreviewUpdateListener {
        void onUpdatePreview(byte[] previewData);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        if (mCamera == null) {
            return;
        }

        if (isPreviewRunning) {
            mCamera.stopPreview();
        }

        Camera.Parameters parameters = mCamera.getParameters();
        Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

        if (display.getRotation() == Surface.ROTATION_0) {
            parameters.setPreviewSize(mPreviewHeight, mPreviewWidth);
            mCamera.setDisplayOrientation(90);
        }

        if (display.getRotation() == Surface.ROTATION_90) {
            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
        }

        if (display.getRotation() == Surface.ROTATION_180) {
            parameters.setPreviewSize(mPreviewHeight, mPreviewWidth);
        }

        if (display.getRotation() == Surface.ROTATION_270) {
            parameters.setPreviewSize(mPreviewWidth, mPreviewHeight);
            mCamera.setDisplayOrientation(180);
        }

        mCamera.setParameters(parameters);
        refreshCamera(mCamera);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int actualWidth = UIUtil.getScreenWidth(getContext());
        final int actualHeight = UIUtil.getScreenHeight(getContext());

        if (mPreviewWidth > 0 && mPreviewHeight > 0) {
            float widthRatio = (float) actualWidth / mPreviewWidth;
            float heightRatio = (float) actualHeight / mPreviewHeight;

            float maxRatio = Math.min(widthRatio, heightRatio);

            int scaledWidth = (int) (mPreviewWidth * maxRatio);
            int scaledHeight = (int) (mPreviewHeight * maxRatio);
            setMeasuredDimension(scaledWidth, scaledHeight);
        }
    }
}