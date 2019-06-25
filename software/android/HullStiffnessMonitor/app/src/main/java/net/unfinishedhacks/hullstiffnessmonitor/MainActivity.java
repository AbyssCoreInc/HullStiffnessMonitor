package net.unfinishedhacks.hullstiffnessmonitor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObservable;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.util.SortedList;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 0x81;
    private static WifiManager wifiManager;
    private TextView mTextMessage;
    private TextView accX;
    private TextView accY;
    private TextView accZ;
    private TextView dotX;
    private TextView dotY;
    private TextView batV;
    private Button calibBtn;
    private DataView dataView;
    private int tracking;
    private int recording;
    private double dot_x;
    private double dot_y;
    private double acc_x;
    private double acc_y;
    private double acc_z;
    private double bat_v;
    private double height;

    private Controller controller;
    private Vector data;

    private WifiManager.LocalOnlyHotspotReservation mReservation;
    private final String TAG = "MainActivity";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void turnOnHotspot() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        manager.startLocalOnlyHotspot(new WifiManager.LocalOnlyHotspotCallback() {

            @Override
            public void onStarted(WifiManager.LocalOnlyHotspotReservation reservation) {
                super.onStarted(reservation);
                Log.d(TAG, "Wifi Hotspot is on now");
                mReservation = reservation;
            }

            @Override
            public void onStopped() {
                super.onStopped();
                Log.d(TAG, "onStopped: ");
            }

            @Override
            public void onFailed(int reason) {
                super.onFailed(reason);
                Log.d(TAG, "onFailed: ");
            }
        }, new Handler());
    }

    private void turnOffHotspot() {
        if (mReservation != null) {
            mReservation.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        height = 2000;
        data = new Vector();

        tracking = 0;
        recording = 0;
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        this.controller = new Controller(this);
        this.controller.updateConversationHandler = new Handler();


        calibBtn = (Button) findViewById(R.id.calib_btn);
        calibBtn.setOnClickListener(this);

        // seek permissions
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            // Permission has already been granted
        }

        turnOnHotspot();
        this.controller.startServerThread();
    }

    public void updateAcc(double x, double y, double z)
    {
        acc_x = x;
        acc_y = y;
        acc_z = z;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Stuff that updates the UI
                accX.setText(String.format("%.2f", acc_x));
                accY.setText(String.format("%.2f", acc_y));
                accZ.setText(String.format("%.2f", acc_z));
            }
        });

    }

    public void updateDot(double x, double y)
    {
        dot_x = x;
        dot_y = y;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dotX.setText(String.format("%.2f", dot_x));
                dotY.setText(String.format("%.2f", dot_y));
            }
        });
    }

    public void updateVoltage(double v)
    {
        bat_v = v;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                batV.setText(String.format("%.2f", bat_v));
            }
        });
    }

    public void updateDataSet(double a_y, double a_z, double d_x)
    {
        dataView.addData(new DataElement(a_y, a_z, d_x, height));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dataView.invalidate();
            }
        });
    }

    public void updateCalibrate(int stat)
    {
        tracking = stat;
        String text = "Start Calib";
        if (tracking == 1)
            text = "Stop Calibr";
        final String finalText = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calibBtn.setText(finalText);
            }
        });
    }
    public void setTestMessage(String text)
    {
        mTextMessage.setText(text);
    }

    @Override
    protected void onStop() {
        super.onStop();
        controller.stopServerThread();
    }


    @Override
    public void onClick(View view) {
        System.out.println("MainActivity.onClick");
        if(view.getId() == calibBtn.getId())
        {
            if (tracking == 0)
               this.controller.startCalibrartion();
            else
                this.controller.stopCalibration();
        }
    }

    public Controller getController() {
        return controller;
    }
}
