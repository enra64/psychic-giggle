package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.Command;

import java.io.IOException;

/**
 * Created by arne on 11/13/16.
 */
public interface CommandSink {
    void sendCommand(Command command) throws IOException;
    void close();
}
