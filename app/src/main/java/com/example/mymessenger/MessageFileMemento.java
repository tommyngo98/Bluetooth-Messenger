package com.example.mymessenger;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MessageFileMemento extends MessageStringMemento {
    private static final String TAG = "FileMemento";
    private Context context;
    private File directory;
    private String filename;
    private FileInputStream logStream;
    private File log;

    public MessageFileMemento(Context context){
        this.context = context;
        directory = context.getFilesDir();
        filename = "log.txt";
    }

    public void saveMessages(ArrayList<Message> messages, String adress){
        String formattedAdress = adress.replace(":", "-");
        filename = formattedAdress + ".txt";
        Toast.makeText(context, "Saved to " + directory + "/" + filename, Toast.LENGTH_LONG).show();
        String messageString = listToString(messages);
        Log.d(TAG, messageString);
        FileOutputStream outputStream = null;

        try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(messageString.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException e) {
                e.printStackTrace();
                }
            }
        }
    }

    public ArrayList<Message> restoreMessages(String adress, String sender){
        String formattedAdress = adress.replace(":", "-");
        filename = formattedAdress + ".txt";

        ArrayList<Message> messages = new ArrayList<>();

        FileInputStream inputStream = null;
        try {
            inputStream = context.openFileInput(filename);
            //InputStreamReader inputReader = new InputStreamReader(inputStream);
           BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(inputStream));
           StringBuilder builder = new StringBuilder();
           String text;
           while ((text = bufferedReader.readLine()) != null){
               builder.append(text);
           }
           messages = stringToList(builder.toString(), sender);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (int i = 0; i< messages.size(); i++){
            Log.d(TAG,  i + " " +  messages.get(i).getText());
        }
        return messages;
    }
}
