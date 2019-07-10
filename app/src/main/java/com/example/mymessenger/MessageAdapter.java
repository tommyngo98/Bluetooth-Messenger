package com.example.mymessenger;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    ArrayList<Message> messages = new ArrayList<Message>();
    Context context;

    public MessageAdapter(Context context) {
        this.context = context;
    }

    public void add(Message message) {
        this.messages.add(message);
        notifyDataSetChanged(); // to render the list we need to notify
    }

    public void addAll(ArrayList<Message> newMessages){
        for(int i = 0; i < newMessages.size(); i++ ){
            add(newMessages.get(i));
        }
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    // This is the backbone of the class, it handles the creation of single ListView row (chat bubble)
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        MessageViewHolder holder = new MessageViewHolder();
        LayoutInflater messageInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        Message message = messages.get(i);

        if (message.isBelongsToCurrentUser()) { // this message was sent by us so let's create a basic chat bubble on the right
            convertView = messageInflater.inflate(R.layout.my_message, null);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);
            holder.messageBody.setText(message.getText());
            holder.time.setText(message.getTime());

        } else { // this message was sent by someone else so let's create an advanced chat bubble on the left
            convertView = messageInflater.inflate(R.layout.others_message, null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.messageBody = (TextView) convertView.findViewById(R.id.message_body);
            holder.time = (TextView) convertView.findViewById(R.id.time);
            convertView.setTag(holder);

            holder.name.setText(message.getSender());
            holder.messageBody.setText(message.getText());
            holder.time.setText(message.getTime());
        }

        return convertView;
    }

    public ArrayList<Message> getMessages(){
        return messages;
    }
}

class MessageViewHolder {
    public TextView name;
    public TextView messageBody;
    public TextView time;
}
