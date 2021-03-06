package com.example.tgtob.myapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import com.example.tgtob.myapp.EspComUltis;

import java.util.Timer;
import java.util.TimerTask;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class EspConnect extends Service {

    final Context context = this;

    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;

    Timer timer = new Timer();
    Boolean on = false;

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {

            Intent indent = (Intent) msg.obj;

            if(on)
            {
                EspComUltis.send_udp("&c_mode=0&",context);
                timer.cancel();
                on = false;
                stopSelf();
            }
            else {

                EspComUltis.send_udp("&c_mode=4&",context);
                timer.schedule(new TimerTask() {

                    @Override
                    public void run() {
                        //Log.d("TINER", "jkjk");
                        EspComUltis.send_udp("y",context);
                    }

                }, 0, 300);//Update text every second
                on = true;
            }



        }
    }

    public EspConnect() {
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
        mServiceHandler = new ServiceHandler(mServiceLooper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

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
        Log.d("ESPConnect","service destroyed");
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}
