package de.ovgu.softwareprojekt.servers.gui;


import de.ovgu.softwareprojekt.networking.AbstractServer;
import de.ovgu.softwareprojekt.servers.kuka.KukaServer;
import de.ovgu.softwareprojekt.servers.mouse.MouseServer;
import de.ovgu.softwareprojekt.servers.nes.NesServer;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JLabel;

import java.awt.GridLayout;
import java.awt.AWTException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Created by Ulrich on 26.01.2017.
 *
 * This panel creates buttons which call their respective servers or closes them before opening another
 */
public class ButtonPanel extends JPanel implements ActionListener {

    private AbstractServer server;
    private JButton startMouseBtn, startNesBtn, startKukaBtn, closeBtn;
    private JTextArea console;
    private boolean isRunning;

    public ButtonPanel(JTextArea textArea)
    {
        console = textArea;

        //Set Layout and create panels
        this.setLayout(new GridLayout(3,1));

        //Order decides the position
        this.add(createServerButtons());
        this.add(new JLabel(" ")); //empty label for layout reasons
        this.add(createCloseButton());
    }

    /**
     * This Panel holds a button which closes the server connection
     * @return the Panel responsible for displaying the close Button
     */
    private JPanel createCloseButton()
    {
        JPanel closePan = new JPanel(new GridLayout(1, 3));

        //Setup layout and create close button
        closePan.add(new JLabel(" "));

        closeBtn = new JButton("Close Servers");
        closeBtn.addActionListener(this);

        closePan.add(closeBtn);

        closePan.add(new JLabel(" "));

        return closePan;
    }

    /**
     * This Panel holds the 3 server start buttons
     * @return the Panel responsible for displaying the close Button
     */
    private JPanel createServerButtons()
    {
        JPanel serverButtonPan = new JPanel(new GridLayout(1, 5));

        //Create buttons and setup layout
        startMouseBtn = new JButton("Mouse");
        serverButtonPan.add(startMouseBtn);
        serverButtonPan.add(new JLabel(" "));

        startNesBtn = new JButton("Nes");
        serverButtonPan.add(startNesBtn);
        serverButtonPan.add(new JLabel(" "));

        startKukaBtn = new JButton("kuka");
        serverButtonPan.add(startKukaBtn);

        //Give buttons an actionlistener
        startMouseBtn.addActionListener(this);
        startNesBtn.addActionListener(this);
        startKukaBtn.addActionListener(this);

        return serverButtonPan;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
            try {
                //TODO: Shit code alert ... closeButton should only close if server is running but setting isRunning is fucked up
                if(e.getSource() == closeBtn && isRunning)
                {
                    server.close();
                    console.append("\nServer closed");
                    isRunning = false;
                }

                else if(isRunning)
                {
                    server.close();
                    console.append("\nServer closed");
                    isRunning = false;
                }

                //Check for the specific button input
                if(e.getSource() == startMouseBtn) {
                    server = new MouseServer(null);
                    console.append("\nMouseServer started");
                    isRunning = true;
                }
                else if(e.getSource() == startNesBtn) {
                    server = new NesServer(null);
                    console.append("\nNesServer started");
                    isRunning = true;
                }
                else if(e.getSource() == startKukaBtn) {
                    server = new KukaServer();
                    console.append("\nKukaServer started");
                    isRunning = true;
                }

                // doesn't look nice that same if-statement is needed ...
                if(isRunning)
                    server.start();

                //scrolls down automatically if the textarea border is reached
                console.setCaretPosition(console.getDocument().getLength());
            } catch (IOException | AWTException exc) {
                exc.printStackTrace();
            }
    }
}
