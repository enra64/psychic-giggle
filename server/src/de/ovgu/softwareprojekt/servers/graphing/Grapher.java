package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.ThroughputMeasurer;
import de.ovgu.softwareprojekt.pipeline.filters.*;
import de.ovgu.softwareprojekt.networking.Server;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.*;
import java.util.Timer;

public class Grapher extends Server {
    Grapher(final GraphPanel graphPanel) {
        super("graphing server");

        // set up throughput measurement system
        ThroughputMeasurer throughputMeasurer = new ThroughputMeasurer(1000, 10);
        scheduleThroughputUpdates(graphPanel, throughputMeasurer);

        try {
            //registerAccelerationMovementDetectionPipelineTestbed(graphPanel, throughputMeasurer);
            registerSnesGyroTestbed(graphPanel, throughputMeasurer);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private void registerSnesGyroTestbed(final GraphPanel graphPanel, ThroughputMeasurer throughputMeasurer) throws IOException {
        // normalize output
        //setSensorOutputRange(SensorType.LinearAcceleration, 10);

        // split off the current data stream
        NetworkDataSink accelerationCurrentLine = graphPanel.getDataSink(SensorType.Orientation, 2);
        FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
        pipelineBuilder.append(throughputMeasurer);
        pipelineBuilder.append(new IntegratingFilter(null));
        registerDataSink(pipelineBuilder.build(accelerationCurrentLine), SensorType.Orientation);
    }

    private void registerAccelerationMovementDetectionPipelineTestbed(final GraphPanel graphPanel, ThroughputMeasurer throughputMeasurer) throws IOException {
        // normalize output
        setSensorOutputRange(SensorType.LinearAcceleration, 10);

        // split off the current data stream
        NetworkDataSink accelerationCurrentLine = graphPanel.getDataSink(SensorType.LinearAcceleration, 2);
        FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
        pipelineBuilder.append(throughputMeasurer);
        pipelineBuilder.append(new ThresholdingFilter(null, .5f, 2));
        pipelineBuilder.append(new AveragingFilter(5));
        registerDataSink(pipelineBuilder.build(accelerationCurrentLine), SensorType.LinearAcceleration);
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
    public void onButtonClick(ButtonClick buttonClick, NetworkDevice networkDevice) {
        // naah
    }

    @Override
    public void onClientDisconnected(NetworkDevice networkDevice) {
        // naaah
    }

    @Override
    public void onClientTimeout(NetworkDevice networkDevice) {
        // naaaah
    }

    @Override
    public void onException(Object origin, Exception exception, String info) {
        // ok guys maybe we should do something about this
        exception.printStackTrace();
    }
}