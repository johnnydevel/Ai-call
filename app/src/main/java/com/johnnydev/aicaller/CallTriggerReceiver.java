package com.johnnydev.aicaller;

import android.app.*;
import android.content.*;

public class CallTriggerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context ctx, Intent intent) {
        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        String action = intent.getAction();

        if ("com.johnnydev.HIDE_CALL".equals(action)) {
            nm.cancel(9991);
            return;
        }

        String chanId = "real_call_ui_v2";
        NotificationChannel chan = new NotificationChannel(chanId, "AI Calls", NotificationManager.IMPORTANCE_HIGH);
        chan.setSound(null, null); // Let MacroDroid handle the ringtone sound
        nm.createNotificationChannel(chan);
        int flags = 201326592;

        Intent ans = new Intent("com.arlosoft.macrodroid.ShortcutTrigger")
            .setPackage("com.arlosoft.macrodroid")
            .putExtra("com.arlosoft.macrodroid.MACRO_NAME", "Answer AI Call");
        PendingIntent ansPi = PendingIntent.getBroadcast(ctx, 101, ans, flags);

        Intent dec = new Intent("com.arlosoft.macrodroid.ShortcutTrigger")
            .setPackage("com.arlosoft.macrodroid")
            .putExtra("com.arlosoft.macrodroid.MACRO_NAME", "Decline AI Call");
        PendingIntent decPi = PendingIntent.getBroadcast(ctx, 102, dec, flags);

        // Explicit full screen intent to wake the screen and show full caller UI
        Intent fs = new Intent(ctx, MainActivity.class);
        fs.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fsPi = PendingIntent.getActivity(ctx, 103, fs, flags);

        Person caller = new Person.Builder().setName("Coder Mode AI").setImportant(true).build();

        if ("com.johnnydev.ONGOING_CALL".equals(action)) {
            Notification ongoing = new Notification.Builder(ctx, chanId)
                .setSmallIcon(android.R.drawable.sym_action_call)
                .setStyle(Notification.CallStyle.forOngoingCall(caller, decPi))
                .setUsesChronometer(true)
                .setOngoing(true)
                .build();
            nm.notify(9991, ongoing);
            return;
        }

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
