package net.unfinishedhacks.hullstiffnessmonitor;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import android.net.nsd.NsdManager;

class CommunicationThread implements Runnable {
    private final String TAG = "CommunicationThread";
    private PrintWriter output;


    private Controller controller;
    private Socket clientSocket;
    private BufferedReader input;


    public CommunicationThread(Socket clientSocket, Controller controller) {
        this.controller = controller;
        this.clientSocket = clientSocket;

        try {

            this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            this.output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.clientSocket.getOutputStream())),true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void run() {
        Log.d(TAG, "run CommThread ");
        while (!Thread.currentThread().isInterrupted())
        {
            try {
                String read = input.readLine();
                //Log.d(TAG, "wait for message ("+read+")");
                if (read != null && read.length() > 0) {
                    Log.d(TAG, "got message "+read);
                    controller.updateConversationHandler.post(new updateUIThread(read, this.controller));
                    this.output.println("ok");
                    controller.showMessage(read);
                }
                if (controller.timeSinceLastMsg() > 5.0)
                {
                    controller.resetComms();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
