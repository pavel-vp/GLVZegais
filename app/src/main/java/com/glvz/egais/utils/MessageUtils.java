package com.glvz.egais.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.os.Handler;
import android.widget.Toast;
import com.glvz.egais.MainApp;

public class MessageUtils {

    private static final Handler handler = new Handler(MainApp.getContext().getMainLooper());

///////////////////////////////
    public static void showToastMessage(final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainApp.getContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void showToastMessage(final String msgFormat, Object... objects ) {
        String msg = String.format(msgFormat, objects);
        showToastMessage(msg);
    }
//////////////////////////////////
    public static void showModalMessage(final Activity activity, final String title, final String msg) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // показываем ошибку
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title)
                        .setMessage(msg)
                        .setCancelable(false)
                        .setNegativeButton("Ок",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
/*                                if (finish) {
                                    ctx.finish();
                                }*/
                                    }
                                });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public static void showModalMessage(final Activity activity, final String title, final String msgFormat, Object... objects ) {
        String msg = String.format(msgFormat, objects);
        showModalMessage(activity, title, msg);
    }

////////////////////////////

    public static void ShowModalAndConfirm(final Activity activity, final String title, final String msg,
                                           final DialogInterface.OnClickListener listenerOk) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                // показываем ошибку
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle(title)
                        .setMessage(msg)
                        .setCancelable(false)
                        .setPositiveButton("Да", listenerOk)
                        .setNegativeButton("Нет", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }

    public static void ShowModalAndConfirm(final Activity activity, final String title, final String msgFormat,
                                           DialogInterface.OnClickListener listenerOk, Object... objects ) {
        String msg = String.format(msgFormat, objects);
        ShowModalAndConfirm(activity, title, msg, listenerOk);
    }

////////////////////

    public static void playSound(int idSound){
        MediaPlayer mPlayer = MediaPlayer.create(MainApp.getContext(), idSound);
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.release();
            }
        });
        mPlayer.start();
    }

}
