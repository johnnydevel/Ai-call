package com.johnnydev.aicaller;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView statusText;
    private Chronometer callTimer;
    private Button answerBtn;
    private Button declineBtn;
    private LinearLayout dashboardBox;

    private final BroadcastReceiver uiCloseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.johnnydev.CLOSE_ACTIVITY".equals(intent.getAction())) {
                finishAndRemoveTask();
            } else if ("com.johnnydev.SWITCH_ONGOING_UI".equals(intent.getAction())) {
                showOngoingState();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Wake screen and show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
            if (km != null) km.requestDismissKeyguard(this, null);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.johnnydev.CLOSE_ACTIVITY");
        filter.addAction("com.johnnydev.SWITCH_ONGOING_UI");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(uiCloseReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(uiCloseReceiver, filter);
        }

        buildUI();
        handleMode(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleMode(intent);
    }

    private void buildUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(64, 140, 64, 100);

        // One UI Deep Dark Gradient Background
        GradientDrawable bg = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#0F172A"), Color.parseColor("#020617")}
        );
        root.setBackground(bg);

        // Top Badge
        TextView badge = new TextView(this);
        badge.setText("CODER MODE • VOICE LINE");
        badge.setTextColor(Color.parseColor("#38BDF8"));
        badge.setTextSize(12);
        badge.setTypeface(null, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        root.addView(badge);

        // Avatar Circle
        TextView avatar = new TextView(this);
        avatar.setText("AI");
        avatar.setTextColor(Color.WHITE);
        avatar.setTextSize(38);
        avatar.setTypeface(null, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        GradientDrawable avatarShape = new GradientDrawable();
        avatarShape.setShape(GradientDrawable.OVAL);
        avatarShape.setColor(Color.parseColor("#1E293B"));
        avatarShape.setStroke(4, Color.parseColor("#38BDF8"));
        avatar.setBackground(avatarShape);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(260, 260);
        avatarParams.topMargin = 70;
        avatarParams.bottomMargin = 40;
        root.addView(avatar, avatarParams);

        // Caller Name
        TextView name = new TextView(this);
        name.setText("Coder Mode AI");
        name.setTextColor(Color.WHITE);
        name.setTextSize(30);
        name.setTypeface(null, Typeface.BOLD);
        name.setGravity(Gravity.CENTER);
        root.addView(name);

        // Call Status
        statusText = new TextView(this);
        statusText.setText("Incoming Summary Call...");
        statusText.setTextColor(Color.parseColor("#94A3B8"));
        statusText.setTextSize(17);
        statusText.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.topMargin = 16;
        root.addView(statusText, statusParams);

        // Chronometer (Call Timer)
        callTimer = new Chronometer(this);
        callTimer.setTextColor(Color.parseColor("#4ADE80"));
        callTimer.setTextSize(22);
        callTimer.setTypeface(null, Typeface.BOLD);
        callTimer.setGravity(Gravity.CENTER);
        callTimer.setVisibility(View.GONE);
        LinearLayout.LayoutParams timerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        timerParams.topMargin = 20;
        root.addView(callTimer, timerParams);

        // Dashboard Box (For testing when opening the app manually)
        dashboardBox = new LinearLayout(this);
        dashboardBox.setOrientation(LinearLayout.VERTICAL);
        dashboardBox.setGravity(Gravity.CENTER);
        dashboardBox.setPadding(40, 40, 40, 40);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1E293B"));
        cardBg.setCornerRadius(36f);
        dashboardBox.setBackground(cardBg);
        LinearLayout.LayoutParams dashParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dashParams.topMargin = 70;

        Button testCallBtn = createPillButton("Trigger Test Incoming Call", "#2563EB");
        testCallBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, CallTriggerReceiver.class).setAction("com.johnnydev.SHOW_CALL");
            sendBroadcast(i);
        });
        dashboardBox.addView(testCallBtn);

        Button fsSettingsBtn = createPillButton("Full Screen Alert Settings", "#334155");
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        settingsParams.topMargin = 24;
        fsSettingsBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startActivity(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT));
            }
        });
        dashboardBox.addView(fsSettingsBtn, settingsParams);
        root.addView(dashboardBox, dashParams);

        // Spacer to push Call Buttons to bottom
        View spacer = new View(this);
        LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(spacer, spacerParams);

        // Bottom Call Action Row (Decline & Answer)
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setGravity(Gravity.CENTER);

        declineBtn = createCircleCallButton("Decline", "#EF4444");
        declineBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, CallTriggerReceiver.class).setAction("com.johnnydev.BTN_DECLINE");
            sendBroadcast(i);
            finishAndRemoveTask();
        });

        answerBtn = createCircleCallButton("Answer", "#22C55E");
        LinearLayout.LayoutParams ansParams = new LinearLayout.LayoutParams(280, 150);
        ansParams.leftMargin = 60;
        answerBtn.setOnClickListener(v -> {
            Intent i = new Intent(this, CallTriggerReceiver.class).setAction("com.johnnydev.BTN_ANSWER");
            sendBroadcast(i);
            showOngoingState();
        });

        btnRow.addView(declineBtn, new LinearLayout.LayoutParams(280, 150));
        btnRow.addView(answerBtn, ansParams);
        root.addView(btnRow);

        setContentView(root);
    }

    private void handleMode(Intent intent) {
        if (intent != null && "CALL_SCREEN".equals(intent.getStringExtra("mode"))) {
            dashboardBox.setVisibility(View.GONE);
            statusText.setText("Incoming Summary Call...");
        } else if (intent != null && "ONGOING_SCREEN".equals(intent.getStringExtra("mode"))) {
            showOngoingState();
        } else {
            dashboardBox.setVisibility(View.VISIBLE);
            statusText.setText("Ready • Waiting for MacroDroid");
        }
    }

    private void showOngoingState() {
        dashboardBox.setVisibility(View.GONE);
        statusText.setText("Speaking Coder Summary...");
        answerBtn.setVisibility(View.GONE);
        declineBtn.setText("Hang Up");
        callTimer.setBase(SystemClock.elapsedRealtime());
        callTimer.setVisibility(View.VISIBLE);
        callTimer.start();
    }

    private Button createCircleCallButton(String text, String hexColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(16);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(hexColor));
        shape.setCornerRadius(100f);
        b.setBackground(shape);
        return b;
    }

    private Button createPillButton(String text, String hexColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setAllCaps(false);
        b.setPadding(40, 30, 40, 30);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.parseColor(hexColor));
        shape.setCornerRadius(28f);
        b.setBackground(shape);
        return b;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(uiCloseReceiver);
        } catch (Exception ignored) {}
    }
}
