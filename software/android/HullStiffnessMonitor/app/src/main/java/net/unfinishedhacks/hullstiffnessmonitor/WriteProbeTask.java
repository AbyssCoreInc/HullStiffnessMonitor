package net.unfinishedhacks.hullstiffnessmonitor;

import android.os.AsyncTask;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class WriteProbeTask extends AsyncTask<String, Void, String> {

    private Exception exception;

    protected String doInBackground(String... urls) {
        try {
            String ip = urls[0];
            ip = ip.replace("/","");
            String portS =  urls[1];
            String data = urls[2];
            int port = Integer.parseInt(portS);
            System.out.println("WriteProbeTask " + data);
            if (ip != null && port > 0) {
                System.out.println("open socke to "+ip+":"+port);
                Socket socket = new Socket(ip, port);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                out.writeBytes(data);
                out.close();
                socket.close();
                System.out.println("wrote data to probe");
                return "done";
            }
            return "address null";
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }

    protected void onPostExecute(String response) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }
}