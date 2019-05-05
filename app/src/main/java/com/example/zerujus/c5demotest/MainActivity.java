package com.example.zerujus.c5demotest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private Switch switch_statusBar;
    private Switch switch_navigationBar;
    private Switch switch_silentUpdate;
    private Switch switch_nfc;
    private TextView textView_nfc;
    private Spinner spinner;
    private EditText editText;
    private Button button;

    public static int flag = 1;

    private static boolean checked1 = true;
    private static boolean checked2 = true;
    private static boolean checked3 = true;
    private static boolean checked4 = false;

    private static boolean updateChecked = true;
    private Intent serviceIntent;
    private NfcService.MyBinder binder;

    private ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (NfcService.MyBinder) service;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder = null;
        }
    };

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy年MM月dd日   HH:mm:ss");
                    Date curDate = new Date(System.currentTimeMillis());
                    String data = (String) msg.obj;
                    if (data != null && data.length() > 0) {
                        textView_nfc.setText("[" + formatter.format(curDate) + "] : " + data);
                    }
                    break;
                case 1:
                    textView_nfc.setText("");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceIntent = new Intent(this, NfcService.class);
        startService(serviceIntent);

        switch_statusBar = findViewById(R.id.switch_statusBar);
        switch_navigationBar = findViewById(R.id.switch_Navigation);
        switch_silentUpdate = findViewById(R.id.switch_silentUpdate);
        switch_nfc = findViewById(R.id.switch_nfc);
        textView_nfc = findViewById(R.id.textView_nfc_log);
        spinner = findViewById(R.id.spinner);

        switch_statusBar.setChecked(checked1);
        switch_navigationBar.setChecked(checkDeviceHasNavigationBar(this));
        switch_silentUpdate.setChecked(checked3);
        spinner.setSelection(flag);

        editText = findViewById(R.id.editText);
        button = findViewById(R.id.button);

        String path = Environment.getExternalStorageDirectory().getPath();
        Log.d("ginger", path);

        switch_statusBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked1 = isChecked;
                if (isChecked) {
                    Intent intent = new Intent("com.hra.showStatusBar");
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent("com.hra.hideStatusBar");
                    sendBroadcast(intent);
                }
            }
        });

        //导航栏控制
        switch_navigationBar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked2 = isChecked;
                if (isChecked) {
                    Intent intent = new Intent("com.hra.disableAllButtom");
                    sendBroadcast(intent);
                } else {
                    Intent intent = new Intent("com.hra.enableAllButtom");
                    sendBroadcast(intent);
                }
            }
        });

        //静默安装控制   true为安装后启动   false反之
        switch_silentUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked3 = isChecked;
                if (isChecked) {
                    updateChecked = true;
                } else {
                    updateChecked = false;
                }
            }
        });

        switch_silentUpdate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent, "选择升级apk文件"), 1);

                switch_silentUpdate.setEnabled(false);
                return true;
            }
        });

        //选中开始读卡，取消选中禁止读卡
        switch_nfc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                checked4 = isChecked;
                if (isChecked) {
                    Log.d("ginger", "开启NFC");
                    button.setEnabled(true);
                    spinner.setEnabled(false);
                    if (binder != null)
                        binder.startLoop(handler);
                } else {
                    button.setEnabled(false);
                    spinner.setEnabled(true);
                    Log.d("ginger", "关闭NFC");
                    if (binder != null)
                        binder.endLoop();
                }
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                flag = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                flag = 0;
            }
        });
    }

    /**
     * button 点击事件
     *
     * @param v
     */
    public void sendCommand(View v){
        String command = editText.getText().toString();
        Log.d("Ginger", "command + " + command);
        binder.sendCommand(command);
        binder.getCommand();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(new Intent(MainActivity.this, NfcService.class), conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(conn);
    }

    /**
     * 此处便于演示静默安装功能使用长按选择文件的方式，贵方实现只需传入文件路径并执行start->end之间即可
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                Uri uri = data.getData();
                String path = DocumentsContract.getDocumentId(uri);
                Log.d("ginger", "file is " + path);
                String[] str = path.split(":");
                //start
                Intent intent = new Intent("com.hra.Silence.install");
                String filePath = Environment.getExternalStorageDirectory().getPath() + "/" + str[1]; //直接传入apk文件的路径即可
                Log.d("ginger", "filePath is " + filePath);
                intent.putExtra("filePath", filePath);
                intent.putExtra("open", updateChecked);
                sendBroadcast(intent);
                //end

                switch_silentUpdate.setEnabled(true);
            }
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (checked2 != checkDeviceHasNavigationBar(this))
            switch_navigationBar.setChecked(!checked2);

        return super.onTouchEvent(event);
    }

    //获取是否存在NavigationBar
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {

        }
        return hasNavigationBar;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
