package com.example.tgtob.myapp;

import android.util.Log;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tgtob on 2017-01-14.
 */
public class UDPReaderRunnable implements Runnable {
    Thread mUDPreadThread;
    private TextView m_tw;
    private final static int BUFF_SIZE = 5000;
    public boolean started = false;
    private boolean keep_running = true;

    public void stop() {
        keep_running = false;
    }

    private String param = "";
    private String value = "";
    private  String old_str = "";

    private enum State {READ_PARAM, READ_VALUE, SEARCH}

    public Map<String, String> params = new HashMap<String, String>();

    private State state = State.SEARCH;

    UDPReaderRunnable(TextView tw) {
        m_tw = tw;
    }

    private void write_to_tw(String data) {
        final String str = data;
        m_tw.post(new Runnable() {
            public void run() {
                m_tw.append(str);
                int ex_chars = m_tw.length() - BUFF_SIZE;
                if (ex_chars > 0) {
                    m_tw.getEditableText().delete(0, ex_chars - 1);
                }
            }
        });
    }


    @Override
    public void run() {
        // Moves the current Thread into the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        mUDPreadThread = Thread.currentThread();
        read_udp();
    }

    private void read_udp() {

        try {
            int port = 11000;

            DatagramSocket dsocket = new DatagramSocket(null);
            dsocket.setReuseAddress(true);
            dsocket.bind(new InetSocketAddress(port));
            byte[] buffer = new byte[2048];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            while (keep_running) {
                dsocket.receive(packet);
                final String str = new String(buffer, 0, packet.getLength());
                Log.d("UDP packet received", str);
                String tmp = check_for_params(str);
                write_to_tw(tmp);
                /*
                m_tw.post(new Runnable() {
                    public void run() {
                        m_tw.append(str);
                    }
                });*/
                packet.setLength(buffer.length);
            }
        } catch (Exception e) {
            System.err.println(e);
            e.printStackTrace();
        }


        write_to_tw("\nUDP reader stopped\n");
    }

    private String check_for_params(String str) {
        String res = "";
        //InputStream is = new ByteArrayInputStream( str.getBytes() );
        char c;
        int index = 0;
        int old_index = 0;
        while (index < str.length()) {
            // read the incoming byte:
            c = str.charAt(index);
            index++;
            switch (state) {
                case READ_PARAM:
                    if (c == '=') {
                        value = "";
                        state = State.READ_VALUE;
                    } else if (c == '&') {
                        param = "";
                        //res += old_str + str.substring(old_index, index);
                        old_index = index;
                    } else if (param.length()> 100)
                    {
                        param = "";
                        res += old_str + str.substring(old_index,index);
                        state = State.SEARCH;
                    }
                    else {
                        param += c;
                    }
                    break;
                case READ_VALUE:
                    if (c == '=') value = "";
                    else if (c == '&') {
                        /* param found **/
                        Log.d("READ PARAMS: ", param + ":" + value);
                        params.put(param,value);
                        param = "";
                        value = "";
                        old_index = index+1;
                        state = State.SEARCH;
                    } else
                        value += c;
                    break;
                case SEARCH:
                    if (c == '&') {
                        param = "";
                        state = State.READ_PARAM;
                        old_index = index;
                    }else
                    {
                        res += c;
                    }
                    break;
            }
        }

        if (old_index <= str.length())
        {
            old_str = str.substring(old_index);
        }

        return res;
    }
}







