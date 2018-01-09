package com.sonelli.juicessh.performancemonitor.controllers;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.sonelli.juicessh.performancemonitor.R;
import com.sonelli.juicessh.pluginlibrary.exceptions.ServiceNotConnectedException;
import com.sonelli.juicessh.pluginlibrary.listeners.OnSessionExecuteListener;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TempController extends BaseController {

    public static final String TAG = "TempController";

    public TempController(Context context) {
        super(context);
    }

    @Override
    public BaseController start() {
        super.start();

        // Work out the temperature of the CPU
        // Function added by Emilio Dalla Torre @ 08 jan 2018

        final Pattern tempPattern = Pattern.compile("....."); // Heavy cpu so do out of loops.

        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {

                try {

                    getPluginClient().executeCommandOnSession(getSessionId(), getSessionKey(), "cat /sys/class/thermal/thermal_zone0/temp", new OnSessionExecuteListener() {
                        @Override
                        public void onCompleted(int exitCode) {
                            switch(exitCode){
                                case 127:
                                    setText(getString(R.string.error));
                                    Log.d(TAG, "Tried to run a command but the command was not found on the server");
                                    break;
                            }
                        }
                        @Override
                        public void onOutputLine(String line) {
                            Matcher tempMatcher = tempPattern.matcher(line);
                            if(tempMatcher.find()){
                                int tempInt = Integer.parseInt(tempMatcher.group(0));
                                DecimalFormat decimalFormat = new DecimalFormat("##,###");
                                String temp = decimalFormat.format(tempInt);
                                setText(temp + "° C");
                            }
                        }

                        @Override
                        public void onError(int error, String reason) {
                            toast(reason);
                        }
                    });
                } catch (ServiceNotConnectedException e){
                    Log.d(TAG, "Tried to execute a command but could not connect to JuiceSSH plugin service");
                }

                if(isRunning()){
                    handler.postDelayed(this, INTERVAL_SECONDS * 1000L);
                }
            }


        });

        return this;

    }

}
