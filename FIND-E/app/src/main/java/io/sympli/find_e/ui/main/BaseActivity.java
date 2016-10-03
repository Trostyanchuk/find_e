package io.sympli.find_e.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import javax.inject.Inject;

import io.sympli.find_e.event.BluetoothAvailableEvent;
import io.sympli.find_e.event.PermissionsGrantResultEvent;
import io.sympli.find_e.services.IBluetoothManager;
import io.sympli.find_e.services.IBroadcast;
import io.sympli.find_e.ui.widget.parallax.FloatArrayEvaluator;
import io.sympli.find_e.ui.widget.parallax.SensorAnalyzer;

public class BaseActivity extends AppCompatActivity implements SensorEventListener  {

    private FloatArrayEvaluator evaluator = new FloatArrayEvaluator(2);
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorAnalyzer sensorAnalyzer;

    @Inject
    IBroadcast broadcast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        sensorAnalyzer = new SensorAnalyzer(evaluator);
        final int rotation = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay().getRotation();
        sensorAnalyzer.remapAxis(rotation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        broadcast.register(this);
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        broadcast.unregister(this);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float[] newOffsetItems = sensorAnalyzer.normalizeAxisDueToEvent(sensorEvent);
        if (newOffsetItems != null) {
            broadcast.postEvent(new io.sympli.find_e.event.SensorEvent(newOffsetItems[0], newOffsetItems[1]));
            //TODO post event about it
//            bindingObject.circlesBg.setOffset(newOffsetItems[0], newOffsetItems[1]);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (!(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            broadcast.postStickyEvent(new PermissionsGrantResultEvent(false, permissions));
        } else {
            broadcast.postStickyEvent(new PermissionsGrantResultEvent(true, permissions));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case IBluetoothManager.REQUEST_ENABLE_BT: {
                broadcast.postEvent(new BluetoothAvailableEvent(resultCode == RESULT_OK));
                break;
            }
        }
    }
}
