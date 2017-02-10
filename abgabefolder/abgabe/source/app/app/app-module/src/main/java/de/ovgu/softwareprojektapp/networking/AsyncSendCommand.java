package de.ovgu.softwareprojektapp.networking;

import android.os.AsyncTask;

import java.io.IOException;

import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.commands.AbstractCommand;

/**
 * This class is a wrapper for {@link CommandConnection#sendCommand(AbstractCommand) sending commands}
 * to avoid dealing with network on the ui thread. Use as follows:
 * <br><br>
 * {@code new AsyncSendCommand().execute(new WhatEverCommand()); }
 * <br><br>
 * where WhatEverCommand is a subclass of {@link AbstractCommand}
 */
public class AsyncSendCommand extends AsyncTask<AbstractCommand, Void, Void> {
    private CommandConnection mCommandConnection;

    /**
     * Create a new sendcommand instance
     *
     * @param commandConnection the {@link CommandConnection} used for sending the command
     */
    public AsyncSendCommand(CommandConnection commandConnection) {
        mCommandConnection = commandConnection;
    }

    /**
     * What gets done when you call {@link #execute(Object[])}
     *
     * @param commands a single AbstractCommand is expected
     * @return null
     */
    @Override
    protected Void doInBackground(AbstractCommand... commands) {
        try {
            mCommandConnection.sendCommand(commands[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}