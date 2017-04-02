package com.example.tgtob.myapp;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;


/**
 * Created by tgtob on 2017-01-14.
 */
public class UDPReader_service extends Service {

    final Context context = this;

    private Looper mServiceLooper;
    private UDPReader_service.ServiceHandler mServiceHandler;

    private final static int BUFF_SIZE = 5000;
    public boolean started = false;
    volatile private boolean keep_running = true;

    public void stop() {
        keep_running = false;
    }

    private String param = "";
    private String value = "";
    private  String old_str = "";

    private enum State {READ_PARAM, READ_VALUE, SEARCH};
    private State state = State.SEARCH;

    public Map<String, String> params = new HashMap<String, String>();

    private TextView m_tw = null;


    private UDPRead udpRead = new UDPRead();
    private Thread read_udp_thread;
    volatile Boolean on = false;

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            Intent indent = (Intent) msg.obj;

            if(on)
            {
                Log.d("tgtest","try to stop");
                on = false;
                keep_running = false;
                stopSelf();

            }
            else {
                Log.d("tgtest","try to start");
                String local_ip = Utils.getIPAddress(true);
                EspComUltis.send_udp("set_client_ip?"+local_ip,context);
                on = true;
                read_udp_thread = new Thread(udpRead);
                read_udp_thread.start();
                //read_udp();
            }

        }
    }

    public UDPReader_service() {

    }

    private void write_to_tw(String data) {
        if(m_tw == null) return;
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
    public void onCreate() {
        on = false;
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new UDPReader_service.ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        Log.d("UDPReader","service destroyed");
        Toast.makeText(this, "udp reader stopped", Toast.LENGTH_SHORT).show();
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

    class UDPRead implements Runnable {
        UDPRead() {

        }

        public void run() {
            // Moves the current Thread into the background
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            Log.d("tgtest","run read udp");
            read_udp();
        }
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
                        state = UDPReader_service.State.READ_PARAM;
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


