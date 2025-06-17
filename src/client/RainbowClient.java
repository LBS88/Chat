
package client;

import utils.EncryptionUtils;

import javax.crypto.SecretKey;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import com.formdev.flatlaf.FlatDarkLaf;

public class RainbowClient extends JFrame {

    private JTextPane chatPane;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private SecretKey sharedKey;

    private final String username;

    private static String getEthernetExterneIp() {
        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces = java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface iface = interfaces.nextElement();
                if (iface.getDisplayName().equalsIgnoreCase("Hyper-V Virtual Ethernet Adapter #2")) {
                    java.util.Enumeration<java.net.InetAddress> addresses = iface.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        java.net.InetAddress addr = addresses.nextElement();
                        if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
    }

    private final String clientIp = getEthernetExterneIp();

    public RainbowClient() {
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
            String keyString = "1234567890ABCDEF";
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
            sharedKey = EncryptionUtils.loadKeyFromBytes(keyBytes);
        } catch (Exception e) {
            System.err.println("Failed to load shared key");
        }
    }

    private void setupUI() {
        try {
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

        setTitle("Chat Client");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatPane = new JTextPane();
        chatPane.setEditable(false);
        chatPane.setFont(new Font("Lucida Console", Font.PLAIN, 16));
        JScrollPane scrollPane = new JScrollPane(chatPane);
        add(scrollPane, BorderLayout.CENTER);

        inputField = new JTextField();
        inputField.setFont(new Font("Lucida Console", Font.PLAIN, 16));
        sendButton = new JButton("Send");

        sendButton.addActionListener(e -> sendMessage());
        inputField.addActionListener(e -> sendMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        setVisible(true);
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
                        String[] parts = line.split("::", 2);
                        if (parts.length == 2) {
                            String sender = EncryptionUtils.decrypt(parts[0], sharedKey);
                            String decrypted = EncryptionUtils.decrypt(parts[1], sharedKey);
                            SwingUtilities.invokeLater(() -> appendMessage(sender, decrypted));
                        }
                    }
                } catch (Exception e) {
                    appendSystemMessage("Connection lost or failed to read.");
                }
            }).start();

        } catch (IOException e) {
            appendSystemMessage("Unable to connect to server.");
        }
    }

    private void appendSystemMessage(String message) {
        try {
            StyledDocument doc = chatPane.getStyledDocument();
            doc.insertString(doc.getLength(), message + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String sender, String content) {
        StyledDocument doc = chatPane.getStyledDocument();
        try {
            String time = "[" + LocalDateTime.now().getHour() + ":" + LocalDateTime.now().getMinute() + "] ";
            doc.insertString(doc.getLength(), time, null);

            boolean rainbow = sender.equals("Cordon") || clientIp.equals("172.16.64.182") || sender.contains("(I'm Gay)");

            if (rainbow) {
                Color[] rainbowColors = {
                        Color.RED, new Color(255, 165, 0), Color.YELLOW,
                        Color.GREEN, Color.BLUE, new Color(75, 0, 130), new Color(238, 130, 238)
                };
                for (int i = 0; i < sender.length(); i++) {
                    SimpleAttributeSet attr = new SimpleAttributeSet();
                    StyleConstants.setForeground(attr, rainbowColors[i % rainbowColors.length]);
                    doc.insertString(doc.getLength(), String.valueOf(sender.charAt(i)), attr);
                }
            } else {
                doc.insertString(doc.getLength(), sender, null);
            }

            doc.insertString(doc.getLength(), ": " + content + "\n", null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (!text.isEmpty() && out != null) {
            try {
                String visibleUsername = username;
                if (clientIp.equals("172.16.64.182")) {
                    visibleUsername += " (I'm Gay)";
                }
                String encryptedText = EncryptionUtils.encrypt(text, sharedKey);
                String encrytedUsername = EncryptionUtils.encrypt(visibleUsername, sharedKey);
                out.println(encrytedUsername + "::" + encryptedText);
                inputField.setText("");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Failed to encrypt and send message");
            }
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        System.out.println(getEthernetExterneIp());
        SwingUtilities.invokeLater(RainbowClient::new);
    }
}