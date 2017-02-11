package de.ovgu.softwareprojekt.examples.graphing;

import de.ovgu.softwareprojekt.networking.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The base of this code is the following github gist:
 * https://gist.github.com/roooodcastro/6325153. It was modified to support multiple
 * independent lines.
 */
public class GraphPanel extends JPanel {
    /**
     * A preset of 8 clearly distinguishable colors.
     */
    private static final Color[] mLineColors = new Color[]{
            new Color(255, 0, 0),
            new Color(0, 255, 0),
            new Color(0, 0, 255),
            new Color(255, 0, 255),
            new Color(0, 255, 255),
            new Color(128, 128, 0),
            new Color(0, 128, 128),
            new Color(128, 0, 128)
    };

    /**
     * The color that is used to draw the background grid
     */
    private static final Color GRID_COLOR = new Color(200, 200, 200, 200);

    /**
     * Padding variables
     */
    private static final int PADDING = 25, LABEL_PADDING = 40;

    /**
     * How large the buffer should be, and thus how many data points are in one full line
     */
    static final int BUFFER_SIZE = 512;

    /**
     * The white background start coordinates
     */
    static final int Y_START = PADDING, X_START = PADDING + LABEL_PADDING;

    /**
     * How wide the hatch marks are (px)
     */
    private static final int HATCH_MARK_WIDTH = 4;

    /**
     * How many vertical lines the grid should have
     */
    private static final int Y_DIVISION_COUNT = 20;

    /**
     * The lines currently drawn
     */
    private List<MultiPointLine> mLines = new ArrayList<>(8);

    /**
     * How much data/s arrives here
     */
    private float mThroughput = 0;

    /**
     * Set what data throughput rate is displayed
     *
     * @param throughput the throughput to be displayed.
     */
    void setThroughput(float throughput) {
        mThroughput = throughput;
    }

    /**
     * Called whenever the graph needs to be redrawn
     *
     * @param g the graphics object to be used for drawing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // calculate some useful sizes
        int backgroundWidth = getWidth() - 2 * PADDING - LABEL_PADDING;
        int xHatchMarkEnd = HATCH_MARK_WIDTH + PADDING + LABEL_PADDING;
        int xEnd = getWidth() - PADDING;

        int backgroundHeight = getHeight() - 2 * PADDING;
        int yEnd = getHeight() - PADDING;

        FontMetrics metrics = graphics2D.getFontMetrics();
        int textHeight = metrics.getHeight();

        // draw white background
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(X_START, Y_START, backgroundWidth, backgroundHeight);
        graphics2D.setColor(Color.BLACK);

        // draw throughput string
        String throughput = String.format("%.2f updates / s", mThroughput);
        graphics2D.drawString(throughput, xEnd - 5 - metrics.stringWidth(throughput), Y_START);

        // create hatch marks, grid labels and grid lines for y axis.
        float min = getMinValue(), max = getMaxValue();
        for (int i = 0; i < Y_DIVISION_COUNT + 1; i++) {
            // draw the grid line
            graphics2D.setColor(GRID_COLOR);
            int y = getHeight() - (i * backgroundHeight / Y_DIVISION_COUNT + PADDING);
            graphics2D.drawLine(xHatchMarkEnd + 1, y, xEnd, y);

            // use black for label and hatch mark
            graphics2D.setColor(Color.BLACK);

            // draw the label
            String yLabel = String.format("%.2f", ((min + (max - min) * (((double) i) / Y_DIVISION_COUNT))));
            graphics2D.drawString(yLabel, X_START - metrics.stringWidth(yLabel) - 5, y + (textHeight / 2) - 3);

            // draw the hatch mark
            graphics2D.drawLine(X_START, y, xHatchMarkEnd, y);
        }

        // create x and y axes 
        graphics2D.drawLine(X_START, yEnd, X_START, Y_START);
        graphics2D.drawLine(X_START, yEnd, xEnd, yEnd);

        // draw all lines
        double xScale = ((double) backgroundWidth) / (BUFFER_SIZE - 1);
        double yScale = ((double) backgroundHeight) / (max - min);

        // draw all lines
        for (MultiPointLine line : mLines)
            line.draw(graphics2D, backgroundHeight, min, xScale, yScale);
    }

    /**
     * Count the number of lines we have stored
     *
     * @return number of lines we are drawing
     */
    private int getLineCount() {
        return mLines.size();
    }

    /**
     * Minimum value contained in all lines
     * @return minimum value contained in all lines
     */
    private float getMinValue() {
        float min = Float.MAX_VALUE;
        for (MultiPointLine line : mLines)
            min = Math.min(min, line.getMinimum());
        return min;
    }

    /**
     * Maximum value contained in all lines
     * @return maximum value contained in all lines
     */
    private float getMaxValue() {
        float max = -Float.MAX_VALUE;
        for (MultiPointLine line : mLines)
            max = Math.max(max, line.getMaximum());
        return max;
    }

    /**
     * Remove a line from service
     *
     * @param sensor     the line to be removed
     * @param sensorAxis the axis the line is for
     */
    @SuppressWarnings("unused")
    void removeLine(SensorType sensor, int sensorAxis) {
        // remove all matching lines
        mLines.removeIf(line -> line.getSensorType() == sensor && line.getAxis() == sensorAxis);
    }

    /**
     * Get a data sink. This new data sink corresponds to a new line in the graph, which will display the data received
     * by this data sink.
     *
     * @param sensor requested sensor type
     * @param axis   requested sensor axis
     * @return a data sink usable for pushing data into the graph
     */
    NetworkDataSink getDataSink(SensorType sensor, final int axis) {
        // use a random line color if no predefined is left
        Color color;
        int lineCount = getLineCount();
        if (lineCount < mLineColors.length)
            color = mLineColors[lineCount];
        else
            color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());

        // create new line
        MultiPointLine newLine = new MultiPointLine(color, sensor, axis);
        mLines.add(newLine);

        // return new data sink that will notify the new line of incoming data
        return new NetworkDataSink() {
            @Override
            public void onData(NetworkDevice networkDevice, SensorData sensorData, float userSensitivity) {
                newLine.addPoint(sensorData.data[axis]);
                repaint();
            }

            @Override
            public void close() {
            }
        };
    }
}