package de.ovgu.softwareprojekt.control;

import de.ovgu.softwareprojekt.control.commands.Command;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import java.io.IOException;
import java.net.InetAddress;

public interface CommandSource {
    public interface OnCommandListener {
        void onCommand(InetAddress origin, Command command);
    }

    void setCommandListener(OnCommandListener listener);

    void start() throws IOException;

    void close();
}
