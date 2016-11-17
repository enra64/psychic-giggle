package de.ovgu.softwareprojekt.networking;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.Mover;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.CommandConnection;
import de.ovgu.softwareprojekt.control.OnCommandListener;
import de.ovgu.softwareprojekt.control.commands.*;
import de.ovgu.softwareprojekt.misc.ExceptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;

/**
 * This class encapsulates the whole server system:
 * <p>
 * 1) A DiscoveryServer used to make clients able to find us
 * 2) A CommandConnection to be able to reliable communicate about important stuff, like enabling sensors
 * 3) A DataConnection to rapidly transmit sensor data
 */
public class Server implements OnCommandListener, DataSink {
    /**
     * Useful when you want to transmit SensorData
     */
    private UdpDataConnection mDataConnection;

    /**
     * Two-way communication for basically everything non-data, like enabling sensors or requesting connections
     */
    private CommandConnection mCommandConnection;

    /**
     * Interface to communicate about lost and won clients
     */
    private ClientListener mClientListener;
    /**
     * interface to process infos about clicked Buttons
     */
    private ButtonListener mButtonListener;
    /**
     * Stores all known data sinks
     * <p>
     * This variable is an EnumMap, so iterations over the keys should be quite fast. The sinks are stored in a
     * HashSet for each sensor, so no sink can occur twice for one sensor type.
     */
    private EnumMap<SensorType, HashSet<DataSink>> mDataSinks = new EnumMap<>(SensorType.class);

    /**
     * Creates a new server object
     *
     * @throws IOException when something goes wrong...
     */
    public Server(ExceptionListener exceptionListener, ClientListener clientListener, ButtonListener buttonListener) throws IOException {
        // initialise the command and data connections
        initialiseCommandConnection();
        initialiseDataConnection();

        // the DiscoveryServer makes it possible for the client to find us, but it needs to know the command and
        // data ports, which is why we had to initialise those connections first
        DiscoveryServer discoveryServer = new DiscoveryServer(
                exceptionListener,
                8888,
                mCommandConnection.getLocalPort(),
                mDataConnection.getLocalPort(),
                "lessig. _christian_ lessig");
        discoveryServer.start();

        mClientListener = clientListener;

        System.out.println("discovery server started");
    }

    /**
     * Register a new data sink to be included in the data stream
     * @param dataSink where new data from the sensor should go
     * @param requestedSensor which sensors events are relevant
     */
    public void registerDataSink(DataSink dataSink, SensorType requestedSensor) {
        // add new data sink list if the requested sensor type has no sinks yet
        if (!mDataSinks.containsKey(requestedSensor))
            mDataSinks.put(requestedSensor, new HashSet<DataSink>());

        // add the new sink to the list of sink for the sensor
        mDataSinks.get(requestedSensor).add(dataSink);
    }

    /**
     * Unregister a data sink from a sensors data stream
     * @param dataSink the data sink to be unregistered
     * @param requestedSensor the sensor the sink should be unregistered from
     */
    public void unregisterDataSink(DataSink dataSink, SensorType requestedSensor){
        mDataSinks.get(requestedSensor).remove(dataSink);
    }

    /**
     * Unregister a data sink from all sensors
     * @param dataSink which data sink to remove
     */
    public void unregisterDataSink(DataSink dataSink){
        for(SensorType type : mDataSinks.keySet())
            unregisterDataSink(dataSink, type);
    }

    /**
     * Called whenever a command packet has arrived
     *
     * @param origin  host of the sender
     * @param command the command issued
     */
    @Override
    public void onCommand(InetAddress origin, Command command) {
        // command-line logging
        System.out.println("command received, type " + command.getCommandType().toString());

        // decide what to do with the packet
        switch (command.getCommandType()) {
            case ConnectionRequest:
                // since this commands CommandType is ConnectionRequest, we know to what to cast it
                ConnectionRequest request = (ConnectionRequest) command;

                try {
                    // update the request address
                    request.self.address = origin.getHostAddress();

                    // only accept clients which are accepted by our client listener
                    if(mClientListener.acceptClient(request.self)) {
                        // now that we have a connection, we know who to talk to for the commands
                        mCommandConnection.setRemote(origin, request.self.commandPort);

                        //test TODO: wieder entfernen und umschreiben
                        addButton("LeftClick", 0);

                        // for now, immediately let the client begin sending gyroscope data
                        mCommandConnection.sendCommand(new SetSensorCommand(SensorType.Gyroscope, true));
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case EndConnection:
                // close connections
                mDataConnection.close();
                mCommandConnection.close();

                // notify listener of disconnect
                EndConnection endConnection = (EndConnection) command;
                endConnection.self.address = origin.getHostAddress();
                mClientListener.onClientDisconnected(endConnection.self);

                break;

            case ButtonClick:
                ButtonClick btnClk = (ButtonClick) command;
                mButtonListener.onButtonClick(btnClk);

            // ignore unhandled commands (like SetSensorCommand)
            default:
                break;
        }
    }

    /**
     * Simply prepare the command connection and register us as listener
     */
    private void initialiseCommandConnection() throws IOException {
        // begin a new command connection, set this as callback
        mCommandConnection = new CommandConnection(this);

        // begin listening for commands
        mCommandConnection.start();
    }

    /**
     * Prepare the data connection, and create a listener.
     */
    private void initialiseDataConnection() throws SocketException {
        // begin a new data connection
        mDataConnection = new UdpDataConnection();

        // register a callback for data objects
        mDataConnection.setDataSink(this);

        // begin listening for SensorData objects
        mDataConnection.start();
    }

    @Override
    public void onData(SensorData sensorData) {
        // iterate over all data sinks marked as interested in this sensor type
        for(DataSink sink : mDataSinks.get(sensorData.sensorType))
            sink.onData(sensorData);
    }

    @Override
    public void close() {
        mCommandConnection.close();
        mDataConnection.close();
    }

    public void addButton(String name, int id){
        try {
            mCommandConnection.sendCommand(new AddButton(name , id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}