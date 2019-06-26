package net.unfinishedhacks.hullstiffnessmonitor;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.text.format.Formatter;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import static android.content.Context.WIFI_SERVICE;

class ServerThread implements Runnable {

    private static final int SERVERPORT = 6001;
    private ServerSocket serverSocket;
    private Controller controller;
    private final String TAG = "ServerThread";

    private boolean initCommSuccess;
    private InetAddress probeHost;
    private int probePort;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public String mServiceName = "Hull Stiffness App";
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdServiceInfo mService;
    NsdManager.DiscoveryListener mDiscoveryListener;


    public ServerThread(Controller ctrl)
    {
        initCommSuccess = false;
        this.controller = ctrl;
        mNsdManager = (NsdManager) controller.getContext().getSystemService(Context.NSD_SERVICE);
        initializeResolveListener();
        initializeDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void run() {
        Socket socket = null;
        try {
            serverSocket = new ServerSocket(SERVERPORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (!Thread.currentThread().isInterrupted()) {

            try {
                if (serverSocket != null) {
                    socket = serverSocket.accept();
                    System.out.println("starting commThread");
                    CommunicationThread commThread = new CommunicationThread(socket, this.controller);
                    new Thread(commThread).start();
                }

            } catch (IOException e) {
                e.printStackTrace();
                try {
                    serverSocket = new ServerSocket(SERVERPORT);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void close() {
        try {
            if (serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initializeResolveListener() {
        Log.d(TAG, "initializeResolveListener");
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                //Log.d(TAG, "Resolve Succeeded. " + serviceInfo);
                System.out.printf(TAG+" Resolve Succeeded. " + serviceInfo);
                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                else {
                    System.out.printf(TAG+" call to writeIPtoProbe");
                    writeIPtoProbe(serviceInfo.getHost(),serviceInfo.getPort());

                }
                mService = serviceInfo;
            }
        };
    }

    private void writeIPtoProbe(InetAddress host, int port) {
        // Write our IP address and port to the probe socket
        System.out.println("writeIPtoProbe");
        try {
            Socket socket = new Socket(host, port);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());
            Log.i(TAG, "Calling Write");
            WifiManager wm = (WifiManager) controller.getContext().getSystemService(WIFI_SERVICE);
            String ip = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
            String comment = ip+":"+SERVERPORT;
            out.writeBytes(comment);
            //out.flush();
            //String resposeFromServer = in.readUTF();
            out.close();
            in.close();
            //Log.d(TAG, "Response: "+ resposeFromServer);
            socket.close();
            this.probeHost = host;
            this.probePort = port;
            System.out.println("change initCommSuccess from "+initCommSuccess+" to true");
            initCommSuccess = true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeCalibrateToProbe(int status) {

        new WriteProbeTask().execute(probeHost.toString(),Integer.toString(probePort), "calibrate:"+status);
        /*
        // Write our IP address and port to the probe socket
        System.out.println("writeCalibratetoProbe " + status);
        if (probeHost != null && probePort > 0) {
            try {
                System.out.println("open socke to "+probeHost+":"+probePort);
                Socket socket = new Socket(probeHost, probePort);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                String msg = "calibrate:" + status;
                out.writeBytes(msg);
                //out.flush();

                out.close();
                //Log.d(TAG, "Response: "+ resposeFromServer);
                socket.close();
                System.out.println("wrote calibrate to probe");
                initCommSuccess = true;
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/
    }

    public boolean initCommSuccess()
    {
        return initCommSuccess;
    }

    public void initializeDiscoveryListener() {
        Log.d(TAG, "initializeDiscoveryListener");
        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains("Hull Stiffness Monitor")){
                    Log.d(TAG, "Another machine: " + service.getServiceName());
                    Log.d(TAG, "attrs: " +service.getAttributes());
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

}
