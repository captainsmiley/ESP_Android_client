package com.example.tgtob.myapp;

import android.content.SharedPreferences;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.SubMenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    private  UDPReaderRunnable m_udp_r;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();

        TextView outputText = (TextView) findViewById(R.id.out_put);
        outputText.setMovementMethod(new ScrollingMovementMethod());
        m_udp_r = new UDPReaderRunnable((TextView) findViewById(R.id.out_put));
        hideSoftKeyboard();



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // action with ID action_refresh was selected
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                break;
            default:
                break;
        }

        return true;
    }

    public void sendMessage(View view) {

        DownloadWebpageTask dlt = new DownloadWebpageTask();
        dlt.textView = (TextView) findViewById(R.id.show_result);
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String command = editText.getText().toString();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = sharedPref.getString("pref_ip", "");
        dlt.execute("http://" + ip + "/" + command );

    }



    public void readUDP(View view)
    {
        Intent intent = new Intent(this, UDPReader_service.class);
        startService(intent);
        /*

        Log.d("tgtest","Read UDP");
        //Intent intent = new Intent(this, UDPReader_service.class);
        //startService(intent);

        String local_ip = Utils.getIPAddress(true);

        runCommand("set_client_ip?"+local_ip);



        if(m_udp_r.started)
        {
            m_udp_r.stop();
            ((Button) findViewById(R.id.udp_button)).setText("Start UDP");
            m_udp_r.started = false;
        }
        else
        {
            m_udp_r = new UDPReaderRunnable((TextView) findViewById(R.id.out_put));
            m_udp_r.started = true;
            new Thread(m_udp_r).start();
            ((Button) findViewById(R.id.udp_button)).setText("Stop UDP");
        } */

    }

    public void espConnect(View view)
    {
        Intent intent = new Intent(this, EspConnect.class);
        startService(intent);
    }



    public void ledOff(View view) {
        runCommand(getResources().getString(R.string.cmd_led_off));
    }

    public void servoHigh(View view) {runCommand("servo_h");}

    public void servoLow(View view) {runCommand("servo_l");}

    public void startControlsActivity(View view)
    {
        Intent intent = new Intent(this, ControlsActivity.class);
        startActivity(intent);
    }


    private void runCommand(String cmd) {
        DownloadWebpageTask dlt = new DownloadWebpageTask();
        dlt.textView = (TextView) findViewById(R.id.show_result);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String ip = sharedPref.getString("pref_ip", "");
        dlt.execute("http://" + ip + "/" + cmd );
    }


    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
}
