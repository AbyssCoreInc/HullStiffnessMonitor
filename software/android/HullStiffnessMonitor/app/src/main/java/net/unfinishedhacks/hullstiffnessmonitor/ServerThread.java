package net.unfinishedhacks.hullstiffnessmonitor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

class ServerThread implements Runnable {

    private static final int SERVERPORT = 6000;
    private ServerSocket serverSocket;
    private Controller controller;

    public ServerThread(Controller ctrl)
    {
        this.controller = ctrl;
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

                socket = serverSocket.accept();
                CommunicationThread commThread = new CommunicationThread(socket, this.controller);
                new Thread(commThread).start();

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
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
