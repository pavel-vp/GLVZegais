package com.glvz.egais.utils;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

public class MessageUtils {

    public static void showModalMessage(final Activity act, final String msg) {
        act.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(act, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
