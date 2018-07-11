package com.glvz.egais.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.widget.Toast;
import com.glvz.egais.MainApp;

public class MessageUtils {

    private static final Handler handler = new Handler(MainApp.getContext().getMainLooper());

    public static void showModalMessage(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainApp.getContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void showModalMessage(final String msgFormat, Object... objects ) {
        String msg = String.format(msgFormat, objects);
        showModalMessage(msg);

    }

}
