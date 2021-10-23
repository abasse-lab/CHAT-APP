package model;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.atomic.AtomicLong;

public class Message {
    private static final AtomicLong count = new AtomicLong(0);
    private final Long idMessage;
    private String content;
    private User user;
    private LocalDateTime time;

    public Message(String content, User user, LocalDateTime time) {
        this.idMessage = count.getAndIncrement();
        this.content = content;
        this.user = user;
        this.time = time;
    }

    public Long getIdMessage() {
        return idMessage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
