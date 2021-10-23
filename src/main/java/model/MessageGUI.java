package model;

public class MessageGUI {
    private String userName;
    private String content;
    private String day;
    private String time;

    public MessageGUI(String userName, String content, String day, String time) {
        this.userName = userName;
        this.content = content;
        this.day = day;
        this.time = time;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
