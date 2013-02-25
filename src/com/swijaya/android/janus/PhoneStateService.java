package com.swijaya.android.janus;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PhoneStateService extends Service {
    
    private static final String TAG = "PhoneStateService";

    private PhoneCall currentCall;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate: service starting");
        currentCall = null;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy: service destroyed");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand");

        if (intent == null) {
            // started after the system had to stop an ongoing service for
            // some reason
            Log.w(TAG, "onStartCommand: reentrant");
        }
        else {
            Bundle extras = intent.getExtras();
            assert (extras != null);
            
            int phoneState = extras.getInt(PhoneStateReceiver.EXTRA_PHONE_STATE, 0xffffffff);
            switch (phoneState) {
                case PhoneStateReceiver.EXTRA_STATE_RINGING:
                case PhoneStateReceiver.EXTRA_STATE_OUTGOING:
                    String phoneNumber = extras.getString(PhoneStateReceiver.EXTRA_PHONE_NUMBER);
                    assert (phoneNumber != null);
                    
                    currentCall = new PhoneCall(phoneNumber);
                    Log.d(TAG, "Current call: " + currentCall.toString());
                    break;
                case PhoneStateReceiver.EXTRA_STATE_OFFHOOK:
                    Log.d(TAG, "Starting foreground service");
                    startForeground(currentCall.getID(), createOngoingCallNotification());
                    break;
                case PhoneStateReceiver.EXTRA_STATE_IDLE:
                    Log.d(TAG, "Stopping foreground service");
                    stopForeground(true);
                    currentCall = null;
                    break;            
                default:
                    assert (false);
                    break;
            }
            
            if (currentCall == null) {
                Log.d(TAG, "Stopping service");
                stopService(intent);
            }
        }

        // Unless we are finishing all calls, this service should be sticky. Otherwise,
        // the ever helpful IntentService destroys the current service object after
        // handling, say, EXTRA_STATE_RINGING, and we lose all context about currently active call(s)!
        return Service.START_STICKY;
    }
    
    private Notification createOngoingCallNotification() {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message));
        
        return builder.build();
    }

}
