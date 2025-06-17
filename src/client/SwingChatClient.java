package client;

import games.HangmanGUI;
import utils.EncryptionUtils;

import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.formdev.flatlaf.FlatDarkLaf;


/**
 * A minimal Swing-based chat client that connects to a server and exchanges messages.
 */
public class SwingChatClient extends JFrame {

    private JTextArea chatArea;       // Displays chat messages
    private JTextField inputField;    // User input for sending messages
    private JButton sendButton;       // Send button
    private PrintWriter out;          // Output stream to server

    private final String username;
    private SecretKey sharedKey;


    public SwingChatClient() {
        this.username = promptUsername();
        initializeLookAndFeel();
        setupUI();
        loadKey();
        connectToServer();
    }

    private String promptUsername() {
        String name = JOptionPane.showInputDialog(this, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
        return (name == null || name.trim().isEmpty()) ? "Anonymous" : name.trim();
    }

    private void initializeLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception e) {
            System.err.println("Failed to initialize FlatLaf dark theme");
        }
    }

    private void loadKey() {
        try {
            String keyString = "1234567890ABCDEF"; // must be 16, 24, or 32 chars
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
            sharedKey = EncryptionUtils.loadKeyFromBytes(keyBytes);
        } catch (Exception e) {
            System.err.println("Failed to load shared key");
        }
    }

    private void setupUI() {

        try {
            // Load and set custom .ico icon
            URL iconURL = getClass().getClassLoader().getResource("resources/chat_icon.png");
            if (iconURL != null) {
                Image icon = ImageIO.read(iconURL);
                setIconImage(icon);
            } else {
                System.err.println("Icon not found in resources.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        setTitle("Le Chat des PD(I)");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chat area setup
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Lucida Console", Font.PLAIN, 18));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        // Input area setup
        inputField = new JTextField();
        inputField.setFont(new Font("Lucida Console", Font.PLAIN, 16));
        sendButton = new JButton("Send");

        // Send message on button click or Enter key
        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Optional feature panel for the HangmanGame
        JPanel featurePanel = new JPanel();
        JButton playHangmanBtn = new JButton("🎮 Play Hangman");
        playHangmanBtn.addActionListener(e -> new HangmanGUI());
        featurePanel.add(playHangmanBtn);
        add(featurePanel, BorderLayout.NORTH);

        setVisible(true);
        // Automatically focus the chat area
        SwingUtilities.invokeLater(() -> inputField.requestFocusInWindow());
    }

    private void connectToServer() {
        try {
            Socket socket = new Socket("172.16.64.193", 12345);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(() -> {
                String line;
                try {
                    while ((line = in.readLine()) != null) {
                        String[] parts = line.split("::", 2); // Expect format "encryptedusername::encryptedText"
                        if (parts.length == 2) {
                            String sender = EncryptionUtils.decrypt(parts[0], sharedKey);
                            String decrypted = EncryptionUtils.decrypt(parts[1], sharedKey);
                            LocalDateTime timestamp = LocalDateTime.now();
                            chatArea.append("[" + timestamp.getHour() + ":" + timestamp.getMinute() + "]" + sender + ": " + decrypted + "\n");
                            chatArea.setCaretPosition(chatArea.getDocument().getLength()); // Auto-scroll
                        }
                    }
                } catch (Exception e) {
                    chatArea.append("Connection lost or failed to read.\n");
                }
            }).start();

        } catch (IOException e) {
            chatArea.append("Unable to connect to server.\n");
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && out != null) {
            try {
                String encryptedText = EncryptionUtils.encrypt(text, sharedKey);
                String encryptedUsername = EncryptionUtils.encrypt(username, sharedKey);
                out.println(encryptedUsername + "::" + encryptedText); // Send both username + encrypted content
                inputField.setText("");
            } catch (Exception e) {
                showError("Failed to encrypt and send message");
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SwingChatClient::new);
    }
}
