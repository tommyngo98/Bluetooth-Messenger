package com.example.mymessenger;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.nio.charset.Charset;
import java.util.UUID;

public class MessengerActivity extends AppCompatActivity {
    private MessageAdapter msgAdapter;
    private ListView messagesView;
    private EditText editText;
    private Button save;
    private MessageFileMemento memento;
    private ConnectionService connection;

    BluetoothDevice device;
    UUID uuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        memento = new MessageFileMemento(getApplicationContext());
        setContentView(R.layout.activity_messenger);
        save = findViewById(R.id.saveBtn);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveToMemento();
            }
        });
        // manual save button for debugging saving messages
        // remove the line below to make it visible
        save.setVisibility(View.GONE);
        editText = (EditText) findViewById(R.id.editText);
        msgAdapter = new MessageAdapter(this);
        messagesView = (ListView) findViewById(R.id.messages_view);
        messagesView.setAdapter(msgAdapter);
        connection = new ConnectionService(MessengerActivity.this);


        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter("incomingMessage"));

        device = getIntent().getParcelableExtra("btDevice");
        Log.d("Messenger", device.getName());
        uuid = (UUID) getIntent().getSerializableExtra("uuid");
        Log.d("Messenger", uuid.toString());
        restoreFromMemento();
        startConnection();
    }

    public void startConnection() {
        connection.startClient(device, uuid);
        Log.d("Messenger", "starting connection device:" + device.getName());
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("theMessage");
            onMessage(text);
        }
    };

    public void onMessage(String text){
        String time = TimePrinter.getCurrentTime();
        String sender = device.getName();
        Message message = new Message(text, sender, time, false);
        msgAdapter.add(message);
    }

    public void sendMessage(View view){
        String message = editText.getText().toString();
        String time = TimePrinter.getCurrentTime();
        if (message.length() > 0) {
            byte[] bytes = message.getBytes(Charset.defaultCharset());
            connection.write(bytes);
            msgAdapter.add(new Message(message,"me", time, true ));
            editText.getText().clear();
        }
    }

    protected void onDestroy(){
        saveToMemento();
        super.onDestroy();
    }
    public void saveToMemento() {
        memento.saveMessages(msgAdapter.getMessages(), device.getAddress());
    }

    public void restoreFromMemento(){
        Log.d("messenger", "Restoring from memento..");
       msgAdapter.addAll(memento.restoreMessages(device.getAddress(), device.getName()));
    }
}
