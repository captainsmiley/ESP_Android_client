package com.example.tgtob.myapp;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;


public class ControlsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controls);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        final Context context = this;
        final TextView tw = (TextView) findViewById(R.id.textView);

        SeekBar turnSeekBar = (SeekBar) findViewById(R.id.seekBar2);
        turnSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onProgressChanged(SeekBar arg0,
                                          int progress, boolean arg2) {
                //EspComUltis.runCommand("send_params_serial?turn="+Integer.toString(progress),tw,context
                EspComUltis.send_udp("&turn="+Integer.toString(progress)+"&",context);
                //EspComUltis.send_udp("test",context);


            }
        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    EspComUltis.send_udp("&bw=1&",context);
                } else {
                    EspComUltis.send_udp("&bw=0&",context);
                }
            }
        });


        SeekBar speedSeekBar = (SeekBar) findViewById(R.id.speed);
        speedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar arg0) {
            }

            public void onStartTrackingTouch(SeekBar arg0) {
            }
            public void onProgressChanged(SeekBar arg0,
                                          int progress, boolean arg2) {
                //EspComUltis.runCommand("send_params_serial?turn="+Integer.toString(progress),tw,context
                EspComUltis.send_udp("&speed="+Integer.toString(progress)+"&",context);
                //EspComUltis.send_udp("test",context);
            }
        });
    }

    public void test(View view)
    {
        TextView tw = (TextView) findViewById(R.id.textView);
        EspComUltis.runCommand("test",tw,this);
    }

    public void activate_drive(View view)
    {
        EspComUltis.send_udp("&drive=1&",this);
    }

    public void deactivate_drive(View view)
    {
        EspComUltis.send_udp("&drive=0&",this);
    }

    public void fw_bw(View view)
    {
    }




}


