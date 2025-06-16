package client;

import common.Message;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;

/**
 * A minimal Swing-based chat client that connects to a server and exchanges messages.
 */
public class SwingChatClient extends JFrame {

    private JTextArea chatArea;       // Displays chat messages
    private JTextField inputField;    // User input for sending messages
    private JButton sendButton;       // Send button
    private PrintWriter out;          // Output stream to server

    private final String username;

    public SwingChatClient() {
        this.username = promptUsername();
        setupUI();
        connectToServer();
    }

    private String promptUsername() {
        String name = JOptionPane.showInputDialog(this, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
        return (name == null || name.trim().isEmpty()) ? "Anonymous" : name.trim();
    }

    private void setupUI() {
        setTitle("Chat Client");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chat area setup
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input area setup
        inputField = new JTextField();
        sendButton = new JButton("Send");

        // Send message on button click or Enter key
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("172.16.64.193", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Thread to read messages from the server
            new Thread(() -> {
                String msg;
                try {
                    while ((msg = in.readLine()) != null) {
                        chatArea.append(msg + "\n");
                    }
                } catch (IOException e) {
                    chatArea.append("Disconnected from server.\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Unable to connect to server.\n");
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        Message msg =  new Message(username, text, LocalDateTime.now());
        if (!text.isEmpty() && out != null) {
            out.println(msg.toString());
            inputField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingChatClient::new);
    }
}
