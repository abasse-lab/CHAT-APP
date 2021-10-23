package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class User {
    private static final AtomicLong count = new AtomicLong(0);
    private final Long idUser;
    private String pseudo;
    private boolean connected;
    private LocalDateTime lastConnection;
    private String lastConversation;

    public User(String pseudo) {
        this.idUser = count.getAndIncrement();
        this.pseudo = pseudo;
        this.connected = false;
        this.lastConnection = LocalDateTime.now();
        this.lastConversation = "";
    }

    public User(String pseudo, LocalDateTime lastConnection, String lastConversation) {
        this.idUser = count.getAndIncrement();
        this.pseudo = pseudo;
        this.connected = false;
        this.lastConnection = lastConnection;
        this.lastConversation = lastConversation;
    }

    public Long getIdUser() {
        return idUser;
    }

    public String getPseudo() {
        return pseudo;
    }

    public void setPseudo(String pseudo) {
        this.pseudo = pseudo;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public LocalDateTime getLastConnection() {
        return lastConnection;
    }

    public void setLastConnection(LocalDateTime lastConnection) {
        this.lastConnection = lastConnection;
    }

    public String getLastConversation() {
        return lastConversation;
    }

    public void setLastConversation(String lastConversation) {
        this.lastConversation = lastConversation;
    }
}
