package de.ovgu.softwareprojektapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * Created by arne on 11/24/16.
 */

public class UiUtil {
    public static void showToast(final Activity host, final String toast) {
        host.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(host, toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    static void showAlert(final Activity host, final String title, final String message){
        host.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // show a warning if the requested sensor type does not exist
                AlertDialog alertDialog = new AlertDialog.Builder(host).create();
                alertDialog.setTitle(title);
                alertDialog.setMessage(message);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", (DialogInterface.OnClickListener) null);
                alertDialog.show();
            }
        });
    }
}
