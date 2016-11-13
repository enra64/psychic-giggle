package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.Command;

import java.io.IOException;

/**
 * Created by arne on 11/13/16.
 */
public interface CommandSource {
    public interface OnCommandListener {
        void onCommand(Command command);
    }

    void setCommandListener(OnCommandListener listener);

    void start() throws IOException;

    void close();
}
