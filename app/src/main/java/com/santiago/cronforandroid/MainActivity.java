package com.santiago.cronforandroid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import cron4j.Scheduler;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Scheduler scheduler = new Scheduler();
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scheduler.isStarted()) return;
                scheduler.schedule("10 50-55 16 * * *", new Runnable() {
                    @Override
                    public void run() {
                        Log.d("scheduler", "run: ");
                    }
                });

                scheduler.start();
            }
        });
    }
}
