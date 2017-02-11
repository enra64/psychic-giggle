package de.ovgu.softwareprojekt.control.commands;

/**
 * This command is sent when the server detects command messages from client A on the port destined
 * for client B. If it is received, the ports should be changed accordingly.
 */
public class RemapPorts extends AbstractCommand {
    /**
     * The new ports to be used
     */
    public int newDataPort, newCommandPort;

    /**
     * Create a new command with the given command type
     */
    public RemapPorts(int newDataPort, int newCommandPort) {
        super(CommandType.RemapPorts);
        this.newCommandPort = newCommandPort;
        this.newDataPort = newDataPort;
    }
}
