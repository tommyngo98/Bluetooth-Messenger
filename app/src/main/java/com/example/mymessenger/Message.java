package com.example.mymessenger;

public class Message {
    private String text; // message body
    private String sender; // data of the user that sent this message
    private String time;
    private boolean belongsToCurrentUser; // is this message sent by us?

    public Message(String text, String sender, String time, boolean belongsToCurrentUser) {
        this.text = text;
        this.sender = sender;
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public Message(String text, String time, boolean belongsToCurrentUser) {
        this.text = text;
        if(belongsToCurrentUser) {
            this.sender = "me";
        }
        else {
            this.sender = "unknown";
        }
        this.time = time;
        this.belongsToCurrentUser = belongsToCurrentUser;
    }

    public String getText() {
        return text;
    }

    public String getSender() {
        return sender;
    }

    public String getTime(){ return time; }

    public boolean isBelongsToCurrentUser() {
        return belongsToCurrentUser;
    }
}
