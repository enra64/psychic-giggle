package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.util.RingBuffer;

import java.awt.*;
import java.util.Objects;

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

    public MultiPointLine(Color lineColor) {
        mLineColor = lineColor;
    }

    /**
     * Add a point to this line. The oldest point may be overwritten
     *
     * @param value the new value to display
     */
    public void addPoint(float value) {
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
    public float getMinimum() {
        return mMinValue;
    }

    /**
     * Retrieve the maximum value contained in this line
     */
    public float getMaximum() {
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
     * @param graphics a {@link Graphics2D} object
     * @param mXScale  the x scale
     * @param mYScale  the y scale
     */
    public void draw(Graphics2D graphics, int drawingAreaHeight,  double mXScale, double mYScale) {
        Stroke oldStroke = graphics.getStroke();

        for (int i = 1; i < mData.size(); i++) {
            int x1 = GraphPanel.X_START + (int) ((i - 1) * mXScale);
            int y1 = GraphPanel.Y_START + drawingAreaHeight - (int) (mData.get(i - 1) * mYScale);
            int x2 = GraphPanel.X_START + (int) (i * mXScale);
            int y2 = GraphPanel.Y_START + drawingAreaHeight - (int) (mData.get(i) * mYScale);

            // draw the line using a the graphing stroke and the specified color
            graphics.setColor(mLineColor);
            graphics.setStroke(GRAPH_STROKE);
            graphics.drawLine(x1, y1, x2, y2);

            // draw a circle over the point
            graphics.setStroke(oldStroke);
            graphics.setColor(POINT_COLOR);
            graphics.fillOval(x2 - 2, y2 - 2, 4, 4);
        }
    }
}