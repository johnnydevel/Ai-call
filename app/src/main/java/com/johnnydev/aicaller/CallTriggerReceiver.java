package com.johnnydev.aicaller;

import android.app.*;
import android.content.*;

public class CallTriggerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        String action = intent.getAction();

        // 1. Decline / Hang Up
        if ("com.johnnydev.HIDE_CALL".equals(action) || "com.johnnydev.BTN_DECLINE".equals(action)) {
            nm.cancel(9991);
            ctx.sendBroadcast(new Intent("com.johnnydev.CLOSE_ACTIVITY"));

            if ("com.johnnydev.BTN_DECLINE".equals(action)) {
                Intent md = new Intent("com.johnnydev.ACTION_DECLINED");
                md.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                ctx.sendBroadcast(md);
            }
            return;
        }

        String chanId = "real_call_ui_v4";
        NotificationChannel chan = new NotificationChannel(chanId, "AI Calls", NotificationManager.IMPORTANCE_HIGH);
        chan.setSound(null, null);
        nm.createNotificationChannel(chan);
        int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;

        Intent ans = new Intent(ctx, CallTriggerReceiver.class).setAction("com.johnnydev.BTN_ANSWER");
        PendingIntent ansPi = PendingIntent.getBroadcast(ctx, 101, ans, flags);

        Intent dec = new Intent(ctx, CallTriggerReceiver.class).setAction("com.johnnydev.BTN_DECLINE");
        PendingIntent decPi = PendingIntent.getBroadcast(ctx, 102, dec, flags);

        Intent fs = new Intent(ctx, MainActivity.class);
        fs.putExtra("mode", "CALL_SCREEN");
        fs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fsPi = PendingIntent.getActivity(ctx, 103, fs, flags);

        Person caller = new Person.Builder().setName("Coder Mode AI").setImportant(true).build();

        // 2. Answer / Ongoing Call
        if ("com.johnnydev.ONGOING_CALL".equals(action) || "com.johnnydev.BTN_ANSWER".equals(action)) {
            Notification ongoing = new Notification.Builder(ctx, chanId)
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setStyle(Notification.CallStyle.forOngoingCall(caller, decPi))
                .setUsesChronometer(true)
                .setOngoing(true)
                .build();
            nm.notify(9991, ongoing);

            ctx.sendBroadcast(new Intent("com.johnnydev.SWITCH_ONGOING_UI"));

            if ("com.johnnydev.BTN_ANSWER".equals(action)) {
                Intent md = new Intent("com.johnnydev.ACTION_ANSWERED");
                md.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                ctx.sendBroadcast(md);
            }
            return;
        }

        // 3. Incoming Call (Notification Banner + Full Screen Launch)
        Notification notif = new Notification.Builder(ctx, chanId)
            .setSmallIcon(android.R.drawable.sym_action_call)
            .setStyle(Notification.CallStyle.forIncomingCall(caller, decPi, ansPi))
            .setFullScreenIntent(fsPi, true)
            .setCategory(Notification.CATEGORY_CALL)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .build();

        nm.notify(9991, notif);
    }
}
