package com.swijaya.android.janus;

import java.util.EmptyStackException;
import java.util.Stack;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PhoneStateService extends Service {
    
    private static final String TAG = "PhoneStateService";
    private static final int ONGOING_NOTIFICATION = 0x00000001;

    private Stack<PhoneCall> mActiveCalls;
    private PhoneCall mCurrentCall;
    private boolean mActive;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate: service starting");
        mActiveCalls = new Stack<PhoneCall>();
        mCurrentCall = null;
        mActive = false;
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
            // we are restarted after the system had to stop an ongoing
        	// service for some reason
            Log.w(TAG, "onStartCommand: reentrant");
            // not sure how to handle this yet, some sort of state persistence?
        }
        else {
            Bundle extras = intent.getExtras();
            assert (extras != null);
            
            PhoneState phoneState = (PhoneState) extras.getSerializable(PhoneStateReceiver.EXTRA_PHONE_STATE);
            assert (phoneState != null);
            switch (phoneState) {
                case RINGING:
                case OUTGOING:
                    String phoneNumber = extras.getString(PhoneStateReceiver.EXTRA_PHONE_NUMBER);
                    assert (phoneNumber != null);
                    
                    mCurrentCall = new PhoneCall(phoneNumber);
                    Log.d(TAG, "Current call: " + mCurrentCall.toString());
                    
                    // if user is making a call while an active call is ongoing,
                    // we won't re-enter the OFFHOOK state again;
                    // but if the user receives a call while an active call is ongoing,
                    // we WILL re-enter the OFFHOOK state again!
                    if (!mActive || phoneState == PhoneState.RINGING) {
                    	break;
                    }
                case OFFHOOK:
                	// this state might be reentrant on an existing call!
                	// TODO
                    mActiveCalls.push(mCurrentCall);
                    Log.d(TAG, "Current active calls count: " + Integer.toString(mActiveCalls.size()));
                    // move this service on foreground the first time it handles an active call
                    if (mActiveCalls.size() == 1) {
                    	Log.d(TAG, "Starting foreground service");
                    	startForeground(ONGOING_NOTIFICATION, createOngoingCallNotification());
                    }
                    // for additional calls, update the ongoing notification
                    else if (mActiveCalls.size() > 1) {
                    	// TODO
                    }
                    
                    mActive = true;
                    break;
                case IDLE:
                	// this state is reached when all calls are completed
                    Log.d(TAG, "Stopping foreground service");
                    stopForeground(true);
                    
                    mActive = false;
                    drainActiveCalls();
                    
                    Log.d(TAG, "Stopping service");
                    stopService(intent);
                    break;
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
    
    private void drainActiveCalls() {
    	Log.d(TAG, "Draining all active calls");
    	try {
	    	for (PhoneCall call = mActiveCalls.pop(); call != null; call = mActiveCalls.pop()) {
	    		Log.d(TAG, "Stop call: " + call.toString());
	    		// TODO
	    	}
    	} catch (EmptyStackException e) {
    		// done
    	}
    	mCurrentCall = null;
    }

}
