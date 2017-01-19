package de.ovgu.softwareprojektapp;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

/**
 * User interface utility functions.
 */
public class UiUtil {
    /**
     * Display a toast using the ui thread with show length LENGTH_LONG
     *
     * @param host  activity that hosts the toast
     * @param toast message the toast should display
     */
    public static void showToast(final Activity host, final String toast) {
        host.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(host, toast, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Display an alert dialog using the ui thread. The dialog will have a single OK button.
     *
     * @param host    activity that hosts the alert dialog
     * @param title   title of the alert dialog
     * @param message message of the alert dialog
     */
    public static void showAlert(final Activity host, final String title, final String message) {
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
