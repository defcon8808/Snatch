package com.factorysoft.snatch;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class CreateNotification extends Activity {
    private static SQLiteDatabase db;
    private String title, content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_notification);
    }

    private void queryDB(Context context, int id) {
        DBHelper helper = new DBHelper(context);

        try {
            db = helper.getWritableDatabase();
        } catch (SQLiteException e) {
            db = helper.getReadableDatabase();
        }

        Cursor query = db.rawQuery("SELECT title, content FROM memo WHERE _id='"+ id +"'", null);
        //Log.d("CreateNotification", "count : " + query.getCount());

        while(query.moveToNext()) {
            title = query.getString(query.getColumnIndex("title"));
            content = query.getString(query.getColumnIndex("content"));
            query.close();
        }
    }

    public void showNotification(Context context, int requestCode) {
        queryDB(context, requestCode);

        Intent intent = new Intent(context, EditMemo.class);
        intent.putExtra("id", requestCode);

        PendingIntent contentIntent = PendingIntent.getActivity(context, requestCode,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
                        .setContentTitle(title)
                        .setContentText(content);

        mBuilder.setContentIntent(contentIntent);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(requestCode, mBuilder.build());
    }
}
