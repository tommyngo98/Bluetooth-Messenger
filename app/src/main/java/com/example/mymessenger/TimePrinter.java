package com.example.mymessenger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimePrinter {

    public static String getCurrentTime() {

        Date date = new Date();

        String strDateFormat = "HH:mm";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        String formattedDate= dateFormat.format(date);

       return formattedDate;

    }
}
