package de.ovgu.softwareprojekt.servers.graphing;

import de.ovgu.softwareprojekt.SensorData;
import org.apache.commons.collections4.queue.CircularFifoQueue;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * This code is taken almost unchanged from a github gist.
 * @author Rodrigo
 */
public class GraphPanel extends JPanel {
    private Color line1Color = new Color(255, 0, 0, 180);
    private Color line2Color = new Color(0, 255, 0, 180);
    private Color line3Color = new Color(0, 0, 255, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);

    private CircularFifoQueue<float[]> dataBuffer = new CircularFifoQueue<>(512);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 25;
        int labelPadding = 25;
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (dataBuffer.size() - 1);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        int pointWidth = 4;
        int numberYDivisions = 10;
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (dataBuffer.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                String yLabel = ((int) ((getMinScore() + (getMaxScore() - getMinScore()) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < dataBuffer.size(); i++) {
            if (dataBuffer.size() > 1) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (dataBuffer.size() - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                if ((i % ((int) ((dataBuffer.size() / 20.0)) + 1)) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    String xLabel = i + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        drawLine(g2, line1Color, 0);
        drawLine(g2, line2Color, 1);
        drawLine(g2, line3Color, 2);
    }

    private void drawLine(Graphics2D g2, Color lineColor, int dataBufferArrayPos){
        int padding = 25;
        int labelPadding = 25;
        double xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (dataBuffer.size() - 1);
        double yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore() - getMinScore());

        // create a list of points from the data buffer
        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < dataBuffer.size(); i++) {
            int x1 = (int) (i * xScale + padding + labelPadding);
            int y1 = (int) ((getMaxScore() - dataBuffer.get(i)[dataBufferArrayPos]) * yScale + padding);
            graphPoints.add(new Point(x1, y1));
        }

        Stroke oldStroke = g2.getStroke();
        g2.setColor(lineColor);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        g2.setStroke(oldStroke);
        g2.setColor(pointColor);
        for (Point graphPoint : graphPoints) {
            int x = graphPoint.x - 4 / 2;
            int y = graphPoint.y - 4 / 2;
            g2.fillOval(x, y, 4, 4);
        }
    }

    private double getMinScore() {
        double minScore = Double.MAX_VALUE;
        for (float[] score : dataBuffer) {
            float tmpMin = Float.MAX_VALUE;
            for(Float f : score)
                tmpMin = Math.min(tmpMin, f);

            minScore = Math.min(minScore, tmpMin);
        }
        return minScore;
    }

    private double getMaxScore() {
        double maxScore = Double.MIN_VALUE;
        for (float[] score : dataBuffer) {
            float tmpMax = -Float.MAX_VALUE;
            for(Float f : score)
                tmpMax = Math.max(tmpMax, f);
            maxScore = Math.max(maxScore, tmpMax);
        }
        return maxScore;
    }


    public void addData(SensorData data) {
        dataBuffer.add(data.data);
        invalidate();
        this.repaint();
    }
}