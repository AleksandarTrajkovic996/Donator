package com.arteam.donator;

public class Notification extends NotificationID {

    String text;

    public Notification(){

    }

    public Notification(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
