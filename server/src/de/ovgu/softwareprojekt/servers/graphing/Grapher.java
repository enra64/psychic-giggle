package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.DataSink;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.networking.Server;

import javax.swing.*;
import java.io.IOException;

public class Grapher extends Server {
    public Grapher(final GraphPanel graphPanel) {
        super("graphing server");

        try {
            registerDataSink(new NetworkDataSink() {
                @Override
                public void onData(NetworkDevice origin, SensorData data) {
                    graphPanel.addData(data);
                }

                @Override
                public void close() {
                }
            }, SensorType.Gyroscope);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Start a new user interface
     *
     * @param args ignored
     */
    public static void main(String[] args) throws IOException {
        GraphPanel graphPanel = new GraphPanel();

        // show the ui
        JFrame frame = new JFrame("Grapher");
        frame.setContentPane(graphPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


        // start the server
        new Grapher(graphPanel).start();
    }

    @Override
    public boolean acceptClient(NetworkDevice networkDevice) {
        // yah
        return true;
    }

    @Override
    public void onResetPosition(NetworkDevice networkDevice) {
        // nah
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        // naah
    }

    @Override
    public void onButtonClick(ButtonClick buttonClick, NetworkDevice networkDevice) {
        // naaah
    }

    @Override
    public void onClientDisconnected(NetworkDevice networkDevice) {
        // naaaah
    }

    @Override
    public void onClientTimeout(NetworkDevice networkDevice) {
        // naaaaah
    }
}