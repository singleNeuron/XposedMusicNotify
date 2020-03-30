package me.singleneuron.originalmusicnotification_debugtool;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.topjohnwu.superuser.Shell;

import java.io.PrintWriter;
import java.io.StringWriter;

public class MainActivity extends Activity {

    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        try {
            Shell.Result result = Shell.sh("su --version").exec();
            writeToTextview("su --version: " + (result.isSuccess() ? result.getOut().toString() : result.getErr().toString()));
            writeToTextview("taichi_magisk: " + System.getProperty("taichi_magisk"));
            toHook();
        } catch (Exception e) {
            e.printStackTrace();
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            e.printStackTrace(printWriter);
            writeToTextview(stringWriter.toString());
        }
    }

    public void toHook() {
        writeToTextview("原生音乐通知未找到");
        Log.d("XposedMusicNotify_DebugTool", "原生音乐通知未找到");
    }

    public void writeToTextview(String string) {
        textView.setText(textView.getText() + string + "\n\n");
    }

}