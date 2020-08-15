package com.lionuncle.redirecting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class SmsListener extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "SMS reveived in new app", Toast.LENGTH_SHORT).show();
                SharedPreferences sp = context.getSharedPreferences("check", MODE_PRIVATE);
        boolean isSms = sp.getBoolean("isSms", false);
        if (!isSms){
            return;
        }
//        Toast.makeText(context, "sms"+isSms, Toast.LENGTH_SHORT).show();
        if (Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) {

            for (SmsMessage smsMessage : Telephony.Sms.Intents.getMessagesFromIntent(intent)) {

                String messageBody = smsMessage.getMessageBody();
                String address = smsMessage.getOriginatingAddress();
                String message = "[" + address + "] " + messageBody;
                String number = context.getSharedPreferences("data", MODE_PRIVATE).getString("number", "");
                if (number != null && number.equals("")) {
                    return;
                }
                SmsManager.getDefault().sendTextMessage(number,null,message,null,null);
            }
        }
    }
}
