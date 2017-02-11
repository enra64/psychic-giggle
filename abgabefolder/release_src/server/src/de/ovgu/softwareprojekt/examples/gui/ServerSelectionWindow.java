package de.ovgu.softwareprojekt.examples.gui;

import javax.swing.*;
import java.awt.GridLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 * Created by Ulrich on 26.01.2017.
 * <p>
 * This class creates a simple window that displays buttons, which call their respective server
 * and a simple information output area
 */

public class ServerSelectionWindow {

    private JTextArea consoleText;

    public ServerSelectionWindow() {

        //Create window
        JFrame window = new JFrame();
        window.setSize(800, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //masterPanel holds two sub panels for layout reasons
        JPanel masterPanel = new JPanel(new GridLayout(2, 1));

        //This text area displays all information regarding the server usage
        consoleText = new JTextArea();
        consoleText.setEditable(false);

        //Direct Outputstream through consoleText
        redirectSystemStreams();

        // This panel is responsible for the button display and their usages
        ButtonPanel btnPanel = new ButtonPanel(consoleText);

        //A scrollpanel is responsible for automatically showing the newest information regarding the examples
        JScrollPane scroll = new JScrollPane(consoleText);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        //add panels to masterpanel
        masterPanel.add(btnPanel);
        masterPanel.add(scroll);

        //finally add masterpanel and make window visible for user
        window.add(masterPanel);
        window.setVisible(true);
    }

    /**
     * This method displays text received in an Outputstream in the consoleText JTextarea of the GUI
     * @param text
     */
    private void updateTextArea(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                consoleText.append(text);
            }
        });
    }

    /**
     * This method redirects all System.out and System.err messages through a JTextArea
     */
    private void redirectSystemStreams() {
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                updateTextArea(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                updateTextArea(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };

        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    /**
     * Start the main frame
     */
    public static void start() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerSelectionWindow();
            }
        });
    }
}
