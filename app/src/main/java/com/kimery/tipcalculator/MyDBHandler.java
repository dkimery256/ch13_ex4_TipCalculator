package com.kimery.tipcalculator;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;
import android.content.Context;
import android.util.Log;

import java.util.ArrayList;


public class MyDBHandler extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "tips.db";
    public static final String TABLE_SAVED_TIPS = "savedTips";
    public static final String COLUMN_ID = "_id";
    public static final int COLUMN_ID_COL = 0;
    public static final String COLUMN_BILL_DATE = "bill_date";
    public static final int COLUMN_BILL_DATE_COL = 1;
    public static final String COLUMN_BILL_AMOUNT = "bill_amount";
    public static final int COLUMN_BILL_AMOUNT_COL = 2;
    public static final String COLUMN_TIP_PERCENT = "tip_percent";
    public static final int COLUMN_TIP_PERCENT_COL = 3;
    private static final String TAG = "MyDBHandler";
    private int entries = 0;
    ArrayList<Tip> tips = new ArrayList<Tip>();
    Tip tip;


    public MyDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create and add 2 entries
        String CREATE_TABLE =
            "CREATE TABLE "    + TABLE_SAVED_TIPS + "(" +
            COLUMN_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_BILL_DATE   + " INTEGER, " +
            COLUMN_BILL_AMOUNT + " REAL, " +
            COLUMN_TIP_PERCENT + " REAL);";

        db.execSQL(CREATE_TABLE);

        String insert = "INSERT INTO " + TABLE_SAVED_TIPS + " VALUES (1, 0, 45.63, 0.20)";
        db.execSQL(insert);
        insert = "INSERT INTO " + TABLE_SAVED_TIPS + " VALUES (2, 0, 105.32, 0.18)";
        db.execSQL(insert);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SAVED_TIPS);
        onCreate(db);

    }

    public ArrayList<Tip> getTips(int num) {
        //pass 1 only for first db access to build array, else just return array.
        if (num == 1){
            String query = "SELECT * FROM " + TABLE_SAVED_TIPS + ";";

            SQLiteDatabase db = getReadableDatabase();
            // cursor point to a location in your results
            Cursor c = db.rawQuery(query, null);
            // move to the first row in your results
            c.moveToFirst();
            //Add tip objects to the tips array list
            while (!c.isAfterLast()) {

                int id = c.getInt(COLUMN_ID_COL);
                int billDate = c.getInt(COLUMN_BILL_DATE_COL);
                float billAmount = c.getFloat(COLUMN_BILL_AMOUNT_COL);
                float tipPercent = c.getFloat(COLUMN_TIP_PERCENT_COL);

                tip = new Tip(id, billDate, billAmount, tipPercent);

                tips.add(tip);
                //increment entry number for later inserts
                entries++;
                c.moveToNext();
            }
            //close database
            db.close();
        }else{

        }
        return tips;
    }
    public String getArray(ArrayList <Tip> t){
        String output = "";
        String delimiter = "\n";
        //loop to separate the array elements
        for ( int i = 0; i < t.size(); i ++ ){
            String data = t.get(i).toString();
            output = output + data + delimiter;
        }
        return output;
    }
    public void saveTip(float billAmount, float tipPercent){
        //set entry point for table
        int entry = entries + 1;
        //get system date to display latest saved date later
        long date = System.currentTimeMillis();
        //create new object to add to the array list
        Tip savedTip = new Tip(entry, date, billAmount, tipPercent);
        SQLiteDatabase db =  getWritableDatabase();
        //Insert data on the screen
        String insert = "INSERT INTO " + TABLE_SAVED_TIPS + " VALUES (" + entry + ", " + date +", " +
                billAmount + ", " + tipPercent + ");";
        db.execSQL(insert);
        //increment entry number for later inserts
        entries++;
        //Add object to array list for later use of the getTips method
        tips.add(savedTip);
        db.close();
    }
    public String getDate(){
        SQLiteDatabase db =  getWritableDatabase();
        //query max date
        String insert = "SELECT MAX(" + COLUMN_BILL_DATE + ") FROM " + TABLE_SAVED_TIPS;
        Cursor c = db.rawQuery(insert, null);
        c.moveToFirst();
        //get the max date from the cursor
        long date = c.getLong(0);
        //convert long date into simple date format
        String newDate = tip.getDateStringFormattedFromDB(date);
        db.close();
        return newDate;
    }
    public float getAverage(){
        SQLiteDatabase db =  getWritableDatabase();
        //query average of the tip percent column
        String insert = "SELECT  AVG(" + COLUMN_TIP_PERCENT + ")FROM " + TABLE_SAVED_TIPS;
        Cursor c = db.rawQuery(insert, null);
        c.moveToFirst();
        //get the average from the cursor
        float average = c.getFloat(0);
        db.close();
        Log.d(TAG, "\nAverage: " + average);
        return average;
    }

}