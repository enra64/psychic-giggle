package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.SensorType;
import de.ovgu.softwareprojekt.util.RingBuffer;

import java.awt.*;
import java.util.Objects;

import static java.lang.Math.abs;

/**
 * This class contains all data and some methods necessary to deal with the data of a single line
 */
public class MultiPointLine {
    /**
     * Stroke we use for drawing
     */
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);

    /**
     * The color that is used to draw data points
     */
    private static final Color POINT_COLOR = new Color(100, 100, 100, 180);

    /**
     * The line color to be used for this line
     */
    private final Color mLineColor;

    /**
     * Storage for our values
     */
    private RingBuffer<Float> mData = new RingBuffer<>(GraphPanel.BUFFER_SIZE, 0.f);

    /**
     * Storage variables to avoid calculating the extrema as much as possible
     */
    private Float mMinValue = Float.MAX_VALUE, mMaxValue = -Float.MAX_VALUE;

    private final SensorType mSensorType;
    private final int mAxis;

    MultiPointLine(Color lineColor, SensorType sensorType, int axis) {
        mLineColor = lineColor;
        mSensorType = sensorType;
        mAxis = axis;
    }

    SensorType getSensorType() {
        return mSensorType;
    }

    int getAxis() {
        return mAxis;
    }

    /**
     * Add a point to this line. The oldest point may be overwritten
     *
     * @param value the new value to display
     */
    void addPoint(float value) {
        Float old = mData.add(value);

        // update the min/max values of this line
        mMinValue = Math.min(mMinValue, value);
        mMaxValue = Math.max(mMaxValue, value);

        // check if the old value was an extreme, because if so, the min/max values must be recalculated
        if (Objects.equals(old, mMaxValue))
            mMaxValue = calcMaxValue();
        if (Objects.equals(old, mMinValue))
            mMinValue = calcMinValue();
    }

    /**
     * Retrieve the minimum value contained in this line
     */
    float getMinimum() {
        return mMinValue;
    }

    /**
     * Retrieve the maximum value contained in this line
     */
    float getMaximum() {
        return mMaxValue;
    }

    /**
     * Recalculate the minimum value contained in this line
     */
    private float calcMinValue() {
        float minValue = Float.MAX_VALUE;
        for (int i = 0; i < mData.size(); i++)
            minValue = Math.min(minValue, mData.get(i));
        return minValue;
    }

    /**
     * Recalculate the maximum value contained in this line
     */
    private float calcMaxValue() {
        float minValue = -Float.MAX_VALUE;
        for (int i = 0; i < mData.size(); i++)
            minValue = Math.max(minValue, mData.get(i));
        return minValue;
    }

    /**
     * Draw this line
     *
     * @param graphics         a {@link Graphics2D} object
     * @param backgroundHeight how high is the white background
     * @param min              the minimum value drawn in any line
     * @param mXScale          the x scale
     * @param mYScale          the y scale
     */
    void draw(Graphics2D graphics, int backgroundHeight, float min, double mXScale, double mYScale) {
        // set fitting color
        graphics.setColor(mLineColor);
        graphics.setStroke(GRAPH_STROKE);

        // the height of the negative numbers is zero if the smallest number is positive, and
        // the absolute required height otherwise
        int negativeHeight = min < 0 ? (int) abs(min * mYScale) : 0;

        // draw all lines
        for (int i = 1; i < mData.size(); i++) {
            graphics.drawLine(
                    GraphPanel.X_START + (int) ((i - 1) * mXScale),
                    GraphPanel.Y_START + (int) (backgroundHeight - ((mData.get(i - 1))) * mYScale) - negativeHeight,
                    GraphPanel.X_START + (int) (i * mXScale),
                    GraphPanel.Y_START + (int) (backgroundHeight - ((mData.get(i))) * mYScale) - negativeHeight);
        }
    }
}