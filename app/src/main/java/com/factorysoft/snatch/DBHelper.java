package com.factorysoft.snatch;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * Created by defcon-Dev on 2014-03-26.
 */
public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "memo.db";
    private static final int DATABASE_VERSION = 1;
    public Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        //Log.d("DBHelper", "생성자호출");
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        //sqLiteDatabase.execSQL("CREATE TABLE memo (_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT, content TEXT, rgb TEXT, time DATE);");
        sqLiteDatabase.execSQL("CREATE TABLE memo (_id INTEGER PRIMARY KEY, title TEXT, content TEXT, rgb TEXT, time DATE, addr TEXT);");
        //Log.d("DB", "DB 생성");
        //Toast.makeText(context, "DB 생성", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS memo");
        onCreate(sqLiteDatabase);
    }
}
