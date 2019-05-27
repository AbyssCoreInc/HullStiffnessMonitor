package net.unfinishedhacks.hullstiffnessmonitor;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

class CommunicationThread implements Runnable {
    private final String TAG = "CommunicationThread";

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
    }

    public void run() {

        while (!Thread.currentThread().isInterrupted()) {

            try {

                String read = input.readLine();
                if (read != null && read.length() > 0) {
                    //Log.d(TAG, "got message "+read);
                    controller.updateConversationHandler.post(new updateUIThread(read, this.controller));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
