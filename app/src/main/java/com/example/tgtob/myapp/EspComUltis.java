package com.example.tgtob.myapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

/**
 * Created by tgtob on 2017-01-28.
 */

public class EspComUltis {

    public static void runCommand(String cmd,TextView tw, final Context context) {
        DownloadWebpageTask dlt = new DownloadWebpageTask();
        dlt.textView = tw;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = sharedPref.getString("pref_ip", "");
        dlt.execute("http://" + ip + "/" + cmd );
    }


    public static void send_udp(String str,Context context)
    {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = sharedPref.getString("pref_ip", "");
        new EspComUltis.UdpSenderTask().execute(str,ip);
    }

    public static void sendMessage(String str) {

        DownloadWebpageTask dlt = new DownloadWebpageTask();
        //dlt.textView = TextView();//(TextView) findViewById(R.id.show_result);
        //EditText editText = (EditText) findViewById(R.id.edit_message);
        //String command = editText.getText().toString();
        //SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        //String ip = sharedPref.getString("pref_ip", "");
        //dlt.execute("http://" + ip + "/" + command );

    }


    static public class UdpSenderTask extends AsyncTask<String,Void,Void> {
        private static final String DEBUG_TAG = "UdpSender";

        @Override
        protected Void doInBackground(String... strs) {
            Log.d(DEBUG_TAG, "Start bg task");

            if(strs.length != 2)
            {
                Log.d(DEBUG_TAG,"input error not 2 args");
                return null;
            }

            String str = strs[0];
            String ip = strs[1];
            Log.d(DEBUG_TAG, "IP:"+ip);

            try
            {
                int server_port = 2390;
                byte[] message = str.getBytes();
                DatagramPacket p = new DatagramPacket(message, 0, str.length(), InetAddress.getByName(ip),server_port);
                DatagramSocket s = new DatagramSocket(server_port);
                s.setReuseAddress(true);
                Log.d("TGTEST",p.toString());
                s.send(p);
                s.close();

            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace();
            }

            return null;


        }




    }



}


