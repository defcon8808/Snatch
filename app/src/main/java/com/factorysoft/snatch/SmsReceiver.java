package com.factorysoft.snatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.PatternSyntaxException;

public class SmsReceiver extends BroadcastReceiver {
    private final String TAG = "SmsReceiver";
    private static final String ACTION_MMS_RECEIVED = "android.provider.Telephony.WAP_PUSH_RECEIVED";
    private static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    private static final String MMS_DATA_TYPE = "application/vnd.wap.mms-message";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d("SmsReceiver", "onReceive()");
        Bundle bundle = intent.getExtras();
        String action = intent.getAction();
        String type = intent.getType();

        SmsMessage[] msgs = null;
        String strMessage = "";

        Log.d("SmsReceiver", action);

        if(action.equals(ACTION_MMS_RECEIVED) && type.equals(MMS_DATA_TYPE)) {
            if (bundle != null) {
                int pduType = bundle.getInt("pduType");
                Log.d(TAG, "pduType : " + pduType);

                byte[] buffer = bundle.getByteArray("header");
                String header = new String(buffer);
                Log.d(TAG, "header : " + header);

                byte[] buffer2 = bundle.getByteArray("data");
                String data = new String(buffer2);
                Log.d(TAG, "Data : " + data);
            }
        } else if(action.equals(ACTION_SMS_RECEIVED)) {
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];

                for (int i = 0; i < msgs.length; i++) {
                    msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    strMessage += msgs[i].getMessageBody().toString();
                }
                Log.d("SmsReceiver", strMessage);
            }
        }

        try {
            strMessage = strMessage.replace("\n", "");

            if(strMessage.matches(".*#자동등록.*")) {
                Log.d(TAG, "태그가 존재함");

            }
        } catch (PatternSyntaxException e) {
            Log.e(TAG, e.toString());
        }
    }
}
