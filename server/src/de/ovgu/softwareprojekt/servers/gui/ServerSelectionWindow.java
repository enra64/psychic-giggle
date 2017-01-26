package de.ovgu.softwareprojekt.servers.gui;

import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.GridLayout;


/**
 * Created by Ulrich on 26.01.2017.
 *
 * This class creates a simple window that displays buttons, which call their respective server
 * and a simple information output area
 */

public class ServerSelectionWindow {

    public ServerSelectionWindow()
    {
        //Create window
        JFrame window = new JFrame();
        window.setSize(800, 600);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //masterPanel holds two sub panels for layout reasons
        JPanel masterPanel = new JPanel(new GridLayout(2, 1));

        //TODO: channel the standard output stream through this textarea
        //This text area displays all information regarding the server usage
        JTextArea consoleText = new JTextArea(" Hello, this is a simple information output");
        consoleText.setEditable(false);

        // This panel is responsible for the button display and their usages
        ButtonPanel btnPanel = new ButtonPanel(consoleText);

        //A scrollpanel is responsible for automatically showing the newest information regarding the servers
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
     * Start the main frame
     */
    public static void start()
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                new ServerSelectionWindow();
            }
        });
    }
}
