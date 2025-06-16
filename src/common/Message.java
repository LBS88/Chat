package common;

import java.awt.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

public class Message implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L; // Required for Serializable

    private final String sender; // Who sent the message
    private final String content;
    private final LocalDateTime timestamp;
//    private MessageType type;


//    public enum MessageType {
//        CHAT,
//        JOIN,
//        LEAVE,
//        PRIVATE,
//    }

    // Constructor to create a message
    public Message(String sender, String content,  LocalDateTime timestamp) {
        this.sender = sender;
        this.content = content;
//        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

//    public MessageType getType() {
//        return type;
//    }

    @Override
    public String toString() {
        return "[" + timestamp.getHour() + ":" + timestamp.getMinute() + "] " + sender + ": " + content;
    }
}
