package com.display.hlog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.display.loglibrary.LogUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LogUtil.init(getApplication());
        LogUtil.setLogFilepath("/sdcard/Alog");
        LogUtil.setPrintLevel(Log.DEBUG);

        LogUtil.setLogStorageSize(1024*1024*10);
        findViewById(R.id.tvTest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i=0;i<500;i++){
                    LogUtil.i(""+1L+1L+1L+1L+1L+String.valueOf(i));
                }
            }
        });
    }
}
