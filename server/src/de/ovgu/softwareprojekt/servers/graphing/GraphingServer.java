package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.ThroughputMeasurer;
import de.ovgu.softwareprojekt.pipeline.filters.*;
import de.ovgu.softwareprojekt.networking.AbstractServer;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

/**
 * A server for live graphing of sensor data
 */
public class GraphingServer extends AbstractServer {

    /**
     * Create a new GraphingServer.
     *
     * @param graphPanel the GraphPanel that will be used for displaying the sensor data
     */
    private GraphingServer(final GraphPanel graphPanel) {
        super("graphing server");

        // set up throughput measurement system
        ThroughputMeasurer throughputMeasurer = new ThroughputMeasurer(1000, 10);
        scheduleThroughputUpdates(graphPanel, throughputMeasurer);

        try {
            registerGyroTestbed(graphPanel, throughputMeasurer);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Register a gyro testbed (all axes), normalized with range 100 and the throughput measurer applied
     *
     * @param graphPanel         the graph panel to use for display
     * @param throughputMeasurer the throughput measurer that should be connected
     * @throws IOException if the testbed couldnt be brought online
     */
    private void registerGyroTestbed(final GraphPanel graphPanel, ThroughputMeasurer throughputMeasurer) throws IOException {
        // register the throughput measuring stream
        NetworkDataSink accelerationCurrentLine = graphPanel.getDataSink(SensorType.GyroscopeUncalibrated, 0);
        FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
        pipelineBuilder.append(throughputMeasurer);
        //pipelineBuilder.append(new ScalingFilter(1000f, 10f, new AveragingFilter(5)));
        registerDataSink(pipelineBuilder.build(accelerationCurrentLine), SensorType.GyroscopeUncalibrated);

        /**
        // register Y-Axis
        NetworkDataSink accelerationCurrentLine1 = graphPanel.getDataSink(SensorType.LinearAcceleration, 1);
        registerDataSink(pipelineBuilder.build(accelerationCurrentLine1), SensorType.LinearAcceleration);

        //register Z-Axis
        NetworkDataSink accelerationCurrentLine2 = graphPanel.getDataSink(SensorType.LinearAcceleration, 2);
        registerDataSink(pipelineBuilder.build(accelerationCurrentLine2), SensorType.LinearAcceleration);

         */
    }

    /**
     * Schedule regular updates of the throughput.
     *
     * @param panel    the panel that should display the throughput
     * @param measurer the ThroughputMeasurer that measures the throughput...
     */
    private void scheduleThroughputUpdates(GraphPanel panel, ThroughputMeasurer measurer) {
        // schedule regular throughput updates
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                panel.setThroughput(measurer.getThroughput());
                panel.repaint();
            }
        }, 0, 1000);

        // cancel the timer if the panel is closed
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(panel);
        parentFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                super.windowClosing(windowEvent);
                timer.cancel();
            }
        });
    }

    /**
     * Start a new graphing user interface
     *
     * @param args ignored
     * @throws IOException if the device connection fails
     */
    public static void main(String[] args) throws IOException {
        GraphPanel graphPanel = new GraphPanel();

        // show the ui
        JFrame frame = new JFrame("GraphingServer");
        frame.setContentPane(graphPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);


        // start the server
        new GraphingServer(graphPanel).start();
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        // display exception for user to interpret
        System.out.println("Exception in " + origin.getClass() + ", info: " + info);
        System.out.flush();
        exception.printStackTrace();
    }

    /*


    All following callbacks are ignored


     */

    /**
     * Check whether a new client should be accepted
     *
     * @param networkDevice the new clients identification
     * @return true if the client should be accepted, false otherwise
     */
    @Override
    public boolean acceptClient(NetworkDevice networkDevice) {
        // yah
        return true;
    }

    /**
     * Called when the user presses the reset position button
     *
     * @param networkDevice which device pressed the button
     */
    @Override
    public void onResetPosition(NetworkDevice networkDevice) {
        // nah
    }

    /**
     * Called whenever a button is clicked
     *
     * @param buttonClick   event object specifying details like button id
     * @param networkDevice the network device that sent the button click
     */
    @Override
    public void onButtonClick(ButtonClick buttonClick, NetworkDevice networkDevice) {
        // naah
    }

    /**
     * Called when a client sent a disconnect signal
     *
     * @param networkDevice the lost client
     */
    @Override
    public void onClientDisconnected(NetworkDevice networkDevice) {
        // naaah
    }

    /**
     * Called when a client hasn't responded to connection check requests within 500ms
     *
     * @param networkDevice the client that did not respond
     */
    @Override
    public void onClientTimeout(NetworkDevice networkDevice) {
        // naaaah
    }

    /**
     * called when a Client is successfully connected
     *
     * @param connectedClient the client that connected successfully
     */
    @Override
    public void onClientAccepted(NetworkDevice connectedClient) {
        // naaaaah
    }
}