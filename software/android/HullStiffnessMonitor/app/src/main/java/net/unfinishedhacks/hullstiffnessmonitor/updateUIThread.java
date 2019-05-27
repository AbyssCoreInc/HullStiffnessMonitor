package net.unfinishedhacks.hullstiffnessmonitor;

import android.util.Log;

class updateUIThread implements Runnable {
    private static final String TAG = "updateUIThread";
    private final Controller controller;
    private String msg;

    public updateUIThread(String str, Controller ctrl) {
        this.controller = ctrl;
        this.msg = str;
    }

    @Override
    public void run() {
        //text.setText(text.getText().toString()+"Client Says: "+ msg + "\n");
        Log.d(TAG, "Client Says: "+ msg);
        
    }
}