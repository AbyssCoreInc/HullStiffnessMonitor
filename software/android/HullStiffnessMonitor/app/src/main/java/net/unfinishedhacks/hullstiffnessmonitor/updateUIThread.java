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
        String[] separated = msg.split(":");
        if (separated.length == 2)
        {
            this.interpretMessage(separated[0], separated[1]);
        }
        else if (separated.length > 4)
        {
            this.interpretAcceleration(separated[0], separated[1], separated[2], separated[3], separated[4]);
        }

    }

    private void interpretMessage(String key, String val) {
        if (key.compareTo("bat_v")==0)
        {
            this.controller.updateBatV(Double.parseDouble(val));
        }
        if (key.compareTo("meas_stat")==0)
        {
            this.controller.updateStatus(Integer.parseInt(val));
        }
    }

    private void interpretAcceleration(String x,String y, String z, String dot_x, String dot_y) {
        this.controller.updateAccVector(Double.parseDouble(x),Double.parseDouble(y),Double.parseDouble(z));
        this.controller.updateDotPosition(Double.parseDouble(dot_x),Double.parseDouble(dot_y));
    }
}