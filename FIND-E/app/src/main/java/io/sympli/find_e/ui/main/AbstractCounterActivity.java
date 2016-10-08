package io.sympli.find_e.ui.main;

import android.support.v7.app.AppCompatActivity;

import io.sympli.find_e.ApplicationController;

public class AbstractCounterActivity extends AppCompatActivity {

    @Override
    protected void onStart() {
        super.onStart();
        ApplicationController.increaseActivityCounter();
    }

    @Override
    protected void onStop() {
        super.onStop();
        ApplicationController.decreaseActivityCounter();
    }
}
