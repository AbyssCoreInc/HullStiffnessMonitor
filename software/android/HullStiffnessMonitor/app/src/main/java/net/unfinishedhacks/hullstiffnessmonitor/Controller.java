package net.unfinishedhacks.hullstiffnessmonitor;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;

class Controller{
    private static final String TAG = Controller.class.getCanonicalName();
    public Handler updateConversationHandler;

    MainActivity mActivity;
    private long lastMsgTS;
    private ServerThread serverThreadObject;
    Thread serverThread = null;

    public Controller(MainActivity activity)
    {
        mActivity = activity;
    }

    public Context getContext()
    {
        return mActivity.getApplicationContext();
    }

    void updateBatV(double voltage)
    {

        Log.d(TAG, "Battery voltage: "+voltage);
    }

    void updateStatus(int stat)
    {
        Log.d(TAG, "Probe Status: "+stat);
    }

    void updateAccVector(double x, double y, double z)
    {
        Log.d(TAG, "Acceleration: "+x+","+y+","+z);
        mActivity.updateAcc(x,y,z);

    }

    void updateDotPosition(double x, double y)
    {
        Log.d(TAG, "Dot: "+x+","+y);
        mActivity.updateDot(x,y);
    }
    void updateBatvoltage(double v)
    {
        Log.d(TAG, "Voltage: "+v);
        mActivity.updateVoltage(v);
    }

    public void showMessage(String read) {
        String[] separated = read.split(":");
        this.lastMsgTS = (int) (System.currentTimeMillis());
        if (separated.length > 4)
        {
            updateAccVector(Double.parseDouble(separated[0]),Double.parseDouble(separated[1]),Double.parseDouble(separated[2]));
            updateDotPosition(Double.parseDouble(separated[3]),Double.parseDouble(separated[4]));
            updateBatvoltage(Double.parseDouble(separated[5]));
            updateCalibrateStatus(Integer.parseInt(separated[6]));
            mActivity.updateDataSet(Double.parseDouble(separated[1]),Double.parseDouble(separated[2]),Double.parseDouble(separated[3]));
        }

    }

    private void updateCalibrateStatus(int i) {
        Log.d(TAG, "Calibrate: "+i);
        mActivity.updateCalibrate(i);
    }

    public double timeSinceLastMsg() {
        return ((int) (System.currentTimeMillis())-this.lastMsgTS)/1000;
    }

    public void resetComms() {
        stopServerThread();
        startServerThread();
    }

    public void startServerThread() {
        this.serverThreadObject = new ServerThread(this);
        while(!serverThreadObject.initCommSuccess())
        {
            System.out.print("initializing resolve listener");
            serverThreadObject.initializeResolveListener();
            try {
                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        this.serverThread = new Thread(this.serverThreadObject);
        this.serverThread.start();
    }

    public void stopServerThread() {
        serverThread.interrupt();
        serverThreadObject.close();
    }

    public void startCalibrartion() {
        System.out.println("Contoller.startCalibrartion");
        serverThreadObject.writeCalibrateToProbe(1);
    }

    public void stopCalibration() {
        System.out.println("Contoller.stopCalibration");
        serverThreadObject.writeCalibrateToProbe(0);
    }
}
