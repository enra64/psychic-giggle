package de.ovgu.softwareprojekt.servers.graphing;

import com.sun.org.apache.xpath.internal.operations.Mult;
import de.ovgu.softwareprojekt.NetworkDataSink;
import de.ovgu.softwareprojekt.SensorData;
import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.discovery.NetworkDevice;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * This code is taken almost unchanged from a github gist.
 *
 * @author Rodrigo
 */
public class GraphPanel extends JPanel implements NetworkDataSink {
    /**
     * The set of available colors.
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
    static final int PADDING = 25, LABEL_PADDING = 40;

    static final int BUFFER_SIZE = 512;

    /**
     * The white background start coordinates
     */
    static final int Y_START = PADDING, X_START = PADDING + LABEL_PADDING;

    /**
     * How wide the hatch marks are (px)
     */
    private static final int HATCHMARK_WIDTH = 4;

    /**
     * How many vertical lines the grid should have
     */
    private static final int Y_DIVISION_COUNT = 10;

    /**
     * Map the sensor types to its available axes to the corresponding {@link MultiPointLine}
     */
    private HashMap<SensorType, HashMap<Integer, MultiPointLine>> mSensorMapping = new HashMap<>();

    private List<MultiPointLine> mLines = new ArrayList<>(8);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // calculate some useful sizes
        int backgroundWidth = getWidth() - 2 * PADDING - LABEL_PADDING;
        int xHatchMarkEnd = HATCHMARK_WIDTH + PADDING + LABEL_PADDING;
        int xEnd = getWidth() - PADDING;

        int backgroundHeight = getHeight() - 2 * PADDING;
        int yEnd = getHeight() - PADDING;


        // draw white background
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillRect(
                X_START,
                Y_START,
                backgroundWidth,
                backgroundHeight);


        graphics2D.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        float min = getMinValue(), max = getMaxValue();
        for (int i = 0; i < Y_DIVISION_COUNT + 1; i++) {
            // draw the grid line
            graphics2D.setColor(GRID_COLOR);
            int y = getHeight() - (i * backgroundHeight / Y_DIVISION_COUNT + PADDING);
            graphics2D.drawLine(xHatchMarkEnd + 1, y, xEnd, y);

            // use black for label and hatch mark
            graphics2D.setColor(Color.BLACK);

            // draw the label
            String yLabel = (int) ((min + (max - min) * (((double) i) / Y_DIVISION_COUNT))) + "";
            FontMetrics metrics = graphics2D.getFontMetrics();
            int labelWidth = metrics.stringWidth(yLabel);
            int labelHeight = metrics.getHeight();
            graphics2D.drawString(yLabel, X_START - labelWidth - 5, y + (labelHeight / 2) - 3);

            // draw the hatch mark
            graphics2D.drawLine(X_START, y, xHatchMarkEnd, y);
        }

        // create x and y axes 
        graphics2D.drawLine(X_START, yEnd, X_START, Y_START);
        graphics2D.drawLine(X_START, yEnd, xEnd, yEnd);

        // draw all lines
        double xScale = ((double) backgroundWidth) / (BUFFER_SIZE - 1);
        double yScale = ((double) backgroundHeight) / (max - min);

        for (HashMap<Integer, MultiPointLine> map : mSensorMapping.values())
            for (MultiPointLine line : map.values())
                line.draw(graphics2D, backgroundHeight, xScale, yScale);
    }

    /**
     * Add a new line to be drawn
     *
     * @param sensorType the sensor type that should use this line
     * @param sensorAxis the sensor axis that should be drawn, probably 0, 1 or 2.
     */
    void addLine(SensorType sensorType, int sensorAxis) {
        if (!mSensorMapping.containsKey(sensorType))
            mSensorMapping.put(sensorType, new HashMap<>());

        // use a random color if no predefined is left
        Color color;
        int lineCount = getLineCount();
        if (lineCount < mLineColors.length)
            color = mLineColors[lineCount];
        else
            color = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());

        MultiPointLine newLine = new MultiPointLine(color);
        mSensorMapping.get(sensorType).put(sensorAxis, newLine);
        mLines.add(newLine);
    }

    /**
     * Count the number of lines we have stored
     *
     * @return
     */
    private int getLineCount() {
        return mLines.size();
    }

    private float getMinValue() {
        float min = Float.MAX_VALUE;
        for (MultiPointLine line : mLines)
            min = Math.min(min, line.getMinimum());
        return min;
    }

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
    void removeLine(SensorType sensor, int sensorAxis) {
        if (mSensorMapping.containsKey(sensor)) {
            mLines.remove(mSensorMapping.get(sensor).get(sensorAxis));
            mSensorMapping.get(sensor).remove(sensorAxis);
        }
    }

    /**
     * decide which line should get what data
     *
     * @param networkDevice
     * @param sensorData
     */
    @Override
    public void onData(NetworkDevice networkDevice, SensorData sensorData) {
        try {
            for (Map.Entry<Integer, MultiPointLine> entry : mSensorMapping.get(sensorData.sensorType).entrySet())
                entry.getValue().addPoint(sensorData.data[entry.getKey()]);
        } catch (NullPointerException e) {
            System.out.println("Unknown sensor type received by graph panel");
        }

    }

    @Override
    public void close() {
    }

    GraphPanel(){
        Timer repaintTimer = new Timer();
        repaintTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                repaint();
            }
        }, 30, 30);
    }
}