package com.example.mymessenger;

import android.util.Log;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class MessageStringMemento {
    public final static String MSG_DELIMITER = "|||";
    public static final String ELEMENT_DELIMITER = "~~";

    public String listToString(ArrayList<Message> messages){
        StringBuilder builder = new StringBuilder();
        if(messages != null && messages.size() > 0){
            for (int i = 0; i < messages.size(); i++){
                Message msg = messages.get(i);
                builder.append(msg.getText());
                builder.append(ELEMENT_DELIMITER);
                builder.append(msg.getTime());
                builder.append(ELEMENT_DELIMITER);
                if(msg.isBelongsToCurrentUser()){
                    builder.append("true");
                }
                else {
                    builder.append("false");
                }
                builder.append(MSG_DELIMITER);

            }
        }
        return builder.toString();
    }

    public ArrayList<Message> stringToList(String serialized, String sender){
        ArrayList<Message>messages = new ArrayList<>();
        StringTokenizer st = new StringTokenizer(serialized, MSG_DELIMITER);
        while (st.hasMoreTokens()){
            Message reconstMessage = stringToMessage(st.nextToken(), sender);
            Log.d("StringMemento", "rekonstruierte nachricht: " + reconstMessage.getText());
            messages.add(reconstMessage);
        }
        return messages;
    }

    public Message stringToMessage(String serializedMsg, String sender){
        Log.d("StringMemento", "tokenized message" + serializedMsg + "sender: " + sender);
        StringTokenizer st = new StringTokenizer(serializedMsg, ELEMENT_DELIMITER);
        String text = "";
        String time = "";
        String sentByMe = "";
        if(st.countTokens() == 3) {
            text = st.nextToken();
            time = st.nextToken();
            sentByMe= st.nextToken();
        }
        boolean byMe =  sentByMe.equals("true");
        Message message = null;
        if(byMe){
           message = new Message(text, time, byMe);
        }
        else{
            message = new Message(text, sender, time, byMe );
        }
        return message;
    }
}
