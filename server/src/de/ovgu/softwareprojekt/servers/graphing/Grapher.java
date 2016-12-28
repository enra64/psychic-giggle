package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.control.commands.ButtonClick;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;
import de.ovgu.softwareprojekt.pipeline.FilterPipelineBuilder;
import de.ovgu.softwareprojekt.pipeline.filters.*;
import de.ovgu.softwareprojekt.networking.Server;
import de.ovgu.softwareprojekt.pipeline.splitters.PipelineDuplication;
import de.ovgu.softwareprojekt.pipeline.splitters.SensorSplitter;

import javax.swing.*;
import java.io.IOException;

public class Grapher extends Server {
    public Grapher(final GraphPanel graphPanel) {
        super("graphing server");

        try {

            // register a duplication element as first
            PipelineDuplication pipelineStart = new PipelineDuplication();
            registerDataSink(pipelineStart, SensorType.LinearAcceleration);

            // split off the integration data stream
            FilterPipelineBuilder pipelineBuilder = new FilterPipelineBuilder();
//            NetworkDataSink accelerationIntegralLine = graphPanel.getDataSink(SensorType.LinearAcceleration, 2);
//            pipelineBuilder.append(new AbsoluteFilter());
//            pipelineBuilder.append(new AverageMovementFilter(5));
//            pipelineBuilder.append(new ThresholdingFilter(null, 10, 2));
//            pipelineBuilder.append(new TemporaryIntegratingFilter(null, 5));
//            pipelineStart.addDataSink(pipelineBuilder.build(accelerationIntegralLine));

            // split off the current data stream
            NetworkDataSink accelerationCurrentLine = graphPanel.getDataSink(SensorType.LinearAcceleration, 2);
            pipelineBuilder = new FilterPipelineBuilder();
            pipelineBuilder.append(new AverageMovementFilter(5));
            pipelineBuilder.append(new ThresholdingFilter(null, 10, 2));
            pipelineStart.addDataSink(pipelineBuilder.build(accelerationCurrentLine));
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