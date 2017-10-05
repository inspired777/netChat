package com.myfirstchat.netChat.client;

import com.myfirstchat.netChat.network.TCPConnection;
import com.myfirstchat.netChat.network.TCPConnectionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * Class creates window for the chat, using JFrame.
 * The window contains a textField for the nick name, a textArea for the list of messages, a textField to input message.
 * */
public class ClientWindow extends JFrame implements ActionListener, TCPConnectionListener {

    private static final String IP_ADDR = "192.168.0.100";  // hardcoding the IP address of the server
    private static final int PORT = 8189;                   // hardcoding the port
    private static final int WIDTH = 600;                   // width of the window
    private static final int HEIGHT = 400;                  // height of the window

/* the main thread of the client side (EDT - event dispatching thread)  */
// first - run the server, then - run the client
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ClientWindow();     // run the instance of the current class in EDT
            }
        });
    }

    private final JTextField fieldNickName = new JTextField("nick");    // for a nick name
    private final JTextArea log = new JTextArea();                      // for users' messages
    private final JTextField fieldInput = new JTextField();             // for a message

    private TCPConnection connection;                                   // for connection

    /* constructor */
    private ClientWindow() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(WIDTH, HEIGHT);

        add(fieldNickName, BorderLayout.NORTH);         // add textField for nick name to the window

        setLocationRelativeTo(null);                    // center of the screen
        setAlwaysOnTop(true);                           // always on top of other windows

        log.setEditable(false);                         // forbid editing sent messages
        log.setLineWrap(true);                          // lines will be wrapped if they are too long
        add(log, BorderLayout.CENTER);                  // add textArea for messages to the window

        fieldInput.addActionListener(this);          // add action listener to the field with input message to act on pressing Enter
        add(fieldInput, BorderLayout.SOUTH);            // add textField for input message to the window

        setVisible(true);                               // make the window visible

        try {
            // try to set new connection with the give IP address and port
            connection = new TCPConnection(this, IP_ADDR, PORT);
        } catch (IOException e) {
            printMessage("Connection exception: " + e);
        }

    }

    @Override  /* implementing ActionListener interface */
    public void actionPerformed(ActionEvent e) {
        String msg = fieldInput.getText();      // save input message to msg variable
        if(msg.equals("")) {                    // if the message is empty, don't send it
            return;
        }
        fieldInput.setText(null);               // clear fieldInput
        connection.sendString(fieldNickName.getText() + ": " + msg);    // send input message to other users
    }

    /* implementng TCPConnectionListener interface */
    @Override
    public void onConnectionReady(TCPConnection tcpConnection) {
        printMessage("Connection is ready...");
    }

    @Override
    public void onReceiveString(TCPConnection tcpConnection, String value) {
        printMessage(value);
    }

    @Override
    public void onDisconnect(TCPConnection tcpConnection) {
        printMessage("Connection is closed.");
    }

    @Override
    public void onException(TCPConnection tcpConnection, Exception e) {
        printMessage("Connection exception: " + e);
    }

    // an application thread needs to update the GUI
    private synchronized void printMessage(String msg) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                log.append(msg + "\n");
                log.setCaretPosition(log.getDocument().getLength());  // set caret position, so the chat will scroll down
            }
        });
    }
}
