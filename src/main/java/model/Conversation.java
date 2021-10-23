package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
// protected keyword is an access modifier for method and variable of a class
public class Conversation {
    protected static final AtomicLong count = new AtomicLong(0);
    protected final Long idConversation;
    protected final List<Message> messages = new ArrayList<>();
    protected String name;
    //private can only be accessed within the class in which they declared
    private final List<User> users = new ArrayList<>();
    private LocalDateTime timeLastMessage;

    public Conversation(String name, List<User> users, LocalDateTime timeLastMessage) {
        //this means current object of the reference variable
        //getandincrement variable will increase the value by one and return the value before updation . type int
        this.idConversation = count.getAndIncrement();
        this.name = name;
        this.users.addAll(users);
        this.timeLastMessage = timeLastMessage;
    }

    public Long getIdConversation() {
        return idConversation;
    }

    public String getName() {
        return name;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        if(!users.contains(user)) {
            users.add(user);
        }
    }

    public LocalDateTime getTimeLastMessage() {
        return timeLastMessage;
    }

    public void setTimeLastMessage(LocalDateTime timeLastMessage) {
        this.timeLastMessage = timeLastMessage;
    }
}
